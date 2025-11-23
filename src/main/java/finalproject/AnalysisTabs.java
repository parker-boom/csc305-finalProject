package finalproject;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Hosts the tabbed analysis views.
 *
 * @version 1.0
 */
public class AnalysisTabs extends JPanel {

    private final FileGridTab gridTab;

    public AnalysisTabs() {
        super(new BorderLayout());
        gridTab = new FileGridTab();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Grid", gridTab);
        tabbedPane.addTab("Metrics", new MetricsTab());
        tabbedPane.addTab("Diagram", new DiagramTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public FileGridTab getGridTab() {
        return gridTab;
    }
}
