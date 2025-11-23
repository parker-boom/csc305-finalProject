package finalproject;

/**
 * Metrics for a single source file.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 2.0
 */
public class FileStats {
    private final String name;
    private final int lineCount;
    private final int complexity;

    public FileStats(String name, int lineCount, int complexity) {
        this.name = name;
        this.lineCount = lineCount;
        this.complexity = complexity;
    }

    public String getName() {
        return name;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getComplexity() {
        return complexity;
    }
}
