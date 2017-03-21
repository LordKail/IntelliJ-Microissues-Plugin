package uk.ac.glasgow.microissues.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import uk.ac.glasgow.microissues.ui.TaskTree;

/**
 * The class handling the PsiElements (PsiComments) and correspondingly updating the task tree.
 */
public class PsiAndTicketHandler {

    private TaskTree taskTree;
    private Project project;

    public PsiAndTicketHandler(TaskTree taskTree, Project project){
        this.taskTree = taskTree;
        this.project = project;
    }

    /**
     * Recursive function for scanning the psiFile for comments.
     * @param element the element to be checked if it is an instance of PsiComment (and if so, whether it is also a ticket).
     * @param root The root psiFile (used for extracting the filename)
     */
    public void scanPsiFile(PsiElement element, PsiFile root){
        if(element instanceof PsiComment){
            if(element.getText().contains("@tckt")){
                String fileName = root.getVirtualFile().getName();

                Ticket newTicket = new Ticket();
                newTicket.buildIssue((PsiComment) element);

                taskTree.addTicket(newTicket, fileName);

            }

        } else {
            for(PsiElement psiChild : element.getChildren()){
                scanPsiFile(psiChild, root);
            }
        }
    }

    /**
     * The first method that is called upon launching the plugin at IDE startup.
     * The method for scanning the whole project for java files and then using scanPsiFile method for checking the files
     * for comments.
     */
    public void processProjectFiles() {
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileInProject) {
                if(fileInProject.getFileType().getName().equals("JAVA")){
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(fileInProject);
                    taskTree.flushTicketsInFile(psiFile.getVirtualFile().getName());
                    scanPsiFile(psiFile, psiFile);
                }
                return true;
            }
        });

        addPsiListener();
    }


    /**
     * The method that is responsible for checking if the replaced,added or removed element is a comment containing a
     * ticket. If so, flushTicketsInFile and scanPsiFile methods are called to
     * @param event The event that triggered the change, carries the corresponding psiElement with it.
     * @param elementToCheck The element extracted from the event change, checked whether it is a ticket-containing comment.
     */
    public void elementAddedOrRemoved(PsiTreeChangeEvent event, PsiElement elementToCheck){
        if(elementToCheck instanceof PsiComment) {
            String commentText = elementToCheck.getText();
            if (commentText.startsWith("/*") && commentText.endsWith("*/")) {
                if (commentText.contains("@tckt")) {
                    PsiElement possibleParent = event.getParent();

                    while(!(possibleParent instanceof PsiFile)){
                        possibleParent = possibleParent.getParent();
                    }

                    PsiFile parent = (PsiFile) possibleParent;
                    taskTree.flushTicketsInFile(parent.getVirtualFile().getName());
                    scanPsiFile(parent, parent);
                }
            }
        }
    }


    /**
     * The listener for PsiElement changes in the code. Is responsible for keeping track whenever PsiElements are added,
     * deleted, replaced or if their property has been changed.
     */
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
                elementAddedOrRemoved(event, event.getOldChild());
                elementAddedOrRemoved(event, event.getNewChild());
            }

            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                PsiElement parent = event.getParent();
                if(parent instanceof PsiFile){
                    PsiFile psiFile = (PsiFile) parent;
                    taskTree.flushTicketsInFile(psiFile.getVirtualFile().getName());
                    scanPsiFile(psiFile, psiFile);
                }
            }

            @Override
            public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
                if(event.getPropertyName().equals("fileName")){
                    String newFileName = event.getNewValue().toString();
                    String oldFileName = event.getOldValue().toString();

                    taskTree.fileRenamed(oldFileName, newFileName);
                }
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
            public void childMoved(@NotNull PsiTreeChangeEvent event) {}

        });
    }
}
