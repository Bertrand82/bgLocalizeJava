package com.bertrand82.bglocalize.features;

import java.util.Objects;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

public final class FeatureExtractionResult {

    private final String imageId;
    private final String imagePath;
    private final int width;
    private final int height;
    private final FeatureAlgorithm algorithm;
    private final MatOfKeyPoint keypoints;
    private final Mat descriptors;
    private final int keypointCount;
    private final int descriptorMatType;

    public FeatureExtractionResult(
            String imageId,
            String imagePath,
            int width,
            int height,
            FeatureAlgorithm algorithm,
            MatOfKeyPoint keypoints,
            Mat descriptors) {
        this.imageId = Objects.requireNonNull(imageId, "imageId must not be null");
        this.imagePath = Objects.requireNonNull(imagePath, "imagePath must not be null");
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
        this.keypoints = Objects.requireNonNull(keypoints, "keypoints must not be null");
        this.descriptors = Objects.requireNonNull(descriptors, "descriptors must not be null");
        this.width = width;
        this.height = height;
        this.keypointCount = Math.toIntExact(keypoints.total());
        this.descriptorMatType = descriptors.type();
    }

    public String getImageId() {
        return imageId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public FeatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    public MatOfKeyPoint getKeypoints() {
        return keypoints;
    }

    public Mat getDescriptors() {
        return descriptors;
    }

    public int getKeypointCount() {
        return keypointCount;
    }

    public int getDescriptorMatType() {
        return descriptorMatType;
    }
}
