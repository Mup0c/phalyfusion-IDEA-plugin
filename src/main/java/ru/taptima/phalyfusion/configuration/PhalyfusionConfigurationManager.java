package ru.taptima.phalyfusion.configuration;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.jetbrains.php.tools.quality.QualityToolConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhalyfusionConfigurationManager extends QualityToolConfigurationManager<PhalyfusionConfiguration> {
    public PhalyfusionConfigurationManager(@Nullable Project project) {
        super(project);
        if (project != null) {
            this.myProjectManager = ServiceManager.getService(project, ProjectPhalyfusionConfigurationBaseManager.class);
        }

        this.myApplicationManager = ServiceManager.getService(AppPhalyfusionConfigurationBaseManager.class);
    }

    @NotNull
    protected List<PhalyfusionConfiguration> getDefaultProjectSettings() {
        ProjectPhalyfusionConfigurationBaseManager service = ServiceManager.getService(
            ProjectManager.getInstance().getDefaultProject(),
            ProjectPhalyfusionConfigurationBaseManager.class
        );

        return service.getSettings();
    }

    public static PhalyfusionConfigurationManager getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, PhalyfusionConfigurationManager.class);
    }

    @State(
        name = "Phalyfusion",
        storages = {@Storage("php.xml")}
    )
    static class AppPhalyfusionConfigurationBaseManager extends PhalyfusionConfigurationBaseManager {
        AppPhalyfusionConfigurationBaseManager() {}
    }

    @State(
        name = "Phalyfusion",
        storages = {@Storage("php.xml")}
    )
    static class ProjectPhalyfusionConfigurationBaseManager extends PhalyfusionConfigurationBaseManager {
        ProjectPhalyfusionConfigurationBaseManager() {}
    }
}
