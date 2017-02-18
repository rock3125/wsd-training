package industries.vocht.wsd_trainingset_creation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by peter on 19/02/17.
 *
 * a list of tokens, an English sentence
 *
 */
public class Sentence {

    private List<Token> tokenList;

    public Sentence() {
        this.tokenList = new ArrayList<>();
    }

    public Sentence(List<Token> tokenList) {
        this.tokenList = tokenList;
    }


    public List<Token> getTokenList() {
        return tokenList;
    }

    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

}
