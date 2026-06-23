package com.bg.bglocalize.colmap;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
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

public final class ColmapImageOpenCVFactory implements Closeable {

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
        OpenCvInitializer.initialize();
    }

    public ColmapImageOpenCV create(ColmapImage2D colmapImage, FeatureAlgorithm algorithm) throws SQLException {
        Objects.requireNonNull(colmapImage, "colmapImage must not be null");
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        return create(colmapImage, algorithm, algorithm.createExtractor());
    }

    public List<ColmapImageOpenCV> createAll(List<ColmapImage2D> colmapImages, FeatureAlgorithm algorithm) throws SQLException {
        List<ColmapImageOpenCV> results = new ArrayList<>(colmapImages.size());
        Feature2D extractor = algorithm.createExtractor();

        for (ColmapImage2D colmapImage : colmapImages) {
        	long timeStart = System.currentTimeMillis();
        	System.out.print("createAll ProcessOpenCv : "+colmapImage.imageId()+"  observations.size :"+colmapImage.observations().size());
        	ColmapImageOpenCV cio = create(colmapImage, algorithm, extractor);
        	System.out.println(" done  :"+getDeltaTime(timeStart)+"  "+cio);
            results.add(cio);
        }
        return results;
    }

    @Override
    public void close() throws IOException {
        databaseReader.close();
    }

    private ColmapImageOpenCV create(ColmapImage2D colmapImage, FeatureAlgorithm algorithm, Feature2D extractor)
            throws SQLException {
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
                    extractor);
            return new ColmapImageOpenCV(colmapImage, imageName, algorithm, observationFeatures);
        } finally {
            loadedImage.getImage().release();
        }
    }

    private String getDeltaTime(long timeStart) {
		
		return( ((System.currentTimeMillis()-timeStart)/1000)+" secondes ");
	}

	private List<ColmapImageObservationOpenCV> extractObservationFeatures(
            LoadedImage image,
            List<ColmapImageObservation> observations,
            Feature2D extractor) {

        Mat grayscale = new Mat();
        try {
            Mat sourceImage = image.getImage();
            if (sourceImage.channels() > 1) {
                Imgproc.cvtColor(sourceImage, grayscale, Imgproc.COLOR_BGR2GRAY);
            } else {
                sourceImage.copyTo(grayscale);
            }

            for (ColmapImageObservation observation : observations) {
                validateObservation(observation, image);
            }

            // Build all keypoints at once for a single batch compute() call per image
            KeyPoint[] inputKps = new KeyPoint[observations.size()];
            for (int i = 0; i < observations.size(); i++) {
                ColmapImageObservation obs = observations.get(i);
                inputKps[i] = new KeyPoint((float) obs.x(), (float) obs.y(), DEFAULT_KEYPOINT_SIZE);
            }

            MatOfKeyPoint keyPoints = new MatOfKeyPoint(inputKps);
            Mat allDescriptors = new Mat();
            try {
                extractor.compute(grayscale, keyPoints, allDescriptors);
                KeyPoint[] computedKps = keyPoints.toArray();

                List<ColmapImageObservationOpenCV> features = new ArrayList<>(observations.size());
                for (int i = 0; i < observations.size(); i++) {
                    // computedKps are in the same order as inputKps; some may be filtered at the tail
                    KeyPoint kp = (i < computedKps.length) ? computedKps[i] : inputKps[i];
                    Mat descriptor = (!allDescriptors.empty() && i < allDescriptors.rows())
                            ? allDescriptors.row(i).clone()
                            : new Mat();
                    features.add(new ColmapImageObservationOpenCV(observations.get(i), kp, descriptor));
                }
                return features;
            } finally {
                keyPoints.release();
                allDescriptors.release();
            }
        } finally {
            grayscale.release();
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
