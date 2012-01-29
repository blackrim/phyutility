package jade.reconstruct.area;

import jade.math.AP_Praxis;
import jade.tree.*;
import jade.data.*;
import java.util.*;

public class JointRangeReconstructor implements RangeReconstructor {
	private RateModel ratemodel;

	private String LTGN = "likelihood_of_tree_given_n";

	private Tree tree;

	private Alignment aln;

	private String COND = "conditionals";

	private String BS = "branch_segment";

	public JointRangeReconstructor(RateModel ratemodel, Tree tree,
			Alignment aln) {
		this.ratemodel = ratemodel;
		this.aln = aln;
		this.tree = tree;
		TreeUtils tu = new TreeUtils();
		tu.setDistanceFromTip(tree);
		setup_branch_segments(tree.getRoot());
		setup_tip_conditionals();
	}

	public double eval_likelihood() {
		ancdist_conditional_like(tree.getRoot());
		return jade.math.Utils.sum((double[]) tree.getRoot().getObject(COND));
	}

	public void set_localextinction(ArrayList<Double> e){
		ratemodel.set_localextinction(e);
	}
	
	public void set_dispersalmatrix(ArrayList<ArrayList<Double>> d, int period){
		ratemodel.set_dispersalmatrix(d, period);
	}
	
	public void set_dipsersal(double d) {
		ratemodel.set_dispersal(d);
	}

	public void set_extinction(double e) {
		ratemodel.set_extinction(e);
	}

	public void set_ratemodel() {
		ratemodel.setup(false);
	}

	public Tree get_tree() {
		return tree;
	}
	
	public int get_num_areas(){
		return ratemodel.get_areas().size();
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
				int[][][] tret = iter_dist_splits_weighted(ratemodel.get_dists()[i]);
				double maxl = 0;
				double maxr = 0;
				for (int j = 0; j < tret.length; j++) {
					int[] d1 = tret[j][0];// left
					int[] d2 = tret[j][1];// right
					if(c1[ratemodel.get_distribution_index(d1)]>maxl){
						maxl = c1[ratemodel.get_distribution_index(d1)];
					}
					if(c2[ratemodel.get_distribution_index(d2)]>maxr){
						maxr = c2[ratemodel.get_distribution_index(d2)];
					}
					//double lh_part = (c1[ratemodel.get_distribution_index(d1)] * c2[ratemodel
					//                                                                .get_distribution_index(d2)]);
					//lh += lh_part * weight;
					//AncSplit as = new AncSplit(ratemodel.get_dists()[i],
					//		tret[j], weight, lh_part);
					//ancsplits.add(as);
				}
				double lh_part = maxl * maxr;
				lh += lh_part * weight;
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
		ArrayList<BranchSegment> segs = (ArrayList<BranchSegment>) node
		.getObject(BS);
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
					distrange.add(ratemodel.get_distribution_index(segs.get(seg)
							.get_startdists().get(i)));
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
			// System.out.println("d\t"+segs.get(seg).get_period());
			ret = v;
		}// for
		// System.out.println("a\t"+jade.math.Utils.printDVec(ret));
		node.assocObject(LTGN, ret);
		return ret;
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

	private int[][][] iter_dist_splits_weighted(int[] dist) {
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
		int [][][] ret = jade.math.Utils.iter_splitranges(new int[] {1,0,0});
		for(int i=0;i<ret.length;i++){
			for(int j=0;j<ret[i][0].length;j++){
				System.out.print(ret[i][0][j]);
			}
			System.out.print("\t");
			for(int j=0;j<ret[i][1].length;j++){
				System.out.print(ret[i][1][j]);
			}
			System.out.println();
		}
	}
}//class
