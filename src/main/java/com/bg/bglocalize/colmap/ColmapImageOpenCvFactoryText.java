package com.bg.bglocalize.colmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import com.bg.bglocalize.features.FeatureAlgorithm;

/**
 * Reads and writes {@link ColmapImageOpenCV} objects to/from a text file.
 *
 * <p>File format — one image uses 2 + N lines (N = number of observations):
 * <pre>
 *   # Comment lines starting with '#' are ignored
 *   IMAGE_ID QW QX QY QZ TX TY TZ CAMERA_ID NAME          (same convention as ColmapTextModelReader)
 *   ALGORITHM OBSERVATION_COUNT
 *   X Y POINT3D_ID KP_X KP_Y KP_SIZE KP_ANGLE KP_RESPONSE KP_OCTAVE KP_CLASS_ID DESC_COLS DESC_TYPE val1 val2 ...
 *   ...  (one line per observation)
 * </pre>
 */
public final class ColmapImageOpenCvFactoryText {

    /**
     * Writes a list of {@link ColmapImageOpenCV} to a text file.
     *
     * @param images     images to write
     * @param outputPath destination file path (created or overwritten)
     * @throws IOException if an I/O error occurs
     */
    public void write(List<ColmapImageOpenCV> images, Path outputPath) throws IOException {
        Objects.requireNonNull(images, "images must not be null");
        Objects.requireNonNull(outputPath, "outputPath must not be null");

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("# ColmapImageOpenCV list with multiple lines of data per image:");
            writer.newLine();
            writer.write("#   IMAGE_ID QW QX QY QZ TX TY TZ CAMERA_ID NAME");
            writer.newLine();
            writer.write("#   ALGORITHM OBSERVATION_COUNT");
            writer.newLine();
            writer.write("#   POINTS2D_OPENCV[] as (X, Y, POINT3D_ID, KP_X, KP_Y, KP_SIZE, KP_ANGLE, KP_RESPONSE, KP_OCTAVE, KP_CLASS_ID, DESC_COLS, DESC_TYPE, DESC_VALUES...)");
            writer.newLine();

            for (ColmapImageOpenCV image : images) {
                writeImage(writer, image);
            }
        }
    }

    /**
     * Reads a list of {@link ColmapImageOpenCV} from a text file previously written by
     * {@link #write(List, Path)}.
     *
     * @param path path to the text file
     * @return immutable list of images in file order
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if the file is missing or malformed
     */
    public List<ColmapImageOpenCV> read(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedPath)) {
            throw new IllegalArgumentException("File not found: " + normalizedPath);
        }

        List<ColmapImageOpenCV> images = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(normalizedPath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (shouldSkip(line)) {
                    continue;
                }

                // Line 1: image header — same as COLMAP images.txt format
                String[] header = line.trim().split("\\s+", 10);
                if (header.length < 10) {
                    throw new IllegalArgumentException(
                            "Invalid image header at line " + lineNumber + ": " + line);
                }
                long imageId = Long.parseLong(header[0]);
                double qw = Double.parseDouble(header[1]);
                double qx = Double.parseDouble(header[2]);
                double qy = Double.parseDouble(header[3]);
                double qz = Double.parseDouble(header[4]);
                double tx = Double.parseDouble(header[5]);
                double ty = Double.parseDouble(header[6]);
                double tz = Double.parseDouble(header[7]);
                long cameraId = Long.parseLong(header[8]);
                String name = header[9];

                // Line 2: algorithm and observation count
                String algorithmLine = reader.readLine();
                lineNumber++;
                if (algorithmLine == null) {
                    throw new IllegalArgumentException("Unexpected end of file at line " + lineNumber);
                }
                String[] algorithmTokens = algorithmLine.trim().split("\\s+");
                if (algorithmTokens.length < 2) {
                    throw new IllegalArgumentException(
                            "Invalid algorithm line at line " + lineNumber + ": " + algorithmLine);
                }
                FeatureAlgorithm algorithm = FeatureAlgorithm.from(algorithmTokens[0]);
                int observationCount = Integer.parseInt(algorithmTokens[1]);

                // N observation lines
                List<ColmapImageObservation> colmapObservations = new ArrayList<>(observationCount);
                List<ColmapImageObservationOpenCV> observationFeatures = new ArrayList<>(observationCount);
                for (int i = 0; i < observationCount; i++) {
                    String obsLine = reader.readLine();
                    lineNumber++;
                    if (obsLine == null) {
                        throw new IllegalArgumentException(
                                "Unexpected end of file at line " + lineNumber);
                    }
                    String[] tokens = obsLine.trim().split("\\s+");
                    if (tokens.length < 12) {
                        throw new IllegalArgumentException(
                                "Invalid observation line at line " + lineNumber + ": " + obsLine);
                    }

                    double x = Double.parseDouble(tokens[0]);
                    double y = Double.parseDouble(tokens[1]);
                    long point3DId = Long.parseLong(tokens[2]);
                    ColmapImageObservation obs = new ColmapImageObservation(x, y, point3DId);
                    colmapObservations.add(obs);

                    float kpX = Float.parseFloat(tokens[3]);
                    float kpY = Float.parseFloat(tokens[4]);
                    float kpSize = Float.parseFloat(tokens[5]);
                    float kpAngle = Float.parseFloat(tokens[6]);
                    float kpResponse = Float.parseFloat(tokens[7]);
                    int kpOctave = Integer.parseInt(tokens[8]);
                    int kpClassId = Integer.parseInt(tokens[9]);
                    KeyPoint keyPoint = new KeyPoint(kpX, kpY, kpSize, kpAngle, kpResponse, kpOctave, kpClassId);

                    int descCols = Integer.parseInt(tokens[10]);
                    int descType = Integer.parseInt(tokens[11]);
                    Mat descriptor = parseDescriptor(tokens, descCols, descType, lineNumber);

                    observationFeatures.add(new ColmapImageObservationOpenCV(obs, keyPoint, descriptor));
                }

                ColmapImage colmapImage = new ColmapImage(
                        imageId, qw, qx, qy, qz, tx, ty, tz, cameraId, name, colmapObservations);
                images.add(new ColmapImageOpenCV(colmapImage, name, algorithm, observationFeatures));
            }
        }

        return List.copyOf(images);
    }

    private static void writeImage(BufferedWriter writer, ColmapImageOpenCV image) throws IOException {
        ColmapImage ci = image.getColmapImage();

        // Line 1: same convention as ColmapTextModelReader images.txt header
        writer.write(ci.imageId() + " " + ci.qw() + " " + ci.qx() + " " + ci.qy() + " " + ci.qz()
                + " " + ci.tx() + " " + ci.ty() + " " + ci.tz() + " " + ci.cameraId() + " " + ci.name());
        writer.newLine();

        // Line 2: algorithm name and number of observation features
        List<ColmapImageObservationOpenCV> obsFeatures = image.getObservationFeatures();
        writer.write(image.getAlgorithm().name() + " " + obsFeatures.size());
        writer.newLine();

        // One line per observation feature
        for (ColmapImageObservationOpenCV obsFeature : obsFeatures) {
            writer.write(serializeObservationFeature(obsFeature));
            writer.newLine();
        }
    }

    private static String serializeObservationFeature(ColmapImageObservationOpenCV obsFeature) {
        StringBuilder sb = new StringBuilder();

        ColmapImageObservation obs = obsFeature.getObservation();
        sb.append(obs.x()).append(' ').append(obs.y()).append(' ').append(obs.point3DId());

        KeyPoint kp = obsFeature.getKeyPoint();
        Point pt = kp.pt;
        sb.append(' ').append(pt.x)
          .append(' ').append(pt.y)
          .append(' ').append(kp.size)
          .append(' ').append(kp.angle)
          .append(' ').append(kp.response)
          .append(' ').append(kp.octave)
          .append(' ').append(kp.class_id);

        Mat descriptor = obsFeature.getDescriptor();
        if (descriptor.empty()) {
            sb.append(" 0 0");
        } else {
            int cols = descriptor.cols();
            int type = descriptor.type();
            sb.append(' ').append(cols).append(' ').append(type);
            for (int c = 0; c < cols; c++) {
                sb.append(' ').append(descriptor.get(0, c)[0]);
            }
        }

        return sb.toString();
    }

    private static Mat parseDescriptor(String[] tokens, int descCols, int descType, int lineNumber) {
        if (descCols == 0) {
            return new Mat();
        }
        if (tokens.length < 12 + descCols) {
            throw new IllegalArgumentException(
                    "Not enough descriptor values at line " + lineNumber
                            + ": expected " + descCols + " but found " + (tokens.length - 12));
        }
        Mat descriptor = new Mat(1, descCols, descType);
        double[] values = new double[descCols];
        for (int c = 0; c < descCols; c++) {
            values[c] = Double.parseDouble(tokens[12 + c]);
        }
        descriptor.put(0, 0, values);
        return descriptor;
    }

    private static boolean shouldSkip(String line) {
        String trimmed = line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("#");
    }
}
