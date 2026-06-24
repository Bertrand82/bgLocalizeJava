package com.bg.bglocalize.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.features2d.DescriptorMatcher;

import com.bg.bglocalize.colmap.ColmapImageObservationOpenCV;
import com.bg.bglocalize.colmap.Image2DColmapOpenCV;
import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.opencv.OpenCvInitializer;

public final class ImageMatchService {

    public ImageMatchService() {
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
        if (!query.getAlgorithm().equals(target.getAlgorithm())) {
            throw new IllegalArgumentException(
                    "Algorithm mismatch: query uses " + query.getAlgorithm()
                            + " but target uses " + target.getAlgorithm());
        }
        OpenCvInitializer.initialize();

        Mat targetDescriptors = buildDescriptorMatrix(target);
        try {
            if (query.getDescriptors().empty() || targetDescriptors.empty()) {
                return new FeatureMatchResult(query, target, List.of());
            }

            DescriptorMatcher matcher = createMatcher(query.getAlgorithm());
            MatOfDMatch matOfDMatch = new MatOfDMatch();
            try {
                matcher.match(query.getDescriptors(), targetDescriptors, matOfDMatch);
                List<DMatch> matches = matOfDMatch.toList();
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

    private static Mat buildDescriptorMatrix(Image2DColmapOpenCV image) {
        List<ColmapImageObservationOpenCV> observations = image.getObservationFeatures();
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
