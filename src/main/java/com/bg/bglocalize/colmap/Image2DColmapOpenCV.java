package com.bg.bglocalize.colmap;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.bg.bglocalize.features.FeatureAlgorithm;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public final class Image2DColmapOpenCV {  

    private final Image2DColmap imageColmap;
    private final Image2DOpenCV imageOpenCV;
   
    private final String imageName;

    public Image2DColmapOpenCV(
            Image2DColmap colmapImage,
            String imageName,
            FeatureAlgorithm algorithm,
            List<ColmapImageObservationOpenCV> observationFeatures) {
        this.imageColmap = Objects.requireNonNull(colmapImage, "colmapImage must not be null");
        this.imageName = Objects.requireNonNull(imageName, "imageName must not be null");
        this.imageOpenCV = new Image2DOpenCV(imageName, algorithm, observationFeatures);
    }

    public Image2DColmap getImageColmap() {
        return imageColmap;
    }

    public String getImageName() {
        return imageName;
    }

    public FeatureAlgorithm getAlgorithm() {
        return imageOpenCV.getAlgorithm();
    }

    public List<ColmapImageObservationOpenCV> getObservationFeatures() {
        return imageOpenCV.getObservationFeatures();
    }

    public void releaseDescriptors() {
        for (ColmapImageObservationOpenCV observationFeature : getObservationFeatures()) {
            observationFeature.getDescriptor().release();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Image2DColmapOpenCV other)) {
            return false;
        }
        return Objects.equals(imageColmap, other.imageColmap)
                && Objects.equals(imageName, other.imageName)
                && Objects.equals(getAlgorithm(), other.getAlgorithm())
                && observationFeaturesEquals(getObservationFeatures(), other.getObservationFeatures());
    }



	@Override
	public String toString() {
		return "ColmapImageOpenCV [colmapImage=" + imageColmap.imageId() + ", imageName=" + imageName + ", algorithm=" + getAlgorithm()
				+ ", observationFeatures.size=" + getObservationFeatures().size()+" colmapImage.observations.size= " +imageColmap.observations().size()+ "]";
	}

	public List<ColmapImageObservationOpenCV> getObservationFeaturesOpenCv() {
		return getObservationFeatures();
	}


	@Override
	public int hashCode() {
		return Objects.hash(imageColmap, imageName, getAlgorithm(), observationFeaturesHashCode(getObservationFeatures()));
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
        return descriptorEquals(left.getDescriptor(), right.getDescriptor());
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
                descriptorHashCode(observationFeature.getDescriptor()));
    }

    private static boolean descriptorEquals(Mat left, Mat right) {
        if (left.rows() != right.rows() || left.cols() != right.cols() || left.type() != right.type()) {
            return false;
        }

        int elementCount = descriptorElementCount(left);
        return switch (CvType.depth(left.type())) {
            case CvType.CV_8U, CvType.CV_8S -> {
                byte[] leftData = new byte[elementCount];
                byte[] rightData = new byte[elementCount];
                left.get(0, 0, leftData);
                right.get(0, 0, rightData);
                yield Arrays.equals(leftData, rightData);
            }
            case CvType.CV_16U, CvType.CV_16S -> {
                short[] leftData = new short[elementCount];
                short[] rightData = new short[elementCount];
                left.get(0, 0, leftData);
                right.get(0, 0, rightData);
                yield Arrays.equals(leftData, rightData);
            }
            case CvType.CV_32S -> {
                int[] leftData = new int[elementCount];
                int[] rightData = new int[elementCount];
                left.get(0, 0, leftData);
                right.get(0, 0, rightData);
                yield Arrays.equals(leftData, rightData);
            }
            case CvType.CV_32F -> {
                float[] leftData = new float[elementCount];
                float[] rightData = new float[elementCount];
                left.get(0, 0, leftData);
                right.get(0, 0, rightData);
                yield Arrays.equals(leftData, rightData);
            }
            case CvType.CV_64F -> {
                double[] leftData = new double[elementCount];
                double[] rightData = new double[elementCount];
                left.get(0, 0, leftData);
                right.get(0, 0, rightData);
                yield Arrays.equals(leftData, rightData);
            }
            default -> throw new IllegalArgumentException("Unsupported descriptor type: " + left.type());
        };
    } 

    private static int descriptorHashCode(Mat descriptor) {
        int elementCount = descriptorElementCount(descriptor);
        return switch (CvType.depth(descriptor.type())) {
            case CvType.CV_8U, CvType.CV_8S -> {
                byte[] data = new byte[elementCount];
                descriptor.get(0, 0, data);
                yield Objects.hash(descriptor.rows(), descriptor.cols(), descriptor.type(), Arrays.hashCode(data));
            }
            case CvType.CV_16U, CvType.CV_16S -> {
                short[] data = new short[elementCount];
                descriptor.get(0, 0, data);
                yield Objects.hash(descriptor.rows(), descriptor.cols(), descriptor.type(), Arrays.hashCode(data));
            }
            case CvType.CV_32S -> {
                int[] data = new int[elementCount];
                descriptor.get(0, 0, data);
                yield Objects.hash(descriptor.rows(), descriptor.cols(), descriptor.type(), Arrays.hashCode(data));
            }
            case CvType.CV_32F -> {
                float[] data = new float[elementCount];
                descriptor.get(0, 0, data);
                yield Objects.hash(descriptor.rows(), descriptor.cols(), descriptor.type(), Arrays.hashCode(data));
            }
            case CvType.CV_64F -> {
                double[] data = new double[elementCount];
                descriptor.get(0, 0, data);
                yield Objects.hash(descriptor.rows(), descriptor.cols(), descriptor.type(), Arrays.hashCode(data));
            }
            default -> throw new IllegalArgumentException("Unsupported descriptor type: " + descriptor.type());
        };
    }

    private static int descriptorElementCount(Mat descriptor) {
        return Math.toIntExact(descriptor.total() * descriptor.channels());
    }


}
