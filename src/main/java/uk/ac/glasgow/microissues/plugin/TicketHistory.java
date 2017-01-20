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
 * A class used for storing the data of ticket histories.
 */

public class TicketHistory {

    private Ticket mainTicket;
    LinkedHashMap<Ticket, PersonIdent> olderVersionTickets;

    public TicketHistory(Ticket ticket){
        this.mainTicket = ticket;
    }

    public LinkedHashMap<Ticket, PersonIdent> retrieveTicketHistory() {
        if(olderVersionTickets != null){
            System.out.println("OlderVersions is not null!");
            return olderVersionTickets;
        }
        else {
            System.out.println("OlderVersions is null, creating a list.");
            olderVersionTickets = new LinkedHashMap<>();
            File gitFolder = new File("C:\\Users\\Al3x\\IdeaProjects\\Microissues" + "\\.git");
            FileRepository repo = null;
            try {
                repo = new FileRepository("C:\\Users\\Al3x\\IdeaProjects\\Microissues" + "\\.git");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Git git = new Git(repo);
            RevWalk walk = new RevWalk(repo);
            System.out.println("Commits of repo: ");
            System.out.println("-------------------------------------");

            try {
                Iterable<RevCommit> commits = git.log().all().call();
                AbstractTreeIterator oldTreeParser = null;
                AbstractTreeIterator newTreeParser = null;
                for (RevCommit commit : commits) {
                    System.out.println("Printing info for commit: " + commit.getName());
                    if ((oldTreeParser == null) && (newTreeParser == null)) {
                        System.out.println("Created tree for old tree");
                        oldTreeParser = prepareTree(repo, commit);
                    }

                    else {
                        newTreeParser = prepareTree(repo, commit);
                        List<DiffEntry> diff = git.diff().
                                setOldTree(oldTreeParser).
                                setNewTree(newTreeParser).
                                call();
                        System.out.println("Affected files: ");
                        for (DiffEntry entry : diff) {
                            Path p = Paths.get(entry.getNewPath());
                            System.out.println("Associated file in ticket: " + mainTicket.getAssociatedFile());
                            System.out.println("File name the ticket that was rclicked on: " + p.getFileName());
                            if(p.getFileName().toString().equals(mainTicket.getAssociatedFile())) {
                                System.out.println(entry.getNewId());
                                ObjectLoader loader = repo.open(entry.getNewId().toObjectId());
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                loader.copyTo(baos);
                                String oldFile = new String(baos.toByteArray(), Charsets.UTF_8);

                                // Pattern for detecting comments
                                String pattern = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";
                                Pattern r = Pattern.compile(pattern);
                                Matcher m = r.matcher(oldFile);

                                while(m.find()){
                                    if(m.group(0).contains("@tckt")){
                                        System.out.println("FOUND TICKET;");
                                        int ratio = FuzzyMatch.getRatio(m.group(0),
                                                mainTicket.getAssociatedComment().getText(), false);
                                        System.out.println("FUZZY MATCH RATIO: " + ratio);
                                        System.out.println("Between: \n" + mainTicket.getAssociatedComment().getText());
                                        System.out.println(m.group(0));
                                        System.out.println("Ratio: " + ratio);
                                        if(ratio>45){
                                            if(ratio != 100) {
                                                Ticket olderVersion = new Ticket();
                                                olderVersion.buildIssue(m.group(0));
                                                olderVersionTickets.put(olderVersion, commit.getCommitterIdent());
                                            }
                                        }
                                    }
                                    System.out.println("FOUND COMMENT: " + m.group(0));
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
            return olderVersionTickets;
        }
    }

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

}
