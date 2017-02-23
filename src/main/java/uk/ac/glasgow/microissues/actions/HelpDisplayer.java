package uk.ac.glasgow.microissues.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * Class extending AnAction responsible for displaying helpful information relating to how to use the plugin and which
 * tags related to ticket creation are available.
 */
public class HelpDisplayer extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = DataKeys.PROJECT.getData(e.getDataContext());
        displayHelpDialog(project);
    }

    public void displayHelpDialog(Project project){
        StringBuilder sb = new StringBuilder();
        sb.append("<html><h3> How to use the Microissues plugin</h3>");
        sb.append("<p>The plugin automatically scans all your comments for tickets and displays them in the ToolWindow.");
        sb.append("<p>The format of the tickets is as follows: </p>");
        sb.append("<p> /* </p>");
        sb.append("<p>@tckt Ticket summary here [ANY TEXT] [REQUIRED] \t</p>");
        sb.append("<p>@type Ticket type here [ANY TEXT] [OPTIONAL] \t</p>");
        sb.append("<p>@priority Ticket priority here [INTEGER] [OPTIONAL] \t</p>");
        sb.append("<p>*/<p>");
        sb.append("<br><br>");
        sb.append("<p><b>If your tickets are not recognised or displayed in the Tool Window, please hit the refresh button.</b></p>");
        sb.append("<p>Further information, with examples, are available on the GitHub page of the plugin:</p>");
        sb.append("<a href='https://github.com/LordKail/IntelliJ-Microissues-Plugin'>IntelliJ Microissues Plugin</a>");
        sb.append("</html>");

        JEditorPane helpMessage = new JEditorPane("text/html", sb.toString());

        JLabel label = new JLabel();

        helpMessage.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                    //ProcessHandler.launchUrl(e.getURL().toString()); // roll your own link launcher or use Desktop if J6+
                    try
                    {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                    catch(java.net.URISyntaxException e1)
                    {
                        System.err.println(e1.getMessage());
                    }
                    catch(java.io.IOException e2)
                    {
                        System.err.println(e2.getMessage());
                    }
            }
        });
        helpMessage.setEditable(false);
        helpMessage.setBackground(label.getBackground());
        JOptionPane.showMessageDialog(null, helpMessage);

    }
}
