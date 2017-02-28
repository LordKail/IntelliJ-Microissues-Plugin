package uk.ac.glasgow.microissues;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiTreeChangeEvent;
import org.easymock.EasyMock;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.ac.glasgow.microissues.plugin.PsiAndTicketHandler;
import uk.ac.glasgow.microissues.ui.TaskTree;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Test for the PsiAndTicketHandler class.
 */
public class PsiAndTicketHandlerTest extends EasyMockSupport {

    PsiAndTicketHandler psiAndTicketHandler;

    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    private PsiElement psiElement;

    @Mock
    private PsiComment psiComment;

    @Mock
    private PsiFile psiFile;

    @Mock
    VirtualFile virtualFile;

    @Mock
    PsiTreeChangeEvent psiTreeChangeEvent;

    private TaskTree taskTree = EasyMock.niceMock(TaskTree.class);

    @Mock
    Project project;

    @Before
    public void setUp() throws Exception {
        replay(taskTree);

        psiAndTicketHandler = new PsiAndTicketHandler(taskTree, project);
    }

    @Test
    public void testScanPsiFile_elementHasNoChildren(){
        expect(psiElement.getChildren()).andReturn(PsiElement.EMPTY_ARRAY);
        replay(psiElement);

        psiAndTicketHandler.scanPsiFile(psiElement, psiFile);
        EasyMock.verify(psiElement);
    }

    @Test
    public void testScanPsiFile_psiCommentIsTicket(){
        expect(psiFile.getVirtualFile()).andReturn(virtualFile);
        expect(psiFile.getName()).andReturn("Testing 1.java");
        replay(psiFile);

        expect(psiComment.getText()).andReturn("/*\n@tckt Sample summary\n*/").times(2);
        expect(psiComment.getParent()).andReturn(psiFile);

        expect(virtualFile.getName()).andReturn("Testing 2.java");
        replay(virtualFile);
        replay(psiComment);
        psiAndTicketHandler.scanPsiFile(psiComment, psiFile);

        EasyMock.verify(psiComment);
        EasyMock.verify(psiFile);
        EasyMock.verify(virtualFile);
    }

    @Test
    public void testElementAddedOrRemoved(){
        expect(psiTreeChangeEvent.getParent()).andReturn(psiFile);
        replay(psiTreeChangeEvent);

        expect(psiFile.getVirtualFile()).andReturn(virtualFile);
        expect(psiFile.getChildren()).andReturn(PsiElement.EMPTY_ARRAY);
        replay(psiFile);

        expect(psiComment.getText()).andReturn("/*\n@tckt Sample summary\n*/").times(2);
        replay(psiComment);

        psiAndTicketHandler.elementAddedOrRemoved(psiTreeChangeEvent, psiComment);

        EasyMock.verify(psiTreeChangeEvent);
        EasyMock.verify(psiFile);
        EasyMock.verify(psiComment);
    }

}
