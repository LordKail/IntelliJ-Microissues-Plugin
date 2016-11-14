package uk.ac.glasgow.microissues.plugin;

import com.intellij.psi.PsiComment;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Al3x on 13/11/2016.
 */
public class TicketBuilder {

    private String summary;
    private String description;
    private String type;
    private PsiComment associatedComment;
    private static final Pattern TAG_REGEX = Pattern.compile("@(.+?)\\s(.+?)\\n");

    public Ticket buildIssue(PsiComment comment){
        this.associatedComment = comment;
        String commentString = comment.getText();
        HashMap<String, String> tagMap = new HashMap<>();
        final Matcher matcher = TAG_REGEX.matcher(commentString);

        while (matcher.find()) {
            tagMap.put(matcher.group(1), matcher.group(2));
        }

        if(tagMap.containsKey("tckt")){
            this.summary = tagMap.get("tckt");
            this.type = tagMap.get("type");
            return new Ticket(summary, description, type, associatedComment);
        }
        else{
            return null;
        }
    }

}
