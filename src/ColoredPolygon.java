/**
 * 2013 Archie Monji
 * ColoredPolygon
 * Polygon that keeps color information.
 */
import java.awt.Color;
import java.awt.Polygon;

public class ColoredPolygon extends Polygon {
	private Color color;
	
	public ColoredPolygon(){
		this(Color.white);
	}
	
	public ColoredPolygon(Color color){
		super();
		this.color = color;
	}
	
	public ColoredPolygon(int[] xpoints, int[] ypoints, int npoints, Color color){
		super(xpoints, ypoints, npoints);
		this.color = color;
	}
	
	public Color getColor(){
		return color;
	}
	
	public void setColor(Color color){
		this.color = color;
	}
	
	public ColoredPolygon getCopy(){
		return new ColoredPolygon(xpoints, ypoints, npoints, color);
	}
}
