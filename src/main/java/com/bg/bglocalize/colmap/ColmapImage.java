package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;

public record ColmapImage(
        long imageId,
        double qw,
        double qx,
        double qy,
        double qz,
        double tx,
        double ty,
        double tz,
        long cameraId,
        String name,
        List<ColmapImageObservation> observations) {

    public ColmapImage {
        Objects.requireNonNull(name, "name must not be null");
        observations = List.copyOf(Objects.requireNonNull(observations, "observations must not be null"));
    }
}
