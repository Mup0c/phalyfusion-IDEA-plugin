package ru.taptima.phalyfusion;

import com.google.common.io.CharStreams;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.tools.quality.QualityToolAnnotator;
import com.jetbrains.php.tools.quality.QualityToolAnnotatorInfo;
import com.jetbrains.php.tools.quality.QualityToolMessage;
import com.jetbrains.php.tools.quality.QualityToolXmlMessageProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.taptima.phalyfusion.form.PhalyfusionConfigurable;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

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
    private final Set<String> lineMessages = new HashSet<>();
    private int myPrevLine = -1;
    private VirtualFile myCurFile = null;
    private boolean isFileTag = false;

    private final LocalFileSystem myFileSystem;

    public PhalyfusionMessageProcessor(QualityToolAnnotatorInfo info) {
        super(info);
        // allow config?
        // this.myWarningsHighlightLevel = ((PhpCSValidationInspection)info.getInspection()).getWarningLevel();
        this.myWarningsHighlightLevel = HighlightDisplayLevel.WARNING;
        myFileSystem = LocalFileSystem.getInstance();
    }

    protected XMLMessageHandler getXmlMessageHandler() {
        return new CheckstyleXmlMessageHandler();
    }

    public int getMessageStart(@NotNull String line) {
        int messageStart = line.indexOf("<file");
        isFileTag = true;
        if (messageStart < 0) {
            messageStart = line.indexOf("<error");
            if (messageStart < 0) {
                messageStart = line.indexOf("<warning");
            }
            isFileTag = false;
        }

        return messageStart;
    }

    public int getMessageEnd(@NotNull String line)
    {
        if (isFileTag) {
            return line.indexOf(">");
        }
        return line.indexOf("/>");
    }

    public void loadFromCache(@NotNull Collection<QualityToolMessage> cachedMessages, QualityToolAnnotatorInfo annotatorInfo) {
        for (QualityToolMessage message : cachedMessages) {
            if (message instanceof PhalyfusionMessage) {
                PhalyfusionMessage phalyfusionMessage = (PhalyfusionMessage)message;
                if (phalyfusionMessage.getFile().equals(annotatorInfo.getOriginalFile())) {
                    addMessage(message);
                }
            }
        }
    }

    protected IntentionAction @NotNull [] getQuickFix(XMLMessageHandler messageHandler) {
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
        return true;
    }

    /**
     * Convert to extract the attributes
     *
     * <error line="8" column="1" severity="error" message="Undefined variable: $td" />
     */
    private static class CheckstyleXmlMessageHandler extends XMLMessageHandler {
        private String message;

        protected void parseTag(@NotNull String tagName, @NotNull Attributes attributes) {
            if ("file".equals(tagName)) {
                this.mySeverity = QualityToolMessage.Severity.WARNING;
                this.message = attributes.getValue("name");
                this.myLineNumber = -1;
                return;
            }

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
            return this.myLineNumber >= -1;
        }
    }

    @Override
    protected void processMessage(InputSource source) throws SAXException, IOException {
        QualityToolXmlMessageProcessor.XMLMessageHandler messageHandler = this.getXmlMessageHandler();

        if (isFileTag) {
            // Nasty way to process file tags. It seems like there is no api to process nested tags (or I didn't find it)
            String fixedFileString = CharStreams.toString(source.getCharacterStream()) + "</file>";
            this.mySAXParser.parse(new InputSource(new StringReader(fixedFileString)), messageHandler);
        } else {
            this.mySAXParser.parse(source, messageHandler);
        }

        if (messageHandler.isStatusValid()) {
            if (messageHandler.getLineNumber() == -1 && messageHandler.getSeverity() == QualityToolMessage.Severity.WARNING) {
                myCurFile = myFileSystem.findFileByPath(messageHandler.getMessageText());
                return;
            }

            PhalyfusionMessage qualityToolMessage = new PhalyfusionMessage(this, messageHandler.getLineNumber(),
                    messageHandler.getSeverity(), messageHandler.getMessageText(), myCurFile, this.getQuickFix(messageHandler));
            int currLine = qualityToolMessage.getLineNum();
            if (currLine != this.myPrevLine) {
                this.lineMessages.clear();
                this.myPrevLine = currLine;
            }

            String messageText = qualityToolMessage.getMessageText();
            if (this.lineMessages.add(messageText)) {
                this.addMessage(qualityToolMessage);
            }
        }
    }

    @Override
    protected void addMessage(QualityToolMessage message) {
        if (message.isInternalError()) {
            message = new QualityToolMessage(this, message.getLineNum(), QualityToolMessage.Severity.INTERNAL_ERROR,
                    message.getMessageText().substring(0, message.getMessageText().indexOf('\n')));
        }

        super.addMessage(message);
    }

    @Override
    public PsiFile getFile() {
        if (myCurFile == null) {
            return myFile;
        }

        return PsiManager.getInstance(myFile.getProject()).findFile(myCurFile);
    }
}