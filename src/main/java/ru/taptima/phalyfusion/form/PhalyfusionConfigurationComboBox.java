package ru.taptima.phalyfusion.form;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.QualityToolConfigurableList;
import com.jetbrains.php.tools.quality.QualityToolConfigurationComboBox;
import com.jetbrains.php.tools.quality.QualityToolConfigurationManager;
import ru.taptima.phalyfusion.PhalyfusionQualityToolType;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhalyfusionConfigurationComboBox extends QualityToolConfigurationComboBox<PhalyfusionConfiguration> {
    public PhalyfusionConfigurationComboBox(@Nullable Project project) {
        super(project, PhalyfusionQualityToolType.INSTANCE);
    }

    protected QualityToolConfigurableList<PhalyfusionConfiguration> getQualityToolConfigurableList(@NotNull Project project, @Nullable String item) {
        return new PhalyfusionConfigurableList(project, item);
    }

    protected QualityToolConfigurationManager<PhalyfusionConfiguration> getConfigurationManager(@NotNull Project project) {
        return PhalyfusionConfigurationManager.getInstance(project);
    }
}
