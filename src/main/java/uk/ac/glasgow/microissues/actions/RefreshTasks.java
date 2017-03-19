package uk.ac.glasgow.microissues.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import uk.ac.glasgow.microissues.ui.ToolWindowComponent;

/**
 * Class extending AnAction that is called to rescan all project files in case the user has noticed discrepancies between his entered tickets
 * and the tickets displayed in the ToolWindow.
 */
public class RefreshTasks extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        ToolWindowComponent toolWindowComponent = project.getComponent(ToolWindowComponent.class);

        // Reprocesses all the project files to detect tickets.
        toolWindowComponent.getPsiHandler().processProjectFiles();
    }
}