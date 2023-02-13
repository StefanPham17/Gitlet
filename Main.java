package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Stefan Pham
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            Repository repo = new Repository();
            switch (args[0]) {
            case "init":
                repo.init();
                break;
            case "add":
                repo.add(args[1]);
                break;
            case "commit":
                repo.commit(args[1]);
                break;
            case "log":
                repo.log();
                break;
            case "checkout":
                if ((args.length == 3 && args[1].equals("--"))
                        || (args.length == 4 && args[2].equals("--"))
                        || (args.length == 2)) {
                    repo.checkout(args);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "rm":
                repo.remove(args[1]);
                break;
            case "global-log":
                repo.globalLog();
                break;
            case "find":
                repo.find(args[1]);
                break;
            case "status":
                repo.status();
                break;
            case "branch":
                repo.branch(args[1]);
                break;
            case "rm-branch":
                repo.removeBranch(args[1]);
                break;
            case "reset":
                repo.reset(args[1]);
                break;
            case "merge":
                repo.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
            }
        }
    }
}
