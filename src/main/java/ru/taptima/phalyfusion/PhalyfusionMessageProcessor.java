package ru.taptima.phalyfusion;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.tools.quality.QualityToolAnnotatorInfo;
import com.jetbrains.php.tools.quality.QualityToolMessage;
import com.jetbrains.php.tools.quality.QualityToolXmlMessageProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.Attributes;
import ru.taptima.phalyfusion.form.PhalyfusionConfigurable;

/**
 * All common tools all output as "checkstyle" format
 *
 * <?xml version="1.0" encoding="UTF-8"?>
 * <checkstyle>
 * <file name="src/Foo.php">
 *   <error line="8" column="1" severity="error" message="Undefined variable: $td" />
 *   <error line="26" column="1" severity="error" message="Undefined variable: $testsssss" />
 * </file>
 * </checkstyle>
 *
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhalyfusionMessageProcessor extends QualityToolXmlMessageProcessor {
    private final HighlightDisplayLevel myWarningsHighlightLevel;

    public PhalyfusionMessageProcessor(QualityToolAnnotatorInfo info) {
        super(info);
        // allow config?
        // this.myWarningsHighlightLevel = ((PhpCSValidationInspection)info.getInspection()).getWarningLevel();
        this.myWarningsHighlightLevel = HighlightDisplayLevel.WARNING;
    }

    protected XMLMessageHandler getXmlMessageHandler() {
        return new CheckstyleXmlMessageHandler();
    }

    public int getMessageStart(@NotNull String line) {
        int messageStart = line.indexOf("<error");
        if (messageStart < 0) {
            messageStart = line.indexOf("<warning");
        }

        return messageStart;
    }

    public int getMessageEnd(@NotNull String line) {
        return line.indexOf("/>");
    }

    @NotNull
    protected IntentionAction[] getQuickFix(XMLMessageHandler messageHandler) {
        return IntentionAction.EMPTY_ARRAY;
    }

    @Nullable
    protected String getMessagePrefix() {
        return null;
    }

    @Nullable
    protected HighlightDisplayLevel severityToDisplayLevel(@NotNull QualityToolMessage.Severity severity) {
        return QualityToolMessage.Severity.WARNING.equals(severity) ? this.myWarningsHighlightLevel : null;
    }

    @NotNull
    protected String getQuickFixFamilyName() {
        return "Phalyfusion";
    }

    @Override
    protected Configurable getToolConfigurable(@NotNull Project project) {
        return new PhalyfusionConfigurable(project);
    }

    public boolean processStdErrMessages() {
        return false;
    }

    /**
     * Convert to extract the attributes
     *
     * <error line="8" column="1" severity="error" message="Undefined variable: $td" />
     */
    private static class CheckstyleXmlMessageHandler extends XMLMessageHandler {
        private String message;

        protected void parseTag(@NotNull String tagName, @NotNull Attributes attributes) {
            if ("error".equals(tagName)) {
                this.mySeverity = QualityToolMessage.Severity.ERROR;
            } else if ("warning".equals(tagName)) {
                this.mySeverity = QualityToolMessage.Severity.WARNING;
            }

            this.myLineNumber = parseLineNumber(attributes.getValue("line"));
            this.message = attributes.getValue("message");
        }

        public String getMessageText() {
            return this.message;
        }

        public boolean isStatusValid() {
            return this.myLineNumber > -1;
        }
    }
}