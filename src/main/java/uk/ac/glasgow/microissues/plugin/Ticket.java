package uk.ac.glasgow.microissues.plugin;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The ticket class representing an issue extracted from a comment with certain tags,
 * which identify it as a ticket.
 */
public class Ticket {

    private String summary; // Summary of the ticket.
    private String description; // Description of the ticket
    private String type; // The type of the ticket.
    private int priority;
    private PsiComment associatedComment; // The associated Psi Element (PsiComment).
    private String associatedFile; // The associated Java file name in which the ticket resides.
    private TicketHistory ticketHistory;
    private boolean oldTicket = false;

    // REGEX for scanning the ticket for tags in the comment.
    private static final Pattern TAG_REGEX = Pattern.compile("@(.+?)\\s(.+?)\\n");

    // Default empty constructor
    public Ticket() {
        this.ticketHistory = new TicketHistory(this);
    }

    public Ticket(boolean oldTicket){
        this.ticketHistory = new TicketHistory(this);
        this.oldTicket = true;

    }

    public Ticket(String summary, String description, String type, PsiComment associatedComment){
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.ticketHistory = new TicketHistory(this);
        this.associatedComment = associatedComment;
    }

    // The builder method for the ticket.
    public void buildIssue(PsiComment comment) {
        this.associatedComment = comment;

        //PsiFile psifile = (PsiFile) comment.getParent().getParent();
        //this.associatedFile = psifile.getName();

        String commentString = comment.getText();
        buildIssue(commentString);
    }

    public void buildIssue(String commentString){

        HashMap<String, String> tagMap = getTagMap(commentString);

        if (tagMap.containsKey("tckt")) {
            this.summary = tagMap.get("tckt");
            this.type = tagMap.get("type");
            this.description = "";
            System.out.println("PRIORITY IN TAGMAP: " + tagMap.get("priority"));
            if(tagMap.get("priority") != null) {
                try {
                    this.priority = Integer.parseInt(tagMap.get("priority"));
                } catch (Exception e) {
                    this.priority = -1;
                }
            }
            String lines[] = commentString.split("\\r?\\n");
            for (String line : lines){
                if(!line.contains("@") && !line.contains("/*") && !line.contains("*/")){
                    this.description += line.replace("*", "");
                }
            }
        }
    }

    @NotNull
    private HashMap<String, String> getTagMap(String commentString) {
        HashMap<String, String> tagMap = new HashMap<>();
        final Matcher matcher = TAG_REGEX.matcher(commentString);

        while (matcher.find()) {
            tagMap.put(matcher.group(1), matcher.group(2));
        }
        return tagMap;
    }

    public String getSummary() {
        return summary;
    }
    public String getType() { return type; }

    public String getAssociatedFile(){
        if (associatedFile != null){
            return associatedFile;
        }
        else{
            PsiFile psiFile = (PsiFile) associatedComment.getParent().getParent();
            this.associatedFile = psiFile.getName();
            return associatedFile;
        }
    }

    public TicketHistory getTicketHistory(){ return ticketHistory; }
    public PsiComment getAssociatedComment() { return associatedComment; }

    public void setTicketHistory(TicketHistory ticketHistory) {
        this.ticketHistory = new TicketHistory(this);
    }

    public String toString(){
        return summary;
    }

    public String toPanelString() {

        StringBuilder sb = new StringBuilder();
        sb.append("<html><h3> Ticket Information </h3>");
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
