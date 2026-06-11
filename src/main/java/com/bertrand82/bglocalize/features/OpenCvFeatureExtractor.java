package com.bertrand82.bglocalize.features;

import java.util.Objects;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Feature2D;
import org.opencv.imgproc.Imgproc;

import com.bertrand82.bglocalize.image.FilesystemImageLoader;
import com.bertrand82.bglocalize.image.ImageLoader;
import com.bertrand82.bglocalize.image.LoadedImage;
import com.bertrand82.bglocalize.opencv.OpenCvInitializer;

public final class OpenCvFeatureExtractor implements FeatureExtractor {

    private final ImageLoader imageLoader;

    public OpenCvFeatureExtractor() {
        this(new FilesystemImageLoader());
    }

    public OpenCvFeatureExtractor(ImageLoader imageLoader) {
        this.imageLoader = Objects.requireNonNull(imageLoader, "imageLoader must not be null");
    }

    @Override
    public FeatureExtractionResult extract(String imagePath, FeatureAlgorithm algorithm) {
        return extract(imageLoader.load(imagePath), algorithm);
    }

    @Override
    public FeatureExtractionResult extract(LoadedImage image, FeatureAlgorithm algorithm) {
        Objects.requireNonNull(image, "image must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        OpenCvInitializer.initialize();

        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        Mat grayscale = new Mat();
        Mat mask = new Mat();
        Feature2D extractor = null;

        try {
            extractor = algorithm.createExtractor();
            Mat sourceImage = image.getImage();
            if (sourceImage.channels() > 1) {
                Imgproc.cvtColor(sourceImage, grayscale, Imgproc.COLOR_BGR2GRAY);
            } else {
                sourceImage.copyTo(grayscale);
            }

            extractor.detectAndCompute(grayscale, mask, keypoints, descriptors);
            return new FeatureExtractionResult(
                    image.getImageId(),
                    image.getImagePath(),
                    image.getWidth(),
                    image.getHeight(),
                    algorithm,
                    keypoints,
                    descriptors);
        } finally {
            if (extractor != null) {
                extractor.clear();
            }
            mask.release();
            grayscale.release();
        }
    }
}
