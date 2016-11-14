package uk.ac.glasgow.microissues.plugin;

import com.intellij.psi.PsiComment;

/**
 * Created by Al3x on 11/11/2016.
 */
public class Ticket {

    private String summary;
    private String description;
    private String type;
    private PsiComment associatedComment;

    public Ticket(String summary, String description, String type, PsiComment associatedComment){
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.associatedComment = associatedComment;
    }

    public String getSummary() {
        return summary;
    }
}
