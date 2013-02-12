
public class PartitionedColorHistogram {
	private int numCols;
	private int numRows;
	ColorHistogram[][] pch;
	
	public PartitionedColorHistogram(){
		this(4,4,4);
	}
	
	public PartitionedColorHistogram(int numRows, int numCols, int numBins){
		this.numRows = numRows;
		this.numCols = numCols;
		pch = new ColorHistogram[numRows][numCols];
		for(int r = 0; r < numRows; r++){
			for(int c = 0; c < numCols; c++){
				pch[r][c] = new ColorHistogram(numBins);
			}
		}
	}
	
	public void incrementPixelCount(int x, int y, int r, int g, int b){
		pch[x][y].incrementPixelCount(r, g, b);
	}
	
	public void clear(){
		for(int r = 0; r < numRows; r++){
			for(int c = 0; c < numCols; c++){
				pch[r][c].clear();
			}
		}
	}
	
	public int getDifference(PartitionedColorHistogram other){
		int sum = 0;
		for(int r = 0; r < numRows; r++){
			for(int c = 0; c < numCols; c++){
				sum += pch[r][c].getDifference(other.getParition(r, c));
			}
		}
		return sum;
	}
	
	public ColorHistogram getParition(int row, int col){
		return pch[row][col];
	}

	public String toString(){
		String s = "PartitionedColorHistogram (# Partitions = " + numCols * numRows + "){\n";
		for(int r = 0; r < numRows; r++){
			for(int c = 0; c < numCols; c++){
				s += "\t[ Row: " + r + ", Col: " + c + " ] : " + pch[r][c].getPixelCount() + "\n"; 
			}
		}
		s += "}";
		return s;
	}
	
	public int getRows() {
		return numRows;
	}
	
	public int getCols() {
		return numCols;
	}
	
}
