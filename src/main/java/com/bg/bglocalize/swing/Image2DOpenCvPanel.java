package com.bg.bglocalize.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import org.opencv.core.DMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bg.bglocalize.colmap.ColmapImageObservationOpenCV;
import com.bg.bglocalize.colmap.Image2DOpenCV;

public class Image2DOpenCvPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(Image2DOpenCvPanel.class);

    private Image2DOpenCV image2DOpenCV;
    private BufferedImage bufferedImage;
    private boolean showObservations = true;
    private List<DMatch> topMatches;

    private static final Color FEATURE_OVERLAY_COLOR = new Color(0, 220, 0, 200);

    public Image2DOpenCvPanel() {
        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(640, 480));
    }

    public void setShowObservations(boolean showObservations) {
        this.showObservations = showObservations;
        repaint();
    }

    public Image2DOpenCV getImage2DOpenCV() {
        return image2DOpenCV;
    }

    public void setTopMatches(List<DMatch> topMatches) {
        this.topMatches = topMatches;
        repaint();
    }

    /**
     * Updates the panel with a pre-loaded image and its OpenCV features.
     * Must be called on the EDT.
     */
    public void setImage2DOpenCV(Image2DOpenCV image2DOpenCV, BufferedImage bufferedImage) {
        this.image2DOpenCV = image2DOpenCV;
        this.bufferedImage = bufferedImage;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bufferedImage == null) {
            drawPlaceholder(g);
            return;
        }

        int panelW = getWidth();
        int panelH = getHeight();
        int imgW = bufferedImage.getWidth();
        int imgH = bufferedImage.getHeight();

        double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
        int scaledW = (int) (imgW * scale);
        int scaledH = (int) (imgH * scale);
        int offsetX = (panelW - scaledW) / 2;
        int offsetY = (panelH - scaledH) / 2;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(bufferedImage, offsetX, offsetY, scaledW, scaledH, null);

        if (showObservations) {
            drawFeatures(g2d, scale, offsetX, offsetY);
        }
        drawMatchInfo(g2d);
    }

    private void drawPlaceholder(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 16f));
        String msg = "Aucune image chargée";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(msg)) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(msg, x, y);
    }

    private void drawFeatures(Graphics2D g2d, double scale, int offsetX, int offsetY) {
        if (image2DOpenCV == null) {
            return;
        }
        List<ColmapImageObservationOpenCV> features = image2DOpenCV.getObservationFeatures();
        if (features == null || features.isEmpty()) {
            return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(FEATURE_OVERLAY_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));

        for (ColmapImageObservationOpenCV feature : features) {
            double kpX = feature.getKeyPoint().pt.x * scale + offsetX;
            double kpY = feature.getKeyPoint().pt.y * scale + offsetY;
            double radius = Math.max(3.0, feature.getKeyPoint().size * scale / 2.0);
            int cx = (int) Math.round(kpX - radius);
            int cy = (int) Math.round(kpY - radius);
            int diameter = (int) Math.round(radius * 2);
            g2d.drawOval(cx, cy, diameter, diameter);
        }
    }

    private void drawMatchInfo(Graphics2D g2d) {
        if (topMatches == null || topMatches.isEmpty()) {
            return;
        }

        float minDistance = Float.POSITIVE_INFINITY;
        float maxDistance = Float.NEGATIVE_INFINITY;
        for (DMatch match : topMatches) {
            minDistance = Math.min(minDistance, match.distance);
            maxDistance = Math.max(maxDistance, match.distance);
        }

        String label = String.format("Matchs: %d | distance: %.2f - %.2f", topMatches.size(), minDistance, maxDistance);
        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRoundRect(10, 10, g2d.getFontMetrics().stringWidth(label) + 14, 24, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, 17, 27);
    }
}
