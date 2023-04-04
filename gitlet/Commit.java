package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/** This class represents a commit object in gitlet.
 * @author Harshaan Sall */
public class Commit implements Serializable {

    /** message. */
    private final String _message;

    /** Timestamp. */
    private final String _timestamp;

    /** Parent. */
    private final String _commitParent;

    /** Files. */
    private HashMap<String, Blob> _files;

    /** ID. */
    private String uniqueID;

    /** Constructor for commit object. MESSAGE, TIMESTAMP, FILES, PARENT. */
    public Commit(String message, String timestamp,
                  HashMap<String, Blob> files, String parent) {
        this._message = message;
        this._timestamp = timestamp;
        this._files = files;
        this._commitParent = parent;
    }

    /** Sets unique ID. */
    public void setUniqueID() {
        this.uniqueID = Utils.sha1(_message, _timestamp);
    }

    /** Getter for message.
     * @return message. */
    public String getMessage() {
        return this._message;
    }

    /** Getter for timestamp.
     * @return timestamp. */
    public String getTimestamp() {
        return this._timestamp;
    }

    /** Getter for id.
     * @return id. */
    public String getUniqueID() {
        return this.uniqueID;
    }

    /** Getter for files.
     * @return files. */
    public HashMap<String, Blob> getFiles() {
        return this._files;
    }

    /** Getter for file names.
     * @return names. */
    public Set<String> getallFileNames() {
        return this.getFiles().keySet();
    }

    /** Getter for file.
     * @param name name.
     * @return get file. */
    public Blob getFile(String name) {
        return this.getFiles().get(name);
    }

    /** Checks for file.
     * @param name file.
     * @return file. */
    public boolean containsFile(String name) {
        return this.getFiles().containsKey(name);
    }

    /** Getter for contents of a file.
     * @param name name.
     * @return content. */
    public String getFileContents(String name) {
        return this.getFile(name).getContent();
    }
}

