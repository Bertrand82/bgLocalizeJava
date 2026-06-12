package com.bg.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.drew.metadata.mov.QuickTimeDirectory;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility class to:
 * <ul>
 *   <li>Read metadata (creation date, GPS) from a video file.</li>
 *   <li>Write EXIF metadata (DateTimeOriginal, CreateDate, GPS) into a JPEG file.</li>
 * </ul>
 *
 * <p>This implementation is pure Java: it uses the <em>metadata-extractor</em> library
 * for reading metadata from video files (MP4, MOV, …) and <em>Apache Commons Imaging</em>
 * for writing EXIF data into JPEG files. No external tool is required.</p>
 *
 * <p>Usage example:
 * <pre>{@code
 * VideoMetadata meta = ExifToolHelper.readVideoMetadata(new File("video.mp4"));
 * ExifToolHelper.writeMetadataToJpeg(new File("frame.jpg"), Instant.now(), meta);
 * }</pre></p>
 */
public class ExifToolHelper {

    /** EXIF date/time format: {@code yyyy:MM:dd HH:mm:ss}. */
    private static final DateTimeFormatter EXIF_DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    private ExifToolHelper() {
        // utility class
    }

    // -------------------------------------------------------------------------
    // Availability check (kept for API compatibility – always returns true)
    // -------------------------------------------------------------------------

    /**
     * Always returns {@code true}: this implementation is pure Java and requires no
     * external tool.
     */
    public static boolean isExifToolAvailable() {
        return true;
    }

    // -------------------------------------------------------------------------
    // Read metadata from a video file
    // -------------------------------------------------------------------------

    /**
     * Reads creation date/time and GPS coordinates from {@code videoFile} using the
     * metadata-extractor library (pure Java, no external process).
     *
     * <p>The returned {@link VideoMetadata} may have {@code null} fields when the video
     * does not embed the corresponding information.</p>
     *
     * @param videoFile the source video file
     * @return parsed metadata; fields may be {@code null}
     * @throws ExifToolException if the file cannot be read or metadata parsing fails
     */
    public static VideoMetadata readVideoMetadata(File videoFile) throws ExifToolException {
        System.out.println("[ExifHelper] Reading metadata from: " + videoFile.getAbsolutePath());
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(videoFile);

            Instant creationInstant = extractCreationInstant(metadata);
            GeoLocation geo         = extractGeoLocation(metadata);
            Double altitude         = extractAltitude(metadata);

            Double latitude  = geo != null ? geo.getLatitude()  : null;
            Double longitude = geo != null ? geo.getLongitude() : null;

            VideoMetadata meta = new VideoMetadata(creationInstant, latitude, longitude, altitude);
            System.out.println("[ExifHelper] Parsed: " + meta);

            if (!meta.hasDateTime()) {
                System.out.println("[ExifHelper] WARNING: no creation date found in video – timestamps will be skipped.");
            }
            if (!meta.hasGps()) {
                System.out.println("[ExifHelper] WARNING: no GPS coordinates found in video – GPS tags will be skipped.");
            }
            return meta;

        } catch (ImageProcessingException | IOException e) {
            throw new ExifToolException(
                    "Failed to read metadata from " + videoFile.getName() + ": " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Write metadata to a JPEG file
    // -------------------------------------------------------------------------

    /**
     * Writes EXIF metadata into {@code jpegFile} using Apache Commons Imaging (pure Java).
     *
     * <ul>
     *   <li>If {@code frameInstant} is non-null, {@code DateTimeOriginal} and
     *       {@code CreateDate} are written.</li>
     *   <li>If {@code metadata} contains GPS data, {@code GPSLatitude},
     *       {@code GPSLatitudeRef}, {@code GPSLongitude}, {@code GPSLongitudeRef}
     *       (and optionally {@code GPSAltitude}) are written.</li>
     *   <li>If neither date nor GPS is available the method logs a warning and
     *       returns without modifying the file.</li>
     * </ul>
     *
     * @param jpegFile     the JPEG file to enrich
     * @param frameInstant the exact capture instant of the frame (UTC), may be {@code null}
     * @param metadata     video-level metadata; may contain {@code null} fields
     * @throws ExifToolException if EXIF data cannot be written to the file
     */
    public static void writeMetadataToJpeg(File jpegFile, Instant frameInstant, VideoMetadata metadata)
            throws ExifToolException {

        TiffOutputSet outputSet = loadOrCreateOutputSet(jpegFile);
        boolean hasData = false;

        try {
            // --- date/time tags ---
            if (frameInstant != null) {
                String dateStr = EXIF_DT_FORMATTER.format(
                        LocalDateTime.ofInstant(frameInstant, ZoneOffset.UTC));
                TiffOutputDirectory exifDir = outputSet.getOrCreateExifDirectory();
                exifDir.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                exifDir.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, dateStr);
                // EXIF 'CreateDate' is stored as the DateTimeDigitized tag (tag 0x9004)
                exifDir.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
                exifDir.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, dateStr);
                System.out.println("[ExifHelper] Writing DateTimeOriginal=" + dateStr
                        + " to " + jpegFile.getName());
                hasData = true;
            } else {
                System.out.println("[ExifHelper] WARNING: no date/time for " + jpegFile.getName()
                        + " – temporal EXIF skipped.");
            }

            // --- GPS tags ---
            if (metadata != null && metadata.hasGps()) {
                double lat = metadata.getLatitude();
                double lon = metadata.getLongitude();
                // setGPSInDegrees(longitude, latitude)
                outputSet.setGPSInDegrees(lon, lat);
                if (metadata.getAltitude() != null) {
                    double alt = metadata.getAltitude();
                    TiffOutputDirectory gpsDir = outputSet.getOrCreateGPSDirectory();
                    gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
                    gpsDir.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE,
                            RationalNumber.valueOf(Math.abs(alt)));
                    gpsDir.removeField(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);
                    gpsDir.add(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF,
                            (byte) (alt >= 0
                                    ? GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF_VALUE_ABOVE_SEA_LEVEL
                                    : GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF_VALUE_BELOW_SEA_LEVEL));
                }
                System.out.println("[ExifHelper] Writing GPS lat=" + lat + " lon=" + lon
                        + " to " + jpegFile.getName());
                hasData = true;
            } else {
                System.out.println("[ExifHelper] INFO: no GPS data for " + jpegFile.getName()
                        + " – GPS EXIF skipped.");
            }

            if (!hasData) {
                System.out.println("[ExifHelper] WARNING: no metadata to write for "
                        + jpegFile.getName() + " – file left unchanged.");
                return;
            }

            // Write to a temporary file, then atomically replace the original
            File tmp = File.createTempFile("exif_", ".jpg", jpegFile.getParentFile());
            try {
                try (OutputStream os = new FileOutputStream(tmp)) {
                    new ExifRewriter().updateExifMetadataLossless(jpegFile, os, outputSet);
                }
                Files.move(tmp.toPath(), jpegFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (ImageWriteException | ImageReadException | IOException e) {
                tmp.delete();
                throw e;
            }

        } catch (ImageWriteException | ImageReadException | IOException e) {
            throw new ExifToolException(
                    "Failed to write EXIF to " + jpegFile.getName() + ": " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Loads the existing EXIF output set from a JPEG, or returns a fresh empty set
     * when the file has no EXIF block or cannot be parsed.
     */
    private static TiffOutputSet loadOrCreateOutputSet(File jpegFile) {
        try {
            JpegImageMetadata jpegMeta = (JpegImageMetadata) Imaging.getMetadata(jpegFile);
            if (jpegMeta != null) {
                TiffImageMetadata exif = jpegMeta.getExif();
                if (exif != null) {
                    return exif.getOutputSet();
                }
            }
        } catch (ImageReadException | ImageWriteException | IOException e) {
            // fall through – return a fresh set below
        }
        return new TiffOutputSet();
    }

    /** Returns the video creation time as an {@link Instant}, or {@code null} if absent. */
    private static Instant extractCreationInstant(Metadata metadata) {
        // MP4 / ISOM containers
        Mp4Directory mp4Dir = metadata.getFirstDirectoryOfType(Mp4Directory.class);
        if (mp4Dir != null) {
            Date d = mp4Dir.getDate(Mp4Directory.TAG_CREATION_TIME, TimeZone.getTimeZone("UTC"));
            if (d != null) {
                return d.toInstant();
            }
        }
        // QuickTime / MOV containers
        QuickTimeDirectory qtDir = metadata.getFirstDirectoryOfType(QuickTimeDirectory.class);
        if (qtDir != null) {
            Date d = qtDir.getDate(QuickTimeDirectory.TAG_CREATION_TIME, TimeZone.getTimeZone("UTC"));
            if (d != null) {
                return d.toInstant();
            }
        }
        return null;
    }

    /**
     * Returns GPS coordinates as a {@link GeoLocation}, or {@code null} if absent.
     * Checks the standard {@link GpsDirectory} first, then the MP4 inline GPS fields.
     * @param <T>
     */
    private static <T> GeoLocation extractGeoLocation(Metadata metadata) {
    	Collection<GpsDirectory> listDirGps = 	metadata.getDirectoriesOfType(GpsDirectory.class);
    	System.err.println("listDirGps size "+listDirGps.size());
        GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDir != null) {
            GeoLocation geo = gpsDir.getGeoLocation();
            if (geo != null && !geo.isZero()) {
                return geo;
            }
        }
        // MP4 inline GPS stored as decimal values in the 'udta' box
        Mp4Directory mp4Dir = metadata.getFirstDirectoryOfType(Mp4Directory.class);
        if (mp4Dir != null
                && mp4Dir.containsTag(Mp4Directory.TAG_LATITUDE)
                && mp4Dir.containsTag(Mp4Directory.TAG_LONGITUDE)) {
            Double lat = mp4Dir.getDoubleObject(Mp4Directory.TAG_LATITUDE);
            Double lon = mp4Dir.getDoubleObject(Mp4Directory.TAG_LONGITUDE);
            if (lat != null && lon != null) {
                return new GeoLocation(lat, lon);
            }
        }
        return null;
    }

    /** Returns GPS altitude in metres (negative = below sea level), or {@code null} if absent. */
    private static Double extractAltitude(Metadata metadata) {
        GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDir != null && gpsDir.containsTag(GpsDirectory.TAG_ALTITUDE)) {
            Double alt = gpsDir.getDoubleObject(GpsDirectory.TAG_ALTITUDE);
            if (alt != null) {
                Integer ref = gpsDir.getInteger(GpsDirectory.TAG_ALTITUDE_REF);
                return (ref != null && ref == 1) ? -alt : alt;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Checked exception
    // -------------------------------------------------------------------------

    /**
     * Thrown when metadata cannot be read from a video file or written to a JPEG.
     */
    public static class ExifToolException extends Exception {
        public ExifToolException(String message) {
            super(message);
        }
        public ExifToolException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
