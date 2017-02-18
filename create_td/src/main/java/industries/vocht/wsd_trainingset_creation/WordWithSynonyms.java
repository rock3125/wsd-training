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
import java.util.List;

/**
 * Created by peter on 22/05/16.
 *
 * encapsulate a wordnet syn with its synonyms
 *
 */
public class WordWithSynonyms {

    private String wordStr;
    private List<String> synonymList;

    public WordWithSynonyms() {
        synonymList = new ArrayList<>();
    }

    public WordWithSynonyms(String wordStr, List<String> synonymList ) {
        this.wordStr = wordStr;
        this.synonymList = synonymList;
    }

    // pretty print
    public String toString() {
        String str = wordStr;
        if ( synonymList.size() > 0 ) {
            str = str + ",";
            for ( int i = 0; i < synonymList.size(); i++ ) {
                str = str + synonymList.get(i);
                if ( i + 1 < synonymList.size() ) {
                    str = str + ", ";
                }
            }
        }
        return str;
    }

    public String getWordStr() {
        return wordStr;
    }

    public void setWordStr(String wordStr) {
        this.wordStr = wordStr;
    }

    public List<String> getSynonymList() {
        return synonymList;
    }

    public void setSynonymList(List<String> synonymList) {
        this.synonymList = synonymList;
    }


}
