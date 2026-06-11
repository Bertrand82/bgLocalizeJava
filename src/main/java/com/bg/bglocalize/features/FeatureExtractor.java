package com.bg.bglocalize.features;

import com.bg.bglocalize.image.LoadedImage;

public interface FeatureExtractor {

    FeatureExtractionResult extract(String imagePath, FeatureAlgorithm algorithm);

    FeatureExtractionResult extract(LoadedImage image, FeatureAlgorithm algorithm);
}
