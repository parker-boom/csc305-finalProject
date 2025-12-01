package finalproject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import io.github.cdimascio.dotenv.Dotenv;
import javiergs.tulip.GitHubHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ROLE: Controller.
 * Coordinates user actions (URL input, menu commands) and triggers GitHub fetch and analysis.
 * Wires SearchBar and MenuBar inputs to GitFetch/Blackboard updates and status messages.
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
        String token = dotenv.get("GH_ACCESS_TOKEN");
        if (token == null || token.isBlank()) {
            LOG.warn("GH_ACCESS_TOKEN missing; prompt user to configure .env");
            gitHubHandler = null;
            bottomBar.setStatusMessage("Add GH_ACCESS_TOKEN in src/main/java/finalproject/.env and retry.");
        } else {
            this.gitHubHandler = new GitHubHandler(token);
        }

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

    // Menu and button events
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
        if (gitHubHandler == null) {
            bottomBar.setStatusMessage("Missing GH_ACCESS_TOKEN; add it to .env then retry.");
            LOG.warn("Fetch aborted: no GH_ACCESS_TOKEN configured.");
            return;
        }
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
