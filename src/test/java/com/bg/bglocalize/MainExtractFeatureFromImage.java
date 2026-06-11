package com.bg.bglocalize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.features.OpenCvFeatureExtractor;
import com.bg.bglocalize.image.FilesystemImageLoader;
import com.bg.bglocalize.image.LoadedImage;

public class MainExtractFeatureFromImage {

	static File imageTest = new File("data", "BG.jpg");

	public static void main(String[] args) throws Exception{
		extractFeatures(imageTest);
	}

	public static void extractFeatures(File imageFile) throws Exception {
		
		FeatureAlgorithm algorithm = FeatureAlgorithm.SIFT;
		LoadedImage loadedImage = new FilesystemImageLoader().load(imageFile); 
		FeatureExtractionResult result = new OpenCvFeatureExtractor().extract(loadedImage, algorithm);
		System.out.println("result : "+result.toString());
	}
	
	

    void shouldExtractOpenCvCompatibleFeatures(FeatureAlgorithm algorithm, @TempDir Path tempDir) throws IOException {
    	Path imagePath = ExtractionFeaturesTest. getImageTest();

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

}
