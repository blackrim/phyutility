package jade.reconstruct.area;

import java.util.*;

public class BayesChain {
	public BayesChain(double disp_sliding_window,double ext_sliding_window){
		this.disp_sliding_window = disp_sliding_window;
		this.ext_sliding_window = ext_sliding_window;
	}
	public double score;
	public double vag;
	public double ext;
	public double ext_sliding_window;
	public double disp_sliding_window;
	public double next_ext(double cur){
		return Math.abs(cur -(ext_sliding_window/2)+(ext_sliding_window*ran.nextDouble()));
	}
	public double next_disp(double cur){
		return Math.abs(cur -(disp_sliding_window/2)+(disp_sliding_window*ran.nextDouble()));
	}
	private Random ran = new Random();
}
