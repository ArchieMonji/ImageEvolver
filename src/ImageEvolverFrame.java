/**
 * 2013 Archie Monji
 * 
 * ImageEvolverFrame
 * The UI and program entry point.
 */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageEvolverFrame extends JFrame{
	private static final long serialVersionUID = 2L;
	//TODO: Fields need organization/refactoring
	public int polyCount = 75;
	public int vertCount = 6;
	private TextField loadFileTextField = new TextField("Path to Image");
	//private JButton loadFileButton = new JButton("Load Image");
	private JButton screenshotButton = new JButton("Capture");
	private JButton screenshotButton2 = new JButton("Capture Comparison");
	private JButton exportDataButton = new JButton("Export Data");
	private JButton stipplizeButton = new JButton("Stipplize");
	private Canvas originalImageCanvas = new Canvas();
	private Canvas bestAttemptCanvas = new Canvas();
	private Canvas testCanvas = new Canvas();
	private CanvasPanel cp = new CanvasPanel("Original", originalImageCanvas);
	private CanvasPanel cp2 = new CanvasPanel("Best", bestAttemptCanvas);
	private CanvasPanel cp3 = new CanvasPanel("Try", testCanvas);
	private JLabel console = new JLabel("By Archie Monji, inspired by http://alteredqualia.com/");
	private BufferedImage image;
	private FileDialog fileDialog = new FileDialog(new Frame(), "Select Image");
	private JPanel canvasPanelContainer = new JPanel();
	public static int borderThickness = 3;
	private JPanel polyPanel = new JPanel();
	private JLabel polyLabel = new JLabel("Polygons: " + polyCount);
	private JButton addPoly = new JButton("+");
	private JButton subPoly = new JButton("-");
	
	private JPanel vertPanel = new JPanel();
	private JLabel vertLabel = new JLabel("Vertices: " + vertCount);
	private JButton addVert = new JButton("+");
	private JButton subVert = new JButton("-");
	private float dotSize = 3;
	private JPanel dotSizePanel = new JPanel();
	private TextField dotSizeTextField = new TextField(dotSize + "");
	private JButton incSize = new JButton("+");
	private JButton decSize = new JButton("-");
	private JButton refreshButton = new JButton("Refresh");
	private JButton clearButton = new JButton("Clear");
	private ArrayList<Component> modComponentList = new ArrayList<Component>();
	private JButton randomizeButton = new JButton("Randomize");
	private JButton runButton = new JButton("Run");
	private String lastImageFileName;
	private ImageEvolver evolver = new ImageEvolver(this);
	private JLabel mutationsLabel = new JLabel("0");
	private JLabel improvementsLabel = new JLabel("0");
	private JLabel rateLabel = new JLabel("0");
	private JLabel fitnessLabel = new JLabel("0");
	private CanvasPainter canvasPainter = new CanvasPainter();
	private String originalFileName = null;

	
	//Entry point
	public static void main(String[] args){
		ImageEvolverFrame program = new ImageEvolverFrame();
		program.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		program.setLayout(null);
		program.init();
		program.setVisible(true);
	}
	
	//TODO: Need to do something about this.
	public void init(){
		this.setTitle("Image Evolver 0.1");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		this.add(loadFileTextField);
		modComponentList.add(loadFileTextField);
		loadFileTextField.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent arg0){
				ImageEvolverFrame.this.loadImage();
			}
			@Override
			public void mouseEntered(MouseEvent arg0){}
			@Override
			public void mouseExited(MouseEvent arg0){}
			@Override
			public void mousePressed(MouseEvent arg0){}
			@Override
			public void mouseReleased(MouseEvent arg0){}
		});
		/**
		this.add(loadFileButton);
		loadFileButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
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
		mutationsLabel.setPreferredSize(new Dimension(70,30));
		mutationsPanel.add(mutationsLabel);
		mutationsPanel.add(new JLabel("Rate: "));
		mutationsPanel.add(rateLabel);
		rateLabel.setPreferredSize(new Dimension(30,30));
		JLabel mutpersecLabel = new JLabel("mutations/sec");
		mutationsPanel.add(mutpersecLabel);
		mutpersecLabel.setPreferredSize(new Dimension(100,30));
		mutationsPanel.add(new JLabel("Improvements: "));
		mutationsPanel.add(improvementsLabel);
		improvementsLabel.setPreferredSize(new Dimension(70,30));
		mutationsPanel.add(new JLabel("Fitness: "));
		mutationsPanel.add(fitnessLabel);
		fitnessLabel.setPreferredSize(new Dimension(70,30));
		add(mutationsPanel);
		
		polyPanel.setLayout(new GridLayout(1,3));
		polyPanel.add(polyLabel);
		polyPanel.add(subPoly);
		modComponentList.add(subPoly);
		//polyPanel.setPreferredSize(new Dimension(10, 5));
		subPoly.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(polyCount > 1){
					polyCount--;
					polyLabel.setText("Polygons: " + polyCount);
				}
			}
		});
		polyPanel.add(addPoly);
		modComponentList.add(addPoly);
		addPoly.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				polyCount++;
				polyLabel.setText("Polygons: " + polyCount);
			}
		});
		add(polyPanel);

		vertPanel.setLayout(new GridLayout(1,3));
		vertPanel.add(vertLabel);
		vertPanel.add(subVert);
		modComponentList.add(subVert);
		subVert.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(vertCount > 3){
					vertCount--;
					vertLabel.setText("Vertices: " + vertCount);
				}
			}
		});
		vertPanel.add(addVert);
		modComponentList.add(addVert);
		addVert.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				vertCount++;
				vertLabel.setText("Vertices: " + vertCount);
			}
		});
		add(vertPanel);

		dotSizePanel.setLayout(new GridLayout(1,4));
		JLabel dotSizeLabel = new JLabel("Dot Size: ");
		dotSizePanel.add(dotSizeLabel);
		dotSizePanel.add(dotSizeTextField);
		dotSizePanel.add(decSize);
		decSize.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(dotSize > 1){
					dotSize -= 1;	
					dotSizeTextField.setText(dotSize + "");
				}
			}
		});
		dotSizePanel.add(incSize);
		incSize.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				dotSize += 1;	
				dotSizeTextField.setText(dotSize + "");
			}
		});
	
		add(dotSizePanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(randomizeButton);
		modComponentList.add(randomizeButton);
		randomizeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				randomizePolygons();
				console.setText("Randomized.");
			}
		});
		buttonPanel.add(stipplizeButton);
		modComponentList.add(stipplizeButton);
		stipplizeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				if(image != null){
					try{
						dotSize = Float.parseFloat(dotSizeTextField.getText());
						if(dotSize < .1){
							console.setText("Enter a size > .1.");
							return;
						}
						bestAttemptCanvas.setImage(StippledImageGenerator.generateStippledImage(
								image, dotSize, false));
						bestAttemptCanvas.repaint();
						console.setText("Stippled.");
					}
					catch(NumberFormatException e){
						console.setText("Dot Size NaN.");
					}
				}
				else{
					console.setText("There's no image!");
				}
			}
		});
		buttonPanel.add(exportDataButton);
		exportDataButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				try {
					exportPolygonData();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					console.setText("Data write error");
				}
			}
		});
		buttonPanel.add(screenshotButton);
		//this.add(screenshotButton);
		screenshotButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				capture();
			}
		});
		buttonPanel.add(screenshotButton2);
		//this.add(screenshotButton);
		screenshotButton2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				captureBothCanvases();
			}
		});
		buttonPanel.add(refreshButton);
		modComponentList.add(refreshButton);
		refreshButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				updateEvolver();
				paint(getGraphics());
				console.setText("Refeshed.");
			}
		});

		buttonPanel.add(clearButton);
		modComponentList.add(clearButton);
		clearButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
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
				rateLabel.setText("0");
				fitness = 0;
				fitnessLabel.setText(MessageFormat.format("{0,number,#.##%}", fitness));
				console.setText("Cleared.");
			}
		});

		buttonPanel.add(runButton);
		runButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				//updateEvolver();
				if(evolver.isRunning()){
					evolver.stopRunning();
					for(Component c: modComponentList)
						c.setEnabled(true);
					console.setText("Stopped.");
				}
				else{
					if(evolver.getMaxPolyCount() != polyCount || evolver.getVertCount() != vertCount)
					{
						updateEvolver();
					}
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

	protected void randomizePolygons(){
		evolver.clear();
		evolver.setParameters(polyCount, vertCount, originalImageCanvas.getWidth(), originalImageCanvas.getHeight());
		evolver.setTestImage(evolver.createRandomPolygons());
		testCanvas.setImage(evolver.getTestImage());
		//System.out.println("TEST: " + evolver.calculatePixelSum(evolver.getTestImage()));
		repaint();
	}


	/*
	 * Remembers last save path
	 * Provides default formatted save path for convenience
	 * if user input own save path, then user's save path will be 
	 * suggested for consequence, otherwise uses default format
	*/
	private boolean userSetImageName;
	
	//TODO: Maybe Refactor into a ImageSaver class
	public void capture(){
		DateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd] (HH-mm-ss)");
		Date date = new Date();
		String defaultFileName = "";
		if(!userSetImageName){
			lastImageFileName = defaultFileName = originalFileName + " " + dateFormat.format(date) +  
					" (" + MessageFormat.format("{0,number,##.#%}",fitness) + "fit " + MessageFormat.format("{0,number,####}",mutations/1000) + "k_muts " + polyCount + "p " + vertCount + "v)";
		}
		fileDialog.setMode(FileDialog.SAVE);
		fileDialog.setFile(lastImageFileName);
		fileDialog.setVisible(true);
		try{
			if(fileDialog.getFile() == null)
				return;
			if(!fileDialog.getFile().equals(defaultFileName)){
				userSetImageName = true;
			}
			lastImageFileName = fileDialog.getDirectory() + fileDialog.getFile();
			BufferedImage imageToSave = canvasPainter.generateImageFromCanvas(bestAttemptCanvas);
			if(lastImageFileName.length() >= 4 && lastImageFileName.substring(lastImageFileName.length()-4).equals(".png")){
				ImageIO.write(imageToSave, "PNG", new File(lastImageFileName));
			}
			else{
				//appends ".png" to file name so file is saved as PNG instead of File
				ImageIO.write(imageToSave, "PNG", new File(lastImageFileName + ".png"));
			}
			console.setText("Capture saved to: " + lastImageFileName);
		} catch (IOException e){
			console.setText("Error saving.");
		}
	}
	
	public void captureBothCanvases(){
		DateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd] (HH-mm-ss)");
		Date date = new Date();
		String defaultFileName = "";
		if(!userSetImageName){
			lastImageFileName = defaultFileName = originalFileName + " " + dateFormat.format(date) +  
					" (" + MessageFormat.format("{0,number,##.#%}",fitness) + "fit " + MessageFormat.format("{0,number,####}",mutations/1000) + "k_muts " + polyCount + "p " + vertCount + "v)";
		}
		fileDialog.setMode(FileDialog.SAVE);
		fileDialog.setFile(lastImageFileName);
		fileDialog.setVisible(true);
		try{
			if(fileDialog.getFile() == null)
				return;
			if(!fileDialog.getFile().equals(defaultFileName)){
				System.out.println("e");
				userSetImageName = true;
			}
			lastImageFileName = fileDialog.getDirectory() + fileDialog.getFile();
			BufferedImage imageToSave = canvasPainter.generateComparisonImage(originalImageCanvas,bestAttemptCanvas);

			if(lastImageFileName.length() >= 4 && lastImageFileName.substring(lastImageFileName.length()-4).equals(".png")){
				ImageIO.write(imageToSave, "PNG", new File(lastImageFileName));
			}
			else{
				//appends ".png" to file name so file is saved as PNG instead of File
				ImageIO.write(imageToSave, "PNG", new File(lastImageFileName + ".png"));
			}
			console.setText("Capture saved to: " + lastImageFileName);
		} catch (IOException e){
			console.setText("Error saving.");
		}
	}

	public void loadImage(){
		fileDialog.setMode(FileDialog.LOAD);
		fileDialog.setVisible(true);
		if(fileDialog.getFile() != null){
			loadFileTextField.setText(fileDialog.getDirectory() + fileDialog.getFile());
		}
		try{
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
		} catch (IOException e){
			console.setText("File not found.");
		}
		updateEvolver();
	}	

	//TODO: could use work.
	public void updateCanvases(){
		bestAttemptCanvas.setImage(evolver.getBestImage());
		bestAttemptCanvas.repaint();
		testCanvas.setImage(evolver.getTestImage());
		testCanvas.repaint();
	}

	public void updateTestCanvas(){
		testCanvas.setImage(evolver.getTestImage());
		testCanvas.repaint();
	}

	public void updateBestCanvas(){
		bestAttemptCanvas.setImage(evolver.getBestImage());
		bestAttemptCanvas.repaint();
	}

	/*
	 * Pause evolver, set evolver's parameters, then resume (if was running).
	 */
	public void updateEvolver(){
		boolean resume = false;
		if(evolver.isRunning()){
			resume = true;
			evolver.stopRunning();
		}
		evolver.setImage(image);
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
	int mutations;
	public void updateMutationsLabel(int mutations){
		this.mutations = mutations;
		mutationsLabel.setText(""+ mutations);
	}
	
	private double fitness;
	//TODO: refactor
	public void updateFitnessAndImprovementsLabel(int improvements, double fitness){
		improvementsLabel.setText(""+ improvements);
		this.fitness = fitness;
		fitnessLabel.setText(MessageFormat.format("{0,number,#.##%}", fitness));
	}
	
	private boolean userSetDataName = false;
	private String lastDataFileName = null;
	public void exportPolygonData() throws IOException{
		fileDialog.setMode(FileDialog.SAVE);
		String defaultFileName = null;
		if(originalFileName == null){
			defaultFileName = "untitled.txt";
		}
		else{
			defaultFileName = originalFileName.substring(0,originalFileName.length() - 4) + ".txt";
		}
		if(!userSetDataName){
			fileDialog.setFile(defaultFileName);
		}
		else{
			fileDialog.setFile(lastDataFileName);
		}
		fileDialog.setVisible(true);
		try{
			if(fileDialog.getFile() == null)
				return;
			String saveLocation= "";
			saveLocation = fileDialog.getDirectory() + fileDialog.getFile();
			if(!fileDialog.getFile().equals(defaultFileName)){
				lastDataFileName = saveLocation;
				saveLocation += ".txt";
				userSetDataName = true;
			}

			FileOutputStream fis = null;
			fis = new FileOutputStream(saveLocation); 
			OutputStreamWriter out = new OutputStreamWriter(fis, "UTF-8");
			List<ColoredPolygon> polygons = evolver.getPolygons();
			if(polygons.size() != 0){
				out.write(polygons.size() + " " + polygons.get(0).npoints+ System.getProperty("line.separator"));
				for(ColoredPolygon p: polygons){
					System.out.println(p.toString());
					out.write(p.toString() + System.getProperty("line.separator"));
				}
			}
			out.close();
			console.setText("Data saved to: " + saveLocation);
		} catch (IOException e){
			console.setText("Error saving.");
			e.printStackTrace();
		}
	}

	public void updateTimePassed(long timePassed) {
		rateLabel.setText(mutations*1000/timePassed + "");
		
	}
}
