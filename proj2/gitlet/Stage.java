package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Staging Area for gitlet
 */
public class Stage implements Serializable {
    /** Staging Area directory. */
    public static final File STAGE_DIR = Utils.join(Repository.GITLET_DIR, "staging");

    /** stage file. */
    private HashMap<String, String> files;

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
        return files.isEmpty();
    }

    /** Get staging files. */
    public HashMap<String, String> getFiles() {
        return files;
    }

    public void deleteFiles() {
        for(Map.Entry<String, String> entry: files.entrySet())
        {
            File file = Utils.join(STAGE_DIR, entry.getValue());
            file.delete();
        }
        files.clear();
    }
}
