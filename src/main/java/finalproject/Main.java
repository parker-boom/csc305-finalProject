package finalproject;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * Entry point panel for the assignment UI.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 3.0
 */
public class Main extends JPanel {

    private final MenuBar menuBar;

    public Main() {
        super(new BorderLayout());

        SearchBar searchBar = new SearchBar();
        BottomBar bottomBar = new BottomBar();
        FileBrowserPanel fileBrowserPanel = new FileBrowserPanel();
        AnalysisTabs analysisTabs = new AnalysisTabs(bottomBar);
        menuBar = new MenuBar();

        new Controller(searchBar, bottomBar, menuBar);

        add(searchBar, BorderLayout.NORTH);
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileBrowserPanel, analysisTabs);
        centerSplit.setResizeWeight(0.45); // give browser a bit more width
        centerSplit.setContinuousLayout(true);
        centerSplit.setOneTouchExpandable(true);
        add(centerSplit, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Assignment 03");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Main mainPanel = new Main();

        frame.setContentPane(mainPanel);
        frame.setJMenuBar(mainPanel.getMenuBar());
        frame.setSize((int) (600 * 2.0), (int) (400 * 2.0)); // doubled size
        frame.setVisible(true);
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }
}
