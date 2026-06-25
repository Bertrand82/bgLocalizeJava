package com.bg.bglocalize.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bg.bglocalize.colmap.FactoryImage2DOpenCV;
import com.bg.bglocalize.colmap.Image2DOpenCV;
import com.bg.bglocalize.match.FeatureMatchResult;
import com.bg.bglocalize.match.ImageMatchService;
import com.bg.bglocalize.opencv.OpenCvInitializer;

public class MainSwing {

    private static final Logger logger = LoggerFactory.getLogger(MainSwing.class);

    private final Image2DOpenCvPanel panel1 = new Image2DOpenCvPanel();
    private final Image2DOpenCvPanel panel2 = new Image2DOpenCvPanel();
    private final FactoryImage2DOpenCV factory = new FactoryImage2DOpenCV();
    private final ImageMatchService imageMatchService = new ImageMatchService();
    private FeatureMatchResult matchResult;

    private JFrame frame;

    public void show() {
        frame = new JFrame("bgLocalize - Feature Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panel2);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);

        JCheckBox showObservationsCheckBox = new JCheckBox("Afficher les observations", true);
        showObservationsCheckBox.addActionListener(e -> {
            boolean selected = showObservationsCheckBox.isSelected();
            panel1.setShowObservations(selected);
            panel2.setShowObservations(selected);
        });
        JCheckBox showTopMatchesCheckBox = new JCheckBox("Afficher 30 premiers matchs", false);
        showTopMatchesCheckBox.addActionListener(e -> updateTopMatchesDisplay(showTopMatchesCheckBox.isSelected()));
        JButton matchImagesButton = new JButton("Matcher les images");
        matchImagesButton.addActionListener(e -> matchImages(showTopMatchesCheckBox, matchImagesButton));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(showObservationsCheckBox);
        controlPanel.add(showTopMatchesCheckBox);
        controlPanel.add(matchImagesButton);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        frame.setJMenuBar(buildMenuBar());

        frame.setSize(1280, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFichier = new JMenu("Fichier");

        JMenuItem loadImage1 = new JMenuItem("Charger image 1");
        JMenuItem loadImage2 = new JMenuItem("Charger image 2");

        loadImage1.addActionListener(e -> loadImage(panel1, "Charger image 1"));
        loadImage2.addActionListener(e -> loadImage(panel2, "Charger image 2"));

        menuFichier.add(loadImage1);
        menuFichier.add(loadImage2);
        menuBar.add(menuFichier);
        return menuBar;
    }

    private void loadImage(Image2DOpenCvPanel targetPanel, String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Images (jpg, jpeg, png, bmp, tiff)", "jpg", "jpeg", "png", "bmp", "tiff", "tif"));
        fileChooser.setAcceptAllFileFilterUsed(true);

        if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();

        new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() throws Exception {
                Image2DOpenCV image2DOpenCV = factory.create(selectedFile);
                BufferedImage bufferedImage = ImageIO.read(selectedFile);
                return new LoadResult(image2DOpenCV, bufferedImage);
            }

            @Override
            protected void done() {
                try {
                    LoadResult result = get();
                    targetPanel.setImage2DOpenCV(result.image2DOpenCV(), result.bufferedImage());
                    matchResult = null;
                    panel1.setTopMatches(null);
                    panel2.setTopMatches(null);
                    logger.info("Loaded image: {} with {} features",
                            selectedFile.getName(), result.image2DOpenCV().getObservationFeatures().size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Image loading interrupted: {}", selectedFile, e);
                } catch (ExecutionException e) {
                    logger.error("Failed to load image: {}", selectedFile, e.getCause());
                    JOptionPane.showMessageDialog(frame,
                            "Impossible de charger l'image :\n" + e.getCause().getMessage(),
                            "Erreur de chargement",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void matchImages(JCheckBox showTopMatchesCheckBox, JButton matchImagesButton) {
        Image2DOpenCV firstImage = panel1.getImage2DOpenCV();
        Image2DOpenCV secondImage = panel2.getImage2DOpenCV();
        if (firstImage == null || secondImage == null) {
            JOptionPane.showMessageDialog(frame,
                    "Veuillez charger les deux images avant de lancer le matching.",
                    "Images manquantes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        matchImagesButton.setEnabled(false);
        new SwingWorker<FeatureMatchResult, Void>() {
            @Override
            protected FeatureMatchResult doInBackground() {
                return imageMatchService.match(firstImage, secondImage);
            }

            @Override
            protected void done() {
                matchImagesButton.setEnabled(true);
                try {
                    matchResult = get();
                    updateTopMatchesDisplay(showTopMatchesCheckBox.isSelected());
                    logger.info("Match success: {}", matchResult);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Image matching interrupted", e);
                } catch (ExecutionException e) {
                    logger.error("Failed to match images", e.getCause());
                    JOptionPane.showMessageDialog(frame,
                            "Impossible de comparer les images :\n" + e.getCause().getMessage(),
                            "Erreur de matching",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateTopMatchesDisplay(boolean showTopMatches) {
        if (!showTopMatches || matchResult == null) {
            panel1.setTopMatches(null);
            panel2.setTopMatches(null);
            return;
        }
        List<org.opencv.core.DMatch> bestMatches = matchResult.getMatches().stream()
                .sorted(Comparator.comparingDouble(match -> match.distance))
                .limit(30)
                .toList();
        panel1.setTopMatches(bestMatches);
        panel2.setTopMatches(bestMatches);
    }

    private record LoadResult(Image2DOpenCV image2DOpenCV, BufferedImage bufferedImage) {}

    public static void main(String[] args) {
    	OpenCvInitializer.initialize();
    	System.out.println("MainSwing start");
       
    	 new MainSwing().show();
    }
}
