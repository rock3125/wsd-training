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

import java.security.InvalidParameterException;

/*
 * Created by peter on 21/12/14.
 *
 * a token
 *
 */
public class Token {

    private TokenizerConstants.Type type;   // the type of the token (see tokenizer)
    private String text;                    // token text
    private int synid;                      // synset id if applicable
    private PennType pennType;              // penn tag

    public Token() {
    }

    public Token(TokenizerConstants.Type type, String text) {
        this.type = type;
        this.text = text;
        this.pennType = PennType.UNC;
        this.synid = -1;
    }

    public Token(String text, TokenizerConstants.Type type, PennType pennType) {
        if (text == null || text.length() == 0)
            throw new InvalidParameterException("invalid token (null or empty)");
        if (pennType == null)
            throw new InvalidParameterException("invalid type (null)");

        this.type = type;
        this.text = text;
        this.pennType = pennType;
        this.synid = -1;
    }

    // return true if this token is all text (i.e. no numbers)
    public boolean isText() {
        for (char ch : text.toCharArray()) {
            if ((ch >= '0' && ch <= '9') || ch == '.' || ch ==',') {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return text;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PennType getPennType() {
        return pennType;
    }

    public void setPennType(PennType pennType) {
        this.pennType = pennType;
    }

    public int getSynid() {
        return synid;
    }

    public void setSynid(int synid) {
        this.synid = synid;
    }


    public TokenizerConstants.Type getType() {
        return type;
    }

    public void setType(TokenizerConstants.Type type) {
        this.type = type;
    }
}

