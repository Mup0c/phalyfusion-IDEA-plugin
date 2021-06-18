package ru.taptima.phalyfusion.configuration;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.php.tools.quality.QualityToolConfigurationBaseManager;
import com.jetbrains.php.tools.quality.QualityToolType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.taptima.phalyfusion.PhalyfusionQualityToolType;


public class PhalyfusionConfigurationBaseManager extends QualityToolConfigurationBaseManager<PhalyfusionConfiguration> {
    public PhalyfusionConfigurationBaseManager() {
    }

    @Override
    protected @NotNull QualityToolType<PhalyfusionConfiguration> getQualityToolType() {
        return PhalyfusionQualityToolType.INSTANCE;
    }

    public static PhalyfusionConfigurationBaseManager getInstance() {
        return ServiceManager.getService(PhalyfusionConfigurationBaseManager.class);
    }

    @Override
    @NotNull
    protected String getOldStyleToolPathName() {
        return "phalyfusion";
    }

    @Override
    @NotNull
    protected String getConfigurationRootName() {
        return "phalyfusion_settings";
    }

    @Override
    @Nullable
    protected PhalyfusionConfiguration loadLocal(Element element) {
        return XmlSerializer.deserialize(element, PhalyfusionConfiguration.class);
    }
}