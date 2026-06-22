package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;

public record ColmapPoint3D(
        long point3DId,
        double x,
        double y,
        double z,
        int red,
        int green,
        int blue,
        double error,
        List<ColmapTrackElement> track) {

    public ColmapPoint3D {
        track = List.copyOf(Objects.requireNonNull(track, "track must not be null"));
    }
}
