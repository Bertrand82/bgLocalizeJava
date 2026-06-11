package com.bg.bglocalize;

import java.io.File;
import java.net.URL;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class MainConvertAVI2JPG {
	public static void main(String[] args_) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String videoPath = "data/VID_20260610_134434.AVI";
         videoPath = "D:\\BG.mp4";
         File target = new File("target");
        File outputDir = new File(target, "output_"+System.currentTimeMillis());
        File fileVideo = new File(videoPath);

        System.out.println("fileVideo exists " + fileVideo.exists() + "  " + fileVideo.getAbsolutePath());

        VideoCapture capture = new VideoCapture(fileVideo.getAbsolutePath());

        if (!capture.isOpened()) {
            System.out.println("Impossible d'ouvrir la vidéo : " + videoPath);
            return;
        }

        double fps = capture.get(Videoio.CAP_PROP_FPS);
        int frameStep = (int) Math.round(fps * 2.0);

        System.out.println("fps=" + fps);
        System.out.println("frameStep=" + frameStep);

        Mat frame = new Mat();
        int frameCount = 0;
        int savedCount = 0;

        outputDir.mkdirs();

        while (capture.read(frame)) {
            if (frameCount % frameStep == 0) {
                File fileImage = new File(outputDir, String.format("image_%05d.jpg", savedCount));
                Imgcodecs.imwrite(fileImage.getAbsolutePath(), frame);

                System.out.println(fileImage.getAbsolutePath()
                        + " saved=true"
                        + " sourceFrame=" + frameCount
                        + " posMsec=" + capture.get(Videoio.CAP_PROP_POS_MSEC));

                savedCount++;
            }

            frameCount++;
        }

        capture.release();
        System.out.println("Extraction terminée : " + savedCount + " images dans : " + outputDir.getAbsolutePath());
    }

}