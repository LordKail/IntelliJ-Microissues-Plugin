package uk.ac.glasgow.microissues.plugin;

import com.google.common.base.Charsets;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import uk.ac.glasgow.microissues.fuzzyhash.FuzzyMatch;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class used for storing the data of ticket histories and containing the method for accessing git history.
 */

public class TicketHistory {


    private Ticket mainTicket;

    private LinkedHashMap<OldTicket, PersonIdent> olderVersionTickets;

    public TicketHistory(Ticket ticket){
        this.mainTicket = ticket;
    }

    /**
     * The method for retrieving ticket history. Accesses Git repository and the commits for the project.
     * Finds the diffs which include the corresponding file of the main ticket and tries to find tickets similar to the
     * ones in the current file version.
     * @return olderVersionTickets - a hashmap of older ticket versions.
     */
    public LinkedHashMap<OldTicket, PersonIdent> retrieveTicketHistory() {
        String ticketText = mainTicket.getAssociatedComment().getText();
        if(olderVersionTickets != null){
            return olderVersionTickets;
        }
        else {
            olderVersionTickets = new LinkedHashMap<>();

            JOptionPane.showMessageDialog(null, "Please select the root folder of your application where the .git folder is inside.");

            // Create a file chooser
            JFileChooser fc = new JFileChooser();
            fc.setFileHidingEnabled(false);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


            //In response to a button click:
            int returnVal = fc.showOpenDialog(null);

            File file = fc.getSelectedFile();
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                // Get the git folder.
                File gitFolder = new File(file.getAbsolutePath() + "/.git");
                FileRepository repo = null;
                try {
                    repo = new FileRepository(gitFolder);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Git git = new Git(repo);

                // Iterate through the commits of the repo.
                try {
                    Iterable<RevCommit> commits = git.log().all().call();
                    AbstractTreeIterator oldTreeParser = null;
                    AbstractTreeIterator newTreeParser = null;

                    for (RevCommit commit : commits) {
                        if ((oldTreeParser == null) && (newTreeParser == null)) {
                            oldTreeParser = prepareTree(repo, commit);
                        } else {
                            newTreeParser = prepareTree(repo, commit);
                            List<DiffEntry> diff = git.diff().
                                    setOldTree(oldTreeParser).
                                    setNewTree(newTreeParser).
                                    call();

                            // Goes through each diff
                            for (DiffEntry entry : diff) {
                                Path p = Paths.get(entry.getNewPath());
                                // Checks if a file of the corresponding diff is a java file.
                                if (p.getFileName().toString().endsWith(".java")) {
                                    if (p.getFileName().toString().equals(mainTicket.getAssociatedFile())) {
                                        // Checks if the file in diff is the same as te main ticket's file.
                                        ObjectLoader loader = repo.open(entry.getNewId().toObjectId());
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        loader.copyTo(baos);
                                        String oldFile = new String(baos.toByteArray(), Charsets.UTF_8);

                                        // Pattern for detecting comments
                                        String pattern = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";
                                        Pattern r = Pattern.compile(pattern);
                                        Matcher m = r.matcher(oldFile);

                                        while (m.find()) {
                                            if (m.group(0).contains("@tckt")) {
                                                // Finds the ratio between the main ticket's text and the diff ticket
                                                int ratio = FuzzyMatch.getRatio(m.group(0),
                                                        ticketText, false);

                                                // Arbitrary ratio to consider one ticket similar enough to another one.
                                                if (ratio > 70) {
                                                    if (ratio != 100) {
                                                        OldTicket olderVersion = new OldTicket(mainTicket, commit.getCommitterIdent());
                                                        olderVersion.buildIssue(m.group(0));
                                                        Ticket comparisonTicket = new Ticket();
                                                        comparisonTicket.buildIssue(ticketText);

                                                        // Additional check to check how similar the summaries are.
                                                        if (FuzzyMatch.getRatio(olderVersion.getSummary(), comparisonTicket.getSummary(), false) > 70) {
                                                            olderVersionTickets.put(olderVersion, commit.getCommitterIdent());
                                                            ticketText = m.group(0);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                            oldTreeParser = newTreeParser;
                        }
                    }
                } catch (GitAPIException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            return olderVersionTickets;
        }
    }

    /**
     *
     * @param repo - the Git repository instance.
     * @param commit - The corresponding commit of a project.
     * @return AbstractTreeIterator - the tree iterator to be used for iterating through the diffs of a particular commit.
     */
    private AbstractTreeIterator prepareTree(FileRepository repo, RevCommit commit) {
        try (RevWalk walk = new RevWalk(repo)) {
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = repo.newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            }

            walk.dispose();

            return oldTreeParser;

        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (MissingObjectException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setOlderVersionTickets(LinkedHashMap<OldTicket, PersonIdent> olderVersionTickets) {
        this.olderVersionTickets = olderVersionTickets;
    }


}
