package finalproject;

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.ByteArrayOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.sourceforge.plantuml.SourceStringReader;

/**
 * Renders the UML diagram using PlantUML text from the blackboard.
 *
 * @version 1.0
 */
public class DiagramTab extends JPanel {

    private final Blackboard blackboard = Blackboard.getInstance();
    private final JLabel imageLabel;

    public DiagramTab() {
        super(new BorderLayout());
        imageLabel = new JLabel("Run analysis to see UML diagram.", JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        blackboard.addUmlListener(this::refreshDiagram);
        refreshDiagram();
    }

    private void refreshDiagram() {
        SwingUtilities.invokeLater(() -> {
            UmlDiagramData uml = blackboard.getUmlDiagram();
            if (uml == null || uml.getPlantUmlText() == null || uml.getPlantUmlText().isBlank()) {
                imageLabel.setIcon(null);
                imageLabel.setText("Run analysis to see UML diagram.");
                return;
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                SourceStringReader reader = new SourceStringReader(uml.getPlantUmlText());
                reader.outputImage(out);
                ImageIcon rawIcon = new ImageIcon(out.toByteArray());
                Image scaled = rawIcon.getImage().getScaledInstance(imageLabel.getWidth(), -1, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setText(null);
            } catch (Exception ex) {
                imageLabel.setIcon(null);
                imageLabel.setText("Unable to render UML: " + ex.getMessage());
            }
        });
    }
}
