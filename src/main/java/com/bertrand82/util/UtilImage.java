package com.bertrand82.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class UtilImage {

	public static Path createTexturedImage(File imageFile) throws IOException {
    	System.out.println("create Image test : "+imageFile.getAbsolutePath());
        BufferedImage image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(3f));
        graphics.drawRect(20, 20, 280, 180);
        graphics.drawLine(20, 20, 300, 200);
        graphics.drawLine(300, 20, 20, 200);
        graphics.setColor(Color.BLUE);
        graphics.fillOval(40, 40, 60, 60);
        graphics.setColor(Color.RED);
        graphics.fillOval(220, 120, 50, 50);
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawString("bgLocalize", 110, 110);
        graphics.setColor(Color.GREEN);
        for (int x = 0; x < image.getWidth(); x += 20) {
            graphics.drawLine(x, 0, x, image.getHeight());
        }
        for (int y = 0; y < image.getHeight(); y += 20) {
            graphics.drawLine(0, y, image.getWidth(), y);
        }
        graphics.dispose();
        ImageIO.write(image, "png", imageFile);
        return imageFile.toPath();
    }

	public static Path createTexturedImage(Path filePath) throws IOException{
		return createTexturedImage(filePath.toFile());
	}
}
