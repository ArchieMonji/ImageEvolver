import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Random;


public class ImageEvolver extends Thread{
	private boolean isRunning;
	private boolean isFinished;
	private BufferedImage orig, best, test;
	private ImageEvolverFrame ui;
	private ArrayList<ColoredPolygon> polygons = new ArrayList<ColoredPolygon>();
	private int polyCount, vertCount, width, height;
	private double lastEval = Double.MAX_VALUE;
	private int mutations;
	private int improvements;
	private double pixelSumOrig;
	public ImageEvolver(ImageEvolverFrame ui) {
		start();
		this.ui = ui;
		polyCount = 5;
		vertCount = 6;
		width = 200;
		height = 200;
	}

	public void setParameters(int polyCount, int vertCount, int w, int h){
		this.polyCount = polyCount;
		this.vertCount = vertCount;
		this.width = w;
		this.height = h;
	}

	@Override
	public void run() {
		while(true){
			while(isRunning) {
				Random random = new Random();
				boolean createdPolygon = false;
				//if less polygons than max, consider creating new polygon
				if(polygons.size() < polyCount){
					createdPolygon = addPolygon(random);
				}
				//mutating existing polygon
				if(createdPolygon != true){
					boolean changeColor = random.nextBoolean();
					if(changeColor){
						mutateColor(polygons.get(random.nextInt(polygons.size())), random);
					}
					else{
						mutateVertice(polygons.get(random.nextInt(polygons.size())), random);
					}
					ui.updateMutationsLabel(++mutations);
				}
				//System.out.println("Best: " + lastEval + " | New: " + calculateSumOfPixelDifferences(orig,test));
			}
			try {
				sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void mutateVertice(ColoredPolygon testPolygon, Random random){
		int verticeToChange = random.nextInt(vertCount); 
		int oldX = testPolygon.xpoints[verticeToChange];
		int oldY = testPolygon.ypoints[verticeToChange];
		testPolygon.xpoints[verticeToChange] = random.nextInt(width);
		testPolygon.ypoints[verticeToChange] = random.nextInt(height);
		test = createImageFromPolygons(polygons);
		if(mutations % uiUpdateDelay == 0){
			ui.updateTestCanvas();
		}
		//System.out.println("ORIG: " + calculatePixelSum(orig));
		//System.out.println("TEST: " + calculatePixelSum(test));
		if(!goodMutation()){
			testPolygon.xpoints[verticeToChange] = oldX;
			testPolygon.ypoints[verticeToChange] = oldY;
		}
	}

	private void mutateColor(ColoredPolygon testPolygon, Random random){
		Color oldColor = testPolygon.getColor();
		Color newColor = null;
		int parameterToChange = random.nextInt(5);
		float value = random.nextFloat();
		switch(parameterToChange){
		case 0: newColor = new Color(value,(float)oldColor.getGreen()/255, (float)oldColor.getBlue()/255, (float)oldColor.getAlpha()/255); break;
		case 1: newColor = new Color((float)oldColor.getRed()/255, value, (float)oldColor.getBlue()/255, (float)oldColor.getAlpha()/255); break;
		case 2: newColor = new Color((float)oldColor.getRed()/255, (float)oldColor.getGreen()/255, value, (float)oldColor.getAlpha()/255); break;
		case 3: newColor = new Color((float)oldColor.getRed()/255, (float)oldColor.getGreen()/255,(float)oldColor.getBlue()/255, value); break;
		case 4: newColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat()); break;
		default:  newColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat()); break;
		}
		testPolygon.setColor(newColor);
		test = createImageFromPolygons(polygons);
		if(mutations % uiUpdateDelay == 0){
			ui.updateTestCanvas();
		}
		if(!goodMutation()){
			testPolygon.setColor(oldColor);
		}
	}
	int uiUpdateDelay = 50;
	private boolean addPolygon(Random random){
		//if no polygons, try making a polygon 
		if(polygons.size() == 0){
			ColoredPolygon testPolygon = createRandomPolygon();
			polygons.add(testPolygon);
			test = createImageFromPolygons(polygons);
			if(mutations % uiUpdateDelay == 0){
				ui.updateTestCanvas();
			}
			if(!goodMutation()){
				polygons.remove(testPolygon);
			}
			return true;
		}
		else{
			//choose between making new polygon or mutating existing polygon
			boolean createNewPolygon = random.nextBoolean();
			if(createNewPolygon){
				ColoredPolygon testPolygon = createRandomPolygon();
				polygons.add(testPolygon);
				test = createImageFromPolygons(polygons);
				if(mutations % uiUpdateDelay == 0){
					ui.updateTestCanvas();
				}
				if(!goodMutation()){
					polygons.remove(testPolygon);
				}
				return true;
			}
		}
		return false;
	}

	private ColoredPolygon createRandomPolygon() {
		Random r = new Random();
		ColoredPolygon p = new ColoredPolygon(Color.black);//new ColoredPolygon(new Color(r.nextFloat(),r.nextFloat(),r.nextFloat(),r.nextFloat()));
		for(int v = 0; v < vertCount; v++)
			p.addPoint((int)(r.nextDouble() * width),(int)(r.nextDouble() * height));
		return p;
	}

	public void startRunning() {
		isRunning = true;
	}

	public void stopRunning() {
		isRunning = false;
	}

	public ArrayList<ColoredPolygon> initRandomizedPolygons() {
		ArrayList<ColoredPolygon> polygons = new ArrayList<ColoredPolygon>();
		for(int i = 0; i < polyCount; i++){
			polygons.add(createRandomPolygon());
		}
		return polygons;
	}

	public double calculatePixelSum(BufferedImage bim){
		if(bim == null) return Double.MAX_VALUE;
		final byte[] pixels = ((DataBufferByte) bim.getRaster().getDataBuffer()).getData();
		final boolean hasAlphaChannel = bim.getAlphaRaster() != null;
		double sum = 0;
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				sum += ((double) pixels[pixel])/255; // alpha
				//System.out.println(sum);
				sum += ((double) pixels[pixel + 1])/255; // blue
				//System.out.println(sum);
				sum += (((double) pixels[pixel + 2]))/255; // green
				//System.out.println(sum);
				sum += (((double) pixels[pixel + 3]))/255; // red
				//System.out.println(sum);
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				sum += ((double) pixels[pixel])/255; // blue
				sum += (((double) pixels[pixel + 1]))/255; // green
				sum += (((double) pixels[pixel + 2]))/255; // red
			}
		}
		return sum;
	}

	public double calculatePixelSum(BufferedImage bim, boolean includeAlpha){
		if(bim == null) return Double.MAX_VALUE;
		final byte[] pixels = ((DataBufferByte) bim.getRaster().getDataBuffer()).getData();
		final boolean hasAlphaChannel = bim.getAlphaRaster() != null;
		double sum = 0;
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				if(includeAlpha){
					sum += (pixels[pixel]  & 0xff);// << 24; 
					//System.out.println("A: " + (pixels[pixel] & 0xff));
				}
				sum += (pixels[pixel + 1]  & 0xff); 
				//System.out.print(", B: " + (pixels[pixel + 1] & 0xff));
				sum += (pixels[pixel + 2]  & 0xff);// << 8;
				//System.out.print(", G: " + (pixels[pixel + 2] & 0xff));
				sum += (pixels[pixel + 3]  & 0xff);// << 16;
				//System.out.println(", R: " + (pixels[pixel + 3] & 0xff));
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				sum += (pixels[pixel] & 0xff); // blue
				//System.out.print("B: " + (pixels[pixel] & 0xff));
				sum += (pixels[pixel + 1] & 0xff); // green
				//System.out.print(", G: " + (pixels[pixel + 1] & 0xff));
				sum += (pixels[pixel + 2] & 0xff); // red
				//System.out.println(", R: " + (pixels[pixel + 2] & 0xff));
			}
		}
		return sum;
	}
	
	public double subEval(int b1, int g1, int r1, int b2, int g2, int r2){
		//TODO:
			return 0;
	}
	

	private double calculateSumOfPixelDifferences(BufferedImage bim1, BufferedImage bim2) {
		if(bim1 == null || bim2 == null || bim1.getWidth() != bim2.getWidth() || bim1.getHeight() != bim2.getHeight()){
			return Double.MAX_VALUE;
		}
		//double bim1Sum = calculatePixelSum(bim1, false);
		//System.out.print("I1: " + bim1Sum);
		double bim2Sum = calculatePixelSum(bim2, false);
		//System.out.println(" | I2: " + bim2Sum);
		return pixelSumOrig - bim2Sum;
	}	

	public void paintImage(Graphics g, ArrayList<ColoredPolygon> polygons){
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setColor(Color.white);
		g2.fillRect(0, 0, width, height);
		for(ColoredPolygon p: polygons){
			g2.setColor(p.getColor());
			g2.fillPolygon(p);
		}
		//g.setColor(Color.red);
		//g.drawRect(0,0,3,3);
	}

	public void setImage(BufferedImage i){
		orig = i;
		if(i != null){
			width = orig.getWidth();
			height = orig.getHeight();
			pixelSumOrig = this.calculatePixelSum(orig, false);
		}
	}

	public BufferedImage getTestImage(){
		return test;
	}

	public BufferedImage createImageFromPolygons(ArrayList<ColoredPolygon> polygons){
		//BufferedImage.TYPE_INT_ARGB + setOpaque(false) - for images that include alpha (are transparent) 
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR); 
		Graphics g = newImage.getGraphics();
		paintImage(g, polygons);
		return newImage;
	}

	public BufferedImage getOriginalImage(){
		return orig;
	}

	public BufferedImage getBestImage(){
		return best;
	}

	public BufferedImage createRandomPolygons() {
		return createImageFromPolygons(initRandomizedPolygons());
	}

	public boolean isRunning() {
		return isRunning;
	}

	public BufferedImage deepCopy(BufferedImage bim) {
		ColorModel cm = bim.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bim.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public int getVertCount() {
		return polyCount;
	}

	public int getPolyCount() {
		return polyCount;
	}

	public void clearPolygons() {
		polygons.clear();
		best = test = null;
		lastEval = Double.MAX_VALUE;
		pixelSumOrig = 0;
	}

	public void setTestImage(BufferedImage image){
		test = image;
	}

	//returns true if positive mutation
	public boolean goodMutation(){
		double newEval =  evaluate();
		if(newEval <= lastEval){
			lastEval = newEval;
			best = test;
			ui.updateBestCanvas();
			ui.updateFitnessAndImprovementsLabel(++improvements, Math.abs(1 - lastEval/255.0/3/width/height));
			return true;
		}
		return false;
	}
	
	public double evaluate(){
		return Math.abs(pixelSumOrig - calculatePixelSum(test, false));
	}
	
	public void clear() {
		clearPolygons();
		mutations = 0;
		improvements = 0;
	}
}
