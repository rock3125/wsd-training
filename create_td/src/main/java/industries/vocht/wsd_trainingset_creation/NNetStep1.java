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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by peter on 25/05/16.
 *
 * create training data from a large text set using the semantic lexicon lexicon
 *
 *
 */
public class NNetStep1 {

    public NNetStep1() {
    }

    /**
     * load a set of samples from a training set using the semantic lexicon
     * and write any instance of the training words to their files, pre-collecting
     * windows of training data, for both labelled and un-labelled instances
     *
     * @param trainingSetFileDirectory the training set files base directory (only scans one level deep for *.txt)
     * @param output_directories where to write the resulting files
     * @param maxFileSizeInBytes an optional limit (in bytes) to the generated file size (if <=0, ignored)
     * @param windowSize the size of the windows, try 25
     * @param wordArray a list of focus words (all if null)
     */
    public void create( String dataPath, String trainingSetFileDirectory, String output_directories,
                        long maxFileSizeInBytes, int windowSize, String... wordArray ) throws Exception {

        // setup the open nlp parser
        NLPParser parser = new NLPParser(dataPath);

        System.out.println("step 1: reading each .txt file in " + trainingSetFileDirectory);

        Undesirables undesirables = new Undesirables();
        if ( !output_directories.endsWith("/") ) {
            output_directories += "/";
        }

        // create unlabelled directory
        String nnetUnlabelledDirectory = output_directories + "unlabelled/";
        new File(nnetUnlabelledDirectory).mkdirs();

        // get the ambiguous map sets - from Peter's semantic lexicon
        Map<String, WordnetAmbiguousSet> map = WordnetAmbiguousSet.readFromFile(dataPath);

        // setup what words to look for - these can be filtered to look for a subset for testing
        HashSet<String> focus = new HashSet<>();
        if ( wordArray == null || wordArray.length == 0 ) {
            focus.addAll( map.keySet() ); // all words?
        } else { // or parameters?
            for ( String word : wordArray ) {
                if ( !map.containsKey(word) ) {
                    throw new IOException("unknown focus word \"" + word + "\"");
                }
                focus.add(word);
            }
        }

        // remove words that have already been processed - because this process can be slow
        List<String> toRemove = new ArrayList<>();
        for ( String word : focus ) {
            if ( map.get(word).getWordPlural() == null || !map.get(word).getWordPlural().equals(word) ) {
                if ( new File(outputFilename(nnetUnlabelledDirectory, word)).exists() ) {
                    toRemove.add(word);
                    if ( map.get(word).getWordPlural() != null ) {
                        toRemove.add(map.get(word).getWordPlural());
                    }
                }
            }
        }
        for ( String str : toRemove ) {
            focus.remove(str);
        }

        // do we have anything left to process?
        if (focus.size() == 0) {
            System.out.println("step 1: all items already processed, skipping step 1.");
            return;
        }

        Map<String, PrintWriter> openFileSet = new HashMap<>();
        Map<String, Long> openFileSize = new HashMap<>();

        int lineCounter = 0;

        int minValidSize = (windowSize / 2); // min number of items needed for a valid training set

        // for each file that ends in .txt
        File folder = new File(trainingSetFileDirectory);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.getAbsolutePath().endsWith(".txt")) {

                    System.out.println("parsing and analysing " + file.getAbsolutePath());

                    String textFileContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                    try {
                        List<Sentence> sentenceList = parser.parse(textFileContent);
                        if (sentenceList == null) {
                            System.out.println("empty: " + file.getAbsolutePath());
                            continue;
                        }

                        System.out.println("sentences: " + sentenceList.size() + ", for " + file.getAbsolutePath());

                        for (Sentence sentence : sentenceList ) {

                            // for each token
                            List<Token> tokenList = sentence.getTokenList();
                            int size = tokenList.size();

                            for (int i = 0; i < size; i++) {

                                // is this token / word one of the ambiguous words from Peter's lexicon?
                                Token token = tokenList.get(i);
                                String part = token.getText();
                                String pennType = token.getPennType().toString();
                                if (focus.contains(part.toLowerCase()) && pennType.startsWith("NN")) {
                                    // get the set
                                    WordnetAmbiguousSet set = map.get(part.toLowerCase());

                                    // construct a window left and right of the word
                                    int left = i - windowSize;
                                    if (left < 0) left = 0;
                                    for (int j = left; j < i; j++) {
                                        String punc = tokenList.get(j).getText();
                                        if (punc.equals(".")) {
                                            left = j + 1;
                                        }
                                    }
                                    int right = i + windowSize;
                                    if (right + 1 >= size) {
                                        right = size - 1;
                                    }

                                    // 1.5 window size at least
                                    if (Math.abs(left - right) >= minValidSize) {

                                        // get the singular version
                                        String wordStr = part.toLowerCase();
                                        String plural = set.getWordPlural();
                                        if (plural != null && plural.equals(wordStr)) {
                                            wordStr = set.getWord();
                                        }
                                        Long fileSize = openFileSize.get(wordStr);
                                        PrintWriter writer = openFileSet.get(wordStr);
                                        if (writer == null) {
                                            writer = new PrintWriter(outputFilename(nnetUnlabelledDirectory, wordStr));
                                            openFileSet.put(wordStr, writer);
                                            fileSize = 0L;
                                            openFileSize.put(wordStr, fileSize);
                                        }

                                        // a hit for each syn is counted, we don't want any crossovers between synsets
                                        int count = 0;
                                        StringBuilder sb = new StringBuilder();
                                        int j;
                                        for (j = left; j <= right; j++) {
                                            token = tokenList.get(j);
                                            if (token.isText()) {
                                                String part_j = token.getText().toLowerCase();
                                                if (!undesirables.isUndesirable(part_j)) {
                                                    count = count + 1;
                                                    if (sb.length() > 0) {
                                                        sb.append(",");
                                                    }
                                                    sb.append(part_j);
                                                } // if not undesirable
                                            } else if (token.getText().equals(".")) {
                                                break; // stop collecting at end of sentence events
                                            }
                                        }
                                        // a valid piece of text to collect?
                                        if (count >= minValidSize && (maxFileSizeInBytes <= 0 || fileSize < maxFileSizeInBytes)) {
                                            sb.append("\n");
                                            writer.write(sb.toString());
                                            fileSize = fileSize + sb.length();
                                            openFileSize.put(wordStr, fileSize);
                                        }

                                    } // if window size big enough

                                } // if is focus word

                            } // for each word

                            lineCounter = lineCounter + 1;

                            // display periodic progress
                            if (lineCounter % 100_000 == 0) {
                                System.out.println("   lines processed: " + lineCounter);
                            } // if lineCounter hit

                        }

                    } catch(Exception ex){
                        System.out.println("error parsing file:" + ex.toString());
                    }

                } // io try

            }
        }

        // close all open files
        for ( PrintWriter writer : openFileSet.values() ) {
            writer.close();
        }

    }

    private String outputFilename( String nnetUnlabelledDirectory, String word ) {
        return nnetUnlabelledDirectory + word + "-trainingset.csv";
    }

}


