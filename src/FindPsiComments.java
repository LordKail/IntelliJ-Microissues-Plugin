import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Al3x on 30/10/2016.
 */
public class FindPsiComments extends AnAction {

    public FindPsiComments() {
        // Set the menu item name.
        super("Retrieve Psi");
        // Set the menu item name, description and icon.
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        //final VirtualFile[] vFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        VirtualFile virtuaFile = psiFile.getVirtualFile();
        System.out.println(virtuaFile.getName());

        PsiElement element = psiFile.getFirstChild();
        System.out.println(element.getText());
        System.out.println(element.getChildren().length);
        System.out.println(element.getParent().getText());
        System.out.println(element.getParent().getChildren().length);

        for(PsiElement el : element.getParent().getChildren()){
            for(PsiElement el2 : el.getChildren()){
                if(el2 instanceof PsiComment){
                    System.out.println("FOUND COMMENT");
                    System.out.println(el2.getText());
                    ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Microissues");
                    JLabel labelText = new JLabel(el2.getText());
                    JComponent check = (JComponent) window.getComponent().add(labelText);
                    check.repaint();
                    window.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(
                            check, "Comment", true));;


                }
            }
            if(el instanceof PsiComment) {
                System.out.println(el.getText());
            }
        }
    }
}
