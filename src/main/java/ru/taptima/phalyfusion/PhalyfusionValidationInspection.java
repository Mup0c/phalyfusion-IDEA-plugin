package ru.taptima.phalyfusion;

import com.intellij.codeInspection.CleanupLocalInspectionTool;
import com.jetbrains.php.tools.quality.QualityToolAnnotator;
import com.jetbrains.php.tools.quality.QualityToolValidationInspection;
import org.jetbrains.annotations.NotNull;

public class PhalyfusionValidationInspection extends QualityToolValidationInspection {
    @NotNull
    @Override
    protected QualityToolAnnotator getAnnotator() {
        return PhalyfusionAnnotator.INSTANCE;
    }

    @Override
    public String getToolName() {
        return "Phalyfusion";
    }
}
