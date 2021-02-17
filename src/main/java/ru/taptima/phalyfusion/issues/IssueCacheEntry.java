package ru.taptima.phalyfusion.issues;

import com.jetbrains.php.tools.quality.QualityToolMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class IssueCacheEntry {
    public IssueCacheEntry(@NotNull Collection<QualityToolMessage> issues) {
        issuesCollection = issues;
        isValid = true;
    }

    private Collection<QualityToolMessage> issuesCollection;
    private boolean isValid;

    public void updateIssues(@NotNull Collection<QualityToolMessage> issues) {
        issuesCollection = issues;
        isValid = true;
    }

    public void markDirty() {
        isValid = false;
    }

    public boolean isDirty() {
        return !isValid;
    }

    @NotNull
    public Collection<QualityToolMessage> getIssues() {
        if (isValid) {
            return issuesCollection;
        }
        return Collections.emptyList();
    }
}
