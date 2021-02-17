package ru.taptima.phalyfusion.issues;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;
import com.jetbrains.php.tools.quality.QualityToolMessage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class IssueCacheManager {
    public IssueCacheManager(@NotNull Project project) {
        cachedIssueMap = new HashMap<>();

        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                synchronized (IssueCacheManager.this) {
                    for (VFileEvent event : events) {
                        VirtualFile file = event.getFile();
                        if (cachedIssueMap.containsKey(file)) {
                            cachedIssueMap.get(file).markDirty();
                        }
                    }
                }
            }
        });

    }

    private final Map<VirtualFile, IssueCacheEntry> cachedIssueMap;

    @NotNull
    public synchronized Collection<QualityToolMessage> getCachedResultForFile(@NotNull VirtualFile file) {
        if (cachedIssueMap.containsKey(file)) {
            return cachedIssueMap.get(file).getIssues();
        }

        return Collections.emptyList();
    }

    public synchronized void setCachedResultsForFile(@NotNull VirtualFile file, @NotNull Collection<QualityToolMessage> issues) {
        if (cachedIssueMap.containsKey(file)) {
            cachedIssueMap.get(file).updateIssues(issues);
            return;
        }

        cachedIssueMap.put(file, new IssueCacheEntry(issues));
    }
}
