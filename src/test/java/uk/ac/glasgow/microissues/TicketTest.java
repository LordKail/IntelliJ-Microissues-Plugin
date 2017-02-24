package uk.ac.glasgow.microissues;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.ac.glasgow.microissues.plugin.Ticket;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class TicketTest extends EasyMockSupport {

    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    private PsiComment psiComment;

    @Mock
    private PsiFile psiFile;

    private Ticket ticket;

    @Before
    public void setUp() throws Exception {
        ticket = new Ticket();
    }

    @Test
    public void testBuildTicket_withPsiCommentParameter() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("/*\n");
        sb.append("@tckt Sample summary 1\n");
        sb.append("@type BUG\n");
        sb.append("@priority 3\n");
        sb.append("*/");

        expect(psiComment.getText()).andReturn(sb.toString());
        expect(psiComment.getParent()).andReturn(psiFile);
        expect(psiFile.getName()).andReturn("Test.java");
        replay(psiComment);
        replay(psiFile);

        ticket.buildIssue(psiComment);

        Assert.assertEquals("Sample summary 1", ticket.getSummary());
        Assert.assertEquals("BUG", ticket.getType());
        Assert.assertEquals(3, ticket.getPriority());
        Assert.assertEquals("Test.java", ticket.getAssociatedFile());
        Assert.assertEquals(psiComment, ticket.getAssociatedComment());
    }

    @Test
    public void testBuildTicket_WithStringParameter() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("/*\n");
        sb.append("@tckt Sample summary 2\n");
        sb.append("@type TASK\n");
        sb.append("@priority 5\n");
        sb.append("*/");

        String comment = sb.toString();
        ticket.buildIssue(comment);
        Assert.assertEquals("Sample summary 2", ticket.getSummary());
        Assert.assertEquals("TASK", ticket.getType());
        Assert.assertEquals(5, ticket.getPriority());
    }

    @Test
    public void testTicketLabel_toString(){
        String summary = "Sample summary 3";
        ticket.setSummary(summary);

        Ticket.TicketLabel ticketLabel = ticket.new TicketLabel();
        Assert.assertEquals(summary, ticketLabel.toString());
    }

    @Test
    public void testGetAssociatedFile_associatedFileNull(){

    }

    @Test
    public void testToPanelString_everythingExceptSummaryNull(){
        ticket.setSummary("Sample summary 4");
        StringBuilder sb = new StringBuilder();
        sb.append("<html><h3> Ticket Information </h3>");
        sb.append("<p>Summary: Sample summary 4</p>");
        sb.append("</html>");

        Assert.assertEquals(sb.toString(), ticket.toPanelString());
    }

}
