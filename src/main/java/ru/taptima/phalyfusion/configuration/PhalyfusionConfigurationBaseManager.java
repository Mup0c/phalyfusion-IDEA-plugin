package ru.taptima.phalyfusion.configuration;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.php.tools.quality.QualityToolConfigurationBaseManager;
import com.jetbrains.php.tools.quality.QualityToolConfigurationProvider;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhalyfusionConfigurationBaseManager extends QualityToolConfigurationBaseManager<PhalyfusionConfiguration> {
    public PhalyfusionConfigurationBaseManager() {
    }

    public static PhalyfusionConfigurationBaseManager getInstance() {
        return ServiceManager.getService(PhalyfusionConfigurationBaseManager.class);
    }

    @NotNull
    protected PhalyfusionConfiguration createLocalSettings() {
        return new PhalyfusionConfiguration();
    }

    @NotNull
    protected String getQualityToolName() {
        return "Phalyfusion";
    }

    @NotNull
    protected String getOldStyleToolPathName() {
        return "phalyfusion";
    }

    @NotNull
    protected String getConfigurationRootName() {
        return "phalyfusion_settings";
    }

    @Nullable
    protected QualityToolConfigurationProvider<PhalyfusionConfiguration> getConfigurationProvider() {
        return PhalyfusionConfigurationProvider.getInstances();
    }

    @Nullable
    protected PhalyfusionConfiguration loadLocal(Element element) {
        return XmlSerializer.deserialize(element, PhalyfusionConfiguration.class);
    }
}