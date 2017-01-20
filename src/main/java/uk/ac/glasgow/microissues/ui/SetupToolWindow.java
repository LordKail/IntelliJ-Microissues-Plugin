package uk.ac.glasgow.microissues.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import uk.ac.glasgow.microissues.plugin.TaskTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;


public class SetupToolWindow implements ProjectComponent{

    private JComponent microissuesContainer;
    private String toolwindowTitle = "Microissues";
    private Project project;

    public SetupToolWindow(Project project) {
        this.project = project;
    }

    @Override
    public void initComponent() {
        microissuesContainer = new JPanel(new BorderLayout(1, 1));
        microissuesContainer.setBorder(null);
    }

    @Override
    public void projectOpened() {
        ToolWindow tasksToolWindow = ToolWindowManager.getInstance(project).registerToolWindow(toolwindowTitle, false, ToolWindowAnchor.BOTTOM);
        TaskTree taskTree = new TaskTree(project);
        taskTree.processComments();

        //tasksToolWindow.getContentManager().getComponent().add(randomTree, BorderLayout.CENTER);
    }

    @Override
    public void projectClosed() {
        //ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        //toolWindowManager.unregisterToolWindow(toolwindowTitle);
    }

    @Override
    public void disposeComponent() {
        //Not sure what goes here.
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Microissues ToolWindow Component";
    }
}
