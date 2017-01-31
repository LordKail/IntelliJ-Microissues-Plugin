package uk.ac.glasgow.microissues.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The class that is responsible for processing the Psi elements and thus creating the appropriate JTree and Ticket
 * class instances that will store the information for each comment.
 */
public class TaskTree {

    private ArrayList<VirtualFile> vFiles = new ArrayList<>();
    private ArrayList<PsiComment> psiCommentList = new ArrayList<>();
    private ArrayList<Ticket> ticketList = new ArrayList<>();
    private HashMap<Ticket, DefaultMutableTreeNode> ticketToNode = new HashMap<>();
    private HashMap<PsiComment, Ticket> commentToTicket = new HashMap<>();
    private JComponent microissuesContainer;
    private Tree taskTree;
    private Project project;
    boolean unfinishedComment;
    PsiComment unfinishedPsiComment;

    public TaskTree(Project project) {
        this.project = project;
    }

    public void isPsiComment(PsiElement element){
        if(element instanceof PsiComment){
            System.out.println("ADDING TO psiCommentList");
            psiCommentList.add((PsiComment) element);
        } else {
            for(PsiElement psiChild : element.getChildren()){
                isPsiComment(psiChild);
            }
        }
    }

    public void processComments() {
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(new ContentIterator() {
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

        for(VirtualFile vFile : vFiles){
            isPsiComment(PsiManager.getInstance(project).findFile(vFile));
        }

        for(PsiComment comment : psiCommentList){
            Ticket addingTicket = new Ticket();
            addingTicket.buildIssue(comment);
            System.out.println("CONSTRUCTED ISSUE CLASS, Summary: " + addingTicket.getSummary());

            if(addingTicket.getSummary() != null){
                ticketList.add(addingTicket);
                commentToTicket.put(comment, addingTicket);
            }
        }

        addPsiListener();

        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Microissues");
        buildTree(window);
    }

    public void buildTree(ToolWindow window) {
        //microissuesContainer = new JPanel(new BorderLayout(1, 1));
        //microissuesContainer.setBorder(null);
        System.out.println("BUILDING TREE");
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("All issues");
        createNodes(top);
        taskTree = new Tree(top);
        taskTree.setVisible(true);

        System.out.println("TESTING WINDOW TITLE: " + window.getTitle());

        JLabel ticketInfoTab = new JLabel("Ticket information will appear here");

        MouseListener ml = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.out.println("MOUSE CLICKED");
                if (SwingUtilities.isRightMouseButton(e)) {
                    System.out.println("RIGHT MOUSE CLICKED");
                    DefaultMutableTreeNode selectedElement
                            =(DefaultMutableTreeNode)taskTree.getSelectionPath().getLastPathComponent();
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem viewInfo= new JMenuItem("View ticket history");
                    viewInfo.addActionListener(new TreeMenuListener(window, selectedElement));
                    popup.add(viewInfo);
                    popup.show(taskTree, e.getX(), e.getY());

                }else{
                    DefaultMutableTreeNode selectedElement
                            =(DefaultMutableTreeNode)taskTree.getSelectionPath().getLastPathComponent();
                    System.out.println(selectedElement.getUserObject());
                    if(e.getClickCount() == 1){

                        Ticket selected = (Ticket) selectedElement.getUserObject();
                        ticketInfoTab.setText(selected.toPanelString());
                    }
                    // Ridiculous nested if statement block
                    if(e.getClickCount() == 2){
                        System.out.println("Double click!");
                        if(ticketToNode.containsValue(selectedElement)){
                            for(Ticket key : ticketToNode.keySet()){
                                if(ticketToNode.get(key).equals(selectedElement)){
                                    for(PsiComment comment : commentToTicket.keySet()){
                                        if(commentToTicket.get(comment).equals(key)){
                                            NavigatablePsiElement navigatable = (NavigatablePsiElement) comment.getParent();
                                            navigatable.navigate(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        };

        taskTree.addMouseListener(ml);
        taskTree.setToggleClickCount(0);

        //microissuesContainer.add(new JBScrollPane(taskTree), BorderLayout.WEST);

        // Adding an information window for the ticket
        JPanel newJPanel = new JPanel();
        FlowLayout layout = (FlowLayout) newJPanel.getLayout();
        layout.setVgap(0);
        layout.setAlignment(FlowLayout.LEFT);
        newJPanel.add(ticketInfoTab, BorderLayout.WEST);
        //microissuesContainer.add(new JBScrollPane(newJPanel));

        microissuesContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JBScrollPane(taskTree), new JBScrollPane(newJPanel));
        ((JSplitPane) microissuesContainer).setResizeWeight(0.5);

        window.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(microissuesContainer, "All issues", true));
    }

    public void createNodes(DefaultMutableTreeNode top){
        System.out.println("Printing out ticketList size: " + ticketList.size());
        for(Ticket ticket : ticketList){
            DefaultMutableTreeNode ticketNode = new DefaultMutableTreeNode(ticket);
            System.out.println("Printing ticket summary: " + ticket);
            ticketToNode.put(ticket, ticketNode);
            top.add(ticketNode);
        }
    }

    public void addPsiListener(){
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeListener() {

            @Override
            public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childAdded(@NotNull PsiTreeChangeEvent event) {
                System.out.println("CHILD HAS BEEN ADDED");
                System.out.println(event.getChild().getText());
                if(event.getChild() instanceof PsiComment && event.getChild().getText().endsWith("*/")) {
                    System.out.println(commentToTicket.containsKey((PsiComment) event.getChild()));
                    if(event.getChild().getText().contains("@tckt") && !commentToTicket.containsKey((PsiComment) event.getChild())){
                        boolean replacementPsiElement = false;
                        for(PsiComment comment : commentToTicket.keySet()){
                            if(comment.getText().equals(event.getChild().getText())){
                                replacementPsiElement = true;
                                Ticket oldPsiTicket = commentToTicket.get(comment);
                                commentToTicket.remove(comment);
                                commentToTicket.put((PsiComment) event.getChild(), oldPsiTicket);
                            }
                        }
                        if(!replacementPsiElement) {
                            Ticket newTicket = new Ticket();
                            newTicket.buildIssue((PsiComment) event.getChild());
                            commentToTicket.put((PsiComment) event.getChild(), newTicket);
                            DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
                            DefaultMutableTreeNode root = (DefaultMutableTreeNode) defaultModel.getRoot();
                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newTicket);
                            root.add(newNode);
                            ticketToNode.put(newTicket, newNode);
                            defaultModel.reload();
                        }
                    }
                }
            }

            @Override
            public void childRemoved(@NotNull PsiTreeChangeEvent event) {
                System.out.println(event.getChild().getText());
                System.out.println("CHILD REMOVED");
                if(commentToTicket.containsKey(event.getChild()) && !unfinishedComment){
                    Ticket removedTicket = commentToTicket.get(event.getChild());
                    DefaultMutableTreeNode removedNode = ticketToNode.get(removedTicket);
                    DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
                    defaultModel.removeNodeFromParent(removedNode);
                    defaultModel.reload();
                }
            }

            @Override
            public void childReplaced(@NotNull PsiTreeChangeEvent event) {
                System.out.println("Child has been replaced");
                System.out.println("Printing replaced child's text: " + event.getOldChild().getText());
                System.out.println("Printing new child's text: " + event.getNewChild().getText());
                if(StringUtils.countMatches(event.getNewChild().getText(), "/*")>1){
                    unfinishedComment = true;
                    unfinishedPsiComment = (PsiComment) event.getOldChild();
                }
                else{
                    unfinishedComment = false;
                }
                if(event.getOldChild() instanceof PsiComment && !unfinishedComment) {
                    if (commentToTicket.containsKey(event.getOldChild())) {
                        System.out.println("COMMENT WAS PREVIOUSLY IN psiCommentList!");
                        PsiComment oldChild = (PsiComment) event.getOldChild();
                        PsiComment newChild = (PsiComment) event.getNewChild();
                        Ticket changeTicket = commentToTicket.get(oldChild);
                        Ticket newTicket = changeTicket;
                        newTicket.buildIssue(newChild);
                        commentToTicket.put(newChild, commentToTicket.get(oldChild));
                        ticketToNode.get(commentToTicket.get(oldChild)).setUserObject(newTicket);
                        DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
                        defaultModel.nodeChanged(ticketToNode.get(commentToTicket.get(oldChild)));
                        commentToTicket.remove(oldChild);
                    }

                    else if(StringUtils.countMatches(event.getOldChild().getText(), "/*")>1){
                        PsiComment oldChild = unfinishedPsiComment;
                        PsiComment newChild = (PsiComment) event.getNewChild();
                        Ticket changeTicket = commentToTicket.get(oldChild);
                        Ticket newTicket = changeTicket;
                        newTicket.buildIssue(newChild);
                        commentToTicket.put(newChild, commentToTicket.get(oldChild));
                        ticketToNode.get(commentToTicket.get(oldChild)).setUserObject(newTicket);
                        DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
                        defaultModel.nodeChanged(ticketToNode.get(commentToTicket.get(oldChild)));
                        commentToTicket.remove(oldChild);
                    }

                    else if(event.getNewChild().getText().contains("@tckt")){
                        System.out.println("A NEW TICKET HAS HAS BEEN CREATED.");
                        Ticket newTicket = new Ticket();
                        newTicket.buildIssue((PsiComment) event.getNewChild());
                        commentToTicket.put((PsiComment) event.getNewChild(), newTicket);
                        DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
                        DefaultMutableTreeNode root = (DefaultMutableTreeNode) defaultModel.getRoot();
                        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(newTicket);
                        root.add(newChild);
                        defaultModel.reload();
                        ticketToNode.put(newTicket, newChild);
                    }
                }
            }

            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                /*
                System.out.println("CHILDREN CHANGED");
                System.out.println(event.getParent().getText());
                for (PsiElement element : event.getParent().getChildren()){
                    System.out.println("PRINTING CHILD ELEMENT TEXT: " + element.getText());
                    if(element instanceof PsiComment){
                        NavigatablePsiElement navigatable = (NavigatablePsiElement)element.getNavigationElement();
                        navigatable.navigate(true);
                        if(psiCommentList.contains((PsiComment) element)){
                            System.out.println("IN THE LIST ALREADY " + element.getText());
                        }
                    }
                }

                if(event.getElement() instanceof PsiComment){
                    System.out.println(event.getElement().getText());
                    if(psiCommentList.contains((PsiComment) event.getElement())) {
                        System.out.println("ELEMENT HAS BEEN CHANGED WHOA");
                    }
                }
                */
            }

            @Override
            public void childMoved(@NotNull PsiTreeChangeEvent event) {
                System.out.println("Child moved?");
            }

            @Override
            public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

            }

        });

    }
}
