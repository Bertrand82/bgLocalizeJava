package com.bertrand82.bglocalize.features;

import java.util.Locale;

import org.opencv.features2d.Feature2D;

public enum FeatureAlgorithm {
    ORB {
        @Override
        public Feature2D createExtractor() {
            return org.opencv.features2d.ORB.create();
        }
    },
    SIFT {
        @Override
        public Feature2D createExtractor() {
            return org.opencv.features2d.SIFT.create();
        }
    },
    AKAZE {
        @Override
        public Feature2D createExtractor() {
            return org.opencv.features2d.AKAZE.create();
        }
    },
    BRISK {
        @Override
        public Feature2D createExtractor() {
            return org.opencv.features2d.BRISK.create();
        }
    };

    public abstract Feature2D createExtractor();

    public static FeatureAlgorithm from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Feature algorithm must not be blank");
        }
        return FeatureAlgorithm.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
