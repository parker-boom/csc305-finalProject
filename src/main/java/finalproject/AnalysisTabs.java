package finalproject;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Hosts the tabbed analysis views.
 *
 * @version 1.0
 */
public class AnalysisTabs extends JPanel {

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
            } else if (index == 1) {
                bottomBar.setView(BottomBar.ViewMode.DIA);
            } else if (index == 2) {
                bottomBar.setView(BottomBar.ViewMode.UML);
            }
        }
    }
}
