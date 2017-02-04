package uk.ac.glasgow.microissues.ui;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import uk.ac.glasgow.microissues.plugin.PsiAndTicketHandler;

import javax.swing.*;
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
        PsiAndTicketHandler psiHandler = new PsiAndTicketHandler(taskTree, project);
        psiHandler.processProjectFiles();

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
