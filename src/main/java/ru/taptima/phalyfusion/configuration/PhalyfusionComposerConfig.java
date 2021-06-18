package ru.taptima.phalyfusion.configuration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.jetbrains.php.composer.actions.log.ComposerLogMessageBuilder;
import com.jetbrains.php.tools.quality.QualityToolConfigurationManager;
import com.jetbrains.php.tools.quality.QualityToolsComposerConfig;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import ru.taptima.phalyfusion.PhalyfusionQualityToolType;
import ru.taptima.phalyfusion.PhalyfusionValidationInspection;


public class PhalyfusionComposerConfig extends QualityToolsComposerConfig<PhalyfusionConfiguration, PhalyfusionValidationInspection> {
    @NonNls
    private static final String PACKAGE = "taptima/phalyfusion";
    @NonNls
    private static final String RELATIVE_PATH;

    public PhalyfusionComposerConfig() {
        super(PACKAGE, RELATIVE_PATH);
    }

    @Override
    protected ComposerLogMessageBuilder.Settings getQualityToolsInspectionSettings() {
        return null;
    }

    @Override
    public String getQualityInspectionShortName() {
        return PhalyfusionQualityToolType.INSTANCE.getInspectionId();
    }

    @Override
    public @NotNull QualityToolConfigurationManager<PhalyfusionConfiguration> getConfigurationManager(@NotNull Project project) {
        return PhalyfusionConfigurationManager.getInstance(project);
    }

    static {
        RELATIVE_PATH = "bin/phalyfusion" + (SystemInfo.isWindows ? ".bat" : "");
    }
}
