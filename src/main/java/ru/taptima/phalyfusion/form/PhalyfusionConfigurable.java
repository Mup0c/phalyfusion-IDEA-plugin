package ru.taptima.phalyfusion.form;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.*;
import ru.taptima.phalyfusion.PhalyfusionQualityToolType;
import ru.taptima.phalyfusion.blacklist.PhalyfusionIgnoredFilesConfigurable;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import ru.taptima.phalyfusion.configuration.PhalyfusionProjectConfiguration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PhalyfusionConfigurable extends QualityToolProjectConfigurableForm implements Configurable.NoScroll {
    public PhalyfusionConfigurable(@NotNull Project project) {
        super(project);
    }

    protected QualityToolProjectConfiguration<PhalyfusionConfiguration> getProjectConfiguration() {
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

    @Override
    public @NotNull JComponent getComponent() {
        return super.getComponent();
    }

    @NotNull
    protected QualityToolConfigurationComboBox<PhalyfusionConfiguration> createConfigurationComboBox() {
        return new PhalyfusionConfigurationComboBox(this.myProject);
    }

    @Override
    protected QualityToolType<PhalyfusionConfiguration> getQualityToolType() {
        return PhalyfusionQualityToolType.INSTANCE;
    }

    protected QualityToolsIgnoreFilesConfigurable getIgnoredFilesConfigurable() {
        return new PhalyfusionIgnoredFilesConfigurable(this.myProject);
    }
}

