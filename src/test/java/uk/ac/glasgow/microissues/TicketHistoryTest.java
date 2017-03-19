package uk.ac.glasgow.microissues;

import com.intellij.psi.PsiComment;
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
import uk.ac.glasgow.microissues.plugin.TicketHistory;

import java.util.LinkedHashMap;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * The test for TicketHistory class.
 */
public class TicketHistoryTest extends EasyMockSupport {

    private TicketHistory ticketHistory;

    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    Ticket ticket;

    @Mock
    PersonIdent personIdent;

    @Mock
    OldTicket oldTicket;

    @Mock
    PsiComment psiComment;

    @Before
    public void setUp() throws Exception {
        ticketHistory = new TicketHistory(ticket);
        expect(ticket.getAssociatedComment()).andReturn(psiComment);
        replay(ticket);
        expect(psiComment.getText()).andReturn("/*\n@tckt Sample Summary\n*/");
    }

    @Test
    public void testRetrieveTicketHistory_OlderVersionTicketsNotNull(){
        LinkedHashMap<OldTicket, PersonIdent> olderVersionTickets = new LinkedHashMap<>();
        ticketHistory.setOlderVersionTickets(olderVersionTickets);
        Assert.assertEquals(ticketHistory.retrieveTicketHistory(), olderVersionTickets);
    }

}
