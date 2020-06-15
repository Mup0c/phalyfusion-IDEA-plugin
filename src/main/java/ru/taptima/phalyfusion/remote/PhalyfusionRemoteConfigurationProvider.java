package ru.taptima.phalyfusion.remote;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathMappingSettings;
import com.intellij.util.xmlb.XmlSerializer;
import com.jetbrains.php.config.interpreters.PhpInterpreter;
import com.jetbrains.php.config.interpreters.PhpInterpretersManagerImpl;
import com.jetbrains.php.config.interpreters.PhpSdkAdditionalData;
import com.jetbrains.php.remote.tools.quality.QualityToolByInterpreterConfigurableForm;
import com.jetbrains.php.run.remote.PhpRemoteInterpreterManager;
import com.jetbrains.php.tools.quality.QualityToolConfigurableForm;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationManager;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfigurationProvider;
import ru.taptima.phalyfusion.form.PhalyfusionConfigurableForm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhalyfusionRemoteConfigurationProvider extends PhalyfusionConfigurationProvider {
    public String getConfigurationName(@Nullable String interpreterName) {
        return PhalyfusionRemoteConfiguration.getDefaultName(interpreterName);
    }

    public boolean canLoad(@NotNull String tagName) {
        return StringUtil.equals(tagName, "phalyfusion_by_interpreter");
    }

    @Nullable
    public PhalyfusionConfiguration load(@NotNull Element element) {
        return XmlSerializer.deserialize(element, PhalyfusionRemoteConfiguration.class);
    }

    @Nullable
    public QualityToolConfigurableForm<PhalyfusionRemoteConfiguration> createConfigurationForm(@NotNull Project project, @NotNull PhalyfusionConfiguration settings) {
        if (settings instanceof PhalyfusionRemoteConfiguration) {
            PhalyfusionRemoteConfiguration remoteConfiguration = (PhalyfusionRemoteConfiguration)settings;
            PhalyfusionConfigurableForm<PhalyfusionRemoteConfiguration> delegate = new PhalyfusionConfigurableForm<>(project, remoteConfiguration);
            return new QualityToolByInterpreterConfigurableForm<>(project, remoteConfiguration, delegate);
        } else {
            return null;
        }
    }

    public PhalyfusionConfiguration createNewInstance(@Nullable Project project, @NotNull List<PhalyfusionConfiguration> existingSettings) {
        PhalyfusionByInterpreterDialog dialog = new PhalyfusionByInterpreterDialog(project, existingSettings);
        if (dialog.showAndGet()) {
            String id = PhpInterpretersManagerImpl.getInstance(project).findInterpreterId(dialog.getSelectedInterpreterName());
            if (StringUtil.isNotEmpty(id)) {
                PhalyfusionRemoteConfiguration settings = new PhalyfusionRemoteConfiguration();
                settings.setInterpreterId(id);
                PhpSdkAdditionalData data = PhpInterpretersManagerImpl.getInstance(project).findInterpreterDataById(id);
                PhpRemoteInterpreterManager manager = PhpRemoteInterpreterManager.getInstance();
                if (manager != null && data != null) {
                    PathMappingSettings mappings = manager.createPathMappings(project, data);
                    if (project != null) {
                        this.fillSettingsByDefaultValue(settings, PhalyfusionConfigurationManager.getInstance(project).getLocalSettings(), (localPath) -> localPath == null ? null : mappings.convertToRemote(localPath));
                    }
                }

                return settings;
            }
        }

        return null;
    }

    public PhalyfusionConfiguration createConfigurationByInterpreter(@NotNull PhpInterpreter interpreter) {
        PhalyfusionRemoteConfiguration settings = new PhalyfusionRemoteConfiguration();
        settings.setInterpreterId(interpreter.getId());
        return settings;
    }
}
