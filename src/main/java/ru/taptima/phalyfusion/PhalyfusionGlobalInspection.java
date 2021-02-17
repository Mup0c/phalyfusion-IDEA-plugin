package ru.taptima.phalyfusion;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.GlobalInspectionToolWrapper;
import com.intellij.codeInspection.reference.RefGraphAnnotator;
import com.intellij.codeInspection.reference.RefManager;
import com.intellij.execution.ExecutionException;
import com.intellij.lang.annotation.ProblemGroup;
import com.intellij.openapi.components.ServiceManager;
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
import java.util.function.Function;
import java.util.function.Predicate;


public class PhalyfusionGlobalInspection extends GlobalInspectionTool {
    private final PhalyfusionValidationInspection myValidationInspection = new PhalyfusionValidationInspection();
    private final QualityToolValidationInspection myQualityValidationInspection = new PhalyfusionValidationInspection();

    public PhalyfusionGlobalInspection() {
        super();
    }

    @Override
    public @Nullable RefGraphAnnotator getAnnotator(@NotNull RefManager refManager) {
        return super.getAnnotator(refManager);
    }

    @Override
    public void runInspection(@NotNull AnalysisScope scope, @NotNull InspectionManager manager,
                              @NotNull GlobalInspectionContext globalContext, @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {
        Set<VirtualFile> filesSet = new HashSet<>();

        //TODO: throw an exception if smth goes wrong

        scope.accept(new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                if (!(file instanceof PsiCompiledElement)) {
                    final VirtualFile virtualFile = file.getVirtualFile();
                    if (virtualFile != null) {
                        filesSet.add(virtualFile);
                    }
                }
            }
        });

        PsiManager psiManager = PsiManager.getInstance(scope.getProject());

        VirtualFile[] files = filesSet.stream().filter(new Predicate<VirtualFile>() {
            @Override
            public boolean test(VirtualFile virtualFile) {
                return isFileSuitable(psiManager.findFile(virtualFile));
            }
        }).toArray(VirtualFile[]::new);

        if (files.length == 0) {
            return;
        }

        PsiFile psiFile = psiManager.findFile(files[0]);

        QualityToolAnnotatorInfo annotatorInfo = collectAnnotatorInfo(psiFile);
        QualityToolMessageProcessor messageProcessor = new PhalyfusionMessageProcessor(annotatorInfo);

        String id = annotatorInfo.getInterpreterId();
        PhpSdkAdditionalData sdkData = StringUtil.isEmpty(id) ? null : PhpInterpretersManagerImpl.getInstance(annotatorInfo.getProject()).findInterpreterDataById(id);
        PhpSdkFileTransfer transfer = PhpSdkFileTransfer.getSdkFileTransfer(sdkData);

        try {
            this.runTool(messageProcessor, annotatorInfo, transfer, files);
        } catch (ExecutionException var17) {
            // TODO: logging
            //showProcessErrorMessage(annotatorInfo, var17.getMessage());
            //logWarning(collectedInfo, "Can not execute quality tool", var17);
        } finally {
            try {
                removeTempFile(annotatorInfo, transfer);
            } catch (ExecutionException var16) {
                // TODO: add logging
                //logWarning(collectedInfo, "Can not remove temporary file", var16);
            }
        }

        for (QualityToolMessage message : messageProcessor.getMessages()) {
            PhalyfusionMessage phalyfusionMessage = (PhalyfusionMessage)message;

            HighlightInfoType highlightInfoType = HighlightInfoType.ERROR;
            switch (phalyfusionMessage.getSeverity()) {
                case WARNING:
                    highlightInfoType = HighlightInfoType.WARNING;
                    break;
                case INTERNAL_ERROR:
                    continue;
            }

            // TODO: change ProblemGroup
            GlobalInspectionUtil.createProblem(phalyfusionMessage.getFile(),
                    HighlightInfo.newHighlightInfo(highlightInfoType).description(phalyfusionMessage.getMessageText()).create(),
                    phalyfusionMessage.getTextRange(), new ProblemGroup() {
                @Override
                public @Nullable String getProblemName() {
                    return "Quality Tool Error";
                }
            }, InspectionManager.getInstance(annotatorInfo.getProject()), problemDescriptionsProcessor, globalContext);
        }
    }

    private static void removeTempFile(@NotNull QualityToolAnnotatorInfo collectedInfo, @NotNull PhpSdkFileTransfer transfer) throws ExecutionException {
        String tempFile = collectedInfo.getFile();
        if (tempFile != null) {
            transfer.delete(collectedInfo.getProject(), collectedInfo.getTimeout() / 2, false);
        }
    }

    private void runTool(QualityToolMessageProcessor messageProcessor, QualityToolAnnotatorInfo annotatorInfo, PhpSdkFileTransfer transfer,
                         VirtualFile[] files)
            throws ExecutionException {
        IssueCacheManager issuesCache = ServiceManager.getService(annotatorInfo.getProject(), IssueCacheManager.class);
        String[] filesPaths = Arrays.stream(files).map(new Function<VirtualFile, String>() {
            @Override
            public String apply(VirtualFile virtualFile) {
                return virtualFile.getPath();
            }
        }).toArray(String[]::new);
        List<String> params = getCommandLineOptions(filesPaths);
        String workingDir = QualityToolUtil.getWorkingDirectoryFromAnnotator(annotatorInfo);
        PhalyfusionBlackList blackList = PhalyfusionBlackList.getInstance(annotatorInfo.getProject());

        // TODO: Check for blacklist
        QualityToolProcessCreator.runToolProcess(annotatorInfo, null, messageProcessor, workingDir, transfer, params);
        if (messageProcessor.getInternalErrorMessage() != null) {
            if (annotatorInfo.isOnTheFly()) {
                String message = messageProcessor.getInternalErrorMessage().getMessageText();
                // TODO: make this work
                // showProcessErrorMessage(annotatorInfo, blackList, message);
            }

            messageProcessor.setFatalError();
        }

        issuesCache.setCachedResultsForFile(annotatorInfo.getOriginalFile(), messageProcessor.getMessages());
    }

//TODO: make this work

//    public static void showProcessErrorMessage(@NotNull final QualityToolAnnotatorInfo collectedInfo, @NotNull String messageText) {
//        if (collectedInfo.isOnTheFly() && !collectedInfo.getProject().isDisposed()) {
//            final QualityToolValidationInspection inspection = collectedInfo.getInspection();
//            String text = messageText.replace("\n", "<br>") + (StringUtil.endsWith(messageText, "</a>") ? " or " : "<br>") + "<a href='disable'>Disable inspection</a>";
//            NotificationListener listener = new NotificationListener() {
//                public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
//                    String description = event.getDescription();
//                    Project project = collectedInfo.getProject();
//                    if (!project.isDisposed()) {
//                        if ("disable".equals(description)) {
//                            InspectionProfileManager.getInstance(project).getCurrentProfile().setToolEnabled(inspection.getID(), false);
//                            this.showInfoNotification(inspection.getDisplayName() + " was disabled.");
//                        } else if ("update".equals(description)) {
//                            PhpInterpreter interpreter = PhpInterpretersManagerImpl.getInstance(project).findInterpreterById(collectedInfo.getInterpreterId());
//                            if (interpreter == null) {
//                                this.showWarningNotification(PhpBundle.message("quality.tool.configuration.interpreter.is.undefined", new Object[]{inspection.getToolName()}));
//                                return;
//                            }
//
//                            try {
//                                PhpSdkHelpersManager.getHelpersManager(interpreter.getPhpSdkAdditionalData()).update(project, (JComponent)null);
//                                this.showInfoNotification(PhpBundle.message("quality.tool.configuration.interpreter.was.reloaded", new Object[0]));
//                            } catch (Exception var7) {
//                                this.showWarningNotification(var7.getMessage());
//                                QualityToolAnnotator.LOG.warn(var7);
//                            }
//                        }
//                    }
//
//                }
//
//                private void showWarningNotification(@NotNull String warning) {
//                    Notifications.Bus.notify(new Notification("PHP External Quality Tools", inspection.getToolName(), warning, NotificationType.WARNING));
//                }
//
//                private void showInfoNotification(@NotNull String info) {
//                    Notifications.Bus.notify(new Notification("PHP External Quality Tools", inspection.getToolName(), info, NotificationType.INFORMATION));
//                }
//            };
//            showProcessErrorMessage(inspection, text, listener);
//        } else {
//            LOG.warn(messageText);
//        }
//    }

    @Override
    public boolean isGraphNeeded() {
        return false;
    }

    @Override
    public boolean isEnabledByDefault() {
        return super.isEnabledByDefault();
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
        // not sure if shortName would work
        // TODO: check it
        String id = this.getShortName();
        GlobalInspectionToolWrapper globalInspectionToolWrapper = (GlobalInspectionToolWrapper)inspectionProfile.getInspectionTool(id, file);
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

                    return this.createAnnotatorInfo(file, myQualityValidationInspection, project, configuration);
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    protected boolean isFileSuitable(@NotNull PsiFile file) {
        return file instanceof PhpFile && file.getViewProvider().getBaseLanguage() == PhpLanguage.INSTANCE && file.getContext() == null;
    }

    @NotNull
    protected QualityToolAnnotatorInfo createAnnotatorInfo(@NotNull PsiFile file, QualityToolValidationInspection tool, Project project, QualityToolConfiguration configuration) {
        return new QualityToolAnnotatorInfo(file, tool, project, configuration, false);
    }
}
