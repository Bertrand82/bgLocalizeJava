package com.bg.util;

import java.time.Instant;

/**
 * Holds the metadata extracted from a source video file:
 * creation date/time and optional GPS coordinates.
 */
public class VideoMetadata {

    /** UTC instant corresponding to the start of the video, or {@code null} if unavailable. */
    private final Instant creationInstant;

    /** GPS latitude in decimal degrees (positive = North), or {@code null} if unavailable. */
    private final Double latitude;

    /** GPS longitude in decimal degrees (positive = East), or {@code null} if unavailable. */
    private final Double longitude;

    /** GPS altitude in metres above sea level, or {@code null} if unavailable. */
    private final Double altitude;

    public VideoMetadata(Instant creationInstant, Double latitude, Double longitude, Double altitude) {
        this.creationInstant = creationInstant;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public Instant getCreationInstant() {
        return creationInstant;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public boolean hasDateTime() {
        return creationInstant != null;
    }

    public boolean hasGps() {
        return latitude != null && longitude != null;
    }

    @Override
    public String toString() {
        return "VideoMetadata{"
                + "creationInstant=" + creationInstant
                + ", latitude=" + latitude
                + ", longitude=" + longitude
                + ", altitude=" + altitude
                + '}';
    }
}
