package ru.taptima.phalyfusion.form;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.tools.quality.QualityToolConfigurableForm;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public boolean isValidToolFile(VirtualFile file) {
        return file.getName().startsWith("phalyfusion");
    }
}
