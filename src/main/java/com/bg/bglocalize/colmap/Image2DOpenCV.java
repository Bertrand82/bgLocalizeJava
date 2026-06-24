package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;

import com.bg.bglocalize.features.FeatureAlgorithm;

public final class Image2DOpenCV {

    
    private final String imageName;
    private final FeatureAlgorithm algorithm;
    private final List<ColmapImageObservationOpenCV> observationFeaturesOpenCv;
    
    public Image2DOpenCV(
           String imageName,
            FeatureAlgorithm algorithm,
            List<ColmapImageObservationOpenCV> observationFeatures) {
        this.imageName = Objects.requireNonNull(imageName, "imageName must not be null");
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
        this.observationFeaturesOpenCv = observationFeatures;
    }

    public String getImageName() {
        return imageName;
    }

    public FeatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    public List<ColmapImageObservationOpenCV> getObservationFeatures() {
        return observationFeaturesOpenCv;
    }
}
