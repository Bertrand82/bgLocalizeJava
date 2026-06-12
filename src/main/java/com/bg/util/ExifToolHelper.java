package com.bg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that wraps the ExifTool command-line tool to:
 * <ul>
 *   <li>Read metadata (creation date, GPS) from a video file.</li>
 *   <li>Write EXIF metadata (DateTimeOriginal, CreateDate, GPS) into a JPEG file.</li>
 * </ul>
 *
 * <p>ExifTool must be installed and available on the system PATH.
 * Use {@link #isExifToolAvailable()} to check before calling other methods.</p>
 *
 * <p>Usage example:
 * <pre>{@code
 * if (!ExifToolHelper.isExifToolAvailable()) {
 *     System.err.println("ExifTool not found – EXIF enrichment disabled.");
 * } else {
 *     VideoMetadata meta = ExifToolHelper.readVideoMetadata(new File("video.mp4"));
 *     ExifToolHelper.writeMetadataToJpeg(new File("frame.jpg"), Instant.now(), meta);
 * }
 * }</pre></p>
 */
public class ExifToolHelper {

    /** ExifTool date/time format: {@code yyyy:MM:dd HH:mm:ss}. */
    private static final DateTimeFormatter EXIF_DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    // Regex patterns to extract fields from ExifTool JSON output (-json -n)
    private static final Pattern PAT_CREATE_DATE =
            Pattern.compile("\"(?:CreateDate|DateTimeOriginal)\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PAT_GPS_LAT =
            Pattern.compile("\"GPSLatitude\"\\s*:\\s*([+-]?[\\d.]+)");
    private static final Pattern PAT_GPS_LON =
            Pattern.compile("\"GPSLongitude\"\\s*:\\s*([+-]?[\\d.]+)");
    private static final Pattern PAT_GPS_ALT =
            Pattern.compile("\"GPSAltitude\"\\s*:\\s*([+-]?[\\d.]+)");

    private ExifToolHelper() {
        // utility class
    }

    // -------------------------------------------------------------------------
    // Availability check
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code exiftool} is found on the system PATH.
     */
    public static boolean isExifToolAvailable() {
        try {
            Process p = new ProcessBuilder("exiftool", "-ver")
                    .redirectErrorStream(true)
                    .start();
            String version = new BufferedReader(new InputStreamReader(p.getInputStream()))
                    .readLine();
            p.waitFor();
            boolean available = version != null && !version.isBlank();
            if (available) {
                System.out.println("[ExifTool] version " + version.trim() + " detected.");
            }
            return available;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Read metadata from a video file
    // -------------------------------------------------------------------------

    /**
     * Reads creation date/time and GPS coordinates from {@code videoFile} using ExifTool.
     *
     * <p>The returned {@link VideoMetadata} may have {@code null} fields when the video
     * does not embed the corresponding information.</p>
     *
     * @param videoFile the source video file
     * @return parsed metadata; fields may be {@code null}
     * @throws ExifToolException if ExifTool exits with an error or cannot be launched
     */
    public static VideoMetadata readVideoMetadata(File videoFile) throws ExifToolException {
        System.out.println("[ExifTool] Reading metadata from: " + videoFile.getAbsolutePath());

        List<String> cmd = List.of(
                "exiftool",
                "-json",
                "-n",                     // numeric output for GPS
                "-CreateDate",
                "-DateTimeOriginal",
                "-GPSLatitude",
                "-GPSLongitude",
                "-GPSAltitude",
                videoFile.getAbsolutePath()
        );

        String output = runExifTool(cmd);

        Instant creationInstant = parseDateTime(output);
        Double latitude  = parseDouble(PAT_GPS_LAT, output);
        Double longitude = parseDouble(PAT_GPS_LON, output);
        Double altitude  = parseDouble(PAT_GPS_ALT, output);

        VideoMetadata meta = new VideoMetadata(creationInstant, latitude, longitude, altitude);
        System.out.println("[ExifTool] Parsed: " + meta);

        if (!meta.hasDateTime()) {
            System.out.println("[ExifTool] WARNING: no creation date found in video – timestamps will be skipped.");
        }
        if (!meta.hasGps()) {
            System.out.println("[ExifTool] WARNING: no GPS coordinates found in video – GPS tags will be skipped.");
        }

        return meta;
    }

    // -------------------------------------------------------------------------
    // Write metadata to a JPEG file
    // -------------------------------------------------------------------------

    /**
     * Writes EXIF metadata into {@code jpegFile}.
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
     * @param jpegFile      the JPEG file to enrich
     * @param frameInstant  the exact capture instant of the frame (UTC), may be {@code null}
     * @param metadata      video-level metadata; may contain {@code null} fields
     * @throws ExifToolException if ExifTool exits with an error or cannot be launched
     */
    public static void writeMetadataToJpeg(File jpegFile, Instant frameInstant, VideoMetadata metadata)
            throws ExifToolException {

        List<String> args = new ArrayList<>();
        args.add("exiftool");
        args.add("-overwrite_original");

        // --- date/time tags ---
        if (frameInstant != null) {
            String dateStr = EXIF_DT_FORMATTER.format(
                    LocalDateTime.ofInstant(frameInstant, ZoneOffset.UTC));
            args.add("-DateTimeOriginal=" + dateStr);
            args.add("-CreateDate=" + dateStr);
            System.out.println("[ExifTool] Writing DateTimeOriginal=" + dateStr
                    + " to " + jpegFile.getName());
        } else {
            System.out.println("[ExifTool] WARNING: no date/time for " + jpegFile.getName()
                    + " – temporal EXIF skipped.");
        }

        // --- GPS tags ---
        if (metadata != null && metadata.hasGps()) {
            double lat = metadata.getLatitude();
            double lon = metadata.getLongitude();
            args.add("-GPSLatitude#=" + Math.abs(lat));
            args.add("-GPSLatitudeRef=" + (lat >= 0 ? "N" : "S"));
            args.add("-GPSLongitude#=" + Math.abs(lon));
            args.add("-GPSLongitudeRef=" + (lon >= 0 ? "E" : "W"));
            if (metadata.getAltitude() != null) {
                double alt = metadata.getAltitude();
                args.add("-GPSAltitude#=" + Math.abs(alt));
                args.add("-GPSAltitudeRef=" + (alt >= 0 ? "0" : "1"));
            }
        } else {
            System.out.println("[ExifTool] INFO: no GPS data for " + jpegFile.getName()
                    + " – GPS EXIF skipped.");
        }

        // nothing to write
        if (args.size() == 2) {
            System.out.println("[ExifTool] WARNING: no metadata to write for "
                    + jpegFile.getName() + " – file left unchanged.");
            return;
        }

        args.add(jpegFile.getAbsolutePath());
        runExifTool(args);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Runs the given ExifTool command and returns its combined stdout output.
     *
     * @throws ExifToolException on non-zero exit code or process launch failure
     */
    private static String runExifTool(List<String> cmd) throws ExifToolException {
        try {
            Process process = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }

            int exitCode = process.waitFor();
            String output = sb.toString();

            if (exitCode != 0) {
                throw new ExifToolException(
                        "ExifTool exited with code " + exitCode + ": " + output.trim());
            }
            return output;

        } catch (IOException e) {
            throw new ExifToolException("Failed to launch ExifTool: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExifToolException("ExifTool process interrupted.", e);
        }
    }

    /** Parses the first CreateDate / DateTimeOriginal found in ExifTool JSON output. */
    private static Instant parseDateTime(String json) {
        Matcher m = PAT_CREATE_DATE.matcher(json);
        if (!m.find()) {
            return null;
        }
        String raw = m.group(1).trim();
        try {
            LocalDateTime ldt = LocalDateTime.parse(raw, EXIF_DT_FORMATTER);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            System.out.println("[ExifTool] WARNING: could not parse date '" + raw + "': " + e.getMessage());
            return null;
        }
    }

    /** Extracts the first numeric group matched by {@code pattern} in {@code text}, or {@code null}. */
    private static Double parseDouble(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        if (!m.find()) {
            return null;
        }
        try {
            return Double.parseDouble(m.group(1));
        } catch (NumberFormatException e) {
            System.out.println("[ExifTool] WARNING: could not parse numeric value '" + m.group(1) + "'");
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Checked exception
    // -------------------------------------------------------------------------

    /**
     * Thrown when ExifTool is not available or returns an error.
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
