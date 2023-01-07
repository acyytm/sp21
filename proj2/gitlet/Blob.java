package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 * blobs: The saved contents of files.
 * Since Gitlet saves many versions of files, a single file might correspond to
 * multiple blobs: each being tracked in a different commit.
 * blob can be stored in stage or obj/blob
 */
public class Blob implements Serializable {
    /** File content as a String used to get hash */
    private String hashContent;

    /** real file content. */
    private String fileContent;

    /** File create timestamp. */
    private long timestamp;

    /** File name. */
    private String fileName;

    /** Blob hash value. */
    private String hash;

    /**
     * Constructor, create a blob with fileName in current working dir
     * @param fileName a file name in current working dir
     */
    public Blob(String fileName) {
        File file = Utils.join(Repository.CWD, fileName);
        this.fileName = fileName;
        fileContent = Utils.readContentsAsString(file);
        timestamp = file.lastModified();
        hashContent = "Blob" + fileName + fileContent;
        setHash();
    }

    /**
     * import a blob instance from file
     */
    public static Blob fromFile(File dir, String fileName) {
        File file = Utils.join(dir, fileName);
        Blob blob = Utils.readObject(file, Blob.class);
        return blob;
    }

    /** get hash of this blob instance. */
    public String getHash() {
        return hash;
    }

    /** save blob instance to file. */
    public void saveBlob(File dir) {
        File blob = Utils.join(dir, getHash());
        Utils.writeObject(blob, this);
    }

    /** blob is the abstraction of file, this method write content to file. */
    public void writeToFile(File dir) {
        File file = Utils.join(dir, fileName);
        Utils.writeContents(file, fileContent);
    }

    private String setHash() {
        hash = Utils.sha1(hashContent);
        return hash;
    }

}
