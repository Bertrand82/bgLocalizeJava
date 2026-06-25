package com.bg.bglocalize.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import com.bg.bglocalize.colmap.ColmapImageObservation;
import com.bg.bglocalize.colmap.ColmapImageObservationOpenCV;
import com.bg.bglocalize.colmap.Image2DOpenCV;
import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.opencv.OpenCvInitializer;

class ImageMatchServiceImage2DOpenCVTest {

    private final ImageMatchService service = new ImageMatchService();

    private FeatureMatchResult resultToRelease;
    private Image2DOpenCV sourceToRelease;
    private Image2DOpenCV targetToRelease;

    @BeforeAll
    static void setUpOpenCv() {
        OpenCvInitializer.initialize();
    }

    @AfterEach
    void tearDown() {
        releaseImage(sourceToRelease);
        releaseImage(targetToRelease);
        releaseResultQuery(resultToRelease);
    }

    @Test
    void shouldMatchTwoImage2DOpenCvInstances() {
        sourceToRelease = createOrbImage("source.jpg", new int[] { 0x00, 0xF0, 0xFF });
        targetToRelease = createOrbImage("target.jpg", new int[] { 0x00, 0x0F, 0xF0 });

        resultToRelease = service.match(sourceToRelease, targetToRelease);

        assertNotNull(resultToRelease);
        assertEquals(sourceToRelease.getImageName(), resultToRelease.getQuery().getImageId());
        assertEquals(targetToRelease.getImageName(), resultToRelease.getTarget().getImageName());
        assertTrue(resultToRelease.getMatchCount() > 0);
    }

    @Test
    void shouldReturnEmptyMatchResultWhenTargetHasNoDescriptors() {
        sourceToRelease = createOrbImage("source.jpg", new int[] { 0x00, 0xF0, 0xFF });
        targetToRelease = new Image2DOpenCV("empty-target.jpg", FeatureAlgorithm.ORB, List.of());

        resultToRelease = service.match(sourceToRelease, targetToRelease);

        assertNotNull(resultToRelease);
        assertEquals(0, resultToRelease.getMatchCount());
        assertTrue(resultToRelease.getMatches().isEmpty());
        assertEquals("empty-target.jpg", resultToRelease.getTarget().getImageName());
    }

    @Test
    void shouldRejectNullSourceImage() {
        targetToRelease = createOrbImage("target.jpg", new int[] { 0x00 });

        assertThrows(NullPointerException.class, () -> service.match(null, targetToRelease));
    }

    @Test
    void shouldRejectMismatchedImageAlgorithms() {
        sourceToRelease = createOrbImage("source.jpg", new int[] { 0x00, 0xF0 });
        targetToRelease = createSiftLikeImage("target.jpg", new float[] { 0.0f, 1.0f });

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.match(sourceToRelease, targetToRelease));
        assertTrue(exception.getMessage().contains("Algorithm mismatch"));
    }

    private static Image2DOpenCV createOrbImage(String imageName, int[] descriptorValues) {
        return createImage(imageName, FeatureAlgorithm.ORB, descriptorValues, CvType.CV_8U);
    }

    private static Image2DOpenCV createSiftLikeImage(String imageName, float[] descriptorValues) {
        List<ColmapImageObservationOpenCV> observations = new java.util.ArrayList<>(descriptorValues.length);
        for (int i = 0; i < descriptorValues.length; i++) {
            Mat descriptor = new Mat(1, 1, CvType.CV_32F);
            descriptor.put(0, 0, descriptorValues[i]);
            observations.add(new ColmapImageObservationOpenCV(
                    new ColmapImageObservation(i, i, -1L),
                    new KeyPoint(i, i, 1.0f),
                    descriptor));
        }
        return new Image2DOpenCV(imageName, FeatureAlgorithm.SIFT, observations);
    }

    private static Image2DOpenCV createImage(String imageName, FeatureAlgorithm algorithm, int[] descriptorValues,
            int cvType) {
        List<ColmapImageObservationOpenCV> observations = new java.util.ArrayList<>(descriptorValues.length);
        for (int i = 0; i < descriptorValues.length; i++) {
            Mat descriptor = new Mat(1, 1, cvType);
            descriptor.put(0, 0, descriptorValues[i]);
            observations.add(new ColmapImageObservationOpenCV(
                    new ColmapImageObservation(i, i, -1L),
                    new KeyPoint(i, i, 1.0f),
                    descriptor));
        }
        return new Image2DOpenCV(imageName, algorithm, observations);
    }

    private static void releaseImage(Image2DOpenCV image) {
        if (image == null) {
            return;
        }
        for (ColmapImageObservationOpenCV observation : image.getObservationFeatures()) {
            observation.getDescriptor().release();
        }
    }

    private static void releaseResultQuery(FeatureMatchResult result) {
        if (result == null) {
            return;
        }
        FeatureExtractionResult query = result.getQuery();
        query.getKeypoints().release();
        query.getDescriptors().release();
    }
}
