package com.bg.bglocalize.image;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.bg.bglocalize.opencv.OpenCvInitializer;

public final class FilesystemImageLoader implements ImageLoader {

    @Override
    public LoadedImage load(String imagePath) {
        Objects.requireNonNull(imagePath, "imagePath must not be null");
         

        Path path = Path.of(imagePath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Image file not found: " + path);
        }

        Mat image = Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            throw new IllegalArgumentException("Unable to load image: " + path);
        }

        return new LoadedImage(path.getFileName().toString(), path.toString(), image.cols(), image.rows(), image);
    }
    
    public LoadedImage load(File imageFile) {
    	return load(imageFile.getPath());
    }
}
