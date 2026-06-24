package com.bg.bglocalize.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.bg.bglocalize.colmap.Image2DColmapOpenCV;
import com.bg.bglocalize.colmap.ColmapImageOpenCvFactoryText;
import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.features.OpenCvFeatureExtractor;
import com.bg.bglocalize.opencv.OpenCvInitializer;

class ImageMatchServiceFromTextTest {

    private static final File QUERY_IMAGE_1 = new File("data/BG.jpg");
    private static final File QUERY_IMAGE_2 = new File("data/BG_1.jpg");
    private static final Path IMAGES2D_COLMAP_OPENCV_TXT =
            Path.of("data/BG/sparse/0/images2DColmapOpenCV.txt");

    private static final FeatureAlgorithm ALGORITHM = FeatureAlgorithm.SIFT;

    private static List<Image2DColmapOpenCV> colmapImages;
    private static FeatureExtractionResult queryResult1;
    private static FeatureExtractionResult queryResult2;

    @BeforeAll
    static void setUp() throws IOException {
        OpenCvInitializer.initialize();

        OpenCvFeatureExtractor extractor = new OpenCvFeatureExtractor();

        System.out.println("setUp  extract  2 query images");
        queryResult1 = extractor.extract(QUERY_IMAGE_1.getPath(), ALGORITHM);
        queryResult2 = extractor.extract(QUERY_IMAGE_2.getPath(), ALGORITHM);

        System.out.println("setUp  read ColmapImage2DOpenCV from text file");
        ColmapImageOpenCvFactoryText factoryText = new ColmapImageOpenCvFactoryText();
        colmapImages = factoryText.read(IMAGES2D_COLMAP_OPENCV_TXT);
        System.out.println("setUp  loaded " + colmapImages.size() + " ColmapImage2DOpenCV from text file");
    }

    @AfterAll
    static void tearDown() {
        if (queryResult1 != null) {
            queryResult1.getKeypoints().release();
            queryResult1.getDescriptors().release();
        }
        if (queryResult2 != null) {
            queryResult2.getKeypoints().release();
            queryResult2.getDescriptors().release();
        }
        if (colmapImages != null) {
            colmapImages.forEach(Image2DColmapOpenCV::releaseDescriptors);
        }
    }

    @Test
    void shouldLoadQueryImages() {
        assertNotNull(queryResult1);
        assertFalse(queryResult1.getDescriptors().empty());
        assertTrue(queryResult1.getKeypointCount() > 0);

        assertNotNull(queryResult2);
        assertFalse(queryResult2.getDescriptors().empty());
        assertTrue(queryResult2.getKeypointCount() > 0);
    }

    @Test
    void shouldLoadColmapImagesFromTextFile() {
        assertFalse(colmapImages.isEmpty());
        for (Image2DColmapOpenCV image : colmapImages) {
            assertNotNull(image.getImageName());
            assertEquals(ALGORITHM, image.getAlgorithm());
        }
    }

    @Test
    void shouldMatchBGImageAgainstAllColmapImages() {
        matchAndPrint(queryResult1, "BG", colmapImages);
    }

    @Test
    void shouldMatchBG1ImageAgainstAllColmapImages() {
        matchAndPrint(queryResult2, "BG_1", colmapImages);
    }

    private static void matchAndPrint(FeatureExtractionResult query, String label,
            List<Image2DColmapOpenCV> targets) {
        ImageMatchService service = new ImageMatchService();

        List<FeatureMatchResult> results = service.matchAll(query, targets);

        assertEquals(targets.size(), results.size());

        for (FeatureMatchResult matchResult : results) {
            assertNotNull(matchResult.getTarget());
            assertNotNull(matchResult.getMatches());
            assertTrue(matchResult.getMatchCount() >= 0);
            System.out.println(label + " vs " + matchResult.getTarget().getImageName()
                    + "  features.size " + matchResult.getTarget().getObservationFeatures().size()
                    + " -> " + matchResult.getMatchCount() + " matches "+matchResult);
        }
    }
}
