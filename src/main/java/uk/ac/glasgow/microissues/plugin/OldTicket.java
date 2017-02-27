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

    /**
     * Method to generate the html string to be displayed in the ticket
     * information panel pertaining the old ticket information, including the commit info
     * and an indication if any of the ticket tags are different from the current ticket version.
     */
    public String toPanelString(){

        String changed = "<b>[CHANGED]</b>";

        StringBuilder sb = new StringBuilder();
        sb.append("<html><h3> Ticket History </h3>");
        sb.append("<p>Committer: " + commitInfo.getName() + "</p>");
        sb.append("<p>Commit date: " + commitInfo.getWhen() + "</p>");

        boolean changedProperty;

        sb.append("<h4>This old ticket's properties: </h4>");

        if(!summary.equals(mostRecentVersion.getSummary())) {
            changedProperty = true;
        } else {
            changedProperty = false;
        }

        sb.append("<p>Summary: " + summary + " " + (changedProperty ? changed : "") + "</p>");

        if(type != null) {
            if(!type.equals(mostRecentVersion.getType())) {
                changedProperty = true;
            } else {
                changedProperty = false;
            }
        sb.append("<p>Type: " + type + " " + (changedProperty ? changed : "") + "</p>");
        }

        if(priority != 0) {
            if(priority == -1){
                sb.append("<p> Priority: Please use an integer value for the priority </p>");
            } else {
                if(priority != mostRecentVersion.getPriority()) {
                    changedProperty = true;
                } else {
                    changedProperty = false;
                }
                sb.append("<p>Priority: " + priority + " " + (changedProperty ? changed : "") + "</p>");
            }
        }

        if(!(description == null)) {
            if(!description.equals(mostRecentVersion.getDescription())) {
                changedProperty = true;
            } else {
                changedProperty = false;
            }
            sb.append("<p>Description: " + description + " " + (changedProperty ? changed : "") + "</p>");
        }

        sb.append("</html>");

        return sb.toString();
    }

    public class TicketLabel {

        public OldTicket getTicket() {
            return OldTicket.this;
        }

        public String toString(){
            int numOfDifferences = 0;

            String difference = "Multiple changes";
            if(!getSummary().equals(mostRecentVersion.getSummary())){
                numOfDifferences += 1;
                difference = "Change: Summary";
            }
            if(!getType().equals(mostRecentVersion.getType())){
                numOfDifferences += 1;
                difference = "Change: Type";
            }
            return (numOfDifferences == 1 ? difference : "Multiple Changes") + ", Committed on: " + commitInfo.getWhen();
        }
    }

}
