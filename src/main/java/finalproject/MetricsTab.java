package finalproject;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

/**
 * Displays the DIA chart for the analyzed files.
 *
 * @version 1.0
 */
public class MetricsTab extends JPanel {

    private static final Color BACKGROUND = new Color(0xC9D9D6);
    private static final Color AXIS_COLOR = new Color(0x7A8E8C);
    private static final Color POINT_COLOR = new Color(0x2F2F2F);

    private final Blackboard blackboard;
    private List<DiaMetricsData> metrics = Collections.emptyList();

    public MetricsTab() {
        blackboard = Blackboard.getInstance();
        setOpaque(true);
        blackboard.addMetricsListener(this::handleMetricsUpdate);
        handleMetricsUpdate();
    }

    private void handleMetricsUpdate() {
        metrics = new ArrayList<>(blackboard.getDiaMetrics());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        g2.setColor(BACKGROUND);
        g2.fillRect(0, 0, width, height);

        int padding = 40;
        int plotWidth = Math.max(10, width - padding * 2);
        int plotHeight = Math.max(10, height - padding * 2);

        drawAxes(g2, width, height, padding);
        drawDiagonal(g2, width, height, padding);
        drawLabels(g2, width, height, padding);

        if (metrics.isEmpty()) {
            drawEmptyMessage(g2, width, height);
        } else {
            plotPoints(g2, padding, plotWidth, plotHeight, height);
        }
        g2.dispose();
    }

    private void drawAxes(Graphics2D g2, int width, int height, int padding) {
        g2.setColor(AXIS_COLOR);
        g2.drawLine(padding, height - padding, width - padding, height - padding); // X axis
        g2.drawLine(padding, height - padding, padding, padding); // Y axis
    }

    private void drawDiagonal(Graphics2D g2, int width, int height, int padding) {
        g2.setColor(Color.WHITE);
        g2.drawLine(padding, padding, width - padding, height - padding);
    }

    private void drawLabels(Graphics2D g2, int width, int height, int padding) {
        g2.setColor(Color.DARK_GRAY);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("Instability", width / 2 - fm.stringWidth("Instability") / 2, height - padding + 30);
        AffineTransform previous = g2.getTransform();
        g2.rotate(-Math.PI / 2);
        g2.drawString("Abstractness", -height / 2 - fm.stringWidth("Abstractness") / 2, padding - 20);
        g2.setTransform(previous);
    }

    private void drawEmptyMessage(Graphics2D g2, int width, int height) {
        g2.setColor(Color.DARK_GRAY);
        String message = "Run an analysis to see DIA metrics.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(message, (width - fm.stringWidth(message)) / 2, height / 2);
    }

    private void plotPoints(Graphics2D g2, int padding, int plotWidth, int plotHeight, int height) {
        for (DiaMetricsData metric : metrics) {
            double instability = clamp(metric.getInstability());
            double abstractness = clamp(metric.getAbstractness());
            int x = padding + (int) Math.round(instability * plotWidth);
            int y = height - padding - (int) Math.round(abstractness * plotHeight);

            g2.setColor(POINT_COLOR);
            g2.fillOval(x - 4, y - 4, 8, 8);
            g2.drawString(metric.getSimpleName(), x + 6, y - 6);
        }
    }

    private double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }
}
