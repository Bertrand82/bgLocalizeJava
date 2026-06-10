package com.bertrand82.bglocalize;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class MainConvertAVI2JPG_2 {
    public static void main(String[] args_) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String videoPath = "data/VID_20260610_134434.AVI";
        File target = new File("target");
        File outputDir = new File(target, "output");
        File fileVideo = new File(videoPath);

        System.out.println("fileVideo exists " + fileVideo.exists() + "  " + fileVideo.getAbsolutePath());

        VideoCapture capture = new VideoCapture(fileVideo.getAbsolutePath());

        if (!capture.isOpened()) {
            System.out.println("Impossible d'ouvrir la vidéo : " + videoPath);
            return;
        }

        System.out.println("FPS=" + capture.get(Videoio.CAP_PROP_FPS));
        System.out.println("FRAME_COUNT=" + capture.get(Videoio.CAP_PROP_FRAME_COUNT));
        System.out.println("WIDTH=" + capture.get(Videoio.CAP_PROP_FRAME_WIDTH));
        System.out.println("HEIGHT=" + capture.get(Videoio.CAP_PROP_FRAME_HEIGHT));

        Mat frame = new Mat();
        Mat previous = new Mat();
        Mat diff = new Mat();

        int frameCount = 0;
        outputDir.mkdirs();

        while (capture.read(frame)) {
            System.out.println("frame " + frameCount + " empty=" + frame.empty() + " size=" + frame.cols() + "x" + frame.rows());

            if (!previous.empty()) {
                Core.absdiff(previous, frame, diff);
                Scalar s = Core.sumElems(diff);
                double score = s.val[0] + s.val[1] + s.val[2];
                System.out.println("diff=" + score);
            }

            File fileImage = new File(outputDir, String.format("image_%05d.jpg", frameCount));
            Mat snapshot = frame.clone();
            Imgcodecs.imwrite(fileImage.getAbsolutePath(), snapshot);
            snapshot.release();

            frame.copyTo(previous);
            frameCount++;
        }

        capture.release();
        previous.release();
        diff.release();
        frame.release();

        System.out.println("Extraction terminée : " + frameCount + " images dans : " + outputDir.getAbsolutePath());
    }
}