package uk.ac.glasgow.microissues.ui;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import uk.ac.glasgow.microissues.plugin.PsiAndTicketHandler;

import javax.swing.*;
import java.awt.*;

/**
 * The class for setting the tool window containing the tickets and ticket information as a project component.
 */
public class ToolWindowComponent implements ProjectComponent {

    private Project project;

    private ToolWindow toolWindow;
    private String toolwindowTitle = "Microissues";

    private JComponent microissuesContainer;
    private TaskTree taskTree;

    PsiAndTicketHandler psiHandler;

    public ToolWindowComponent(Project project){
        this.project = project;
    }

    @Override
    public void initComponent() {
        microissuesContainer = new JPanel(new BorderLayout(1, 1));
        microissuesContainer.setBorder(null);
    }

    @Override
    public void projectOpened() {
        toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(toolwindowTitle, false, ToolWindowAnchor.BOTTOM);
        toolWindow.setIcon(IconLoader.getIcon("/uk/ac/glasgow/microissues/icons/task.png"));

        taskTree = new TaskTree(project);
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "MicroissuesToolWindowComponent";
    }

    public TaskTree getTaskTree(){
        return taskTree;
    }

    public PsiAndTicketHandler getPsiHandler() {
        return psiHandler;
    }

    public void setPsiHandler(PsiAndTicketHandler psiHandler) {
        this.psiHandler = psiHandler;
    }
}
