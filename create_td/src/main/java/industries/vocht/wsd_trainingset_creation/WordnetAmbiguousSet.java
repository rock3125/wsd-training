/*
 * Copyright (c) 2017 by Peter de Vocht
 *
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law.
 *
 */

package industries.vocht.wsd_trainingset_creation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by peter on 22/05/16.
 *
 * an encapsulation of Peter's noun lexicon for generating training data for SVMs
 *
 */
public class WordnetAmbiguousSet {

    final static Logger logger = LoggerFactory.getLogger(WordnetAmbiguousSet.class);

    // the noun concerned
    private String word;
    // its optional plural (can be null)
    private String wordPlural;

    // the pretty names of the different syns
    private List<String> setNameList;

    // the set of words for each syn-set
    private List<HashSet<String>> setList;

    // set of words with no luck / training
    private List<HashMap<String, Integer>> unknownList;

    // list of samples for each synset
    private List<List<HashMap<String, Integer>>> sampleSet;

    public WordnetAmbiguousSet() {
        this.setList = new ArrayList<>();
        this.sampleSet = new ArrayList<>();
        this.setNameList = new ArrayList<>();
        this.unknownList = new ArrayList<>();
    }

    public WordnetAmbiguousSet(String word, String wordPlural ) {
        this.word = word;
        this.setWordPlural(wordPlural);
        this.setList = new ArrayList<>();
        this.sampleSet = new ArrayList<>();
        this.setNameList = new ArrayList<>();
        this.unknownList = new ArrayList<>();
    }

    /**
     * re hydrate a sample set from file for nnet processing
     * format: //  comment
     *         1|str1,str2,str3
     *         label | items
     * @param filename the filename of the sample set to load
     * @throws IOException file dne
     */
    public void loadSampleSet( String filename, int dataItemLimit ) throws IOException {
        logger.info("loading training sample " + filename);
        Map<Integer, List<HashMap<String,Integer>>> sampleSet = new HashMap<>();
        int total_size = 0;
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for(String line; (line = br.readLine()) != null; ) {
                if ( !line.startsWith("//") ) {
                    String[] parts = line.split("\\|");
                    if ( parts.length == 2 ) {
                        int label = Integer.parseInt(parts[0]);
                        List<HashMap<String,Integer>> list = sampleSet.get(label);
                        if ( list == null ) {
                            list = new ArrayList<>();
                            sampleSet.put( label, list );
                        }
                        String[] samples = parts[1].split(",");
                        HashMap<String,Integer> map = samplesToMap(samples);
                        if ( map != null ) {
                            list.add(map);
                            total_size += 1;
                        }

                    } // | items
                } // not a comment

                // do we have enough items/stop?
                if (dataItemLimit > 0 && total_size >= dataItemLimit) {
                    break;
                }
            } // for each line
        } // read each line

        // perform a count for information purposes
        int total_sets = sampleSet.size();
        System.out.println("loaded " + total_sets + " labels with a total of " + total_size + " samples for " + filename);

        //put the samples into order into the sample bank
        List<Integer> orderedList = new ArrayList<Integer>();
        orderedList.addAll( sampleSet.keySet() );
        Collections.sort(orderedList);
        this.sampleSet = new ArrayList<>();
        for ( int id : orderedList ) {
            this.sampleSet.add( sampleSet.get(id) );
        }
    }

    /**
     * clear the memory allocated by the sample sets by releasing the reference
     */
    public void clearSampleSet() {
        this.sampleSet = new ArrayList<>();
    }

    /**
     * put all the items in the string array into a count hash map
     * @param items the items to process
     * @return the count map
     */
    private HashMap<String, Integer> samplesToMap( String[] items ) {
        HashMap<String, Integer> map = new HashMap<>();
        for ( String str : items ) {
            Integer value = map.get(str);
            if ( value == null ) {
                value = 1;
            } else {
                value = value + 1;
            }
            map.put(str, value);
        }
        if ( map.size() > 0 ) {
            return map;
        }
        return null;
    }


    // return the minimum set size of the sets in the classes
    public int getMinSampleSize() {
        int size = Integer.MAX_VALUE;
        for ( List<HashMap<String, Integer>> set : sampleSet ) {
            if ( set.size() < size ) {
                size = set.size();
            }
        }
        return size;
    }

    // return the total number of samples in this set for all classes
    public int getSampleSize() {
        int size = 0;
        for ( List<HashMap<String, Integer>> set : sampleSet ) {
            size = size + set.size();
        }
        return size;
    }

    /**
     * convert a line from Peter's noun lexicon to a single syn entry for this noun
     * not used for samples, just creating the syns and pretty print labels that define the syns for this noun
     * @param line the line to convert to a syn entry
     */
    public void addSetLine( String line ) {
        if ( line != null && !line.startsWith("//") ) {
            String[] parts = line.split(",");
            setNameList.add( word + " (" + parts[1] + ")"); // what separates this syn from the others
            HashSet<String> set = new HashSet<>();
            for ( int i = 1; i < parts.length; i++ ) {
                String part = parts[i].trim().toLowerCase();
                if ( !part.equals(word) && (wordPlural == null || !part.equals(wordPlural)) ) {
                    set.add(part);
                }
            }
            setList.add( set );
            while ( setList.size() > sampleSet.size() ) {
                sampleSet.add( new ArrayList<>() );
            }
        }
    }

    /**
     * remove samples from this set to create a training set
     * @param percentSize a number between 1 and 99 for removing training samples percentage wise
     * @return a new training set - and a removed set of training samples
     */
    public WordnetAmbiguousSet createTrainingSet( int percentSize ) {
        WordnetAmbiguousSet trainingSet = new WordnetAmbiguousSet();
        trainingSet.word = this.word;
        trainingSet.wordPlural = this.wordPlural;
        trainingSet.setNameList = this.setNameList;
        trainingSet.setList = this.setList;

        Random random = new Random(System.currentTimeMillis());

        for ( List<HashMap<String, Integer>> classItem : sampleSet ) {
            int numToRemove = classItem.size() / percentSize;
            List<HashMap<String, Integer>> newSet = new ArrayList<>();
            for ( int i = 0; i < numToRemove; i++ ) {
                int index = Math.abs(random.nextInt(classItem.size()));
                newSet.add( classItem.remove(index) );
            }
            trainingSet.sampleSet.add( newSet );
        }
        return trainingSet;
    }


    /**
     * read the set from file - the carefully manually constructed set by PMGDV
     * @return the map of the syn sets
     */
    public static Map<String, WordnetAmbiguousSet> readFromFile(String dataPath) throws IOException {
        Map<String, WordnetAmbiguousSet> map = new HashMap<>();
        List<String> traingList = Files.readAllLines(Paths.get(dataPath + "/lexicon/semantic-nouns.txt"));
        for ( String line : traingList ) {
            if ( line.length() > 0 && !line.startsWith("//") ) {
                String[] parts = line.split(",");
                String part = parts[0];
                String[] items = part.split("\\|");

                String word;
                String wordPlural = null;
                if ( items.length == 2 ) {
                    word = items[0].trim();
                    wordPlural = items[1].trim();
                } else {
                    word = items[0].trim();
                }

                if ( map.containsKey(word) ) {
                    WordnetAmbiguousSet set = map.get(word);
                    set.addSetLine(line);
                } else {
                    WordnetAmbiguousSet set = new WordnetAmbiguousSet(word, wordPlural);
                    map.put(word, set);
                    if ( wordPlural != null ) {
                        map.put(wordPlural, set);
                    }
                    set.addSetLine(line);
                }
            }
        }
        return map;
    }

    public void addSample( int index, HashMap<String, Integer> sample ) {
        sampleSet.get(index).add( sample );
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<HashSet<String>> getSetList() {
        return setList;
    }

    public void setSetList(List<HashSet<String>> setList) {
        this.setList = setList;
    }

    public List<List<HashMap<String, Integer>>> getSampleSet() {
        return sampleSet;
    }

    public void setSampleSet(List<List<HashMap<String, Integer>>> sampleSet) {
        this.sampleSet = sampleSet;
    }

    public String getWordPlural() {
        return wordPlural;
    }

    public void setWordPlural(String wordPlural) {
        this.wordPlural = wordPlural;
    }


    public List<String> getSetNameList() {
        return setNameList;
    }

    public void setSetNameList(List<String> setNameList) {
        this.setNameList = setNameList;
    }

    public List<HashMap<String, Integer>> getUnknownList() {
        return unknownList;
    }

    public void setUnknownList(List<HashMap<String, Integer>> unknownList) {
        this.unknownList = unknownList;
    }

}

