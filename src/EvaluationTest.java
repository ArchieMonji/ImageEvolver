import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class EvaluationTest{

	public static class Triple{
		double x, y, z;
		public Triple(int a, int b, int c){
			this.x = a;
			this.y = b;
			this.z = c;
		}
	}
	static ArrayList<Triple> a = new ArrayList<Triple>();
	static ArrayList<Triple> b = new ArrayList<Triple>();
	static double percent;
	static double diff;
	static final double MAX = 255;
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try {
			//run("eval1Parameter");
			//run("eval1Parameter2");
			run("evalPercent");
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	static void eval1Parameter(){
		for(int i = 0; i < a.size(); i++){
			Triple O = a.get(i);
			Triple T = b.get(i);
			double v = O.x - T.x;
			diff += v;
		}
		percent = diff/a.size()/255 * 100;
		print();
	}
	
	static void eval1Parameter2(){
		for(int i = 0; i < a.size(); i++){
			Triple O = a.get(i);
			Triple T = b.get(i);
			double v = O.x - T.x;
			diff += v/255;
		}
		percent = diff/a.size() * 100;
		print();
	}
	

	static void evalPercent(){
		
		for(int i = 0; i < a.size(); i++){
			Triple O = a.get(i);
			Triple T = b.get(i);
			double v = (O.x) - (T.x);
			diff += v;
			//System.out.println("Subsum: " + v + " Fractional Diff: " + v/O.x*100);
		}
		percent = diff /255/2;
		print();
	}
	
	public static String getMethodName()
	{
	  final StackTraceElement[] ste = Thread.currentThread().getStackTrace();

	  //System. out.println(ste[ste.length-depth].getClassName()+"#"+ste[ste.length-depth].getMethodName());
	  // return ste[ste.length - depth].getMethodName();  //Wrong, fails for depth = 0
	  return ste[ste.length - 3].getMethodName(); //Thank you Tom Tresansky
	}
	
	static void initMaxMin(){
		reset();
		a.add(new Triple(255,0,0));
		a.add(new Triple(255,0,0));
		b.add(new Triple(0,0,0));
		b.add(new Triple(0,0,0));
	}
	
	static void initMidValues(){
		reset();
		a.add(new Triple(100,0,0));
		a.add(new Triple(100,0,0));
		b.add(new Triple(50,0,0));
		b.add(new Triple(50,0,0));
	}
	
	static void initMidValues2(){
		reset();
		a.add(new Triple(50,0,0));
		a.add(new Triple(50,0,0));
		b.add(new Triple(100,0,0));
		b.add(new Triple(100,0,0));
	}
	
	static void initMinMax(){
		reset();
		a.add(new Triple(0,0,0));
		a.add(new Triple(0,0,0));
		b.add(new Triple(255,0,0));
		b.add(new Triple(255,0,0));
	}
	
	static void initSymmetricMax(){
		reset();
		a.add(new Triple(0,0,0));
		a.add(new Triple(255,0,0));
		b.add(new Triple(255,0,0));
		b.add(new Triple(0,0,0));
	}
	
	static void initSymmetricMid(){
		reset();
		a.add(new Triple(70,0,0));
		a.add(new Triple(170,0,0));
		b.add(new Triple(170,0,0));
		b.add(new Triple(70,0,0));
	}
	
	static void initEqualMax(){
		reset();
		a.add(new Triple(0,0,0));
		a.add(new Triple(255,0,0));
		b.add(new Triple(0,0,0));
		b.add(new Triple(255,0,0));
	}
	
	static void initEqualMid(){
		reset();
		a.add(new Triple(0,0,0));
		a.add(new Triple(50,0,0));
		b.add(new Triple(0,0,0));
		b.add(new Triple(50,0,0));
	}
	
	static void initZero(){
		reset();
		a.add(new Triple(0,0,0));
		a.add(new Triple(0,0,0));
		b.add(new Triple(0,0,0));
		b.add(new Triple(0,0,0));
	}
	
	static void reset(){
		System.out.println(getMethodName());
		int count = 0;
		diff = 0;
		percent = 0;
		a.clear();
		b.clear();
	}
	
	static void print(){
		int count = 0;
		for(Triple t: a){
			System.out.print(count++ + ": [" + t.x + "] ; ");
		}
		System.out.println();
		count = 0;
		for(Triple t: b){
			System.out.print(count++ + ": [" + t.x + "] ; ");
		}
		System.out.println();
		System.out.println("+- = " + diff);
		System.out.println("%  = " + percent);
		System.out.println();
	}

	static void run(String method) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException{

		System.out.println("Eval: " + method);
		Method m = EvaluationTest.class.getDeclaredMethod(method);
		initMaxMin();
		m.invoke(null, null);
		initMinMax();
		m.invoke(null, null);
		initMidValues();
		m.invoke(null, null);
		initMidValues2();
		m.invoke(null, null);
		initSymmetricMax();
		m.invoke(null, null);
		initSymmetricMid();
		m.invoke(null, null);
		initEqualMax();
		m.invoke(null, null);
		initEqualMid();
		m.invoke(null, null);
		initZero();
		m.invoke(null, null);
		System.out.println("//////////////////////////////////////////////");
	}
	
}
