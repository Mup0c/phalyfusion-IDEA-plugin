package ru.taptima.phalyfusion;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.GlobalInspectionToolWrapper;
import com.intellij.codeInspection.reference.RefGraphAnnotator;
import com.intellij.codeInspection.reference.RefManager;
import com.intellij.execution.ExecutionException;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiCompiledElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.config.interpreters.*;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.run.remote.PhpRemoteInterpreterManager;
import com.jetbrains.php.tools.quality.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.taptima.phalyfusion.blacklist.PhalyfusionBlackList;
import ru.taptima.phalyfusion.configuration.PhalyfusionProjectConfiguration;
import ru.taptima.phalyfusion.issues.IssueCacheManager;

import java.util.*;

public class PhalyfusionGlobalInspection extends GlobalInspectionTool {
    private static final String GROUP_ID = "PHP External Quality Tools";
    private static final Logger LOG = Logger.getInstance(PhalyfusionGlobalInspection.class);

    private final PhalyfusionValidationInspection myValidationInspection = new PhalyfusionValidationInspection();

    @Override
    public @Nullable RefGraphAnnotator getAnnotator(@NotNull RefManager refManager) {
        return super.getAnnotator(refManager);
    }

    @Override
    public void runInspection(@NotNull AnalysisScope scope, @NotNull InspectionManager manager, @NotNull GlobalInspectionContext globalContext,
                              @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {
        Set<PsiFile> filesSet = new HashSet<>();

        try {
            scope.accept(new PsiElementVisitor() {
                @Override
                public void visitFile(@NotNull PsiFile file) {
                    if (!(file instanceof PsiCompiledElement)) {
                        final VirtualFile virtualFile = file.getVirtualFile();
                        if (virtualFile != null) {
                            filesSet.add(file);
                        }
                    }
                }
            });

            PhalyfusionBlackList blackList = PhalyfusionBlackList.getInstance(scope.getProject());

            PsiFile[] files = filesSet.stream().filter(psiFile ->
                    isFileSuitable(psiFile) && !blackList.containsFile(psiFile.getVirtualFile())).toArray(PsiFile[]::new);

            if (files.length == 0) {
                showInfo(getDisplayName(), "Phalyfusion did not run", "No files to analyse",
                        NotificationType.INFORMATION, null);
                return;
            }

            QualityToolAnnotatorInfo annotatorInfo = collectAnnotatorInfo(files[0]);
            if (annotatorInfo == null) {
                showInfo(getDisplayName(), "Phalyfusion did not run", "Can not get annotatorInfo", NotificationType.ERROR, null);
                return;
            }
            QualityToolMessageProcessor messageProcessor = new PhalyfusionMessageProcessor(annotatorInfo);
            String id = annotatorInfo.getInterpreterId();
            PhpSdkAdditionalData sdkData
                    = StringUtil.isEmpty(id) ? null : PhpInterpretersManagerImpl.getInstance(annotatorInfo.getProject()).findInterpreterDataById(id);
            PhpSdkFileTransfer transfer = PhpSdkFileTransfer.getSdkFileTransfer(sdkData);

            try {
                this.runTool(messageProcessor, annotatorInfo, transfer, files);
            } catch (ExecutionException e) {
                showInfo(getDisplayName(), "Can not execute quality tool", e.getMessage(), NotificationType.ERROR, annotatorInfo);
            } finally {
                try {
                    removeTempFile(annotatorInfo, transfer);
                } catch (ExecutionException e) {
                    logWarning("Can not remove temporary file", e.getMessage(), annotatorInfo);
                }
            }

            PsiManager psiManager = PsiManager.getInstance(scope.getProject());

            for (QualityToolMessage message : messageProcessor.getMessages()) {
                PhalyfusionMessage phalyfusionMessage = (PhalyfusionMessage)message;
                HighlightInfoType highlightInfoType = HighlightInfoType.ERROR;
                switch (phalyfusionMessage.getSeverity()) {
                    case WARNING:
                        highlightInfoType = HighlightInfoType.WARNING;
                        break;
                    case INTERNAL_ERROR:
                        showInfo(getDisplayName(), "Internal error", phalyfusionMessage.getMessageText(), NotificationType.ERROR, annotatorInfo);
                        continue;
                }

                HighlightInfo highlightInfo = HighlightInfo.newHighlightInfo(highlightInfoType).description(phalyfusionMessage.getMessageText())
                        .range(phalyfusionMessage.getTextRange()).create();

                GlobalInspectionUtil.createProblem(Objects.requireNonNull(psiManager.findFile(phalyfusionMessage.getFile())),
                        Objects.requireNonNull(highlightInfo), phalyfusionMessage.getTextRange(), () -> "Quality Tool Error",
                        InspectionManager.getInstance(annotatorInfo.getProject()), problemDescriptionsProcessor, globalContext);
            }
        } catch (Exception e) {
            showInfo(getDisplayName(), "Exception during Phalyfusion run", e.getMessage(), NotificationType.ERROR, null);
        }
    }

    private static void removeTempFile(@NotNull QualityToolAnnotatorInfo collectedInfo, @NotNull PhpSdkFileTransfer transfer) throws ExecutionException {
        String tempFile = collectedInfo.getFile();
        if (tempFile != null) {
            transfer.delete(collectedInfo.getProject(), collectedInfo.getTimeout() / 2, false);
        }
    }

    private void runTool(QualityToolMessageProcessor messageProcessor, QualityToolAnnotatorInfo annotatorInfo, PhpSdkFileTransfer transfer,
                         PsiFile[] files)
            throws ExecutionException {
        IssueCacheManager issuesCache = ServiceManager.getService(annotatorInfo.getProject(), IssueCacheManager.class);
        String[] filesPaths = Arrays.stream(files).map((PsiFile file) -> file.getVirtualFile().getPath()).toArray(String[]::new);
        List<String> params = getCommandLineOptions(filesPaths);
        String workingDir = QualityToolUtil.getWorkingDirectoryFromAnnotator(annotatorInfo);

        QualityToolProcessCreator.runToolProcess(annotatorInfo, null, messageProcessor, workingDir, transfer, params);
        if (messageProcessor.getInternalErrorMessage() != null) {
            if (annotatorInfo.isOnTheFly()) {
                String message = messageProcessor.getInternalErrorMessage().getMessageText();
                QualityToolAnnotator.showProcessErrorMessage(annotatorInfo, message);
            }

            messageProcessor.setFatalError();
        }

        issuesCache.setCachedResultsForFile(annotatorInfo.getOriginalFile(), messageProcessor.getMessages());
    }

    @Override
    public boolean isGraphNeeded() {
        return false;
    }

    @Override
    public @Nullable LocalInspectionTool getSharedLocalInspectionTool() {
        return myValidationInspection;
    }

    private List<String> getCommandLineOptions(String[] filePaths) {
        ArrayList<String> options = new ArrayList<>();
        options.add("analyse");
        options.add("--format=checkstyle");
        options.addAll(Arrays.asList(filePaths));

        return options;
    }

    @Nullable
    protected QualityToolConfiguration getConfiguration(@NotNull Project project) {
        try {
            return PhalyfusionProjectConfiguration.getInstance(project).findSelectedConfiguration(project);
        } catch (QualityToolValidationException e) {
            return null;
        }
    }

    public final QualityToolAnnotatorInfo collectAnnotatorInfo(@NotNull PsiFile file) {
        InspectionProfile inspectionProfile = InspectionProjectProfileManager.getInstance(file.getProject()).getCurrentProfile();
        GlobalInspectionToolWrapper globalInspectionToolWrapper
                = (GlobalInspectionToolWrapper)inspectionProfile.getInspectionTool(this.getShortName(), file);
        if (globalInspectionToolWrapper != null) {
            GlobalInspectionTool tool = globalInspectionToolWrapper.getTool();
            if (!this.isFileSuitable(file)) {
                return null;
            } else if (SuppressionUtil.inspectionResultSuppressed(file, tool)) {
                return null;
            } else {
                Project project = file.getProject();
                QualityToolConfiguration configuration = this.getConfiguration(project);
                if (configuration != null && !StringUtil.isEmpty(configuration.getToolPath())) {
                    if (StringUtil.isNotEmpty(configuration.getInterpreterId())) {
                        String interpreterId = configuration.getInterpreterId();
                        PhpSdkAdditionalData data = PhpInterpretersManagerImpl.getInstance(project).findInterpreterDataById(interpreterId);
                        PhpRemoteInterpreterManager manager = PhpRemoteInterpreterManager.getInstance();
                        if (manager != null && !manager.producesSshSdkCredentials(data)) {
                            return null;
                        }
                    }

                    return this.createAnnotatorInfo(file, myValidationInspection, project, configuration);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private boolean isFileSuitable(@NotNull PsiFile file) {
        return file instanceof PhpFile && file.getViewProvider().getBaseLanguage() == PhpLanguage.INSTANCE
                && file.getContext() == null;
    }

    @NotNull
    private QualityToolAnnotatorInfo createAnnotatorInfo(@NotNull PsiFile file, QualityToolValidationInspection tool,
                                                           Project project, QualityToolConfiguration configuration) {
        return new QualityToolAnnotatorInfo(file, tool, project, configuration, false);
    }

    private static String toPresentableLocation(@NotNull QualityToolAnnotatorInfo collectedInfo) {
        String interpreterId = collectedInfo.getInterpreterId();
        if (StringUtil.isNotEmpty(interpreterId)) {
            Project project = collectedInfo.getProject();
            if (!project.isDisposed()) {
                PhpInterpreter interpreter = PhpInterpretersManagerImpl.getInstance(project).findInterpreterById(interpreterId);
                if (interpreter != null) {
                    return interpreter.getPhpSdkAdditionalData().toPresentablePath();
                }
            }
        }

        return "local";
    }

    private static void logWarning(@NotNull String prefix, @NotNull String message, @Nullable QualityToolAnnotatorInfo collectedInfo) {
        if (collectedInfo == null) {
            LOG.warn(prefix + ": " + message);
            return;
        }
        String formattedPrefix = prefix + " for '" + collectedInfo.getOriginalFile().getPath() + "' on " + toPresentableLocation(collectedInfo);
        LOG.warn(formattedPrefix + ": " + message);
    }

    private static void logError(@NotNull String prefix, @NotNull String message, @Nullable QualityToolAnnotatorInfo collectedInfo) {
        if (collectedInfo == null) {
            LOG.error(prefix + ": " + message);
            return;
        }
        String formattedPrefix = prefix + " for '" + collectedInfo.getOriginalFile().getPath() + "' on " + toPresentableLocation(collectedInfo);
        LOG.error(formattedPrefix + ": " + message);
    }

    private static void showInfo(@NotNull String title, @NotNull String prefix, @NotNull String message,
                                 @NotNull NotificationType type, @Nullable QualityToolAnnotatorInfo annotatorInfo) {
        Notifications.Bus.notify(new Notification(GROUP_ID, title, prefix + ": " + message, type, null));
        switch (type) {
            case ERROR:
                logError(prefix, message, annotatorInfo);
            case WARNING:
                logWarning(prefix, message, annotatorInfo);
        }
    }
}
