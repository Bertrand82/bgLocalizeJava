package com.bg.bglocalize.swing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bg.bglocalize.colmap.FactoryImage2DOpenCV;
import com.bg.bglocalize.colmap.Image2DOpenCV;
import com.bg.bglocalize.opencv.OpenCvInitializer;

public class MainSwing {

    private static final Logger logger = LoggerFactory.getLogger(MainSwing.class);

    private final Image2DOpenCvPanel panel1 = new Image2DOpenCvPanel();
    private final Image2DOpenCvPanel panel2 = new Image2DOpenCvPanel();
    private final FactoryImage2DOpenCV factory = new FactoryImage2DOpenCV();

    private JFrame frame;

    public void show() {
        frame = new JFrame("bgLocalize - Feature Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panel2);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        frame.getContentPane().add(splitPane);

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

    private record LoadResult(Image2DOpenCV image2DOpenCV, BufferedImage bufferedImage) {}

    public static void main(String[] args) {
        SwingWorker<Void, Void> initWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                OpenCvInitializer.initialize();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("OpenCV initialization interrupted", e);
                    JOptionPane.showMessageDialog(null,
                            "L'initialisation d'OpenCV a été interrompue.",
                            "Erreur d'initialisation",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (ExecutionException e) {
                    logger.error("OpenCV initialization failed", e.getCause());
                    JOptionPane.showMessageDialog(null,
                            "Impossible d'initialiser OpenCV :\n" + e.getCause().getMessage(),
                            "Erreur d'initialisation",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                SwingUtilities.invokeLater(() -> new MainSwing().show());
            }
        };
        initWorker.execute();
    }
}
