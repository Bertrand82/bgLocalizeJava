package com.bertrand82.bglocalize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.bertrand82.bglocalize.cli.FeatureCli;
import com.bertrand82.bglocalize.features.FeatureAlgorithm;
import com.bertrand82.bglocalize.features.FeatureExtractionResult;
import com.bertrand82.bglocalize.features.OpenCvFeatureExtractor;
import com.bertrand82.bglocalize.image.FilesystemImageLoader;
import com.bertrand82.bglocalize.image.LoadedImage;
import com.bertrand82.bglocalize.opencv.OpenCvInitializer;

class AppTest {

    @BeforeAll
    static void initializeOpenCv() {
        OpenCvInitializer.initialize();
    }

    @Test
    void shouldLoadImageFromFilesystem(@TempDir Path tempDir) throws IOException {
        Path imagePath = createTexturedImage(tempDir.resolve("sample.png"));

        LoadedImage loadedImage = new FilesystemImageLoader().load(imagePath.toString());

        try {
            assertEquals(imagePath.toAbsolutePath().toString(), loadedImage.getImagePath());
            assertEquals("sample.png", loadedImage.getImageId());
            assertEquals(320, loadedImage.getWidth());
            assertEquals(240, loadedImage.getHeight());
            assertFalse(loadedImage.getImage().empty());
        } finally {
            loadedImage.getImage().release();
        }
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void shouldExtractOpenCvCompatibleFeatures(FeatureAlgorithm algorithm, @TempDir Path tempDir) throws IOException {
        Path imagePath = createTexturedImage(tempDir.resolve(algorithm.name().toLowerCase() + ".png"));

        FeatureExtractionResult result = new OpenCvFeatureExtractor().extract(imagePath.toString(), algorithm);

        try {
            assertEquals(imagePath.toAbsolutePath().toString(), result.getImagePath());
            assertEquals(imagePath.getFileName().toString(), result.getImageId());
            assertEquals(algorithm, result.getAlgorithm());
            assertEquals(320, result.getWidth());
            assertEquals(240, result.getHeight());
            assertTrue(result.getKeypointCount() > 0);
            assertFalse(result.getKeypoints().empty());
            assertFalse(result.getDescriptors().empty());
            assertEquals(result.getKeypointCount(), result.getDescriptors().rows());
            assertEquals(result.getDescriptorMatType(), result.getDescriptors().type());
        } finally {
            result.getKeypoints().release();
            result.getDescriptors().release();
        }
    }

    @Test
    void shouldRunCli(@TempDir Path tempDir) throws IOException {
        Path imagePath = createTexturedImage(tempDir.resolve("cli.png"));
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitCode = FeatureCli.run(
                new String[] { "--image", imagePath.toString(), "--algorithm", "SIFT" },
                new PrintStream(stdout),
                new PrintStream(stderr));

        assertEquals(0, exitCode);
        assertTrue(stderr.toString().isBlank());
        assertTrue(stdout.toString().contains("algorithm=SIFT"));
        assertTrue(stdout.toString().contains("keypoints="));
    }

    private static Stream<FeatureAlgorithm> algorithms() {
        return Stream.of(FeatureAlgorithm.ORB, FeatureAlgorithm.SIFT, FeatureAlgorithm.AKAZE, FeatureAlgorithm.BRISK);
    }

    private static Path createTexturedImage(Path imagePath) throws IOException {
        BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(3f));
        graphics.drawRect(20, 20, 280, 180);
        graphics.drawLine(20, 20, 300, 200);
        graphics.drawLine(300, 20, 20, 200);
        graphics.setColor(Color.BLUE);
        graphics.fillOval(40, 40, 60, 60);
        graphics.setColor(Color.RED);
        graphics.fillOval(220, 120, 50, 50);
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawString("bgLocalize", 110, 110);
        graphics.setColor(Color.GREEN);
        for (int x = 0; x < image.getWidth(); x += 20) {
            graphics.drawLine(x, 0, x, image.getHeight());
        }
        for (int y = 0; y < image.getHeight(); y += 20) {
            graphics.drawLine(0, y, image.getWidth(), y);
        }
        graphics.dispose();
        ImageIO.write(image, "png", imagePath.toFile());
        return imagePath;
    }
}
