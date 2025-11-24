package finalproject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Central panel that lays out file squares.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 2.5
 */
public class FileGridTab extends JPanel {

    private final Blackboard blackboard;
    private final JPanel gridPanel;
    private final JLabel emptyLabel;

    public FileGridTab() {
        super(new BorderLayout());
        this.blackboard = Blackboard.getInstance();
        gridPanel = new JPanel(new GridLayout(0, 4, 8, 8));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        emptyLabel = new JLabel("No files loaded", SwingConstants.CENTER);

        add(scrollPane, BorderLayout.CENTER);
        add(emptyLabel, BorderLayout.NORTH);

        blackboard.addDataListener(this::refreshGrid);
        blackboard.addFilterListener(this::refreshGrid);
        refreshGrid();
    }

    private void refreshGrid() {
        gridPanel.removeAll();
        List<GridFileData> files = filterFiles(blackboard.getGridFiles(), blackboard.getFolderFilter());
        int maxLineCount = blackboard.getMaxLineCount();
        if (files.isEmpty()) {
            emptyLabel.setVisible(true);
        } else {
            emptyLabel.setVisible(false);
            for (GridFileData stats : files) {
                gridPanel.add(new FileSquare(stats, maxLineCount));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private List<GridFileData> filterFiles(List<GridFileData> files, String folderFilter) {
        if (folderFilter == null) {
            return files;
        }
        List<GridFileData> filtered = new ArrayList<>();
        String normalizedFilter = folderFilter.endsWith("/") ? folderFilter : folderFilter + "/";
        for (GridFileData stats : files) {
            String normalizedName = stats.getName().replace('\\', '/');
            if (normalizedName.startsWith(normalizedFilter)) {
                filtered.add(stats);
            }
        }
        return filtered;
    }
}
