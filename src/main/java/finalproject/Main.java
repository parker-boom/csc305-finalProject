package finalproject;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

/**
 * Entry point frame for the assignment UI.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 3.5
 */
public class Main extends JFrame {

    private final MenuBar menuBar;

    public Main() {
        setLayout(new BorderLayout());

        SearchBar searchBar = new SearchBar();
        BottomBar bottomBar = new BottomBar();
        FileBrowserPanel fileBrowserPanel = new FileBrowserPanel();
        AnalysisTabs analysisTabs = new AnalysisTabs(bottomBar);
        menuBar = new MenuBar();

        new Controller(searchBar, bottomBar, menuBar);

        add(searchBar, BorderLayout.NORTH);
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileBrowserPanel, analysisTabs);
        centerSplit.setResizeWeight(0.45);
        centerSplit.setContinuousLayout(true);
        centerSplit.setOneTouchExpandable(true);
        add(centerSplit, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        Main frame = new Main();

        frame.setTitle("CSC305 - Final Project");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(frame.menuBar);
        frame.setSize(1200, 800);
        frame.setVisible(true);
    }
}
