package uk.ac.glasgow.microissues.plugin;

import com.google.common.base.Charsets;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import uk.ac.glasgow.microissues.fuzzyhash.FuzzyMatch;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Al3x on 27/11/2016.
 */
public class TreeRClickMenuListener implements ActionListener {
    Git git;
    ToolWindow window;
    DefaultMutableTreeNode selectedElement;

    public TreeRClickMenuListener(ToolWindow window, DefaultMutableTreeNode selectedElement) {
        this.window = window;
        this.selectedElement = selectedElement;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // Was used for displaying information in the info window. Currently a proof of context and might be removed.
        System.out.println("SELECTED:" + e.getActionCommand());
        ArrayList<String> olderVersions = new ArrayList<>();
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

                else{
                    newTreeParser = prepareTree(repo, commit);
                    List<DiffEntry> diff = git.diff().
                            setOldTree(oldTreeParser).
                            setNewTree(newTreeParser).
                            call();
                    System.out.println("Affected files: ");
                    for (DiffEntry entry : diff) {
                        Path p = Paths.get(entry.getNewPath());
                        Ticket ticket = (Ticket) selectedElement.getUserObject();
                        System.out.println("Associated file in ticket: " + ticket.getAssociatedFile());
                        System.out.println("File name the ticket that was rclicked on: " + p.getFileName());
                        if(p.getFileName().toString().equals(ticket.getAssociatedFile())) {
                            System.out.println(entry.getNewId());
                            ObjectLoader loader = repo.open(entry.getNewId().toObjectId());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            loader.copyTo(baos);
                            String oldFile = new String(baos.toByteArray(), Charsets.UTF_8);
                            System.out.println(FuzzyMatch.getRatio("Hi", "Hi", false));
                            String pattern = "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/";
                            Pattern r = Pattern.compile(pattern);
                            Matcher m = r.matcher(oldFile);

                            while(m.find()){
                                if(m.group(0).contains("@tckt")){
                                    System.out.println("FOUND TICKET;");
                                    int ratio = FuzzyMatch.getRatio(m.group(0),
                                            ticket.getAssociatedComment().getText(), false);
                                    System.out.println("FUZZY MATCH RATIO: " + ratio);
                                    if(ratio>50){
                                        olderVersions.add(commit.getCommitterIdent().getName());
                                        olderVersions.add(m.group(0));
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

        StringBuilder sb = new StringBuilder();
        for(String oldComment : olderVersions){
            sb.append(oldComment + "\n");
        }
        JOptionPane.showMessageDialog(null, sb);
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
