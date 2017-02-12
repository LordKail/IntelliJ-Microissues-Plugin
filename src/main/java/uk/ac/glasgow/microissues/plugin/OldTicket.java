package uk.ac.glasgow.microissues.plugin;

import org.eclipse.jgit.lib.PersonIdent;

/**
 * A subclass of Ticket, representing an older version of the ticket that will inherit a different toString method.
 */
public class OldTicket extends Ticket {

    Ticket mostRecentVersion; // Represents the most recent version of the ticket, i.e where the OldTicket stems from.
    PersonIdent commitInfo; // Represents the commit information (person identity + time).

    public OldTicket(Ticket mostRecentVersionTicket, PersonIdent commitInfo){
        this.mostRecentVersion = mostRecentVersionTicket;
        this.commitInfo = commitInfo;

    }


    public String toString(){
        int numOfDifferences = 0;

        String difference = "Multiple changes";
        if(!this.getSummary().equals(mostRecentVersion.getSummary())){
            numOfDifferences += 1;
            difference = "Change: Summary";
        }
        if(!this.getType().equals(mostRecentVersion.getType())){
            numOfDifferences += 1;
            difference = "Change: Type";
        }
        return (numOfDifferences == 1 ? difference : "Multiple Changes") + ", Committed on: " + commitInfo.getWhen();
    }

    public String toPanelString(){

        StringBuilder sb = new StringBuilder();
        sb.append("<html><h3> Ticket History </h3>");
        sb.append("<p>Committer: " + commitInfo.getName() + "</p>");
        sb.append("<p>Commit date: " + commitInfo.getWhen() + "</p>");

        sb.append("<h4>Ticket Changes: </h4>");

        sb.append("<p>Summary: " + summary + "</p>");
        if(type != null) {
            sb.append("<p>Type: " + type + "</p>");
        }
        if(priority != 0) {
            if(priority == -1){
                sb.append("<p> Priority: Please use an integer value for the priority </p>");
            } else {
                sb.append("<p>Priority: " + priority + "</p>");
            }
        }

        if(!description.equals("")) {
            sb.append("<p>Description: " + description + "</p>");
        }

        sb.append("</html>");

        return sb.toString();
    }

}
