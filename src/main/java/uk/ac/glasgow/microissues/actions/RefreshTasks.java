package uk.ac.glasgow.microissues.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

/**
 * Created by 2090140l on 20/02/17.
 */
public class RefreshTasks extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        // Code to reload the tasks for the project.
        // Most likely calls the processFiles() from PsiAndTicketHandler.
    }
}