package ru.taptima.phalyfusion;

import com.intellij.codeInspection.ExternalAnnotatorInspectionVisitor;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PhalyfusionHighlightingInspection extends LocalInspectionTool implements ExternalAnnotatorBatchInspection {
    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public @NotNull String getShortName() {
        return "PhalyfusionHighlighting";
    }

    /**
     * Override the method to provide your own inspection visitor.
     * Created visitor must not be recursive (e.g. it must not inherit {@link PsiRecursiveElementVisitor})
     * since it will be fed with every element in the file anyway.
     * Visitor created must be thread-safe since it might be called on several elements concurrently.
     *
     * @param holder     where visitor will register problems found.
     * @param isOnTheFly true if inspection was run in non-batch mode
     * @return not-null visitor for this inspection.
     * @see PsiRecursiveVisitor
     */
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new ExternalAnnotatorInspectionVisitor(holder, PhalyfusionAnnotator.INSTANCE, isOnTheFly);
        //return super.buildVisitor(holder, isOnTheFly);
    }

    //    @Override
//    protected @NotNull QualityToolAnnotator getAnnotator() {
//        return PhalyfusionAnnotator.INSTANCE;
//    }
//
//    @Override
//    public String getToolName() {
//        return "Phalyfusion";
//    }
}
