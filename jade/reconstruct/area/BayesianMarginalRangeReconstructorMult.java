package jade.reconstruct.area;

import jade.tree.*;
import jade.data.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class BayesianMarginalRangeReconstructorMult{
	String LTGN = "likelihood_of_tree_given_n";
	String MAPLTGN = "map_of_likelihood_of_tree_given_n";
	private RateModel ratemodel;
	private ArrayList<RangeReconstructor> rr = new ArrayList<RangeReconstructor>();
	private ArrayList<Tree> tree;
	private ArrayList<Alignment> aln;
	private double disp_sliding_window = 0.01;
	private double ext_sliding_window = 0.01;
	private int print_freq = 10;
	private int report_freq = 10;
	private int disp_para;
	private boolean to_file = true;
	private String outfile;

	public BayesianMarginalRangeReconstructorMult(RateModel ratemodel, ArrayList<Tree> tree,ArrayList<Alignment> aln) {
		this.ratemodel = ratemodel;
		this.aln = aln;
		this.tree = tree;
		this.disp_para = ratemodel.getPeriods().size()*
		((ratemodel.get_areas().size()*ratemodel.get_areas().size())-ratemodel.get_areas().size());
		System.out.println(this.disp_para);
	}

	public void start_run(int n, String outfile1){
		this.outfile = outfile1;
		File outf = new File(outfile);
		outf.delete();
		double accept = 0;
		double reject = 0;
		try {
			FileWriter fw = new FileWriter(outfile, true);
			fw.write("rep"+"\t");
			for(int i=0;i<disp_para;i++){
				fw.write("disp"+i+"\t");
			}
			fw.write("ext"+"\t"+"score"+"\n");
			/*
			 * 
			 */
			Random ran = new Random();
			/*
			 * priors
			 */
			jade.math.ExponentialDistribution disp = new jade.math.ExponentialDistribution();
			jade.math.ExponentialDistribution ext = new jade.math.ExponentialDistribution();
			disp.setFallOff(0.02);
			ext.setFallOff(0.02);
			//jade.math.NormalDistribution disp = new jade.math.NormalDistribution();
			//jade.math.NormalDistribution ext = new jade.math.NormalDistribution();
			//disp.setMean(0.02);
			//disp.setStDev(0.5);
			//ext.setMean(0.02);
			//ext.setStDev(0.5);
			/*
			 * start the initial chain
			 */
			double cur_ext = 0.02;
			for(int i=0;i<tree.size();i++){
				rr.add(new MarginalRangeReconstructor(ratemodel, tree.get(i), aln.get(i)));
			}
			
			ArrayList<ArrayList<ArrayList<Double>>> dold = new ArrayList<ArrayList<ArrayList<Double>>> ();
			for(int j=0;j<ratemodel.getPeriods().size();j++){//period
				ArrayList<ArrayList<Double>> tp = new ArrayList<ArrayList<Double>>();
				for(int k=0;k<ratemodel.get_areas().size();k++){
					ArrayList<Double> tpp = new ArrayList<Double>();
					for(int l=0;l<ratemodel.get_areas().size();l++){
						if(k!=l)
							tpp.add(0.02);
						else
							tpp.add(0.0);
					}
					tp.add(tpp);
				}
				dold.add(tp);
			}
			ArrayList<ArrayList<ArrayList<Double>>> dnew = new ArrayList<ArrayList<ArrayList<Double>>> ();
			for(int j=0;j<ratemodel.getPeriods().size();j++){//period
				ArrayList<ArrayList<Double>> tp = new ArrayList<ArrayList<Double>>();
				for(int k=0;k<ratemodel.get_areas().size();k++){
					ArrayList<Double> tpp = new ArrayList<Double>();
					for(int l=0;l<ratemodel.get_areas().size();l++){
						if(k!=l)
							tpp.add(0.02);
						else
							tpp.add(0.0);
					}
					tp.add(tpp);
				}
				dnew.add(tp);
			}
			int change = ran.nextInt(ratemodel.getPeriods().size()+1);
			double new_ext = cur_ext;
			double cur_score = eval_likelihood(dold,new_ext);
			for(int i=0;i<n;i++){//numofreps
				change = ran.nextInt(ratemodel.getPeriods().size()+1);
				int cur = 0;
				if(change == 0){
					new_ext = Math.abs(cur_ext +((ran.nextDouble()-0.5)*ext_sliding_window));
				}else{
					change = change -1;
					for(int j=0;j<dold.size();j++){//period
						if(cur == change){
							for(int k=0;k<dold.get(j).size();k++){
								for(int l=0;l<dold.get(j).get(k).size();l++){
									if(k != l){
										dnew.get(j).get(k).set(l,
												Math.abs(dold.get(j).get(k).get(l)
														+((ran.nextDouble()-0.5)*disp_sliding_window)));
									}
								}
							}
						}else{
							for(int k=0;k<dold.get(j).size();k++){
								for(int l=0;l<dold.get(j).get(k).size();l++){
									if(k != l){
										dnew.get(j).get(k).set(l,
												dold.get(j).get(k).get(l));
									}
								}
							}
						}
						cur++;
					}
				}
				
				double new_score = eval_likelihood(dnew,new_ext);
				/*
				 * accept or reject
				 */
				boolean acc = false;
				double disp_prior_ratio = 1;
				for(int j=0;j<dold.size();j++){//period
					for(int k=0;k<dold.get(j).size();k++){
						for(int l=0;l<dold.get(j).get(k).size();l++){
							if(k!=l){
								//System.out.println(disp.getPDF(dnew.get(j).get(k).get(l)));
								//System.out.println(disp.getPDF(dold.get(j).get(k).get(l)));
								disp_prior_ratio *= disp.getPDF(dnew.get(j).get(k).get(l))/
								disp.getPDF(dold.get(j).get(k).get(l));
							}
						}
					}
				}
				
				double ext_prior_ratio = ext.getPDF(new_ext)/ext.getPDF(cur_ext);
				double like_ratio = Math.exp(cur_score-new_score);
				double dou_acc = disp_prior_ratio*ext_prior_ratio*like_ratio;
				//double dou_acc = like_ratio;
				double min_acc = Math.min(1, dou_acc);
				if(ran.nextDouble()<min_acc){
					acc = true;
				}
				if(acc == true){
					copy(dold,dnew);
					cur_ext = new_ext;
					cur_score = new_score;
					accept ++;
				}else{
					reject ++;
				}
				if (accept > reject){
					disp_sliding_window *= Math.exp(1.0/accept);
					ext_sliding_window *= Math.exp(1.0/accept);
				}else if (accept < reject){
					disp_sliding_window /= Math.exp(1.0/reject);
					ext_sliding_window /= Math.exp(1.0/reject);
				}
				if((i%print_freq) == 0){
					System.out.println(i
							+"\t"+new_score
							+"\t"+cur_score
							+"\t"+like_ratio+"\t"+dou_acc+"\t"+accept+"\t"+reject);
				}
				if((i%report_freq) == 0 && to_file == true){
					try {
						fw.write(i+"\t");
						for(int j=0;j<dold.size();j++){//period
							for(int k=0;k<dold.get(j).size();k++){
								for(int l=0;l<dold.get(j).get(k).size();l++){
									if(k!=l)
										fw.write(dold.get(j).get(k).get(l)+"\t");
								}
							}
						}
						fw.write(cur_ext+"\t"+cur_score+"\n");
						fw.flush();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void copy(ArrayList<ArrayList<ArrayList<Double>>> d1, 
			ArrayList<ArrayList<ArrayList<Double>>> d2){
		for(int j=0;j<d1.size();j++){//period
			for(int k=0;k<d1.get(j).size();k++){
				for(int l=0;l<d1.get(j).get(k).size();l++){
					d1.get(j).get(k).set(l, d2.get(j).get(k).get(l));
				}
			}
		}
	}
	private double eval_likelihood(ArrayList<ArrayList<ArrayList<Double>>> d,double e){
		double lh = 1;
		for(int i=0;i<rr.size();i++){
			for(int j = 0;j<d.size();j++){
				rr.get(i).set_dispersalmatrix(d.get(j),j);
			}
			rr.get(i).set_extinction(e);
			rr.get(i).set_ratemodel();
			lh += -Math.log(rr.get(i).eval_likelihood());
		}
		return lh;
	}
	
}//class
