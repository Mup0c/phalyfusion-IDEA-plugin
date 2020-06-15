package ru.taptima.phalyfusion.remote;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.remote.tools.quality.QualityToolByInterpreterDialog;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhalyfusionByInterpreterDialog extends QualityToolByInterpreterDialog<PhalyfusionConfiguration, PhalyfusionRemoteConfiguration> {
    protected PhalyfusionByInterpreterDialog(@Nullable Project project, @NotNull List<PhalyfusionConfiguration> settings) {
        super(project, settings, "Phalyfusion");
    }

    protected boolean canProcessSetting(@NotNull PhalyfusionConfiguration settings) {
        return settings instanceof PhalyfusionRemoteConfiguration;
    }

    @Nullable
    protected String getInterpreterId(@NotNull PhalyfusionRemoteConfiguration configuration) {
        return configuration.getInterpreterId();
    }
}
