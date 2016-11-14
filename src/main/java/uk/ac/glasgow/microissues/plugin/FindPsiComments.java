package uk.ac.glasgow.microissues.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import uk.ac.glasgow.microissues.ui.CreateTaskTree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

/**
 * Created by Al3x on 30/10/2016.
 */
public class FindPsiComments extends AnAction {

    private ArrayList<VirtualFile> vFiles = new ArrayList<>();
    private ArrayList<PsiComment> psiCommentList = new ArrayList<>();
    private ArrayList<Ticket> ticketList = new ArrayList<>();

    public FindPsiComments() {
        // Set the menu item name.
        super("Retrieve Psi");
        // Set the menu item name, description and icon.
    }

    public void isPsiComment(PsiElement element){
        if(element instanceof PsiComment){
            psiCommentList.add((PsiComment) element);
        } else {
            for(PsiElement psiChild : element.getChildren()){
                isPsiComment(psiChild);
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ProjectFileIndex.SERVICE.getInstance(e.getProject()).iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileInProject) {
                if(fileInProject.getFileType().getName().equals("JAVA")){
                    vFiles.add(fileInProject);
                }
                System.out.println(fileInProject.getFileType().getName());
                System.out.println(fileInProject.getCanonicalPath());
                return true;
            }
        });

        System.out.println("VFiles Size: " + vFiles.size());

        for(VirtualFile vFile : vFiles){
            isPsiComment(PsiManager.getInstance(e.getProject()).findFile(vFile));
        }

        System.out.println("PsiCommentList size after loop: " + psiCommentList.size());

        for(PsiComment comment : psiCommentList){
            TicketBuilder buildingIssue = new TicketBuilder();
            Ticket addingTicket = buildingIssue.buildIssue(comment);
            if(addingTicket != null) {
                System.out.println("CONSTRUCTED ISSUE CLASS, Summary: " + addingTicket.getSummary());
                ticketList.add(addingTicket);
            }
        }

        CreateTaskTree taskTree = new CreateTaskTree(ticketList);
        ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Microissues");
        taskTree.buildTree(window);
    }

    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode summary = null;

    }
}
