package com.bg.bglocalize.colmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ColmapTextModelReader {

    public List<Image2DColmap> readImages2D(Path path) throws IOException {
        Path normalizedPath = normalizePath(path);
        List<Image2DColmap> images = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(normalizedPath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (shouldSkip(line)) {
                    continue;
                }

                String[] header = line.trim().split("\\s+", 10);
                if (header.length < 10) {
                    throw new IllegalArgumentException("Invalid COLMAP image header at line " + lineNumber + ": " + line);
                }

                String observationsLine = reader.readLine();
                lineNumber++;
                List<ColmapImageObservation> observations = parseObservations(observationsLine, lineNumber);

                images.add(new Image2DColmap(
                        Long.parseLong(header[0]),
                        Double.parseDouble(header[1]),
                        Double.parseDouble(header[2]),
                        Double.parseDouble(header[3]),
                        Double.parseDouble(header[4]),
                        Double.parseDouble(header[5]),
                        Double.parseDouble(header[6]),
                        Double.parseDouble(header[7]),
                        Long.parseLong(header[8]),
                        header[9],
                        observations));
            }
        }

        return images;
    }

    public List<ColmapPoint3D> readPoints3D(Path path) throws IOException {
        Path normalizedPath = normalizePath(path);
        List<ColmapPoint3D> points = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(normalizedPath)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (shouldSkip(line)) {
                    continue;
                }

                String[] tokens = line.trim().split("\\s+");
                if (tokens.length < 8) {
                    throw new IllegalArgumentException("Invalid COLMAP point3D line at line " + lineNumber + ": " + line);
                }
                if ((tokens.length - 8) % 2 != 0) {
                    throw new IllegalArgumentException("Invalid COLMAP point3D track at line " + lineNumber + ": " + line);
                }

                points.add(new ColmapPoint3D(
                        Long.parseLong(tokens[0]),
                        Double.parseDouble(tokens[1]),
                        Double.parseDouble(tokens[2]),
                        Double.parseDouble(tokens[3]),
                        Integer.parseInt(tokens[4]),
                        Integer.parseInt(tokens[5]),
                        Integer.parseInt(tokens[6]),
                        Double.parseDouble(tokens[7]),
                        parseTrack(tokens)));
            }
        }

        return List.copyOf(points);
    }

    private static Path normalizePath(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedPath)) {
            throw new IllegalArgumentException("COLMAP file not found: " + normalizedPath);
        }
        return normalizedPath;
    }

    private static List<ColmapImageObservation> parseObservations(String line, int lineNumber) {
        if (line == null || line.isBlank()) {
            return List.of();
        }

        String[] tokens = line.trim().split("\\s+");
        if (tokens.length % 3 != 0) {
            throw new IllegalArgumentException("Invalid COLMAP image observations at line " + lineNumber + ": " + line);
        }

        List<ColmapImageObservation> observations = new ArrayList<>(tokens.length / 3);
        for (int i = 0; i < tokens.length; i += 3) {
            observations.add(new ColmapImageObservation(
                    Double.parseDouble(tokens[i]),
                    Double.parseDouble(tokens[i + 1]),
                    Long.parseLong(tokens[i + 2])));
        }
        return observations;
    }

    private static List<ColmapTrackElement> parseTrack(String[] tokens) {
        List<ColmapTrackElement> track = new ArrayList<>((tokens.length - 8) / 2);
        for (int i = 8; i < tokens.length; i += 2) {
            track.add(new ColmapTrackElement(
                    Long.parseLong(tokens[i]),
                    Long.parseLong(tokens[i + 1])));
        }
        return List.copyOf(track);
    }

    private static boolean shouldSkip(String line) {
        String trimmedLine = line.trim();
        return trimmedLine.isEmpty() || trimmedLine.startsWith("#");
    }
}
