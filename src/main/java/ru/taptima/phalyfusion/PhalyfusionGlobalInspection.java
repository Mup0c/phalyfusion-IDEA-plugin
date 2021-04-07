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
import com.intellij.openapi.util.SystemInfo;
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
import java.util.stream.Collectors;

public class PhalyfusionGlobalInspection extends GlobalInspectionTool {
    private static final int MAX_WINDOWS_CMD_LENGTH = 8192;

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
                        if (virtualFile != null && virtualFile.isValid()) {
                            filesSet.add(file);
                        }
                    }
                }
            });

            PhalyfusionBlackList blackList = PhalyfusionBlackList.getInstance(scope.getProject());

            PsiFile[] psiFiles = filesSet.stream().filter(psiFile -> isFileSuitable(psiFile, blackList))
                    .toArray(PsiFile[]::new);

            if (psiFiles.length == 0) {
                showInfo(getDisplayName(), "Phalyfusion did not run", "No files to analyse",
                        NotificationType.INFORMATION, null);
                return;
            }

            QualityToolAnnotatorInfo annotatorInfo = collectAnnotatorInfo(psiFiles[0]);
            if (annotatorInfo == null) {
                showInfo(getDisplayName(), "Phalyfusion did not run", "Can not get annotatorInfo", NotificationType.ERROR, null);
                return;
            }

            QualityToolMessageProcessor messageProcessor = new PhalyfusionMessageProcessor(annotatorInfo);
            PhpSdkFileTransfer transfer = getPhpSdkFileTransfer(annotatorInfo);

            splitRunTool(psiFiles, annotatorInfo, messageProcessor, transfer);

            processMessages(globalContext, annotatorInfo, messageProcessor, psiFiles, problemDescriptionsProcessor);
        } catch (Exception e) {
            showInfo(getDisplayName(), "Exception during Phalyfusion run", e.getMessage(), NotificationType.ERROR, null);
        }
    }

    private void processMessages(@NotNull GlobalInspectionContext globalContext, @NotNull QualityToolAnnotatorInfo annotatorInfo,
                                 @NotNull QualityToolMessageProcessor messageProcessor, @NotNull PsiFile[] psiFiles,
                                 @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {
        PsiManager psiManager = PsiManager.getInstance(globalContext.getProject());

        Set<VirtualFile> initialFileSet = Arrays.stream(psiFiles).map(PsiFile::getVirtualFile).collect(Collectors.toSet());

        IssueCacheManager issuesCache = ServiceManager.getService(annotatorInfo.getProject(), IssueCacheManager.class);

        var messageMap = new HashMap<VirtualFile, List<QualityToolMessage>>();

        for (QualityToolMessage message : messageProcessor.getMessages()) {
            HighlightInfoType highlightInfoType = HighlightInfoType.WARNING;
            if (message.getSeverity() == QualityToolMessage.Severity.INTERNAL_ERROR) {
                showInfo(getDisplayName(), "Internal error", message.getMessageText(), NotificationType.ERROR, annotatorInfo);
                continue;
            }

            PhalyfusionMessage phalyfusionMessage = (PhalyfusionMessage)message;
            if (!initialFileSet.contains(phalyfusionMessage.getFile())) {
                continue;
            }

            var psiFile = psiManager.findFile(phalyfusionMessage.getFile());
            if (psiFile == null) {
                logError("No PSI file", phalyfusionMessage.getFile().getPath(), null);
            }

            if (!messageMap.containsKey(phalyfusionMessage.getFile())) {
                messageMap.put(phalyfusionMessage.getFile(), new ArrayList<>());
            }

            messageMap.get(phalyfusionMessage.getFile()).add(phalyfusionMessage);

            HighlightInfo highlightInfo = HighlightInfo.newHighlightInfo(highlightInfoType).description(phalyfusionMessage.getMessageText())
                    .range(phalyfusionMessage.getTextRange()).create();

            GlobalInspectionUtil.createProblem(Objects.requireNonNull(psiManager.findFile(phalyfusionMessage.getFile())),
                    Objects.requireNonNull(highlightInfo), phalyfusionMessage.getTextRange(), () -> "Quality Tool Error",
                    InspectionManager.getInstance(annotatorInfo.getProject()), problemDescriptionsProcessor, globalContext);
        }

        issuesCache.setCachedResults(messageMap);
    }

    private void splitRunTool(@NotNull PsiFile[] psiFiles, @NotNull QualityToolAnnotatorInfo annotatorInfo,
                              @NotNull QualityToolMessageProcessor messageProcessor, @NotNull PhpSdkFileTransfer transfer) {
        if (!SystemInfo.isWindows) {
            tryRunTool(annotatorInfo, messageProcessor, transfer, psiFiles);
            return;
        }

        int curFileIdx = 0;
        while (curFileIdx < psiFiles.length) {
            var curFilesList = new ArrayList<PsiFile>();
            int totalLen = 0;
            while (curFileIdx < psiFiles.length
                    && totalLen + psiFiles[curFileIdx].getVirtualFile().getPath().length() < MAX_WINDOWS_CMD_LENGTH - 300) {
                curFilesList.add(psiFiles[curFileIdx]);
                totalLen += psiFiles[curFileIdx].getVirtualFile().getPath().length();
                curFileIdx++;
            }

            if (!tryRunTool(annotatorInfo, messageProcessor, transfer, curFilesList.toArray(PsiFile[]::new))) {
                break;
            }
        }
    }

    private boolean tryRunTool(@NotNull QualityToolAnnotatorInfo annotatorInfo, @NotNull QualityToolMessageProcessor messageProcessor,
                               @NotNull PhpSdkFileTransfer transfer, @NotNull PsiFile[] files) {
        boolean isExecutionException = true;
        try {
            this.runTool(messageProcessor, annotatorInfo, transfer, files);
        } catch (ExecutionException e) {
            isExecutionException = false;
            showInfo(getDisplayName(), "Can not execute quality tool", e.getMessage(), NotificationType.ERROR, annotatorInfo);
        } finally {
            try {
                removeTempFile(annotatorInfo, transfer);
            } catch (ExecutionException e) {
                isExecutionException = false;
                logWarning("Can not remove temporary file", e.getMessage(), annotatorInfo);
            }
        }
        return isExecutionException;
    }

    private PhpSdkFileTransfer getPhpSdkFileTransfer(@NotNull QualityToolAnnotatorInfo annotatorInfo) {
        String id = annotatorInfo.getInterpreterId();
        PhpSdkAdditionalData sdkData
                = StringUtil.isEmpty(id) ? null : PhpInterpretersManagerImpl.getInstance(annotatorInfo.getProject()).findInterpreterDataById(id);
        return PhpSdkFileTransfer.getSdkFileTransfer(sdkData);
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

        String[] filesPaths = Arrays.stream(files)
                .map(psiFile -> psiFile.getVirtualFile().getPath()).toArray(String[]::new);
        List<String> params = getCommandLineOptions(filesPaths);
        String workingDir = QualityToolUtil.getWorkingDirectoryFromAnnotator(annotatorInfo);
        QualityToolProcessCreator.runToolProcess(annotatorInfo, null, messageProcessor, workingDir, transfer, params);
        if (messageProcessor.getInternalErrorMessage() != null) {
            messageProcessor.setFatalError();
        }
    }

    @Override
    public boolean isGraphNeeded() {
        return false;
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
            if (SuppressionUtil.inspectionResultSuppressed(file, tool)) {
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

    private boolean isFileSuitable(@NotNull PsiFile file, @NotNull PhalyfusionBlackList blackList) {
        return file instanceof PhpFile && file.getViewProvider().getBaseLanguage() == PhpLanguage.INSTANCE
                && file.getContext() == null && !blackList.containsFile(file.getVirtualFile())
                && file.getVirtualFile().isValid();
    }

    @NotNull
    private QualityToolAnnotatorInfo createAnnotatorInfo(@NotNull PsiFile file, QualityToolValidationInspection tool,
                                                           Project project, QualityToolConfiguration configuration) {
        return new QualityToolAnnotatorInfo(file, tool, project, configuration, false);
    }

    @Override
    public @Nullable LocalInspectionTool getSharedLocalInspectionTool() {
        return myValidationInspection;
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
