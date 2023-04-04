package gitlet;

import java.io.Serializable;
import java.util.LinkedList;

/** This file represents a branch object.
 * @author Harshaan Sall*/
public class Branch implements Serializable {

    /** pointer for branch. */
    private String _commitID;

    /** Branch's commit history. */
    private LinkedList<String> _allCommits;

    /** Constructor for branch objects. COMMITID, COMMITHISTORY. */
    public Branch(String commitID, LinkedList<String> commitHistory) {
        this._commitID = commitID;
        this._allCommits = commitHistory;
    }

    /** Moves pointer of branch.
     * @param newComId id. */
    public void updatePointer(String newComId) {
        this._commitID = newComId;
    }

    /** Getter for commit id.
     * @return id. */
    public String getID() {
        return this._commitID;
    }

    /** Getter for history.
     * @return hist. */
    public LinkedList<String> getHist() {
        return this._allCommits;
    }

    /** Adds commit to history.
     * @param neww comid. */
    public void addCommit(String neww) {
        this._allCommits.add(neww);
    }

    /** Checks if branch has commit with ID.
     * @param id id.
     * @return bool.*/
    public boolean containsCommit(String id) {
        return this.getHist().contains(id);
    }


}

