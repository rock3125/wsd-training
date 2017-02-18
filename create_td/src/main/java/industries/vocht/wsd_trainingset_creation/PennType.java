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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;

/*
 * Created by peter on 15/02/15.
 *
 * penn based tag library
 *
 */
public enum PennType {

    Unused0,
    UNC, // not known

    CC, // Coordinating conjunction ('and', 'but', 'nor', 'or', 'yet')
    CD, // Cardinal number
    DT, // Determiner (the articles a(n), every, no and the)
    EX, // Existential there
    FW, // Foreign word ('bete noire' and 'persona non grata' should be tagged bete/FW noire/FW and persona/FW non/FW grata/FW)
    IN, // Preposition or subordinating conjunction
    JJ, // Adjective (also hyphenated, e.g. happy-go-lucky/JJ)
    JJR, // Adjective, comparative (bigger)
    JJS, // Adjective, superlative (biggest)
    LS, // List item marker (letters and numerals when they are used to identify items in a list)
    MD, // Modal (This category includes all verbs that don't take an -s ending in the third person singular present: 'can', 'could,' '(dare)', 'may', 'might', 'must', 'ought', 'shall', 'should', 'will', 'would')
    NN, // Noun, singular or mass (tiger, chair, laughter)
    NNS, // Noun, plural
    NNP, // Proper noun, singular
    NNPS, // Proper noun, plural
    PDT, // Predeterminer
    POS, // Possessive ending
    PRP, // Personal pronoun (('I', 'me', 'you', 'he', 'him')
    PRPS, // Possessive pronoun ('my', 'your', 'his', 'her', 'its', 'our' and 'their')
    RB, // Adverb (most words that end in -ly as well as degree words like quite, too and very)
    RBR, // Adverb, comparative (Adverbs with the comparative ending -er but without a strictly comparative meaning)
    RBS, // Adverb, superlative
    RP, // Particle (This category includes a number of mostly monosyllabic words that also double as directional adverbs and prepositions)
    SYM, // Symbol (mathematical, scientific and technical symbols or expressions that aren't words of English)
    TO, // to
    UH, // Interjection
    VB, // verb, base form, e.g. to think
    VBD, // Verb, past tense (they thought)
    VBG, // Verb, gerund or present participle (thinking is fun)
    VBN, // Verb, past participle (e.g. a sunken ship)
    VBP, // Verb, non­3rd person singular present, "I think"
    VBZ, // Verb, 3rd person singular present, verb, 3rd person singular present "she thinks"
    WDT, // Wh­determiner (This category includes which, as well as that when it is used as a relative pronoun) (was dtq) (which, whatever, whichever)
    WP, // Wh­pronoun (This category includes 'what', 'who' and 'whom')
    WPS, // Possessive wh­pronoun (the wh-word whose, whosever)
    WRB, // Wh­adverb ('how', 'where', 'why', 'when')

    // replacement values for symbol based tags on Penn
    LRB, // left brackets [ { (
    RRB, // right brackets ] } )
    PUN, // punctuation . ,

    SQT, // start and end quote
    EQT,

    X, // bad bracketing - uncertain

    // phrase types
    ADJP, // Adjective Phrase. Phrasal category headed by an adjective (including comparative and superlative adjectives). Example: outrageously expensive.
    ADVP, // Adverb Phrase. Phrasal category headed by an adverb (including comparative and superlative adverbs). Examples: rather timidly, very well indeed, rapidly.
    CONJP, // Conjunction Phrase. Used to mark certain "multi-word" conjunctions, such as 'as well as', 'instead of'.
    NP, // Noun Phrase.  Phrasal category that includes all constituents that depend on a head noun.FRAG, // Fragment.
    PP, // Prepositional Phrase. Phrasal category headed by a preposition.
    QP, // Quantifier Phrase (i.e. complex measure/amount phrase); used within NP.
    S, // Simple declarative clause. One that is not introduced by a (possible empty) subordinating conjunction or a wh-word and that does not exhibit subject-verb inversion.
    SBAR, // Clause introduced by a (possibly empty) subordinating conjunction. Direct question introduced by a wh-word or a wh-phrase. Indirect questions and relative clauses should be bracketed as SBAR, not SBARQ.
    SINV, // Inverted declarative sentence.  One in which the subject follows the tensed verb or modal.
    SQ, // Inverted yes/no question, or main clause of a wh-question, following the wh-phrase in SBARQ.
    SBARQ,
    UCP, // Unlike Coordinated Phrase
    VP, // Verb Phrase.  Phrasal category headed a verb.
    WHADJP, // Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in 'how hot'.
    WHADVP, // Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing a wh-adverb such as 'how' or 'why'.
    WHNP, // Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing some wh-word, e.g. 'who', 'which book', 'whose daughter', 'none of which', or 'how many leopards'.
    WHPP, // Wh-prepositional Phrase.  Prepositional phrase containing a wh-noun phrase (such as 'of which' or 'by whose authority') that either introduces a PP gap or is contained by a WHNP.
    ROOT, // the root of a parse tree
    PRN, // Parenthetical
    PRT, // Particle. Category for words that should be tagged RP.

    FRAG, // fragment
    NP_TMP,
    RRC,
    NX,
    NAC,
    LST,

    SP, // space
    SPACYERR, // spacy bad parsing

    LastValue;


    // convert a string tag to an enumerated value
    public static PennType fromString( String str ) {
        if ( str != null ) {
            String lowerStr = str.toLowerCase();
            switch ( lowerStr ) {
                case "cc": return PennType.CC;
                case "cd": return PennType.CD;
                case "dt": return PennType.DT;
                case "ex": return PennType.EX;
                case "fw": return PennType.FW;
                case "in": return PennType.IN;
                case "jj": return PennType.JJ;
                case "jjr": return PennType.JJR;
                case "jjs": return PennType.JJS;
                case "ls": return PennType.LS;
                case "md": return PennType.MD;
                case "nn": return PennType.NN;
                case "nns": return PennType.NNS;
                case "nnp": return PennType.NNP;
                case "nnps": return PennType.NNPS;
                case "pdt": return PennType.PDT;
                case "pos": return PennType.POS;
                case "prp": return PennType.PRP;
                case "prp$":
                case "prps": return PennType.PRPS;
                case "rb": return PennType.RB;
                case "rbr": return PennType.RBR;
                case "rbs": return PennType.RBS;
                case "rp": return PennType.RP;
                case "sym": return PennType.SYM;
                case "to": return PennType.TO;
                case "intj":
                case "uh": return PennType.UH;
                case "vb": return PennType.VB;
                case "vbd": return PennType.VBD;
                case "vbg": return PennType.VBG;
                case "vbn": return PennType.VBN;
                case "vbp": return PennType.VBP;
                case "vbz": return PennType.VBZ;
                case "wdt": return PennType.WDT;
                case "wp": return PennType.WP;
                case "wp$":
                case "wps": return PennType.WPS;
                case "wrb": return PennType.WRB;

                case "rsb": // square bracket
                case "-rsb-": // square bracket
                case "rcb": // curly bracket
                case "-rcb-": // curly bracket
                case "rrb": // round bracket
                case "-rrb-": return PennType.RRB;

                case "-lsb-": // square bracket
                case "lsb": // square bracket
                case "-lcb-": // curly bracket
                case "lcb": // curly bracket
                case "lrb": // round bracket
                case "-lrb-": return PennType.LRB;

                case "np-tmp": return PennType.NP_TMP;

                case "pun":
                case "#":
                case ":":
                case ";":
                case ",":
                case "!":
                case "-":
                case "hyph":
                case "?":
                case "$":
                case ".": return PennType.PUN;

                case "``":
                case "''":
                case "`":
                case "'":
                case "\"":
                case "sqt": return PennType.SQT;
                case "eqt": return PennType.EQT;

                case "x":
                case "xx":
                    return PennType.X;

                case "sp":
                case " ":
                    return PennType.SP;

                case "adjp": return PennType.ADJP;
                case "advp": return PennType.ADVP;
                case "conjp": return PennType.CONJP;
                case "np": return PennType.NP;
                case "vp": return PennType.VP;
                case "pp": return PennType.PP;
                case "qp": return PennType.QP;
                case "s": return PennType.S;
                case "sq": return PennType.SQ;
                case "sbarq": return PennType.SBARQ;
                case "sbar": return PennType.SBAR;
                case "sinv": return PennType.SINV;
                case "ucp": return PennType.UCP;
                case "whadjp": return PennType.WHADJP;
                case "whadvp": return PennType.WHADVP;
                case "whnp": return PennType.WHNP;
                case "whpp": return PennType.WHPP;
                case "root": return PennType.ROOT;
                case "prn": return PennType.PRN; // Parenthetical
                case "frag": return PennType.FRAG;
                case "prt": return PennType.PRT; // Particle. Category for words that should be tagged RP.
                case "rrc": return PennType.RRC;
                case "nx": return PennType.NX;
                case "nac": return PennType.NAC;
                case "lst": return PennType.LST;

                case "add":
                case "afx":
                case "gw":
                case "bes":
                case "hvs":
                case "nfp": return PennType.SPACYERR;

                default: {
                    logger.error("invalid tag str '" + str + "'");
                    return PennType.UNC;
                }

            }
        }
        throw new InvalidParameterException("invalid tag (null)");
    }

    // convert a string tag to an enumerated value
    public static PennType fromInt( int index ) {
        switch ( index ) {
            case 0: return PennType.Unused0;
            case 1: return PennType.UNC;
            case 2: return PennType.CC;
            case 3: return PennType.CD;
            case 4: return PennType.DT;
            case 5: return PennType.EX;
            case 6: return PennType.FW;
            case 7: return PennType.IN;
            case 8: return PennType.JJ;
            case 9: return PennType.JJR;
            case 10: return PennType.JJS;
            case 11: return PennType.LS;
            case 12: return PennType.MD;
            case 13: return PennType.NN;
            case 14: return PennType.NNS;
            case 15: return PennType.NNP;
            case 16: return PennType.NNPS;
            case 17: return PennType.PDT;
            case 18: return PennType.POS;
            case 19: return PennType.PRP;
            case 20: return PennType.PRPS;
            case 21: return PennType.RB;
            case 22: return PennType.RBR;
            case 23: return PennType.RBS;
            case 24: return PennType.RP;
            case 25: return PennType.SYM;
            case 26: return PennType.TO;
            case 27: return PennType.UH;
            case 28: return PennType.VB;
            case 29: return PennType.VBD;
            case 30: return PennType.VBG;
            case 31: return PennType.VBN;
            case 32: return PennType.VBP;
            case 33: return PennType.VBZ;
            case 34: return PennType.WDT;
            case 35: return PennType.WP;
            case 36: return PennType.WPS;
            case 37: return PennType.WRB;

            case 38: return PennType.LRB;
            case 39: return PennType.RRB;

            case 40: return PennType.PUN;

            case 41: return PennType.SQT;
            case 42: return PennType.EQT;

            case 43: return PennType.X;

            case 44: return PennType.ADJP;
            case 45: return PennType.ADVP;
            case 46: return PennType.CONJP;
            case 47: return PennType.NP;
            case 56: return PennType.VP;
            case 48: return PennType.PP;
            case 49: return PennType.QP;
            case 50: return PennType.S;
            case 53: return PennType.SQ;
            case 54: return PennType.SBARQ;
            case 51: return PennType.SBAR;
            case 52: return PennType.SINV;
            case 55: return PennType.UCP;
            case 57: return PennType.WHADJP;
            case 58: return PennType.WHADVP;
            case 59: return PennType.WHNP;
            case 60: return PennType.WHPP;
            case 61: return PennType.ROOT;
            case 62: return PennType.PRN;
            case 63: return PennType.PRT;
            case 64: return PennType.FRAG;
            case 65: return PennType.NP_TMP;
            case 66: return PennType.RRC;
            case 67: return PennType.NX;
            case 68: return PennType.NAC;
            case 69: return PennType.LST;

            default: {
                logger.error("invalid tag id '" + index + "'");
                return PennType.UNC;
            }
        }
    }

    // is this text an aux verb?
    public static boolean isAux( String str ) {
        return str != null && auxVerbSet.contains(str.toLowerCase());
    }

    // the auxiliary verbs
    private static String[] auxVerbArray = new String[] {
            "be", "am", "is", "are", "was", "were", "being", "been",
            "do", "does", "did", "doing", "done",
            "have", "has", "had", "having", "had"
    };

    private static HashSet<String> auxVerbSet = new HashSet<>(50);

    // setup a lookup for aux
    static
    {
        auxVerbSet.addAll(Arrays.asList(auxVerbArray) );
    }

    final static Logger logger = LoggerFactory.getLogger(PennType.class);
}

