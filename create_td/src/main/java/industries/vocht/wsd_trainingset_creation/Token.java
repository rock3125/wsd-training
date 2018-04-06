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

    private String text;    // token text
    private String tag;     // penn tag

    public Token() {
    }

    public Token(String text) {
        this.text = text;
        this.tag = "UNC";  // unknown tag marker
    }

    public Token(String text, String tag) {
        if (text == null || text.length() == 0)
            throw new InvalidParameterException("invalid token (null or empty)");
        if (tag == null)
            throw new InvalidParameterException("invalid tag (null)");

        this.text = text;
        this.tag = tag;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


}

