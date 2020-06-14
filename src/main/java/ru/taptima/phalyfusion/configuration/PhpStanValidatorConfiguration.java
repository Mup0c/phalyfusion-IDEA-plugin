package ru.taptima.phalyfusion.configuration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Transient;
import com.jetbrains.php.tools.quality.QualityToolConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpStanValidatorConfiguration implements QualityToolConfiguration {
    private static final String LOCAL = "Local";
    private String myPHPStanPath = "";
    private String myStandards = "";
    private int myMaxMessagesPerFile = 100;
    private int myTimeoutMs = 30000;

    public PhpStanValidatorConfiguration() {
    }

    @Transient
    public String getToolPath() {
        return this.myPHPStanPath;
    }

    public void setToolPath(String toolPath) {
        this.myPHPStanPath = toolPath;
    }

    @Attribute("tool_path")
    @Nullable
    public String getSerializedToolPath() {
        return this.serialize(this.myPHPStanPath);
    }

    public void setSerializedToolPath(@Nullable String configurationFilePath) {
        this.myPHPStanPath = this.deserialize(configurationFilePath);
    }

    @Attribute("max_messages_per_file")
    public int getMaxMessagesPerFile() {
        return this.myMaxMessagesPerFile;
    }

    public void setMaxMessagesPerFile(int maxMessagesPerFile) {
        this.myMaxMessagesPerFile = maxMessagesPerFile;
    }

    @Attribute("standards")
    public String getSerializedStandards() {
        return this.myStandards;
    }

    public void setSerializedStandards(String standards) {
        this.myStandards = standards;
    }

    @Transient
    public String[] getStandards() {
        return (String[]) ArrayUtil.append(this.myStandards.split(";"), "Custom");
    }

    public void setStandards(String[] standards) {
        this.myStandards = (String) Arrays.stream(standards).filter((standard) -> {
            return !"Custom".equals(standard);
        }).collect(Collectors.joining(";"));
    }

    @Attribute("timeout")
    public int getTimeout() {
        return this.myTimeoutMs;
    }

    public void setTimeout(int timeout) {
        this.myTimeoutMs = timeout;
    }

    @NotNull
    public String getPresentableName(@Nullable Project project) {
        return this.getId();
    }

    @NotNull
    public String getId() {
        return "Local";
    }

    @Nullable
    public String getInterpreterId() {
        return null;
    }

    public PhpStanValidatorConfiguration clone() {
        PhpStanValidatorConfiguration settings = new PhpStanValidatorConfiguration();
        this.clone(settings);
        return settings;
    }

    public void clone(@NotNull PhpStanValidatorConfiguration settings) {
        settings.myPHPStanPath = this.myPHPStanPath;
        settings.myStandards = this.myStandards;
        settings.myMaxMessagesPerFile = this.myMaxMessagesPerFile;
        settings.myTimeoutMs = this.myTimeoutMs;
    }

    public int compareTo(@NotNull QualityToolConfiguration o) {
        if (!(o instanceof PhpStanValidatorConfiguration)) {
            return 1;
        } else if (StringUtil.equals(this.getPresentableName(null), "Local")) {
            return -1;
        } else {
            return StringUtil.equals(o.getPresentableName(null), "Local") ? 1 : StringUtil.compare(this.getPresentableName(null), o.getPresentableName(null), false);
        }
    }
}