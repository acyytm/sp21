package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * class of branch, which is a "pointer" to commit (using hash code to ref
 * remember to update branch each time you commit
 */
public class Branches implements Serializable {

    public static final File BRANCHES = Utils.join(Repository.GITLET_DIR, "BRANCHES");

    /** A map, branch name to commit ref(hash). */
    private HashMap<String, String> map;

    /** constructor, which create a branch point to commit. */
    public Branches() {
        map = new HashMap<>();
    }

    // seems useless
    /** Return commit instance that branch point to
     * @param name: branch name
     */
    public Commit getCommit(String name) {
        String hash = map.get(name);
        if(hash == null) {
            System.out.println("Don't have this branch");
            System.exit(0);
        }
        return Commit.fromFile(hash);
    }

    /**
     * Add branch to branches, point to current head commit
     * @param branchName branch name you want
     * you may not specify a commit, because you can only add a branch reference to head commit
     */
    public void addBranch(String branchName) {
        if(map.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        Head head = Repository.getHead();
        Commit headCommit = head.getCommit();
        map.put(branchName, headCommit.getHash());
    }

    /**
     * Return branch with branchName, null if it doesn't exist
     */
    public String getBranch(String branchName) {
        return map.get(branchName);
    }

    /**
     * save branches to file
     */
    public void saveBranches() {
        Utils.writeObject(BRANCHES, this);
    }

    /**
     * read branches from file
     * @return branches instance
     */
    public static Branches fromFile() {
        Branches branches = Utils.readObject(BRANCHES, Branches.class);
        return branches;
    }

    /**
     * while commit, branch will keep track on new commit.
     */
    public void update(String branch, Commit commit) {
        map.put(branch, commit.getHash());
    }

    /**
     * Display branches, with * in head branch.
     */
    public void display() {
        Set<String> set = map.keySet();
        Object[] arr = set.toArray();
        Arrays.sort(arr);

        System.out.println("=== Branches ===");

        for(Object key: arr)
        {
            if(Repository.getHead().getCurrentBranch().equals(key))
                System.out.print("*");
            System.out.println(key);
        }
        System.out.println();
    }
}
