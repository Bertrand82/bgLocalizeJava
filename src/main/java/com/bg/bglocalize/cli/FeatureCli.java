package com.bg.bglocalize.cli;

import java.io.PrintStream;
import java.util.Objects;

import org.opencv.core.CvType;

import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.features.OpenCvFeatureExtractor;

public final class FeatureCli {

    private FeatureCli() {
    }

    public static void main(String[] args) {
       

        try {
            CliOptions options = CliOptions.parse(args);
            if (options.help()) {
                printUsage();
                return ;
            }

            if (options.imagePath() == null) {
                printUsage();
                System.exit(1);
            }

            FeatureExtractionResult result = new OpenCvFeatureExtractor()
                    .extract(options.imagePath(), options.algorithm());
            try {
               System.out.println("imagePath=" + result.getImagePath());
               System.out.println("imageId=" + result.getImageId());
               System.out.println("algorithm=" + result.getAlgorithm());
               System.out.println("dimensions=" + result.getWidth() + "x" + result.getHeight());
               System.out.println("keypoints=" + result.getKeypointCount());
               System.out.println("descriptorType=" + result.getDescriptorMatType());
               System.out.println("descriptorTypeName=" + CvType.typeToString(result.getDescriptorMatType()));
               System.out.println("descriptorRows=" + result.getDescriptors().rows());
               System.out.println("descriptorCols=" + result.getDescriptors().cols());
            } finally {
                result.getKeypoints().release();
                result.getDescriptors().release();
            }
           
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar bgLocalizeJava-jar-with-dependencies.jar --image <path> [--algorithm ORB|SIFT|AKAZE|BRISK]");
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
