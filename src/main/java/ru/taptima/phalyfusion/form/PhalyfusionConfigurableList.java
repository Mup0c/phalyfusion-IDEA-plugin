package ru.taptima.phalyfusion.form;

import com.intellij.openapi.project.Project;
import com.intellij.util.ObjectUtils;
import com.jetbrains.php.tools.quality.*;
import ru.taptima.phalyfusion.PhalyfusionQualityToolType;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationManager;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhalyfusionConfigurableList extends QualityToolConfigurableList<PhalyfusionConfiguration> {
    public PhalyfusionConfigurableList(@NotNull Project project, @Nullable String initialElement) {
        super(project, PhalyfusionQualityToolType.INSTANCE, initialElement);
        this.setSubjectDisplayName("phalyfusion");
    }

    @Nullable
    protected PhalyfusionConfiguration getConfiguration(@Nullable QualityToolConfiguration configuration) {
        return ObjectUtils.tryCast(configuration, PhalyfusionConfiguration.class);
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Phalyfusion";
    }

    @Override
    protected QualityToolType<PhalyfusionConfiguration> getQualityToolType() {
        return PhalyfusionQualityToolType.INSTANCE;
    }
}
