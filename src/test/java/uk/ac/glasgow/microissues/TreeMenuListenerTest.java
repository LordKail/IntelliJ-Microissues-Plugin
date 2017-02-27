package uk.ac.glasgow.microissues;

import org.easymock.EasyMock;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.ac.glasgow.microissues.plugin.OldTicket;
import uk.ac.glasgow.microissues.plugin.Ticket;
import uk.ac.glasgow.microissues.plugin.TicketHistory;
import uk.ac.glasgow.microissues.plugin.TreeMenuListener;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Test for TreeMenuListener.
 */
public class TreeMenuListenerTest extends EasyMockSupport {

    private TreeMenuListener treeMenuListener;

    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    private DefaultMutableTreeNode selectedElement = EasyMock.niceMock(DefaultMutableTreeNode.class);

    @Mock
    private ActionEvent e;

    @Mock
    private Ticket ticket;

    @Mock
    private PersonIdent personIdent;

    @Mock
    private Ticket.TicketLabel ticketLabel;

    @Mock
    private TicketHistory ticketHistory;

    @Before
    public void setUp() throws Exception {
        treeMenuListener = new TreeMenuListener(selectedElement);
        expect(e.getActionCommand()).andReturn("View ticket history");
        expect(selectedElement.getUserObject()).andReturn(ticketLabel).times(2);
        expect(ticketLabel.getTicket()).andReturn(ticket);
        expect(ticket.getTicketHistory()).andReturn(ticketHistory);
        replay(selectedElement);
        replay(ticketLabel);
        replay(ticket);
    }

    @Test
    public void testActionPerformed(){
        LinkedHashMap<OldTicket, PersonIdent> mapOfPrevTickets = new LinkedHashMap<OldTicket, PersonIdent>();
        OldTicket oldTicket1 = new OldTicket(ticket, personIdent);
        OldTicket oldTicket2 = new OldTicket(ticket, personIdent);
        OldTicket oldTicket3 = new OldTicket(ticket, personIdent);

        mapOfPrevTickets.put(oldTicket1, personIdent);
        mapOfPrevTickets.put(oldTicket2, personIdent);
        mapOfPrevTickets.put(oldTicket3, personIdent);

        expect(ticketHistory.retrieveTicketHistory())
                .andReturn(mapOfPrevTickets);
        replay(ticketHistory);

        treeMenuListener.actionPerformed(e);

    }



}
