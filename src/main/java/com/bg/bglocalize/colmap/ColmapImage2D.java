package com.bg.bglocalize.colmap;

import java.util.List;

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
    	observations = observations.stream()
				.filter(o -> o.point3DId() != -1)
    		    .toList();
    }
}
