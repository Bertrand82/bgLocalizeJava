package com.bg.bglocalize.colmap;

import java.util.Objects;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;

public final class ColmapImageObservationOpenCV {

    private final ColmapImageObservation observation;
    private final KeyPoint keyPoint;
    private final Mat descriptor;

    public ColmapImageObservationOpenCV(ColmapImageObservation observation, KeyPoint keyPoint, Mat descriptor) {
        this.observation = Objects.requireNonNull(observation, "observation must not be null");
        this.keyPoint = Objects.requireNonNull(keyPoint, "keyPoint must not be null");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null");
    }

    public ColmapImageObservation getObservation() {
        return observation;
    }

    public KeyPoint getKeyPoint() {
        return keyPoint;
    }

    public Mat getDescriptor() {
        return descriptor;
    }

    public boolean hasDescriptor() {
        return !descriptor.empty();
    }
}
