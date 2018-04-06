package industries.vocht.wsd_trainingset_creation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Settings {

    private String filename;


    // the store itself, read-only after init
    private Map<String, String> store;


    public Settings(String filename) throws IOException {
        this.filename = filename;
        init();
    }


    private void init() throws IOException {
        store = new HashMap<>();
        byte[] data = Files.readAllBytes(Paths.get(filename));
        String[] fileContents = new String(data, "UTF-8").split("\n");
        for (String line : fileContents) {
            line = line.trim();
            if (!line.startsWith("//") && !line.startsWith("#")) {
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        store.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }
    }

    /**
     * return a value from the store by its key
     *
     * @param key the key
     * @return null if dne, otherwise the value associated with key
     */
    public String getValueByKey(String key) {
        return store.get(key);
    }

}

