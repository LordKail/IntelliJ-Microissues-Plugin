package uk.ac.glasgow.microissues.plugin;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
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

    protected String summary; // Summary of the ticket.
    protected String description; // Description of the ticket
    protected String type; // The type of the ticket.
    protected int priority;
    private PsiComment associatedComment; // The associated Psi Element (PsiComment).
    private String associatedFile; // The associated Java file name in which the ticket resides.
    private TicketHistory ticketHistory;

    // REGEX for scanning the ticket for tags in the comment.
    private static final Pattern TAG_REGEX = Pattern.compile("@(.+?)\\s(.+?)\\n");

    // Default empty constructor
    public Ticket() {}

    /**
     * The method for building the ticket using a PsiComment instance, which contains
     * the text of the comment. The associated PsiComment and the filename in which
     * it resides are recorded for ticket management purposes.
     * @param comment
     */
    public void buildIssue(PsiComment comment) {
        this.associatedComment = comment;

        PsiElement possiblePsiFile = associatedComment.getParent();
        while(!(possiblePsiFile instanceof PsiFile)){
            possiblePsiFile = possiblePsiFile.getParent();
        }

        PsiFile psiFile = (PsiFile) possiblePsiFile;
        this.associatedFile = psiFile.getName();

        String commentString = comment.getText();
        buildIssue(commentString);
    }

    /**
     * The method for building the ticket from a comment string. The comment string
     * is checked via regex whether it is in fact intended to be a comment and
     * according to all the tags written in the comment, ticket details are recorded.
     * @param commentString
     */

    public void buildIssue(String commentString){

        HashMap<String, String> tagMap = getTagMap(commentString);

        if (tagMap.containsKey("tckt")) {
            this.summary = tagMap.get("tckt");
            this.type = tagMap.get("type");

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
                    if(description == null){
                        description = "";
                    }
                    this.description += line.replace("*", "");
                }
            }
        }
    }

    /**
     * Uses the defined regex to match lines for tags, which are of the format
     * {@literal}<Tag> <Information>
     * @param commentString
     * @return A {Tag [String] : Information[String]} hashmap is returned.
     */

    @NotNull
    private HashMap<String, String> getTagMap(String commentString) {
        HashMap<String, String> tagMap = new HashMap<>();
        final Matcher matcher = TAG_REGEX.matcher(commentString);

        while (matcher.find()) {
            tagMap.put(matcher.group(1), matcher.group(2));
        }
        return tagMap;
    }

    /**
     * Getters and setters
     */

    public void setSummary(String summary){
        this.summary = summary;
    }
    public void setType(String type) { this.type = type; }


    public String getSummary() {
        return summary;
    }
    public String getType() { return type; }
    public int getPriority() { return priority; }
    public String getAssociatedFile(){
        return associatedFile;
    }
    public TicketHistory getTicketHistory(){
        return ticketHistory;
    }
    public PsiComment getAssociatedComment() { return associatedComment; }
    public String getDescription() { return description; }

    /**
     * Method to generate the html string to be displayed in the ticket
     * information panel.
     * @return
     */

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

        if(!(description == null)) {
            sb.append("<p>Description: " + description + "</p>");
        }

        sb.append("</html>");

        return sb.toString();
    }

    /**
     * TicketLabel class which overrides toString - used as the object
     * to be passed to the JTree, which will use the toString to display
     * the summary of the ticket as the node.
     */

    public class TicketLabel {

        public Ticket getTicket() {
            return Ticket.this;
        }

        @Override
        public String toString(){
            return summary;
        }
    }
}
