package uk.ac.glasgow.microissues.plugin;

import com.google.common.base.Charsets;
import com.intellij.openapi.wm.ToolWindow;
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
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Al3x on 27/11/2016.
 */
public class TreeMenuListener implements ActionListener {
    Git git;
    ToolWindow window;
    DefaultMutableTreeNode selectedElement;
    TicketHistory tcktHistory;

    public TreeMenuListener(ToolWindow window, DefaultMutableTreeNode selectedElement) {
        this.window = window;
        this.selectedElement = selectedElement;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Was used for displaying information in the info window. Currently a proof of concept and might be removed.
        System.out.println("SELECTED:" + e.getActionCommand());
        Ticket selectedTicket = (Ticket) selectedElement.getUserObject();
        TicketHistory history = selectedTicket.getTicketHistory();
        LinkedHashMap<Ticket, PersonIdent> mapOfPreviousTickets = history.retrieveTicketHistory();
        for(Ticket olderTicket : mapOfPreviousTickets.keySet()){
            selectedElement.add(new DefaultMutableTreeNode(olderTicket));
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
