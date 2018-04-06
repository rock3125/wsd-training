package industries.vocht.wsd_trainingset_creation;

import java.io.File;

/**
 * Created by peter on 18/12/16.
 *
 * create all the neural networks from the paths
 *
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // current directory
        String cwd = new java.io.File( "." ).getCanonicalPath();
        String prefix = "/";

        // first get the data directory's location relative to the exe
        String path = cwd + prefix + "data";
        for ( int i = 0; i < 5; i++ ) {
            if (new File(path + "/lexicon/semantic-nouns.txt").exists() ) {
                break;
            } else {
                prefix = prefix + "../";
                path = cwd + prefix + "data";
            }
        }
        if (!new File(path + "/lexicon/semantic-nouns.txt").exists() ) {
            throw new Exception("cannot find data directory with the lexicon relative to this exe");
        }

        if ( args.length != 2 ) {
            System.out.println("Create training data for WSD nnets");
            System.out.println("usage: /path/to/text/files/*.txt|*.gz /output/path/to/write/to");
            System.exit(1);
        }
        Main creator = new Main();

        // numIterations:  the number of times we run the samples through the nnets (not epochs)
        // dataItemLimit:  how many items to read at most for each nnet training set
        creator.create(path, args[0], args[1]);
    }


    private Main() {
    }

    /**
     * Go through the creation steps for the training data
     */
    private void create(String dataPath, String trainingSetFileFolder, String outputDirectoryBase) throws Exception {

        // load the settings
        Settings settings = new Settings(dataPath + "/../wsd.properties");

        new File(outputDirectoryBase).mkdirs(); // create dir if dne

        // surrounding words window size
        int windowSize = Integer.parseInt(settings.getValueByKey("windowSize"));

        // number of items to return for top frequency matches (i.e. top x relations to the noun)
        int collectorCount = Integer.parseInt(settings.getValueByKey("collectorCount"));

        // limit unlabelled files if > 0 to this many bytes
        long maxFileSizeInBytes = Long.parseLong(settings.getValueByKey("maxFileSizeInBytes"));

        // percentage at which labelled sets get split into good and bad to see how well the semantic cloud works for each noun
        double failThreshold = Double.parseDouble(settings.getValueByKey("failThreshold"));

        // step 1.  turn unlabelled data into labelled sets
        // parse the text files, look for nouns that are in the lexicon (see data/lexicon)
        // and start collecting related data
        NNetStep1 step1 = new NNetStep1();
        step1.create(dataPath, trainingSetFileFolder, outputDirectoryBase, maxFileSizeInBytes, windowSize);

        NNetStep2 step2 = new NNetStep2();
        step2.create(dataPath, outputDirectoryBase, failThreshold, collectorCount);
    }




}


