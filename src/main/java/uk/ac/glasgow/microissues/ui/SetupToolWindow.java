package uk.ac.glasgow.microissues.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import org.jetbrains.annotations.NotNull;
import uk.ac.glasgow.microissues.plugin.PsiAndTicketHandler;

import javax.swing.*;
import java.awt.*;

/**
 * The class for initiating setup of the toolwindow - starting the process of scanning for tickets in the project.
 */

public class SetupToolWindow implements StartupActivity {

    private JComponent microissuesContainer;

    /**
     * Invoked after the project has fully loaded. The tool window of the plugin is retrieved and the process of
     * scanning java files for tickets is started.
     * @param project
     */
    @Override
    public void runActivity(@NotNull Project project) {

        microissuesContainer = new JPanel(new BorderLayout(1, 1));
        microissuesContainer.setBorder(null);

        StartupManager.getInstance(project)
                .registerPostStartupActivity(() -> {

        ApplicationManager.getApplication().invokeLater(

                new Runnable() {
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        ToolWindowComponent toolWindowComponent = project.getComponent(ToolWindowComponent.class);
                                        TaskTree taskTree = toolWindowComponent.getTaskTree();
                                        PsiAndTicketHandler psiHandler = new PsiAndTicketHandler(taskTree, project);
                                        psiHandler.processProjectFiles();
                                        toolWindowComponent.setPsiHandler(psiHandler);
                                    }
                                }
                        );
                    }
                });
        });
    }

}
