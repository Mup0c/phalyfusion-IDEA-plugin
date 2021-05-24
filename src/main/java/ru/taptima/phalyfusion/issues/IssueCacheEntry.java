package ru.taptima.phalyfusion.issues;

import com.jetbrains.php.tools.quality.QualityToolMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IssueCacheEntry {
    public IssueCacheEntry(@NotNull Collection<QualityToolMessage> issues) {
        issuesCollection = new ArrayList<>(issues);
        isValid = true;
    }

    private List<QualityToolMessage> issuesCollection;
    private boolean isValid;

    public void updateIssues(@NotNull Collection<QualityToolMessage> issues) {
        issuesCollection = issues.stream().filter(message -> !message.isInternalError() ).collect(Collectors.toList());
        isValid = true;
    }

    public void markDirty() {
        isValid = false;
    }

    public boolean isDirty() {
        return !isValid;
    }

    @NotNull
    public List<QualityToolMessage> getIssues() {
        if (isValid) {
            return issuesCollection;
        }
        return Collections.emptyList();
    }
}
