package com.bg.bglocalize.colmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.features.OpenCvFeatureExtractor;

public final class FactoryImage2DOpenCV {

    private static final long UNASSIGNED_POINT_3D_ID = -1L;

    private final OpenCvFeatureExtractor featureExtractor;

    public FactoryImage2DOpenCV() {
        this(new OpenCvFeatureExtractor());
    }

    public FactoryImage2DOpenCV(OpenCvFeatureExtractor featureExtractor) {
        this.featureExtractor = Objects.requireNonNull(featureExtractor, "featureExtractor must not be null");
    }

    public Image2DOpenCV create(File imageFile) {
        Objects.requireNonNull(imageFile, "imageFile must not be null");
        return create(imageFile.getPath(), FeatureAlgorithm.SIFT);
    }

    public Image2DOpenCV create(String imagePath) {
        return create(imagePath, FeatureAlgorithm.SIFT);
    }

    public Image2DOpenCV create(File imageFile, FeatureAlgorithm algorithm) {
        Objects.requireNonNull(imageFile, "imageFile must not be null");
        return create(imageFile.getPath(), algorithm);
    }

    public Image2DOpenCV create(String imagePath, FeatureAlgorithm algorithm) {
        Objects.requireNonNull(imagePath, "imagePath must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        FeatureExtractionResult extraction = featureExtractor.extract(imagePath, algorithm);
        try {
            List<ColmapImageObservationOpenCV> observations = toObservations(extraction);
            return new Image2DOpenCV(extraction.getImageId(), algorithm, observations);
        } finally {
            extraction.getKeypoints().release();
            extraction.getDescriptors().release();
        }
    }

    private static List<ColmapImageObservationOpenCV> toObservations(FeatureExtractionResult extraction) {
        KeyPoint[] keyPoints = extraction.getKeypoints().toArray();
        List<ColmapImageObservationOpenCV> observations = new ArrayList<>(keyPoints.length);
        Mat descriptors = extraction.getDescriptors();

        for (int i = 0; i < keyPoints.length; i++) {
            KeyPoint keyPoint = keyPoints[i];
            ColmapImageObservation observation = new ColmapImageObservation(
                    keyPoint.pt.x,
                    keyPoint.pt.y,
                    UNASSIGNED_POINT_3D_ID);
            Mat descriptor = descriptors.empty() ? new Mat() : descriptors.row(i).clone();
            observations.add(new ColmapImageObservationOpenCV(observation, keyPoint, descriptor));
        }
        return observations;
    }
}
