package com.bertrand82.bglocalize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import com.bertrand82.util.UtilImage;

class AppTest {
	
	static File dirTarget = new File("target");

    @BeforeAll
    static void initializeOpenCv() {
        OpenCvInitializer.initialize();
        System.out.println("bg initializeOpenCv done");
    }

    @Test
    void shouldLoadImageFromFilesystem() throws IOException {
    	File fileImage = new File(dirTarget, "sample.png");
        Path imagePath =UtilImage. createTexturedImage(fileImage.toPath());

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
        System.out.println("bg shouldLoadImageFromFilesystem  done "+fileImage.getAbsolutePath());
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void shouldExtractOpenCvCompatibleFeatures(FeatureAlgorithm algorithm, @TempDir Path tempDir) throws IOException {
    	File fileImage = new File(dirTarget,"image_"+algorithm.name().toLowerCase() + ".png");
        Path imagePath = UtilImage.createTexturedImage(fileImage);

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
        Path imagePath =UtilImage. createTexturedImage(tempDir.resolve("cli.png"));
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

    
}
