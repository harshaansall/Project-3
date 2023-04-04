package gitlet;

import java.io.Serializable;

/** This class represents blobs.
 * @author Harshaan Sall*/
public class Blob implements Serializable {

    /** Blobname.*/
    private String _blobName;

    /** Blob contents.*/
    private String _inside;


    /** Constructor for blob, NAME is name, CONTENT is content, ID is id. */
    public Blob(String name, String content) {
        this._blobName = name;
        this._inside = content;
    }

    /** Getter method for.
     *  @return Blobname.*/
    public String getBlobName() {
        return this._blobName;
    }

    /** Getter method for.
     * @return content.*/
    public String getContent() {
        return this._inside;
    }

}
