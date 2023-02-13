package gitlet;

import java.io.FileFilter;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.File;
public class Repository {

    public Repository() {
        wd = new File(".");
        File head = new File(".gitlet/branches/HEAD.txt");
        if (head.exists()) {
            _head = Utils.readContentsAsString(head);
        }
        File stageCheck = new File(".gitlet/staging/stage.txt");
        if (stageCheck.exists()) {
            stage = Utils.readObject(stageCheck, Stage.class);
        }
        File commitGer = new File(".gitlet/graph/commitG.txt");
        if (commitGer.exists()) {
            commitG = Utils.readObject(commitGer, Graph.class);
        }
    }

    public void init() {
        File git = new File(".gitlet");
        if (git.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            git.mkdirs();
            _head = "master";
            stage = new Stage();
            Utils.join(git, "branches").mkdirs();
            Utils.join(git, "commits").mkdirs();
            Utils.join(git, "blobs").mkdirs();
            Utils.join(git, "staging").mkdirs();
            Utils.join(git, "graph").mkdirs();
            commitG = new Graph(new HashMap<String, ArrayList<String>>());
            Commit initCommit = new Commit("initial commit",
                    null, new TreeMap<String, String>());
            Utils.writeObject(new File(".gitlet/commits/"
                    + initCommit.getSelfID() + ".txt"), initCommit);
            Utils.writeContents(new File(".gitlet/branches/master.txt"),
                    initCommit.getSelfID());
            Utils.writeContents(new File(".gitlet/branches/HEAD.txt"),
                    "master");
            Utils.writeObject(new File(".gitlet/graph/commitG.txt"),
                    commitG);
            updateStage();
        }
    }

    public void add(String fileName) {
        File addend = new File(fileName);
        if (!addend.exists()) {
            System.out.println("File does not exist.");
        } else {
            Blob blob = new Blob(addend);
            if (getCurrentCommit().getBlobs().containsKey(addend.getName())
                && getCurrentCommit().getBlobs().
                    get(fileName).equals(blob.getSelfID())) {
                if (stage.getRemovals().contains(fileName)) {
                    stage.getRemovals().remove(fileName);
                    updateStage();
                }
                return;
            }
            if (stage.getRemovals().contains(fileName)) {
                stage.getRemovals().remove(fileName);
            }
            Utils.writeObject(new File(".gitlet/blobs/"
                    + blob.getSelfID() + ".txt"), blob);
            stage.add(fileName, blob.getSelfID());
            updateStage();
        }
    }

    public void updateStage() {
        Utils.writeObject(new File(".gitlet/staging/stage.txt"), stage);
    }

    public Commit getCurrentCommit() {
        String headBranch = Utils.readContentsAsString(
                new File(".gitlet/branches/" + _head + ".txt"));
        return Utils.readObject(new File(".gitlet/commits/"
                + headBranch + ".txt"), Commit.class);
    }

    public void commit(String message) {
        if (message == null || message.length() == 0) {
            System.out.println("Please enter a commit message.");
        } else if (stage.getRemovals().isEmpty()
                && stage.getAdditions().isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else {
            TreeMap<String, String> savedBlobsi = getCurrentCommit().getBlobs();
            TreeMap<String, String> savedBlobs = new TreeMap<>();
            for (String s: savedBlobsi.keySet()) {
                savedBlobs.put(s, savedBlobsi.get(s));
            }
            for (String file: stage.getAdditions().keySet()) {
                savedBlobs.put(file, stage.getAdditions().get(file));
            }
            for (String file: stage.getRemovals()) {
                savedBlobs.remove(file);
            }
            stage.clear();
            updateStage();
            Commit newCommit = new Commit(message,
                    getCurrentCommit().getSelfID(), savedBlobs);
            Utils.writeObject(new File(".gitlet/commits/"
                    + newCommit.getSelfID() + ".txt"), newCommit);
            ArrayList<String> parents = new ArrayList<>();
            parents.add(getCurrentCommit().getSelfID());
            Utils.writeContents(new File(".gitlet/branches/"
                    + _head + ".txt"), newCommit.getSelfID());
            commitG.getMap().put(newCommit.getSelfID(), parents);
            Utils.writeObject(new File(".gitlet/graph/commitG.txt"),
                    commitG);
        }
    }

    public void log() {
        Commit commie = getCurrentCommit();
        while (true) {
            System.out.println("===");
            System.out.println("commit " + commie.getSelfID());
            System.out.println("Date: " + commie.getTimeStamp());
            System.out.println(commie.getMessage());
            System.out.println();
            if (commie.getParentID() != null) {
                commie = Utils.readObject(new File(".gitlet/commits/"
                        + commie.getParentID() + ".txt"), Commit.class);
            } else {
                break;
            }
        }
    }

    public void checkout(String[] args) {
        switch (args.length) {
        case 2:
            checkoutCase1(args[1]);
            break;
        case 3:
            checkoutCase2(args);
            break;
        case 4:
            checkoutCase3(args);
            break;
        default:
            break;
        }
    }

    public void checkoutCase1(String branch) {
        File branchFile = new File(".gitlet/branches/" + branch + ".txt");
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        } else if (branch.equals(_head)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String branchCommitID = Utils.readContentsAsString(branchFile);
        Commit branchCommit = Utils.readObject(new File(
                ".gitlet/commits/" + branchCommitID + ".txt"), Commit.class);
        Commit currentCommit = getCurrentCommit();
        for (File file: wd.listFiles(txtFiles)) {
            if (!currentCommit.getBlobs().containsKey(file.getName())
                && branchCommit.getBlobs().containsKey(file.getName())) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        for (File file: wd.listFiles(txtFiles)) {
            Utils.restrictedDelete(file);
        }
        for (String fileName: branchCommit.getBlobs().keySet()) {
            String blobID = branchCommit.getBlobs().get(fileName);
            File newFile = new File(wd.getPath()
                    + "/.gitlet/blobs/" + blobID + ".txt");
            Blob blob = Utils.readObject(newFile, Blob.class);
            Utils.writeContents(
                    new File(wd.getPath(), fileName), blob.getSelf());
        }
        stage.clear();
        updateStage();
        Utils.writeContents(new File(".gitlet/branches/HEAD.txt"), branch);
    }

    /** TXTFILES ensures every file is of the txt type. */
    private FileFilter txtFiles = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".txt");
        }
    };

    public void checkoutCase2(String[] args) {
        TreeMap<String, String> commits = getCurrentCommit().getBlobs();
        String fileName = args[2];
        if (!commits.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
        } else {
            if (new File(wd.getPath() + fileName).exists()) {
                Utils.restrictedDelete(wd.getPath() + fileName);
            }
            File file = new File(wd.getPath()
                    + "/.gitlet/blobs/" + commits.get(fileName) + ".txt");
            String content = Utils.readObject(file, Blob.class).getSelf();
            Utils.writeContents(new File(wd.getPath(), fileName), content);
        }
    }

    public void checkoutCase3(String[] args) {
        String id = args[1];
        ArrayList<String> commitList = new ArrayList<>();
        for (File file: new File(".gitlet/commits/").listFiles(txtFiles)) {
            if (file.getName().contains(id)) {
                id = file.getName().substring(0, file.getName().length() - 4);
            }
        }
        String fileName = args[3];
        File filo = new File(".gitlet/commits/" + id + ".txt");
        if (!filo.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject(new File(
                ".gitlet/commits/" + id + ".txt"), Commit.class);
        if (!commit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
        } else {
            if (new File(wd.getPath() + fileName).exists()) {
                Utils.restrictedDelete(wd.getPath() + fileName);
            }
            String aid = commit.getBlobs().get(fileName);
            File newFile = new File(wd.getPath()
                    + "/.gitlet/blobs/" + aid + ".txt");
            Blob blob = Utils.readObject(newFile, Blob.class);
            Utils.writeContents(
                    new File(wd.getPath(), fileName), blob.getSelf());
        }
    }

    public void remove(String fileName) {
        if (!(stage.getAdditions().containsKey(fileName)
                || getCurrentCommit().getBlobs().containsKey(fileName))) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (stage.getAdditions().containsKey(fileName)) {
            stage.getAdditions().remove(fileName);
        }
        if (getCurrentCommit().getBlobs().containsKey(fileName)) {
            stage.remove(fileName);
            Utils.restrictedDelete(fileName);
        }
        updateStage();
    }

    public void globalLog() {
        List<String> commitIDs = Utils.plainFilenamesIn(
                new File(".gitlet/commits"));
        for (String commit: commitIDs) {
            Commit commie = Utils.readObject(
                    new File(".gitlet/commits/" + commit), Commit.class);
            System.out.println("===");
            System.out.println("commit " + commie.getSelfID());
            System.out.println("Date: " + commie.getTimeStamp());
            System.out.println(commie.getMessage());
            System.out.println();
        }
    }

    public void find(String message) {
        List<String> commitIDs = Utils.plainFilenamesIn(
                new File(".gitlet/commits"));
        boolean checker = true;
        for (String commit: commitIDs) {
            Commit commie = Utils.readObject(
                    new File(".gitlet/commits/" + commit), Commit.class);
            if (commie.getMessage().equals(message)) {
                System.out.println(commit.substring(0, commit.length() - 4));
                checker = false;
            }
        }
        if (checker) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        if (!new File(".gitlet/branches/HEAD.txt").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        List<String> branches = Utils.plainFilenamesIn(
                new File(".gitlet/branches"));
        String headBranch = Utils.readContentsAsString(
                new File(".gitlet/branches/HEAD.txt"));
        System.out.println("*" + headBranch);
        java.util.Collections.sort(branches);
        for (String s: branches) {
            if (!(s.equals("HEAD.txt") || s.equals(headBranch + ".txt"))) {
                System.out.println(s.substring(0, s.length() - 4));
            }
        }
        System.out.println();
        List<String> stagedFiles = new ArrayList<>();
        List<String> removedFiles = new ArrayList<>();
        List<String> modifiedFiles = new ArrayList<>();
        List<String> untrackedFiles = new ArrayList<>();
        for (String s: stage.getAdditions().keySet()) {
            String objectifiedS = stage.getAdditions().get(s);
            if (!new File(wd.getPath() + "/" + s).exists()) {
                modifiedFiles.add(s + " (deleted)");
            } else if (!objectifiedS.equals(new Blob(
                    new File(wd.getPath() + "/" + s)).getSelfID())) {
                modifiedFiles.add(s + " (modified)");
            } else {
                stagedFiles.add(s);
            }
        }
        for (String s: stage.getRemovals()) {
            removedFiles.add(s);
        }
        Commit currentCommit = getCurrentCommit();
        modifiedFiler(modifiedFiles, stagedFiles, removedFiles);
        for (String s: Utils.plainFilenamesIn(wd)) {
            if (!(currentCommit.getBlobs().containsKey(s)
                    || stagedFiles.contains(s)) && s.endsWith(".txt")) {
                untrackedFiles.add(s);
            }
        }
        statusHelper(stagedFiles, removedFiles, modifiedFiles, untrackedFiles);
    }

    private void modifiedFiler(List<String> modifiedFiles,
                               List<String> stagedFiles,
                               List<String> removedFiles) {
        Commit current = getCurrentCommit();
        ArrayList<String> workingFiles = new ArrayList<>();
        for (File file: wd.listFiles(txtFiles)) {
            workingFiles.add(file.getName());
        }
        for (String s: current.getBlobs().keySet()) {
            String blobID = current.getBlobs().get(s);
            String contents = Utils.readObject(new File(
                    ".gitlet/blobs/" + blobID + ".txt"), Blob.class).getSelf();
            if (!workingFiles.contains(s)
                    && !stage.getRemovals().contains(s)) {
                modifiedFiles.add(s + " (deleted)");
            } else if (workingFiles.contains(s)
                    && !contents.equals(Utils.
                    readContentsAsString(new File(wd.getPath(), s)))
                    && !stage.getAdditions().containsKey(s)) {
                modifiedFiles.add(s + " (modified)");
            }
        }
        for (String s: stage.getAdditions().keySet()) {
            String contents = Utils.readObject(new File(
                    ".gitlet/blobs/" + stage.getAdditions().get(s)
                            + ".txt"), Blob.class).getSelf();
            if (!contents.equals(Utils.
                    readContentsAsString(new File(wd.getPath(), s)))) {
                modifiedFiles.add(s + " (modified)");
            }
        }
    }

    private void statusHelper(List<String> stagedFiles,
                              List<String> removedFiles,
                              List<String> modifiedFiles,
                              List<String> untrackedFiles) {
        java.util.Collections.sort(stagedFiles);
        java.util.Collections.sort(removedFiles);
        java.util.Collections.sort(modifiedFiles);
        java.util.Collections.sort(untrackedFiles);
        System.out.println("=== Staged Files ===");
        for (String s: stagedFiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String s: removedFiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String s: modifiedFiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String s: untrackedFiles) {
            System.out.println(s);
        }
    }

    public void branch(String b) {
        File branchFile = new File(wd.getPath()
                + "/.gitlet/branches/" + b + ".txt");
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Utils.writeContents(branchFile, getCurrentCommit().getSelfID());
    }

    public void removeBranch(String b) {
        File branchFile = new File(wd.getPath()
                + "/.gitlet/branches/" + b + ".txt");
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (Utils.readContentsAsString(branchFile).
                equals(getCurrentCommit().getSelfID())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branchFile.delete();
    }

    public void reset(String commitID) {
        File commitFile = new File(".gitlet/commits/" + commitID + ".txt");
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        for (File file: wd.listFiles(txtFiles)) {
            if (!getCurrentCommit().getBlobs().containsKey(file.getName())
                    && commit.getBlobs().containsKey(file.getName())) {
                System.out.println("There is an untracked file in "
                        + "the way; delete it, or add and commit it first.");
                return;
            }
        }
        for (File file: wd.listFiles(txtFiles)) {
            Utils.restrictedDelete(file);
        }
        for (String fileName: commit.getBlobs().keySet()) {
            String blobID = commit.getBlobs().get(fileName);
            File newFile = new File(wd.getPath()
                    + "/.gitlet/blobs/" + blobID + ".txt");
            Blob blob = Utils.readObject(newFile, Blob.class);
            Utils.writeContents(
                    new File(wd.getPath(), fileName), blob.getSelf());
        }
        stage.clear();
        updateStage();
        Utils.writeContents(new File(".gitlet/branches/"
                + _head + ".txt"), commitID);
    }

    private Commit splitPoint(String branch) {
        metroplex = new ArrayList<>();
        trypticon = new ArrayList<>();
        String optimus = getCurrentCommit().getSelfID();
        String megatron = Utils.readContentsAsString(
                new File(".gitlet/branches/" + branch + ".txt"));
        findAutobots(optimus, 0);
        findDecepticons(megatron);
        Cybertronian spymaster = null;
        for (Cybertronian decepticon: trypticon) {
            if (spymaster == null
                    || decepticon.getDepth() <= spymaster.getDepth()) {
                spymaster = decepticon;
            }
        }
        return Utils.readObject(new File(".gitlet/commits/"
                + spymaster.designation + ".txt"), Commit.class);
    }

    private void findDecepticons(String commit) {
        for (Cybertronian tf: metroplex) {
            if (tf.getDesignation().equals(commit)) {
                trypticon.add(tf);
                return;
            }
        }
        if (commitG.getMap().containsKey(commit)) {
            ArrayList<String> parents = commitG.getMap().get(commit);
            for (String parent: parents) {
                findDecepticons(parent);
            }
        }
    }

    private void findAutobots(String commit, int d) {
        for (Cybertronian tf: metroplex) {
            if (tf.getDesignation().equals(commit)) {
                return;
            }
        }
        metroplex.add(new Cybertronian(commit, d));
        if (commitG.getMap().containsKey(commit)) {
            ArrayList<String> parents = commitG.getMap().get(commit);
            for (String parent: parents) {
                findAutobots(parent, d + 1);
            }
        }
    }

    private class Cybertronian {
        Cybertronian(String commit, int d) {
            designation = commit;
            depth = d;
        }

        public String getDesignation() {
            return designation;
        }

        public int getDepth() {
            return depth;
        }

        /** Variable for DESIGNATION. */
        private String designation;

        /** Variable for DEPTH. */
        private int depth;
    }
    private Boolean mergeHelper1(String branch) {
        if (!(stage.getRemovals().isEmpty()
                && stage.getAdditions().isEmpty())) {
            System.out.println("You have uncommitted changes.");
            return true;
        } else if (!new File(".gitlet/branches/" + branch + ".txt").exists()) {
            System.out.println("A branch with that name does not exist.");
            return true;
        } else {
            Commit current = getCurrentCommit();
            String branchedID = Utils.readContentsAsString(
                    new File(".gitlet/branches/" + branch + ".txt"));
            Commit brancher = Utils.readObject(new File(
                    ".gitlet/commits/" + branchedID + ".txt"), Commit.class);
            if (branchedID.equals(current.getSelfID())) {
                System.out.println("Cannot merge a branch with itself.");
                return true;
            }
        }
        return false;
    }

    public void merge(String branch) {
        Commit current = getCurrentCommit();
        if (mergeHelper1(branch)) {
            return;
        }
        String branchedID = Utils.readContentsAsString(
                new File(".gitlet/branches/" + branch + ".txt"));
        Commit brancher = Utils.readObject(new File(
                ".gitlet/commits/" + branchedID + ".txt"), Commit.class);
        for (File file: wd.listFiles(txtFiles)) {
            if (!current.getBlobs().containsKey(file.getName())
                    && brancher.getBlobs().containsKey(file.getName())) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
                return;
            }
        }
        Commit splitPoint = splitPoint(branch);
        if (splitPoint.getSelfID().equals(branchedID)) {
            System.out.println("Given branch is "
                    + "an ancestor of the current branch.");
            return;
        }
        if (splitPoint.getSelfID().equals(current.getSelfID())) {
            checkoutCase1(branch);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        Commit branched = Utils.readObject(new File(
                ".gitlet/commits/" + branchedID + ".txt"), Commit.class);
        TreeMap<String, String> currentBlobs = current.getBlobs();
        TreeMap<String, String> splitBlobs = splitPoint.getBlobs();
        TreeMap<String, String> branchBlobs = branched.getBlobs();
        Boolean conflict = false;
        ArrayList<String> files = new ArrayList<>();
        for (String file: currentBlobs.keySet()) {
            files.add(file);
        }
        for (String file: splitBlobs.keySet()) {
            if (!files.contains(file)) {
                files.add(file);
            }
        }
        for (String file: branchBlobs.keySet()) {
            if (!files.contains(file)) {
                files.add(file);
            }
        }
        if (mergeHelper3(splitBlobs, currentBlobs, branchBlobs)) {
            return;
        }
        conflict = mergeHelper2(files, splitBlobs,
                currentBlobs, branchBlobs, branchedID);
        commit("Merged " + branch + " into " + _head + ".");
        commitG.getMap().get(getCurrentCommit().getSelfID()).add(branchedID);
        updateGraph();
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private void updateGraph() {
        Utils.writeObject(new File(".gitlet/graph/commitG.txt"),
                commitG);
    }

    private Boolean mergeHelper2(ArrayList<String> files,
                         TreeMap<String, String> splitBlobs,
                         TreeMap<String, String> currentBlobs,
                         TreeMap<String, String> branchBlobs,
                         String branchedID) {
        String sFile, bFile, cFile;
        Boolean conflict = false;
        for (String file: files) {
            sFile = bFile = cFile = "Stefan";
            if (splitBlobs.containsKey(file)
                    && splitBlobs.get(file) != null) {
                sFile = splitBlobs.get(file);
            }
            if (currentBlobs.containsKey(file)
                    && currentBlobs.get(file) != null) {
                cFile = currentBlobs.get(file);
            }
            if (branchBlobs.containsKey(file)
                    && branchBlobs.get(file) != null) {
                bFile = branchBlobs.get(file);
            }
            if (cFile.equals(sFile)
                    && !bFile.equals(sFile)) {
                if (bFile.equals("Stefan")) {
                    remove(file);
                } else {
                    checkoutCase3(new String[]{"", branchedID, "--", file});
                    stage.add(file, bFile);
                }
            } else if (!cFile.equals(sFile)
                    && !cFile.equals(bFile)
                    && !bFile.equals(sFile)) {
                conflict = true;
                String bContents, cContents;
                bContents = cContents = "";
                if (!bFile.equals("Stefan")) {
                    bContents =
                            Utils.readObject(new File(".gitlet/blobs/"
                                    + bFile + ".txt"), Blob.class).getSelf();
                }
                if (!cFile.equals("Stefan")) {
                    cContents =
                            Utils.readObject(new File(".gitlet/blobs/"
                                    + cFile + ".txt"), Blob.class).getSelf();
                }
                String content = "<<<<<<< HEAD\n" + cContents
                        + "=======\n" + bContents + ">>>>>>>\n";
                Utils.writeContents(new File(wd.getPath(), file), content);
                add(file);
            }
        }
        return conflict;
    }

    private boolean mergeHelper3(TreeMap<String, String> splitBlobs,
                              TreeMap<String, String> currentBlobs,
                              TreeMap<String, String> branchBlobs) {
        Boolean cancel = false;
        String sFile, bFile, cFile;
        ArrayList<String> filesWD = new ArrayList<>();
        for (File file: wd.listFiles(txtFiles)) {
            filesWD.add(file.getName());
        }
        for (String file: filesWD) {
            sFile = cFile = bFile = "Stefan";
            if (splitBlobs.containsKey(file) && splitBlobs.get(file) != null) {
                sFile = splitBlobs.get(file);
            }
            if (currentBlobs.containsKey(file)
                    && currentBlobs.get(file) != null) {
                cFile = currentBlobs.get(file);
            }
            if (branchBlobs.containsKey(file)
                    && branchBlobs.get(file) != null) {
                bFile = branchBlobs.get(file);
            }
            if (cFile.equals(sFile)
                    && !bFile.equals(sFile)
                    && cFile.equals("Stefan")) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                cancel = true;
            }
        }
        return cancel;
    }

    /** Variable for HEAD. */
    private String _head;

    /** Variable for WD, working directory. */
    private File wd;

    /** Variable for STAGE. */
    private Stage stage;

    /** Variable for commitG. */
    private Graph commitG;

    /** Variable for METROPLEX, an Autobot city-transformer. */
    private ArrayList<Cybertronian> metroplex;

    /** Variable for TRYPTICON, a Decepticon city-transformer. */
    private ArrayList<Cybertronian> trypticon;
}
