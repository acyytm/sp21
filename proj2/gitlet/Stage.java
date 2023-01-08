package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Staging Area for gitlet
 */
public class Stage implements Serializable {
    /** Staging Area directory. */
    public static final File STAGE_DIR = Utils.join(Repository.GITLET_DIR, "staging");

    /** stage file. */
    private HashMap<String, String> files;

    public Stage() {
        files = new HashMap<>();
    }

    /** Add a file to staging area. */
    public void add(String fileName) {
        File file = Utils.join(Repository.CWD, fileName);

        if(!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Blob blob = new Blob(fileName);
        Commit headCommit = Repository.getHead().getCommit();

        /* file in current commit. */
        if(blob.getHash().equals(headCommit.getFileHash(fileName))) {
            return;
        }
        /* stage is empty. */
        else if(files == null) {
            files = new HashMap<>();
            files.put(fileName, blob.getHash());
            blob.saveBlob(STAGE_DIR);
        }
        /* file has been added. */
        else if(files.containsKey(fileName)) {
            /* file changed. delete old file and create a new one*/
            if(!files.get(fileName).equals(blob.getHash())) {
                File oldFile = Utils.join(STAGE_DIR, files.get(fileName));
                blob.saveBlob(STAGE_DIR);
                files.put(fileName, blob.getHash());
            }
        /* stage didn't add this file, create it. */
        }else {
            files.put(fileName, blob.getHash());
            blob.saveBlob(STAGE_DIR);
        }
    }

    public void saveStage() {
        File file = Utils.join(Repository.GITLET_DIR, "STAGE");
        Utils.writeObject(file, this);
    }

    public static Stage fromFile() {
        File file = Utils.join(Repository.GITLET_DIR, "STAGE");
        Stage stage = Utils.readObject(file, Stage.class);
        return stage;
    }

    /** Return if stage is now empty. */
    public boolean empty() {
        if(files == null)
            return true;
        return files.isEmpty();
    }

    /** Get staging files. */
    public HashMap<String, String> getFiles() {
        return files;
    }

    /** Delete all stage files. */
    public void deleteFiles() {
        for(Map.Entry<String, String> entry: files.entrySet())
        {
            File file = Utils.join(STAGE_DIR, entry.getValue());
            file.delete();
        }
        files.clear();
    }

    /** Return if contains specific file, using fileName. */
    public boolean contain(String fileName) {
        return files.containsKey(fileName);
    }

    /** Delete specific file, using fileName. */
    public void removeFromStage(String fileName) {
        files.remove(fileName);
    }

    /** Display staged files. */
    public void display() {
        Set<String> set = files.keySet();
        Object[] arr = set.toArray();
        Arrays.sort(arr);

        System.out.println("=== Staged Files ===");

        for(Object key: arr)
        {
            System.out.println(key);
        }
        System.out.println();
    }
}
