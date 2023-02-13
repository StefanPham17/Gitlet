package gitlet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;
import java.io.Serializable;

public class Commit implements Serializable {

    /** Makes commit with MESSAGE, PARENT, BLOB. */
    public Commit(String message, String parent, TreeMap<String, String> blob) {
        msg = message;
        parentID = parent;
        selfID = Utils.sha1(Utils.serialize(this));
        blobster = blob;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formation =
                DateTimeFormatter.ofPattern("EEE LLL dd HH:mm:ss yyyy -0800");
        timeStamp = now.format(formation);
        if (message == "initial commit") {
            timeStamp = "Wed Dec 31 16:00:00 1969 -0800";
        }
    }

    /** Returns msg. */
    public String getMessage() {
        return msg;
    }

    /** Returns parentID. */
    public String getParentID() {
        return parentID;
    }

    /** Returns selfID. */
    public String getSelfID() {
        return selfID;
    }

    /** Returns blobster. */
    public TreeMap<String, String> getBlobs() {
        return blobster;
    }

    /** Returns timeStamp. */
    public String getTimeStamp() {
        return timeStamp;
    }

    /** Variable MSG. */
    private String msg;

    /** Variable PARENTID. */
    private String parentID;

    /** Variable SELFID. */
    private String selfID;

    /** Variable BLOBSTER. */
    private TreeMap<String, String> blobster;

    /** Variable TIMESTAMP. */
    private String timeStamp;
}
