package ru.taptima.phalyfusion.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;

public class runPhalyfusionInspectionAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        AnAction anAction = ActionManager.getInstance().getAction("RunInspection");

        DataContext context = e.getDataContext(); //DataManager.getInstance().getDataContext();
        AnActionEvent anActionEvent = new AnActionEvent(null, context, "", anAction.getTemplatePresentation(), ActionManager.getInstance(), 0);

        anAction.actionPerformed(anActionEvent);
    }
}
