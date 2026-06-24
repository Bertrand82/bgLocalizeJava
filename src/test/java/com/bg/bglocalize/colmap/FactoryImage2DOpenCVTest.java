package com.bg.bglocalize.colmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.bg.bglocalize.features.FeatureAlgorithm;

class FactoryImage2DOpenCVTest {

    private static final File JPEG_IMAGE = new File("data/BG.jpg");

    private final FactoryImage2DOpenCV factory = new FactoryImage2DOpenCV();

    private Image2DOpenCV createdImage;

    @AfterEach
    void tearDown() {
        if (createdImage == null) {
            return;
        }
        for (ColmapImageObservationOpenCV observation : createdImage.getObservationFeatures()) {
            observation.getDescriptor().release();
        }
    }

    @Test
    void shouldCreateImage2DOpenCVFromJpegPath() {
        createdImage = factory.create(JPEG_IMAGE.getPath());

        assertEquals("BG.jpg", createdImage.getImageName());
        assertEquals(FeatureAlgorithm.SIFT, createdImage.getAlgorithm());
        assertFalse(createdImage.getObservationFeatures().isEmpty());
        assertTrue(createdImage.getObservationFeatures().stream().allMatch(ColmapImageObservationOpenCV::hasDescriptor));
    }

    @Test
    void shouldCreateImage2DOpenCVFromJpegFileWithAlgorithm() {
        createdImage = factory.create(JPEG_IMAGE, FeatureAlgorithm.ORB);

        assertEquals("BG.jpg", createdImage.getImageName());
        assertEquals(FeatureAlgorithm.ORB, createdImage.getAlgorithm());
        assertFalse(createdImage.getObservationFeatures().isEmpty());
        assertTrue(createdImage.getObservationFeatures().stream().allMatch(ColmapImageObservationOpenCV::hasDescriptor));
    }

    @Test
    void shouldRejectNullPath() {
        assertThrows(NullPointerException.class, () -> factory.create((String) null));
    }
}
