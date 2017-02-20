package uk.ac.glasgow.microissues.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * Created by 2090140l on 20/02/17.
 */
public class HelpDisplayer extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        displayHelpDialog(project);
    }

    public void displayHelpDialog(Project project){
        JOptionPane.showMessageDialog(null, "Help on how to use this plugin.");
    }

}
