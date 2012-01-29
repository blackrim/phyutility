package jade.reconstruct.area;
import java.math.BigDecimal;

import jade.math.*;
import jade.tree.*;
import jade.data.*;

public class RangeReconstructionOptimizer implements AP_praxis_method{
	private RangeReconstructor rec;
	
	private double disp;
	
	private double LARGE = 100000;
	private double PMAX = 10.0;
	private double ext;
	
	int decimalPlace = 10;
	
	public RangeReconstructionOptimizer(RangeReconstructor rec){
		this.rec = rec;
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
		rec.set_dipsersal(x[1]);
		disp = x[1];
		rec.set_extinction(x[2]);
		ext = x[2];
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
	
	public double get_disp(){return this.disp;}
	public double get_ext(){return this.ext;}
}
