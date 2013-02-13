/**
 * 2013 Archie Monji
 * 
 * CanvasPanel
 * UI Component to help simplify logic for drawing on the Canvas
 * Actual drawing happens in the Canvas class
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class CanvasPanel extends JPanel {
	private Canvas canvas;
	JPanel borderPanel;
	public CanvasPanel(String title, Canvas ic){
		canvas = ic;
		Border border = BorderFactory.createLineBorder(Color.black, ImageEvolverFrame.borderThickness);
		
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		
		JLabel label = new JLabel(title);
		label.setBackground(Color.white);
		label.setOpaque(true);
		add(label);
		borderPanel = new JPanel();
		borderPanel.setBorder(border);//createBevelBorder(BevelBorder.RAISED));
		borderPanel.setLayout(null);
		borderPanel.add(canvas);
		canvas.setLocation(ImageEvolverFrame.borderThickness,ImageEvolverFrame.borderThickness);
		//imageCanvas.setPreferredSize(new Dimension(100,100));
		add(borderPanel);
		//setSize(120,110);
	}
	
	public void updateSize(int width, int height){
		Dimension newSize = new Dimension(width + 2 * ImageEvolverFrame.borderThickness,height + 2 * ImageEvolverFrame.borderThickness);
		borderPanel.setMaximumSize(newSize);
		borderPanel.setMinimumSize(newSize);
		borderPanel.setPreferredSize(newSize);
		canvas.setSize(width, height);
	}
	
	public Canvas getCanvas(){
		return canvas;
	}
}
