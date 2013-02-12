
public class PixelColorHistogram {
	private int[][][] rgb; 
	private int numBins;
	public PixelColorHistogram(int numBins){
		this.numBins = numBins;
		rgb = new int[numBins][numBins][numBins];
	}
	
	public void increment(int r, int g, int b){
		rgb[r * numBins / 256][g * numBins / 256][b * numBins / 256]++;
	}
	
	public int getDifference(PixelColorHistogram other){
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
		String s = "PixelColorHistogram (# Bins = " + numBins + " ): {\n";
		for(int r = 0; r < numBins; r++){
			for(int g = 0; g <numBins; g++){
				for(int b = 0; b < numBins; b++){
					s += "[ r : " + String.format("%03d",r * 255 / numBins) + "-" + String.format("%03d",(r + 1) * 255 / numBins) + " "; 
					s += ", g : " + String.format("%03d",g * 255 / numBins) + "-" + String.format("%03d",(g + 1) * 255 / numBins) + " ";  
					s += ", b : " + String.format("%03d",b * 255 / numBins) + "-" + String.format("%03d",(b + 1) * 255 / numBins) + " ] = ";
					s += rgb[r][g][b] + "\n";
				}
			}
		}
		return s + "}";
	}
	
}
