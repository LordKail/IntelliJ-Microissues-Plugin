package uk.ac.glasgow.microissues.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import uk.ac.glasgow.microissues.ui.TaskTree;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 2090140l on 03/02/17.
 */
public class PsiAndTicketHandler {

    private TaskTree taskTree;
    private Project project;
    private ConcurrentHashMap<String, ArrayList<PsiComment>> fileToComments;

    public PsiAndTicketHandler(TaskTree taskTree, Project project){
        this.taskTree = taskTree;
        this.project = project;
        fileToComments = new ConcurrentHashMap();
    }


    public void scanPsiFile(PsiElement element, PsiFile root){
        if(element instanceof PsiComment){
            if(element.getText().contains("@tckt")){
                String fileName = root.getVirtualFile().getName();
                fileToComments.putIfAbsent(fileName, new ArrayList<>());
                fileToComments.get(fileName).add((PsiComment) element);

                Ticket newTicket = new Ticket();
                newTicket.buildIssue((PsiComment) element);

                taskTree.addTicket(newTicket, fileName);

                System.out.println(fileName);
                System.out.println("Size of ArrayList: " + fileToComments.get(fileName).size());
            }

        } else {
            for(PsiElement psiChild : element.getChildren()){
                scanPsiFile(psiChild, root);
            }
        }
    }


    public void processProjectFiles() {
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileInProject) {
                if(fileInProject.getFileType().getName().equals("JAVA")){
                    // Something to do here.
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(fileInProject);
                    scanPsiFile(psiFile, psiFile);
                }
                return true;
            }
        });

        addPsiListener();

        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Microissues");
        //buildTree(window);
    }

    public void addPsiListener(){
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeListener() {

            @Override
            public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childAdded(@NotNull PsiTreeChangeEvent event) {
                System.out.println("The following child has been added: ");
                System.out.println(event.getChild().getText());

                if(event.getChild().getText().startsWith("/*") && event.getChild().getText().endsWith("*/")){
                    if(event.getChild().getText().contains("@tckt")){
                        System.out.println("A ticket has been added.");
                    }
                }

            }

            @Override
            public void childRemoved(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childReplaced(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void childMoved(@NotNull PsiTreeChangeEvent event) {

            }

            @Override
            public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

            }
        });
    }
}
