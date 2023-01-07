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
        Head head = Repository.getHead();
        head.pointTo(this);

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
    }

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

    public static Commit fromFile(String sha1) {
        File commitFile = Utils.join(COMMIT_DIR, sha1);
        Commit commit = Utils.readObject(commitFile, Commit.class);
        return commit;
    }

    private String getCommitHash() {
        String content = "commit\n";
        if(parent == null)
            content += message + timestamp + map + "null";
        else
            content += message + timestamp + map + parent;
        String hash = Utils.sha1(content);
        return hash;
    }

    public String getHash() {
        return hash;
    }

    public String getFileHash(String fileName) {
        return map.get(fileName);
    }

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

    public String getLog() {
        if(parent != null) {
            Commit parentCommit = Commit.fromFile(parent);
            return log + parentCommit.getLog();
        }
        return log;
    }
    /* TODO: fill in the rest of this class. */
}
