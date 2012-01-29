package jade.reconstruct.area;
import java.util.*;
public interface RangeReconstructor {
	public void set_dipsersal(double x);
	public void set_extinction(double x);
	public void set_localextinction(ArrayList<Double> e);
	public void set_dispersalmatrix(ArrayList<ArrayList<Double>> d, int period);
	public void set_ratemodel();
	public double eval_likelihood();
	public int get_num_areas();
}
