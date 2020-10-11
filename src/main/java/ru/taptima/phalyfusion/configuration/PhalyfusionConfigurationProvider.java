package ru.taptima.phalyfusion.configuration;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.NullableFunction;
import com.jetbrains.php.tools.quality.QualityToolConfigurationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PhalyfusionConfigurationProvider extends QualityToolConfigurationProvider<PhalyfusionConfiguration> {
    private static final ExtensionPointName<PhalyfusionConfigurationProvider> EP_NAME = ExtensionPointName.create("ru.taptima.phalyfusion.phalyfusionConfigurationProvider");

    @Nullable
    public static PhalyfusionConfigurationProvider getInstances() {
        PhalyfusionConfigurationProvider[] extensions = EP_NAME.getExtensions();
        if (extensions.length > 1) {
            throw new RuntimeException("Several providers for remote Phalyfusion configuration was found");
        }

        return extensions.length == 1 ? extensions[0] : null;
    }

    protected void fillSettingsByDefaultValue(@NotNull PhalyfusionConfiguration settings, @NotNull PhalyfusionConfiguration localConfiguration, @NotNull NullableFunction<String, String> preparePath) {
        super.fillSettingsByDefaultValue(settings, localConfiguration, preparePath);

        String toolPath = preparePath.fun(localConfiguration.getToolPath());
        if (StringUtil.isNotEmpty(toolPath)) {
            settings.setToolPath(toolPath);
        }

        settings.setTimeout(localConfiguration.getTimeout());
    }
}