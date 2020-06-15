package ru.taptima.phalyfusion;

import com.jetbrains.php.tools.quality.QualityToolType;
import org.jetbrains.annotations.NotNull;

public class PhalyfusionQualityToolType extends QualityToolType {
    @NotNull
    @Override
    public String getDisplayName() {
        return "Phalyfusion";
    }
}
