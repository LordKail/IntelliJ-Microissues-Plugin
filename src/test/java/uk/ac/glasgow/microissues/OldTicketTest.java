package uk.ac.glasgow.microissues;

import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.ac.glasgow.microissues.plugin.OldTicket;
import uk.ac.glasgow.microissues.plugin.Ticket;

import java.util.Date;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 *
 */
public class OldTicketTest extends EasyMockSupport {

    private OldTicket oldTicket;

    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    private PersonIdent personIdent;

    @Mock
    private Ticket ticket;

    @Before
    public void setUp() throws Exception {
        expect(ticket.getSummary()).andReturn("Sample summary").anyTimes();
        expect(ticket.getType()).andReturn("BUG").anyTimes();
        expect(ticket.getPriority()).andReturn(9).anyTimes();
        expect(personIdent.getName()).andReturn("LordKail");
        expect(personIdent.getWhen()).andReturn(new Date("02/20/2017"));
        replay(ticket);
        replay(personIdent);
        oldTicket = new OldTicket(ticket, personIdent);
    }

    @Test
    public void testToPanelString_everythingSameAsMainTicket() {
        StringBuilder sb = new StringBuilder();
        sb.append("/*\n");
        sb.append("@tckt Sample summary\n");
        sb.append("@type BUG\n");
        sb.append("@priority 9\n");
        sb.append("*/");

        oldTicket.buildIssue(sb.toString());

        String returnedPanelText = oldTicket.toPanelString();

        StringBuilder expectedPanelTextSb = new StringBuilder();
        expectedPanelTextSb.append("<html><h3> Ticket History </h3>");
        expectedPanelTextSb.append("<p>Committer: LordKail</p>");
        expectedPanelTextSb.append("<p>Commit date: Mon Feb 20 00:00:00 GMT 2017</p>");
        expectedPanelTextSb.append("<h4>This old ticket's properties: </h4>");
        expectedPanelTextSb.append("<p>Summary: Sample summary </p>");
        expectedPanelTextSb.append("<p>Type: BUG </p>");
        expectedPanelTextSb.append("<p>Priority: 9 </p>");
        expectedPanelTextSb.append("</html>");

        Assert.assertEquals(expectedPanelTextSb.toString(), returnedPanelText);
    }

    @Test
    public void testToPanelString_everythingDifferentFromMainTicket() {
        StringBuilder sb = new StringBuilder();
        sb.append("/*\n");
        sb.append("@tckt Sample summary 1\n");
        sb.append("@type TASK\n");
        sb.append("@priority 3\n");
        sb.append("*/");

        oldTicket.buildIssue(sb.toString());

        String returnedPanelText = oldTicket.toPanelString();

        StringBuilder expectedPanelTextSb = new StringBuilder();
        expectedPanelTextSb.append("<html><h3> Ticket History </h3>");
        expectedPanelTextSb.append("<p>Committer: LordKail</p>");
        expectedPanelTextSb.append("<p>Commit date: Mon Feb 20 00:00:00 GMT 2017</p>");
        expectedPanelTextSb.append("<h4>This old ticket's properties: </h4>");
        expectedPanelTextSb.append("<p>Summary: Sample summary 1 <b>[CHANGED] </b></p><p>Current: Sample summary</p>");
        expectedPanelTextSb.append("<p>Type: TASK <b>[CHANGED] </b>Current: BUG</p>");
        expectedPanelTextSb.append("<p>Priority: 3 <b>[CHANGED] </b>Current: 9</p>");
        expectedPanelTextSb.append("</html>");

        Assert.assertEquals(expectedPanelTextSb.toString(), returnedPanelText);
    }

    @Test
    public void testTicketLabel_toStringDifferentSummaries(){
        String summary = "Sample summary 3";
        oldTicket.setSummary(summary);

        OldTicket.TicketLabel ticketLabel = oldTicket.new TicketLabel();
        Assert.assertEquals("Change: Summary, Committed on: Mon Feb 20 00:00:00 GMT 2017"
                , ticketLabel.toString());
    }

    @Test
    public void testTicketLabel_toStringDifferentTypes(){
        String type = "TASK";
        oldTicket.setType(type);

        OldTicket.TicketLabel ticketLabel = oldTicket.new TicketLabel();
        Assert.assertEquals("Change: Type, Committed on: Mon Feb 20 00:00:00 GMT 2017"
                , ticketLabel.toString());
    }

}
