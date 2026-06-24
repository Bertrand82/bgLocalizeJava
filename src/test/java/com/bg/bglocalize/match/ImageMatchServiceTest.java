package com.bg.bglocalize.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.DMatch;

import com.bg.bglocalize.colmap.ColmapImage2D;
import com.bg.bglocalize.colmap.ColmapImageObservation;
import com.bg.bglocalize.colmap.ColmapImage2DOpenCV;
import com.bg.bglocalize.colmap.ColmapImageOpenCVFactory;
import com.bg.bglocalize.colmap.ColmapTextModelReader;
import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.features.OpenCvFeatureExtractor;
import com.bg.bglocalize.opencv.OpenCvInitializer;

class ImageMatchServiceTest {

	private static final File DATABASE_FILE = new File("data/BG/database.db");
	private static final File IMAGES_DIRECTORY = new File("data/BG/images");
	private static final File IMAGES_TXT = new File("data/BG/sparse/0/images.txt");
	private static final File QUERY_IMAGE_1 = new File("data/BG.jpg");
	private static final File QUERY_IMAGE_2 = new File("data/BG_1.jpg");

	private static final FeatureAlgorithm ALGORITHM = FeatureAlgorithm.SIFT;

	private static List<ColmapImage2DOpenCV> colmapImages;
	private static FeatureExtractionResult queryResult1;
	private static FeatureExtractionResult queryResult2;
	private static ColmapImageOpenCVFactory factory;

	private static final int MAX_OBSERVATIONS_PER_IMAGE = 10;

	@BeforeAll
	static void setUp() throws IOException, SQLException {
		System.out.println("setUp  OpenCvInitializer.initialize");
		OpenCvInitializer.initialize();

		OpenCvFeatureExtractor extractor = new OpenCvFeatureExtractor();
		System.out.println("setUp  extract  2 images sources ");
		queryResult1 = extractor.extract(QUERY_IMAGE_1.getPath(), ALGORITHM);
		queryResult2 = extractor.extract(QUERY_IMAGE_2.getPath(), ALGORITHM);
		System.out.println("Lecture images2D colmap start");
		ColmapTextModelReader reader = new ColmapTextModelReader();
		List<ColmapImage2D> images = reader.readImages2D(IMAGES_TXT.toPath());
		System.out.println("Lecture images2D colmap done images.size "+images.size());
		System.out.println("Lecture images2D colmap  openCV");
		factory = new ColmapImageOpenCVFactory(DATABASE_FILE, IMAGES_DIRECTORY);
		System.out.println("Lecture images2D colmap  conversion openCv start");
		colmapImages = factory.createAll(images, ALGORITHM);
		System.out.println("Lecture images2D colmap  conversion openCv done");
	}

	@AfterAll
	static void tearDown() throws Exception {
		if (factory != null) {
			factory.close();
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
	void shouldLoadColmapImages() {
		assertFalse(colmapImages.isEmpty());
	}

	@Test
	void shouldMatchQuery_1_ImageAgainstAllColmapImages() {
		shouldMatchQueryImageAgainstAllColmapImages(queryResult1,"BG_1",colmapImages);
	}

	@Test
	void shouldMatchQuery_2_ImageAgainstAllColmapImages() {
		shouldMatchQueryImageAgainstAllColmapImages(queryResult2,"BG_2",colmapImages);
	}

	void shouldMatchQueryImageAgainstAllColmapImages(FeatureExtractionResult queryResult1, String comment, List<ColmapImage2DOpenCV> colmapImages ) {
		ImageMatchService service = new ImageMatchService();

		List<FeatureMatchResult> results = service.matchAll(queryResult1, colmapImages);

		assertEquals(colmapImages.size(), results.size());

		// 1) Sanity: all results valid
		for (FeatureMatchResult result : results) {
			List<DMatch> listTopN = UtilsMatchFiltering.topN(result.getMatches(), 30);
			double averageTopN =listTopN.stream().mapToDouble(m -> m.distance).average().orElse(Double.NaN);;
			System.out.println(comment+" vs " + result.getTarget().getImageName()+"  features.size " +result.getTarget().getObservationFeatures().size()+ " -> " + result.getMatchCount()+ "  averageTopN "+averageTopN + " matches");
			System.out.println("averageTopN  "+averageTopN);
		}

		// 2) Critical: match counts should not all be identical across different target
		// images
		long distinctCounts = results.stream().map(FeatureMatchResult::getMatchCount).distinct().count();
         System.out.println("distinctCounts "+distinctCounts);
         
		// assertTrue(distinctCounts > 1,"All match counts are identical; likely
		// counting raw descriptors or reusing state.");
	}

	
	@Test
	void shouldMatchSingleQueryAgainstFirstColmapImage() {
		ImageMatchService service = new ImageMatchService();
		ColmapImage2DOpenCV firstTarget = colmapImages.get(0);

		FeatureMatchResult result = service.match(queryResult1, firstTarget);

		assertNotNull(result);
		assertEquals(queryResult1, result.getQuery());
		assertEquals(firstTarget, result.getTarget());
		assertNotNull(result.getMatches());
		assertTrue(result.getMatchCount() >= 0);
	}

	@Test
	void shouldRejectMismatchedAlgorithms() throws IOException {
		OpenCvFeatureExtractor extractor = new OpenCvFeatureExtractor();
		FeatureExtractionResult orbResult = extractor.extract(QUERY_IMAGE_1.getPath(), FeatureAlgorithm.ORB);
		ImageMatchService service = new ImageMatchService();
		ColmapImage2DOpenCV siftTarget = colmapImages.get(0);

		try {
			assertThrows(IllegalArgumentException.class, () -> service.match(orbResult, siftTarget));
		} finally {
			orbResult.getKeypoints().release();
			orbResult.getDescriptors().release();
		}
	}

	private static ColmapImage2D limitObservations(ColmapImage2D image, int maxObservations) {
		List<ColmapImageObservation> limited = image.observations().stream().limit(maxObservations).toList();
		return new ColmapImage2D(image.imageId(), image.qw(), image.qx(), image.qy(), image.qz(), image.tx(), image.ty(),
				image.tz(), image.cameraId(), image.name(), limited);
	}
}
