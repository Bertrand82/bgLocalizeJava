package com.bertrand82.bglocalize.features;

import com.bertrand82.bglocalize.image.LoadedImage;

public interface FeatureExtractor {

    FeatureExtractionResult extract(String imagePath, FeatureAlgorithm algorithm);

    FeatureExtractionResult extract(LoadedImage image, FeatureAlgorithm algorithm);
}
