package uk.ac.glasgow.microissues.ui;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import uk.ac.glasgow.microissues.plugin.Ticket;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

/**
 * Created by Al3x on 14/11/2016.
 */
public class CreateTaskTree {
    private ArrayList<Ticket> ticketList;

    public CreateTaskTree(ArrayList<Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    public void buildTree(ToolWindow window){
        Tree taskTree;
        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("All issues");
        createNodes(top);
        taskTree = new Tree(top);
        window.getComponent().add(new JBScrollPane(taskTree));

    }

    public void createNodes(DefaultMutableTreeNode top){
        for(Ticket ticket : ticketList){
            DefaultMutableTreeNode ticketNode = new DefaultMutableTreeNode(ticket.getSummary());
            top.add(ticketNode);
        }
    }
}
