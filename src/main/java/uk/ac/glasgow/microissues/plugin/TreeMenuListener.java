package uk.ac.glasgow.microissues.plugin;

import org.eclipse.jgit.lib.PersonIdent;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

/**
 * The listener for the menu when right clicking on a node in the task tree
 * (The JTree in the ToolWindow). Used mainly for Checking the ticket history option.
 */

public class TreeMenuListener implements ActionListener {

    private DefaultMutableTreeNode selectedElement;

    // The constructor for the listener.
    public TreeMenuListener(DefaultMutableTreeNode selectedElement) {
        this.selectedElement = selectedElement;
    }


    /**
     * Called when an option is selected from the popup menu. Retrieves the ticket history and adds each found older ticket
     * to the JTree as the main ticket's child.
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Was used for displaying information in the info window. Currently a proof of concept and might be removed.
        Ticket selectedTicket = ((Ticket.TicketLabel) selectedElement.getUserObject()).getTicket();
        TicketHistory history = selectedTicket.getTicketHistory();
        if(history == null){
            history = new TicketHistory(selectedTicket);
        }

        LinkedHashMap<OldTicket, PersonIdent> mapOfPreviousTickets = history.retrieveTicketHistory();

        for(OldTicket olderTicket : mapOfPreviousTickets.keySet()){
            selectedElement.add(new DefaultMutableTreeNode((olderTicket.new TicketLabel())));
        }

    }

}
