/*
 * Copyright (c) 2016 by Peter de Vocht
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

/*
 * Created by peter on 17/12/14.
 *
 * turn a string into a stream of tokens
 *
 */
public class Tokenizer extends TokenizerConstants {

    public Tokenizer() {
    }

    /**
     * take a string apart into tokens
     * @param str the stirng to take apart
     * @return a list of tokens that makes the string
     */
    public List<Token> tokenize(String str ) {
        if ( str != null && str.length() > 0 ) {
            List<Token> tokenList = new ArrayList<>();

            StringBuilder helper = new StringBuilder();

            char[] chArray = str.toCharArray();
            int length = chArray.length;

            int i = 0;
            while ( i < length ) {
                boolean tokenHandled = false;

                // whitespace scanner
                char ch = chArray[i];
                while ( isWhiteSpace(ch) && i < length ) {
                    tokenHandled = true;
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }

                if ( tokenHandled )
                    tokenList.add( new Token(" ") );

                // add full-stops?
                while ( isFullStop(ch) && i < length ) {
                    tokenHandled = true;
                    tokenList.add(new Token("."));
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }

                // add hyphens?
                while ( isHyphen(ch) && i < length ) {
                    tokenHandled = true;
                    tokenList.add(new Token("-"));
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }

                // add single quotes?
                while ( isSingleQuote(ch) && i < length ) {
                    tokenHandled = true;
                    tokenList.add(new Token("'"));
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }

                // add single quotes?
                while ( isDoubleQuote(ch) && i < length ) {
                    tokenHandled = true;
                    tokenList.add(new Token("\""));
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }

                // add special characters ( ) etc.
                while ( isSpecialCharacter(ch) && i < length ) {
                    tokenHandled = true;
                    tokenList.add(new Token( Character.toString(ch) ));
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }

                // add punctuation ! ? etc.
                while ( isPunctuation(ch) && i < length ) {
                    tokenHandled = true;
                    tokenList.add(new Token( Character.toString(ch) ));
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }

                // numeric processor
                helper.setLength(0);
                while ( isNumeric(ch) && i < length ) {
                    tokenHandled = true;
                    helper.append(ch);
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }
                if ( helper.length() > 0 )
                    tokenList.add( new Token(helper.toString()) );

                // text processor
                helper.setLength(0);
                while ( isABC(ch) && i < length ) {
                    tokenHandled = true;
                    helper.append(ch);
                    i = i + 1;
                    if ( i < length ) ch = chArray[i];
                }
                if ( helper.length() > 0 )
                    tokenList.add( new Token(helper.toString()) );

                // discard unknown token?
                if ( !tokenHandled ) {
                    i++; // skip
                }

            }

            // return the list if we have something
            if ( tokenList.size() > 0 )
                return tokenList;
        }
        return null;
    }


}

