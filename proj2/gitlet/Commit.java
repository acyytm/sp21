package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The time of this Commit was created. */
    private Date timestamp;

    /** A mapping of file names to blob references.(using hash) */
    private HashMap<String, String> map;

    /** Parent commits (using hash to ref. */
    private ArrayList<String> parents;

    private String parent;

    /** The commit objects directory. */
    public static final File COMMIT_DIR = Utils.join(Repository.OBJ_DIR, "commit");

    /** The blob dir. */
    public static final File COMMIT_BLOB_DIR = Utils.join(Repository.OBJ_DIR, "blob");

    /** Log information of this commit. */
    private String log;

    /** Hash code of the commit. */
    private String hash;

    /** keep removed files. */
    private HashMap<String, String> removedFiles;

    /**
     * A constructor that takes message and its parent as arguments, timestamp will be initialized automatically.
     * @param message commit message -m message
     * @param parent its parent Commit
     */
    public Commit(String message, String parent) {
        this(message, parent, new Date());

        this.parents.add(parent);

        Commit parentCommit = Commit.fromFile(parent);
        map.putAll(parentCommit.map);
        fromStage();
        hash = getCommitHash();
        // Head head = Repository.getHead();
        //head.pointTo(this);

        generateLog();
    }

    /**
     * A constructor that init a repo and create first Commit with timestamp is 1970 and parent is null
     * @param message commit message
     */
    public Commit(String message) {
        this(message, null, new Date(0));
        hash = getCommitHash();
        generateLog();
    }

    /**
     * help constructor, parent is a hash code
     */
    private Commit(String message, String parent, Date timestamp) {
        this.message = message;
        this.parent = parent;
        this.timestamp = timestamp;
        parents = new ArrayList<>();
        this.map = new HashMap<>();
        this.removedFiles = new HashMap<>();
    }

    /**
     * Help method, generate this commit's log.
     */
    private void generateLog() {
        Locale usLocale = new Locale("en", "US");
        SimpleDateFormat DateFor = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z", usLocale);
        String stringDate = DateFor.format(timestamp);
        //System.out.println(hash);
        log =     "===\n"
                + "commit " + hash + "\n"
                + "Date: " + stringDate + "\n"
                + message + "\n" + "\n";
    }

    /** save this commit in a file. */
    public void saveCommit() {
        String sha1 = hash;
        File commitFile = Utils.join(COMMIT_DIR, sha1);
        Utils.writeObject(commitFile, this);
    }

    /**
     * Read commit object from specific name(hash).
     * @param sha1
     * @return
     */
    public static Commit fromFile(String sha1) {
        File commitFile = Utils.join(COMMIT_DIR, sha1);
        if(!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        return commit;
    }

    /**
     * Help method, get this commit's hash code
     * @return
     */
    private String getCommitHash() {
        String content = "commit\n";
        if(parent == null)
            content += message + timestamp + map + removedFiles + "null";
        else
            content += message + timestamp + map + removedFiles + parent;
        String hash = Utils.sha1(content);
        return hash;
    }

    /** Return commit's hash code. */
    public String getHash() {
        return hash;
    }

    /**
     * fileName can map to a Blob object, using hash code
     * actual return blob hash.
     * */
    public String getFileHash(String fileName) {
        return map.get(fileName);
    }

    /**
     * help method, read Staging blob and add them to current commit.
     */
    private void fromStage() {
        Stage stage = Repository.getStage();
        HashMap<String, String> stageMap = stage.getFiles();
        for (HashMap.Entry<String, String> entry: stageMap.entrySet())
        {
            map.put(entry.getKey(), entry.getValue());
            Blob blob = Blob.fromFile(Stage.STAGE_DIR, entry.getValue());
            blob.saveBlob(COMMIT_BLOB_DIR);
        }
        stage.deleteFiles();
    }

    /**
     * @return log information of a chain of commit.
     */
    public String getLogChain() {
        if(parent != null) {
            Commit parentCommit = Commit.fromFile(parent);
            return log + parentCommit.getLogChain();
        }
        return log;
    }

    /** Return if this file was contained. */
    public boolean contain(String fileName) {
        return map.containsKey(fileName);
    }

    /** Remove that file from map, and put it into removeFiles. */
    public void remove(String fileName) {
        removedFiles.put(fileName, map.get(fileName));
        map.remove(fileName);
    }

    /** Display removed files. */
    public void displayRemovedFiles() {
        Set<String> set = removedFiles.keySet();
        Object[] arr = set.toArray();
        Arrays.sort(arr);

        System.out.println("=== Removed Files ===");

        for(Object key: arr)
        {
            System.out.println(key);
        }
        System.out.println();
    }

    /** Return whether the file exists in removedFiles. */
    public boolean isRemoved(String fileName) {
        return removedFiles.containsKey(fileName);
    }

    /** Readd a removed file. */
    public void reAdd(String fileName) {
        Blob blob = Blob.fromFile(Commit.COMMIT_BLOB_DIR, removedFiles.get(fileName));
        blob.writeToFile(Repository.CWD);

        map.put(fileName, removedFiles.get(fileName));
        removedFiles.remove(fileName);
    }

    /** Return if removedFiles is empty. */
    public boolean hasNoRemoved() {
        return removedFiles.isEmpty();
    }

    /** Get the commit log. */
    public String getLog() {
        return log;
    }

    /**
     * Get commit message.
     */
    public String getMessage() {
        return message;
    }
    /* TODO: fill in the rest of this class. */
}
