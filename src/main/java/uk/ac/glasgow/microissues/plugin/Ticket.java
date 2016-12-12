package uk.ac.glasgow.microissues.plugin;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;

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
    private PsiComment associatedComment; // The associated Psi Element (PsiComment).
    private String associatedFile; // The associated Java file name in which the ticket resides.

    // REGEX for scanning the ticket for tags in the comment.
    private static final Pattern TAG_REGEX = Pattern.compile("@(.+?)\\s(.+?)\\n");

    // Default empty constructor
    public Ticket() {}

    public Ticket(String summary, String description, String type, PsiComment associatedComment){
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.associatedComment = associatedComment;
    }

    // The builder method for the ticket.
    public Ticket buildIssue(PsiComment comment) {
        this.associatedComment = comment;

        String commentString = comment.getText();
        HashMap<String, String> tagMap = new HashMap<>();
        final Matcher matcher = TAG_REGEX.matcher(commentString);

        while (matcher.find()) {
            tagMap.put(matcher.group(1), matcher.group(2));
        }
        if (tagMap.containsKey("tckt")) {
            System.out.println(comment.getParent().getParent().getText());
            PsiFile psifile = (PsiFile) comment.getParent().getParent();
            this.associatedFile = psifile.getName();

            this.summary = tagMap.get("tckt");
            this.type = tagMap.get("type");
            this.description = "";
            String lines[] = commentString.split("\\r?\\n");
            for (String line : lines){
                if(!line.contains("@") && !line.contains("/*") && !line.contains("*/")){
                    this.description += line.replace("*", "");
                }
            }
            return this;
        }
        return null;
    }

    public String getSummary() {
        return summary;
    }
    public String getType() { return type; }
    public String getAssociatedFile(){ return associatedFile; }
    public PsiComment getAssociatedComment() { return associatedComment; }

    public String toString(){
        return summary;
    }
    public String toPanelString() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
}
