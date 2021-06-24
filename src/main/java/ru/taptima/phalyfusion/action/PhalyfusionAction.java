package ru.taptima.phalyfusion.action;

import com.intellij.codeInspection.actions.RunInspectionAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class PhalyfusionAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Could project be null?
        Project currentProject = getEventProject(event);

        VirtualFile selectedFile = null;
        PsiFile psiFile = null;

        var virtualFiles = FileEditorManager.getInstance(currentProject).getSelectedFiles();
        if (virtualFiles.length > 0) {
            selectedFile = virtualFiles[0];
        }

        if (selectedFile != null) {
            psiFile = PsiManager.getInstance(currentProject).findFile(selectedFile);
        }

        RunInspectionAction.runInspection(currentProject, "PhalyfusionGlobal", selectedFile, null, psiFile);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }
}
