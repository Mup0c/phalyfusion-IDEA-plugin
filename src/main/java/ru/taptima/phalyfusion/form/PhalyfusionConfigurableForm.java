package ru.taptima.phalyfusion.form;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.OnOffButton;
import com.jetbrains.php.tools.quality.QualityToolConfigurableForm;
import com.jetbrains.php.tools.quality.QualityToolCustomSettings;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PhalyfusionConfigurableForm<C extends PhalyfusionConfiguration> extends QualityToolConfigurableForm<C> {
    public PhalyfusionConfigurableForm(@NotNull Project project, @NotNull C configuration) {
        super(project, configuration, "Phalyfusion", "phalyfusion");
    }

    @Nls
    public String getDisplayName() {
        return "Phalyfusion";
    }

    @Nullable
    public String getHelpTopic() {
        return null;
    }

    @NotNull
    public String getId() {
        return PhalyfusionConfigurableForm.class.getName();
    }

    @NotNull
    public Pair<Boolean, String> validateMessage(String message) {
        return message.contains("Phalyfusion")
            ? Pair.create(true, "OK, " + message)
            : Pair.create(false, message);
    }

    @Override
    public @Nullable QualityToolCustomSettings getCustomConfigurable(@NotNull Project project, @NotNull C configuration) {
        return new PhalyfusionCustomConfigurable(configuration);
    }

    public boolean isValidToolFile(VirtualFile file) {
        return file.getName().startsWith("phalyfusion");
    }

    static class PhalyfusionCustomConfigurable extends QualityToolCustomSettings {
        public PhalyfusionCustomConfigurable(PhalyfusionConfiguration configuration) {
            super();
            myConfiguration = configuration;
            initialOnFlyMode = myConfiguration.getOnFlyMode();
        }

        private final PhalyfusionConfiguration myConfiguration;
        private final boolean initialOnFlyMode;
        private OnOffButton onFlyModeBtn;

        @Override
        public @NotNull Pair<Boolean, String> validate() {
            return new Pair<>(true, "OK");
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
            return "Phalyfusion On-Fly Mode";
        }

        @Override
        public @Nullable JComponent createComponent() {
            var panel = new JPanel();
            var layout = new BoxLayout(panel, BoxLayout.X_AXIS);
            panel.setLayout(layout);
            var label = new JLabel("Launch Phalyfusion tool in on-fly mode (may cause lags)");
            panel.add(label);
            onFlyModeBtn = new OnOffButton();
            onFlyModeBtn.setSelected(initialOnFlyMode);
            panel.add(onFlyModeBtn);
            return panel;
        }

        @Override
        public boolean isModified() {
            return onFlyModeBtn.isSelected() != initialOnFlyMode;
        }

        @Override
        public void apply() throws ConfigurationException {
            myConfiguration.setOnFlyMode(onFlyModeBtn.isSelected());
        }
    }
}
