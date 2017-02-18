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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by peter on 22/06/16.
 *
 * investigate the training data and collect the top frequency
 * words that can define a single topic.  Then re-create a better word set for testing sets
 * and output a fully labelled data-set using this new data
 *
 */
public class NNetStep2 {

    public NNetStep2() {
    }

    /**
     * create a set of concentrated training labelled items from unlabelled sets
     * use the overlap between unique items to enrol more labels between the sets
     * @param output_directories where to write the resulting files
     * @param failThreshold the % at which to split success samples vs. failed samples (default 66.0)
     * @param collectorCount how many top frequency items to collect / keep (default: 2000)
     * @param wordArray an optional list of words to focus on (can be null)
     * @throws IOException file error
     */
    public void create( String dataPath, String output_directories, double failThreshold,
                        int collectorCount, String... wordArray ) throws IOException {

        System.out.println("step 2: generating labelled data from unlabelled in " + output_directories);

        if ( !output_directories.endsWith("/") ) {
            output_directories += "/";
        }

        // get the ambiguous map sets - from Peter's lexicon
        Map<String, WordnetAmbiguousSet> map = WordnetAmbiguousSet.readFromFile(dataPath);
        Map<String, WordnetAmbiguousSet> originalMap = WordnetAmbiguousSet.readFromFile(dataPath);

        String nnetUnlabelledDirectory = output_directories + "unlabelled/";
        new File(nnetUnlabelledDirectory).mkdirs();
        String labelledTrainingSetDirectory = output_directories + "labelled/";
        new File(labelledTrainingSetDirectory).mkdirs();

        // setup what words to look for
        HashSet<String> focus = new HashSet<>();
        if (wordArray == null || wordArray.length == 0) {
            focus.addAll(map.keySet()); // all words?
        } else { // or parameters?
            for (String word : wordArray) {
                if (!map.containsKey(word)) {
                    throw new IOException("unknown focus word \"" + word + "\"");
                }
                focus.add(word);
            }
        }

        // remove words that don't have training set files or have already been trained
        List<String> toRemove = new ArrayList<>();
        for ( String word : focus ) {
            if ( map.get(word).getWordPlural() == null || !map.get(word).getWordPlural().equals(word) ) {
                if ( !new File(inputFilename(nnetUnlabelledDirectory, word)).exists() ||
                        new File(outputFilename(labelledTrainingSetDirectory, word)).exists() ) {
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
            System.out.println("step 2: all items already processed, skipping step 2.");
            return;
        }

        // process each word in the set
        for ( String word : focus ) {

            // no unlabelled data?
            File current = new File(inputFilename(nnetUnlabelledDirectory, word));
            if ( !current.exists() || current.length() == 0) {
                continue;
            }

            System.out.println("processing word " + word);

            // gather frequencies
            String wordPlural = map.get(word).getWordPlural();
            List<WordWithFrequency> wordWithFrequencyList = gatherFrequencies(
                    inputFilename(nnetUnlabelledDirectory, word), word, wordPlural, collectorCount );

            // now - create the vector lookup map from the top list
            HashSet<String> vectorLookup =
                    wordWithFrequencyList.stream().map(WordWithFrequency::getWord).collect(Collectors.toCollection(HashSet::new));

            // get the success rate for the initial coverage
            getSuccessRate( inputFilename(nnetUnlabelledDirectory, word), word, wordPlural, vectorLookup);


            // rate the set iteratively until it stabalises
            WordnetAmbiguousSet ambiguousSet = map.get(word);
            List<HashSet<String>> set = ambiguousSet.getSetList();
            List<HashSet<String>> originalSet = originalMap.get(word).getSetList();
            rateSet(nnetUnlabelledDirectory, word, wordPlural, set, originalSet);

            int iteration = 1;
            boolean stable;
            do {

                System.out.println(word + ":iteration " + iteration);
                iteration = iteration + 1;

                // calculate the success rate given Peter's lexicon
                List<HashSet<String>> collectionSet = new ArrayList<>();
                for ( int i = 0; i < set.size(); i++ ) {
                    collectionSet.add(new HashSet<>());
                }

                // re-read the unlabelled set and check its new success rate
                try (BufferedReader br = new BufferedReader(new FileReader(inputFilename(nnetUnlabelledDirectory, word)))) {

                    // for each line in the wiki training set
                    for (String line; (line = br.readLine()) != null; ) {

                        // split csv
                        if (line.length() > 0) {

                            // find unique items
                            int[] counts = new int[set.size()];
                            String[] parts = line.split(",");
                            for (String part : parts) {
                                if (part.compareToIgnoreCase(word) != 0 && (wordPlural == null || wordPlural.compareToIgnoreCase(word) != 0)) {
                                    for (int i = 0; i < set.size(); i++) {
                                        HashSet<String> hs = set.get(i);
                                        if (hs.contains(part.toLowerCase().trim())) {
                                            counts[i] = counts[i] + 1;
                                        }
                                    }
                                }
                            } // for each part of the csv

                            // is this a mono example?
                            int index = getBestIndex(counts);
                            if (index >= 0) {

                                // collect all of the items that aren't part of this set - as they might be
                                HashSet<String> extraItems = new HashSet<>();
                                for (String part : parts) {
                                    String key = part.toLowerCase().trim();
                                    if (key.compareToIgnoreCase(word) != 0 && (wordPlural == null || wordPlural.compareToIgnoreCase(key) != 0)) {
                                        int numSets = 0;
                                        for (int i = 0; i < set.size(); i++) {
                                            HashSet<String> hs = set.get(i);
                                            if ( !hs.contains(key) ) {
                                                numSets = numSets + 1;
                                            }
                                        }
                                        if ( numSets == set.size() ) { // none of the sets contained it
                                            extraItems.add(key);
                                        }
                                    }
                                } // for each part of the csv

                                // add these extra items to the collection sets for the unique items
                                collectionSet.get(index).addAll(extraItems);
                            }

                        } // if line not empty

                    } // read a line at a time

                } // try for each line

                // the collection sets are now to be filtered by unique items for each set
                // to acquire new "learning" pattern items
                stable = true;
                collectionSet = filterDuplicates(collectionSet);
                for (int i = 0; i < collectionSet.size(); i++) {
                    if ( collectionSet.get(i).size() > 0 ) {
                        stable = false;
                    }
                    set.get(i).addAll(collectionSet.get(i));
                }

            } while (!stable);




            // rate the set for the last time
            String resultStr = rateSet(nnetUnlabelledDirectory, word, wordPlural, set, originalSet);
            double successRate = rateSetForScore(nnetUnlabelledDirectory, word, wordPlural, set, originalSet);

            // output examples with labels for the second training set
            try ( BufferedReader br = new BufferedReader(new FileReader(inputFilename(nnetUnlabelledDirectory, word)) ) ) {

                PrintWriter writer2 = null;

                PrintWriter writer = new PrintWriter(outputFilename(labelledTrainingSetDirectory, word));
                if ( successRate < failThreshold ) {
                    writer2 = new PrintWriter(outputFilenameFail(labelledTrainingSetDirectory, word));
                }
                writer.write(resultStr);

                // for each line in the wiki training set
                for (String line; (line = br.readLine()) != null; ) {

                    // split csv
                    if ( line.length() > 0 ) {

                        int[] counts = new int[set.size()];
                        String[] parts = line.split(",");
                        for (String part : parts) {
                            String key = part.toLowerCase().trim();
                            for ( int i = 0; i < set.size(); i++ ) {
                                if (set.get(i).contains(key) ) {
                                    counts[i] = counts[i] + 1;
                                }
                            }
                        } // for each part of the csv

                        int index = getBestIndex(counts);
                        StringBuilder sb = new StringBuilder();
                        sb.append(index).append("|");
                        int counter = 0;
                        for (String part : parts) {
                            String key = part.toLowerCase().trim();
                            if ( counter > 0 ) {
                                sb.append(",");
                            }
                            sb.append(key);
                            counter = counter + 1;
                        }
                        sb.append("\n");
                        if ( index >= 0 ) {
                            writer.write(sb.toString());
                        } else if ( writer2 != null ) {
                            writer2.write(sb.toString());
                        }

                    } // if line not empty

                } // read a line at a time

                writer.close();
                if ( writer2 != null ) {
                    writer2.close();
                }

            } // try for each line


            // get the top collectorSize words for the failed training set if it
            // was less than a threshold
            if ( successRate < failThreshold ) {
                List<WordWithFrequency> failedList = gatherFrequencies(
                        outputFilenameFail(labelledTrainingSetDirectory, word), word, wordPlural, collectorCount);
                if ( failedList != null ) {
                    PrintWriter writer = new PrintWriter(outputFilenameFailFrequencies(labelledTrainingSetDirectory, word));
                    for ( WordWithFrequency wwf : failedList ) {
                        writer.write(wwf.getWord() + "\n");
                    }
                    writer.close();
                }
            }



        } // for each word


    }

    /**
     * get the best winning count - if there is one winning item (one largest with no equals)
     * return its index, otherwise return -1 (fail)
     * @param counts the count array to check
     * @return -1 on fail, or the index of the largest non-zero item where there is only one
     */
    private int getBestIndex( int[] counts ) {
        int numLargerThanZero = 0;
        int bestCount = -1;
        int index = -1;
        for ( int i = 0; i < counts.length; i++ ) {
            int count = counts[i];
            if ( count > bestCount ) {
                bestCount = count;
                numLargerThanZero = 1;
                index = i;
            } else  if ( count == bestCount ) {
                numLargerThanZero = numLargerThanZero + 1;
            }
        }
        if ( numLargerThanZero == 1 ) {
            return index;
        }
        return -1;
    }

    // rate a set for matches and return the success score as a percentage 0..100 (double)
    private String rateSet( String nnetUnlabelledDirectory, String word, String wordPlural,
                            List<HashSet<String>> set, List<HashSet<String>> originalSet ) throws IOException {
        // test the accuracy of the set(s)
        int numMatches = 0;
        int numNotMatches = 0;
        int numAmbiguous = 0;
        try ( BufferedReader br = new BufferedReader(new FileReader(inputFilename(nnetUnlabelledDirectory, word)) ) ) {

            // for each line in the wiki training set
            for (String line; (line = br.readLine()) != null; ) {

                // split csv
                if ( line.length() > 0 ) {

                    // find unique items
                    int[] counts = new int[set.size()];
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        if ( part.compareToIgnoreCase(word) != 0 && ( wordPlural == null || wordPlural.compareToIgnoreCase(word) != 0 ) ) {
                            for ( int i = 0; i < set.size(); i++ ) {
                                HashSet<String> hs = set.get(i);
                                if ( hs.contains(part.toLowerCase().trim()) ) {
                                    counts[i] = counts[i] + 1;
                                }
                            }
                        }
                    } // for each part of the csv

                    // is this a mono example?
                    int numLargerThanZero = 0;
                    int bestCount = -1;
                    for ( int i = 0; i < counts.length; i++ ) {
                        int count = counts[i];
                        if ( count > bestCount ) {
                            bestCount = count;
                            numLargerThanZero = 1;
                        } else  if ( count == bestCount ) {
                            numLargerThanZero = numLargerThanZero + 1;
                        }
                    }

                    // exactly one?
                    if ( numLargerThanZero == 1 ) {
                        numMatches = numMatches + 1;
                    } else if ( numLargerThanZero == 0 ) {
                        numNotMatches = numNotMatches + 1;
                    } else if ( numLargerThanZero > 1 ) {
                        numAmbiguous = numAmbiguous + 1;
                    }

                } // if line not empty

            } // read a line at a time

        } // try for each line


        int total = numMatches + numNotMatches + numAmbiguous;
        StringBuilder sb = new StringBuilder();
        sb.append("// " + word + ":========================================================================\n");
        sb.append("// " + word + ":matched:" + numMatches + ", ambiguous:" + numAmbiguous + "\n");
        sb.append("// " + word + ":matched rate  :" + ((double)(numMatches * 100) / (double)total) + "\n");
        sb.append("// " + word + ":non match rate:" + ((double)(numAmbiguous * 100) / (double)total) + "\n");
        sb.append("// syns\n");
        int counter = 0;
        for ( HashSet<String> setItem : originalSet ) {
            sb.append("// ").append(counter).append(": ");
            for ( String str : setItem ) {
                sb.append(str).append(",");
            }
            sb.setLength( sb.length() - 1);
            sb.append("\n");
            counter = counter + 1;
        }

        counter = 0;
        for ( HashSet<String> setItem : set ) {
            sb.append("// new ").append(counter).append(": ");
            for ( String str : setItem ) {
                sb.append(str).append(",");
            }
            sb.setLength( sb.length() - 1);
            sb.append("\n");
            counter = counter + 1;
        }

        sb.append("// " + word + ":========================================================================\n");

        System.out.println(word + ":========================================================================");
        System.out.println(word + ":matched:" + numMatches + ", ambiguous:" + numAmbiguous);
        System.out.println(word + ":matched rate  :" + ((double)(numMatches * 100) / (double)total));
        System.out.println(word + ":non match rate:" + ((double)(numAmbiguous * 100) / (double)total));
        System.out.println(word + ":========================================================================");

        return sb.toString();
    }

    // rate a set for matches and return the success score as a percentage 0..100 (double)
    private double rateSetForScore( String nnetUnlabelledDirectory, String word, String wordPlural,
                                    List<HashSet<String>> set, List<HashSet<String>> originalSet ) throws IOException {
        // test the accuracy of the set(s)
        int numMatches = 0;
        int numNotMatches = 0;
        int numAmbiguous = 0;
        try ( BufferedReader br = new BufferedReader(new FileReader(inputFilename(nnetUnlabelledDirectory, word)) ) ) {

            // for each line in the wiki training set
            for (String line; (line = br.readLine()) != null; ) {

                // split csv
                if ( line.length() > 0 ) {

                    // find unique items
                    int[] counts = new int[set.size()];
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        if ( part.compareToIgnoreCase(word) != 0 && ( wordPlural == null || wordPlural.compareToIgnoreCase(word) != 0 ) ) {
                            for ( int i = 0; i < set.size(); i++ ) {
                                HashSet<String> hs = set.get(i);
                                if ( hs.contains(part.toLowerCase().trim()) ) {
                                    counts[i] = counts[i] + 1;
                                }
                            }
                        }
                    } // for each part of the csv

                    // is this a mono example?
                    int numLargerThanZero = 0;
                    int bestCount = -1;
                    for ( int i = 0; i < counts.length; i++ ) {
                        int count = counts[i];
                        if ( count > bestCount ) {
                            bestCount = count;
                            numLargerThanZero = 1;
                        } else  if ( count == bestCount ) {
                            numLargerThanZero = numLargerThanZero + 1;
                        }
                    }

                    // exactly one?
                    if ( numLargerThanZero == 1 ) {
                        numMatches = numMatches + 1;
                    } else if ( numLargerThanZero == 0 ) {
                        numNotMatches = numNotMatches + 1;
                    } else if ( numLargerThanZero > 1 ) {
                        numAmbiguous = numAmbiguous + 1;
                    }

                } // if line not empty

            } // read a line at a time

        } // try for each line


        int total = numMatches + numNotMatches + numAmbiguous;
        return ((double)(numMatches * 100) / (double)total);
    }

    // filter out items that aren't unique and return the filters
    private List<HashSet<String>> filterDuplicates( List<HashSet<String>> set ) {

        List<HashSet<String>> collectionSet = new ArrayList<>();
        for ( int i = 0; i < set.size(); i++ ) {
            collectionSet.add(new HashSet<>());
        }

        for ( int i = 0; i < set.size(); i++ ) {

            for ( String str : set.get(i) ) {

                boolean found = false;
                for ( int j = 0; j < set.size(); j++ ) {
                    if ( i != j ) {
                        if ( set.get(j).contains(str) ) {
                            found = true;
                        }
                    }
                }

                if ( !found ) {
                    collectionSet.get(i).add( str );
                }
            }
        }
        return collectionSet;
    }

    // increment the frequency for a word
    private void collectFrequency( String word, Map<String, Integer> map ) {
        if ( word != null && word.length() > 2 && map != null ) {
            String wordStr = word.toLowerCase().trim();
            if ( !wordStr.equals("null") ) {
                Integer value = map.get(wordStr);
                if (value == null) {
                    map.put(wordStr, 1);
                } else {
                    map.put(wordStr, value + 1);
                }
            }
        }
    }


    private String inputFilename( String nnetUnlabelledDirectory, String word ) {
        return nnetUnlabelledDirectory + word + "-trainingset.csv";
    }

    private String outputFilename( String labelledTrainingSetDirectory, String word ) {
        return labelledTrainingSetDirectory + word + "-labelled-trainingset.csv";
    }

    private String outputFilenameFail( String labelledTrainingSetDirectory, String word ) {
        return labelledTrainingSetDirectory + word + "-ambiguous-trainingset.csv";
    }

    private String outputFilenameFailFrequencies( String labelledTrainingSetDirectory, String word ) {
        return labelledTrainingSetDirectory + word + "-fail-frequencies-trainingset.csv";
    }


    /**
     * gather the top collectorCount items with frequencies for word / wordPlural
     * @param filename the file to read for frequency items (text file, csv)
     * @param word the word to ignore, the original focus word
     * @param wordPlural (optional, can be null), the plural of word
     * @param collectorCount the number of top frequencies maximum to return (or zero for no limites)
     * @return a list of word frequencies
     * @throws IOException
     */
    private List<WordWithFrequency> gatherFrequencies( String filename, String word, String wordPlural, int collectorCount ) throws IOException {
        // gather frequencies
        Map<String, Integer> frequencyMap = new HashMap<>();

        // read line by line
        // open the wiki set (plain text) for reading
        try ( BufferedReader br = new BufferedReader(new FileReader(filename) ) ) {

            // for each line in the wiki training set
            for (String line; (line = br.readLine()) != null; ) {

                // split csv
                if ( line.length() > 0 ) {
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        // collect frequencies of all words, but not the original word itself
                        if ( part.compareToIgnoreCase(word) != 0 && ( wordPlural == null || wordPlural.compareToIgnoreCase(word) != 0 ) ) {
                            collectFrequency(part, frequencyMap);
                        }
                    } // for each part of the csv

                } // if line not empty

            } // read a line at a time

        } // try for each line

        // analyse the frequency map and cut it down to collectorCount size if its > 0
        List<WordWithFrequency> wordWithFrequencyList = new ArrayList<>();
        for ( String key : frequencyMap.keySet() ) {
            Integer value = frequencyMap.get(key);
            wordWithFrequencyList.add( new WordWithFrequency(key, value.longValue() ) );
        }
        Collections.sort(wordWithFrequencyList);
        Collections.reverse(wordWithFrequencyList);
        if ( collectorCount > 0 && wordWithFrequencyList.size() > collectorCount ) {
            wordWithFrequencyList = wordWithFrequencyList.subList(0, collectorCount);
        }
        return wordWithFrequencyList;
    }


    /**
     * score / output the success rate of a training set
     * @param filename the file to read for frequency items (text file, csv)
     * @param word the word to ignore, the original focus word
     * @param wordPlural (optional, can be null), the plural of word
     * @param vectorLookup the vector of top words
     * @throws IOException
     */
    private void getSuccessRate(String filename, String word, String wordPlural, HashSet<String> vectorLookup) throws IOException {
        // read line by line
        // open the unlabelled set for re-reading
        int numSuccess = 0;
        int numFailed = 0;
        try ( BufferedReader br = new BufferedReader(new FileReader(filename) ) ) {

            // for each line in the unlabelled training set
            for (String line; (line = br.readLine()) != null; ) {

                // split csv
                if ( line.length() > 0 ) {
                    String[] parts = line.split(",");
                    boolean found = false;
                    for (String part : parts) {
                        if ( part.compareToIgnoreCase(word) != 0 && ( wordPlural == null || wordPlural.compareToIgnoreCase(word) != 0 ) ) {
                            if ( vectorLookup.contains(part) ) {
                                found = true;
                                break;
                            }
                        }
                    } // for each part of the csv
                    if ( found ) {
                        numSuccess++;
                    } else {
                        numFailed++;
                    }

                } // if line not empty

            } // read a line at a time

        } // try for each line

        // output success / fail ration
        System.out.println(word + ":top " + vectorLookup.size() + " success rate");

        int total = numFailed + numSuccess;
        System.out.println(word + ":success:" + numSuccess + ", failed:" + numFailed);
        System.out.println(word + ":success rate:" + ((double)(numSuccess * 100) / (double)total));
        System.out.println(word + ":fail rate:" + ((double)(numFailed * 100) / (double)total));
    }

}

