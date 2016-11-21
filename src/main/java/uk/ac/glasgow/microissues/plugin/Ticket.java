package uk.ac.glasgow.microissues.plugin;

import com.intellij.psi.PsiComment;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Al3x on 11/11/2016.
 */
public class Ticket {

    private String summary;
    private String description;
    private String type;
    private PsiComment associatedComment;
    private static final Pattern TAG_REGEX = Pattern.compile("@(.+?)\\s(.+?)\\n");

    public Ticket(){

    }

    public Ticket(String summary, String description, String type, PsiComment associatedComment){
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.associatedComment = associatedComment;
    }

    public Ticket buildIssue(PsiComment comment) {
        this.associatedComment = comment;
        String commentString = comment.getText();
        HashMap<String, String> tagMap = new HashMap<>();
        final Matcher matcher = TAG_REGEX.matcher(commentString);

        while (matcher.find()) {
            tagMap.put(matcher.group(1), matcher.group(2));
        }

        if (tagMap.containsKey("tckt")) {
            this.summary = tagMap.get("tckt");
            this.type = tagMap.get("type");
            return this;
        }
        return null;
    }

    public String getSummary() {
        return summary;
    }
    public String getType() { return type; }
}
