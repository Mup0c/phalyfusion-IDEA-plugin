package ru.taptima.phalyfusion;

import com.jetbrains.php.tools.quality.QualityToolAnnotator;
import com.jetbrains.php.tools.quality.QualityToolValidationInspection;
import org.jetbrains.annotations.NotNull;

public class PhalyfusionValidationInspection extends QualityToolValidationInspection {
    @NotNull
    @Override
    protected PhalyfusionAnnotator getAnnotator() {
        return PhalyfusionAnnotator.INSTANCE;
    }

    @Override
    public String getToolName() {
        return "Phalyfusion";
    }
}
