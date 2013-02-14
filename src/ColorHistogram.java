/**
 * 2013 Archie Monji
 * ColorHistogram
 * Organizes pixel data by their rbg.
 * Total size is numColorDivisions^3
 * Very EXPENSIVE to calculate quickly and discard. 
 */

public class ColorHistogram {
	
	private int[][][] rgb; 
	private int numBins;
	private int count;
	public ColorHistogram(int numBins){
		this.numBins = numBins;
		rgb = new int[numBins][numBins][numBins];
	}
	
	public ColorHistogram(){
		this(4);
	}
	
	public void incrementPixelCount(int r, int g, int b){
		count++;
		rgb[r * numBins / 256][g * numBins / 256][b * numBins / 256]++;
	}
	
	public int getDifference(ColorHistogram other){
		int sum = 0;
		for(int r = 0; r < numBins; r++){
			for(int g = 0; g < numBins; g++){
				for(int b = 0; b < numBins; b++){
					sum += Math.abs(this.rgb[r][g][b] - other.rgb[r][g][b]);
				}
			}
		}
		return sum;
	}
	
	@Override
	public String toString(){
		String s = "PixelColorHistogram (# Bins = " + numBins + "){\n";
		for(int r = 0; r < numBins; r++){
			for(int g = 0; g <numBins; g++){
				for(int b = 0; b < numBins; b++){
					s += "\t[ r : " + String.format("%03d",r * 255 / numBins) + "-" + String.format("%03d",(r + 1) * 255 / numBins) + " "; 
					s += ", g : " + String.format("%03d",g * 255 / numBins) + "-" + String.format("%03d",(g + 1) * 255 / numBins) + " ";  
					s += ", b : " + String.format("%03d",b * 255 / numBins) + "-" + String.format("%03d",(b + 1) * 255 / numBins) + " ] = ";
					s += String.format("%6d",rgb[r][g][b]) + "\n";
				}
			}
		}
		return s + "}";
	}

	public void clear() {
		for(int r = 0; r < numBins; r++){
			for(int g = 0; g <numBins; g++){
				for(int b = 0; b < numBins; b++){
					rgb[r][g][b] = 0;
				}
			}
		}
		count = 0;
	}

	public int getPixelCount() {
		return count;
	}
}
