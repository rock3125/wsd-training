package industries.vocht.wsd_trainingset_creation;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple OpenNLP wrapper for sentence boundary detection and POS tagging
 *
 *
 */
public class NLPParser {

    private static Logger logger = LoggerFactory.getLogger(NLPParser.class);

    // open-nlp penn-tree tagger
    private POSTaggerME posTagger = null;

    // open-nlp sentence boundary detector
    private SentenceDetectorME sentenceDetector = null;

    // OpenNLP data files location
    private String dataDirectory;


    public NLPParser(String dataDirectory) throws Exception {
        this.dataDirectory = dataDirectory;
        this.init();
    }

    /**
     * sentence split and tokenize a block of text
     *
     * @param text the text to process
     * @return a list of sentences
     */
    public List<Sentence> parse(String text) throws Exception {
        List<Sentence> resultList = new ArrayList<>();
        if ( text != null && text.length() > 0 ) {
            Tokenizer tokenizer = new Tokenizer();

            String[] sentenceList = getSentences(text); // sentence boundary detection
            if (sentenceList != null) {
                // for each "text" sentence
                for (String text_sentence : sentenceList) {

                    // perform a syntactic parse
                    List<Token> words = tokenizer.tokenize(text_sentence);
                    setupTags(words); // use the open-nlp pos tagger to set the penn tags

                    resultList.add(new Sentence(words));
                }
            }
        }
        return resultList;
    }

    /**
     * open nlp sentence boundary detection
     * @param text the text to perform detection on
     * @return a list of sentence strings
     */
    private String[] getSentences(String text) {
        return sentenceDetector.sentDetect(text);
    }

    /**
     * open nlp pos tagger in action
     * @param tokens the
     * @return a list of penn-tags
     */
    private String[] getTags(String[] tokens) {
        return posTagger.tag(tokens);
    }

    /**
     * setup the tags on a token list using open nlp
     * @param tokenList the tokenList to add penn tags to
     */
    private void setupTags(List<Token> tokenList) throws Exception {
        if (tokenList != null && tokenList.size() > 0) {
            String[] words = new String[tokenList.size()];
            for (int i = 0; i < tokenList.size(); i++ ) {
                words[i] = tokenList.get(i).getText();
            }
            String[] tags = getTags(words);
            if (tags != null && tags.length == tokenList.size()) {
                for (int i = 0; i < tokenList.size(); i++ ) {
                    tokenList.get(i).setPennType(PennType.fromString(tags[i]));
                }
            } else {
                throw new Exception("invalid return from open-nlp tagger");
            }
        }
    }

    /**
     * setup openNLP
     * @throws IOException files incorrect/missing
     */
    public void init() throws IOException {

        logger.info("NLPParser: setup from (" + dataDirectory + ")");

        {
            {
                InputStream modelIn = null;
                try {
                    logger.debug("NLPParser: sentence detected = en-sent.bin");
                    // Loading sentence detection model
                    modelIn = new FileInputStream(dataDirectory + "/opennlp/models-1.5/en-sent.bin");
                    final SentenceModel sentenceModel = new SentenceModel(modelIn);
                    modelIn.close();

                    // create the booktrack maximum entropy sentence scanner
                    sentenceDetector = new SentenceDetectorME(sentenceModel);

                } finally {
                    if (modelIn != null) {
                        modelIn.close();
                    }
                }
            }


            InputStream modelIn = null;
            try {
                logger.debug("NLPParser: pos-tagger = en-pos-maxent.bin");
                // Loading tokenizer model
                modelIn = new FileInputStream(dataDirectory + "/opennlp/models-1.5/en-pos-maxent.bin");
                final POSModel posModel = new POSModel(modelIn);
                modelIn.close();

                posTagger = new POSTaggerME(posModel);

            } finally {
                if (modelIn != null) {
                    modelIn.close();
                }
            }
        }

    }



}

