package finalproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Shared state between the controller and the UI panels.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 3.0
 */
public class Blackboard {
    private static final Blackboard INSTANCE = new Blackboard();

    private final List<Runnable> dataListeners = new ArrayList<>();
    private final List<Runnable> selectionListeners = new ArrayList<>();
    private final List<Runnable> metricsListeners = new ArrayList<>();
    private final List<FileStats> files = new ArrayList<>();
    private final List<FileMetrics> metrics = new ArrayList<>();
    private final List<Runnable> filterListeners = new ArrayList<>();
    private FileStats selected;
    private int maxLineCount;
    private String folderFilter;

    public static Blackboard getInstance() {
        return INSTANCE;
    }

    private Blackboard() {
    }

    public void setFiles(List<FileStats> newFiles) {
        files.clear();
        files.addAll(newFiles);
        maxLineCount = files.stream().mapToInt(FileStats::getLineCount).max().orElse(0);
        if (!files.contains(selected)) {
            selected = null;
        }
        setFolderFilter(null);
        notifyDataListeners();
        notifySelectionListeners();
    }

    public void clear() {
        files.clear();
        metrics.clear();
        selected = null;
        maxLineCount = 0;
        setFolderFilter(null);
        notifyDataListeners();
        notifyMetricsListeners();
        notifySelectionListeners();
    }

    public void setSelected(FileStats fileStats) {
        if (fileStats == selected) {
            return;
        }
        selected = fileStats;
        notifySelectionListeners();
    }

    public List<FileStats> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void setFileMetrics(List<FileMetrics> newMetrics) {
        metrics.clear();
        metrics.addAll(newMetrics);
        notifyMetricsListeners();
    }

    public List<FileMetrics> getFileMetrics() {
        return Collections.unmodifiableList(metrics);
    }

    public FileStats getSelected() {
        return selected;
    }

    public int getMaxLineCount() {
        return maxLineCount;
    }

    public void addDataListener(Runnable listener) {
        dataListeners.add(listener);
    }

    public void addSelectionListener(Runnable listener) {
        selectionListeners.add(listener);
    }

    public void addMetricsListener(Runnable listener) {
        metricsListeners.add(listener);
    }

    public void addFilterListener(Runnable listener) {
        filterListeners.add(listener);
    }

    public void setFolderFilter(String folderFilter) {
        String normalized = normalizeFolder(folderFilter);
        if (Objects.equals(this.folderFilter, normalized)) {
            return;
        }
        this.folderFilter = normalized;
        notifyFilterListeners();
    }

    public String getFolderFilter() {
        return folderFilter;
    }

    private String normalizeFolder(String folderFilter) {
        if (folderFilter == null) {
            return null;
        }
        String trimmed = folderFilter.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replace('\\', '/');
    }

    private void notifyDataListeners() {
        for (Runnable listener : dataListeners) {
            listener.run();
        }
    }

    private void notifySelectionListeners() {
        for (Runnable listener : selectionListeners) {
            listener.run();
        }
    }

    private void notifyMetricsListeners() {
        for (Runnable listener : metricsListeners) {
            listener.run();
        }
    }

    private void notifyFilterListeners() {
        for (Runnable listener : filterListeners) {
            listener.run();
        }
    }
}
