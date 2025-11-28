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
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\bclass\\s+%s\\s+extends\\s+([A-Z][A-Za-z0-9_]*)");
    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\bclass\\s+%s\\s+implements\\s+([A-Za-z0-9_,\\s]+)");
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
            List<UmlBuilder.SourceFile> sourceFiles = new ArrayList<>();
            for (String path : paths) {
                if (!path.endsWith(".java")) {
                    continue;
                }
                String content = gitHubHandler.getFileContent(helper.owner, helper.repo, path, helper.ref);
                gridFiles.add(analyzeGridData(path, content));
                sourceFiles.add(new UmlBuilder.SourceFile(path, content));
            }
            LOG.info("Collected {} Java sources from {}", gridFiles.size(), url);

            // Stage 3: DIA metrics
            updateStatus("Calculating DIA metrics...");
            List<DiaMetricsData> metrics = buildDiaMetrics(sourceFiles);
            LOG.info("Calculated DIA metrics for {} files", metrics.size());

            // Stage 4: UML
            updateStatus("Building UML...");
            UmlDiagramData uml = new UmlBuilder().build(sourceFiles);
            LOG.info("Built UML diagram with {} relations", umlRelationsCount(uml));

            // Stage 5: publish to UI
            publishResults(gridFiles, metrics, uml);
        } catch (Exception ex) {
            postError(ex);
        }
    }

    private void publishResults(List<GridFileData> gridFiles, List<DiaMetricsData> diaMetrics, UmlDiagramData umlDiagram) {
        SwingUtilities.invokeLater(() -> {
            blackboard.setGridFiles(gridFiles);
            blackboard.setDiaMetrics(diaMetrics);
            blackboard.setUmlDiagram(umlDiagram);
            if (gridFiles.isEmpty()) {
                bottomBar.setStatusMessage("No .java files found.");
            } else {
                bottomBar.setStatusMessage(buildSummary(gridFiles, diaMetrics));
                bottomBar.clearOverride();
                LOG.info("Fetch completed: {} files, avg instability {:.2f}, avg distance {:.2f}",
                        gridFiles.size(),
                        diaMetrics.isEmpty() ? 0.0 : diaMetrics.stream().mapToDouble(DiaMetricsData::getInstability).average().orElse(0.0),
                        diaMetrics.isEmpty() ? 0.0 : diaMetrics.stream().mapToDouble(DiaMetricsData::getDistance).average().orElse(0.0));
            }
        });
    }

    private String buildSummary(List<GridFileData> gridFiles, List<DiaMetricsData> diaMetrics) {
        if (diaMetrics.isEmpty()) {
            return gridFiles.size() + " files analyzed.";
        }
        double avgInstability = diaMetrics.stream()
                .mapToDouble(DiaMetricsData::getInstability)
                .average()
                .orElse(0.0);
        double avgDistance = diaMetrics.stream()
                .mapToDouble(DiaMetricsData::getDistance)
                .average()
                .orElse(0.0);
        return String.format("%d files analyzed | Avg Instability: %.2f | Avg Distance: %.2f",
                gridFiles.size(), avgInstability, avgDistance);
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
    private List<DiaMetricsData> buildDiaMetrics(List<UmlBuilder.SourceFile> files) {
        // Step 1: collect class map and abstract/interface flags
        Map<String, UmlBuilder.SourceFile> byName = new HashMap<>();
        for (UmlBuilder.SourceFile file : files) {
            byName.put(file.className, file);
            file.isInterface = detectInterface(file.content, file.className);
            file.isAbstract = detectAbstractClass(file.content, file.className);
        }

        Set<String> repoClasses = new HashSet<>(byName.keySet());

        // capture extends/implements (only within repo)
        for (UmlBuilder.SourceFile file : files) {
            file.parentClass = resolveExtends(file, repoClasses);
            file.implementedInterfaces = resolveImplements(file, repoClasses);
        }

        // Step 2: collect associations (rough scan for composition/aggregation/dependency)
        analyzeRelations(files, repoClasses);

        // Step 3: count incoming references (all outgoing kinds)
        for (UmlBuilder.SourceFile file : files) {
            for (String target : file.allOutgoing()) {
                UmlBuilder.SourceFile targetFile = byName.get(target);
                if (targetFile != null) {
                    targetFile.incomingCount++;
                }
            }
        }

        // Step 4: compute DIA metrics
        List<DiaMetricsData> metrics = new ArrayList<>();
        for (UmlBuilder.SourceFile file : files) {
            int outgoing = file.allOutgoing().size();
            int incoming = file.incomingCount;
            double abstractness = (file.isAbstract || file.isInterface) ? 1.0 : 0.0;
            double denominator = incoming + outgoing;
            double instability = denominator == 0 ? 0.0 : (double) outgoing / denominator;
            double distance = Math.abs(abstractness + instability - 1.0);
            metrics.add(new DiaMetricsData(file.path, abstractness, instability, distance, incoming, outgoing));
        }
        return metrics;
    }

    private int umlRelationsCount(UmlDiagramData uml) {
        if (uml == null || uml.getPlantUmlText() == null) {
            return 0;
        }
        return (int) uml.getPlantUmlText().lines()
                .filter(line -> line.contains("..>") || line.contains("-->") || line.contains("*--") || line.contains("o--") || line.contains("..|>") || line.contains("--|>"))
                .count();
    }

    private boolean detectInterface(String content, String className) {
        Pattern interfacePattern = Pattern.compile("\\binterface\\s+" + Pattern.quote(className) + "\\b");
        return interfacePattern.matcher(content).find();
    }

    private boolean detectAbstractClass(String content, String className) {
        Pattern abstractClassPattern = Pattern.compile("\\babstract\\s+class\\s+" + Pattern.quote(className) + "\\b");
        return abstractClassPattern.matcher(content).find();
    }

    private void analyzeRelations(List<UmlBuilder.SourceFile> files, Set<String> repoClasses) {
        for (UmlBuilder.SourceFile file : files) {
            String[] lines = file.content.split("\\R");
            for (String line : lines) {
                String trimmed = line.trim();
                Matcher matcher = CLASS_NAME_PATTERN.matcher(line);
                while (matcher.find()) {
                    String candidate = matcher.group(1);
                    if (candidate.equals(file.className) || !repoClasses.contains(candidate)) {
                        continue;
                    }
                    boolean hasNew = line.contains("new " + candidate);
                    boolean looksField = trimmed.matches(".*\\b" + candidate + "\\s+\\w+\\s*(=|;).*");
                    boolean looksParam = trimmed.matches(".*\\b" + candidate + "\\s+\\w*\\(.*") || trimmed.contains("(" + candidate);
                    if (hasNew) {
                        file.compositions.add(candidate);
                    } else if (looksField) {
                        file.aggregations.add(candidate);
                    } else if (looksParam) {
                        file.dependencies.add(candidate); // treat parameters as dashed dependency
                    } else {
                        file.associations.add(candidate); // default to solid association
                    }
                }
            }
        }
    }

    private String resolveExtends(UmlBuilder.SourceFile file, Set<String> repoClasses) {
        Pattern extendsPattern = Pattern.compile(String.format(EXTENDS_PATTERN.pattern(), Pattern.quote(file.className)));
        Matcher matcher = extendsPattern.matcher(file.content);
        if (matcher.find()) {
            String parent = matcher.group(1);
            if (repoClasses.contains(parent)) {
                return parent;
            }
        }
        return null;
    }

    private Set<String> resolveImplements(UmlBuilder.SourceFile file, Set<String> repoClasses) {
        Set<String> interfaces = new HashSet<>();
        Pattern implementsPattern = Pattern.compile(String.format(IMPLEMENTS_PATTERN.pattern(), Pattern.quote(file.className)));
        Matcher matcher = implementsPattern.matcher(file.content);
        if (matcher.find()) {
            String group = matcher.group(1);
            String[] parts = group.split(",");
            for (String part : parts) {
                String name = part.trim();
                if (repoClasses.contains(name)) {
                    interfaces.add(name);
                }
            }
        }
        return interfaces;
    }

}
