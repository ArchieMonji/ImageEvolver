import java.awt.Color;
import java.awt.geom.Ellipse2D;

public class ColoredDot extends Ellipse2D.Float {
	private static final long serialVersionUID = 1L;
	
	public ColoredDot(float x, float y, float width, float height){
		super(x, y, width, height);
	}
	private int count;
	private int redSum;
	private int blueSum;
	private int greenSum;
	private int alphaSum;
	
	public void addColor(Color color){
		redSum += color.getRed(); 
		greenSum += color.getGreen();
		blueSum += color.getBlue(); 
		alphaSum += color.getAlpha();
		count++;
	}
	
	public Color getColor(){
		//System.out.println(redSum);
		if(count == 0){
			count = 1;
		}
		return new Color((redSum)/count,
				(greenSum)/count,
				(blueSum)/count,
				(alphaSum)/count);
	}


}
