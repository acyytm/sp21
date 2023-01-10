package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.util.*;

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

    /** branches. */
    private static Branches branches;

    /* TODO: fill in the rest of this class. */

    /** Init a repo. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        /* create necessary to keep track on repo. */
        setupPersistence();

        Commit commit = new Commit("initial commit");
        commit.saveCommit();

        head = new Head(commit);
        stage = new Stage();
        branches = new Branches();
        branches.addBranch("master");

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
            save();
            System.exit(0);
        }

        Commit commit = head.getCommit();
        if(commit.isRemoved(fileName)) {
            commit.reAdd(fileName);
            commit.saveCommit();
        }
        else {
            stage.add(fileName);
        }

        save();
    }

    /** commit command. */
    public static void commit(String message) {
        read();

        Commit headCommit = head.getCommit();
        if(stage.empty() && headCommit.hasNoRemoved()) {
            System.out.println("No changes added to the commit.");
            save();
            System.exit(0);
        }

        Commit commit = new Commit(message, head.getCommit().getHash());
        commit.saveCommit();

        head.pointTo(commit);
        String branch = head.getCurrentBranch();
        branches.update(branch, commit);

        save();
    }

    /** checkout command. */
    public static void checkout(String fileName) {
        read();
        checkout(fileName, head.getCommit().getHash());
        save();
    }

    /** checkout id -- file command. */
    public static void checkout(String fileName, String hash) {
        read();
        Commit commit = Commit.fromFile(hash);
        String fileHash = commit.getFileHash(fileName);
        if(fileHash == null) {
            System.out.println("File does not exist in that commit.");
            save();
            System.exit(0);
        }
        Blob blob = Blob.fromFile(Commit.COMMIT_BLOB_DIR, fileHash);
        blob.writeToFile(CWD);
        save();
    }

    /** checkout branch name command.*/
    public static void checkoutBranch(String branchName) {
        read();

        /* branch with branchName doesn't exist. */
        if(branches.getBranch(branchName) == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        /* branch with branchName is current head branch. */
        if(head.getCurrentBranch().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Commit commit = branches.getCommit(branchName);

        /* cwd is not working tree clean. */
        if(!checkCWD(commit)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            save();
            System.exit(0);
        }

        /* clear all files head commit tracks on. */
        Commit headCommit = head.getCommit();
        HashMap<String, String> filesMap = headCommit.getMap();
        for(Map.Entry<String, String> entry: filesMap.entrySet())
        {
            File file = Utils.join(CWD, entry.getKey());
            if(file.exists()) {
                file.delete();
            }
        }

        /* write all files tracks by branch to CWD. */
        head.pointTo(commit);
        head.pointToBranch(branchName);
        filesMap = commit.getMap();
        for(Map.Entry<String, String> entry: filesMap.entrySet())
        {
            Blob blob = Blob.fromFile(Commit.COMMIT_BLOB_DIR, entry.getValue());
            blob.writeToFile(CWD);
        }

        save();
    }


    /** Log command. */
    public static void log() {
        read();
        Commit commit = head.getCommit();
        String commitLog = commit.getLogChain();
        System.out.print(commitLog);
        save();
    }

    /** Branch command. */
    public static void branch(String branchName) {
        read();

        branches.addBranch(branchName);

        save();
    }

    /** Rm command. */
    public static void rm(String fileName) {
        read();

        File file = Utils.join(CWD, fileName);

        boolean exist = false;

        if(stage.contain(fileName)) {
            exist = true;
            stage.removeFromStage(fileName);
        }

        Commit commit = head.getCommit();

        if(commit.contain(fileName)) {
            exist = true;
            commit.remove(fileName);
            commit.saveCommit();

            if(file.exists()) {
                file.delete();
            }
        }

        if(!exist) {
            System.out.println("No reason to remove the file.");
            save();
            System.exit(0);
        }

        save();
    }

    /** status command. */
    public static void status() {
        read();

        branches.display();
        stage.display();
        head.getCommit().displayRemovedFiles();

        System.out.println("=== Modifications Not Staged For Commit ===\n");
        System.out.println("=== Untracked Files ===\n");

        save();
    }

    /** global-log command. */
    public static void globalLog() {
        read();

        List<String> commits = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        for (String commitObj: commits)
        {
            Commit commit = Commit.fromFile(commitObj);
            String log = commit.getLog();
            System.out.print(log);
        }

        save();
    }

    /** Find command. */
    public static void find(String message) {
        read();

        List<String> commits = Utils.plainFilenamesIn(Commit.COMMIT_DIR);
        boolean found = false;
        for (String commitObj: commits)
        {
            Commit commit = Commit.fromFile(commitObj);
            String commitMessage = commit.getMessage();
            if(message.equals(commitMessage)) {
                System.out.println(commitObj);
                found = true;
            }
        }

        if(!found) {
            System.out.println("Found no commit with that message.");
            save();
            System.exit(0);
        }

        save();
    }

    /** Reset command.
     * @param hash commit id
     * */
    public static void reset(String hash) {
        read();

        Commit target = Commit.fromFile(hash);

        if(!checkCWD(target)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            save();
            System.exit(0);
        }

        String oldBranch = head.getCurrentBranch();
        Commit oldCommit = head.getCommit();

        head.pointTo(target);
        branches.addBranch("temp");
        head.pointTo(oldCommit);
        checkoutBranch("temp");
        head.pointToBranch(oldBranch);
        branches.removeBranch("temp");
        stage.deleteFiles();
        //=========
        Commit headCommit = head.getCommit();
        headCommit.clearRemoved();
        headCommit.saveCommit();
        head.pointTo(headCommit);
        branches.removeBranch(oldBranch);
        branches.addBranch(oldBranch);
        //=========

        save();
    }

    /**
     * Merge command.
     *      * 1. modified in other but not head --> other, return 1
     *      * 2. modified in head but not other --> head, return 2
     *      * 3. modified in other and head --> in same way, DNM, return 3
     *      *                               --> in diff way, conflict, return 4
     *      * 4. not in split nor other but in head --> head, return 5
     *      * 5. not in split nor head but in other --> other, return 6
     *      * 6. not in head nor other but in split --> remove it, return 7
     *      * 6. unmodified in head but not present in other --> remove it, return 8
     *      * 7. unmodified in other but not present in head --> remain removed return 9
     */
    public static void merge(String branchName) {
        read();

        Commit otherCommit = branches.getCommit(branchName);
        Commit headCommit = head.getCommit();

        if(otherCommit == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        Commit split = findSplit(headCommit, otherCommit);


        if(branchName.equals(head.getCurrentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        if(!stage.empty() || !headCommit.getRemovedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if(!checkCWD(otherCommit)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        if(split.getHash().equals(otherCommit.getHash())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if(split.getHash().equals(headCommit.getHash())) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
            return;
        }

        Set<String> fileSet = new HashSet<>();
        fileSet.addAll(otherCommit.getMap().keySet());
        fileSet.addAll(headCommit.getMap().keySet());
        fileSet.addAll(split.getMap().keySet());

        for (String fileName: fileSet) {
            int property = judge(fileName, headCommit, otherCommit, split);
            switch (property) {
                case 1:
                case 6:
                    String blobHash = otherCommit.getFileHash(fileName);
                    Blob blob = Blob.fromFile(Commit.COMMIT_BLOB_DIR, blobHash);
                    blob.writeToFile(CWD);
                    stage.add(fileName);
                    break;
                case 2:
                case 3:
                    break;
                case 4:
                    //conflict
                    String targetContent = getConflict(fileName, headCommit, otherCommit);
                    File file = Utils.join(CWD, fileName);
                    Utils.writeContents(file, targetContent);
                    stage.add(fileName);
                    System.out.println("Encountered a merge conflict.");
                    break;
                case 5:
                    break;
                case 7:
                    break;
                case 8:
                    rm(fileName);
                case 9:
                    break;
            }
        }

        commit("Merged "  + branchName + " into " + head.getCurrentBranch() + ".");

        save();
    }

    private static String getConflict(String fileName, Commit curr, Commit other) {
        Blob currBlob = Blob.fromFile(Commit.COMMIT_BLOB_DIR, curr.getMap().get(fileName));
        Blob otherBlob = Blob.fromFile(Commit.COMMIT_BLOB_DIR, other.getMap().get(fileName));
        String currContent = currBlob.getFileContent();
        String otherContent = otherBlob.getFileContent();
        String content =
                "<<<<<<< HEAD\n" +
                currContent +
                "=======\n" +
                otherContent +
                ">>>>>>>\n";
        return content;
    }

    /**
     * Help method, find split commit and return it
     */
    private  static Commit findSplit(Commit headCommit, Commit otherCommit) {
        Set<String> set = new HashSet<>();
        Commit split = null;

        for(String hash = headCommit.getHash(); hash != null; hash = headCommit.getHash()) {
            set.add(headCommit.getHash());
            if(headCommit.getParent() == null) {
                break;
            }
            headCommit = Commit.fromFile(headCommit.getParent());
        }

        for(String hash = otherCommit.getHash(); hash != null; hash = otherCommit.getHash()) {
            if (set.contains(hash)) {
                split = Commit.fromFile(hash);
                break;
            }
            set.add(otherCommit.getHash());
            if(otherCommit.getParent() == null) {
                break;
            }
            otherCommit = Commit.fromFile(otherCommit.getParent());
        }
        return split;
    }

    /**
     * Help method, judge a file
     * 1. modified in other but not head --> other, return 1
     * 2. modified in head but not other --> head, return 2
     * 3. modified in other and head --> in same way, DNM, return 3
     *                               --> in diff way, conflict, return 4
     * 4. not in split nor other but in head --> head, return 5
     * 5. not in split nor head but in other --> other, return 6
     * 6. not in head nor other but in split --> remove it, return 7
     * 6. unmodified in head but not present in other --> remove it, return 8
     * 7. unmodified in other but not present in head --> remain removed return 9
     */
    private static int judge(String fileName, Commit headCommit, Commit otherCommit, Commit splitCommit) {
        String headFileHash = headCommit.getFileHash(fileName);
        String otherFileHash = otherCommit.getFileHash(fileName);
        String splitFileHash = splitCommit.getFileHash(fileName);
        String removedFile = splitCommit.getRemovedFiles().get(fileName);



        if(splitFileHash == null && otherFileHash == null && removedFile == null) {
            return 5;
        }
        if(splitFileHash == null && headFileHash == null) {
            return 6;
        }
        if(headFileHash == null && otherFileHash == null) {
            return 7;
        }
        if(otherFileHash == null && (removedFile.equals(headFileHash) || splitFileHash.equals(headFileHash))) {
            return 8;
        }
        if(headFileHash == null && splitFileHash.equals(otherFileHash)) {
            return 9;
        }
        if(splitFileHash == null && headFileHash.equals(otherFileHash)) {
            return 3;
        }
        if(headFileHash.equals(splitFileHash) && !otherFileHash.equals(splitFileHash)) {
            return 1;
        }
        if(otherFileHash.equals(splitFileHash) && !headFileHash.equals(splitFileHash)) {
            return 2;
        }
        if(!otherFileHash.equals(headFileHash)) {
            return 4;
        }
        return 3;
    }

    /**
     * Remove branch command
     */
    public static void removeBranch(String branchName) {
        read();

        if(head.getCurrentBranch().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            save();
            System.exit(0);
        }

        branches.removeBranch(branchName);

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
        if(stage == null)
            stage = Stage.fromFile();
        if(head == null)
            head = Head.fromFile();
        if(branches == null)
            branches = Branches.fromFile();
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
        branches.saveBranches();
    }

    /**
     * check if current working dir has everything committed
     * @return true while everything has been committed, false while not
     */
    private static boolean checkCWD(Commit target) {
        //Commit currentCommit = head.getCommit();

        List<String> filesCWD = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> filesTargetCommitted = target.getMap();
        HashMap<String, String> currentCommitFiles = head.getCommit().getMap();

        for(String file: filesCWD)
        {
            Blob blob = new Blob(file);
            if(filesTargetCommitted.containsKey(file)) {
                 if (filesTargetCommitted.get(file).equals(blob.getHash())) {
                     continue;
                 }
                 if(!currentCommitFiles.containsKey(file)){
                     return false;
                 }else if(!currentCommitFiles.get(file).equals(blob.getHash())){
                     return false;
                 }
            }
        }
        return true;
    }
}
