package com.bg.bglocalize.colmap;

import java.awt.RenderingHints.Key;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.KeyPoint;

import com.bg.bglocalize.features.FeatureAlgorithm;
import com.bg.bglocalize.features.FeatureExtractionResult;
import com.bg.bglocalize.features.OpenCvFeatureExtractor;
import com.bg.bglocalize.opencv.OpenCvInitializer;

public class MainPrepareFilesText {

    private static final ColmapTextModelReader reader = new ColmapTextModelReader();
    private final File projetDirectory;
    private final File imagesDirectory;
    private final File image2D_Txt;
    private final File dataBaseFile ;
    private final File dirTarget = new File("target");
    private final File dirOut = new File(dirTarget,"OUT_bg");
    
    private static final File IMAGES_DIRECTORY = new File("data/BG/images");

    // ✅ Déclaration + initialisation de factory
    private final ColmapImageOpenCVFactory factory ;
    
    public MainPrepareFilesText(File projetDirectory) throws Exception {
    	this.projetDirectory = projetDirectory;
    	this.imagesDirectory = new File(projetDirectory,"images");
    	this.dataBaseFile = new File(projetDirectory,"database.db");
    	File sparseDir = new File(projetDirectory,"sparse");
    	File sparseDir_0 = new File(sparseDir,"0");
    	image2D_Txt = new File(sparseDir_0,"images.txt");
    	factory  = new ColmapImageOpenCVFactory(dataBaseFile, imagesDirectory);
    }

    public static void main(String[] args) throws Exception {
    	 OpenCvInitializer.initialize();
         File PROJET_DIRECTORY = new File("data/BG");
       
        new MainPrepareFilesText(PROJET_DIRECTORY).processTrace();
    }

    /**
     * Je veux essayer d'examiner si les keyPoint de OpenCv correspondent au keypoints de colmap
     * @throws Exception
     */
    public void processTrace() throws Exception{
    	long timeStart = System.currentTimeMillis();
    	System.out.println("Read2D start");
        List<ColmapImage2D> images = reader.readImages2D(image2D_Txt.toPath());
        System.out.println("Read2D done "+getDuree(timeStart)+"   images.size : "+images.size());
        
        System.out.println("Process2DOpenCv start ");
        List<ColmapImage2DOpenCV> listColmapImageOpenCV = factory.createAll(images, FeatureAlgorithm.SIFT);
        System.out.println("rocess2DOpenCv  done "+getDuree(timeStart));
        System.out.println("Generated: " + listColmapImageOpenCV.size()  +" / ");
        
        ColmapImageOpenCvFactoryText factoryText = new ColmapImageOpenCvFactoryText();
        dirOut.mkdirs();
        File tempFile = new File(dirOut,"images2DColmapOpenCV.txt");
        factoryText.write(listColmapImageOpenCV, tempFile.toPath());
        System.out.println(" factoryText.write  done "+getDuree(timeStart)+"  "+tempFile.toPath());
        List<ColmapImage2DOpenCV>  listRead = factoryText.read(tempFile.toPath());
        System.out.println(" factoryText.read  done "+getDuree(timeStart)+"  "+tempFile.toPath());
        System.out.println("equals "+listRead.equals(listColmapImageOpenCV));
    }

  

	long timeEtape = -1;
	private String getDuree(long timeStart) {
		String duree_s = ((System.currentTimeMillis()-timeStart)/1000) +" secondes";
		String duree_etape ;
		if (timeEtape >0) {
			duree_etape = ((System.currentTimeMillis()-timeEtape)/1000) +" secondes";
		}else {
			duree_etape = duree_s;
		}
		return " duree etape : "+duree_etape +" duree :"+duree_s;
	}

   
}