import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.JPanel;

public class StippledImageGenerator extends JPanel{
	private static final long serialVersionUID = 1L;

	public static BufferedImage generateStippledImage(BufferedImage image, int rows, int cols, boolean includeAlpha){
		final byte[] p1 = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;
		if(!hasAlphaChannel){
			includeAlpha = false;
		}
		int pixelLength = hasAlphaChannel? 4 : 3;
		ColoredDot[][] dots = new ColoredDot[rows][cols];
		float w = image.getWidth();
		float h = image.getHeight();
		float dotW = w/cols;
		float dotH = h/rows;
		float dotR = h/cols;
		for(int r = 0; r < rows; r++){
			for(int c = 0; c < cols; c++){
				dots[r][c] = new ColoredDot(c * dotW, r * dotH, dotR, dotR);
			}
		}
		float x = 0, y = 0;
		int row = 0 , col = 0;
		float p = 0;
		int a = 0, r = 0, g = 0, b = 0;
		for (int pixel = hasAlphaChannel? 1 : 0; pixel < p1.length; pixel += pixelLength){
			//Build AlphaHistogram, or ignore alpha
			p = pixel / pixelLength;
			x = p % w;
			y = p / w;
			row = (int)(p / w / dotH);
			col = (int)((p % w) / dotW);
			if(dots[row][col].intersects(x, y, 1, 1)){
				if(includeAlpha){
					a = (p1[pixel + - 1] & 0xff); //alpha
				}
				else{
					a = 255;
				}
				b = (p1[pixel + 0] & 0xff); // blue
				g = (p1[pixel + 1] & 0xff); // green
				r = (p1[pixel + 2] & 0xff); // red
			}
			dots[row][col].addColor(new Color(r,g,b,a));
		}	
		return generateImage(dots, image.getWidth(), image.getHeight(), includeAlpha);
	}
	
	public static BufferedImage generateImage(ColoredDot[][] dots, int width, int height, boolean includeAlpha){
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR); 
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		if(!includeAlpha){
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);
		}
		for(int r = 0; r < dots.length; r++){
			for(int c = 0; c < dots[0].length; c++){
				g.setColor(dots[r][c].getColor());
				g.fill(dots[r][c]);
			}
		}
		return image;
	}

}
