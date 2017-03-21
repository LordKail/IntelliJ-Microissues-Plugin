package uk.ac.glasgow.microissues.ui;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import uk.ac.glasgow.microissues.plugin.OldTicket;
import uk.ac.glasgow.microissues.plugin.Ticket;
import uk.ac.glasgow.microissues.plugin.TreeMenuListener;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The class that is responsible for processing the Psi elements and thus creating the appropriate JTree and Ticket
 * class instances that will store the information for each comment.
 */
public class TaskTree {

    private JComponent microissuesContainer;
    private Tree taskTree;
    private Project project;

    private ConcurrentHashMap<String, CopyOnWriteArrayList<DefaultMutableTreeNode>> fileToNodes;

    public TaskTree(Project project) {
        this.project = project;
        fileToNodes = new ConcurrentHashMap<>();
        initializeTree();
    }

    /**
     * Creates the tree to be used within the ToolWindow. The Tree has a mouselistener that checks the nodes
     * selected.
     */
    private void initializeTree() {

        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Microissues");

        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("All issues");
        taskTree = new Tree(top);
        taskTree.setVisible(true);

        JLabel ticketInfoTab = new JLabel("Ticket information will appear here");

        MouseListener ml = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (taskTree.getSelectionPath() != null) {
                    DefaultMutableTreeNode selectedElement
                            = (DefaultMutableTreeNode) taskTree.getSelectionPath().getLastPathComponent();
                    if (!(selectedElement.getUserObject() instanceof String)) {

                        if (SwingUtilities.isRightMouseButton(e)) {
                            JPopupMenu popup = new JPopupMenu();
                            JMenuItem viewInfo = new JMenuItem("View ticket history");
                            viewInfo.addActionListener(new TreeMenuListener(selectedElement));
                            popup.add(viewInfo);
                            popup.show(taskTree, e.getX(), e.getY());

                        } else {
                                Ticket selectedTicket;
                                if (selectedElement.getUserObject() instanceof OldTicket.TicketLabel) {
                                    OldTicket.TicketLabel ticketLabel = (OldTicket.TicketLabel) selectedElement.getUserObject();
                                    selectedTicket = ticketLabel.getTicket();
                                } else {
                                    Ticket.TicketLabel ticketLabel = (Ticket.TicketLabel) selectedElement.getUserObject();
                                    selectedTicket = ticketLabel.getTicket();
                                }

                                if (e.getClickCount() == 1) {
                                    ticketInfoTab.setText(selectedTicket.toPanelString());
                                } else {
                                    NavigatablePsiElement navigatable = (NavigatablePsiElement) selectedTicket.getAssociatedComment().getParent();
                                    navigatable.navigate(true);
                            }
                        }
                    }
                }
            }

        };

        taskTree.addMouseListener(ml);
        taskTree.setToggleClickCount(0);


        // Adding an information window for the ticket
        JPanel newJPanel = new JPanel();
        FlowLayout layout = (FlowLayout) newJPanel.getLayout();
        layout.setVgap(0);
        layout.setAlignment(FlowLayout.LEFT);
        newJPanel.add(ticketInfoTab, BorderLayout.WEST);

        JSplitPane treeAndInfoPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JBScrollPane(taskTree), new JBScrollPane(newJPanel));
        treeAndInfoPanel.setResizeWeight(0.5);

        microissuesContainer = new JPanel(new BorderLayout(1, 1));
        microissuesContainer.add(treeAndInfoPanel, BorderLayout.CENTER);

        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("TasksAdditionalToolBarGroup");
        ActionToolbar toolBar = ActionManager.getInstance().createActionToolbar("TasksAdditionalToolBarGroupPlace", actionGroup, false);

        JPanel toolBarPanel = new JPanel(new BorderLayout(1, 1));
        toolBarPanel.add(toolBar.getComponent(), BorderLayout.WEST);
        toolBarPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        microissuesContainer.add(toolBarPanel, BorderLayout.WEST);

        window.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(microissuesContainer, "All issues", true));
    }


    /**
     * Deletes all the tickets from the tasktree that belong to the specified filename.
     * @param fileName Used for deleting the tickets belonging to the file.
     */
    public void flushTicketsInFile(String fileName){
        DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
        if(fileToNodes.get(fileName) != null) {
            for (DefaultMutableTreeNode node : fileToNodes.get(fileName)) {
                defaultModel.removeNodeFromParent(node);
                fileToNodes.get(fileName).remove(node);

            }
        }
    }


    /**
     * Method for adding a new ticket to the task tree - used to create the TicketLabel and also tie the filename to the
     * ticket.
     * @param newTicket The ticket to be added to the tree.
     * @param fileName The filename to which to tie the ticket.
     */
    public void addTicket(Ticket newTicket, String fileName){
        DefaultTreeModel defaultModel = (DefaultTreeModel) taskTree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) defaultModel.getRoot();
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newTicket.new TicketLabel());
        root.add(newNode);

        fileToNodes.putIfAbsent(fileName, new CopyOnWriteArrayList<>());
        fileToNodes.get(fileName).add(newNode);

        defaultModel.reload();
    }


    /**
     * Method for handling the case when a file has been renamed.
     * Replaces the filename in the hashmap in order to register the nodes corresponding to the old filename
     * to the new filename.
     * @param oldFileName The previous filename.
     * @param newFileName The new filename that the old one was renamed to.
     */
    public void fileRenamed(String oldFileName, String newFileName){
        if(fileToNodes.containsKey(oldFileName) && !fileToNodes.containsKey(newFileName)){
            fileToNodes.put(newFileName, fileToNodes.remove(oldFileName));
        }
    }
}
