package com.bg.bglocalize.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
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
        try {
            Image2DOpenCV image2DOpenCV = factory.create(selectedFile);
            targetPanel.setImage2DOpenCV(image2DOpenCV, selectedFile);
            logger.info("Loaded image: {} with {} features",
                    selectedFile.getName(), image2DOpenCV.getObservationFeatures().size());
        } catch (Exception e) {
            logger.error("Failed to load image: {}", selectedFile, e);
            JOptionPane.showMessageDialog(frame,
                    "Impossible de charger l'image :\n" + e.getMessage(),
                    "Erreur de chargement",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        OpenCvInitializer.initialize();
        SwingUtilities.invokeLater(() -> new MainSwing().show());
    }
}
