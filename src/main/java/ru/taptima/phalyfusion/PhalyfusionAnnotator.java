package ru.taptima.phalyfusion;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.composer.ComposerConfigUtils;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.config.interpreters.PhpSdkFileTransfer;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.tools.quality.*;
import ru.taptima.phalyfusion.blacklist.PhalyfusionBlackList;
import ru.taptima.phalyfusion.configuration.PhalyfusionConfiguration;
import ru.taptima.phalyfusion.configuration.PhalyfusionProjectConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.taptima.phalyfusion.issues.IssueCacheManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhalyfusionAnnotator extends QualityToolAnnotator {
    public static final PhalyfusionAnnotator INSTANCE = new PhalyfusionAnnotator();
    private final PhalyfusionValidationInspection myInspection = new PhalyfusionValidationInspection();

    @NotNull
    @Override
    protected String getTemporaryFilesFolder() {
        return "phalyfusion_temp.tmp";
    }

    @NotNull
    @Override
    protected String getInspectionId() {
        return myInspection.getID();
    }

    @Override
    protected QualityToolMessageProcessor createMessageProcessor(@NotNull QualityToolAnnotatorInfo qualityToolAnnotatorInfo) {
        return new PhalyfusionMessageProcessor(qualityToolAnnotatorInfo);
    }

    public static void launchQualityTool(@NotNull PsiFile[] files, @NotNull QualityToolAnnotatorInfo annotatorInfo, @NotNull QualityToolMessageProcessor messageProcessor,
                                         @NotNull PhpSdkFileTransfer transfer) throws ExecutionException {
        PhalyfusionBlackList blackList = PhalyfusionBlackList.getInstance(annotatorInfo.getProject());
        String[] filesPaths = Arrays.stream(files).filter(psiFile -> isFileSuitable(psiFile, blackList))
                .map(psiFile -> psiFile.getVirtualFile().getPath()).toArray(String[]::new);

        if (filesPaths.length == 0) {
            return;
        }

        List<String> params = getCommandLineOptions(filesPaths);
        String workingDir = QualityToolUtil.getWorkingDirectoryFromAnnotator(annotatorInfo);

        try {
            synchronized (FILE_LOCK) {
                checkNeonConfiguration(annotatorInfo.getProject(), annotatorInfo);
            }
        } catch (IOException e) {
            logWarning(annotatorInfo, "Failed to create phalyfusion configuration file", e);
        }


        QualityToolProcessCreator.runToolProcess(annotatorInfo, blackList, messageProcessor, workingDir, transfer, params);

        if (messageProcessor.getInternalErrorMessage() != null) {
            if (annotatorInfo.isOnTheFly()) {
                String message = messageProcessor.getInternalErrorMessage().getMessageText();
                showProcessErrorMessage(annotatorInfo, message);
            }

            messageProcessor.setFatalError();
        }
    }

    private static class CodeAnalyzer {
        public CodeAnalyzer(String name, String fullName) {
            this.name = name;
            this.fullName = fullName;
        }

        private final String name;
        private final String fullName;

        public String getName() {
            return name;
        }

        public String getFullName() {
            return fullName;
        }
    }

    private static final ArrayList<CodeAnalyzer> CODE_ANALYZERS = new ArrayList<>(Arrays.asList(
            new CodeAnalyzer("phpstan", "phpstan/phpstan"),
            new CodeAnalyzer("phan", "phan/phan"),
            new CodeAnalyzer("psalm", "vimeo/psalm"),
            new CodeAnalyzer("phpmd", "phpmd/phpmd"),
            new CodeAnalyzer("php-cs-fixer", "friendsofphp/php-cs-fixer")));

    private final static Object FILE_LOCK = new Object();

    private static void checkNeonConfiguration(@NotNull Project project, @NotNull QualityToolAnnotatorInfo annotatorInfo) throws IOException {
        File neonConfig = new File(project.getBasePath() + "/" + "phalyfusion.neon");
        if (neonConfig.isFile()) {
            return;
        }

        var config= ComposerDataService.getInstance(project).getConfigFile();
        if (config == null) {
            return;
        }

        var vendors = ComposerConfigUtils.getVendorAndBinDirs(config);
        if (vendors == null) {
            return;
        }

        var packagesList = ComposerConfigUtils.getInstalledPackagesFromConfig(config);
        List<Pair<CodeAnalyzer, String>> existingAnalysers = new ArrayList<>();
        for (var pkg : packagesList) {
            var res = CODE_ANALYZERS.stream().filter((it) -> it.fullName.equals(pkg.getName())).findFirst();

            if (res.isEmpty()) {
                continue;
            }

            var analyserPath = project.getBasePath() + "/" + vendors.second + "/" + res.get().name;

            if (!new File(analyserPath).isFile()) {
                logWarning(annotatorInfo, "Can not find " + res.get().name + " package bin", null);
                continue;
            }

            existingAnalysers.add(new Pair<>(res.get(), analyserPath));
        }

        createNeonConfiguration(neonConfig, existingAnalysers);
    }

    private static void createNeonConfiguration(@NotNull File neonFile, @NotNull List<Pair<CodeAnalyzer, String>> analyzers) throws IOException {
        try (var writer = new BufferedWriter(new FileWriter(neonFile))) {
            writer.write("plugins:\n");
            String indent = "    ";
            writer.write(indent + "usePlugins:\n");
            for (var analyser : analyzers) {
                writer.write(indent + indent + "- " + analyser.first.name + "\n");
            }

            writer.write(indent + "runCommands:\n");

            for (var analyser : analyzers) {
                writer.write(indent + indent + analyser.first.name + ": \"" + analyser.second + "\"\n");
            }
        }
    }

    private static boolean isFileSuitable(@NotNull PsiFile file, @NotNull PhalyfusionBlackList blackList) {
        return file instanceof PhpFile && file.getViewProvider().getBaseLanguage() == PhpLanguage.INSTANCE
                && file.getContext() == null && !blackList.containsFile(file.getVirtualFile())
                && file.getVirtualFile().isValid();
    }

    private static List<String> getCommandLineOptions(String[] filePaths) {
        ArrayList<String> options = new ArrayList<>();
        options.add("analyse");
        options.add("--format=checkstyle");
        options.addAll(Arrays.asList(filePaths));
        return options;
    }

    protected void runTool(@NotNull QualityToolMessageProcessor messageProcessor, @NotNull QualityToolAnnotatorInfo annotatorInfo,
                           @NotNull PhpSdkFileTransfer transfer) throws ExecutionException {
        // This inspection is only for on-fly mode. Batch inspections are provided with PhalyfusionGlobal
        if (!annotatorInfo.isOnTheFly()) {
            return;
        }

        PhalyfusionConfiguration configuration = (PhalyfusionConfiguration) getConfiguration(annotatorInfo.getProject(), myInspection);
        IssueCacheManager issuesCache = ServiceManager.getService(annotatorInfo.getProject(), IssueCacheManager.class);

        if (configuration == null || !configuration.getOnFlyMode()) {
            if (messageProcessor instanceof PhalyfusionMessageProcessor) {
                ((PhalyfusionMessageProcessor)messageProcessor)
                        .loadFromCache(issuesCache.getCachedResultForFile(annotatorInfo.getOriginalFile()), annotatorInfo);
            }
            return;
        }

        launchQualityTool(new PsiFile[] { annotatorInfo.getPsiFile() }, annotatorInfo, messageProcessor, transfer);
        issuesCache.setCachedResultsForFile(annotatorInfo.getOriginalFile(), messageProcessor.getMessages());
    }

    @Nullable
    protected QualityToolConfiguration getConfiguration(@NotNull Project project, @NotNull LocalInspectionTool inspection) {
        try {
            return PhalyfusionProjectConfiguration.getInstance(project).findSelectedConfiguration(project);
        } catch (QualityToolValidationException e) {
            return null;
        }
    }
}
