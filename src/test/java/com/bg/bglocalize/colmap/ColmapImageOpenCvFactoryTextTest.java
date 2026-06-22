package com.bg.bglocalize.colmap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.opencv.OpenCvInitializer;

class ColmapImageOpenCvFactoryTextTest {

    private final ColmapImageOpenCvFactoryText factoryText = new ColmapImageOpenCvFactoryText();

    @BeforeAll
    static void initializeOpenCv() {
        OpenCvInitializer.initialize();
    }

    @Test
    void shouldWriteAndReadBackSingleImage() throws IOException {
        ColmapImageObservation obs = new ColmapImageObservation(174.312, 7.635, -1L);
        KeyPoint kp = new KeyPoint(174.3125f, 7.635f, 31.0f, 45.0f, 0.5f, 1, 0);
        Mat desc = buildFloatDescriptor(128, 0.0f, 1.0f / 128);

        ColmapImageObservationOpenCV obsFeature = new ColmapImageObservationOpenCV(obs, kp, desc);
        ColmapImage ci = new ColmapImage(1L, 0.965, 0.039, 0.258, -0.004, 2.712, -0.871, 2.945, 1L,
                "IMG_20260618_124549.jpg", List.of(obs));
        ColmapImageOpenCV original = new ColmapImageOpenCV(ci, "IMG_20260618_124549.jpg",
                FeatureAlgorithm.SIFT, List.of(obsFeature));

        Path tempFile = Files.createTempFile("colmap_opencv_single_", ".txt");
        try {
            factoryText.write(List.of(original), tempFile);
            List<ColmapImageOpenCV> loaded = factoryText.read(tempFile);

            assertEquals(1, loaded.size());
            ColmapImageOpenCV loadedImage = loaded.get(0);

            assertColmapImageEquals(ci, loadedImage.getColmapImage());
            assertEquals(FeatureAlgorithm.SIFT, loadedImage.getAlgorithm());
            assertEquals("IMG_20260618_124549.jpg", loadedImage.getImageName());

            assertEquals(1, loadedImage.getObservationFeatures().size());
            ColmapImageObservationOpenCV loadedObs = loadedImage.getObservationFeatures().get(0);

            assertEquals(obs, loadedObs.getObservation());
            assertKeyPointEquals(kp, loadedObs.getKeyPoint());
            assertDescriptorEquals(desc, loadedObs.getDescriptor());
        } finally {
            releaseDescriptors(List.of(original));
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldWriteAndReadBackMultipleImages() throws IOException {
        ColmapImageObservation obs1 = new ColmapImageObservation(10.0, 20.0, 5L);
        KeyPoint kp1 = new KeyPoint(10.0f, 20.0f, 31.0f, -1.0f, 0.0f, 0, -1);
        Mat desc1 = buildFloatDescriptor(128, 0.0f, 1.0f);

        ColmapImageObservation obs2a = new ColmapImageObservation(100.0, 200.0, 42L);
        ColmapImageObservation obs2b = new ColmapImageObservation(300.0, 400.0, -1L);
        KeyPoint kp2a = new KeyPoint(100.0f, 200.0f, 31.0f, 90.0f, 0.8f, 2, 0);
        KeyPoint kp2b = new KeyPoint(300.0f, 400.0f, 31.0f, 180.0f, 0.3f, 0, -1);
        Mat desc2a = buildFloatDescriptor(128, 1.0f, 1.0f);
        Mat desc2b = buildFloatDescriptor(128, 2.0f, 1.0f);

        ColmapImage ci1 = new ColmapImage(1L, 0.9, 0.1, 0.0, 0.0, 1.0, 2.0, 3.0, 1L,
                "image1.jpg", List.of(obs1));
        ColmapImage ci2 = new ColmapImage(2L, 0.8, 0.2, 0.1, 0.0, 4.0, 5.0, 6.0, 1L,
                "image2.jpg", List.of(obs2a, obs2b));

        ColmapImageOpenCV image1 = new ColmapImageOpenCV(ci1, "image1.jpg", FeatureAlgorithm.SIFT,
                List.of(new ColmapImageObservationOpenCV(obs1, kp1, desc1)));
        ColmapImageOpenCV image2 = new ColmapImageOpenCV(ci2, "image2.jpg", FeatureAlgorithm.SIFT,
                List.of(new ColmapImageObservationOpenCV(obs2a, kp2a, desc2a),
                        new ColmapImageObservationOpenCV(obs2b, kp2b, desc2b)));

        Path tempFile = Files.createTempFile("colmap_opencv_multi_", ".txt");
        try {
            factoryText.write(List.of(image1, image2), tempFile);
            List<ColmapImageOpenCV> loaded = factoryText.read(tempFile);

            assertEquals(2, loaded.size());

            ColmapImageOpenCV loaded1 = loaded.get(0);
            assertEquals(1L, loaded1.getColmapImage().imageId());
            assertEquals(1, loaded1.getObservationFeatures().size());
            assertEquals(obs1, loaded1.getObservationFeatures().get(0).getObservation());

            ColmapImageOpenCV loaded2 = loaded.get(1);
            assertEquals(2L, loaded2.getColmapImage().imageId());
            assertEquals(2, loaded2.getObservationFeatures().size());
            assertEquals(obs2a, loaded2.getObservationFeatures().get(0).getObservation());
            assertEquals(obs2b, loaded2.getObservationFeatures().get(1).getObservation());
            assertDescriptorEquals(desc2b, loaded2.getObservationFeatures().get(1).getDescriptor());
        } finally {
            releaseDescriptors(List.of(image1, image2));
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldWriteAndReadBackEmptyDescriptor() throws IOException {
        ColmapImageObservation obs = new ColmapImageObservation(50.0, 60.0, -1L);
        KeyPoint kp = new KeyPoint(50.0f, 60.0f, 31.0f);
        Mat emptyDesc = new Mat();

        ColmapImageObservationOpenCV obsFeature = new ColmapImageObservationOpenCV(obs, kp, emptyDesc);
        ColmapImage ci = new ColmapImage(3L, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1L,
                "empty.jpg", List.of(obs));
        ColmapImageOpenCV original = new ColmapImageOpenCV(ci, "empty.jpg",
                FeatureAlgorithm.ORB, List.of(obsFeature));

        Path tempFile = Files.createTempFile("colmap_opencv_empty_desc_", ".txt");
        try {
            factoryText.write(List.of(original), tempFile);
            List<ColmapImageOpenCV> loaded = factoryText.read(tempFile);

            assertEquals(1, loaded.size());
            ColmapImageObservationOpenCV loadedObs = loaded.get(0).getObservationFeatures().get(0);
            assertTrue(loadedObs.getDescriptor().empty());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldWriteAndReadBackImageWithNoObservations() throws IOException {
        ColmapImage ci = new ColmapImage(4L, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1L,
                "noobs.jpg", List.of());
        ColmapImageOpenCV original = new ColmapImageOpenCV(ci, "noobs.jpg",
                FeatureAlgorithm.SIFT, List.of());

        Path tempFile = Files.createTempFile("colmap_opencv_noobs_", ".txt");
        try {
            factoryText.write(List.of(original), tempFile);
            List<ColmapImageOpenCV> loaded = factoryText.read(tempFile);

            assertEquals(1, loaded.size());
            assertEquals(0, loaded.get(0).getObservationFeatures().size());
            assertEquals(4L, loaded.get(0).getColmapImage().imageId());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldPreserveAllColmapImageFields() throws IOException {
        ColmapImageObservation obs = new ColmapImageObservation(1.0, 2.0, 7L);
        KeyPoint kp = new KeyPoint(1.0f, 2.0f, 31.0f);
        Mat desc = new Mat();

        ColmapImage ci = new ColmapImage(
                99L, 0.96519351948199339, 0.039120325255590484,
                0.2585553653980231, -0.0044936761960093895,
                2.7123899170228367, -0.87102923280167643, 2.9455825074265096,
                5L, "my_image.jpg", List.of(obs));
        ColmapImageOpenCV original = new ColmapImageOpenCV(ci, "my_image.jpg",
                FeatureAlgorithm.AKAZE, List.of(new ColmapImageObservationOpenCV(obs, kp, desc)));

        Path tempFile = Files.createTempFile("colmap_opencv_fields_", ".txt");
        try {
            factoryText.write(List.of(original), tempFile);
            List<ColmapImageOpenCV> loaded = factoryText.read(tempFile);

            ColmapImage loadedCi = loaded.get(0).getColmapImage();
            assertEquals(99L, loadedCi.imageId());
            assertEquals(0.96519351948199339, loadedCi.qw());
            assertEquals(0.039120325255590484, loadedCi.qx());
            assertEquals(0.2585553653980231, loadedCi.qy());
            assertEquals(-0.0044936761960093895, loadedCi.qz());
            assertEquals(2.7123899170228367, loadedCi.tx());
            assertEquals(-0.87102923280167643, loadedCi.ty());
            assertEquals(2.9455825074265096, loadedCi.tz());
            assertEquals(5L, loadedCi.cameraId());
            assertEquals("my_image.jpg", loadedCi.name());
            assertEquals(FeatureAlgorithm.AKAZE, loaded.get(0).getAlgorithm());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldRejectMissingFile() {
        assertThrows(IllegalArgumentException.class,
                () -> factoryText.read(Path.of("data", "BG", "sparse", "0", "missing-opencv.txt")));
    }

    // --- helpers ---

    private static Mat buildFloatDescriptor(int cols, float startValue, float step) {
        Mat desc = new Mat(1, cols, CvType.CV_32F);
        float[] values = new float[cols];
        for (int i = 0; i < cols; i++) {
            values[i] = startValue + i * step;
        }
        desc.put(0, 0, values);
        return desc;
    }

    private static void assertColmapImageEquals(ColmapImage expected, ColmapImage actual) {
        assertEquals(expected.imageId(), actual.imageId());
        assertEquals(expected.qw(), actual.qw());
        assertEquals(expected.qx(), actual.qx());
        assertEquals(expected.qy(), actual.qy());
        assertEquals(expected.qz(), actual.qz());
        assertEquals(expected.tx(), actual.tx());
        assertEquals(expected.ty(), actual.ty());
        assertEquals(expected.tz(), actual.tz());
        assertEquals(expected.cameraId(), actual.cameraId());
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.observations().size(), actual.observations().size());
    }

    private static void assertKeyPointEquals(KeyPoint expected, KeyPoint actual) {
        assertEquals((float) expected.pt.x, (float) actual.pt.x, 1e-5f);
        assertEquals((float) expected.pt.y, (float) actual.pt.y, 1e-5f);
        assertEquals(expected.size, actual.size, 1e-5f);
        assertEquals(expected.angle, actual.angle, 1e-5f);
        assertEquals(expected.response, actual.response, 1e-5f);
        assertEquals(expected.octave, actual.octave);
        assertEquals(expected.class_id, actual.class_id);
    }

    private static void assertDescriptorEquals(Mat expected, Mat actual) {
        assertEquals(expected.rows(), actual.rows());
        assertEquals(expected.cols(), actual.cols());
        assertEquals(expected.type(), actual.type());
        float[] expectedValues = new float[expected.cols()];
        float[] actualValues = new float[actual.cols()];
        expected.get(0, 0, expectedValues);
        actual.get(0, 0, actualValues);
        assertArrayEquals(expectedValues, actualValues, 1e-5f);
    }

    private static void releaseDescriptors(List<ColmapImageOpenCV> images) {
        images.forEach(ColmapImageOpenCV::releaseDescriptors);
    }
}
