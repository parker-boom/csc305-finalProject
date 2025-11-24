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
 * @version 3.5
 */
public class Blackboard {

    private static final Blackboard INSTANCE = new Blackboard();

    // Grid / File data
    private final List<Runnable> dataListeners = new ArrayList<>();
    private final List<Runnable> selectionListeners = new ArrayList<>();
    private final List<Runnable> filterListeners = new ArrayList<>();
    private final List<GridFileData> gridFiles = new ArrayList<>();
    private GridFileData selectedFile;
    private int maxLineCount;
    private String folderFilter;

    // DIA metrics
    private final List<Runnable> metricsListeners = new ArrayList<>();
    private final List<DiaMetricsData> diaMetrics = new ArrayList<>();

    // UML diagram
    private final List<Runnable> umlListeners = new ArrayList<>();
    private UmlDiagramData umlDiagram;

    public static Blackboard getInstance() {
        return INSTANCE;
    }

    private Blackboard() {
    }

    // Grid / File data
    public void setGridFiles(List<GridFileData> newFiles) {
        gridFiles.clear();
        gridFiles.addAll(newFiles);
        maxLineCount = gridFiles.stream().mapToInt(GridFileData::getLineCount).max().orElse(0);
        if (!gridFiles.contains(selectedFile)) {
            selectedFile = null;
        }
        setFolderFilter(null);
        notifyDataListeners();
        notifySelectionListeners();
    }

    public List<GridFileData> getGridFiles() {
        return Collections.unmodifiableList(gridFiles);
    }

    public void setSelectedFile(GridFileData file) {
        if (file == selectedFile) {
            return;
        }
        selectedFile = file;
        notifySelectionListeners();
    }

    public GridFileData getSelectedFile() {
        return selectedFile;
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

    // DIA metrics
    public void setDiaMetrics(List<DiaMetricsData> newMetrics) {
        diaMetrics.clear();
        diaMetrics.addAll(newMetrics);
        notifyMetricsListeners();
    }

    public List<DiaMetricsData> getDiaMetrics() {
        return Collections.unmodifiableList(diaMetrics);
    }

    public void addMetricsListener(Runnable listener) {
        metricsListeners.add(listener);
    }

    // UML diagram
    public void setUmlDiagram(UmlDiagramData umlDiagram) {
        this.umlDiagram = umlDiagram;
        notifyUmlListeners();
    }

    public UmlDiagramData getUmlDiagram() {
        return umlDiagram;
    }

    public void addUmlListener(Runnable listener) {
        umlListeners.add(listener);
    }

    // Clear/reset (for all data sources)
    public void clear() {
        gridFiles.clear();
        diaMetrics.clear();
        selectedFile = null;
        maxLineCount = 0;
        setFolderFilter(null);
        notifyDataListeners();
        notifyMetricsListeners();
        notifySelectionListeners();
        setUmlDiagram(null);
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

    private void notifyUmlListeners() {
        for (Runnable listener : umlListeners) {
            listener.run();
        }
    }
}
