package finalproject;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JLabel;
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
 * @version 2.5
 */
public class FileBrowserPanel extends JPanel {

    private final Blackboard blackboard;
    private final DefaultMutableTreeNode rootNode;
    private final DefaultTreeModel treeModel;
    private final JTree tree;
    private final JLabel headerLabel;

    public FileBrowserPanel() {
        super(new BorderLayout());
        this.blackboard = Blackboard.getInstance();

        rootNode = new DefaultMutableTreeNode();
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> handleTreeSelection());

        headerLabel = new JLabel("Files: 0");
        headerLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
        add(headerLabel, BorderLayout.NORTH);

        add(new JScrollPane(tree), BorderLayout.CENTER);

        blackboard.addDataListener(this::rebuildTree);
        rebuildTree();
    }

    private void rebuildTree() {
        SwingUtilities.invokeLater(() -> {
            rootNode.removeAllChildren();
            List<GridFileData> files = blackboard.getGridFiles();
            headerLabel.setText("Files: " + files.size());
            for (GridFileData stats : files) {
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
            GridFileData stats = findFileByPath(path);
            if (stats != null) {
                blackboard.setSelectedFile(stats);
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

    private GridFileData findFileByPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalizedPath = path.replace('\\', '/');
        for (GridFileData stats : blackboard.getGridFiles()) {
            String filePath = stats.getName().replace('\\', '/');
            if (filePath.equals(normalizedPath)) {
                return stats;
            }
        }
        return null;
    }

    private void addPath(GridFileData stats) {
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
