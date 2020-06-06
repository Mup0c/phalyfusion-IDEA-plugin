package ru.taptima.phalyfusion.form;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.QualityToolConfigurableList;
import com.jetbrains.php.tools.quality.QualityToolConfigurationComboBox;
import com.jetbrains.php.tools.quality.QualityToolConfigurationManager;
import ru.taptima.phalyfusion.configuration.PhpStanValidatorConfiguration;
import ru.taptima.phalyfusion.configuration.PhpStanValidatorConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpStanValidatorConfigurationComboBox extends QualityToolConfigurationComboBox<PhpStanValidatorConfiguration> {
    public PhpStanValidatorConfigurationComboBox(@Nullable Project project) {
        super(project);
    }

    protected QualityToolConfigurableList<PhpStanValidatorConfiguration> getQualityToolConfigurableList(@NotNull Project project, @Nullable String item) {
        return new PhpStanValidatorQualityToolConfigurableList(project, item);
    }

    protected QualityToolConfigurationManager<PhpStanValidatorConfiguration> getConfigurationManager(@NotNull Project project) {
        return PhpStanValidatorConfigurationManager.getInstance(project);
    }
}
