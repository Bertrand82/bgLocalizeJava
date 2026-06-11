package com.bertrand82.bglocalize.image;

import java.util.Objects;

import org.opencv.core.Mat;

public final class LoadedImage {

    private final String imageId;
    private final String imagePath;
    private final int width;
    private final int height;
    private final Mat image;

    public LoadedImage(String imageId, String imagePath, int width, int height, Mat image) {
        this.imageId = Objects.requireNonNull(imageId, "imageId must not be null");
        this.imagePath = Objects.requireNonNull(imagePath, "imagePath must not be null");
        this.image = Objects.requireNonNull(image, "image must not be null");
        this.width = width;
        this.height = height;
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

    public Mat getImage() {
        return image;
    }
}
