package ru.taptima.phalyfusion;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.PathUtil;
import com.jetbrains.php.config.interpreters.PhpSdkFileTransfer;
import com.jetbrains.php.tools.quality.*;
import ru.taptima.phalyfusion.blacklist.PhalyfusionBlackList;
import ru.taptima.phalyfusion.configuration.PhalyfusionProjectConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.taptima.phalyfusion.issues.IssueCacheManager;

import java.util.ArrayList;
import java.util.List;

public class PhalyfusionAnnotator extends QualityToolAnnotator {
    public static final PhalyfusionAnnotator INSTANCE = new PhalyfusionAnnotator();

    @NotNull
    @Override
    protected String getTemporaryFilesFolder() {
        return "phalyfusion_temp.tmp";
    }

    @NotNull
    @Override
    protected String getInspectionId() {
        return (new PhalyfusionValidationInspection()).getID();
    }

    @Override
    protected QualityToolMessageProcessor createMessageProcessor(@NotNull QualityToolAnnotatorInfo qualityToolAnnotatorInfo) {
        return new PhalyfusionMessageProcessor(qualityToolAnnotatorInfo);
    }

    protected void runTool(@NotNull QualityToolMessageProcessor messageProcessor, @NotNull QualityToolAnnotatorInfo annotatorInfo,
                           @NotNull PhpSdkFileTransfer transfer) throws ExecutionException {
        List<String> params = getCommandLineOptions(PathUtil.toSystemIndependentName(annotatorInfo.getOriginalFile().getPath()));
        IssueCacheManager issuesCache = ServiceManager.getService(annotatorInfo.getProject(), IssueCacheManager.class);
        if (annotatorInfo.isOnTheFly()) {
            if (messageProcessor instanceof PhalyfusionMessageProcessor) {
                ((PhalyfusionMessageProcessor)messageProcessor)
                        .loadFromCache(issuesCache.getCachedResultForFile(annotatorInfo.getOriginalFile()), annotatorInfo);
            }
            return;
        }
        PhalyfusionBlackList blackList = PhalyfusionBlackList.getInstance(annotatorInfo.getProject());

        String workingDir = QualityToolUtil.getWorkingDirectoryFromAnnotator(annotatorInfo);
        QualityToolProcessCreator.runToolProcess(annotatorInfo, blackList, messageProcessor, workingDir, transfer, params);
        if (messageProcessor.getInternalErrorMessage() != null) {
            if (annotatorInfo.isOnTheFly()) {
                String message = messageProcessor.getInternalErrorMessage().getMessageText();
                showProcessErrorMessage(annotatorInfo, blackList, message);
            }

            messageProcessor.setFatalError();
        }

        issuesCache.setCachedResultsForFile(annotatorInfo.getOriginalFile(), messageProcessor.getMessages());
    }

    private List<String> getCommandLineOptions(String filePath) {
        ArrayList<String> options = new ArrayList<>();

        options.add("analyse");
        options.add("--format=checkstyle");
        options.add(filePath);

        return options;
    }

    @Nullable
    protected QualityToolConfiguration getConfiguration(@NotNull Project project, @NotNull LocalInspectionTool inspection) {
        try {
            return PhalyfusionProjectConfiguration.getInstance(project).findSelectedConfiguration(project);
        } catch (QualityToolValidationException e) {
            return null;
        }
    }

//    @Override
//    public String getPairedBatchInspectionShortName() {
//        return "PhalyfusionHighlighting";
//    }
}
