package com.bg.bglocalize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.bg.bglocalize.cli.FeatureCli;
import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.features.OpenCvFeatureExtractor;
import com.bg.bglocalize.image.FilesystemImageLoader;
import com.bg.bglocalize.image.LoadedImage;
import com.bg.bglocalize.opencv.OpenCvInitializer;

class ExtractionFeaturesTest {
	
	static File dirTarget = new File("target");
	static File imageTest = new File("data","BG.jpg");

    @BeforeAll
    static void initializeOpenCv() {
        OpenCvInitializer.initialize();
      
    }
 

    @Test
    void shouldLoadImageFromFilesystem() throws IOException {
    	
         LoadedImage loadedImage = new FilesystemImageLoader().load(imageTest);

        try {
            
            assertFalse(loadedImage.getImage().empty());
        } finally {
            loadedImage.getImage().release();
        }
        System.out.println("bg shouldLoadImageFromFilesystem  done "+imageTest.toPath());
    }

   

	@ParameterizedTest
    @MethodSource("algorithms")
    void shouldExtractOpenCvCompatibleFeatures(FeatureAlgorithm algorithm, @TempDir Path tempDir) throws IOException {
    	Path imagePath = imageTest.toPath();

        FeatureExtractionResult result = new OpenCvFeatureExtractor().extract(imagePath.toString(), algorithm);

        try {
            assertEquals(imagePath.toAbsolutePath().toString(), result.getImagePath());
            assertEquals(imagePath.getFileName().toString(), result.getImageId());
            assertEquals(algorithm, result.getAlgorithm());
         
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
        Path imagePath =imageTest.toPath();
       
        String[] args =new String[] { "--image", imagePath.toString(), "--algorithm", "SIFT" };
        FeatureCli.main(args);

       
    }

    private static Stream<FeatureAlgorithm> algorithms() {
        return Stream.of(FeatureAlgorithm.ORB, FeatureAlgorithm.SIFT, FeatureAlgorithm.AKAZE, FeatureAlgorithm.BRISK);
    }

    
}
