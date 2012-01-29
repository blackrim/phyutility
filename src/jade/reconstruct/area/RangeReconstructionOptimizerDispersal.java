package jade.reconstruct.area;
import java.math.BigDecimal;
import java.util.*;

import jade.math.*;
import jade.tree.*;
import jade.data.*;

public class RangeReconstructionOptimizerDispersal implements AP_praxis_method{
	private RangeReconstructor rec;
	
	//private double disp;
	
	private double LARGE = 100000;
	private double PMAX = 10.0;
	private double ext;
	private ArrayList<ArrayList<Double>> d0 = new ArrayList<ArrayList<Double>>();
	private ArrayList<ArrayList<Double>> d1 = new ArrayList<ArrayList<Double>>();
	
	int decimalPlace = 10;
	
	public RangeReconstructionOptimizerDispersal(RangeReconstructor rec){
		this.rec = rec;
	}
	
	/*
	 * n = the number of variables
	 * x[0] = global extinction
	 * x[1] = dispersal
	 * x[2] = etc
	 * ...
	 * @see jade.math.AP_praxis_method#funct(int, double[])
	 */
	public double funct(int n, double x[]){
		double lh = 0.0;
		for(int i=1 ; i<x.length; i++){
			if(x[i] <= 0|| x[i] > PMAX)
				return LARGE;
		}
		for(int i=1; i < x.length ; i++){
			try{
				BigDecimal bd1 = new BigDecimal(x[i]);
				bd1 = bd1.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
				x[i] = bd1.doubleValue();
			}catch(java.lang.NumberFormatException nfe){
				return LARGE;
			}
		}
		rec.set_extinction(x[1]);
		//rec.set_dipsersal(x[1]);
		//disp = x[1];
		ext = x[1];
		/*
		 * adding to rate matrix should look like
		 *		1	|	2	|	3
		 * =========|=======|=========
		 * 1|		|	1	|	2
		 * -|-------|-------|--------
		 * 2|	3	|		|	4
		 * -|-------|-------|--------
		 * 3|	5	|	6	|
		 * 
		 */
		d0 = new ArrayList<ArrayList<Double>>();
		int curpara = 2;
		for(int i=0;i<rec.get_num_areas();i++){
			ArrayList<Double> td = new ArrayList<Double>();
			for(int j=0;j<rec.get_num_areas();j++){
				if(i == j){
					td.add(0.0);
				}else{
					td.add(x[curpara]);
					curpara++;
				}
			}
			d0.add(td);
		}
		//1 period 
		d1 = new ArrayList<ArrayList<Double>>();
		for(int i=0;i<rec.get_num_areas();i++){
			ArrayList<Double> td = new ArrayList<Double>();
			for(int j=0;j<rec.get_num_areas();j++){
				if(i == j){
					td.add(0.0);
				}else{
					td.add(x[curpara]);
					curpara++;
				}
			}
			d1.add(td);
		}
		//for(int i=2;i<=n;i++){e.add(x[i]);}
		rec.set_dispersalmatrix(d0,0);
		rec.set_dispersalmatrix(d1,1);
		//ext = x[2];
		rec.set_ratemodel();
		try{
			lh = -Math.log(rec.eval_likelihood());
		}catch(java.lang.ArithmeticException ae){
			return LARGE;
		}
		if(lh < 0 )
			return LARGE;
		return lh;
	}

}
