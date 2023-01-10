package gitlet;

import java.net.ResponseCache;
import java.util.ResourceBundle;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {
    // TODO: command: merge

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs("add", args, 2);  /** notice that gitlet can only add one file at a time */
                Repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                validateMessage(args);
                Repository.commit(args[1]);
                break;
            case "checkout":
                checkout(args);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                Repository.log();
                break;
            case "branch":
                validateNumArgs("branch", args, 2);
                Repository.branch(args[1]);
                break;
            case "rm":
                validateNumArgs("rm", args, 2);
                Repository.rm(args[1]);
                break;
            case "status":
                validateNumArgs("status", args, 1);
                Repository.status();
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs("find", args, 2);
                Repository.find(args[1]);
                break;
            case "reset":
                validateNumArgs("reset", args, 2);
                Repository.reset(args[1]);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch", args, 2);
                Repository.removeBranch(args[1]);
                break;
            case "merge":
                validateNumArgs("merge", args, 2);
                Repository.merge(args[1]);
                break;
            case "":
                System.out.println("Please enter a command.");
                break;
            default:
                System.out.println("No command with that name exists.");
        }

    }

    public static void validateNumArgs(String cmd, String [] args, int num) {
        if(num != args.length) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void validateMessage(String[] args) {
        validateNumArgs("commit", args, 2);
        if(args[1].isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
    }

    public static void checkout(String[] args){
        //checkout id
        if(args.length == 2) { // delete all file that current commit tracked and rewrite all file that new branch tracked
            Repository.checkoutBranch(args[1]);
        }
        // checkout -- [file name]
        if(args.length == 3) {
            Repository.checkout(args[2]);
        }
        //checkout id -- [file name]
        if(args.length == 4) {
            if(!args[2].equals("--")) {
                System.out.println("Incorrect operands");
                System.exit(0);
            }
            Repository.checkout(args[3], args[1]);
        }
    }
}
