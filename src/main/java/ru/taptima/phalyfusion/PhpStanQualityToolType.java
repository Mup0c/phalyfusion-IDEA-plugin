package ru.taptima.phalyfusion;

import com.jetbrains.php.tools.quality.QualityToolType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpStanQualityToolType extends QualityToolType {
    @NotNull
    @Override
    public String getDisplayName() {
        return "PHPStan";
    }
}
