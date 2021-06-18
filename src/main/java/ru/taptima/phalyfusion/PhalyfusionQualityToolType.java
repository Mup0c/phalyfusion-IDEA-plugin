package ru.taptima.phalyfusion;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.taptima.phalyfusion.blacklist.PhalyfusionBlackList;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationManager;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationProvider;
import ru.taptima.phalyfusion.configuration.PhalyfusionProjectConfiguration;
import ru.taptima.phalyfusion.form.PhalyfusionConfigurable;
import ru.taptima.phalyfusion.form.PhalyfusionConfigurableForm;

public class PhalyfusionQualityToolType extends QualityToolType<PhalyfusionConfiguration> {
    public static final PhalyfusionQualityToolType INSTANCE = new PhalyfusionQualityToolType();

    private PhalyfusionQualityToolType() {
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Phalyfusion";
    }

    @Override
    public @NotNull QualityToolBlackList getQualityToolBlackList(@NotNull Project project) {
        return PhalyfusionBlackList.getInstance(project);
    }

    @Override
    protected @NotNull QualityToolConfigurationManager<PhalyfusionConfiguration> getConfigurationManager(@NotNull Project project) {
        return PhalyfusionConfigurationManager.getInstance(project);
    }

    @Override
    protected @NotNull QualityToolValidationInspection getInspection() {
        return new PhalyfusionValidationInspection();
    }

    @Override
    protected @Nullable QualityToolConfigurationProvider<PhalyfusionConfiguration> getConfigurationProvider() {
        return PhalyfusionConfigurationProvider.getInstances();
    }

    @Override
    protected @NotNull QualityToolConfigurableForm<PhalyfusionConfiguration> createConfigurableForm(@NotNull Project project, PhalyfusionConfiguration qualityToolConfiguration) {
        return new PhalyfusionConfigurableForm(project, qualityToolConfiguration);
    }

    @Override
    protected @NotNull Configurable getToolConfigurable(@NotNull Project project) {
        return new PhalyfusionConfigurable(project);
    }

    @Override
    protected @NotNull QualityToolProjectConfiguration getProjectConfiguration(@NotNull Project project) {
        return PhalyfusionProjectConfiguration.getInstance(project);
    }

    @NotNull
    @Override
    protected PhalyfusionConfiguration createConfiguration() {
        return new PhalyfusionConfiguration();
    }
}
