package com.bertrand82.bglocalize.cli;

import java.io.PrintStream;
import java.util.Objects;

import org.opencv.core.CvType;

import com.bertrand82.bglocalize.features.FeatureAlgorithm;
import com.bertrand82.bglocalize.features.FeatureExtractionResult;
import com.bertrand82.bglocalize.features.OpenCvFeatureExtractor;

public final class FeatureCli {

    private FeatureCli() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        Objects.requireNonNull(out, "out must not be null");
        Objects.requireNonNull(err, "err must not be null");

        try {
            CliOptions options = CliOptions.parse(args);
            if (options.help()) {
                printUsage(out);
                return 0;
            }

            if (options.imagePath() == null) {
                printUsage(err);
                return 1;
            }

            FeatureExtractionResult result = new OpenCvFeatureExtractor()
                    .extract(options.imagePath(), options.algorithm());
            try {
                out.println("imagePath=" + result.getImagePath());
                out.println("imageId=" + result.getImageId());
                out.println("algorithm=" + result.getAlgorithm());
                out.println("dimensions=" + result.getWidth() + "x" + result.getHeight());
                out.println("keypoints=" + result.getKeypointCount());
                out.println("descriptorType=" + result.getDescriptorMatType());
                out.println("descriptorTypeName=" + CvType.typeToString(result.getDescriptorMatType()));
                out.println("descriptorRows=" + result.getDescriptors().rows());
                out.println("descriptorCols=" + result.getDescriptors().cols());
            } finally {
                result.getKeypoints().release();
                result.getDescriptors().release();
            }
            return 0;
        } catch (IllegalArgumentException e) {
            err.println(e.getMessage());
            return 2;
        }
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage: java -jar bgLocalizeJava-jar-with-dependencies.jar --image <path> [--algorithm ORB|SIFT|AKAZE|BRISK]");
    }

    private record CliOptions(String imagePath, FeatureAlgorithm algorithm, boolean help) {

        private static CliOptions parse(String[] args) {
            String imagePath = null;
            FeatureAlgorithm algorithm = FeatureAlgorithm.ORB;
            boolean help = false;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--help".equals(arg) || "-h".equals(arg)) {
                    help = true;
                } else if ("--image".equals(arg)) {
                    imagePath = requireValue(args, ++i, "--image");
                } else if (arg.startsWith("--image=")) {
                    imagePath = arg.substring("--image=".length());
                } else if ("--algorithm".equals(arg)) {
                    algorithm = FeatureAlgorithm.from(requireValue(args, ++i, "--algorithm"));
                } else if (arg.startsWith("--algorithm=")) {
                    algorithm = FeatureAlgorithm.from(arg.substring("--algorithm=".length()));
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }

            return new CliOptions(imagePath, algorithm, help);
        }

        private static String requireValue(String[] args, int index, String option) {
            if (index >= args.length) {
                throw new IllegalArgumentException("Missing value for " + option);
            }
            return args[index];
        }
    }
}
