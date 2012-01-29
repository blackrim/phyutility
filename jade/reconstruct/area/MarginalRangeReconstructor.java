package jade.reconstruct.area;

import jade.math.AP_Praxis;
import jade.tree.*;
import jade.data.*;
import java.util.*;

public class MarginalRangeReconstructor implements RangeReconstructor{
	private RateModel ratemodel;

	private String LTGN = "likelihood_of_tree_given_n";
	private String DISTMAP = "internal_combined_likelihoods";
	private String MAPLTGN = "map_of_likelihood_of_tree_given_n";
	
	private String ANCMAP = "ancmap";
	
	private Tree tree;

	private Alignment aln;

	private String COND = "conditionals";

	private String BS = "branch_segment";

	private String ANC = "ancsplits";

	public MarginalRangeReconstructor(RateModel ratemodel, Tree tree,
			Alignment aln) {
		this.ratemodel = ratemodel;
		this.aln = aln;
		this.tree = tree;
		this.setDistanceFromTipForBG();
		setup_branch_segments(tree.getRoot());
		setup_tip_conditionals();
	}

	public int get_num_areas(){
		return ratemodel.get_areas().size();
	}
	
	public double eval_likelihood() {
		ancdist_conditional_like(tree.getRoot());
		return jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND));
	}

	public void set_internals_likelihoods(){
		preorder_set_internal_conditionals(tree.getRoot());
	}

	public void set_dipsersal(double d) {
		ratemodel.set_dispersal(d);
	}

	public void set_extinction(double e) {
		ratemodel.set_extinction(e);
	}

	/*
	 * need code to make sure that local extinction array is the correct size
	 */
	public void set_localextinction(ArrayList<Double> e){
		ratemodel.set_localextinction(e);
	}
	
	public void set_dispersalmatrix(ArrayList<ArrayList<Double>> d, int period){
		ratemodel.set_dispersalmatrix(d, period);
	}
	
	public void set_ratemodel() {
		ratemodel.setup(false);
	}

	public Tree get_tree() {
		return tree;
	}

	/*
	 * private
	 */
	private void ancdist_conditional_like(Node node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			ancdist_conditional_like(node.getChild(i));
		}
		double[] distconds = new double[ratemodel.get_dists().length];
		if (node.isInternal()) {
			double[] c1 = conditionals(node.getChild(0));
			double[] c2 = conditionals(node.getChild(1));
			ArrayList<AncSplit> ancsplits = new ArrayList<AncSplit>();
			for (int i = 0; i < ratemodel.get_dists().length; i++) {
				double lh = 0.0;
				int[][][] tret = iter_dist_splits_weighted(ratemodel
						.get_dists()[i]);
				for (int j = 0; j < tret.length; j++) {
					int[] d1 = tret[j][0];// left
					int[] d2 = tret[j][1];// right
					double lh_part = (c1[ratemodel.get_distribution_index(d1)] * c2[ratemodel
					                                                                .get_distribution_index(d2)]);
					lh += lh_part * weight;
					AncSplit as = new AncSplit(ratemodel.get_dists()[i],
							tret[j], weight, lh_part);
					ancsplits.add(as);
				}
				distconds[i] = lh;
			}
			//node.assocObject(ANC, ancsplits);
		} else {
			distconds = ((ArrayList<BranchSegment>) node.getObject(BS)).get(0)
			.get_conditionals();
		}
		if (node.hasParent()) {
			((ArrayList<BranchSegment>) node.getObject(BS)).get(0)
			.set_conditionals(distconds);
			node.assocObject(COND, distconds);
		} else {
			node.assocObject(COND, distconds);// root
		}
	}

	private double[] conditionals(Node node) {
		double[] ret = new double[ratemodel.get_dists().length];
		ArrayList<BranchSegment> segs = (ArrayList<BranchSegment>) node.getObject(BS);
		ret = segs.get(0).get_conditionals();//conditionals at beginning of branch
		// System.out.println("b\t"+jade.math.Utils.printDVec(ret));
		int num_of_periods = ratemodel.getPeriods().size();
		for (int seg = 0; seg < segs.size(); seg++) {
			segs.get(seg).set_conditionals(ret);
			double[][] p = ratemodel.P((num_of_periods - 1) - segs.get(seg).get_period(), 
					segs.get(seg).get_duration());
			double[] v = new double[ratemodel.get_dists().length];
			ArrayList<Integer> distrange = new ArrayList<Integer>();
			if (segs.get(seg).get_startdists().size() > 0){
				for(int i=0;i<segs.get(seg).get_startdists().size();i++){
					distrange.add(ratemodel.get_distribution_index(segs.get(seg).get_startdists().get(i)));
				}
			}
			else {
				for (int i = 0; i < ratemodel.get_dists().length; i++) {
					distrange.add(i);
				}
			}
			for (int i = 0; i < distrange.size(); i++) {
				// P[i] is the vector of probabilities of going from dist i
				// to all other dists
				double[] tv = new double[v.length];
				for (int j = 0; j < tv.length; j++) {
					tv[j] = ret[j] * p[distrange.get(i)][j];
				}
				v[distrange.get(i)] = jade.math.Utils.sum(tv);
			}// for
			//System.out.println("d\t"+segs.get(seg).get_period());
			ret = v;
		}// for
		//System.out.println("a\t"+jade.math.Utils.printDVec(ret));
		return ret;
	}

	/*
	 * doesn't seem to work yet
	 */

	private void preorder_set_internal_conditionals(Node node) {
		if ((tree.getRoot() != node) && !node.isExternal()) {
			// conditional likelihood excluding X
			double[] clex = calcExclude(node.getParent(), node);
			// L(T|X)
			double[] x = new double[ratemodel.get_dists().length];
			for (int i = 0; i < ratemodel.get_dists().length; i++) {
				x[i] = ((double [])node.getObject(COND))[i];
				double t = 0;
				for (int j = 0; j < ratemodel.get_dists().length; j++) {
					t = t +	calc_cond_p_for_excl(clex[j], node, i,j);
				}
				x[i] = x[i] * t;
			}
			node.assocObject(LTGN, x);
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			preorder_set_internal_conditionals(node.getChild(i));
		}
	}

	/*
	 * goes with preorder...
	 * doesn't work yet
	 */

	private double[] calcExclude(Node node, Node excl) {
		double[] clex = new double[ratemodel.get_dists().length];
		for (int i = 0; i < ratemodel.get_dists().length; i++) {
			clex[i] = 1.0;
		}
		// conditional likelihood excluding X
		for (int i = 0; i < node.getChildCount(); i++) {
			if (node.getChild(i) != excl) {
				for (int j = 0; j < ratemodel.get_dists().length; j++) {
					double t = 0;
					for (int k = 0; k < ratemodel.get_dists().length; k++) {
						t = t + calc_cond_p_for_excl(((double[])node.getChild(i).getObject(COND))[k],node.getChild(i),j,k);
					}
					clex[j] = clex[j] * t;
				}
			}
		}
		Node mother = node.getParent();
		if (mother != null) {
			for (int i = 0; i < mother.getChildCount(); i++) {
				if (mother.getChild(i) == node) {
					double[] clExclude = calcExclude(mother, node);
					for (int j = 0; j < ratemodel.get_dists().length; j++) {
						double t = 0;
						for (int k = 0; k < ratemodel.get_dists().length; k++) {
							t = t + calc_cond_p_for_excl(clExclude[k], mother.getChild(i),j,k);
						}
						clex[j] = clex[j] * t;
					}
				}
			}
		}
		double[] d = clex;
		return d;
	}

	/*
	 * goes with calcExclude
	 * doesn't work yet
	 */
	private double calc_cond_p_for_excl(double value, Node node, int j, int k) {
		double ret = value;
		ArrayList<BranchSegment> segs = (ArrayList<BranchSegment>) node.getObject(BS);
		int num_of_periods = ratemodel.getPeriods().size();
		for (int seg = 0; seg < segs.size(); seg++) {
			double[][] p = ratemodel.P((num_of_periods - 1) - segs.get(seg).get_period(), 
					segs.get(seg).get_duration());
			ret = ret * p[j][k];
		}// for
		return ret;
	}

	/*
	 * slower but gets the job done
	 * does the internal nodes as the ancestral range and considers all possible scenarios for that range
	 * instaed of doing each subdivision seperateely as in all_ranges
	 */
	public void calc_internal_likelihoods(boolean opt){
		calc_internal_likelihoods(tree.getRoot(), opt);
	}

	private void calc_internal_likelihoods(Node node, boolean opt){
		if(node.isInternal()){
			for(int i=0;i<node.getChildCount();i++){
				calc_internal_likelihoods(node.getChild(i), opt);
			}
			HashMap<int[],Double> anc = new HashMap<int[],Double> ();
			node.assocObject(DISTMAP,anc);
			//System.out.println(node.getChild(0).getName()+"\t"+node.getChild(1).getName());
			this.clear_start_dists();
			for(int i=0;i<ratemodel.get_dists().length;i++){
				//for(int j=0;j<ratemodel.get_dists()[i].length;j++){System.out.print(ratemodel.get_dists()[i][j]);}

				int [][][] iters = this.iter_dist_splits_weighted(ratemodel.get_dists()[i]);
				ArrayList<int []> bd1 = new ArrayList<int []> ();
				ArrayList<int []> bd2 = new ArrayList<int []> ();
				for(int j=0;j<iters.length;j++){
					bd1.add(iters[j][0]);
					bd2.add(iters[j][1]);
				}
				((ArrayList<BranchSegment>)node.getChild(0).getObject(BS)).get(0).set_startdists(bd1);
				((ArrayList<BranchSegment>)node.getChild(1).getObject(BS)).get(0).set_startdists(bd2);
				/*
				 * opt code
				 */
				if(opt == true){
					double [] init = {666,0.1,0.1};
					double [] in = new double [10];
					in [0] = jade.math.PrecisionCalculator.getMachinePrecision();
					//in [0] = 0.00000000001;
					in [1] = 1.1102230246251565E-8;
					//in [1] = 3.1622776601683794E-6;
					//System.out.println("s "+Math.sqrt(in[0]));
					in [2] = 0.000001;
					in [5] = 1000;
					in [6] = 0.01;
					in [7] = 1;
					in [8] = 1;
					in [9] = -1;
					double [] out = new double [7];
					RangeReconstructionOptimizer rec = new RangeReconstructionOptimizer(this);
					try{
						AP_Praxis.praxis(2, init, rec, in, out);
					}catch(java.lang.ArithmeticException ae){
						continue;
					}
					System.out.print("\t"+
							Math.log(jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND)))
							+"\t"+init[1]+"\t"+init[2]+"\t"+out[2]+"\n"
					);
				}/*
				 * end opt code
				 */
				else{
					this.eval_likelihood();
					((HashMap<int [] ,Double>)node.getObject(DISTMAP)).put(
							ratemodel.get_dists()[i],
							-Math.log(jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND))));
					//System.out.print("\t"+
					//		Math.log(jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND)))
					//		+"\n"
					//);
				}
			}
		}
	}

	private void clear_start_dists(){
		for(int j=0;j<tree.getExternalNodeCount();j++){
			Node node = tree.getExternalNode(j);
			if(((ArrayList<BranchSegment>)node.getObject(BS))!=null){
				for(int i=0;i<((ArrayList<BranchSegment>)node.getObject(BS)).size();i++){
					((ArrayList<BranchSegment>)node.getObject(BS)).get(i).set_startdists(new ArrayList<int[]>());
				}
			}
		}
		for(int j=0;j<tree.getInternalNodeCount();j++){
			Node node = tree.getInternalNode(j);
			if(((ArrayList<BranchSegment>)node.getObject(BS))!=null){
				for(int i=0;i<((ArrayList<BranchSegment>)node.getObject(BS)).size();i++){
					((ArrayList<BranchSegment>)node.getObject(BS)).get(i).set_startdists(new ArrayList<int[]>());
				}
			}
		}
	}

	public void calc_internal_likelihoods_all_ranges(boolean opt){
		//calc_internal_likelihoods_all_ranges(tree.getInternalNode("M"),opt);
		calc_internal_likelihoods_all_ranges(tree.getRoot(),opt);
	}
	private void calc_internal_likelihoods_all_ranges(Node node, boolean opt){
		for(int i=0;i<node.getChildCount();i++){
			calc_internal_likelihoods_all_ranges(node.getChild(i),opt);
		}
		if(node.isInternal()){
			//System.out.println(node.getChild(0).getName()+"\t"+node.getChild(1).getName());
			ArrayList<Double> lt = new ArrayList<Double>();
			node.assocObject(LTGN,lt);
			HashMap<Double,String> mlt = new HashMap<Double,String>();
			node.assocObject(MAPLTGN,mlt);
			HashMap<Double,String> anc = new HashMap<Double,String>();
			node.assocObject(ANCMAP,anc);
			HashMap<String,Double> a = new HashMap<String,Double> ();
			node.assocObject(DISTMAP,a);
			this.clear_start_dists();
			//this.setup_branch_segments(tree.getRoot());
			//this.setup_tip_conditionals();
			for(int i=0;i<ratemodel.get_dists().length;i++){
				int [][][] iters = this.iter_dist_splits_weighted(ratemodel.get_dists()[i]);
				for(int j=0;j<iters.length;j++){
					int [] d1 = iters[j][0];
					int [] d2 = iters[j][1];
					int which0 = ((ArrayList<BranchSegment>)node.getChild(0).getObject(BS)).size()-1;
					((ArrayList<BranchSegment>)node.getChild(0).getObject(BS)).get(which0).set_startdist(d1);
					int which1 = ((ArrayList<BranchSegment>)node.getChild(1).getObject(BS)).size()-1;
					((ArrayList<BranchSegment>)node.getChild(1).getObject(BS)).get(which1).set_startdist(d2);
					//could add optimization code here
					/*
					 * opt code
					 */
					if(opt == true){
						double [] init = {666,0.1,0.1};
						double [] in = new double [10];
						in [0] = jade.math.PrecisionCalculator.getMachinePrecision();
						//in [0] = 0.00000000001;
						in [1] = 1.1102230246251565E-8;
						//in [1] = 3.1622776601683794E-6;
						//System.out.println("s "+Math.sqrt(in[0]));
						in [2] = 0.000001;
						in [5] = 1000;
						in [6] = 0.01;
						in [7] = 1;
						in [8] = 1;
						in [9] = -1;
						double [] out = new double [7];
						RangeReconstructionOptimizer rec = new RangeReconstructionOptimizer(this);
						try{
							AP_Praxis.praxis(2, init, rec, in, out);
						}catch(java.lang.ArithmeticException ae){
							continue;
						}

						/*
						 * end opt code
						 */
						//System.out.println(
						//		jade.math.Utils.printCombIVec(d1,d2)+" "+
						//		jade.math.Utils.printIVec(d1)+" "+
						//		jade.math.Utils.printIVec(d2)+" "+
						//		"\t"+init[1]+"\t"+init[2]+"\t"+
						//		out[2]
						//);
					}else{
						this.eval_likelihood();
						/*System.out.print(jade.math.Utils.printCombIVec(d1,d2)+" "+
								jade.math.Utils.printIVec(d1)+" "+
								jade.math.Utils.printIVec(d2)+" "+
								Math.log(jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND)))+"\n"
						);*/
						((ArrayList<Double>)node.getObject(LTGN)).
						add(-Math.log(jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND))));
						((HashMap<Double,String>)node.getObject(ANCMAP)).put(
								-Math.log(jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND))),
								jade.math.Utils.printCombIVec(d1,d2));
						((HashMap<Double,String>)node.getObject(MAPLTGN)).put(
								-Math.log(jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND))), 
								jade.math.Utils.printCombIVec(d1,d2)+"\t"+
								jade.math.Utils.printIVec(d1)+"\t"+
								jade.math.Utils.printIVec(d2));
						((HashMap<String,Double>)node.getObject(DISTMAP)).put(
								(jade.math.Utils.printIVec(d1)+"_"+
										jade.math.Utils.printIVec(d2)),
								Math.log(jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND))));
					}//else
				}//for each iter
			}//for each start dist
			Collections.sort(((ArrayList<Double>)node.getObject(LTGN)));
		}//if node is internal
	}

	private void setup_tip_conditionals() {
		for (int i = 0; i < tree.getExternalNodeCount(); i++) {
			double[] cond = new double[ratemodel.get_dists().length];
			for (int j = 0; j < cond.length; j++) {
				cond[j] = 0.0;
			}
			int id = 0;
			for (int j = 0; j < aln.getIdCount(); j++) {
				if (tree.getExternalNode(i).getName().compareTo(
						aln.getIdentifier(j)) == 0) {
					id = j;
				}// if
			}// for
			cond[ratemodel.get_distribution_index(aln
					.getAlignedSequenceString(id))] = 1.0;
			ArrayList<BranchSegment> bs = (ArrayList<BranchSegment>) tree
			.getExternalNode(i).getObject(BS);
			bs.get(0).set_conditionals(cond);
		}
	}

	private void setup_branch_segments(Node node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			setup_branch_segments(node.getChild(i));
		}
		if (node.hasParent()) {
			ArrayList<BranchSegment> abs = new ArrayList<BranchSegment>();
			double anc = node.getParent().getDistanceFromTip();// getage
			double dec = node.getDistanceFromTip();// getage
			double t = dec;
			int n = 0;
			while (t < anc) {
				int p = ratemodel.getRelevantPeriod(t);
				double p_start = ratemodel.getPeriods().get(p);
				double s = 0.0;// start
				if (anc < p_start) {// segment is shorter than period
					s = anc;
					// t = anc;
				} else {// segment is longer than period
					s = p_start;
					// t = p_start;
				}
				BranchSegment bs = new BranchSegment(s, t, n, p);// start,
				// end,
				// number,
				// period
				abs.add(bs);
				// System.out.println(s+"\t"+t);
				t = s;
				n++;
			}// while
			node.assocObject(BS, abs);// these are stored from youngest to
			// oldest, so 0 will be the one closest
			// to the tip and >0 is closer to node
		}// if
	}// private

	public int[][][] iter_dist_splits_weighted(int[] dist) {
		int[][][] ret = new int[0][0][0];
		if (jade.math.Utils.sum(dist) == 1) {
			ret = jade.math.Utils.iter_splitranges(dist);
			weight = 1.0;
		} else {
			weight = 1.0 / (jade.math.Utils.sum(dist) * 4);
			ret = jade.math.Utils.iter_splitranges(dist);

			//for sp in iter_dist_splits(dist):
			//    yield sp, wt
		}
		return ret;
	}

	private double weight;

	public static void main(String [] args){
		int [][][] ret = jade.math.Utils.iter_splitranges(new int[] {1,1,1,1,1,1,1});
		for(int i=0;i<ret.length;i++){
			for(int j=0;j<ret[i][0].length;j++){
				System.out.print(ret[i][0][j]);
			}
			System.out.print("\t");
			for(int j=0;j<ret[i][1].length;j++){
				System.out.print(ret[i][1][j]);
			}
			System.out.println();
		}System.out.println(ret.length);
	}
	
	private void setDistanceFromTipForBG() {
		for (int i = 0; i < tree.getExternalNodeCount(); i++) {
			Node cur = tree.getExternalNode(i);
			double curh = 0.0;
			while (cur != null) {
				cur.setDistanceFromTip(curh);
				curh += cur.getBL();
				cur = cur.getParent();
			}
		}
	}
}//class
