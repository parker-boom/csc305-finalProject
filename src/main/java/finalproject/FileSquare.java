package finalproject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Square panel that represents one file in the grid.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 2.0
 */
public class FileSquare extends JPanel {

    private static final Color GREEN = new Color(0, 153, 0);
    private static final Color YELLOW = new Color(255, 204, 0);
    private static final Color RED = new Color(204, 0, 0);

    private final Blackboard blackboard = Blackboard.getInstance();
    private final GridFileData fileStats;
    private final Color displayColor;

    public FileSquare(GridFileData fileStats, int maxLineCount) {
        this.fileStats = fileStats;
        setPreferredSize(new Dimension(60, 60));
        setToolTipText(buildTooltip());
        setOpaque(false);

        displayColor = buildColor(fileStats, maxLineCount);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                blackboard.setSelectedFile(fileStats);
            }
        });

        blackboard.addSelectionListener(this::updateSelectionBorder);
        updateSelectionBorder();
    }

    private String buildTooltip() {
        return fileStats.getName() + " | Lines: " + fileStats.getLineCount()
                + " | Complexity: " + fileStats.getComplexity();
    }

    private Color buildColor(GridFileData stats, int maxLineCount) {
        Color base;
        if (stats.getComplexity() > 10) {
            base = RED;
        } else if (stats.getComplexity() > 5) {
            base = YELLOW;
        } else {
            base = GREEN;
        }
        int alpha;
        if (maxLineCount <= 0) {
            alpha = 0;
        } else if (stats.getLineCount() <= 0) {
            alpha = 0;
        } else {
            double ratio = (double) stats.getLineCount() / (double) maxLineCount;
            alpha = (int) Math.round(Math.min(1.0, Math.max(0.0, ratio)) * 255);
        }
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    // For hover
    private void updateSelectionBorder() {
        if (blackboard.getSelectedFile() == fileStats) {
            setBorder(BorderFactory.createStrokeBorder(new BasicStroke(3f), Color.BLACK));
        } else {
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(displayColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
