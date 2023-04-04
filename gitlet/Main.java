package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Harshaan Sall
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            return;
        }
        switch (args[0]) {
        case "init":
            Repo.init();
            break;
        case "add":
            Repo.add(args[1]);
            break;
        case "commit":
            Repo.commit(args[1]);
            break;
        case "checkout":
            if (args.length == 3) {
                Repo.checkout(args[2]);
                break;
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                }
                Repo.checkout(args[1], args[3]);
                break;
            } else {
                Repo.checkoutBranch(args[1]);
                break;
            }
        case "log":
            Repo.log();
            break;
        default:
            mainOverflow(args);
        }
    }

    public static void mainOverflow(String... args) {
        switch (args[0]) {
        case "global-log":
            Repo.globaLog();
            break;
        case "find":
            Repo.find(args[1]);
            break;
        case "rm":
            Repo.rm(args[1]);
            break;
        case "status":
            Repo.status();
            break;
        case "branch":
            Repo.branch(args[1]);
            break;
        case "rm-branch":
            Repo.rmBranch(args[1]);
            break;
        case "reset":
            Repo.reset(args[1]);
            break;
        case "merge":
            Repo.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }
}
