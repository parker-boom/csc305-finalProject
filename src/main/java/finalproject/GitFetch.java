package finalproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import javiergs.tulip.GitHubHandler;
import javiergs.tulip.URLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background worker that fetches and analyzes GitHub files.
 *
 * @version 3.5
 */
public class GitFetch implements Runnable {

    private static final Pattern COMPLEXITY_PATTERN = Pattern.compile("\\b(if|switch|for|while)\\b");
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("\\b([A-Z][A-Za-z0-9_]*)\\b");
    private static final Logger LOG = LoggerFactory.getLogger(GitFetch.class);

    private final String url;
    private final GitHubHandler gitHubHandler;
    private final Blackboard blackboard;
    private final BottomBar bottomBar;

    public GitFetch(String url, GitHubHandler gitHubHandler, Blackboard blackboard, BottomBar bottomBar) {
        this.url = url;
        this.gitHubHandler = gitHubHandler;
        this.blackboard = blackboard;
        this.bottomBar = bottomBar;
    }

    public void start() {
        Thread worker = new Thread(this);
        worker.setName("GitFetchWorker");
        worker.start();
    }

    @Override
    public void run() {
        try {
            updateStatus("Fetching file list...");

            // Parse URL and ensure it's a folder
            URLHelper helper = URLHelper.parseGitHubUrl(url);
            if (helper.isBlob) {
                throw new IllegalArgumentException("URL must point to a folder.");
            }

            // Stage 1: list files
            List<String> paths = gitHubHandler.listFilesRecursive(url);
            updateStatus("Downloading sources...");
            LOG.info("Listed {} paths from {}", paths.size(), url);

            // Stage 2: download + build grid data and raw parse info
            List<GridFileData> gridFiles = new ArrayList<>();
            List<SourceFile> sourceFiles = new ArrayList<>();
            for (String path : paths) {
                if (!path.endsWith(".java")) {
                    continue;
                }
                String content = gitHubHandler.getFileContent(helper.owner, helper.repo, path, helper.ref);
                gridFiles.add(analyzeGridData(path, content));
                sourceFiles.add(new SourceFile(path, content));
            }
            LOG.info("Collected {} Java sources from {}", gridFiles.size(), url);

            // Stage 3: DIA metrics
            updateStatus("Calculating DIA metrics...");
            List<DiaMetricsData> metrics = buildDiaMetrics(sourceFiles);
            LOG.info("Calculated DIA metrics for {} files", metrics.size());

            // Stage 4: UML
            updateStatus("Building UML...");
            UmlDiagramData uml = buildUml(sourceFiles);
            LOG.info("Built UML diagram with {} relations", umlRelationsCount(uml));

            // Stage 5: publish to UI
            postSuccess(new FetchResult(gridFiles, metrics, uml));
        } catch (Exception ex) {
            postError(ex);
        }
    }

    private void postSuccess(FetchResult result) {
        SwingUtilities.invokeLater(() -> {
            blackboard.setGridFiles(result.gridFiles);
            blackboard.setDiaMetrics(result.diaMetrics);
            blackboard.setUmlDiagram(result.umlDiagram);
            if (result.gridFiles.isEmpty()) {
                bottomBar.setStatusMessage("No .java files found.");
            } else {
                bottomBar.setStatusMessage(buildSummary(result));
                bottomBar.clearOverride();
                LOG.info("Fetch completed: {} files, avg instability {:.2f}, avg distance {:.2f}",
                        result.gridFiles.size(),
                        result.diaMetrics.isEmpty() ? 0.0 : result.diaMetrics.stream().mapToDouble(DiaMetricsData::getInstability).average().orElse(0.0),
                        result.diaMetrics.isEmpty() ? 0.0 : result.diaMetrics.stream().mapToDouble(DiaMetricsData::getDistance).average().orElse(0.0));
            }
        });
    }

    private String buildSummary(FetchResult result) {
        if (result.diaMetrics.isEmpty()) {
            return result.gridFiles.size() + " files analyzed.";
        }
        double avgInstability = result.diaMetrics.stream()
                .mapToDouble(DiaMetricsData::getInstability)
                .average()
                .orElse(0.0);
        double avgDistance = result.diaMetrics.stream()
                .mapToDouble(DiaMetricsData::getDistance)
                .average()
                .orElse(0.0);
        return String.format("%d files analyzed | Avg Instability: %.2f | Avg Distance: %.2f",
                result.gridFiles.size(), avgInstability, avgDistance);
    }

    private void postError(Exception ex) {
        SwingUtilities.invokeLater(() -> {
            LOG.error("Fetch failed for URL: {}", url, ex);
            bottomBar.setStatusMessage("Error: " + ex.getMessage());
            blackboard.clear();
        });
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> bottomBar.setStatusMessage(message));
    }

    // Grid data: line count + simple complexity
    private GridFileData analyzeGridData(String path, String content) {
        String[] lines = content.split("\\R");
        int lineCount = 0;
        int complexity = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                lineCount++;
            }
            Matcher matcher = COMPLEXITY_PATTERN.matcher(line);
            while (matcher.find()) {
                complexity++;
            }
        }
        return new GridFileData(path, lineCount, complexity);
    }

    // DIA metrics using only repo classes
    private List<DiaMetricsData> buildDiaMetrics(List<SourceFile> files) {
        // Step 1: collect class map and abstract/interface flags
        Map<String, SourceFile> byName = new HashMap<>();
        for (SourceFile file : files) {
            byName.put(file.className, file);
            file.isInterface = detectInterface(file.content, file.className);
            file.isAbstract = detectAbstractClass(file.content, file.className);
        }

        Set<String> repoClasses = new HashSet<>(byName.keySet());

        // Step 2: find outgoing references to other repo classes
        for (SourceFile file : files) {
            Matcher matcher = CLASS_NAME_PATTERN.matcher(file.content);
            while (matcher.find()) {
                String candidate = matcher.group(1);
                if (candidate.equals(file.className) || !repoClasses.contains(candidate)) {
                    continue;
                }
                file.outgoing.add(candidate);
            }
        }

        // Step 3: count incoming references
        for (SourceFile file : files) {
            for (String target : file.outgoing) {
                SourceFile targetFile = byName.get(target);
                if (targetFile != null) {
                    targetFile.incomingCount++;
                }
            }
        }

        // Step 4: compute DIA metrics
        List<DiaMetricsData> metrics = new ArrayList<>();
        for (SourceFile file : files) {
            int outgoing = file.outgoing.size();
            int incoming = file.incomingCount;
            double abstractness = (file.isAbstract || file.isInterface) ? 1.0 : 0.0;
            double denominator = incoming + outgoing;
            double instability = denominator == 0 ? 0.0 : (double) outgoing / denominator;
            double distance = Math.abs(abstractness + instability - 1.0);
            metrics.add(new DiaMetricsData(file.path, abstractness, instability, distance, incoming, outgoing));
        }
        return metrics;
    }

    private UmlDiagramData buildUml(List<SourceFile> files) {
        // Declare classes/interfaces/abstracts
        Set<String> seenRelations = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        builder.append("@startuml\n");

        for (SourceFile file : files) {
            if (file.isInterface) {
                builder.append("interface ").append(file.className).append("\n");
            } else if (file.isAbstract) {
                builder.append("abstract class ").append(file.className).append("\n");
            } else {
                builder.append("class ").append(file.className).append("\n");
            }
        }

        // Add simple dependency arrows between repo classes
        for (SourceFile file : files) {
            for (String target : file.outgoing) {
                String key = file.className + "->" + target;
                if (seenRelations.add(key)) {
                    builder.append(file.className).append(" ..> ").append(target).append("\n");
                }
            }
        }

        builder.append("@enduml");
        return new UmlDiagramData(builder.toString());
    }

    private int umlRelationsCount(UmlDiagramData uml) {
        if (uml == null || uml.getPlantUmlText() == null) {
            return 0;
        }
        return (int) uml.getPlantUmlText().lines().filter(line -> line.contains("..>")).count();
    }

    private boolean detectInterface(String content, String className) {
        Pattern interfacePattern = Pattern.compile("\\binterface\\s+" + Pattern.quote(className) + "\\b");
        return interfacePattern.matcher(content).find();
    }

    private boolean detectAbstractClass(String content, String className) {
        Pattern abstractClassPattern = Pattern.compile("\\babstract\\s+class\\s+" + Pattern.quote(className) + "\\b");
        return abstractClassPattern.matcher(content).find();
    }

    public static final class FetchResult {
        private final List<GridFileData> gridFiles;
        private final List<DiaMetricsData> diaMetrics;
        private final UmlDiagramData umlDiagram;

        private FetchResult(List<GridFileData> gridFiles, List<DiaMetricsData> diaMetrics, UmlDiagramData umlDiagram) {
            this.gridFiles = gridFiles;
            this.diaMetrics = diaMetrics;
            this.umlDiagram = umlDiagram;
        }
    }

    private static final class SourceFile {
        private final String path;
        private final String content;
        private final String className;
        private final Set<String> outgoing = new HashSet<>();
        private boolean isInterface;
        private boolean isAbstract;
        private int incomingCount;

        private SourceFile(String path, String content) {
            this.path = path;
            this.content = content;
            this.className = extractClassName(path);
        }

        private static String extractClassName(String path) {
            int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
            String fileName = slash >= 0 ? path.substring(slash + 1) : path;
            int dot = fileName.lastIndexOf('.');
            return dot >= 0 ? fileName.substring(0, dot) : fileName;
        }
    }
}
