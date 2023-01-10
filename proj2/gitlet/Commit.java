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
        fromRemoval();
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
            content += message + timestamp + map + "null";
        else
            content += message + timestamp + map + parent;
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
        stage.clearStage();
    }

    /**
     * help method, files in removal should be removed.
     */
    private void fromRemoval() {
        Stage removal = Repository.getRemoval();
        HashMap<String, String> stageMap = removal.getFiles();
        for (HashMap.Entry<String, String> entry: stageMap.entrySet())
        {
            map.remove(entry.getKey(), entry.getValue());
            Blob blob = Blob.fromFile(Stage.REMOVAL_DIR, entry.getValue());
            blob.saveBlob(COMMIT_BLOB_DIR);
        }
        removal.clearStage();
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

    /**
     * Return all blob tracks on
     */
    public HashMap<String, String> getMap() {
        return map;
    }

    public String getParent() {
        return parent;
    }

    public void addParent(String branchName) {
        this.parents.add(branchName);
    }

    public ArrayList<String> getParents() {
        return parents;
    }

}
