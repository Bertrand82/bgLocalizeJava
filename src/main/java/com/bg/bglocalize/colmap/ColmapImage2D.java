package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        observations = Objects.requireNonNull(observations, "observations must not be null").stream()
                .filter(o -> o.point3DId() != -1)
                .collect(Collectors.toUnmodifiableList());
    }
}
