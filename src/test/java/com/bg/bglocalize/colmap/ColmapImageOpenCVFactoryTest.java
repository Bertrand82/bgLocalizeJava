package com.bg.bglocalize.colmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.opencv.OpenCvInitializer;

class ColmapImageOpenCVFactoryTest {

    private static final File DATABASE_FILE = new File("data/BG/database.db");
    private static final File IMAGES_DIRECTORY = new File("data/BG/images");
    private static final File IMAGES_FILE = new File("data/BG/sparse/0/images.txt");

    private final ColmapTextModelReader reader = new ColmapTextModelReader();

    private ColmapImageOpenCVFactory factory;

    @BeforeAll
    static void initializeOpenCv() {
        OpenCvInitializer.initialize();
    }

    @BeforeEach
    void setUp() {
        factory = new ColmapImageOpenCVFactory(DATABASE_FILE, IMAGES_DIRECTORY);
    }

    @Test
    void shouldCreateOpenCvFeaturesForOneColmapImage() throws IOException, SQLException {
        ColmapImage colmapImage = firstImageWithLimitedObservations(8);

        ColmapImageOpenCV result = factory.create(colmapImage, FeatureAlgorithm.SIFT);

        try {
            assertEquals(colmapImage, result.getColmapImage());
            assertEquals("IMG_20260618_124549.jpg", result.getImageName());
            assertEquals(FeatureAlgorithm.SIFT, result.getAlgorithm());
            assertEquals(colmapImage.observations().size(), result.getObservationFeatures().size());
            assertTrue(result.getObservationFeatures().stream().allMatch(ColmapImageObservationOpenCV::hasDescriptor));

            ColmapImageObservationOpenCV firstFeature = result.getObservationFeatures().get(0);
            assertEquals(colmapImage.observations().get(0), firstFeature.getObservation());
            assertFalse(firstFeature.getDescriptor().empty());
            assertEquals(1, firstFeature.getDescriptor().rows());
            assertTrue(firstFeature.getDescriptor().cols() > 0);
        } finally {
            result.releaseDescriptors();
        }
    }

    @Test
    void shouldCreateOpenCvFeaturesForManyColmapImages() throws IOException, SQLException {
        List<ColmapImage> images = reader.readImages(IMAGES_FILE.toPath()).stream()
                .limit(2)
                .map(image -> copyWithLimitedObservations(image, 5))
                .toList();

        List<ColmapImageOpenCV> results = factory.createAll(images, FeatureAlgorithm.SIFT);

        try {
            assertEquals(2, results.size());
            assertEquals(images.get(0).imageId(), results.get(0).getColmapImage().imageId());
            assertEquals(images.get(1).imageId(), results.get(1).getColmapImage().imageId());
            assertEquals(images.get(0).observations().size(), results.get(0).getObservationFeatures().size());
            assertEquals(images.get(1).observations().size(), results.get(1).getObservationFeatures().size());
            assertTrue(results.stream()
                    .flatMap(result -> result.getObservationFeatures().stream())
                    .allMatch(ColmapImageObservationOpenCV::hasDescriptor));
        } finally {
            results.forEach(ColmapImageOpenCV::releaseDescriptors);
        }
    }

    @Test
    void shouldRejectObservationOutsideImageBounds() {
        ColmapImage invalidImage = new ColmapImage(
                1L,
                1.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                1L,
                "IMG_20260618_124549.jpg",
                List.of(new ColmapImageObservation(10_000.0, 10_000.0, 1L)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> factory.create(invalidImage, FeatureAlgorithm.SIFT));

        assertTrue(exception.getMessage().contains("outside image bounds"));
    }

    private ColmapImage firstImageWithLimitedObservations(int observationCount) throws IOException {
        ColmapImage firstImage = reader.readImages(IMAGES_FILE.toPath()).get(0);
        return copyWithLimitedObservations(firstImage, observationCount);
    }

    private static ColmapImage copyWithLimitedObservations(ColmapImage image, int observationCount) {
        return new ColmapImage(
                image.imageId(),
                image.qw(),
                image.qx(),
                image.qy(),
                image.qz(),
                image.tx(),
                image.ty(),
                image.tz(),
                image.cameraId(),
                image.name(),
                image.observations().subList(0, observationCount));
    }
}
