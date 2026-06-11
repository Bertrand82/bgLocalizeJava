package com.bg.bglocalize;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.bg.bglocalize.opencv.OpenCvInitializer;

public class OpenCvInitializerTest {

	public static void main(String[] args) {
		OpenCvInitializer.initialize();
		System.out.println("bg initializeOpenCv done");
		System.out.println("OpenCV lib name: " + Core.NATIVE_LIBRARY_NAME);
		System.out.println("java.library.path: " + System.getProperty("java.library.path"));
		System.out.println("OpenCV version: " + Core.getVersionString());
		String[] libsPAth = System.getProperty("java.library.path").split(";");
		for (String lib : libsPAth) {
			if (lib.toLowerCase().indexOf("cv") > 0)
				System.out.println("    xxxxxxxbingoxxx x :   " + lib);

		}
		Mat src = Mat.eye(10, 10, CvType.CV_8UC3);
		Mat dst = new Mat();
		Mat image = src;
		System.out.println("image == null: " + (image == null));
		System.out.println("image.empty(): " + (image != null && image.empty()));
		System.out.println("image.rows(): " + (image != null ? image.rows() : -1));
		System.out.println("image.cols(): " + (image != null ? image.cols() : -1));
		System.out.println("image.channels(): " + (image != null ? image.channels() : -1));
		System.out.println("image.type(): " + (image != null ? image.type() : -1));
		//System.out.println("gray == null: " + (gray == null));
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);

		System.out.println("OpenCV version: " + Core.getVersionString());
		System.out.println("dst rows=" + dst.rows() + ", cols=" + dst.cols());

		System.out.println("dstEmpty " + dst.empty());
	}

}
