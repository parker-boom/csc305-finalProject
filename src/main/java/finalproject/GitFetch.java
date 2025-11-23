package finalproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import javiergs.tulip.GitHubHandler;
import javiergs.tulip.URLHelper;

/**
 * Background worker that fetches and analyzes GitHub files.
 *
 * @version 3.0
 */
public class GitFetch extends SwingWorker<GitFetch.FetchResult, Void> {

    private static final Pattern COMPLEXITY_PATTERN = Pattern.compile("\\b(if|switch|for|while)\\b");
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("\\b([A-Z][A-Za-z0-9_]*)\\b");

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

    @Override
    protected FetchResult doInBackground() throws Exception {
        URLHelper helper = URLHelper.parseGitHubUrl(url);
        if (helper.isBlob) {
            throw new IllegalArgumentException("URL must point to a folder.");
        }
        List<String> paths = gitHubHandler.listFilesRecursive(url);
        List<FileStats> statsResults = new ArrayList<>();
        List<SourceFile> sourceFiles = new ArrayList<>();
        for (String path : paths) {
            if (!path.endsWith(".java")) {
                continue;
            }
            String content = gitHubHandler.getFileContent(helper.owner, helper.repo, path, helper.ref);
            statsResults.add(analyze(path, content));
            sourceFiles.add(new SourceFile(path, content));
        }
        List<FileMetrics> metrics = calculateMetrics(sourceFiles);
        return new FetchResult(statsResults, metrics);
    }

    @Override
    protected void done() {
        try {
            FetchResult result = get();
            blackboard.setFiles(result.fileStats);
            blackboard.setFileMetrics(result.fileMetrics);
            if (result.fileStats.isEmpty()) {
                bottomBar.setStatusMessage("No .java files found.");
            } else {
                bottomBar.setStatusMessage(result.fileStats.size() + " files analyzed.");
            }
        } catch (Exception ex) {
            bottomBar.setStatusMessage("Error: " + ex.getMessage());
            blackboard.clear();
        }
    }

    private FileStats analyze(String path, String content) {
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
        return new FileStats(path, lineCount, complexity);
    }

    private List<FileMetrics> calculateMetrics(List<SourceFile> files) {
        Map<String, SourceFile> byName = new HashMap<>();
        for (SourceFile file : files) {
            byName.put(file.className, file);
            file.isAbstract = detectAbstractness(file.content, file.className);
        }

        Set<String> repoClasses = new HashSet<>(byName.keySet());

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

        for (SourceFile file : files) {
            for (String target : file.outgoing) {
                SourceFile targetFile = byName.get(target);
                if (targetFile != null) {
                    targetFile.incomingCount++;
                }
            }
        }

        List<FileMetrics> metrics = new ArrayList<>();
        for (SourceFile file : files) {
            int outgoing = file.outgoing.size();
            int incoming = file.incomingCount;
            double abstractness = file.isAbstract ? 1.0 : 0.0;
            double denominator = incoming + outgoing;
            double instability = denominator == 0 ? 0.0 : (double) outgoing / denominator;
            double distance = Math.abs(abstractness + instability - 1.0);
            metrics.add(new FileMetrics(file.path, abstractness, instability, distance, incoming, outgoing));
        }
        return metrics;
    }

    private boolean detectAbstractness(String content, String className) {
        Pattern interfacePattern = Pattern.compile("\\binterface\\s+" + Pattern.quote(className) + "\\b");
        Pattern abstractClassPattern = Pattern.compile("\\babstract\\s+class\\s+" + Pattern.quote(className) + "\\b");
        return interfacePattern.matcher(content).find() || abstractClassPattern.matcher(content).find();
    }

    public static final class FetchResult {
        private final List<FileStats> fileStats;
        private final List<FileMetrics> fileMetrics;

        private FetchResult(List<FileStats> fileStats, List<FileMetrics> fileMetrics) {
            this.fileStats = fileStats;
            this.fileMetrics = fileMetrics;
        }
    }

    private static final class SourceFile {
        private final String path;
        private final String content;
        private final String className;
        private final Set<String> outgoing = new HashSet<>();
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
