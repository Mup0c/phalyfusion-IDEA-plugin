package ru.taptima.phalyfusion;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.tools.quality.QualityToolMessage;
import com.jetbrains.php.tools.quality.QualityToolMessageProcessor;

public class PhalyfusionMessage extends QualityToolMessage {
    private final PsiFile myFile;

    public PhalyfusionMessage(QualityToolMessageProcessor messageProcessor, int lineNum, QualityToolMessage.Severity severity,
                              String messageText, PsiFile file, IntentionAction... quickFix) {
        super(messageProcessor, lineNum, severity, messageText, quickFix);
        myFile = file;
    }

    public PsiFile getFile() {
        return myFile;
    }
}
