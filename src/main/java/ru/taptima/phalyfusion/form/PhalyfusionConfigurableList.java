package ru.taptima.phalyfusion.form;

import com.intellij.openapi.project.Project;
import com.intellij.util.ObjectUtils;
import com.jetbrains.php.tools.quality.QualityToolConfigurableForm;
import com.jetbrains.php.tools.quality.QualityToolConfigurableList;
import com.jetbrains.php.tools.quality.QualityToolConfiguration;
import com.jetbrains.php.tools.quality.QualityToolConfigurationProvider;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationManager;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhalyfusionConfigurableList extends QualityToolConfigurableList<PhalyfusionConfiguration> {
    public PhalyfusionConfigurableList(@NotNull Project project, @Nullable String initialElement) {
        super(project, PhalyfusionConfigurationManager.getInstance(project), PhalyfusionConfiguration::new, PhalyfusionConfiguration::clone, (settings) -> {
            PhalyfusionConfigurationProvider provider = PhalyfusionConfigurationProvider.getInstances();
            if (provider != null) {
                QualityToolConfigurableForm form = provider.createConfigurationForm(project, settings);
                if (form != null) {
                    return form;
                }
            }

            return new PhalyfusionConfigurableForm<>(project, settings);
        }, initialElement);
        this.setSubjectDisplayName("phalyfusion");
    }

    @Nullable
    protected PhalyfusionConfiguration getConfiguration(@Nullable QualityToolConfiguration configuration) {
        return ObjectUtils.tryCast(configuration, PhalyfusionConfiguration.class);
    }

    @Nullable
    @Override
    protected QualityToolConfigurationProvider<PhalyfusionConfiguration> getConfigurationProvider() {
        return PhalyfusionConfigurationProvider.getInstances();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Phalyfusion";
    }
}
