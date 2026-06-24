package com.bg.bglocalize.colmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class ColmapTextModelReaderTest {

    private static final Path IMAGES_PATH = Path.of("data", "BG", "sparse", "0", "images.txt");
    private static final Path POINTS3D_PATH = Path.of("data", "BG", "sparse", "0", "points3D.txt");

    private final ColmapTextModelReader reader = new ColmapTextModelReader();

    @Test
    void shouldReadImagesFile() throws IOException {
        List<Image2DColmap> images = reader.readImages2D(IMAGES_PATH);

        assertEquals(6, images.size());

        Image2DColmap firstImage = images.get(0);
        assertEquals(1L, firstImage.imageId());
        assertEquals("IMG_20260618_124549.jpg", firstImage.name());
        assertEquals(1L, firstImage.cameraId());
        assertEquals(134, firstImage.observations().size());
        assertEquals(new ColmapImageObservation(3141.588623046875, 1104.3594970703125, 1157L),
                firstImage.observations().get(0));

        Image2DColmap lastImage = images.get(images.size() - 1);
        assertEquals(10L, lastImage.imageId());
        assertEquals("IMG_20260618_124823.jpg", lastImage.name());
        assertEquals(778, lastImage.observations().size());
    }

    @Test
    void shouldReadPoints3DFile() throws IOException {
        List<ColmapPoint3D> points = reader.readPoints3D(POINTS3D_PATH);

        assertEquals(979, points.size());

        ColmapPoint3D firstPoint = points.get(0);
        assertEquals(1L, firstPoint.point3DId());
        assertEquals(-4.4044685233589203, firstPoint.x());
        assertEquals(27, firstPoint.red());
        assertEquals(3, firstPoint.track().size());
        assertEquals(new ColmapTrackElement(1L, 832L), firstPoint.track().get(0));

        ColmapPoint3D lastPoint = points.get(points.size() - 1);
        assertEquals(1608L, lastPoint.point3DId());
        assertEquals(2, lastPoint.track().size());
        assertEquals(new ColmapTrackElement(3L, 10951L), lastPoint.track().get(lastPoint.track().size() - 1));
    }

    @Test
    void shouldRejectMissingFile() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reader.readImages2D(Path.of("data", "BG", "sparse", "0", "missing-images.txt")));

        assertTrue(exception.getMessage().contains("COLMAP file not found"));
    }
}
