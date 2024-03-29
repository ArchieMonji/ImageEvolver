/**
 * 2013 Archie Monji
 * 
 * ImageEvolver
 * Where the magic happens. Evolves a set of polygons to replicate an image.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImageEvolver extends Thread{
	private boolean isRunning;
	private BufferedImage orig, best, test;
	private ImageEvolverFrame ui;
	private List<ColoredPolygon> polygons = new ArrayList<ColoredPolygon>();
	private int polyCount, vertCount, width, height;
	private double lastEval = 0;
	private int mutations;
	private int improvements;
	private double pixelSumOrig;
	public int histogramRows, histogramCols, histogramColorPartitioningFactor;
	PartitionedColorHistogram origColorHistogram = new PartitionedColorHistogram(4,4,4);
	long startTime;
	long lastPauseTime;
	long timePassed;
	public ImageEvolver(ImageEvolverFrame ui) {
		start();
		this.ui = ui;
		polyCount = ui.polyCount;
		vertCount = ui.vertCount;
		width = 200;
		height = 200;
		histogramRows = 4;
		histogramCols = 4;
		histogramColorPartitioningFactor = 4;
	}

	public void setParameters(final int polys, final int verts, int w, int h){
		boolean updateBestAndTest = false;
		if(polys < polyCount && polygons.size() > polys){
			polygons = polygons.subList(0, polys);
			updateBestAndTest = true;
		}
		polyCount = polys;
		if(verts !=  vertCount){
			int[] oldX = null;
			int[] oldY = null;
			//Checks before for loop to save time
			if(verts < vertCount){
				for(ColoredPolygon p: polygons){
					oldX = p.xpoints;
					oldY = p.ypoints;
					p.reset();
					for(int i = 0; i < verts; i++){
						p.addPoint(oldX[i], oldY[i]);
					}
				}
			}
			else{ 
				//verts > vertCount
				for(ColoredPolygon p: polygons){
					for(int i = verts - vertCount; i > 0; i--){
						p.addPoint(p.xpoints[vertCount - 1], p.ypoints[vertCount - 1]);
					}
				}
			}
			updateBestAndTest = true;
			vertCount = verts;
		}
		if(updateBestAndTest){
			best = test = createImageFromPolygons(polygons);
			lastEval = evaluate();
		}
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
					int changeColor = random.nextInt(3);
					if(changeColor == 0){
						mutateColor(polygons.get(random.nextInt(polygons.size())), random);
					}
					else{
						mutateVertice(polygons.get(random.nextInt(polygons.size())), random);
					}
				}
				ui.updateTestCanvas();
				ui.updateMutationsLabel(++mutations);
				ui.updateTimePassed(System.currentTimeMillis() - startTime);
				//System.out.println("Best: " + lastEval + " | New: " + calculateSumOfPixelDifferences(orig,test));
			}
			try{
				sleep(2);
			}
			catch(Exception e){
				
			}
		}
	}
	
	private void mutateVertice(ColoredPolygon testPolygon, Random random){
		int verticeToChange = random.nextInt(vertCount); 
		int oldX = testPolygon.xpoints[verticeToChange];
		int oldY = testPolygon.ypoints[verticeToChange];
		testPolygon.xpoints[verticeToChange] = random.nextInt(width + 1);
		testPolygon.ypoints[verticeToChange] = random.nextInt(height + 1);
		test = createImageFromPolygons(polygons);
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
		if(!goodMutation()){
			testPolygon.setColor(oldColor);
		}
	}
	
	private boolean addPolygon(Random random){
		//if no polygons, try making a polygon 
		if(polygons.size() == 0){
			ColoredPolygon testPolygon = createRandomPolygon(true);
			polygons.add(testPolygon);
			test = createImageFromPolygons(polygons);
			if(!goodMutation()){
				polygons.remove(testPolygon);
			}
			return true;
		}
		else{
			//choose between making new polygon or mutating existing polygon
			boolean createNewPolygon = random.nextBoolean();
			if(createNewPolygon){
				ColoredPolygon testPolygon = createRandomPolygon(true);
				polygons.add(testPolygon);
				test = createImageFromPolygons(polygons);
				lastEval = evaluate();
				if(!goodMutation()){
					//polygons.remove(testPolygon);
				}
				return true;
			}
		}
		return false;
	}
	
	public void evolveShapes(){
		Random random = new Random();
		if(!polygons.isEmpty())
			mutateColor(polygons.get(random.nextInt(polygons.size())), random);
	}
	
	public void initRandomizedShapes(int x, int y){
		Random r = new Random();
		int sqW = Math.round(width/x);
		int sqH = Math.round(height/y);
		boolean xCount = true;
		for(int yi = 0; yi < y; yi++){
			for(int xi = 0; xi < x; xi++){
				ColoredPolygon p = new ColoredPolygon();
				System.out.println(xi);
				if( yi%2 ==0){
					if(xCount){
						p.addPoint(xi * sqW, yi * sqH);
						p.addPoint(xi * sqW + sqW, yi * sqH);
						p.addPoint(xi * sqW + sqW/2, yi * sqH + sqH);
						xCount = false;
					}
					else{			
						p.addPoint((--xi) * sqW, yi * sqH);
						p.addPoint((xi) * sqW + sqW/2, yi * sqH + sqH);
						p.addPoint((xi) * sqW - sqW/2, yi * sqH + sqH);
						xCount = true;
					}
					
				}
				else{
					if(xCount){
						p.addPoint((--xi) * sqW, yi * sqH);
						p.addPoint((xi) * sqW + sqW/2, yi * sqH + sqH);
						p.addPoint((xi) * sqW - sqW/2, yi * sqH + sqH);
						xCount = false;
					}
					else{
						p.addPoint(xi * sqW, yi * sqH);
						p.addPoint(xi * sqW + sqW, yi * sqH);
						p.addPoint(xi * sqW + sqW/2, yi * sqH + sqH);
						xCount = true;
					}
				}
				p.setColor(new Color(r.nextFloat(),r.nextFloat(),r.nextFloat(),r.nextFloat()));
				
				polygons.add(p);
			}
		}
	}
	
	private ColoredPolygon createRandomPolygon(boolean initRandomColor) {
		Random r = new Random();
		ColoredPolygon p = null;
		if(initRandomColor){
			p = new ColoredPolygon(new Color(r.nextFloat(),r.nextFloat(),r.nextFloat(),r.nextFloat()));
		}
		else{
			p = new ColoredPolygon(Color.black);
		}
		for(int v = 0; v < vertCount; v++){
			p.addPoint((int)(r.nextDouble() * width),(int)(r.nextDouble() * height));
		}
		return p;
	}

	public void startRunning() {
		if(startTime == 0){
			startTime = System.currentTimeMillis();
		}
		if(lastPauseTime != 0){
			startTime += (System.currentTimeMillis() - lastPauseTime);
		}
		isRunning = true;
	}

	public void stopRunning() {
		lastPauseTime = System.currentTimeMillis();
		isRunning = false;
	}

	public List<ColoredPolygon> initRandomizedPolygons() {
		polygons.clear();
		for(int i = 0; i < polyCount; i++){
			polygons.add(createRandomPolygon(true));
		}
		return polygons;
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
					sum += (pixels[pixel]  & 0xff);// alpha
				}
				sum += (pixels[pixel + 1]  & 0xff); //blue
				sum += (pixels[pixel + 2]  & 0xff);// green
				sum += (pixels[pixel + 3]  & 0xff);// red
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				sum += (pixels[pixel] & 0xff); // blue
				sum += (pixels[pixel + 1] & 0xff); // green
				sum += (pixels[pixel + 2] & 0xff); // red
			}
		}
		//System.out.println(pch);
		return sum;
	}

	public ColorHistogram buildColorHistogram(BufferedImage bim, boolean includeAlpha){
		if(bim == null) return null;
		final byte[] pixels = ((DataBufferByte) bim.getRaster().getDataBuffer()).getData();
		final boolean hasAlphaChannel = bim.getAlphaRaster() != null;
		ColorHistogram pch = new ColorHistogram(4);
		int r,g,b;
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				//Build AlphaHistogram, or ignore alpha
				/*if(includeAlpha){
					a += (pixels[pixel]  & 0xff); //alpha
				}*/
				b =  (pixels[pixel + 1]  & 0xff); //blue
				g = (pixels[pixel + 2]  & 0xff);// green
				r = (pixels[pixel + 3]  & 0xff);// red
				pch.incrementPixelCount(r, g, b);
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				b = (pixels[pixel] & 0xff); // blue
				g = (pixels[pixel + 1] & 0xff); // green
				r = (pixels[pixel + 2] & 0xff); // red
				pch.incrementPixelCount(r, g, b);
			}
		}
		//System.out.println(pch);
		return pch;
	}

	public ColorHistogram buildHistogram(BufferedImage bim, boolean includeAlpha){
		if(bim == null) return null;
		final byte[] pixels = ((DataBufferByte) bim.getRaster().getDataBuffer()).getData();
		final boolean hasAlphaChannel = bim.getAlphaRaster() != null;
		ColorHistogram histogram = new ColorHistogram(histogramColorPartitioningFactor);
		int r,g,b;
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				//Build AlphaHistogram, or ignore alpha
				/*if(includeAlpha){
					a += (pixels[pixel]  & 0xff); //alpha
				}*/
				b =  (pixels[pixel + 1]  & 0xff); //blue
				g = (pixels[pixel + 2]  & 0xff); //green
				r = (pixels[pixel + 3]  & 0xff); //red
				histogram.incrementPixelCount(r, g, b);
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				b = (pixels[pixel] & 0xff); // blue
				g = (pixels[pixel + 1] & 0xff); // green
				r = (pixels[pixel + 2] & 0xff); // red
				histogram.incrementPixelCount(r, g, b);
			}
		}
		return histogram;
	}

	public PartitionedColorHistogram buildPartitionedHistogram(BufferedImage bim, boolean includeAlpha){
		if(bim == null) return null;
		final byte[] pixels = ((DataBufferByte) bim.getRaster().getDataBuffer()).getData();
		final boolean hasAlphaChannel = bim.getAlphaRaster() != null;
		PartitionedColorHistogram histogram = new PartitionedColorHistogram(histogramRows,histogramCols,histogramColorPartitioningFactor);
		int row,col,r,g,b;
		int area = width * height;
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				//Build AlphaHistogram, or ignore alpha
				/*if(includeAlpha){
					a += (pixels[pixel]  & 0xff); //alpha
				}*/
				row = pixel / pixelLength * histogram.getCols() / area;
				col = ((pixel/pixelLength) % width) * histogram.getCols() / width;
				b =  (pixels[pixel + 1]  & 0xff); // blue
				g = (pixels[pixel + 2]  & 0xff); // green
				r = (pixels[pixel + 3]  & 0xff); // red
				histogram.incrementPixelCount(row, col, r, g, b);
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				row = pixel / pixelLength * histogram.getCols() / area;
				col = ((pixel/pixelLength) % width) * histogram.getCols() / width;
				b = (pixels[pixel] & 0xff); // blue
				g = (pixels[pixel + 1] & 0xff); // green
				r = (pixels[pixel + 2] & 0xff); // red
				histogram.incrementPixelCount(row, col, r, g, b);
			}
		}
		//System.out.println(hasAlphaChannel);
		return histogram;
	}

	private double calculateSumOfPixelDifferences(BufferedImage bim1, BufferedImage bim2, boolean includeAlpha) {
		if(bim1 == null || bim2 == null || bim1.getWidth() != bim2.getWidth() || bim1.getHeight() != bim2.getHeight()){
			return Double.MAX_VALUE;
		}
		double sum = 0;
		final byte[] p1 = ((DataBufferByte) bim1.getRaster().getDataBuffer()).getData();
		final byte[] p2 = ((DataBufferByte) bim2.getRaster().getDataBuffer()).getData();
		final boolean img1hasAlphaChannel = bim1.getAlphaRaster() != null;
		final boolean img2hasAlphaChannel = bim2.getAlphaRaster() != null;
		if(!img1hasAlphaChannel || !img2hasAlphaChannel){
			includeAlpha = false;
		}
		int img1PixelLength = img1hasAlphaChannel? 4 : 3;
		int img2PixelLength = img2hasAlphaChannel? 4 : 3;
		for (int pix1 = img1hasAlphaChannel? 1 : 0, pix2 = img2hasAlphaChannel? 1 : 0; pix1 < p1.length; pix1 += img1PixelLength, pix2 += img2PixelLength) {
			//Build AlphaHistogram, or ignore alpha
			if(includeAlpha){
				sum +=  Math.abs((p1[pix1 - 1]  & 0xff) - (p2[pix2 - 1]  & 0xff)); //alpha
			}
			sum += Math.abs((p1[pix1 + 0]  & 0xff) 	- (p2[pix2 + 0]  & 0xff)); // blue
			sum += Math.abs((p1[pix1 + 1]  & 0xff) 	- (p2[pix2 + 1]  & 0xff)); // green
			sum += Math.abs((p1[pix1 + 2]  & 0xff) 	- (p2[pix2 + 2]  & 0xff)); // red
		}
		return sum;
	}	

	public void paintImage(Graphics g, List<ColoredPolygon> polygons){
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
			lastEval = Double.MAX_VALUE;
			pixelSumOrig = this.calculatePixelSum(orig, false);
			//origColorHistogram = buildPartitionedHistogram(orig,false);
		}
	}

	public BufferedImage getTestImage(){
		return test;
	}

	public BufferedImage createImageFromPolygons(List<ColoredPolygon> polygons){
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
		test = createImageFromPolygons(initRandomizedPolygons());
		lastEval = evaluate();
		//System.out.println(lastEval);
		return test;
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
		return vertCount;
	}

	public int getMaxPolyCount() {
		return polyCount;
	}
	
	public void clearPolygons() {
		polygons.clear();
		best = test = null;
		lastEval = Double.MAX_VALUE;
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
			//System.out.println(lastEval);
			ui.updateFitnessAndImprovementsLabel(++improvements, 1-lastEval/pixelSumOrig);
			return true;
		}
		return false;
	}

	public double evaluate(){
		//return origColorHistogram.getDifference(buildPartitionedHistogram(test,false));
		return calculateSumOfPixelDifferences(orig, test, false);
	}

	public void clear() {
		clearPolygons();
		timePassed = 0;
		lastPauseTime = 0;
		startTime = 0;
		mutations = 0;
		improvements = 0;
	}
	
	public List<ColoredPolygon> getPolygons(){
		return new ArrayList<ColoredPolygon>(polygons);
	}
}
