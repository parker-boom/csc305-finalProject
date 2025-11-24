package finalproject;

/**
 * Data for the grid view: path, line count, and complexity.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 2.0
 */
public class GridFileData {
    private final String name;
    private final int lineCount;
    private final int complexity;

    public GridFileData(String name, int lineCount, int complexity) {
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
