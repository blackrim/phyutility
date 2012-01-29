package jade.reconstruct.area;

import java.util.*;

public class RateModel {
	private ArrayList<Double> periods;//periods should be from old to young
	private ArrayList<Area> areas;
	private int [][] dists;
	private ArrayList<double [][]> D;
	private ArrayList<double [][]> Dm;//dispersal mask
	private ArrayList<double []> E;
	private ArrayList<double [][]> Q;
	private double dispersal = 0.01;
	private double extinction = 0.01;
	private ArrayList<Double> e;
	private ArrayList<ArrayList<ArrayList<Double>>> d = new ArrayList<ArrayList<ArrayList<Double>>> (); //period, row, column
	private boolean localextinction = false;
	private HashMap<String,Integer > dist_dict;
	private boolean GE = false;

	public RateModel(ArrayList<Area> areas, ArrayList<Double>periods, boolean GE){
		this.areas = areas;
		this.periods = periods;
		dists = jade.math.NChooseM.iterate_all_bv2small(areas.size());
		dist_dict = new HashMap< String, Integer >();
		if(GE == true){
			this.GE = true;
			int [] ex = new int [this.areas.size()];
			for(int i = 0 ;i < ex.length; i++){ex[i] = 0;}
			int [][] tdists = new int [dists.length+1][this.areas.size()];
			tdists[0] = ex;
			for(int i = 1; i < dists.length+1; i++){tdists[i] = dists[i-1];}
			dists = tdists;
		}
		for(int i=0;i<dists.length;i++){
			String x = "";
			for(int j=0;j<dists[i].length;j++){x += String.valueOf(dists[i][j]);}
			dist_dict.put(x, i);
			//System.out.println(x);
		}
		setup_Dm();
	}
	
	public void set_dispersal(double d){this.dispersal = d;}
	public void set_extinction(double e){this.extinction = e;}
	/*
	 * area specific extinction
	 */
	public void set_localextinction(ArrayList<Double> e){this.e = e;localextinction = true;}
	
	/*
	 * more complex dispersal matrix
	 */
	private boolean compdispersalmatrix = false;
	public void set_dispersalmatrix(ArrayList<ArrayList<Double>> d, int period){
		if (this.d.size()<=period || this.d.size() == 0){
			this.d.add(d);
		}else{
			this.d.set(period, d);
		}
		compdispersalmatrix = true;
	}

	
	public void setup(boolean verbose){
		setup_D();
		if(localextinction == false)
			setup_E();
		else{
			setup_E(e);
		}
		setup_Q();
		if(verbose == true){
			for(int i=0;i<this.Dm.size();i++){
				System.out.println(jade.math.Utils.printDMat(this.Dm.get(i)));
			}
			for(int i=0;i<this.D.size();i++){
				System.out.println(jade.math.Utils.printDMat(this.D.get(i)));
			}
			for(int i=0;i<this.Q.size();i++){
				System.out.println(jade.math.Utils.printDMat(this.Q.get(i)));
			}
		}
	}

	public ArrayList<Double> getPeriods(){return periods;}

	public int getRelevantPeriod(double time){
		int ret = 0;
		/*
		 * period time is the start time (old time)
		 * for the period, the end time is the start time of the 
		 * next younger period 0
		 */
		int i = 0;
		while(periods.get(i)> time){
			ret = i;
			i++;
			if(i<periods.size()){
				continue;
			}else{
				break;
			}
		}
		return ret;
	}

	/*
	 * return the distribution index for a range
	 */
	public int get_distribution_index(int [] dist){
		String x = "";
		for(int j=0;j<dist.length;j++){x += String.valueOf(dist[j]);}
		return dist_dict.get(x);
	}
	public int get_distribution_index(String dist){
		return dist_dict.get(dist);
	}

	public double [][] P (int period, double time){
		Q tq = new Q(Q.get(period));
		PJAMA tp = new PJAMA(tq);
		tp.setBL(time);
		//System.out.println(time);
		double [][] retp = new double[dists.length][dists.length];
		for(int i=0;i<retp.length;i++){
			for(int j=0;j<retp.length;j++){
				retp[i][j] = tp.getRateChangeProbability(i, j);
			}
		}
		//System.out.println(jade.math.Utils.printDMat(retp));
		return retp;
	}

	public ArrayList<Area> get_areas(){return areas;}

	public int [][] get_dists(){return dists;}

	/*
	 * for dispersal difference
	 */
	public void reset_Dm(){
		setup_Dm();
	}

	public void set_local_dispersal(int period, int start, int end, double value,
			boolean sym){
		Dm.get(period)[start][end] = value;
		if(sym == true){
			Dm.get(period)[end][start] = value;
		}
	}
	/*
	 * private methods
	 */
	/*
	 * mask
	 */
	private void setup_Dm(){
		Dm = new ArrayList<double [][]>();
		for(int i=0;i<periods.size();i++){
			Dm.add(new double [areas.size()][areas.size()]);
			for(int j=0;j<areas.size();j++){
				for(int h=0;h<areas.size();h++){
					Dm.get(i)[j][h] = 1.0;
				}
			}
		}
	}
	private void setup_D(){
		D = new ArrayList<double [][]>();
		for(int i=0;i<periods.size();i++){
			D.add(new double [areas.size()][areas.size()]);
			for(int j=0;j<areas.size();j++){
				for(int h=0;h<areas.size();h++){
					if(this.compdispersalmatrix == false){
						D.get(i)[j][h] = 1.0 * dispersal * Dm.get(i)[j][h];
					}else{
						D.get(i)[j][h] = 1.0 * d.get(i).get(j).get(h) * Dm.get(i)[j][h];
					}
				}
			}
		}
		/*
		 * add some stuff
		 */
		for(int p=0;p<periods.size();p++){
			for(int a = 0;a<areas.size();a++){
				this.D.get(p)[a][a] = 0.0;
			}
		}
	}
	/*
	 * E
	 */
	private void setup_E(){
		E = new ArrayList<double []>();
		for(int i=0;i<periods.size();i++){
			E.add(new double [areas.size()]);
			for(int j=0;j<areas.size();j++){
				E.get(i)[j] = 1.0*extinction ;
			}
		}
	}
	/*
	 * local extinction E
	 */
	private void setup_E(ArrayList<Double> e){
		E = new ArrayList<double []>();
		for(int i=0;i<periods.size();i++){
			E.add(new double [areas.size()]);
			for(int j=0;j<areas.size();j++){
				E.get(i)[j] = 1.0*e.get(j) ;
			}
		}
	}
	/*
	 * Q
	 */
	private void setup_Q(){
		Q = new ArrayList<double [][]>();
		for(int i=0;i<periods.size();i++){
			Q.add(new double [dists.length][dists.length]);
			for(int j=0;j<areas.size();j++){
				for(int h=0;h<areas.size();h++){
					Q.get(i)[j][h] = 0.0;
				}
			}
		}

		for(int i=0;i<periods.size();i++){
			for(int j=0;j<dists.length;j++){
				int s1 = jade.math.Utils.sum(dists[j]);
				if(s1 > 0){
					for(int k=0;k<dists.length;k++){
						int s2 = jade.math.Utils.sum(dists[k]);
						int [] xor = jade.math.Utils.logical_xor_int(dists[j], dists[k]);
						if(jade.math.Utils.sum(xor) == 1){
							ArrayList<Integer> dest = jade.math.Utils.nonzero_int(xor);
							double rate = 0.0;
							if(s1 < s2){//dispersal
								rate = 0.0;
								ArrayList<Integer> src = jade.math.Utils.nonzero_int(dists[j]);
								for(int x =0;x<src.size();x++){rate += this.D.get(i)[src.get(x)][dest.get(0)];}
							}//if
							else{//extinction
								rate = this.E.get(i)[dest.get(0)];
							}//else
							this.Q.get(i)[j][k] = rate;
						}//if
					}//for
				}//if
			}//for
			/*
			 * Q diagonal
			 */
			for(int j=0;j<dists.length;j++){
				this.Q.get(i)[j][j] = jade.math.Utils.sum(this.Q.get(i)[j]) - this.Q.get(i)[j][j] * -1.0;
			}
		}//for
	}//private

	/*
	 * 
	 */


	public static void main(String [] args){

	}
}
