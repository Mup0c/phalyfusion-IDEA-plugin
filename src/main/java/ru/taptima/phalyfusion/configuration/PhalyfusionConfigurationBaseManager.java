package ru.taptima.phalyfusion.configuration;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.php.composer.ComposerConfigUtils;
import com.jetbrains.php.composer.ComposerDataService;
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
        var phalyfusionConfig = new PhalyfusionConfiguration();

        var projects = ProjectManager.getInstance().getOpenProjects();
        for (var project : projects) {
            var config= ComposerDataService.getInstance(project).getConfigFile();
            if (config == null) {
                continue;
            }

            var vendors = ComposerConfigUtils.getVendorAndBinDirs(config);
            if (vendors == null) {
                continue;
            }

            var packagesList = ComposerConfigUtils.getInstalledPackagesFromConfig(config);
            if (packagesList.stream().anyMatch(it -> it.getName().equals("taptima/phalyfusion"))) {
                var phalyfusionPath = vendors.second + "/phalyfusion" + (SystemInfo.isWindows ? ".bat" : "");
                phalyfusionConfig.setToolPath(config.getParent().getPath() + "/" + phalyfusionPath);
                break;
            }
        }

        return phalyfusionConfig;
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