/**
 * 2013 Archie Monji
 * 
 * CanvasPainter
 * Creates image for saving from Canvases
 */

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class CanvasPainter{
	public BufferedImage generateComparisonImage(Canvas canvas1, Canvas canvas2){
		BufferedImage imageToSave = new BufferedImage(canvas1.getWidth() * 2, canvas1.getHeight(), BufferedImage.TYPE_4BYTE_ABGR); 
		Graphics g = imageToSave.getGraphics();
		g.drawImage(canvas1.getCanvasImage(), 0, 0, null);
		g.drawImage(canvas2.getCanvasImage(), canvas1.getWidth(), 0, null);
		return imageToSave;
	}

	public BufferedImage generateImageFromCanvas(Canvas canvas) {
		BufferedImage imageToSave = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_4BYTE_ABGR); 
		Graphics g = imageToSave.getGraphics();
		g.drawImage(canvas.getCanvasImage(), 0, 0, null);
		return imageToSave;
	}
}
