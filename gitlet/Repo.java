package gitlet;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Formatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

/** The repo class contains all gitlet methods and structures
 * as static variables.
 * @author Harshaan Sall */

public class Repo {

    /** Current Working Directory. */
    private static final File CWD = new File(".");

    /** .gitlet directory in which program executes. */
    private static final File GITLET_DIRECTORY = Utils.join(CWD, ".gitlet");

    /** Directory containing staging area.  */
    private static final File STAGING_DIRECTORY
            = Utils.join(GITLET_DIRECTORY, "staging_directory");

    /** Directory containing every commit and commit history. */
    private static final File COMMIT_DIRECTORY
            = Utils.join(GITLET_DIRECTORY, "commit_directory");

    /** Path for serialized file that contains stagedAdd Hashmap. */
    private static final File STAGEDADDFILE
            = Utils.join(STAGING_DIRECTORY, "additionsDirectory");

    /** Path for serialized file that contains stagedRemove hashmap. */
    private static final File STAGEDREMOVEFILE
            = Utils.join(STAGING_DIRECTORY, "removalDirectory");

    /** Path for serialized file that contains every commit. */
    private static final File COMMITS
            = Utils.join(COMMIT_DIRECTORY, "prevCommits");

    /** Path for serialized file that contains
     * commit history in commit directory. */
    private static final File HISTFILE
            = Utils.join(COMMIT_DIRECTORY, "history");

    /** Path for serialized file that contains every branch. */
    private static final File BRANCHFILE
            = Utils.join(COMMIT_DIRECTORY, "branchDir");

    /** Path for serialized file that contains the current head branch. */
    private static final File HEADPOINTER
            = Utils.join(COMMIT_DIRECTORY, "head");

    /** Branch path. */
    private static final File BRANCHHISTFILE
            = Utils.join(COMMIT_DIRECTORY, "branchHist");

    /** Empty hashmap of file blobs for initial commit. */
    private static final HashMap<String, Blob> EMPTYFILES
            = new HashMap<>();

    /** This static variable is the head pointer. */
    private static String  _head;

    /** This static variable contains the files staged for addition. */
    private static HashMap<String, Blob>
            stagedAdd = new HashMap<String, Blob>();;

    /** This static variable contains the files staged for removal. */
    private static HashMap<String, Blob> stagedRemove
            = new HashMap<String, Blob>();;

    /** This static variable contains every commit object created.  */
    private static HashMap<String, Commit>
            _everyCommit =  new HashMap<String, Commit>();

    /** This static variable contains every branch object created.  */
    private static HashMap<String, Branch> _branches = new HashMap<>();;

    /** This static variable contains the commit history for a branch.  */
    private static LinkedList<String> _branchCommitHist = new LinkedList<>();;

    /** This static variable contains the commit history.   */
    private static LinkedList<String> _history
            = new LinkedList<>();;


    /** Creates a new Gitlet version-control system in the current
     * directory with one
     * initial commit and sets up persistence.*/
    public static void init() {
        if (GITLET_DIRECTORY.exists()
                || STAGING_DIRECTORY.exists()
                || STAGING_DIRECTORY.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        }
        setupPersistence();
        Date initialTime = new Date();
        initialTime.setTime(0);
        java.sql.Timestamp time0
                = new java.sql.Timestamp(initialTime.getTime());
        Commit initialCommit
                = new Commit("initial commit",
                time0.toString(), EMPTYFILES, null);
        initialCommit.setUniqueID();
        _everyCommit.put(initialCommit.getUniqueID(), initialCommit);
        LinkedList<String> masterCommitHist = new LinkedList<>();
        Branch masterBranch
                = new Branch(initialCommit.getUniqueID(), masterCommitHist);
        masterBranch.addCommit(initialCommit.getUniqueID());
        _branchCommitHist.add(initialCommit.getUniqueID());
        _branches.put("master", masterBranch);
        _history.add(initialCommit.getUniqueID());
        _head = "master";
        writeAllobjects();
    }

    /** Helper method to return active branch.
     * @return active branch.*/
    public static Branch getActiveBranch() {
        return _branches.get(_head);
    }

    /** Helper method that returns the latest
     *  head commit which is used multiple times.
     * @param branch br.
     * @return commitid.*/
    public static Commit returnHeadCommit(String branch) {
        String comID = _branches.get(branch).getID();
        return _everyCommit.get(comID);
    }

    /** Helper method that serializes all static variables to keep state. */
    public static void writeAllobjects() {
        Utils.writeObject(STAGEDADDFILE, stagedAdd);
        Utils.writeObject(STAGEDREMOVEFILE, stagedRemove);
        Utils.writeObject(COMMITS, _everyCommit);
        Utils.writeObject(HISTFILE, _history);
        Utils.writeObject(BRANCHFILE, _branches);
        Utils.writeObject(HEADPOINTER, _head);
        Utils.writeObject(BRANCHHISTFILE, _branchCommitHist);
    }

    /** Helper method that creates necessary directories. */
    public static void setupPersistence() {
        if (!(GITLET_DIRECTORY.exists())) {
            GITLET_DIRECTORY.mkdirs();
        }
        if (!(STAGING_DIRECTORY.exists())) {
            STAGING_DIRECTORY.mkdirs();
        }
        if (!(COMMIT_DIRECTORY.exists())) {
            COMMIT_DIRECTORY.mkdirs();
        }
    }

   /** Adds a copy of the file as it currently exists to the
     * staging area.
    * @param name n.*/
    @SuppressWarnings("unchecked")
    public static void add(String name) {
        File newFile = Utils.join(CWD, name);
        boolean exists = newFile.exists();
        if (!exists) {
            System.out.println("File does not exist.");
            return;
        }
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _history = Utils.readObject(HISTFILE, LinkedList.class);
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        String content = Utils.readContentsAsString(newFile);
        Blob newFileBlob = new Blob(name, content);
        stagedAdd = Utils.readObject(STAGEDADDFILE, HashMap.class);
        if (stagedAdd.containsKey(name)) {
            stagedAdd.remove(name);
            stagedAdd.put(name, newFileBlob);
        } else {
            stagedAdd.put(name, newFileBlob);
            Branch active = getActiveBranch();
            String activeBranchID = active.getID();
            Commit latestCommit = _everyCommit.get(activeBranchID);
            HashMap<String, Blob> tracked = latestCommit.getFiles();
            stagedRemove = Utils.readObject(STAGEDREMOVEFILE, HashMap.class);
            if (tracked.containsKey(name)) {
                Blob prevBlob = latestCommit.getFiles().get(name);
                if (prevBlob.getContent().equals(content)) {
                    stagedAdd.remove(name);
                    stagedRemove.remove(name);
                }
            }
        }
        writeAllobjects();
    }

    /** Saves a snapshot of tracked files in the current commit and staging area
     * so they can be restored at a later time, creating a new commit.
     * @param message m.*/
    @SuppressWarnings("unchecked")
    public static void commit(String message) {
        stagedAdd = Utils.readObject(STAGEDADDFILE, HashMap.class);
        stagedRemove = Utils.readObject(STAGEDREMOVEFILE, HashMap.class);
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _history = Utils.readObject(HISTFILE, LinkedList.class);
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        _branchCommitHist = Utils.readObject(BRANCHHISTFILE, LinkedList.class);
        if (stagedAdd.isEmpty() && stagedRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
        }
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
        }
        String parentCommitID = getActiveBranch().getID();
        Commit lastCommit = returnHeadCommit(_head);
        for (String filename: lastCommit.getallFileNames()) {
            if (!stagedAdd.containsKey(filename)) {
                stagedAdd.put(filename, lastCommit.getFile(filename));
            }
        }
        for (String filename: stagedRemove.keySet()) {
            if (stagedAdd.containsKey(filename)) {
                stagedAdd.remove(filename);
            }
        }
        Date date1 = new Date();
        java.sql.Timestamp time1
                = new java.sql.Timestamp(date1.getTime());
        HashMap<String, Blob> newComFiles
                = (HashMap<String, Blob>) stagedAdd.clone();
        Commit com1 = new Commit(message, time1.toString(),
                newComFiles, parentCommitID);
        com1.setUniqueID();
        getActiveBranch().updatePointer(com1.getUniqueID());
        getActiveBranch().addCommit(com1.getUniqueID());
        _branchCommitHist.add(com1.getUniqueID());
        _everyCommit.put(com1.getUniqueID(), com1);
        _history.add(com1.getUniqueID());
        stagedAdd.clear();
        stagedRemove.clear();
        writeAllobjects();
    }

    /** Takes the version of the file as it exists in the
     * head commit, the front of the current branch, and
     * puts it in the working directory,
     * overwriting the version of the file
     * that's already there if there is one.
     * The new version of the file is not staged.
     * @param filename f.*/
    public static void checkout(String filename) {
        _head = Utils.readObject(HEADPOINTER, String.class);
        checkout(_head, filename);
    }

    /** Takes the version of the file as it exists
     * in the commit with the given id, and
     * puts it in the working directory, overwriting
     * the version of the file
     * that's already there if there is one. The new version
     * of the file is not staged.
     * @param iD id.
     * @param filename n.*/
    @SuppressWarnings("unchecked")
    public static void checkout(String iD, String filename) {
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _history = Utils.readObject(HISTFILE, LinkedList.class);
        List<String> commitIds = new ArrayList<>(_everyCommit.keySet());
        for (String comIDs : commitIds) {
            String abr = comIDs.substring(0, 8);
            if (iD.equals(abr)) {
                iD = comIDs;
            }
        }
        if (iD.equals(_head)) {
            Commit headCommit = returnHeadCommit(_head);
            if (headCommit.containsFile(filename)) {
                String prevState = headCommit.getFileContents(filename);
                Utils.writeContents(Utils.join(CWD, filename), prevState);
            }
        } else {
            ArrayList<String> comIds = new ArrayList<>();
            for (Commit com : _everyCommit.values()) {
                comIds.add(com.getUniqueID());
            }
            if (!comIds.contains(iD)) {
                System.out.println("No commit with that id exists.");
            } else {
                Commit prevCommit = _everyCommit.get(iD);
                if (!(prevCommit.containsFile(filename))) {
                    System.out.println("File does not exist in that commit.");
                }
                if (prevCommit.containsFile(filename)) {
                    String prevState = prevCommit.getFileContents(filename);
                    Utils.writeContents(Utils.join(CWD, filename), prevState);
                }
            }
        }
    }

    /** Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory,overwriting the versions
     * of the files that are already there if they exist.
     * @param branchName n.*/
    @SuppressWarnings("unchecked")
    public static void checkoutBranch(String branchName) {
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        stagedAdd = Utils.readObject(STAGEDADDFILE, HashMap.class);
        stagedRemove = Utils.readObject(STAGEDREMOVEFILE, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        if (!_branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        } else if (branchName.equals(_head)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit headCommit = returnHeadCommit(branchName);
        Commit currentHead = returnHeadCommit(_head);
        for (String name : Utils.plainFilenamesIn(CWD)) {
            if (!currentHead.containsFile(name)) {
                compareFile(name, headCommit);
            }
        }
        List<String> sorted = new ArrayList<>(currentHead.getallFileNames());
        for (String filename : sorted) {
            if (headCommit.containsFile(filename)) {
                checkout(headCommit.getUniqueID(), filename);
            } else {
                File dirCheckout = Utils.join(CWD, filename);
                dirCheckout.delete();
            }
        }
        Collection<Blob> headBlobs = headCommit.getFiles().values();
        for (Blob blob : headBlobs) {
            String contents = blob.getContent();
            Utils.writeContents(Utils.join(CWD,
                    blob.getBlobName()), contents);
        }
        _head = branchName;
        writeAllobjects();
    }

    /** Helper method that compares version of
     * file in CWD to one in
     * given commit.
     * @param cwdFile cw.
     * @param com c.*/
    public static void compareFile(String cwdFile, Commit com) {
        String currentContents =
                Utils.readContentsAsString(Utils.join(CWD, cwdFile));
        if (com.containsFile(cwdFile)) {
            String commitContents = com.getFileContents(cwdFile);
            if (!commitContents.equals(currentContents)) {
                System.out.println("There is an untracked file in the way;"
                        +  " delete it, or add and commit it first.");
                return;
            }
        }
    }

    /** Starting at the current head commit, display information about each
     * commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents
     * found in merge commits.*/
    @SuppressWarnings("unchecked")
    public static void log() {
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _history = Utils.readObject(HISTFILE, LinkedList.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        LinkedList<String> head = getActiveBranch().getHist();
        printLog(head);
    }

    /** Displays information about all commits ever made.*/
    @SuppressWarnings("unchecked")
   public static void globaLog() {
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _history = Utils.readObject(HISTFILE, LinkedList.class);
        printLog(_history);
    }

    /** Helper method for log and global log.
     * @param container c. */
    public static void printLog(LinkedList<String> container) {
        for (int i = container.size() - 1; i >= 0; i--) {
            String comID = container.get(i);
            Commit com = _everyCommit.get(comID);
            System.out.println("===");
            System.out.println("commit " + com.getUniqueID());
            String timestamp = com.getTimestamp();
            Timestamp time = Timestamp.valueOf(timestamp);
            Formatter form = new Formatter();
            form.format("%tc", time);
            String date = form.toString();
            date = date.replace("PDT ",
                    "").replace("PST ", "");
            System.out.println("Date: " + date + " -0800");
            System.out.println(com.getMessage() + "\n");
        }
    }

    /** Unstages the file if it is currently staged for addition.
     * @param removeFile f.*/
    @SuppressWarnings("unchecked")
    public static void rm(String removeFile) {
        stagedAdd = Utils.readObject(STAGEDADDFILE, HashMap.class);
        stagedRemove = Utils.readObject(STAGEDREMOVEFILE, HashMap.class);
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _history = Utils.readObject(HISTFILE, LinkedList.class);
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        HashMap<String, Blob> comFiles = returnHeadCommit(_head).getFiles();
        if (!stagedAdd.containsKey(removeFile)
                && !comFiles.containsKey(removeFile)) {
            System.out.println("No reason to remove the file.");
        }
        if (stagedAdd.containsKey(removeFile)) {
            stagedAdd.remove(removeFile);
        }
        File removeDir = Utils.join(CWD, removeFile);
        if (comFiles.containsKey(removeFile)) {
            stagedRemove.put(removeFile,
                    comFiles.get(removeFile));
            if (removeDir.exists()) {
                removeDir.delete();
            }
        }
        writeAllobjects();
    }

    /** Prints out the ids of all commits that have
     * the given commit message, one per line.
     * @param message m.*/
    @SuppressWarnings("unchecked")
    public static void find(String message) {
        ArrayList<String> comIds = new ArrayList<>();
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        for (Commit commit : _everyCommit.values()) {
            if (commit.getMessage().equals(message)) {
                comIds.add(commit.getUniqueID());
            }
        }
        if (comIds.isEmpty()) {
            System.out.println("Found no "
                    + "commit with that message.");
        } else {
            for (String id : comIds) {
                System.out.println(id);
            }
        }
    }

    /** Displays what branches currently exist,
     * and marks the current branch with a *. */
    @SuppressWarnings("unchecked")
    public static void status() {
        if (!GITLET_DIRECTORY.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        stagedAdd = Utils.readObject(STAGEDADDFILE, HashMap.class);
        stagedRemove = Utils.readObject(STAGEDREMOVEFILE, HashMap.class);
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        System.out.println("=== Branches ===");
        List<String> branchStatus = statusHelper(_branches);
        for (String br : branchStatus) {
            if (br.equals(_head)) {
                System.out.println("*" + br);
            } else {
                System.out.println(br);
            }
        }
        System.out.println("\n" + "=== Staged Files ===");
        List<String> addStatus = statusHelper(stagedAdd);
        for (String add : addStatus) {
            System.out.println(add);
        }
        System.out.println("\n" + "=== Removed Files ===");
        List<String> remStatus = statusHelper(stagedRemove);
        for (String rem : remStatus) {
            System.out.println(rem);
        }
        System.out.println("\n"
                + "=== Modifications Not Staged For Commit ===");
        System.out.println("\n" + "=== Untracked Files ===");
    }

    /** Status helper.
     * @param list l.
     * @return list.*/
    @SuppressWarnings("unchecked")
    public static List<String> statusHelper(Map<String, ?> list) {
        Set<String> keys = list.keySet();
        List<String> sorted = new ArrayList<>(keys);
        Collections.sort(sorted);
        return sorted;
    }

    /** Creates a new branch with the given name,
     * and points it at the current head node.
     * @param name n. */
    @SuppressWarnings("unchecked")
    public static void branch(String name) {
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        if (_branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.,");
        }
        Branch currHead = getActiveBranch();
        Branch newBranch = new Branch(currHead.getID(),
                (LinkedList<String>)
                currHead.getHist().clone());
        _branches.put(name, newBranch);
        writeAllobjects();
    }

    /**  Deletes the branch with the given name.
     * @param name n. */
    @SuppressWarnings("unchecked")
    public static void rmBranch(String name) {
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        if (!_branches.containsKey(name)) {
            System.out.println("branch with that name does not exist.");
            return;
        } else if (name.equals(_head)) {
            System.out.println("Cannot remove the current branch.");
            return;
        } else {
            _branches.remove(name);
        }
        writeAllobjects();
    }

    /** Checks out all the files tracked by the given commit.
     * @param id n. */
    @SuppressWarnings("unchecked")
    public static void reset(String id) {
        stagedAdd = Utils.readObject(STAGEDADDFILE, HashMap.class);
        stagedRemove = Utils.readObject(STAGEDREMOVEFILE, HashMap.class);
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
        if (!_everyCommit.containsKey(id)) {
            System.out.println("No commit with that id exists.");
            return;
        } else {
            Commit headCommit = _everyCommit.get(id);
            for (String name : Utils.plainFilenamesIn(CWD)) {
                compareFile(name, headCommit);
            }
            List<String> sorted = new ArrayList<>(headCommit.getallFileNames());
            for (String filename : sorted) {
                checkout(id, filename);
            }
        }
        Branch currHead = getActiveBranch();
        if (currHead.containsCommit(id)) {
            int x = currHead.getHist().size() - 1;
            currHead.getHist().remove(x);
        } else {
            currHead.addCommit(id);
        }
        for (Branch br : _branches.values()) {
            if (br.getID().equals(id)) {
                _branches.replace(_head, br);
            }
        }
        stagedAdd.clear();
        stagedRemove.clear();
        writeAllobjects();
    }

    /** Helper method that compares version
     * of file in CWD to one in given commit.
     * @param cwdFile c.
     * @param active a.
     * @param given g.
     * @return int i.*/
    public static int compareFileMerge(String cwdFile,
                                       Commit active, Commit given) {
        int numFiles = 0;
        String currentContents =
                Utils.readContentsAsString(Utils.join(CWD, cwdFile));
        if (!active.containsFile(cwdFile)) {
            if (given.containsFile(cwdFile)) {
                String commitContents = given.getFileContents(cwdFile);
                if (!commitContents.equals(currentContents)) {
                    numFiles++;
                }
            }
        }
        return numFiles;
    }

    /** Read merge objs.*/
    @SuppressWarnings("unchecked")
    public static void read() {
        stagedAdd = Utils.readObject(STAGEDADDFILE, HashMap.class);
        stagedRemove = Utils.readObject(STAGEDREMOVEFILE, HashMap.class);
        _everyCommit = Utils.readObject(COMMITS, HashMap.class);
        _branches = Utils.readObject(BRANCHFILE, HashMap.class);
        _head = Utils.readObject(HEADPOINTER, String.class);
    }

    /** Merges files from the given branch into the current branch.
     * @param branch b. */
    @SuppressWarnings("unchecked")
    public static void merge(String branch) {
        read();
        Commit masterCommit = _everyCommit.get(getActiveBranch().getID());
        Commit givenCommit = _everyCommit.get(_branches.get(branch).getID());
        int cwdF = 0;
        for (String name : Utils.plainFilenamesIn(CWD)) {
            cwdF += compareFileMerge(name, masterCommit, givenCommit);
        }
        if (cwdF > 0) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        }
        String splitPoint = findSplitPoint(getActiveBranch().getHist(),
                _branches.get(branch).getHist());
        Commit splitCommit = _everyCommit.get(splitPoint);
        if (splitPoint.equals(getActiveBranch().getID())) {
            checkoutBranch(branch);
            System.out.println("Current branch fast-forwarded.");
        }
        for (String name : givenCommit.getallFileNames()) {
            if (!splitCommit.containsFile(name)
                    && !masterCommit.containsFile(name)) {
                checkout(_branches.get(branch).getID(), name);
                add(name);
            } else if ((modified(name, splitCommit, givenCommit)
                    && !modified(name, splitCommit, masterCommit))) {
                checkout(_branches.get(branch).getID(), name);
                add(name);
            } else if (masterCommit.containsFile(name)) {
                if (modified(name, splitCommit, masterCommit)) {
                    conflict(name, masterCommit, givenCommit);
                }
            }
        }
        for (String name : splitCommit.getallFileNames()) {
            if (!givenCommit.getallFileNames().contains(name)
                    && !modified(name, splitCommit, masterCommit)) {
                rm(name);
            } else if (!givenCommit.getallFileNames().contains(name)
                     && masterCommit.containsFile(name)) {
                conflict(name, masterCommit, givenCommit);
            }
        }
        commit("Merged " + branch + " into " + _head + ".");
    }

    /** Conflict helper.
     * @param name n.
     * @param active a.
     * @param other a.*/
    public static void conflict(String name,
                                Commit active, Commit other) {
        String activeContent = active.getFileContents(name);
        String otherContent = "";
        if (!other.containsFile(name)) {
            otherContent = "";
        } else {
            otherContent = other.getFileContents(name);
        }
        String updated = "<<<<<<< HEAD\n" + activeContent
                + "=======\n"
                + otherContent + ">>>>>>>\n";
        Utils.writeContents(Utils.join(CWD, name), updated);
        add(name);
        System.out.println("Encountered a merge conflict.");
    }

    /** helper.
     * @param name s.
     * @param active c.
     * @param other g.
     * @return b b.*/
    public static boolean modified(String name,
                                   Commit active, Commit other) {
        Blob blob1 = active.getFile(name);
        Blob blob2 = other.getFile(name);
        return !blob1.equals(blob2);
    }

    /** helper.
     * @param active s.
     * @param given g.
     * @return string s.*/
    public static String findSplitPoint(LinkedList<String> active,
                                        LinkedList<String> given) {
        String splitPoint = "";
        int activeEndInd = active.size() - 1;
        int givenEndInd = given.size() - 1;
        boolean latest = false;
        for (int g = givenEndInd; g >= 0; g--) {
            for (int a = activeEndInd; a >= 0; a--) {
                String currNode = given.get(g);
                if (currNode.equals(active.get(a))) {
                    if (!latest) {
                        splitPoint = given.get(g);
                        latest = true;
                    }
                }
            }
        }
        return splitPoint;
    }

}
