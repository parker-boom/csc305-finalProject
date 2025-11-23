package finalproject;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Application menu bar.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 3.0
 */
public class MenuBar extends JMenuBar {
    private final JMenuItem openFromUrlItem;
    private final JMenuItem exitItem;
    private final JMenuItem reloadItem;
    private final JMenuItem clearItem;
    private final JMenuItem aboutItem;

    public MenuBar() {
        JMenu fileMenu = new JMenu("File");
        openFromUrlItem = new JMenuItem("Open from URL...");
        exitItem = new JMenuItem("Exit");
        fileMenu.add(openFromUrlItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu actionMenu = new JMenu("Action");
        reloadItem = new JMenuItem("Reload");
        clearItem = new JMenuItem("Clear");
        actionMenu.add(reloadItem);
        actionMenu.add(clearItem);

        JMenu helpMenu = new JMenu("Help");
        aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        add(fileMenu);
        add(actionMenu);
        add(helpMenu);
    }

    public JMenuItem getOpenFromUrlItem() {
        return openFromUrlItem;
    }

    public JMenuItem getExitItem() {
        return exitItem;
    }

    public JMenuItem getReloadItem() {
        return reloadItem;
    }

    public JMenuItem getClearItem() {
        return clearItem;
    }

    public JMenuItem getAboutItem() {
        return aboutItem;
    }
}
