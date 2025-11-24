package finalproject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import io.github.cdimascio.dotenv.Dotenv;
import javiergs.tulip.GitHubHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates user actions and background processing.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 3.5
 */
public class Controller implements ActionListener {
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    private final SearchBar searchBar;
    private final BottomBar bottomBar;
    private final MenuBar menuBar;
    private final Blackboard blackboard;
    private final GitHubHandler gitHubHandler;

    private String lastUrl;

    public Controller(SearchBar searchBar, BottomBar bottomBar, MenuBar menuBar) {
        this.searchBar = searchBar;
        this.bottomBar = bottomBar;
        this.menuBar = menuBar;
        this.blackboard = Blackboard.getInstance();
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/java/finalproject")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        this.gitHubHandler = new GitHubHandler(dotenv.get("GH_ACCESS_TOKEN"));

        attachListeners();
    }

    // Wires UI controls to controller actions
    private void attachListeners() {
        searchBar.getOkButton().addActionListener(this);
        menuBar.getOpenFromUrlItem().addActionListener(this);
        menuBar.getReloadItem().addActionListener(this);
        menuBar.getClearItem().addActionListener(this);
        menuBar.getAboutItem().addActionListener(this);
        menuBar.getExitItem().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == searchBar.getOkButton()) {
            handleFetchAction();
        } else if (source == menuBar.getOpenFromUrlItem()) {
            promptForUrl();
        } else if (source == menuBar.getReloadItem()) {
            reloadLastUrl();
        } else if (source == menuBar.getClearItem()) {
            clearResults();
        } else if (source == menuBar.getAboutItem()) {
            showAbout();
        } else if (source == menuBar.getExitItem()) {
            System.exit(0);
        }
    }

    private void handleFetchAction() {
        fetchUrl(searchBar.getUrlText());
    }

    private void promptForUrl() {
        String url = JOptionPane.showInputDialog(searchBar, "Enter GitHub folder URL:", lastUrl == null ? "" : lastUrl);
        if (url != null) {
            url = url.trim();
            if (!url.isEmpty()) {
                searchBar.setUrlText(url);
                LOG.info("URL provided via menu prompt: {}", url);
                fetchUrl(url);
            }
        }
    }

    private void reloadLastUrl() {
        if (lastUrl == null || lastUrl.isBlank()) {
            bottomBar.setStatusMessage("Nothing to reload.");
            LOG.warn("Reload requested but no previous URL available.");
            return;
        }
        LOG.info("Reloading URL: {}", lastUrl);
        fetchUrl(lastUrl);
    }

    private void clearResults() {
        LOG.info("Clear requested.");
        blackboard.clear();
        bottomBar.setStatusMessage("Cleared.");
    }

    private void showAbout() {
        LOG.info("About dialog opened.");
        JOptionPane.showMessageDialog(searchBar, "Assignment 02\nGitHub folder visualizer.", "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void fetchUrl(String url) {
        if (url == null || url.isBlank() || url.contains(" ")) {
            bottomBar.setStatusMessage("Incorrect URL, please enter a GitHub URL in full");
            LOG.warn("Rejected URL input (blank or contains spaces): {}", url);
            return;
        }
        if (!url.toLowerCase().contains("github.com")) {
            bottomBar.setStatusMessage("Incorrect URL, please enter a GitHub URL in full");
            LOG.warn("Rejected non-GitHub URL: {}", url);
            return;
        }

        bottomBar.setStatusMessage("Fetching...");
        lastUrl = url;
        LOG.info("Starting fetch for URL: {}", url);
        new GitFetch(url, gitHubHandler, blackboard, bottomBar).start();
    }
}
