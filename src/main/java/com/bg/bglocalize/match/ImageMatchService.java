package com.bg.bglocalize.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bg.bglocalize.colmap.ColmapImageObservation;
import com.bg.bglocalize.colmap.ColmapImageObservationOpenCV;
import com.bg.bglocalize.colmap.Image2DColmap;
import com.bg.bglocalize.colmap.Image2DColmapOpenCV;
import com.bg.bglocalize.colmap.Image2DOpenCV;
import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.opencv.OpenCvInitializer;

public final class ImageMatchService {

    private static final Logger logger = LoggerFactory.getLogger(ImageMatchService.class);

    public ImageMatchService() {
    }

    /**
     * Matches two {@link Image2DOpenCV} instances using their precomputed descriptors.
     * <p>
     * The returned result reuses the existing {@link FeatureMatchResult} contract by
     * creating an internal {@link FeatureExtractionResult} view for the source image.
     *
     * @param source source image with OpenCV keypoints and descriptors
     * @param target target image with OpenCV keypoints and descriptors
     * @return the match result containing the filtered descriptor matches
     */
    public FeatureMatchResult match(Image2DOpenCV source, Image2DOpenCV target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        validateAlgorithms(source.getAlgorithm(), target.getAlgorithm(), source.getImageName(), target.getImageName());

        logger.info("Matching source image {} against target image {} using {}", source.getImageName(),
                target.getImageName(), source.getAlgorithm());
        FeatureExtractionResult query = toFeatureExtractionResult(source);
        try {
            return match(query, toMatchTarget(target));
        } catch (RuntimeException exception) {
            query.getKeypoints().release();
            query.getDescriptors().release();
            throw exception;
        }
    }

    /**
     * Matches a single query {@link FeatureExtractionResult} against a single
     * {@link Image2DColmapOpenCV} target. Both must have been produced with the same
     * {@link FeatureAlgorithm}.
     *
     * @param query  feature extraction result for the query image
     * @param target COLMAP image with OpenCV descriptors
     * @return the match result containing all raw descriptor matches
     */
    public FeatureMatchResult match(FeatureExtractionResult query, Image2DColmapOpenCV target) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(target, "target must not be null");
        validateAlgorithms(query.getAlgorithm(), target.getAlgorithm(), query.getImageId(), target.getImageName());
        OpenCvInitializer.initialize();

        Mat targetDescriptors = buildDescriptorMatrix(target);
        try {
            if (query.getDescriptors().empty() || targetDescriptors.empty()) {
                logger.info("No correspondence between {} and {} because one image has no descriptors",
                        query.getImageId(), target.getImageName());
                return new FeatureMatchResult(query, target, List.of());
            }

            DescriptorMatcher matcher = createMatcher(query.getAlgorithm());
            MatOfDMatch matOfDMatch = new MatOfDMatch();
            try {
                matcher.match(query.getDescriptors(), targetDescriptors, matOfDMatch);
                List<DMatch> matches = matOfDMatch.toList();
                logger.info("Matched {} raw descriptors between {} and {}", matches.size(), query.getImageId(),
                        target.getImageName());
                return new FeatureMatchResult(query, target, matches);
            } finally {
                matOfDMatch.release();
            }
        } finally {
            targetDescriptors.release();
        }
    }

    /**
     * Matches a query {@link FeatureExtractionResult} against each
     * {@link Image2DColmapOpenCV} in the provided list.
     *
     * @param query   feature extraction result for the query image
     * @param targets list of COLMAP images with OpenCV descriptors
     * @return a list of match results, one per target, in the same order
     */
    public List<FeatureMatchResult> matchAll(FeatureExtractionResult query, List<Image2DColmapOpenCV> targets) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(targets, "targets must not be null");
        List<FeatureMatchResult> results = new ArrayList<>(targets.size());
        for (Image2DColmapOpenCV target : targets) {
            results.add(match(query, target));
        }
        return List.copyOf(results);
    }

    private static FeatureExtractionResult toFeatureExtractionResult(Image2DOpenCV image) {
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        List<KeyPoint> keyPointList = image.getObservationFeatures().stream()
                .map(ColmapImageObservationOpenCV::getKeyPoint)
                .toList();
        keypoints.fromList(keyPointList);
        Mat descriptors = buildDescriptorMatrix(image);
        return new FeatureExtractionResult(
                image.getImageName(),
                image.getImageName(),
                0,
                0,
                image.getAlgorithm(),
                keypoints,
                descriptors);
    }

    private static Image2DColmapOpenCV toMatchTarget(Image2DOpenCV image) {
        List<ColmapImageObservation> observations = image.getObservationFeatures().stream()
                .map(ColmapImageObservationOpenCV::getObservation)
                .toList();
        Image2DColmap syntheticColmapImage = new Image2DColmap(0L, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0L,
                image.getImageName(), observations);
        return new Image2DColmapOpenCV(syntheticColmapImage, image.getImageName(), image.getAlgorithm(),
                image.getObservationFeatures());
    }

    private static Mat buildDescriptorMatrix(Image2DColmapOpenCV image) {
        return buildDescriptorMatrix(image.getObservationFeatures());
    }

    private static Mat buildDescriptorMatrix(Image2DOpenCV image) {
        return buildDescriptorMatrix(image.getObservationFeatures());
    }

    private static Mat buildDescriptorMatrix(List<ColmapImageObservationOpenCV> observations) {
        List<Mat> descriptorRows = new ArrayList<>(observations.size());
        for (ColmapImageObservationOpenCV obs : observations) {
            if (obs.hasDescriptor()) {
                descriptorRows.add(obs.getDescriptor());
            }
        }
        if (descriptorRows.isEmpty()) {
            return new Mat();
        }
        Mat combined = new Mat();
        org.opencv.core.Core.vconcat(descriptorRows, combined);
        return combined;
    }

    private static void validateAlgorithms(FeatureAlgorithm queryAlgorithm, FeatureAlgorithm targetAlgorithm,
            String queryLabel, String targetLabel) {
        if (!queryAlgorithm.equals(targetAlgorithm)) {
            throw new IllegalArgumentException(
                    "Algorithm mismatch: query " + queryLabel + " uses " + queryAlgorithm
                            + " but target " + targetLabel + " uses " + targetAlgorithm);
        }
    }

    private static DescriptorMatcher createMatcher(FeatureAlgorithm algorithm) {
        switch (algorithm) {
            case ORB:
            case BRISK:
            case AKAZE:
                return DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
            case SIFT:
            default:
                return DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        }
    }
}
