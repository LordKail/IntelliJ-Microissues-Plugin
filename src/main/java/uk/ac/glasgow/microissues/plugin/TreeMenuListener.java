package uk.ac.glasgow.microissues.plugin;

import com.intellij.openapi.wm.ToolWindow;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

/**
 * Created by Al3x on 27/11/2016.
 */
public class TreeMenuListener implements ActionListener {
    Git git;
    ToolWindow window;
    DefaultMutableTreeNode selectedElement;
    TicketHistory tcktHistory;

    public TreeMenuListener(ToolWindow window, DefaultMutableTreeNode selectedElement) {
        this.window = window;
        this.selectedElement = selectedElement;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Was used for displaying information in the info window. Currently a proof of concept and might be removed.
        System.out.println("SELECTED:" + e.getActionCommand());
        Ticket selectedTicket = ((Ticket.TicketLabel) selectedElement.getUserObject()).getTicket();
        System.out.println("Selected Ticket: " + selectedElement.getUserObject().toString());
        TicketHistory history = selectedTicket.getTicketHistory();

        if(history == null){
            history = new TicketHistory(selectedTicket);
        }

        LinkedHashMap<Ticket, PersonIdent> mapOfPreviousTickets = history.retrieveTicketHistory();

        for(Ticket olderTicket : mapOfPreviousTickets.keySet()){
            selectedElement.add(new DefaultMutableTreeNode((olderTicket.new TicketLabel())));
        }

    }

}
