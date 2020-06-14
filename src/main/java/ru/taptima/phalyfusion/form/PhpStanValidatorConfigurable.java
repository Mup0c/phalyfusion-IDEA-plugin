package ru.taptima.phalyfusion.form;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.QualityToolConfigurationComboBox;
import com.jetbrains.php.tools.quality.QualityToolProjectConfigurableForm;
import com.jetbrains.php.tools.quality.QualityToolProjectConfiguration;
import com.jetbrains.php.tools.quality.QualityToolsIgnoreFilesConfigurable;
import ru.taptima.phalyfusion.PhpStanFixerValidationInspection;
import ru.taptima.phalyfusion.blacklist.PhpStanValidatorIgnoredFilesConfigurable;
import ru.taptima.phalyfusion.configuration.PhpStanValidatorProjectConfiguration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpStanValidatorConfigurable extends QualityToolProjectConfigurableForm implements Configurable.NoScroll {
    public PhpStanValidatorConfigurable(@NotNull Project project) {
        super(project);
    }

    protected QualityToolProjectConfiguration getProjectConfiguration() {
        return PhpStanValidatorProjectConfiguration.getInstance(this.myProject);
    }

    @Nls
    public String getDisplayName() {
        return "Phalyfusion";
    }

    public String getHelpTopic() {
        return "settings.phpstan.validator";
    }

    @NotNull
    public String getId() {
        return PhpStanValidatorConfigurable.class.getName();
    }

    @NotNull
    protected String getInspectionShortName() {
        return new PhpStanFixerValidationInspection().getShortName();
    }

    @NotNull
    protected QualityToolConfigurationComboBox createConfigurationComboBox() {
        return new PhpStanValidatorConfigurationComboBox(this.myProject);
    }

    protected QualityToolsIgnoreFilesConfigurable getIgnoredFilesConfigurable() {
        return new PhpStanValidatorIgnoredFilesConfigurable(this.myProject);
    }
}

