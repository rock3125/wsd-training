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

/**
 * Created by peter on 22/05/16.
 *
 * a word and its homonym frequency
 *
 */
public class WordWithFrequency implements Comparable<WordWithFrequency> {

    private String word;
    private WordWithSynonyms wordWithSynonyms;
    private long frequency;

    public WordWithFrequency() {
    }

    public String toString() {
        if ( word != null ) {
            return word + ":" + frequency;
        } else {
            return wordWithSynonyms.toString();
        }
    }

    public WordWithFrequency(String word, long frequency ) {
        this.word = word;
        this.frequency = frequency;
    }

    public WordWithFrequency(WordWithSynonyms wordWithSynonyms, long frequency ) {
        this.wordWithSynonyms = wordWithSynonyms;
        this.frequency = frequency;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    @Override
    public int compareTo(WordWithFrequency o) {
        if ( frequency < o.frequency ) return -1;
        if ( frequency > o.frequency ) return 1;
        return 0;
    }

    public WordWithSynonyms getWordWithSynonyms() {
        return wordWithSynonyms;
    }

    public void setWordWithSynonyms(WordWithSynonyms wordWithSynonyms) {
        this.wordWithSynonyms = wordWithSynonyms;
    }
}
