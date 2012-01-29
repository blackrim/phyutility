package jade.reconstruct.area;
import java.math.BigDecimal;
import java.util.*;
import jade.math.*;
import jade.tree.*;
import jade.data.*;

public class RangeReconstructionOptimizerMult implements AP_praxis_method{
	private ArrayList<RangeReconstructor> recs;
	
	private double disp;
	
	private double LARGE = 100000;
	private double PMAX = 10.0;
	private double ext;
	
	int decimalPlace = 10;
	
	public RangeReconstructionOptimizerMult(ArrayList<RangeReconstructor> recs){
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
		disp = x[1];
		ext = x[2];
		try{
			lh = 1;
			for(int i=0;i<recs.size();i++){
				recs.get(i).set_dipsersal(x[1]);
				recs.get(i).set_extinction(x[2]);
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
	
	public double get_disp(){return this.disp;}
	public double get_ext(){return this.ext;}
}
