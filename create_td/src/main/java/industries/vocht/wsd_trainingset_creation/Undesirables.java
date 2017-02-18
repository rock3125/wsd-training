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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/*
 * Created by peter on 18/03/15.
 *
 * words that can be ignored for searching and indexing
 *
 */
public class Undesirables {

    // set for lookup - read only
    private HashSet<String> undesirableSet;

    public Undesirables() {
        undesirableSet = new HashSet<>();
        undesirableSet.addAll( Arrays.asList(undesirableList) );
        undesirableSet.addAll( Arrays.asList(specialCharacterList) );
    }

    /**
     * get all undesirables as a single list
     * @return the list of undesirables
     */
    public List<String> getAsList() {
        List<String> undesirableList = new ArrayList<>();
        undesirableList.addAll(undesirableSet);
        return undesirableList;
    }

    // return true if the string passed in is null or an undesirable word
    // these are words that shouldn't really form part of any index because
    // of little value and high frequency
    public boolean isUndesirable( String str ) {
        return str == null || undesirableSet.contains(str);
    }

    public final String[] undesirableList = new String[] {
        // articles
        "the", "a", "an",

        // one offs (removed C, the language)
        "not", "b", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
        "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",

        // auxilliaries
        "be", "is", "am", "are", "was", "were", "being", "been",
        "do", "did", "doing", "done", "does",
        "have", "had", "having", "has",

        // conjunctions
        "after", "although", "and", "as", "as far as", "as how", "as if",
        "as long as", "as soon as", "as though", "as well as", "because",
        "before", "both", "but", "either", "even if", "even", "though",
        "for", "how", "however", "if only", "in case", "in order that",
        "neither", "nor", "now", "once", "only", "or", "provided", "rather",
        "than", "since", "so", "so that", "than", "that", "though", "till",
        "unless", "until", "when", "whenever", "where", "whereas", "wherever",
        "whether", "while", "yet",

        // determiners
        "my", "his", "her", "our", "your", "its", "their", "what", "whose", "which",
        "these", "some", "a few", "a little", "all", "another", "any", "both", "each",
        "either", "enough", "every", "few", "fewer", "less", "little", "many", "more",
        "most", "much", "neither", "no", "other", "several",

        // modals
        "can", "can't", "could", "couldn't", "may", "might", "mightn't", "must", "mustn't", "shall",
        "shan't", "should", "shouldn't", "will", "won't", "would", "wouldn't", "ought", "oughtn't",
        "dare", "daren't", "need", "needen't", "had better", "used to",

        // prepositions
        "aboard", "about", "above", "across", "after", "against", "along", "amid", "among", "anti",
        "around", "as", "at", "before", "behind", "below", "beneath", "beside", "besides", "between",
        "beyond", "but", "by", "concerning", "considering", "despite", "down", "during", "except",
        "excepting", "excluding", "following", "for", "from", "in", "inside", "into", "like", "minus",
        "near", "of", "off", "on", "onto", "opposite", "outside", "over", "past", "per", "plus", "regarding",
        "round", "save", "since", "than", "through", "to", "toward", "towards", "under", "underneath",
        "unlike", "until", "up", "upon", "versus", "via", "with", "within", "without", "out", "away",

        // pronouns
        "none", "everything", "all", "any", "another", "anybody", "anyone", "anything", "each", "either",
        "less", "much", "neither", "nothing", "none", "one", "other", "somebody", "someone", "something",
        "both", "few", "many", "others", "several", "more", "most", "some", "you", "your", "yours", "yourself",
        "yourselves", "I", "me", "them", "they", "she", "he", "her", "him", "us", "we", "it", "what", "whatever",
        "which", "whichever", "who", "whoever", "whom", "whomever", "whose", "herself", "himself", "itself",
        "myself", "each other", "everybody", "everyone", "hers", "his", "its", "mine", "no one", "nobody",
        "one another", "ours", "ourselves", "that", "their", "theirs", "themselves", "these", "this", "those",
        "why",

        "n't", "''", "`"
    };

    // characters that are sort of noise and shouldn't be indexed
    public final String[] specialCharacterList = {

        // full stops
        "\u002e", "\u06d4", "\u0701", "\u0702",
        "\ufe12", "\ufe52", "\uff0e", "\uff61",

        "!", "?", ",", ":", ";",
        "_", "%", "$", "#", "@", "^", "&", "*", "(", ")", "^",
        "[", "{", "]", "}", "<", ">", "/", "\\", "=", "+", "|", "\"",

        // single quotes
        "\'", "\u02bc", "\u055a", "\u07f4",
        "\u07f5", "\u2019", "\uff07", "\u2018", "\u201a", "\u201b", "\u275b", "\u275c",

        // double quotes
        //"\u0022", "\u00bb", "\u00ab", "\u07f4", "\u07f5", "\u2019", "\uff07",
        "\u201c", "\u201d", "\u201e", "\u201f", "\u2039", "\u203a", "\u275d",
        "\u276e", "\u2760", "\u276f",

        // hyphens
        "\u002d", "\u207b", "\u208b", "\ufe63", "\uff0d",

        // whitespace and noise
        " ",  "\t",  "\r",  "\n", "\u0008",
        "\ufeff", "\u303f", "\u3000", "\u2420", "\u2408", "\u202f", "\u205f",
        "\u2000", "\u2002", "\u2003", "\u2004", "\u2005", "\u2006", "\u2007",
        "\u2008", "\u2009", "\u200a", "\u200b",
    };

}

