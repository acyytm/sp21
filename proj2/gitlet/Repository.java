package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  having method init(),
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The obj directory. */
    public static final File OBJ_DIR = join(GITLET_DIR, "obj");

    /** Head pointer. */
    private static Head head;

    /**Stage instance. */
    private static Stage stage;

    /* TODO: fill in the rest of this class. */

    /** Init a repo. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }

        /* create necessary to keep track on repo. */
        setupPersistence();

        Commit commit = new Commit("initial commit");
        commit.saveCommit();

        head = new Head(commit);
        stage = new Stage();
        save();
    }


    /** Add file to stage area. */
    public static void add(String fileName) {
        read();
        /*TODO: If the current working version of the file is identical to the version
        in the current commit, do not stage it to be added,
         and remove it from the staging area if it is already
         there (as can happen when a file is changed, added,
         and then changed back to it’s original version).*/
        if(!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        stage.add(fileName);
        save();
    }

    /** commit command. */
    public static void commit(String message) {
        read();

        if(stage.empty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit commit = new Commit(message, head.getCommit().getHash());
        commit.saveCommit();

        save();
    }

    /** checkout command. */
    public static void checkout(String fileName) {
        read();
        checkout(fileName, head.getCommit().getHash());
        save();
    }

    public static void checkout(String fileName, String hash) {
        read();
        Commit commit = Commit.fromFile(hash);
        String fileHash = commit.getFileHash(fileName);
        if(fileHash == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob blob = Blob.fromFile(Commit.COMMIT_BLOB_DIR, fileHash);
        blob.writeToFile(CWD);
        save();
    }

    /** Log command. */
    public static void log() {
        read();
        Commit commit = head.getCommit();
        String commitLog = commit.getLog();
        System.out.print(commitLog);
        save();
    }

    /** Set up persistence to save file and create necessary directory. */
    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        OBJ_DIR.mkdir();
        Commit.COMMIT_DIR.mkdir();
        Stage.STAGE_DIR.mkdir();
        Commit.COMMIT_BLOB_DIR.mkdir();
    }

    /** Read necessary instance from file. */
    public static void read() {
        stage = Stage.fromFile();
        head = Head.fromFile();
    }

    /** Get head pointer, you should invoke this after read.*/
    public static Head getHead() {
        return head;
    }

    /**
     * get stage method, you should invoke this after read
     */
    public static Stage getStage() {
        return stage;
    }

    public static void save() {
        stage.saveStage();
        head.saveHead();
    }
}
