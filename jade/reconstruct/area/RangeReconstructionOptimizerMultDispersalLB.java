package jade.reconstruct.area;
import java.math.BigDecimal;
import java.util.*;

import jade.math.*;
import jade.tree.*;
import jade.data.*;

public class RangeReconstructionOptimizerMultDispersalLB implements AP_praxis_method{
	private ArrayList<RangeReconstructor> recs;
	
	private double LARGE = 100000;
	private double PMAX = 10.0;
	private double ext;
	private ArrayList<ArrayList<Double>> d0 = new ArrayList<ArrayList<Double>>();
	private ArrayList<ArrayList<Double>> d1 = new ArrayList<ArrayList<Double>>();
	//private ArrayList<ArrayList<Double>> d2 = new ArrayList<ArrayList<Double>>();
	
	int decimalPlace = 10;
	
	public RangeReconstructionOptimizerMultDispersalLB(ArrayList<RangeReconstructor> recs){
		this.recs = recs;
	}
	
	/*
	 * n = the number of variables
	 * x[0] = global dispersal
	 * x[1] = global dispersal
	 * x[2] = global extinction
	 * @see jade.math.AP_praxis_method#funct(int, double[])
	 */
	public double funct(int n, double x[]){
		double lh = 0.0;
		for(int i=1 ; i<x.length; i++){
			if(x[i] <= 0|| x[i] > PMAX)
				return LARGE;
		}
		try{
			BigDecimal bd1 = new BigDecimal(x[1]);
			bd1 = bd1.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
			x[1] = bd1.doubleValue();
		}catch(java.lang.NumberFormatException nfe){
			return LARGE;
		}
		try{
			BigDecimal bd1 = new BigDecimal(x[2]);
			bd1 = bd1.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
			x[2] = bd1.doubleValue();
		}catch(java.lang.NumberFormatException nfe){
			return LARGE;
		}
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
		//0 period 
		d0 = new ArrayList<ArrayList<Double>>();
		int curpara = 2;
		for(int i=0;i<recs.get(i).get_num_areas();i++){
			ArrayList<Double> td = new ArrayList<Double>();
			for(int j=0;j<recs.get(i).get_num_areas();j++){
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
		for(int i=0;i<recs.get(i).get_num_areas();i++){
			ArrayList<Double> td = new ArrayList<Double>();
			for(int j=0;j<recs.get(i).get_num_areas();j++){
				if(i == j){
					td.add(0.0);
				}else{
					td.add(x[curpara]);
					curpara++;
				}
			}
			d1.add(td);
		}
		//2 period 
		/*d2 = new ArrayList<ArrayList<Double>>();
		for(int i=0;i<recs.get(i).get_num_areas();i++){
			ArrayList<Double> td = new ArrayList<Double>();
			for(int j=0;j<recs.get(i).get_num_areas();j++){
				if(i == j){
					td.add(0.0);
				}else{
					td.add(x[curpara]);
					curpara++;
				}
			}
			d2.add(td);
		}*/
		try{
			lh = 1;
			for(int i=0;i<recs.size();i++){
				recs.get(i).set_dispersalmatrix(d0,0);
				recs.get(i).set_dispersalmatrix(d1,1);
				//recs.get(i).set_dispersalmatrix(d2,2);
				recs.get(i).set_extinction(x[1]);
				recs.get(i).set_ratemodel();
				lh += -Math.log(recs.get(i).eval_likelihood());
			}
		}catch(java.lang.ArithmeticException ae){
			return LARGE;
		}
		if(lh < 0 )
			return LARGE;
		return lh;
	}
}
