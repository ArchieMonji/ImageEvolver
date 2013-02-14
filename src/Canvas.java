/**
 * 2013 Archie Monji
 * 
 * Canvas
 * For displaying images
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Canvas extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage image;
	//public ArrayList<ColoredPolygon> polygons = new ArrayList<ColoredPolygon>();
	public Canvas(){
		//this.setBorder(BorderFactory.createLineBorder(Color.cyan));
		setSize(200,200);
		setBackground(Color.white);
		//setOpaque(false);
		setLayout(null);
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		//Graphics2D g2 = (Graphics2D) g;
		if(image != null){
			g.drawImage(image, 0, 0, this);
		}
		/**
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		for(ColoredPolygon p: polygons){
			g.setColor(p.getColor());
			g.fillPolygon(p);
		}
		**/
		//g.setColor(Color.red);
		//g.drawRect(0,0,3,3);
	}

	public void clear(){
		image = null;
		repaint();
	}
	
	public void setImage(BufferedImage i){
		image = i;
		if(i != null){
			this.setSize(new Dimension(i.getWidth(this),i.getHeight(this)));
		}
		else{
			setSize(200,200);
		}
	}
	
	public BufferedImage getCanvasImage(){
		//BufferedImage.TYPE_INT_ARGB + setOpaque(false) - for images that include alpha (are transparent) 
		BufferedImage newImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB); 
		Graphics g = newImage.getGraphics();
		paintComponent(g);
		//image = newImage;
		return newImage;
	}
	
	public BufferedImage getImage(){
		return image;
	}
}
