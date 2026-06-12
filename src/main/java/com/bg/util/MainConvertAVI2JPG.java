package com.bg.util;

import java.io.File;
import java.time.Instant;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.bg.bglocalize.opencv.OpenCvInitializer;
import com.bg.util.ExifToolHelper.ExifToolException;

/**
 * Extracts one JPEG every N frames from a video file and enriches each JPEG
 * with EXIF metadata (DateTimeOriginal, CreateDate, GPS) read from the video.
 *
 * <p>Metadata enrichment relies on ExifTool being installed and available on
 * the system PATH. If ExifTool is absent the frames are still extracted but
 * written without EXIF tags.</p>
 *
 * <p>Usage example (command line):
 * <pre>
 *   java -cp bgLocalizeJava.jar com.bg.util.MainConvertAVI2JPG
 * </pre>
 * Edit {@code videoPath} below before running, or adapt to accept CLI arguments.</p>
 */
public class MainConvertAVI2JPG {

    public static void main(String[] args_) {
        OpenCvInitializer.initialize();

        // ------------------------------------------------------------------ //
        //  Configuration – adapt as needed                                    //
        // ------------------------------------------------------------------ //
        String videoPath = "data/VID_20260610_134434.AVI";
        videoPath = "D:\\BG.mp4";

        File target    = new File("target");
        File outputDir = new File(target, "output_" + System.currentTimeMillis());
        File fileVideo = new File(videoPath);
        // ------------------------------------------------------------------ //

        System.out.println("fileVideo exists=" + fileVideo.exists()
                + "  path=" + fileVideo.getAbsolutePath());

        // --- Check ExifTool availability once ---
        boolean exifToolAvailable = ExifToolHelper.isExifToolAvailable();
        if (!exifToolAvailable) {
            System.out.println("[WARN] ExifTool not found on PATH – EXIF enrichment will be skipped.");
        }

        // --- Read video metadata ---
        VideoMetadata videoMetadata = null;
        if (exifToolAvailable && fileVideo.exists()) {
            try {
                videoMetadata = ExifToolHelper.readVideoMetadata(fileVideo);
            } catch (ExifToolException e) {
                System.out.println("[WARN] Could not read video metadata: " + e.getMessage());
            }
        }

        // --- Open video ---
        VideoCapture capture = new VideoCapture(fileVideo.getAbsolutePath());
        if (!capture.isOpened()) {
            System.out.println("Impossible d'ouvrir la vidéo : " + videoPath);
            return;
        }

        double fps       = capture.get(Videoio.CAP_PROP_FPS);
        int    frameStep = (int) Math.round(fps * 2.0);

        System.out.println("fps=" + fps);
        System.out.println("frameStep=" + frameStep);

        Mat frame      = new Mat();
        int frameCount = 0;
        int savedCount = 0;

        outputDir.mkdirs();

        // --- Extraction loop ---
        while (capture.read(frame)) {
            if (frameCount % frameStep == 0) {
                double posMsec = capture.get(Videoio.CAP_PROP_POS_MSEC);

                File fileImage = new File(outputDir, String.format("image_%05d.jpg", savedCount));
                Imgcodecs.imwrite(fileImage.getAbsolutePath(), frame);

                System.out.println(fileImage.getAbsolutePath()
                        + " saved=true"
                        + " sourceFrame=" + frameCount
                        + " posMsec=" + posMsec);

                // --- EXIF enrichment ---
                if (exifToolAvailable) {
                    Instant frameInstant = computeFrameInstant(videoMetadata, posMsec);
                    try {
                        ExifToolHelper.writeMetadataToJpeg(fileImage, frameInstant, videoMetadata);
                    } catch (ExifToolException e) {
                        System.out.println("[WARN] EXIF write failed for "
                                + fileImage.getName() + ": " + e.getMessage());
                    }
                }

                savedCount++;
            }
            frameCount++;
        }

        capture.release();
        System.out.println("Extraction terminée : " + savedCount
                + " images dans : " + outputDir.getAbsolutePath());
    }

    /**
     * Computes the exact capture instant of a frame by adding {@code posMsec}
     * to the video's creation instant.
     *
     * @param metadata video metadata (may be {@code null} or have no date)
     * @param posMsec  position of the frame in milliseconds ({@link Videoio#CAP_PROP_POS_MSEC})
     * @return the frame instant, or {@code null} if the creation date is unknown
     */
    private static Instant computeFrameInstant(VideoMetadata metadata, double posMsec) {
        if (metadata == null || !metadata.hasDateTime()) {
            return null;
        }
        return metadata.getCreationInstant().plusMillis((long) posMsec);
    }
}