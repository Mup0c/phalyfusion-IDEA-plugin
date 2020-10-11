package ru.taptima.phalyfusion.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.jetbrains.php.tools.quality.QualityToolProjectConfiguration;
import ru.taptima.phalyfusion.PhalyfusionValidationInspection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "PhalyfusionProjectConfiguration",
    storages = {@Storage("$WORKSPACE_FILE$")}
)
public class PhalyfusionProjectConfiguration extends QualityToolProjectConfiguration<PhalyfusionConfiguration> implements PersistentStateComponent<PhalyfusionProjectConfiguration> {
    public PhalyfusionProjectConfiguration() {
    }

    public static PhalyfusionProjectConfiguration getInstance(Project project) {
        return ServiceManager.getService(project, PhalyfusionProjectConfiguration.class);
    }

    @Nullable
    public PhalyfusionProjectConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PhalyfusionProjectConfiguration state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    protected String getInspectionId() {
        return (new PhalyfusionValidationInspection()).getID();
    }

    @NotNull
    protected String getQualityToolName() {
        return "Phalyfusion";
    }

    @NotNull
    protected PhalyfusionConfigurationManager getConfigurationManager(@NotNull Project project) {
        return PhalyfusionConfigurationManager.getInstance(project);
    }
}

