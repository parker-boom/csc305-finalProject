package finalproject;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hosts the tabbed analysis views.
 *
 * @version 1.5
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
        tabbedPane.addChangeListener(new TabChangeHandler(tabbedPane));
        bottomBar.setView(BottomBar.ViewMode.GRID);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public FileGridTab getGridTab() {
        return gridTab;
    }

    private class TabChangeHandler implements ChangeListener {
        private final JTabbedPane tabbedPane;

        private TabChangeHandler(JTabbedPane tabbedPane) {
            this.tabbedPane = tabbedPane;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            int index = tabbedPane.getSelectedIndex();
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
}
