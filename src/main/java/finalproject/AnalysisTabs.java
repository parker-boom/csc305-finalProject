package finalproject;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ROLE: View.
 * Container for the grid, metrics, and diagram tabs, switching the BottomBar view mode.
 * Instantiated in Main and connects each tab to shared Blackboard data.
 *
 * @version 1.5
 * @author Parker Jones
 * @author Ashley Aring
 */
public class AnalysisTabs extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisTabs.class);

    private final FileGridTab gridTab;
    private final BottomBar bottomBar;

    public AnalysisTabs(BottomBar bottomBar) {
        super(new BorderLayout());
        this.bottomBar = bottomBar;
        gridTab = new FileGridTab();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Grid", gridTab);
        tabbedPane.addTab("Metrics", new MetricsTab());
        tabbedPane.addTab("Diagram", new DiagramTab());
        tabbedPane.addChangeListener(e -> handleTabChange(tabbedPane.getSelectedIndex()));
        bottomBar.setView(BottomBar.ViewMode.GRID);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public FileGridTab getGridTab() {
        return gridTab;
    }

    private void handleTabChange(int index) {
        if (index == 0) {
            bottomBar.setView(BottomBar.ViewMode.GRID);
            LOG.info("Switched to Grid tab");
        } else if (index == 1) {
            bottomBar.setView(BottomBar.ViewMode.DIA);
            LOG.info("Switched to Metrics tab");
        } else if (index == 2) {
            bottomBar.setView(BottomBar.ViewMode.UML);
            LOG.info("Switched to Diagram tab");
        }
    }
}
