package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;

import com.bg.bglocalize.features.FeatureAlgorithm;

public final class ColmapImageOpenCV {

    private final ColmapImage colmapImage;
    private final String imageName;
    private final FeatureAlgorithm algorithm;
    private final List<ColmapImageObservationOpenCV> observationFeatures;

    public ColmapImageOpenCV(
            ColmapImage colmapImage,
            String imageName,
            FeatureAlgorithm algorithm,
            List<ColmapImageObservationOpenCV> observationFeatures) {
        this.colmapImage = Objects.requireNonNull(colmapImage, "colmapImage must not be null");
        this.imageName = Objects.requireNonNull(imageName, "imageName must not be null");
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
        this.observationFeatures = List.copyOf(
                Objects.requireNonNull(observationFeatures, "observationFeatures must not be null"));
    }

    public ColmapImage getColmapImage() {
        return colmapImage;
    }

    public String getImageName() {
        return imageName;
    }

    public FeatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    public List<ColmapImageObservationOpenCV> getObservationFeatures() {
        return observationFeatures;
    }

    public void releaseDescriptors() {
        for (ColmapImageObservationOpenCV observationFeature : observationFeatures) {
            observationFeature.getDescriptor().release();
        }
    }
}
