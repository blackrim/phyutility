package jade.reconstruct.area;

import jade.tree.*;
import jade.data.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class BayesianMarginalRangeReconstructor{
	String LTGN = "likelihood_of_tree_given_n";
	String MAPLTGN = "map_of_likelihood_of_tree_given_n";
	private RateModel ratemodel;
	//int decimalPlace = 18;
	private MarginalRangeReconstructor rr;
	private ArrayList<Node> report_nodes;
	private String alnfile = "";
	private String treestring = "(((1:0.35790777972472654,2:0.35790777972472654)1111:24.81884979583625,3:25.176757575560977)0100:21.651674361662142,((((((4:3.2792789357060386,((5:1.2043992449029162,6:1.2043992449029162)1000:0.2327030710104765,7:1.4371023159133927)1111:1.842176619792646)1101:8.335942634266438,8:11.615221569972476)1000:3.3886557145980234,9:15.0038772845705)1000:4.94912402195925,(10:13.15511790777304,(11:8.946146423606933,(12:4.2155815898511975,13:4.2155815898511975)0110:4.730564833755736)1010:4.208971484166106)1010:6.79788339875671)1010:3.168415958701381,(14:14.757549326376676,(15:3.454726069656715,16:3.454726069656715)1010:11.302823256719961)1010:8.363867938854455)1000:4.054980973482451,((17:2.3602885397146522,18:2.3602885397146522)1000:2.5495179332403097,(19:1.2889113100832716,20:1.2889113100832716)0111:3.6208951628716903)1111:22.26659176575862)1101:19.652033698509538)0101);";
	private Tree tree;
	private Alignment aln;
	private double disp_sliding_window = 0.23;
	private double ext_sliding_window = 0.23;
	private int print_freq = 1000;
	private int report_freq = 100;
	private boolean to_file = true;
	private String outfile = "/home/smitty/scratch/bayes_test.txt";
	private String dir = "/home/smitty/scratch/";

	public BayesianMarginalRangeReconstructor(RateModel ratemodel, Tree tree,
			Alignment aln) {
		this.ratemodel = ratemodel;
		this.aln = aln;
		this.tree = tree;
		report_nodes = new ArrayList<Node>();
		//report_nodes.add(tree.getRoot());
		//report_nodes.add(tree.getInternalNode("M"));
		//report_nodes.add(tree.getInternalNode("X"));
		//report_nodes.add(tree.getInternalNode("Y"));
		//report_nodes.add(tree.getInternalNode("Z"));
	}

	public void start_run(int n, int nchains){
		File outf = new File(outfile);
		outf.delete();
		try {
			FileWriter fw = new FileWriter(outfile, true);
			fw.write("rep"+"\t"+"disp"+"\t"+"ext"+"\t"+"score"+"\n");
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * initiate files for report nodes
		 */
		for(int i=0;i<report_nodes.size();i++){
			outf = new File(dir+report_nodes.get(i).getName());
			outf.delete();
		}
		/*
		 * 
		 */
		Random ran = new Random();
		ArrayList<BayesChain> chains = new ArrayList<BayesChain>();
		for(int i=0;i<nchains;i++){
			if(nchains > 1 && i+1 != nchains){
				BayesChain chain = new BayesChain(0.5,1);
				chains.add(chain);
			}else{
				BayesChain chain = new BayesChain(0.01,0.01);
				chains.add(chain);
			}
		}
		/*
		 * priors
		 */
		jade.math.ExponentialDistribution disp = new jade.math.ExponentialDistribution();
		jade.math.ExponentialDistribution ext = new jade.math.ExponentialDistribution();
		disp.setFallOff(0.5);
		ext.setFallOff(1);
		/*
		 * start the initial chain
		 */
		double cur_disp = 0.1;
		double cur_ext = 1.1;
		rr = new MarginalRangeReconstructor(ratemodel, tree, aln);
		rr.set_dipsersal(cur_disp);
		rr.set_extinction(cur_ext);
		rr.set_ratemodel();
		double cur_score = Math.log(rr.eval_likelihood());
		for(int i=0;i<n;i++){//numofreps
			int best = 0;
			double bsc = 99999999;
			for(int j=0;j<chains.size();j++){
				chains.get(j).vag = chains.get(j).next_disp(cur_disp);
				chains.get(j).ext = chains.get(j).next_disp(cur_ext);
				rr = new MarginalRangeReconstructor(ratemodel, tree, aln);
				rr.set_dipsersal(chains.get(j).vag);
				rr.set_extinction(chains.get(j).ext);
				rr.set_ratemodel();
				chains.get(j).score = Math.log(rr.eval_likelihood());
				if(-chains.get(j).score<bsc){
					best = j;
					bsc = -chains.get(j).score;
				}
			}
			double new_score =chains.get(best).score;
			/*
			 * accept or reject
			 */
			boolean acc = false;
			double disp_prior_ratio = disp.getPDF(chains.get(best).vag)/disp.getPDF(cur_disp);
			double ext_prior_ratio = ext.getPDF(chains.get(best).ext)/ext.getPDF(cur_ext);
			double like_ratio = new_score/cur_score;
			double dou_acc = disp_prior_ratio*ext_prior_ratio*like_ratio;
			double min_acc = Math.min(1, dou_acc);
			if(ran.nextDouble()<min_acc){
				acc = true;
			}
			if(acc == true){
				cur_disp = chains.get(best).vag;
				cur_ext = chains.get(best).ext;
				cur_score = new_score;
			}else{
				chains.get(best).vag = cur_disp;
				chains.get(best).ext = cur_ext;
				chains.get(best).score = cur_score;
			}
			if((i%print_freq) == 0){
				System.out.print(i+"\t"+cur_disp+"\t"+cur_ext+"\t");
				for(int j=0;j<chains.size();j++){
					if(j==best)
						System.out.print("[");
					System.out.print(chains.get(j).score);
					if(j==best)
						System.out.print("]");
					System.out.print("\t");
				}
				System.out.println();
			}
			if((i%report_freq) == 0 && to_file == true){
				try {
					FileWriter fw = new FileWriter(outfile, true);
					fw.write(i+"\t"+cur_disp+"\t"+cur_ext+"\t"+cur_score+"\n");
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*
				 * report nodes
				 */
				if(report_nodes.size()>0){
					rr = new MarginalRangeReconstructor(ratemodel, tree, aln);
					rr.set_dipsersal(chains.get(best).vag);
					rr.set_extinction(chains.get(best).ext);
					rr.set_ratemodel();
					rr.eval_likelihood();
					rr.calc_internal_likelihoods_all_ranges(false);
					for(int j=0;j<report_nodes.size();j++){
						try {
							if(i == 0){
								FileWriter fw = new FileWriter(dir+report_nodes.get(j).getName(), true);
								for(int m=0;m<ratemodel.get_dists().length;m++){
									int [][][] iters = rr.iter_dist_splits_weighted(ratemodel.get_dists()[m]);
									for(int k=0;k<iters.length;k++){
										String pri = "";
										int [] d1 = iters[k][0];
										int [] d2 = iters[k][1];
										pri += jade.math.Utils.printIVec(d1)+"_"+jade.math.Utils.printIVec(d2)+"\t";
										fw.write(pri+"\t");
									}
								}
								fw.write("\n");
								fw.close();
							}
							FileWriter fw = new FileWriter(dir+report_nodes.get(j).getName(), true);
							String pri = i+"\t";
							for(int m=0;m<ratemodel.get_dists().length;m++){
								int [][][] iters = rr.iter_dist_splits_weighted(ratemodel.get_dists()[m]);
								
								for(int k=0;k<iters.length;k++){
									int [] d1 = iters[k][0];
									int [] d2 = iters[k][1];
									String get = jade.math.Utils.printIVec(d1)+"_"+jade.math.Utils.printIVec(d2);
									pri += ((HashMap<String ,Double>)report_nodes.get(j).getObject("internal_combined_likelihoods")).
									get((String)get)+"\t";
								}
							}
							fw.write(pri+"\n");
							fw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}
				}
			}
		}
	}

	public void start_run_single(int n){
		Random ran = new Random();
		jade.math.ExponentialDistribution disp = new jade.math.ExponentialDistribution();
		disp.setFallOff(0.5);
		jade.math.ExponentialDistribution ext = new jade.math.ExponentialDistribution();
		ext.setFallOff(10);
		double cur_disp = 0.5;
		double cur_ext = 9.8;
		rr = new MarginalRangeReconstructor(ratemodel, tree, aln);
		rr.set_dipsersal(cur_disp);
		rr.set_extinction(cur_ext);
		rr.set_ratemodel();
		double cur_score = Math.log(rr.eval_likelihood());
		boolean disp_bool = true;
		double new_disp = cur_disp;
		double new_ext = cur_ext;
		for(int i=0;i<n;i++){
			if(disp_bool == true){
				new_disp = Math.abs(cur_disp -(disp_sliding_window/2)+(disp_sliding_window*ran.nextDouble()));
				disp_bool = false;
			}
			else{
				new_ext = Math.abs(cur_ext -(ext_sliding_window/2)+(ext_sliding_window*ran.nextDouble()));
				disp_bool = true;
			}
			rr = new MarginalRangeReconstructor(ratemodel, tree, aln);
			rr.set_dipsersal(new_disp);
			rr.set_extinction(new_ext);
			rr.set_ratemodel();
			double new_score =Math.log(rr.eval_likelihood());
			/*
			 * accept or reject
			 */
			boolean acc = false;
			double disp_prior_ratio = disp.getPDF(new_disp)/disp.getPDF(cur_disp);
			double ext_prior_ratio = ext.getPDF(new_ext)/ext.getPDF(cur_ext);
			double like_ratio = new_score/cur_score;
			double dou_acc = disp_prior_ratio*ext_prior_ratio*like_ratio;
			double min_acc = Math.min(1, dou_acc);
			if(ran.nextDouble()<min_acc){
				acc = true;
			}
			if(acc == true){
				cur_disp = new_disp;
				cur_ext = new_ext;
				cur_score = new_score;
			}
			if((i%print_freq) == 0){
				System.out.println(i+"\t"+cur_disp+"\t"+cur_ext+"\t"+cur_score+
						"\t" +new_disp
						+"\t"+new_ext
						+"\t"+new_score
						+"\t"+cur_score
						+"\t"+like_ratio+"\t"+dou_acc);
			}
			if((i%report_freq) == 0 && to_file == true){
				try {
					FileWriter fw = new FileWriter(outfile, true);
					fw.write(i+"\t"+cur_disp+"\t"+cur_ext+"\t"+cur_score+"\n");
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
}//class
