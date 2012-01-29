package phyutility.leafstability;

import java.util.*;
import java.io.*;

import jade.tree.*;

public class Runner {

	private String filename;
	private ArrayList<Tree> trees = new ArrayList<Tree>();
	private ArrayList<String> exnames = new ArrayList<String>();
	private int [][] triplets;
	private HashMap<int [],Double> results = new HashMap<int[],Double>();
	
	
	public Runner(String filename){
		this.filename = filename;
	}

	public Runner(ArrayList<Tree> trees){
		this.trees = trees;
		this.initializeRun();
	}
	
	public void readTrees(){
		TreeReader tr = new TreeReader ();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filename));
			String str = "";
			while((str = br.readLine())!=null){
				tr.setTree(str);
				trees.add(tr.readTree());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("read "+trees.size()+" trees");
	}

	public void initializeRun(){
		triplets = jade.math.NChooseM.iterate(trees.get(0).getExternalNodeCount(), 3);
		System.out.println("number of triplets "+triplets.length);
		for(int i=0;i<trees.get(0).getExternalNodeCount();i++){
			exnames.add(trees.get(0).getExternalNode(i).getName());
		}
	}
	
	public void run(){
		Calculator cal = new Calculator();
		for(int i=0;i<triplets.length;i++){
			ArrayList<Integer> all = new ArrayList<Integer>();
			if(i%100 == 0)
				System.out.println("current triplet: "+i);
			String [] inner = {exnames.get(triplets[i][0]),exnames.get(triplets[i][1])};
			String [] outer = {exnames.get(triplets[i][0]),exnames.get(triplets[i][1]), exnames.get(triplets[i][2])};
			int count = 0;
			for(int j=0;j<trees.size();j++){
				count = count + cal.calc(inner, outer, trees.get(j));
			}
			all.add(count);
			inner[0] = exnames.get(triplets[i][0]);
			inner[1] = exnames.get(triplets[i][2]);
			count = 0;
			for(int j=0;j<trees.size();j++){
				count = count + cal.calc(inner, outer, trees.get(j));
			}
			all.add(count);
			inner[0] = exnames.get(triplets[i][1]);
			inner[1] = exnames.get(triplets[i][2]);
			count = 0;
			for(int j=0;j<trees.size();j++){
				count = count + cal.calc(inner, outer, trees.get(j));
			}
			all.add(count);
			Collections.sort(all);
			results.put(triplets[i], (all.get(2)-all.get(1))/Double.valueOf(trees.size()));
		}
	}
	
	public void printResults(){
		for(int i=0;i<exnames.size();i++){
			double mean = 0;
			int count = 0;
			for(int j=0;j<triplets.length;j++){
				if(triplets[j][0] == i || triplets[j][1] == i || triplets[j][2] == i){
					mean = mean + results.get(triplets[j]);
					count ++;
				}
			}
			mean = mean / count;
			System.out.println(exnames.get(i)+"\t"+mean);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Runner run = new Runner(args[0]);
		run.readTrees();
		run.initializeRun();
		run.run();
		run.printResults();
	}

}
