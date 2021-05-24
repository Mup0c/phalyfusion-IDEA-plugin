package ru.taptima.phalyfusion.remote;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.jetbrains.php.config.interpreters.PhpInterpretersManagerImpl;
import com.jetbrains.php.config.interpreters.PhpSdkDependentConfiguration;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Tag("phalyfusion_by_interpreter")
public class PhalyfusionRemoteConfiguration extends PhalyfusionConfiguration implements PhpSdkDependentConfiguration {
    private String myInterpreterId;

    @Attribute("interpreter_id")
    @Nullable
    public String getInterpreterId() {
        return this.myInterpreterId;
    }

    public void setInterpreterId(@NotNull String interpreterId) {
        this.myInterpreterId = interpreterId;
    }

    @NotNull
    public String getPresentableName(@Nullable Project project) {
        return getDefaultName(PhpInterpretersManagerImpl.getInstance(project).findInterpreterName(this.getInterpreterId()));
    }

    @NotNull
    public String getId() {
        String interpreterId = this.getInterpreterId();
        return StringUtil.isEmpty(interpreterId) ? "Undefined interpreter" : interpreterId;
    }

    @NotNull
    public static String getDefaultName(@Nullable String interpreterName) {

        return StringUtil.isEmpty(interpreterName) ? "Undefined interpreter" : "Interpreter: " + interpreterName;
    }

    @NotNull
    public PhalyfusionRemoteConfiguration clone() {
        PhalyfusionRemoteConfiguration settings = new PhalyfusionRemoteConfiguration();
        settings.myInterpreterId = this.myInterpreterId;
        this.clone(settings);
        return settings;
    }

    public String serialize(@Nullable String path) {
        return path;
    }

    public String deserialize(@Nullable String path) {
        return path;
    }
}
