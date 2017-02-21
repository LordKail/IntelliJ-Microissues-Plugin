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
        StringBuilder sb = new StringBuilder();
        sb.append("<html><h3> How to use the Microissues plugin</h3>");
        sb.append("<p>The plugin scans all your comments for tickets and displays them in the ToolWindow.");
        sb.append("<p>The format of the tickets is as follows: </p>");
        sb.append("<p> /* </p>");
        sb.append("<p>@tckt Ticket summary here [REQUIRED] \t</p>");
        sb.append("<p>@type Ticket type here [OPTIONAL] \t</p>");
        sb.append("<p>*/<p>");
        sb.append("<br><br>");
        sb.append("<p>Further information, with examples, are available on the GitHub page of the plugin:</p>");
        sb.append("<a href='https://github.com/LordKail/IntelliJ-Microissues-Plugin'>IntelliJ Microissues Plugin</a>");
        sb.append("</html>");
        JOptionPane.showMessageDialog(null, sb);
    }

}
