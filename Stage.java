package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class Stage implements Serializable {
    public Stage() {
        additions = new TreeMap<>();
        removals = new ArrayList<>();
    }

    public void add(String name, String id) {
        additions.put(name, id);
    }

    public void remove(String name) {
        removals.add(name);
    }

    public TreeMap<String, String> getAdditions() {
        return additions;
    }

    public ArrayList<String> getRemovals() {
        return removals;
    }

    public void clear() {
        additions = new TreeMap<>();
        removals = new ArrayList<>();
    }

    /** Variable for ADDITIONS. */
    private TreeMap<String, String> additions;

    /** Variable for REMOVALS. */
    private ArrayList<String> removals;
}
