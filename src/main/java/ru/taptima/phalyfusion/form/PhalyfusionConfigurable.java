package ru.taptima.phalyfusion.form;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.QualityToolConfigurationComboBox;
import com.jetbrains.php.tools.quality.QualityToolProjectConfigurableForm;
import com.jetbrains.php.tools.quality.QualityToolProjectConfiguration;
import com.jetbrains.php.tools.quality.QualityToolsIgnoreFilesConfigurable;
import ru.taptima.phalyfusion.PhalyfusionValidationInspection;
import ru.taptima.phalyfusion.blacklist.PhalyfusionIgnoredFilesConfigurable;
import ru.taptima.phalyfusion.configuration.PhalyfusionProjectConfiguration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class PhalyfusionConfigurable extends QualityToolProjectConfigurableForm implements Configurable.NoScroll {
    public PhalyfusionConfigurable(@NotNull Project project) {
        super(project);
    }

    protected QualityToolProjectConfiguration getProjectConfiguration() {
        return PhalyfusionProjectConfiguration.getInstance(this.myProject);
    }

    @Nls
    public String getDisplayName() {
        return "Phalyfusion";
    }

    public String getHelpTopic() {
        return null;
    }

    @NotNull
    public String getId() {
        return PhalyfusionConfigurable.class.getName();
    }

    @NotNull
    protected String getInspectionShortName() {
        return new PhalyfusionValidationInspection().getShortName();
    }

    @NotNull
    protected QualityToolConfigurationComboBox createConfigurationComboBox() {
        return new PhalyfusionConfigurationComboBox(this.myProject);
    }

    protected QualityToolsIgnoreFilesConfigurable getIgnoredFilesConfigurable() {
        return new PhalyfusionIgnoredFilesConfigurable(this.myProject);
    }
}

