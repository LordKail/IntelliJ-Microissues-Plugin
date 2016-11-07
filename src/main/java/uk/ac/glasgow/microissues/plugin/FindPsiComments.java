package uk.ac.glasgow.microissues.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Al3x on 30/10/2016.
 */
public class FindPsiComments extends AnAction {

    private ArrayList<VirtualFile> vFiles = new ArrayList<VirtualFile>();
    private ArrayList<PsiComment> psiCommentList = new ArrayList<PsiComment>();

    public FindPsiComments() {
        // Set the menu item name.
        super("Retrieve Psi");
        // Set the menu item name, description and icon.
    }

    public void isPsiComment(PsiElement element){
        if(element instanceof PsiComment){
            psiCommentList.add((PsiComment) element);
        } else {
            for(PsiElement psiChild : element.getChildren()){
                isPsiComment(psiChild);
            }
        }

    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ProjectFileIndex.SERVICE.getInstance(e.getProject()).iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileInProject) {
                if(fileInProject.getFileType().getName().equals("JAVA")){
                    vFiles.add(fileInProject);
                }
                System.out.println(fileInProject.getFileType().getName());
                System.out.println(fileInProject.getCanonicalPath());
                return true;
            }
        });

        System.out.println("VFiles Size: " + vFiles.size());

        for(VirtualFile vFile : vFiles){
            isPsiComment(PsiManager.getInstance(e.getProject()).findFile(vFile));
        }

        System.out.println("PsiCommentList size after loop: " + psiCommentList.size());
        for(PsiComment comment : psiCommentList){
            System.out.println(comment.getText());

            ToolWindow window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Microissues");
            JLabel labelText = new JLabel(comment.getText());
            JComponent check = (JComponent) window.getComponent().add(labelText);
            check.repaint();
            window.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(
                    check, "Comment", true));;
        }
    }
}
