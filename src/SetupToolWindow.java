import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


public class SetupToolWindow implements ProjectComponent{

    private JComponent microissuesContainer;
    private String toolwindowTitle = "Microissues";
    private Project project;

    public SetupToolWindow(Project project) {
        this.project = project;
    }

    /*
        @title = Sample ticket
        @author = Alex
        @prio = 8
        @mile = 31/10/2015
        @desc = An example of a ticket, showing the relevant information and the description of it, i.e what has to be
        done.
     */
    /*
        <Ticket>
        <Name>
        <Author>
        </Ticket>
    */

    @Override
    public void initComponent() {
        //Initialising the component. Might move the methods from "projectOpened" to here.
    }

    @Override
    public void projectOpened() {

        microissuesContainer = new JPanel(new BorderLayout(1, 1));
        microissuesContainer.setBorder(null);

        ToolWindow tasksToolWindow = ToolWindowManager.getInstance(project).registerToolWindow(toolwindowTitle, false, ToolWindowAnchor.BOTTOM);
        tasksToolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(microissuesContainer, "Microissues", true));

        ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("TasksActionGroup");
        ActionToolbar toolBar = ActionManager.getInstance().createActionToolbar("TasksActionGroupPlace", actionGroup, false);

        ActionGroup additionalActionGroup = (ActionGroup) ActionManager.getInstance().getAction("TasksAdditionalToolBarGroup");
        ActionToolbar additionalToolbar = ActionManager.getInstance().createActionToolbar("TasksActionGroupPlace", additionalActionGroup, false);

        JPanel toolBarPanel = new JPanel(new BorderLayout(1, 1));
        toolBarPanel.add(toolBar.getComponent(), BorderLayout.WEST);
        toolBarPanel.add(additionalToolbar.getComponent(), BorderLayout.CENTER);
        toolBarPanel.setBorder(null);
    }

    @Override
    public void projectClosed() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(toolwindowTitle);
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
