package finalproject;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Left-side panel that displays the fetched files in a tree structure.
 *
 * @version 2.0
 */
public class FileBrowserPanel extends JPanel {

    private final Blackboard blackboard;
    private final DefaultMutableTreeNode rootNode;
    private final DefaultTreeModel treeModel;
    private final JTree tree;

    public FileBrowserPanel() {
        super(new BorderLayout());
        this.blackboard = Blackboard.getInstance();

        rootNode = new DefaultMutableTreeNode("Files");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> handleTreeSelection());

        add(new JScrollPane(tree), BorderLayout.CENTER);

        blackboard.addDataListener(this::rebuildTree);
        rebuildTree();
    }

    private void rebuildTree() {
        SwingUtilities.invokeLater(() -> {
            rootNode.removeAllChildren();
            List<FileStats> files = blackboard.getFiles();
            for (FileStats stats : files) {
                addPath(stats);
            }
            treeModel.reload();
            expandRoot();
        });
    }

    private void handleTreeSelection() {
        TreePath selection = tree.getSelectionPath();
        if (selection == null) {
            blackboard.setFolderFilter(null);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selection.getLastPathComponent();
        if (node == rootNode) {
            blackboard.setFolderFilter(null);
            return;
        }
        String path = buildPath(selection);
        if (node.getChildCount() > 0) {
            blackboard.setFolderFilter(path);
        } else {
            blackboard.setFolderFilter(null);
            FileStats stats = findFileByPath(path);
            if (stats != null) {
                blackboard.setSelected(stats);
            }
        }
    }

    private String buildPath(TreePath treePath) {
        Object[] nodes = treePath.getPath();
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < nodes.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes[i];
            String part = String.valueOf(node.getUserObject());
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(part);
        }
        return builder.toString();
    }

    private FileStats findFileByPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalizedPath = path.replace('\\', '/');
        for (FileStats stats : blackboard.getFiles()) {
            String filePath = stats.getName().replace('\\', '/');
            if (filePath.equals(normalizedPath)) {
                return stats;
            }
        }
        return null;
    }

    private void addPath(FileStats stats) {
        DefaultMutableTreeNode parent = rootNode;
        String[] parts = stats.getName().split("[/\\\\]");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isBlank()) {
                continue;
            }
            DefaultMutableTreeNode child = findChild(parent, part);
            if (child == null) {
                child = new DefaultMutableTreeNode(part);
                parent.add(child);
            }
            parent = child;
        }
    }

    private DefaultMutableTreeNode findChild(DefaultMutableTreeNode parent, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            Object userObject = child.getUserObject();
            if (name.equals(String.valueOf(userObject))) {
                return child;
            }
        }
        return null;
    }

    private void expandRoot() {
        SwingUtilities.invokeLater(() -> {
            tree.expandPath(new TreePath(rootNode.getPath()));
        });
    }

}
