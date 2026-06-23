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
        for (ColmapImage2D colmapImage2D : images) {
        	String fileName  = colmapImage2D.name();
        	File file = new File(imagesDirectory,fileName);
        	FeatureExtractionResult result = new OpenCvFeatureExtractor().extract(file.toPath().toString());
        	System.out.println("fileName "+fileName+ "  exists: "+file.exists()+ " colmapImage2D.observations.size : "+colmapImage2D.observations().size()+"  FeatureExtractionResult.getKeypointCount "+result.getKeypointCount());
        	KeyPoint[] keysOpenCv = result.getKeypoints().toArray();
        	System.out.println("keysOpenCv length "+keysOpenCv.length);
        	// Matching entre keyPoint de colmap et keypoint de openCV
        	List<KeyPoint> listReferenced = new ArrayList<KeyPoint>();
        	for (KeyPoint koc : keysOpenCv) {
        		if (isReferencedInColmap(koc, colmapImage2D)) {
        			listReferenced.add(koc);
        		}
        	}
        	System.out.println("ListReferenced :: "+listReferenced.size());
        }
        System.out.println("Process2DOpenCv start ");
        List<ColmapImageOpenCV> results = factory.createAll(images, FeatureAlgorithm.SIFT);
        System.out.println("rocess2DOpenCv  done "+getDuree(timeStart));
        System.out.println("Generated: " + results.size());
    }

    private boolean isReferencedInColmap(KeyPoint koc, ColmapImage2D colmapImage2D) {
		for(ColmapImageObservation feature : colmapImage2D.observations()) {
			if ((koc.pt.x == feature.x()) && (koc.pt.y == feature.y())){
				return true;
			}
			
		}
		return false;
	}

	long timeEtape = -1;
	private String getDuree(long timeStart) {
		String duree_s = ((timeStart-System.currentTimeMillis())/1000) +" secondes";
		String duree_etape ;
		if (timeEtape >0) {
			duree_etape = ((timeEtape-System.currentTimeMillis())/1000) +" secondes";
		}else {
			duree_etape = duree_s;
		}
		return " duree etape : "+duree_etape +" duree :"+duree_s;
	}

   
}