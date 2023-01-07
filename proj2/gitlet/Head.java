package gitlet;

import java.io.File;
import java.io.Serializable;

/**
 * Head is a hash code pointer to current working status
 */
public class Head implements Serializable {
    /** point to current working status, which is a hash code (sha1) */
    private String current;

    public Head(Commit commit) {
        current = commit.getHash();
    }
    public void pointTo(Commit commit) {
        current = commit.getHash();
    }

    public Commit getCommit() {
        Commit head = Commit.fromFile(current);
        return head;
    }

    public void saveHead() {
        File head = Utils.join(Repository.GITLET_DIR, "Head");
        Utils.writeObject(head, this);
    }

    public static Head fromFile() {
        File file = Utils.join(Repository.GITLET_DIR, "Head");
        Head head = Utils.readObject(file, Head.class);
        return head;
    }

}
