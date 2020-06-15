package ru.taptima.phalyfusion.blacklist;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.QualityToolsIgnoreFilesConfigurable;
import org.jetbrains.annotations.NotNull;

public class PhalyfusionIgnoredFilesConfigurable extends QualityToolsIgnoreFilesConfigurable {
    public PhalyfusionIgnoredFilesConfigurable(Project project) {
        super(PhalyfusionBlackList.getInstance(project), project);
    }

    @NotNull
    public String getId() {
        return PhalyfusionIgnoredFilesConfigurable.class.getName();
    }

    @NotNull
    protected String getQualityToolName() {
        return "Phalyfusion";
    }
}
