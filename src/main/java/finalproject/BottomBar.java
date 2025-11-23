package finalproject;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Bottom panel that shows the selected file and status messages.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 2.0
 */
public class BottomBar extends JPanel {
    private final Blackboard blackboard;
    private final JTextField selectedField;
    private final JLabel statusLabel;

    public BottomBar() {
        super(new BorderLayout());
        this.blackboard = Blackboard.getInstance();

        JPanel selectedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        selectedPanel.add(new JLabel("Selected File Name:"));
        selectedField = new JTextField(30);
        selectedField.setEditable(false);
        selectedPanel.add(selectedField);

        statusLabel = new JLabel();

        add(selectedPanel, BorderLayout.WEST);
        add(statusLabel, BorderLayout.CENTER);

        blackboard.addSelectionListener(this::updateSelection);
        updateSelection();
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    private void updateSelection() {
        FileStats stats = blackboard.getSelected();
        if (stats == null) {
            selectedField.setText("");
        } else {
            selectedField.setText(stats.getName());
        }
    }
}
