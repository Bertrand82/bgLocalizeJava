package com.bg.bglocalize.colmap;

import java.util.List;
import java.util.Objects;

public record Image2DColmap(
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

    public Image2DColmap {
    	observations = observations.stream()
				.filter(o -> o.point3DId() != -1)
    		    .toList();
    }

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Image2DColmap other = (Image2DColmap) obj;
		return cameraId == other.cameraId && imageId == other.imageId && Objects.equals(name, other.name)
				&& Objects.equals(observations, other.observations)
				&& Double.doubleToLongBits(qw) == Double.doubleToLongBits(other.qw)
				&& Double.doubleToLongBits(qx) == Double.doubleToLongBits(other.qx)
				&& Double.doubleToLongBits(qy) == Double.doubleToLongBits(other.qy)
				&& Double.doubleToLongBits(qz) == Double.doubleToLongBits(other.qz)
				&& Double.doubleToLongBits(tx) == Double.doubleToLongBits(other.tx)
				&& Double.doubleToLongBits(ty) == Double.doubleToLongBits(other.ty)
				&& Double.doubleToLongBits(tz) == Double.doubleToLongBits(other.tz);
	}
    
    
}
