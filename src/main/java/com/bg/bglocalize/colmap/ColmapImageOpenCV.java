package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;

import com.bg.bglocalize.features.FeatureAlgorithm;

public final class ColmapImageOpenCV {

    private final ColmapImage colmapImage;
    private final String imageName;
    private final FeatureAlgorithm algorithm;
    private final List<ColmapImageObservationOpenCV> observationFeaturesOpenCv;

    public ColmapImageOpenCV(
            ColmapImage colmapImage,
            String imageName,
            FeatureAlgorithm algorithm,
            List<ColmapImageObservationOpenCV> observationFeatures) {
        this.colmapImage = Objects.requireNonNull(colmapImage, "colmapImage must not be null");
        this.imageName = Objects.requireNonNull(imageName, "imageName must not be null");
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
        this.observationFeaturesOpenCv = observationFeatures;
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
        return observationFeaturesOpenCv;
    }

    public void releaseDescriptors() {
        for (ColmapImageObservationOpenCV observationFeature : observationFeaturesOpenCv) {
            observationFeature.getDescriptor().release();
        }
    }

	@Override
	public String toString() {
		return "ColmapImageOpenCV2 [colmapImage=" + colmapImage.imageId() + ", imageName=" + imageName + ", algorithm=" + algorithm
				+ ", observationFeatures.size=" + observationFeaturesOpenCv.size()+" colmapImage.observations.size= " +colmapImage.observations().size()+ "]";
	}
    
}
