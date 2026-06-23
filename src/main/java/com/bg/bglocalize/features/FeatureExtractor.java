package com.bg.bglocalize.features;

import com.bg.bglocalize.image.LoadedImage;

public interface FeatureExtractor {

    FeatureExtractionResult extract(String imagePath, FeatureAlgorithm algorithm);
    FeatureExtractionResult extract(String imagePath);

    FeatureExtractionResult extract(LoadedImage image);
    FeatureExtractionResult extract(LoadedImage image, FeatureAlgorithm algorithm);
}
