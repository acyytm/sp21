package gitlet;

import java.io.File;

public class test {
    public static void main(String[] args) {
        File headFile = Utils.join(Repository.GITLET_DIR, "Head");
        Head head = Utils.readObject(headFile, Head.class);
    }
}
