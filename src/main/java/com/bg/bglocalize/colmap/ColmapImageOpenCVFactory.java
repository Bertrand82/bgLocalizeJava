package com.bg.bglocalize.colmap;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Feature2D;
import org.opencv.imgproc.Imgproc;

import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.image.FilesystemImageLoader;
import com.bg.bglocalize.image.ImageLoader;
import com.bg.bglocalize.image.LoadedImage;
import com.bg.bglocalize.opencv.OpenCvInitializer;

public final class ColmapImageOpenCVFactory {

    private static final float DEFAULT_KEYPOINT_SIZE = 31.0f;

    private final ColmapDatabaseReader databaseReader;
    private final File imagesDirectory;
    private final ImageLoader imageLoader;

    public ColmapImageOpenCVFactory(File databaseFile, File imagesDirectory) {
        this(new ColmapDatabaseReader(databaseFile), imagesDirectory, new FilesystemImageLoader());
    }

    public ColmapImageOpenCVFactory(ColmapDatabaseReader databaseReader, File imagesDirectory, ImageLoader imageLoader) {
        this.databaseReader = Objects.requireNonNull(databaseReader, "databaseReader must not be null");
        this.imagesDirectory = Objects.requireNonNull(imagesDirectory, "imagesDirectory must not be null")
                .getAbsoluteFile();
        this.imageLoader = Objects.requireNonNull(imageLoader, "imageLoader must not be null");
        if (!this.imagesDirectory.isDirectory()) {
            throw new IllegalArgumentException("Images directory not found: " + this.imagesDirectory.getAbsolutePath());
        }
    }

    public ColmapImageOpenCV create(ColmapImage colmapImage, FeatureAlgorithm algorithm) throws SQLException {
        Objects.requireNonNull(colmapImage, "colmapImage must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");

        String imageName = databaseReader.findNameByImageId(colmapImage.imageId());
        if (imageName == null || imageName.isBlank()) {
            throw new IllegalArgumentException("Unable to find image name for COLMAP image_id=" + colmapImage.imageId());
        }

        File imageFile = new File(imagesDirectory, imageName);
        LoadedImage loadedImage = imageLoader.load(imageFile.getAbsolutePath());
        try {
            List<ColmapImageObservationOpenCV> observationFeatures = extractObservationFeatures(
                    loadedImage,
                    colmapImage.observations(),
                    algorithm);
            return new ColmapImageOpenCV(colmapImage, imageName, algorithm, observationFeatures);
        } finally {
            loadedImage.getImage().release();
        }
    }

    public List<ColmapImageOpenCV> createAll(List<ColmapImage> colmapImages, FeatureAlgorithm algorithm) throws SQLException {
        Objects.requireNonNull(colmapImages, "colmapImages must not be null");
        List<ColmapImageOpenCV> results = new ArrayList<>(colmapImages.size());
        for (ColmapImage colmapImage : colmapImages) {
            results.add(create(colmapImage, algorithm));
        }
        return List.copyOf(results);
    }

    private List<ColmapImageObservationOpenCV> extractObservationFeatures(
            LoadedImage image,
            List<ColmapImageObservation> observations,
            FeatureAlgorithm algorithm) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(observations, "observations must not be null");
        OpenCvInitializer.initialize();

        Mat grayscale = new Mat();
        Feature2D extractor = algorithm.createExtractor();

        try {
            Mat sourceImage = image.getImage();
            if (sourceImage.channels() > 1) {
                Imgproc.cvtColor(sourceImage, grayscale, Imgproc.COLOR_BGR2GRAY);
            } else {
                sourceImage.copyTo(grayscale);
            }

            List<ColmapImageObservationOpenCV> features = new ArrayList<>(observations.size());
            for (ColmapImageObservation observation : observations) {
                validateObservation(observation, image);
                features.add(extractObservationFeature(grayscale, extractor, observation));
            }
            return List.copyOf(features);
        } finally {
            grayscale.release();
        }
    }

    private static ColmapImageObservationOpenCV extractObservationFeature(
            Mat grayscale,
            Feature2D extractor,
            ColmapImageObservation observation) {
        KeyPoint inputKeyPoint = new KeyPoint((float) observation.x(), (float) observation.y(), DEFAULT_KEYPOINT_SIZE);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint(inputKeyPoint);
        Mat descriptor = new Mat();

        try {
            extractor.compute(grayscale, keyPoints, descriptor);
            KeyPoint[] computedKeyPoints = keyPoints.toArray();
            KeyPoint featureKeyPoint = computedKeyPoints.length == 0 ? inputKeyPoint : computedKeyPoints[0];
            Mat descriptorCopy = descriptor.empty() ? new Mat() : descriptor.clone();
            return new ColmapImageObservationOpenCV(observation, featureKeyPoint, descriptorCopy);
        } finally {
            keyPoints.release();
            descriptor.release();
        }
    }

    private static void validateObservation(ColmapImageObservation observation, LoadedImage image) {
        Objects.requireNonNull(observation, "observation must not be null");
        if (observation.x() < 0 || observation.y() < 0) {
            throw new IllegalArgumentException("Observation coordinates must be non-negative: " + observation);
        }
        if (observation.x() >= image.getWidth() || observation.y() >= image.getHeight()) {
            throw new IllegalArgumentException(
                    "Observation is outside image bounds: " + observation + ", image=" + image.getImagePath());
        }
    }
}
