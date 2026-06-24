package com.bg.bglocalize.colmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.opencv.OpenCvInitializer;

class ColmapImageOpenCVTest {

    private ColmapImage2DOpenCV left;
    private ColmapImage2DOpenCV right;
    private ColmapImage2DOpenCV different;

    @BeforeAll
    static void initializeOpenCv() {
        OpenCvInitializer.initialize();
    }

    @AfterEach
    void releaseDescriptors() {
        release(left);
        release(right);
        release(different);
    }

    @Test
    void shouldCompareImagesUsingLogicalContent() {
        ColmapImageObservation observation = new ColmapImageObservation(174.312, 7.635, 12L);
        ColmapImage2D colmapImage = new ColmapImage2D(1L, 0.965, 0.039, 0.258, -0.004,
                2.712, -0.871, 2.945, 1L, "IMG_20260618_124549.jpg", List.of(observation));

        left = new ColmapImage2DOpenCV(
                colmapImage,
                "IMG_20260618_124549.jpg",
                FeatureAlgorithm.SIFT,
                List.of(new ColmapImageObservationOpenCV(
                        observation,
                        new KeyPoint(174.3125f, 7.635f, 31.0f, 45.0f, 0.5f, 1, 0),
                        buildFloatDescriptor(4, 0.0f))));
        right = new ColmapImage2DOpenCV(
                colmapImage,
                "IMG_20260618_124549.jpg",
                FeatureAlgorithm.SIFT,
                List.of(new ColmapImageObservationOpenCV(
                        observation,
                        new KeyPoint(174.3125f, 7.635f, 31.0f, 45.0f, 0.5f, 1, 0),
                        buildFloatDescriptor(4, 0.0f))));
        different = new ColmapImage2DOpenCV(
                colmapImage,
                "IMG_20260618_124549.jpg",
                FeatureAlgorithm.SIFT,
                List.of(new ColmapImageObservationOpenCV(
                        observation,
                        new KeyPoint(174.3125f, 7.635f, 31.0f, 45.0f, 0.5f, 1, 0),
                        buildFloatDescriptor(4, 1.0f))));

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, different);
        assertNotEquals(left, null);
        assertNotEquals(left, "not an image");
    }

    private static Mat buildFloatDescriptor(int cols, float startValue) {
        Mat descriptor = new Mat(1, cols, CvType.CV_32F);
        float[] values = new float[cols];
        for (int i = 0; i < cols; i++) {
            values[i] = startValue + i;
        }
        descriptor.put(0, 0, values);
        return descriptor;
    }

    private static void release(ColmapImage2DOpenCV image) {
        if (image != null) {
            image.releaseDescriptors();
        }
    }
}
