package ru.taptima.phalyfusion.blacklist;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.QualityToolBlackList;

@State(
    name = "PhalyfusionBlackList",
    storages = {@Storage("$WORKSPACE_FILE$")}
)
public class PhalyfusionBlackList extends QualityToolBlackList {
    public static PhalyfusionBlackList getInstance(Project project) {
        return ServiceManager.getService(project, PhalyfusionBlackList.class);
    }
}