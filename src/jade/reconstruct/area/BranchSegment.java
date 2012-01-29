package jade.reconstruct.area;

import java.util.*;

public class BranchSegment {
	private double start; //old
	private double end; //young
	private int period;
	private int number;
	private int [] startdist;
	private ArrayList<int[]> startdists;
	private double [] conditionals;
	
	public BranchSegment(double start, double end, int period, int number, int [] startdist){
		this.start = start;
		this.end = end;
		this.period = period;
		this.number = number;
		this.startdist = startdist;
		startdists = new ArrayList<int[]>();
		startdists.add(startdist);
	}
	
	public BranchSegment(double start, double end, int number, int period){
		this.start = start;
		this.end = end;
		this.period = period;
		this.number = number;
		this.startdist = null;
		startdists = new ArrayList<int[]>();
	}
	
	public double get_duration(){return start-end;}
	public double get_start(){return start;}
	public double get_end(){return end;}
	public int get_period(){return period;}
	public int get_number(){return number;}
	public int [] get_startdist(){return startdist;}
	public ArrayList<int[]> get_startdists(){return startdists;}
	public double [] get_conditionals(){return conditionals;}
	public void set_conditionals(double [] cond){this.conditionals  = cond;}
	public void set_start(double start){this.start = start;}
	public void set_end(double end){this.end = end;}
	public void set_number(int number){this.number = number;}
	public void set_period(int period){this.period = period;}
	public void set_startdist(int [] startdist){
		this.startdist = startdist;
		startdists = new ArrayList<int[]>();
		startdists.add(startdist);
	}
	public void set_startdists(ArrayList<int []>sd){
		this.startdists = sd;
	}
}
