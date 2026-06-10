package com.bertrand82.bglocalize;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.osgi.OpenCVNativeLoader;



/**
 * Unit test for OpenCV dependencies.
 */
public class AppTest {
	
	
	public AppTest() {
		try {
			File dirOpenCV = new File("C:\\Users\\bertr\\workspace_c\\opencv");
			File dirOpenCVBuild = new File(dirOpenCV,"build");
			File dirOpenCVBuildLibRelease =  new File(dirOpenCVBuild,"lib\\release");
			File dirOpenCVBuildBinRelease =  new File(dirOpenCVBuild,"bin\\release");
			System.err.println("dirOpenCVBuild           exists "+dirOpenCVBuild.exists()+"  "+dirOpenCVBuild.getAbsolutePath());
			System.err.println("dirOpenCVBuildLibRelease exists "+dirOpenCVBuildLibRelease.exists()+"  "+dirOpenCVBuildLibRelease.getAbsolutePath());
			System.err.println("dirOpenCVBuildBinRelease exists "+dirOpenCVBuildBinRelease.exists()+"  "+dirOpenCVBuildBinRelease.getAbsolutePath());
			File dirOpencvX64=new File(dirOpenCVBuild,"x64");
			System.err.println("dirOpencvX64Bin exists "+dirOpencvX64.exists()+"  "+dirOpencvX64.getAbsolutePath());
			File[] binDllFiles = dirOpenCVBuildBinRelease.listFiles((dir, name) -> name.toLowerCase().endsWith(".dll"));
			System.err.println("Core.NATIVE_LIBRARY_NAME : "+Core.NATIVE_LIBRARY_NAME);
			System.err.println("getProperties" +System.getProperties());
			//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			for(File f : binDllFiles) {
            	System.err.println("fdll   "+f.getName());
            	//System.load(f.getAbsolutePath());
            }
			File[] libDllFiles = dirOpenCVBuildLibRelease.listFiles((dir, name) -> name.toLowerCase().endsWith(".dll"));
            for(File fLibDll : libDllFiles) {
            	System.err.println("fLibDll   "+fLibDll.getAbsolutePath());
            //	System.load(fLibDll.getAbsolutePath());
            }
			
			System.load("C:\\Users\\bertr\\workspace_c\\opencv\\build\\x64\\vc17\\bin\\opencv_core4140.dll");
			//System.load("C:\\Users\\bertr\\workspace_c\\opencv\\build\\x64\\vc17\\bin\\opencv_imgcodecs4140.dll");
			//System.load("C:\\Users\\bertr\\workspace_c\\opencv\\build\\x64\\vc17\\bin\\opencv_videoio4140.dll");
			//System.load("C:\\Users\\bertr\\workspace_c\\opencv\\build\\java\\opencv_java4140.dll");
			//System.out.println("OpenCV version = " + Core.VERSION);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



    @Test
    public void shouldLoadOpenCvDependencies() {
      
    	System.err.println("VERSION : "+Core.VERSION);
        assertNotNull( Core.VERSION);
     
    }
}
