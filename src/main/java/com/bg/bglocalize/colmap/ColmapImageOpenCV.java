package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;

import com.bg.bglocalize.features.FeatureAlgorithm;

public final class ColmapImageOpenCV {

    private final ColmapImage2D colmapImage;
    private final String imageName;
    private final FeatureAlgorithm algorithm;
    private final List<ColmapImageObservationOpenCV> observationFeaturesOpenCv;

    public ColmapImageOpenCV(
            ColmapImage2D colmapImage,
            String imageName,
            FeatureAlgorithm algorithm,
            List<ColmapImageObservationOpenCV> observationFeatures) {
        this.colmapImage = Objects.requireNonNull(colmapImage, "colmapImage must not be null");
        this.imageName = Objects.requireNonNull(imageName, "imageName must not be null");
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
        this.observationFeaturesOpenCv = observationFeatures;
    }

    public ColmapImage2D getColmapImage() {
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ColmapImageOpenCV other)) {
            return false;
        }
        return Objects.equals(colmapImage, other.colmapImage)
                && Objects.equals(imageName, other.imageName)
                && algorithm == other.algorithm
                && observationFeaturesEquals(observationFeaturesOpenCv, other.observationFeaturesOpenCv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colmapImage, imageName, algorithm, observationFeaturesHashCode(observationFeaturesOpenCv));
    }

	@Override
	public String toString() {
		return "ColmapImageOpenCV2 [colmapImage=" + colmapImage.imageId() + ", imageName=" + imageName + ", algorithm=" + algorithm
				+ ", observationFeatures.size=" + observationFeaturesOpenCv.size()+" colmapImage.observations.size= " +colmapImage.observations().size()+ "]";
	}

	public List<ColmapImageObservationOpenCV> getObservationFeaturesOpenCv() {
		return observationFeaturesOpenCv;
	}

    private static boolean observationFeaturesEquals(List<ColmapImageObservationOpenCV> left,
            List<ColmapImageObservationOpenCV> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); i++) {
            if (!observationFeatureEquals(left.get(i), right.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean observationFeatureEquals(ColmapImageObservationOpenCV left,
            ColmapImageObservationOpenCV right) {
        return Objects.equals(left.getObservation(), right.getObservation())
                && keyPointEquals(left, right)
                && descriptorEquals(left, right);
    }

    private static boolean keyPointEquals(ColmapImageObservationOpenCV left, ColmapImageObservationOpenCV right) {
        return Float.compare((float) left.getKeyPoint().pt.x, (float) right.getKeyPoint().pt.x) == 0
                && Float.compare((float) left.getKeyPoint().pt.y, (float) right.getKeyPoint().pt.y) == 0
                && Float.compare(left.getKeyPoint().size, right.getKeyPoint().size) == 0
                && Float.compare(left.getKeyPoint().angle, right.getKeyPoint().angle) == 0
                && Float.compare(left.getKeyPoint().response, right.getKeyPoint().response) == 0
                && left.getKeyPoint().octave == right.getKeyPoint().octave
                && left.getKeyPoint().class_id == right.getKeyPoint().class_id;
    }

    private static boolean descriptorEquals(ColmapImageObservationOpenCV left, ColmapImageObservationOpenCV right) {
        return left.getDescriptor().rows() == right.getDescriptor().rows()
                && left.getDescriptor().cols() == right.getDescriptor().cols()
                && left.getDescriptor().type() == right.getDescriptor().type()
                && Objects.equals(left.getDescriptor().dump(), right.getDescriptor().dump());
    }

    private static int observationFeaturesHashCode(List<ColmapImageObservationOpenCV> observationFeatures) {
        int result = 1;
        for (ColmapImageObservationOpenCV observationFeature : observationFeatures) {
            result = 31 * result + observationFeatureHashCode(observationFeature);
        }
        return result;
    }

    private static int observationFeatureHashCode(ColmapImageObservationOpenCV observationFeature) {
        return Objects.hash(
                observationFeature.getObservation(),
                Float.floatToIntBits((float) observationFeature.getKeyPoint().pt.x),
                Float.floatToIntBits((float) observationFeature.getKeyPoint().pt.y),
                Float.floatToIntBits(observationFeature.getKeyPoint().size),
                Float.floatToIntBits(observationFeature.getKeyPoint().angle),
                Float.floatToIntBits(observationFeature.getKeyPoint().response),
                observationFeature.getKeyPoint().octave,
                observationFeature.getKeyPoint().class_id,
                observationFeature.getDescriptor().rows(),
                observationFeature.getDescriptor().cols(),
                observationFeature.getDescriptor().type(),
                observationFeature.getDescriptor().dump());
    }
    
    
    
}
