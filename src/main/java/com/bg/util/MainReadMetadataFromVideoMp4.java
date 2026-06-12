package com.bg.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.IOException;

/**
 * Reads and prints all metadata found in an MP4 file.
 *
 * <p>Usage:
 * <pre>
 *   java -cp target/bgLocalizeJava-1.0-SNAPSHOT-jar-with-dependencies.jar \
 *     com.bg.util.MainReadMetadataFromVideoMp4 /path/to/video.mp4
 * </pre>
 */
public class MainReadMetadataFromVideoMp4 {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: MainReadMetadataFromVideoMp4 <video.mp4>");
            System.exit(1);
        }

        File videoFile = new File(args[0]);
        if (!videoFile.isFile()) {
            System.err.println("File does not exist or is not a regular file: " + videoFile.getAbsolutePath());
            System.exit(1);
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(videoFile);
            printMetadata(metadata);
        } catch (ImageProcessingException | IOException e) {
            System.err.println("Failed to read metadata from " + videoFile.getAbsolutePath());
            System.err.println(e.toString());
            System.exit(2);
        }
    }

    private static void printMetadata(Metadata metadata) {
        for (Directory directory : metadata.getDirectories()) {
            System.out.println("[" + directory.getName() + "]");

            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    System.out.println("  ERROR: " + error);
                }
            }

            for (Tag tag : directory.getTags()) {
                System.out.println("  " + tag.getTagName() + " = " + tag.getDescription());
            }
        }
    }
}
