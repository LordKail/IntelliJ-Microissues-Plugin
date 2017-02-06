package uk.ac.glasgow.microissues.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiComment;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import uk.ac.glasgow.microissues.plugin.Ticket;
import uk.ac.glasgow.microissues.plugin.TreeMenuListener;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The class that is responsible for processing the Psi elements and thus creating the appropriate JTree and Ticket
 * class instances that will store the information for each comment.
 */
public class TaskTree {

    private ArrayList<VirtualFile> vFiles = new ArrayList<>();
    private ArrayList<PsiComment> psiCommentList = new ArrayList<>();
    private ArrayList<Ticket> ticketList = new ArrayList<>();
    private ConcurrentHashMap<Ticket, DefaultMutableTreeNode> ticketToNode = new ConcurrentHashMap<>();
    private ConcurrentHashMap<PsiComment, Ticket> commentToTicket = new ConcurrentHashMap<>();
    private JComponent microissuesContainer;
    private Tree taskTree;
    private Project project;
    boolean unfinishedComment;
    PsiComment unfinishedPsiComment;

    //Stuff relating to the tree.
    private ConcurrentHashMap<String, CopyOnWriteArrayList<DefaultMutableTreeNode>> fileToNodes;

    public TaskTree(Project project) {
        this.project = project;
        fileToNodes = new ConcurrentHashMap<>();
        initializeTree();
    }

    private void initializeTree() {

        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Microissues");

        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("All issues");
        taskTree = new Tree(top);
        taskTree.setVisible(true);

        JLabel ticketInfoTab = new JLabel("Ticket information will appear here");

        MouseListener ml = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                System.out.println("MOUSE CLICKED");
                if (SwingUtilities.isRightMouseButton(e)) {
                    System.out.println("RIGHT MOUSE CLICKED");
                    DefaultMutableTreeNode selectedElement
                            = (DefaultMutableTreeNode) taskTree.getSelectionPath().getLastPathComponent();
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem viewInfo= new JMenuItem("View ticket history");
                    viewInfo.addActionListener(new TreeMenuListener(window, selectedElement));
                    popup.add(viewInfo);
                    popup.show(taskTree, e.getX(), e.getY());

                }else{
                    DefaultMutableTreeNode selectedElement
                            = (DefaultMutableTreeNode) taskTree.getSelectionPath().getLastPathComponent();
                    Ticket selectedTicket = (Ticket) selectedElement.getUserObject();
                    System.out.println(selectedElement.getUserObject());
                    if(e.getClickCount() == 1){
                        ticketInfoTab.setText(selectedTicket.toPanelString());
                    } else {
                        System.out.println("First child of PsiComment: ");
                        NavigatablePsiElement navigatable = (NavigatablePsiElement) selectedTicket.getAssociatedComment().getParent();
                        navigatable.navigate(true);
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

    public void flushTicketsInFile(String fileName){
        DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
        if(fileToNodes.get(fileName) != null) {
            for (DefaultMutableTreeNode node : fileToNodes.get(fileName)) {
                System.out.println(((Ticket) node.getUserObject()).getSummary());
                if(node.isLeaf()) {
                    defaultModel.removeNodeFromParent(node);
                    fileToNodes.get(fileName).remove(node);
                }
            }
        }
    }

    public void addTicket(Ticket newTicket, String fileName){
        DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) defaultModel.getRoot();
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newTicket);
        root.add(newNode);

        fileToNodes.putIfAbsent(fileName, new CopyOnWriteArrayList<>());
        fileToNodes.get(fileName).add(newNode);

        System.out.println("Added ticket to the tree!" + newNode.getUserObject().toString());
        defaultModel.reload();
    }

    public void fileRenamed(String oldFileName, String newFileName){
        if(fileToNodes.containsKey(oldFileName) && !fileToNodes.containsKey(newFileName)){
            fileToNodes.put(newFileName, fileToNodes.remove(oldFileName));
            System.out.println("File has been changed!");
        }
    }
}
