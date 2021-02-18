package ru.taptima.phalyfusion;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.tools.quality.QualityToolMessage;
import com.jetbrains.php.tools.quality.QualityToolMessageProcessor;

public class PhalyfusionMessage extends QualityToolMessage {
    private final VirtualFile myFile;

    public PhalyfusionMessage(QualityToolMessageProcessor messageProcessor, int lineNum, Severity severity,
                              String messageText, VirtualFile file, IntentionAction... quickFix) {
        super(messageProcessor, lineNum, severity, messageText, quickFix);
        myFile = file;
    }

    public VirtualFile getFile() {
        return myFile;
    }
}
