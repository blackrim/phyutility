package phyutility.consensus;
import jebl.evolution.trees.*;
import jebl.evolution.io.*;

import java.io.*;
import java.util.*;
public class Consensus {
	public Consensus(String filename, Double threshold, String outfile){
		try {
			//if(newick.toUpperCase() == "YES" || newick.toUpperCase() == "Y"){
				NewickImporter ni = new NewickImporter(new FileReader(filename),true);
				ArrayList<Tree> trees =(ArrayList<Tree>) ni.importTrees();
				RootedTree [] tr = new RootedTree[trees.size()];
				for(int i=0;i<tr.length;i++){
					tr[i] = (RootedTree)trees.get(i);
				}
				GreedyRootedConsensusTreeBuilder tb = new GreedyRootedConsensusTreeBuilder(tr,threshold, "greedy", true);
				FileWriter wr = new FileWriter(outfile);
				NexusExporter ne = new NexusExporter(wr);
				ne.exportTree(tb.build());
				wr.close();
			//}else{
			//	NexusImporter ni = new NexusImporter(new FileReader(filename));
			//	ArrayList<Tree> trees =(ArrayList<Tree>) ni.importTrees();
			//	RootedTree [] tr = new RootedTree[trees.size()];
			//	for(int i=0;i<tr.length;i++){
			//		tr[i] = (RootedTree)trees.get(i);
			//	}
			//	GreedyRootedConsensusTreeBuilder tb = new GreedyRootedConsensusTreeBuilder(tr,threshold, "greedy", true);
			//	FileWriter wr = new FileWriter(outfile);
			//	NewickExporter ne = new NewickExporter(wr);
			//	ne.exportTree(tb.build());
			//	wr.close();
			//}
		} catch (FileNotFoundException e) {} catch (IOException e) {} catch (ImportException e) {}
	}
	public static void main(String[] args) {
		//infile thresh outfile new
		Consensus m = new Consensus(args[0], Double.valueOf(args[1]), args[2]);		
	}
}
