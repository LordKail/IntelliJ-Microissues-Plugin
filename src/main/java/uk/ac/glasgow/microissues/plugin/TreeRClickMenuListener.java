package uk.ac.glasgow.microissues.plugin;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Al3x on 27/11/2016.
 */
public class TreeRClickMenuListener implements ActionListener {
    ToolWindow window;

    public TreeRClickMenuListener(ToolWindow window){
        this.window = window;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Was used for displaying information in the info window. Currently a proof of context and might be removed.
        System.out.println("SELECTED:" + e.getActionCommand());;
    }
}
