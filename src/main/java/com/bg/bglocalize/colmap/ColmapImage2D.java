package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;

public record ColmapImage2D(
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

    public ColmapImage2D {
        observations = List.copyOf(Objects.requireNonNull(observations, "observations must not be null"));
    }
}
