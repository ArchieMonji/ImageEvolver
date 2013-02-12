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
	private boolean isFinished;
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
	public ImageEvolver(ImageEvolverFrame ui) {
		start();
		this.ui = ui;
		polyCount = ui.polyCount;
		vertCount = ui.vertCount;
		width = 200;
		height = 200;
		histogramRows = 7;
		histogramCols = 7;
		histogramColorPartitioningFactor = 7;
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
		ui.updateTestCanvas();
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
		ui.updateTestCanvas();
		if(!goodMutation()){
			testPolygon.setColor(oldColor);
		}
	}
	private boolean addPolygon(Random random){
		//if no polygons, try making a polygon 
		if(polygons.size() == 0){
			ColoredPolygon testPolygon = createRandomPolygon(false);
			polygons.add(testPolygon);
			test = createImageFromPolygons(polygons);
			ui.updateTestCanvas();
			if(!goodMutation()){
				polygons.remove(testPolygon);
			}
			return true;
		}
		else{
			//choose between making new polygon or mutating existing polygon
			boolean createNewPolygon = random.nextBoolean();
			if(createNewPolygon){
				ColoredPolygon testPolygon = createRandomPolygon(false);
				polygons.add(testPolygon);
				test = createImageFromPolygons(polygons);
				ui.updateTestCanvas();
				if(!goodMutation()){
					polygons.remove(testPolygon);
				}
				return true;
			}
		}
		return false;
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
		isRunning = true;
	}

	public void stopRunning() {
		isRunning = false;
	}

	public ArrayList<ColoredPolygon> initRandomizedPolygons() {
		ArrayList<ColoredPolygon> polygons = new ArrayList<ColoredPolygon>();
		for(int i = 0; i < polyCount; i++){
			polygons.add(createRandomPolygon(false));
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
				sum += ((double) pixels[pixel + 1])/255; // blue
				sum += (((double) pixels[pixel + 2]))/255; // green
				sum += (((double) pixels[pixel + 3]))/255; // red
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

	public ColorHistogram calculatePixelSum(BufferedImage bim, boolean includeAlpha){
		if(bim == null) return null;
		final byte[] pixels = ((DataBufferByte) bim.getRaster().getDataBuffer()).getData();
		final boolean hasAlphaChannel = bim.getAlphaRaster() != null;
		double sum = 0;
		ColorHistogram pch = new ColorHistogram(4);
		int r,g,b;
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				if(includeAlpha){
					sum += (pixels[pixel]  & 0xff);// alpha
				}
				sum += b =  (pixels[pixel + 1]  & 0xff); //blue
				sum += g = (pixels[pixel + 2]  & 0xff);// green
				sum += r = (pixels[pixel + 3]  & 0xff);// red
				pch.incrementPixelCount(r, g, b);
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0; pixel < pixels.length; pixel += pixelLength) {
				sum += b = (pixels[pixel] & 0xff); // blue
				sum += g = (pixels[pixel + 1] & 0xff); // green
				sum += r = (pixels[pixel + 2] & 0xff); // red
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
		int a,r,g,b;
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
		//System.out.println(histogram);
		return histogram;
	}

	private double calculateSumOfPixelDifferences(BufferedImage bim1, BufferedImage bim2) {
		if(bim1 == null || bim2 == null || bim1.getWidth() != bim2.getWidth() || bim1.getHeight() != bim2.getHeight()){
			return Double.MAX_VALUE;
		}
		//double bim1Sum = calculatePixelSum(bim1, false);
		//System.out.print("I1: " + bim1Sum);
		//double bim2Sum = calculatePixelSum(bim2, false);
		origColorHistogram.getDifference(buildPartitionedHistogram(bim2,false));
		//System.out.println(" | I2: " + bim2Sum);
		return 0;//pixelSumOrig - bim2Sum;
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
			//pixelSumOrig = this.calculatePixelSum(orig, false);
			origColorHistogram = buildPartitionedHistogram(orig,false);
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
			ui.updateFitnessAndImprovementsLabel(++improvements, 1-Math.abs(lastEval/width/height/255/3*100));
			return true;
		}
		return false;
	}

	public double evaluate(){
		return origColorHistogram.getDifference(buildPartitionedHistogram(test,false));
	}

	public void clear() {
		clearPolygons();
		mutations = 0;
		improvements = 0;
	}
}
