package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;

public class Graph implements Serializable {

    /** Makes new Graph with MAPPING. */
    public Graph(HashMap<String, ArrayList<String>> mapping) {
        map = mapping;
    }

    /** Returns MAP. */
    public HashMap<String, ArrayList<String>> getMap() {
        return map;
    }

    /** Variable for MAP. */
    private HashMap<String, ArrayList<String>> map;
}
