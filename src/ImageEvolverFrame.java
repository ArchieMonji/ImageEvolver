import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageEvolverFrame extends JFrame {
	int polyCount = 10;
	int vertCount = 4;
	TextField loadFileTextField = new TextField("Path to Image");
	JButton loadFileButton = new JButton("Load Image");
	JButton screenshotButton = new JButton("Capture");
	JButton screenshotButton2 = new JButton("Capture Comparison");
	Canvas originalImageCanvas = new Canvas();
	Canvas bestAttemptCanvas = new Canvas();
	Canvas testCanvas = new Canvas();
	CanvasPanel cp = new CanvasPanel("Original", originalImageCanvas);
	CanvasPanel cp2 = new CanvasPanel("Best", bestAttemptCanvas);
	CanvasPanel cp3 = new CanvasPanel("Try", testCanvas);
	JLabel console = new JLabel("By Archie Monji, inspired by http://alteredqualia.com/");
	BufferedImage image;
	FileDialog fileDialog = new FileDialog(new Frame(), "Select Image");
	JPanel canvasPanelContainer = new JPanel();
	public static int borderThickness = 3;
	
	JPanel polyPanel = new JPanel();
	JLabel polyLabel = new JLabel("Polygons: " + polyCount);
	JButton addPoly = new JButton("+");
	JButton subPoly = new JButton("-");
	
	JPanel vertPanel = new JPanel();
	JLabel vertLabel = new JLabel("Vertices: " + vertCount);
	JButton addVert = new JButton("+");
	JButton subVert = new JButton("-");

	JButton refreshButton = new JButton("Refresh");
	JButton clearButton = new JButton("Clear");
	ArrayList<Component> modComponentList = new ArrayList<Component>();
	private JButton randomizeButton = new JButton("Randomize");
	private JButton runButton = new JButton("Run");
	private String lastFileName;
	private boolean isRunning;
	ImageEvolver evolver = new ImageEvolver(this);
	JLabel mutationsLabel = new JLabel("0");
	JLabel improvementsLabel = new JLabel("0");
	JLabel fitnessLabel = new JLabel("0");
	CanvasPainter canvasPainter = new CanvasPainter();
	String originalFileName = "";
	public static void main(String[] args){
		ImageEvolverFrame program = new ImageEvolverFrame();
		program.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		program.setLayout(null);
		program.init();
		program.setVisible(true);
	}
	
	public void init(){
		this.setTitle("Image Evolver 0.1");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		this.add(loadFileTextField);
		modComponentList.add(loadFileTextField);
		loadFileTextField.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent arg0) {
				ImageEvolverFrame.this.loadImage();
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
		});
		/**
		this.add(loadFileButton);
		loadFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				loadImage(new File(loadFileTextField.getText()));
			}
		});
		 **/
		//Init image canvases and border panels-----------------
		canvasPanelContainer.setLayout(new FlowLayout());
		canvasPanelContainer.add(cp);
		canvasPanelContainer.add(cp2);
		canvasPanelContainer.add(cp3);
		/**borderPanel.setBorder(border);//createBevelBorder(BevelBorder.RAISED));
		borderPanel.setLayout(null);
		imagePanel.add(borderPanel);
		borderPanel.add(imageCanvas);
		imageCanvas.setLocation(borderThickness,borderThickness);
		imageCanvas.setPreferredSize(new Dimension(100,100));

		borderPanel2.setBorder(border);//createLineBorder(Color.black));
		borderPanel2.setLayout(null);
		imagePanel.add(borderPanel2);
		borderPanel2.add(imageCanvas2);
		imageCanvas2.setLocation(borderThickness,borderThickness);
		imageCanvas2.setPreferredSize(new Dimension(100,100));

		borderPanel3.setBorder(border);//createLineBorder(Color.black));
		borderPanel3.setLayout(null);
		imagePanel.add(borderPanel3);
		borderPanel3.add(imageCanvas3);
		imageCanvas3.setLocation(borderThickness,borderThickness);
		imageCanvas3.setPreferredSize(new Dimension(100,100));**/
		canvasPanelContainer.setPreferredSize(new Dimension(0,500));
		this.add(canvasPanelContainer);
		//------------------------------------------------------

		JPanel mutationsPanel = new JPanel();
		mutationsPanel.add(new JLabel("Mutations: "));
		mutationsLabel.setPreferredSize(new Dimension(100,30));
		mutationsPanel.add(mutationsLabel);
		mutationsPanel.add(new JLabel("Improvements: "));
		mutationsPanel.add(improvementsLabel);
		improvementsLabel.setPreferredSize(new Dimension(100,30));
		mutationsPanel.add(new JLabel("Fitness: "));
		mutationsPanel.add(fitnessLabel);
		fitnessLabel.setPreferredSize(new Dimension(100,30));
		add(mutationsPanel);
		
		polyPanel.setLayout(new GridLayout(1,3));
		polyPanel.add(polyLabel);
		polyPanel.add(subPoly);
		modComponentList.add(subPoly);
		//polyPanel.setPreferredSize(new Dimension(10, 5));
		subPoly.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(polyCount > 1){
					polyCount--;
					polyLabel.setText("Polygons: " + polyCount);
					updateEvolver();
				}
			}
		});
		polyPanel.add(addPoly);
		modComponentList.add(addPoly);
		addPoly.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				polyCount++;
				polyLabel.setText("Polygons: " + polyCount);
				updateEvolver();
			}
		});
		add(polyPanel);

		vertPanel.setLayout(new GridLayout(1,3));
		vertPanel.add(vertLabel);
		vertPanel.add(subVert);
		modComponentList.add(subVert);
		subVert.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(vertCount > 3){
					vertCount--;
					vertLabel.setText("Vertices: " + vertCount);
					updateEvolver();
				}
			}
		});
		vertPanel.add(addVert);
		modComponentList.add(addVert);
		addVert.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				vertCount++;
				vertLabel.setText("Vertices: " + vertCount);
				updateEvolver();
			}
		});
		add(vertPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(randomizeButton);
		modComponentList.add(randomizeButton);
		randomizeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				randomizePolygons();
				console.setText("Randomized.");
			}
		});
		buttonPanel.add(screenshotButton);
		//this.add(screenshotButton);
		screenshotButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				capture();
			}
		});
		buttonPanel.add(screenshotButton2);
		//this.add(screenshotButton);
		screenshotButton2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				captureBothCanvases();
			}
		});
		buttonPanel.add(refreshButton);
		modComponentList.add(refreshButton);
		refreshButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				updateEvolver();
				paint(getGraphics());
				console.setText("Refeshed.");
			}
		});

		buttonPanel.add(clearButton);
		modComponentList.add(clearButton);
		clearButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				if(evolver.isRunning()){
					evolver.stopRunning();
					evolver.clearPolygons();
					evolver.startRunning();
				}
				else{
					evolver.clearPolygons();
				}
				evolver.clear();
				bestAttemptCanvas.clear();
				testCanvas.clear();
				mutationsLabel.setText("0");
				improvementsLabel.setText("0");
				fitness = 0;
				fitnessLabel.setText(MessageFormat.format("{0,number,#.##%}", fitness));
				console.setText("Cleared.");
			}
		});

		buttonPanel.add(runButton);
		runButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				//updateEvolver();
				if(evolver.isRunning()){
					evolver.stopRunning();
					for(Component c: modComponentList)
						c.setEnabled(true);
					console.setText("Stopped.");
				}
				else{
					evolver.startRunning();
					for(Component c: modComponentList)
						c.setEnabled(false);
					console.setText("Running.");
				}
			}
		});
		add(buttonPanel);

		JPanel consolePanel = new JPanel();
		add(consolePanel);
		console.setPreferredSize(new Dimension(400,30));
		consolePanel.add(console);
		cp.updateSize(200,200);
		cp2.updateSize(200,200);
		cp3.updateSize(200,200);
		fileDialog.setDirectory(System.getProperty("user.dir"));
		this.setSize(800,600);
	}

	protected void randomizePolygons() {
		evolver.setParameters(polyCount, vertCount, originalImageCanvas.getWidth(), originalImageCanvas.getHeight());
		evolver.setTestImage(evolver.createRandomPolygons());
		testCanvas.setImage(evolver.getTestImage());
		//System.out.println("TEST: " + evolver.calculatePixelSum(evolver.getTestImage()));
		repaint();
	}

	/**
	public void randomizePolygons() {
		canvas3.polygons.clear();
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		for(int i = 0; i < polyCount; i++){
			ColoredPolygon p = new ColoredPolygon();
			Random r = new Random();
			/**
			float red = r.nextFloat();
			float blue = r.nextFloat();
			float green = r.nextFloat();
			float alpha = r.nextFloat();
			Color c = new Color(red,blue,green,alpha);
			System.out.println(red + " " + blue + " " + green + " " + alpha);

			p.setColor(new Color(r.nextFloat(),r.nextFloat(),r.nextFloat(),r.nextFloat()));
			for(int v = 0; v < vertCount; v++)
				p.addPoint((int)(r.nextDouble() * w),(int)(r.nextDouble() * h));
			canvas3.polygons.add(p);
		}
		console.setText("Randomized");
		paint(getGraphics());
	}
	 **/
	//Remembers last save path
	//Provides default formatted save path for convenience
	//if user input own save path, then user's save path will be suggested for consequence saves, otherwise uses default format
	private boolean userSetName;
	
	public void capture() {
		DateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd] (HH-mm-ss)");
		Date date = new Date();
		String defaultFileName = "";
		if(!userSetName){
			lastFileName = defaultFileName = dateFormat.format(date) + " " + originalFileName + 
					" (" + MessageFormat.format("{0,number,##%}",fitness) + "fit " + polyCount + "p " + vertCount + "v)";
		
		}
		fileDialog.setMode(FileDialog.SAVE);
		fileDialog.setFile(lastFileName);
		fileDialog.setVisible(true);
		try {
			if(fileDialog.getFile() == null)
				return;
			if(!fileDialog.getFile().equals(defaultFileName)){
				System.out.println("e");
				userSetName = true;
			}
			lastFileName = fileDialog.getDirectory() + fileDialog.getFile();
			BufferedImage imageToSave = evolver.getTestImage();
			if(lastFileName.length() >= 4 && lastFileName.substring(lastFileName.length()-4).equals(".png")){
				ImageIO.write(imageToSave, "PNG", new File(lastFileName));
			}
			else{
				//appends ".png" to file name so file is saved as PNG instead of File
				ImageIO.write(imageToSave, "PNG", new File(lastFileName + ".png"));
			}
			console.setText("Capture saved to: " + lastFileName);
		} catch (IOException e) {
			console.setText("Error saving.");
		}
	}
	
	public void captureBothCanvases() {
		DateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd] (HH-mm-ss)");
		Date date = new Date();
		String defaultFileName = "";
		if(!userSetName){
			lastFileName = defaultFileName = dateFormat.format(date) + " " + originalFileName + 
					" (" + MessageFormat.format("{0,number,##%}",fitness) + "fit " + polyCount + "p " + vertCount + "v)";
		}
		fileDialog.setMode(FileDialog.SAVE);
		fileDialog.setFile(lastFileName);
		fileDialog.setVisible(true);
		try {
			if(fileDialog.getFile() == null)
				return;
			if(!fileDialog.getFile().equals(defaultFileName)){
				System.out.println("e");
				userSetName = true;
			}
			lastFileName = fileDialog.getDirectory() + fileDialog.getFile();
			BufferedImage imageToSave = canvasPainter.generateComparisonImage(originalImageCanvas,bestAttemptCanvas);

			if(lastFileName.length() >= 4 && lastFileName.substring(lastFileName.length()-4).equals(".png")){
				ImageIO.write(imageToSave, "PNG", new File(lastFileName));
			}
			else{
				//appends ".png" to file name so file is saved as PNG instead of File
				ImageIO.write(imageToSave, "PNG", new File(lastFileName + ".png"));
			}
			console.setText("Capture saved to: " + lastFileName);
		} catch (IOException e) {
			console.setText("Error saving.");
		}
	}

	public void loadImage(){
		fileDialog.setMode(FileDialog.LOAD);
		fileDialog.setVisible(true);
		if(fileDialog.getFile() != null){
			loadFileTextField.setText(fileDialog.getDirectory() + fileDialog.getFile());
		}
		try {
			image = ImageIO.read(new File(fileDialog.getDirectory() + fileDialog.getFile()));//Program.class.getResource("test.png"));//loadFileTextField.getText()));
			if(image != null){
				originalImageCanvas.setImage(image);
				originalFileName =  fileDialog.getFile();
				//Dimension newSize = new Dimension(imageCanvas.getWidth() + 2 * borderThickness,imageCanvas.getHeight() + 2 * borderThickness);
				cp.updateSize(originalImageCanvas.getWidth(),originalImageCanvas.getHeight());
				cp2.updateSize(originalImageCanvas.getWidth(),originalImageCanvas.getHeight());
				cp3.updateSize(originalImageCanvas.getWidth(),originalImageCanvas.getHeight());
				//updateCanvasPanelDimensions(cp, newSize);
				//updateCanvasPanelDimensions(cp2, newSize);
				//updateCanvasPanelDimensions(cp3, newSize);
				console.setText("Image loaded.");
			}
			else{
				console.setText("Invalid File Type.");
			}
			this.paintComponents(this.getGraphics());
		} catch (IOException e) {
			console.setText("File not found.");
		}
		updateEvolver();
	}	
	//Author Mota - http://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		int[][] result = new int[height][width];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
				argb += ((int) pixels[pixel + 1] & 0xff); // blue
				argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				int argb = 0;
				argb += -16777216; // 255 alpha
				argb += ((int) pixels[pixel] & 0xff); // blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
				result[row][col] = argb;
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}
		return result;
	}

	public void updateCanvases() {
		bestAttemptCanvas.setImage(evolver.getBestImage());
		bestAttemptCanvas.repaint();
		testCanvas.setImage(evolver.getTestImage());
		testCanvas.repaint();
	}

	public void updateTestCanvas() {
		testCanvas.setImage(evolver.getTestImage());
		testCanvas.repaint();
	}

	public void updateBestCanvas() {
		bestAttemptCanvas.setImage(evolver.getBestImage());
		bestAttemptCanvas.repaint();
	}

	public void updateEvolver(){
		boolean resume = false;
		if(evolver.isRunning()){
			resume = true;
			evolver.stopRunning();
		}
		evolver.setImage(image);
		if(evolver.getPolyCount() != polyCount){
			evolver.clearPolygons();
		}
		if(evolver.getVertCount() != vertCount){
			evolver.clearPolygons();
		}
		if(image != null){
			evolver.setParameters(polyCount, vertCount, image.getWidth(), image.getHeight());
		}
		else{
			evolver.setParameters(polyCount, vertCount, 200, 200);
		}
		if(resume){
			evolver.startRunning();
		}
	}
	
	public void updateMutationsLabel(int mutations){
		mutationsLabel.setText(""+ mutations);
	}
	double fitness;
	public void updateFitnessAndImprovementsLabel(int improvements, double fitness){
		improvementsLabel.setText(""+ improvements);
		this.fitness = fitness;
		fitnessLabel.setText(MessageFormat.format("{0,number,#.##%}", fitness));
	}
}
