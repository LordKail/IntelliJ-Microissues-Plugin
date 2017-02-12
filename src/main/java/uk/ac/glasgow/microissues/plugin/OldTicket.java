package uk.ac.glasgow.microissues.plugin;

import org.eclipse.jgit.lib.PersonIdent;

/**
 * A subclass of Ticket, representing an older version of the ticket that will inherit a different toString method.
 */
public class OldTicket extends Ticket {

    Ticket currentVersion; // Represents the most recent version of the ticket, i.e where the OldTicket stems from.
    PersonIdent commitInfo; // Represents the commit information (person identity + time).

    public OldTicket(Ticket currentVersionTicket){
        this.currentVersion = currentVersionTicket;
    }


    public String toString(){

        int numOfDifferences;

        return "Old Ticket";
    }

}
