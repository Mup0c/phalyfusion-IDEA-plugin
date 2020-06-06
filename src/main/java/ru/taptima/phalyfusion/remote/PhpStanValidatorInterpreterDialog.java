package ru.taptima.phalyfusion.remote;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.remote.tools.quality.QualityToolByInterpreterDialog;
import ru.taptima.phalyfusion.configuration.PhpStanValidatorConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpStanValidatorInterpreterDialog extends QualityToolByInterpreterDialog<PhpStanValidatorConfiguration, PhpStanValidatorRemoteConfiguration> {
    protected PhpStanValidatorInterpreterDialog(@Nullable Project project, @NotNull List<PhpStanValidatorConfiguration> settings) {
        super(project, settings, "PHPStan");
    }

    protected boolean canProcessSetting(@NotNull PhpStanValidatorConfiguration settings) {
        return settings instanceof PhpStanValidatorRemoteConfiguration;
    }

    @Nullable
    protected String getInterpreterId(@NotNull PhpStanValidatorRemoteConfiguration configuration) {
        return configuration.getInterpreterId();
    }
}
