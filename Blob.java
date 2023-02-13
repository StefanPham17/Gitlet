package gitlet;

import java.io.Serializable;
import java.io.File;

public class Blob implements Serializable {

    /** Makes new blob with FILE. */
    public Blob(File file) {
        self = Utils.readContentsAsString(file);
        selfID = Utils.sha1(Utils.serialize(this));
    }

    /** Returns selfID. */
    public String getSelfID() {
        return selfID;
    }

    /** Returns self. */
    public String getSelf() {
        return self;
    }

    /** Identifier variable. */
    private String selfID;

    /** Is itself. */
    private String self;
}
