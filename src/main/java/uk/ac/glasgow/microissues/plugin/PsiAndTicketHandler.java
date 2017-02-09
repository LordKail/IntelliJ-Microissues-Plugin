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
                    taskTree.flushTicketsInFile(psiFile.getVirtualFile().getName());
                    scanPsiFile(psiFile, psiFile);
                }
                return true;
            }
        });

        addPsiListener();

        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Microissues");
        //buildTree(window);
    }

    public void elementAddedOrRemoved(PsiTreeChangeEvent event, PsiElement elementToCheck){
        if(elementToCheck instanceof PsiComment) {
            System.out.println("The following child has been added: ");
            System.out.println(elementToCheck.getText());

            String commentText = elementToCheck.getText();
            if (commentText.startsWith("/*") && commentText.endsWith("*/")) {
                if (commentText.contains("@tckt")) {
                    PsiFile parent = null;
                    if(event.getParent() instanceof PsiFile) {
                        System.out.println("Class of parent: " + event.getParent().getClass().getName());
                        parent = (PsiFile) event.getParent();
                    }
                    if(event.getParent().getParent() instanceof PsiFile) {
                        System.out.println("Class of parent parent: " + event.getParent().getParent().getClass().getName());
                        parent = (PsiFile) event.getParent().getParent();
                    }

                    taskTree.flushTicketsInFile(parent.getVirtualFile().getName());
                    scanPsiFile(parent, parent);
                }
            }
        }
    }

    public void addPsiListener(){
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeListener() {

            @Override
            public void childAdded(@NotNull PsiTreeChangeEvent event) {
                elementAddedOrRemoved(event, event.getChild());

            }

            @Override
            public void childRemoved(@NotNull PsiTreeChangeEvent event) {
                elementAddedOrRemoved(event, event.getChild());
            }

            @Override
            public void childReplaced(@NotNull PsiTreeChangeEvent event) {

                /*
                String newChildText = event.getNewChild().getText();
                String oldChildText = event.getOldChild().getText();

                System.out.println("Old Child: " + oldChildText);
                System.out.println("New Child: " + newChildText);

                if(newChildText.startsWith("file://") && newChildText.endsWith(".java")){
                    String oldFileName = oldChildText.substring(oldChildText.lastIndexOf('/') + 1);
                    String newFileName = newChildText.substring(newChildText.lastIndexOf('/') + 1);

                    System.out.println("About to enter fileRenamed:");
                    taskTree.fileRenamed(oldFileName, newFileName);
                }
                else {
                    elementAddedOrRemoved(event, event.getNewChild());
                }
                */

                elementAddedOrRemoved(event, event.getNewChild());
            }

            @Override
            public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {}

            @Override
            public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {}

            @Override
            public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {}

            @Override
            public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {}

            @Override
            public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {}

            @Override
            public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {}

            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                System.out.println("Children changed!");
            }

            @Override
            public void childMoved(@NotNull PsiTreeChangeEvent event) {
                System.out.println("Children moved!");
            }

            @Override
            public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
                System.out.println("Property changed!");

                String newFileName = event.getNewValue().toString();
                String oldFileName = event.getOldValue().toString();

                if(event.getPropertyName().equals("fileName")){
                    System.out.println("About to enter fileRenamed:");
                    taskTree.fileRenamed(oldFileName, newFileName);
                }
            }
        });
    }
}
