package finalproject;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;

/**
 * Bottom panel that surfaces status messages during analysis.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 2.0
 */
public class BottomBar extends JPanel {
    public enum ViewMode {
        GRID,
        DIA,
        UML
    }

    private final Blackboard blackboard = Blackboard.getInstance();
    private final JLabel statusLabel;

    private ViewMode viewMode = ViewMode.GRID;
    private int fileCount;
    private double avgLines;
    private double avgComplexity;
    private double avgInstability;
    private double avgDistance;
    private int umlRelations;
    private String overrideMessage = "Please enter a URL above to start analysis";

    public BottomBar() {
        super(new BorderLayout());

        statusLabel = new JLabel(overrideMessage, SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 14f));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        statusPanel.add(statusLabel);
        statusPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10));
        add(statusPanel, BorderLayout.CENTER);

        blackboard.addDataListener(this::recomputeGridStats);
        blackboard.addMetricsListener(this::recomputeDiaStats);
        blackboard.addUmlListener(this::recomputeUmlStats);
    }

    public void setStatusMessage(String message) {
        overrideMessage = message;
        updateInfoLabel();
    }

    public void setView(ViewMode viewMode) {
        this.viewMode = viewMode;
        updateInfoLabel();
    }

    private void recomputeGridStats() {
        SwingUtilities.invokeLater(() -> {
            var files = blackboard.getGridFiles();
            fileCount = files.size();
            if (files.isEmpty()) {
                avgLines = 0.0;
                avgComplexity = 0.0;
            } else {
                avgLines = files.stream().mapToInt(GridFileData::getLineCount).average().orElse(0.0);
                avgComplexity = files.stream().mapToInt(GridFileData::getComplexity).average().orElse(0.0);
            }
            updateInfoLabel();
        });
    }

    private void recomputeDiaStats() {
        SwingUtilities.invokeLater(() -> {
            var metrics = blackboard.getDiaMetrics();
            if (metrics.isEmpty()) {
                avgInstability = 0.0;
                avgDistance = 0.0;
            } else {
                avgInstability = metrics.stream().mapToDouble(DiaMetricsData::getInstability).average().orElse(0.0);
                avgDistance = metrics.stream().mapToDouble(DiaMetricsData::getDistance).average().orElse(0.0);
            }
            updateInfoLabel();
        });
    }

    private void recomputeUmlStats() {
        SwingUtilities.invokeLater(() -> {
            UmlDiagramData uml = blackboard.getUmlDiagram();
            if (uml == null || uml.getPlantUmlText() == null) {
                umlRelations = 0;
            } else {
                umlRelations = (int) uml.getPlantUmlText().lines()
                        .filter(line -> line.contains("..>"))
                        .count();
            }
            updateInfoLabel();
        });
    }

    private void updateInfoLabel() {
        String text;
        if (overrideMessage != null) {
            text = overrideMessage;
        } else {
            switch (viewMode) {
                case GRID -> text = String.format("Avg lines: %.1f | Avg complexity: %.1f",
                        avgLines, avgComplexity);
                case DIA -> text = String.format("Avg instability: %.2f | Avg distance: %.2f",
                        avgInstability, avgDistance);
                case UML -> text = String.format("Classes: %d | Relations: %d", fileCount, umlRelations);
                default -> text = "";
            }
        }
        statusLabel.setText(text);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void clearOverride() {
        overrideMessage = null;
        updateInfoLabel();
    }
}
