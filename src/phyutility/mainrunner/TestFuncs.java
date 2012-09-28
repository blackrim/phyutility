/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package phyutility.mainrunner;


import jade.tree.TreeReader;

import java.util.*;
import java.io.*;

import jebl.evolution.io.ImportException;
import phyutility.concat.Concat;
import phyutility.drb.WwdEmbedded;

/**
 *
 * @author smitty
 */

public class TestFuncs {
	/*
	 * NEW
	 */
	private boolean monophylyMask = false;
	
	private boolean ambigdups = false;
	
	private boolean paraortho = false;
	
	private boolean paraorthoprune = false;
	
	
	/*
	 * END NEW
	 */
	//db
	private WwdEmbedded db;
	
	private boolean out_oth = false; //-othout
	private boolean treesupp = false; //-ts
	private boolean prune = false; //use -names for which to prune
	private String tree = null; //-tree
	private boolean consensus = false; //-con requires threshold
	private double threshold = 0.0; //-t
	private boolean reroot = false; //-rr
	private boolean derb = false;
	//sequence functions
	private boolean concat = false;
	private boolean clean = false; 
	private double cleannum = 0.5;
	private boolean parse = false;
	private int parsenum = 1;
	
	private ArrayList<String> mrcanames; //-names
	private String logfile = null; //-log
	private String outfile = null; //-out
	private ArrayList<String> infiles; //-in
	private FileWriter fw;
	
	public TestFuncs(String [] args){
		System.out.println("YOU HAVE ENTERED A PLACE YOU MAY WANT TO LEAVE");
		processArgs(args);
		runArgs();
	}
	
	private void processArgs(String [] args){
		startLogging();
		for(int i=0;i<args.length;i++){
			if(args[i].toLowerCase().compareTo("-mm")==0){
				monophylyMask = true;
				log("monophyly movement\n");
			}else if(args[i].toLowerCase().compareTo("-po")==0){
				paraortho = true;
				log("paralog ortholog\n");
			}else if(args[i].toLowerCase().compareTo("-ad")==0){
				this.ambigdups = true;
				log("ambiguous duplicates\n");
			}else if(args[i].toLowerCase().compareTo("-popr")==0){
				this.paraorthoprune = true;
				log("paralog ortholog pruning (the new one)\n");
			}else if(args[i].toLowerCase().compareTo("-ts")==0){
				treesupp = true;
				log("tree support\n");
			}else if(args[i].toLowerCase().compareTo("-pr")==0){
				prune = true;
			}else if(args[i].toLowerCase().compareTo("-t")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a threshhold after -t");
					System.exit(0);
				}else{
					threshold = Double.valueOf(args[i]);
				}
			}else if(args[i].toLowerCase().compareTo("-names")==0){
				mrcanames = new ArrayList<String>();
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter taxon names after -names");
					System.exit(0);
				}else{
					while(args[i].trim().startsWith("-")==false){
						mrcanames.add(args[i]);
						log("mrca: "+args[i]+"\n");
						i++;
						if(i >= args.length){
							break;
						}
					}
					i--;
				}
			}
			//seqence
			else if(args[i].toLowerCase().compareTo("-concat")==0){
				concat = true;
				log("concat\n");
			}else if(args[i].toLowerCase().compareTo("-clean")==0){
				clean = true;
				log("clean\n");
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a cleannum after -clean");
					System.exit(0);
				}else{
					cleannum = Double.valueOf(args[i]);
					log("cleannum: "+cleannum+"\n");
				}
			}else if(args[i].toLowerCase().compareTo("-parse")==0){
				parse = true;
				log("parse\n");
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a parsenum after -parse");
					System.exit(0);
				}else{
					parsenum = Integer.valueOf(args[i]);
					log("parsenum: "+parsenum+"\n");
				}
			}
			//other
			else if(args[i].toLowerCase().compareTo("-out")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a out filename after -out");
					System.exit(0);
				}else{
					outfile = args[i];
					log("outfile: "+outfile+"\n");
				}
			}else if(args[i].toLowerCase().compareTo("-in")==0){
				infiles = new ArrayList<String>();
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter filenames after -in");
					System.exit(0);
				}else{
					while(args[i].trim().startsWith("-")==false){
						infiles.add(args[i]);
						log("in filename: "+args[i]+"\n");
						i++;
						if(i >= args.length){
							break;
						}
					}
					i--;
				}
			}else if(args[i].toLowerCase().compareTo("-log")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a out filename after -log");
					System.exit(0);
				}
			}else{
				System.out.println("don't recognize argument "+args[i]);
			}
		}
	}
	
	private void runArgs(){
		int x = 0;
		if(treesupp == true){
			x++;
		}if(prune == true){
			x++;
		}if(monophylyMask == true){
			x++;
		}if(this.paraortho == true){
			x++;
		}if(this.ambigdups == true){
			x++;
		}if(this.paraorthoprune == true){
			x++;
		}
		//sequence
		if(concat == true){
			x++;
		}if(parse == true){
			x++;
		}if(clean == true){
			x++;
		}if(x == 0){
			System.out.println("you have to enter some sort of analysis");
			printUsage();
			System.exit(0);
		}if(x > 1){
			System.out.println("you can only do one thing at a time");
			printUsage();
			System.exit(0);
		}
		if(infiles == null){
			System.out.println("you have to enter some infiles (-in)");
			printUsage();
			System.exit(0);
		}
		/*
		 * NEW
		 */
		if(this.monophylyMask == true){
			this.monophylyMasking();
			System.exit(0);
		}if(this.paraortho == true){
			this.paralog();
			System.exit(0);
		}if(this.ambigdups == true){
			this.ambigdup();
		}if(this.paraorthoprune == true){
			this.paraorthoprune();
		}
		/*
		 * END NEW
		 */
		if(consensus == true){
			consensus();
			System.exit(0);
		}if(reroot == true){
			reroot();
			System.exit(0);
		}if(prune == true){
			prune();
			System.exit(0);
		}
		//sequence
		if(concat == true){
			concat();
			System.exit(0);
		}
	}
	
	private void monophylyMasking(){
		if(outfile == null){
			System.out.println("please put an out file (-out)");
			System.exit(0);
		}
		/*
		 * masking
		 * 
		 * BASICALLY
		 * move from the tips to the root and detect monophyly of things with
		 * the first two parts of the name identical.
		 * DON'T cross the root.
		 * 
		 * EASIEST way is to just check each taxa name
		 */
		
		/*
		 * get all the trees
		 */
		ArrayList<jade.tree.Tree> intrees = simpleReadJADETrees();
		/*
		 * go through each tree and check for monophyly each taxa (and mask
		 * where found)
		 */
		for(int i=0;i<intrees.size();i++){
			//System.out.println("==");
			//make an arraylist for each and remove as it goes
			ArrayList<jade.tree.Node> toprune = new ArrayList<jade.tree.Node>();
			boolean keep = true;
			while (keep == true) {
				for (int j = 0; j < intrees.get(i).getInternalNodeCount(); j++) {
					ArrayList<ArrayList<jade.tree.Node>> allChNdArr =
						new ArrayList<ArrayList<jade.tree.Node>>();
					ArrayList<jade.tree.Node> treeExtNodes =
						this.getExtNodesFromIntNode(intrees.get(i).getRoot());
					jade.tree.Node innode = intrees.get(i).getInternalNode(j);
					ArrayList<jade.tree.Node> left = this.getDifferenceBetweenArray2(treeExtNodes,
						this.getExtNodesFromIntNode(innode));
					/*
					 * get arralists for all the children from the internal node
					 */
					allChNdArr.add(left);
					for (int k = 0; k < innode.getChildCount(); k++) {
						allChNdArr.add(this.getExtNodesFromIntNode(innode.getChild(k)));
					}
					ArrayList<ArrayList<jade.tree.Node>> erase = new ArrayList<ArrayList<jade.tree.Node>>();
					for (int k = 0; k < allChNdArr.size(); k++) {
						if (allChNdArr.get(k).size() == 0 || this.allNamesSame(allChNdArr.get(k)) == false) {
							erase.add(allChNdArr.get(k));
						}
					}
					for (int k = 0; k < erase.size(); k++) {
						allChNdArr.remove(erase.get(k));
					}
					//System.out.println("--"+allChNdArr.size());
					Iterator it = allChNdArr.iterator();
					keep = false;
					while (it.hasNext()) {
						ArrayList<jade.tree.Node> nds = (ArrayList<jade.tree.Node>) it.next();
						erase = new ArrayList<ArrayList<jade.tree.Node>>();
						for (int k = 0; k < allChNdArr.size(); k++) {
							if (allChNdArr.get(k) != nds) {
								if (nds.get(0).getName().split("@")[0].compareTo(
									allChNdArr.get(k).get(0).getName().split("@")[0]) == 0) {
									nds = this.combineLists(nds, allChNdArr.get(k));
									erase.add(allChNdArr.get(k));
								}
							}
						}
						for (int k = 0; k < erase.size(); k++) {
							allChNdArr.remove(erase.get(k));
						}
						/*
						 * remove all but one
						 */
						for (int k = 1; k < nds.size(); k++) {
							System.out.println(nds.get(k).getName());
							if(intrees.get(i).getExternalNode(nds.get(k).getName()).getParent()==
								intrees.get(i).getRoot()){
								intrees.get(i).getRoot().removeChild(nds.get(k));
							}else{
								intrees.get(i).pruneExternalNode(nds.get(k));
							}
							j = 0;
							keep = true;
						}
					}
				}
			}
			try {
				BufferedWriter fw = new BufferedWriter(new FileWriter(outfile));
				fw.write(intrees.get(i).getRoot().getNewick(true)+";\n");
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void ambigdup(){
		if(outfile == null){
			System.out.println("please put an out file (-out)");
			System.exit(0);
		}
		ArrayList<jade.tree.Tree> intrees = simpleReadJADETrees();
		for(int i=0;i<intrees.size();i++){
			ArrayList<jade.tree.Node> allseqs = this.getExtNodesFromIntNode(intrees.get(i).getRoot());
			int numOfNames = this.numOfDiffs(allseqs);
			ArrayList<String> uniqNames = this.nameDiffs(allseqs);
			int largest = 0;
			for(int j=0;j<intrees.get(i).getInternalNodeCount();j++){
				ArrayList<ArrayList<jade.tree.Node>> allChNdArr = 
					new ArrayList<ArrayList<jade.tree.Node>>();
				ArrayList<jade.tree.Node> treeExtNodes = 
					this.getExtNodesFromIntNode(intrees.get(i).getRoot());
				jade.tree.Node innode = intrees.get(i).getInternalNode(j);
				ArrayList<jade.tree.Node> left = this.getDifferenceBetweenArray(treeExtNodes,
					this.getExtNodesFromIntNode(innode));
				/*
				 * get arralists for all the children from the internal node
				 */
				if(innode != intrees.get(i).getRoot())
					allChNdArr.add(left);
				for(int k=0;k<innode.getChildCount();k++){
					allChNdArr.add(this.getExtNodesFromIntNode(innode.getChild(k)));
				}
				
				//COMBINED LISTS
				ArrayList<ArrayList<jade.tree.Node>> COMBINED = new ArrayList<ArrayList<jade.tree.Node>>();
				int[][] y = jade.math.NChooseM.iterate_all_bv2small(allChNdArr.size());
				for (int w = 0; w < y.length; w++) {
					ArrayList<jade.tree.Node> TCOMBINED = new ArrayList<jade.tree.Node>();
					for (int k = 0; k < y[w].length; k++) {
						if(y[w][k] == 1){
							TCOMBINED = this.combineLists(TCOMBINED, allChNdArr.get(k));
						}
					}
					COMBINED.add(TCOMBINED);
				}
				for(int w = 0;w < COMBINED.size();w++){
					if(COMBINED.get(w).size() == numOfNames &&
						containsOnly(COMBINED.get(w),uniqNames) &&
						containsAll(COMBINED.get(w), allseqs) &&
						this.numOfDiffs(COMBINED.get(w))==numOfNames){
						System.out.println(numOfNames +" "+COMBINED.get(w).size());
						
						//for(int k=0;k<COMBINED.size();k++){
							//System.out.println("=====");
							ArrayList<jade.tree.Node> TCOMBINED = COMBINED.get(w);
							for(int h=0;h<TCOMBINED.size();h++){
								System.out.println(TCOMBINED.get(h).getName());
							}
						//}
						
						jade.tree.TreeReader tr = new jade.tree.TreeReader();
						tr.setTree(intrees.get(i).getRoot().getNewick(true)+";");
						jade.tree.Tree trt = tr.readTree();
						ArrayList<jade.tree.Node> del = this.getDifferenceBetweenArray(this.getExtNodesFromIntNode(intrees.get(i).getRoot()),
							COMBINED.get(w)
							);
						System.out.println(del.size());
						for(int k=0;k<del.size();k++){
							if(trt.getExternalNode(del.get(k).getName()).getParent()==
								trt.getRoot()){
								trt.getRoot().removeChild(trt.getExternalNode(del.get(k).getName()));
								if(trt.getRoot().getChildCount()==1){
									jade.tree.Node nr = trt.getRoot().getChild(0);
									trt.setRoot(nr);
									trt.processRoot();
								}
								System.out.println("YEAH"+trt.getRoot().getChildCount());
							}else{
								trt.pruneExternalNode(trt.getExternalNode(del.get(k).getName()));
							}
							System.out.println("--"+del.get(k).getName());
							trt.processRoot();
						}
						try {
							BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
							bw.write(trt.getRoot().getNewick(true) + ";\n");
							bw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (int k = 0; k < COMBINED.get(w).size(); k++) {
							this.log(COMBINED.get(w).get(k).getName()+"\n");
							//allseqs.remove(COMBINED.get(w).get(k));
						}
					}
					
				}
				
			}
		}
	}
	
	private void paralog(){
		if(outfile == null){
			System.out.println("please put an out file (-out)");
			System.exit(0);
		}
		ArrayList<jade.tree.Tree> intrees = simpleReadJADETrees();
		for(int i=0;i<intrees.size();i++){
			ArrayList<jade.tree.Node> allseqs = this.getExtNodesFromIntNode(intrees.get(i).getRoot());
			int numOfNames = this.numOfDiffs(allseqs);
			ArrayList<String> uniqNames = this.nameDiffs(allseqs);
			int largest = 0;
			for(int j=0;j<intrees.get(i).getInternalNodeCount();j++){
				ArrayList<ArrayList<jade.tree.Node>> allChNdArr = 
					new ArrayList<ArrayList<jade.tree.Node>>();
				ArrayList<jade.tree.Node> treeExtNodes = 
					this.getExtNodesFromIntNode(intrees.get(i).getRoot());
				jade.tree.Node innode = intrees.get(i).getInternalNode(j);
				ArrayList<jade.tree.Node> left = this.getDifferenceBetweenArray(treeExtNodes,
					this.getExtNodesFromIntNode(innode));
				/*
				 * get arralists for all the children from the internal node
				 */
				if(innode != intrees.get(i).getRoot())
					allChNdArr.add(left);
				for(int k=0;k<innode.getChildCount();k++){
					allChNdArr.add(this.getExtNodesFromIntNode(innode.getChild(k)));
				}
				
				//COMBINED LISTS
				ArrayList<ArrayList<jade.tree.Node>> COMBINED = new ArrayList<ArrayList<jade.tree.Node>>();
				int[][] y = jade.math.NChooseM.iterate_all_bv2small(allChNdArr.size());
				for (int w = 0; w < y.length; w++) {
					ArrayList<jade.tree.Node> TCOMBINED = new ArrayList<jade.tree.Node>();
					for (int k = 0; k < y[w].length; k++) {
						if(y[w][k] == 1){
							TCOMBINED = this.combineLists(TCOMBINED, allChNdArr.get(k));
						}
					}
					COMBINED.add(TCOMBINED);
				}
				for(int w = 0;w < COMBINED.size();w++){
					if(COMBINED.get(w).size() == numOfNames &&
						containsOnly(COMBINED.get(w),uniqNames) &&
						containsAll(COMBINED.get(w), allseqs) &&
						this.numOfDiffs(COMBINED.get(w))==numOfNames){
						System.out.println(numOfNames +" "+COMBINED.get(w).size());
						
						//for(int k=0;k<COMBINED.size();k++){
							//System.out.println("=====");
							ArrayList<jade.tree.Node> TCOMBINED = COMBINED.get(w);
							for(int h=0;h<TCOMBINED.size();h++){
								System.out.println(TCOMBINED.get(h).getName());
							}
						//}
						
						jade.tree.TreeReader tr = new jade.tree.TreeReader();
						tr.setTree(intrees.get(i).getRoot().getNewick(true)+";");
						jade.tree.Tree trt = tr.readTree();
						ArrayList<jade.tree.Node> del = this.getDifferenceBetweenArray(this.getExtNodesFromIntNode(intrees.get(i).getRoot()),
							COMBINED.get(w)
							);
						System.out.println(del.size());
						for(int k=0;k<del.size();k++){
							if(trt.getExternalNode(del.get(k).getName()).getParent()==
								trt.getRoot()){
								trt.getRoot().removeChild(trt.getExternalNode(del.get(k).getName()));
								if(trt.getRoot().getChildCount()==1){
									jade.tree.Node nr = trt.getRoot().getChild(0);
									trt.setRoot(nr);
									trt.processRoot();
								}
								System.out.println("YEAH"+trt.getRoot().getChildCount());
							}else{
								trt.pruneExternalNode(trt.getExternalNode(del.get(k).getName()));
							}
							System.out.println("--"+del.get(k).getName());
							trt.processRoot();
						}
						try {
							BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
							bw.write(trt.getRoot().getNewick(true) + ";\n");
							bw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (int k = 0; k < COMBINED.get(w).size(); k++) {
							this.log(COMBINED.get(w).get(k).getName()+"\n");
							allseqs.remove(COMBINED.get(w).get(k));
						}
					}
					
				}
				
			}
		}
		
	}
	
	int U = 1;
	
	private void paraorthoprune(){
		if(outfile == null){
			System.out.println("please put an out file (-out)");
			System.exit(0);
		}
		ArrayList<jade.tree.Tree> intrees = simpleReadJADETrees();
		for(int i=0;i<intrees.size();i++){
			ArrayList<jade.tree.Tree> finaltrees = new ArrayList<jade.tree.Tree>();
			//ArrayList<jade.tree.Node> allseqs = this.getExtNodesFromIntNode(intrees.get(i).getRoot());
			//int numOfNames = this.numOfDiffs(allseqs);
			//ArrayList<String> uniqNames = this.nameDiffs(allseqs);
			
			
			boolean something = true; /* keep going while there are clades left
									   * in the tree
									   */
			while (something == true) {
				boolean gotone = false;
				int largest = 0;
				ArrayList<jade.tree.Node> largestAR = new ArrayList<jade.tree.Node>();
				
				ArrayList<jade.tree.Node> allseqs = this.getExtNodesFromIntNode(intrees.get(i).getRoot());
				int numOfNames = this.numOfDiffs(allseqs);
				ArrayList<String> uniqNames = this.nameDiffs(allseqs);
				finaltrees = new ArrayList<jade.tree.Tree>();
				int LARGEST = 0;
				for (int j = 0; j < intrees.get(i).getInternalNodeCount(); j++) {
					ArrayList<ArrayList<jade.tree.Node>> allChNdArr =
						new ArrayList<ArrayList<jade.tree.Node>>();
					ArrayList<jade.tree.Node> treeExtNodes =
						this.getExtNodesFromIntNode(intrees.get(i).getRoot());
					jade.tree.Node innode = intrees.get(i).getInternalNode(j);
					ArrayList<jade.tree.Node> left = this.getDifferenceBetweenArray(treeExtNodes,
						this.getExtNodesFromIntNode(innode));
					/*
					 * get arralists for all the children from the internal node
					 */
					if (innode != intrees.get(i).getRoot()) {
						allChNdArr.add(left);
					}
					for (int k = 0; k < innode.getChildCount(); k++) {
						allChNdArr.add(this.getExtNodesFromIntNode(innode.getChild(k)));
					}

					//COMBINED LISTS
					ArrayList<ArrayList<jade.tree.Node>> COMBINED = new ArrayList<ArrayList<jade.tree.Node>>();
					int[][] y = jade.math.NChooseM.iterate_all_bv2small(allChNdArr.size());
					for (int w = 0; w < y.length; w++) {
						ArrayList<jade.tree.Node> TCOMBINED = new ArrayList<jade.tree.Node>();
						for (int k = 0; k < y[w].length; k++) {
							if (y[w][k] == 1) {
								TCOMBINED = this.combineLists(TCOMBINED, allChNdArr.get(k));
							}
						}
						COMBINED.add(TCOMBINED);
					}
					for (int w = 0; w < COMBINED.size(); w++) {
						if (COMBINED.get(w).size() == numOfNames &&
							containsOnly(COMBINED.get(w), uniqNames) &&
							containsAll(COMBINED.get(w), allseqs) &&
							this.numOfDiffs(COMBINED.get(w)) == numOfNames) {
							System.out.println(numOfNames + " " + COMBINED.get(w).size());

							//for(int k=0;k<COMBINED.size();k++){
							//System.out.println("=====");
							ArrayList<jade.tree.Node> TCOMBINED = COMBINED.get(w);
							for (int h = 0; h < TCOMBINED.size(); h++) {
								System.out.println(TCOMBINED.get(h).getName());
							}
							//}

							jade.tree.TreeReader tr = new jade.tree.TreeReader();
							tr.setTree(intrees.get(i).getRoot().getNewick(true) + ";");
							jade.tree.Tree trt = tr.readTree();
							ArrayList<jade.tree.Node> del = this.getDifferenceBetweenArray(this.getExtNodesFromIntNode(intrees.get(i).getRoot()),
								COMBINED.get(w));
							System.out.println(del.size());
							for (int k = 0; k < del.size(); k++) {
								if (trt.getExternalNode(del.get(k).getName()).getParent() ==
									trt.getRoot()) {
									trt.getRoot().removeChild(trt.getExternalNode(del.get(k).getName()));
									if (trt.getRoot().getChildCount() == 1) {
										jade.tree.Node nr = trt.getRoot().getChild(0);
										trt.setRoot(nr);
										trt.processRoot();
									}
									System.out.println("YEAH" + trt.getRoot().getChildCount());
								} else {
									trt.pruneExternalNode(trt.getExternalNode(del.get(k).getName()));
								}
								System.out.println("--" + del.get(k).getName());
								trt.processRoot();
							}
							/*try {
								BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
								bw.write(trt.getRoot().getNewick(true) + ";\n");
								bw.close();
								
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
							finaltrees.add(trt);
							for (int k = 0; k < COMBINED.get(w).size(); k++) {
								this.log(COMBINED.get(w).get(k).getName() + "\n");
								//allseqs.remove(COMBINED.get(w).get(k));
							}
							gotone = true;
						}else{//get the largest clade
							if(this.allUnique(COMBINED.get(w)) && 
								this.numOfDiffs(COMBINED.get(w))>LARGEST){
								LARGEST = this.numOfDiffs(COMBINED.get(w));
								largestAR = COMBINED.get(w);
							}
						}
					}
				}//end of internal node
				if(U==1 && gotone == false){
					System.out.println(LARGEST);
					System.out.println(numOfNames + " " + largestAR.size());

					//for(int k=0;k<COMBINED.size();k++){
					//System.out.println("=====");
					ArrayList<jade.tree.Node> TCOMBINED =largestAR;
					for (int h = 0; h < TCOMBINED.size(); h++) {
						System.out.println(TCOMBINED.get(h).getName());
					}
					//}

					jade.tree.TreeReader tr = new jade.tree.TreeReader();
					tr.setTree(intrees.get(i).getRoot().getNewick(true) + ";");
					if(intrees.get(i).getExternalNodeCount()==1){
						tr.setTree("("+intrees.get(i).getRoot().getNewick(true) + ");");
					}
					jade.tree.Tree trt = tr.readTree();
					if (trt.getExternalNodeCount() > 1) {
						ArrayList<jade.tree.Node> del = this.getDifferenceBetweenArray(this.getExtNodesFromIntNode(intrees.get(i).getRoot()),
							largestAR);
						System.out.println(del.size());
						for (int k = 0; k < del.size(); k++) {
							if (trt.getExternalNode(del.get(k).getName()).getParent() ==
								trt.getRoot()) {
								trt.getRoot().removeChild(trt.getExternalNode(del.get(k).getName()));
								if (trt.getRoot().getChildCount() == 1) {
									jade.tree.Node nr = trt.getRoot().getChild(0);
									trt.setRoot(nr);
									trt.processRoot();
								}
								System.out.println("YEAH" + trt.getRoot().getChildCount());
							} else {
								trt.pruneExternalNode(trt.getExternalNode(del.get(k).getName()));
							}
							System.out.println("--" + del.get(k).getName());
							trt.processRoot();
						}
					}
					/*try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
					bw.write(trt.getRoot().getNewick(true) + ";\n");
					bw.close();
					
					
					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}*/
					finaltrees.add(trt);
					for (int k = 0; k < largestAR.size(); k++) {
						this.log(largestAR.get(k).getName() + "\n");
					//allseqs.remove(COMBINED.get(w).get(k));
					}
				}
				if (finaltrees.size() == 0){
					something = false;
				}else{
					/*
					 * put ambiguity code here 
					 */
					//get rid of identical trees
					System.out.println("s"+finaltrees.size());
					for(int z=0;z<finaltrees.size();z++){
						for(int x=0;x<finaltrees.size();x++){
							if(x != z){
								if(this.getDifferenceBetweenArray2(
									this.getExtNodesFromIntNode(finaltrees.get(x).getRoot()), 
									this.getExtNodesFromIntNode(finaltrees.get(z).getRoot())).size()
									>0){
									finaltrees.remove(x);
									z = 0; x = 0;
								}
							}
						}
					}
					//get rid of ambigious tips 
					
					System.out.println("e"+finaltrees.size());
					
					/*
					 * get largest
					 */ 
					int tlarge = 0;
					int elarge = 0;
					for(int z=0;z<finaltrees.size();z++){
						if(finaltrees.get(z).getExternalNodeCount()>elarge){
							elarge = finaltrees.get(z).getExternalNodeCount();
							tlarge = z;
						}
					}
					/*
					 * write to file
					 */ 
					for(int x = 0; x < finaltrees.size();x++){
						if(finaltrees.get(x).getExternalNodeCount() == elarge){
							try {
								BufferedWriter bw = new BufferedWriter(new FileWriter(outfile, true));
								bw.write(finaltrees.get(x).getRoot().getNewick(true) + ";\n");
								bw.close();			
							} catch (IOException e) {
							// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					/*
					 * remove the largest one 
					 */
					for (int x = 0; x < finaltrees.size(); x++) {
						if (finaltrees.get(x).getExternalNodeCount() == elarge) {
							if (finaltrees.get(x).getExternalNodeCount() >=
								intrees.get(i).getExternalNodeCount() ) {//had a -1 at end
								something = false;
							} else {
								for (int z = 0; z < finaltrees.get(x).getExternalNodeCount(); z++) {
									if (intrees.get(i).getExternalNode(finaltrees.get(x).getExternalNode(z).getName()).getParent() ==
										intrees.get(i).getRoot()) {
										intrees.get(i).getRoot().removeChild(intrees.get(i).getExternalNode(finaltrees.get(x).getExternalNode(z).getName()));
										if (intrees.get(i).getRoot().getChildCount() == 1) {
											jade.tree.Node nr = intrees.get(i).getRoot().getChild(0);
											intrees.get(i).setRoot(nr);
											intrees.get(i).processRoot();
										}
										System.out.println("YEAH" + intrees.get(i).getRoot().getChildCount());
									} else {
										intrees.get(i).pruneExternalNode(intrees.get(i).getExternalNode(finaltrees.get(x).getExternalNode(z).getName()));
									}
								}
							}

						}
					}
					
				}
			}//end of while
		}
	}
	
	private void consensus(){
		ArrayList<jebl.evolution.trees.Tree> intrees = new ArrayList<jebl.evolution.trees.Tree>();
		ArrayList<jebl.evolution.trees.Tree> alltrees = new ArrayList<jebl.evolution.trees.Tree>();
		boolean rooted = false;
		for(int i=0;i<infiles.size();i++){
			if(testForNexus(infiles.get(0))){
				jebl.evolution.io.NexusImporter ni;
				try {
					ni = new jebl.evolution.io.NexusImporter(new FileReader(infiles.get(0)));
					alltrees =  (ArrayList<jebl.evolution.trees.Tree>)ni.importTrees();
					if(getJadeTreeFromJeblTree(alltrees.get(0)).getRoot().getChildCount()>2){
						for(int j=0;j<alltrees.size();j++){
							intrees.add(alltrees.get(j));
						}
					}else{
						rooted = true;
						for(int j=0;j<alltrees.size();j++){
							intrees.add(alltrees.get(j));
						}
					}
					alltrees = null;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ImportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				jebl.evolution.io.NewickImporter ni;
				try {
					ni = new jebl.evolution.io.NewickImporter(new FileReader(infiles.get(0)), true);
					alltrees =  (ArrayList<jebl.evolution.trees.Tree>)ni.importTrees();
					if(getJadeTreeFromJeblTree(alltrees.get(0)).getRoot().getChildCount()>2){
						for(int j=0;j<alltrees.size();j++){
							intrees.add(alltrees.get(j));
						}
					}else{
						rooted = true;
						for(int j=0;j<alltrees.size();j++){
							intrees.add(alltrees.get(j));
						}
					}
					alltrees = null;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ImportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(rooted == true){
			jebl.evolution.trees.RootedTree [] tr = new jebl.evolution.trees.RootedTree[intrees.size()];
			for(int i=0;i<tr.length;i++){
				tr[i] = (jebl.evolution.trees.RootedTree)intrees.get(i);
			}
			intrees = null;
			jebl.evolution.trees.GreedyRootedConsensusTreeBuilder con = new jebl.evolution.trees.GreedyRootedConsensusTreeBuilder(tr, threshold);
			if(outfile == null){
				log("place an outfile to get internal node values (-out)\n");
				System.out.println(jebl.evolution.trees.Utils.toNewick(con.build()));
			}else{
				try {
					if(out_oth == true){
						File toutf = new File(outfile+"temp");
						BufferedWriter fw = new BufferedWriter(new FileWriter(toutf));
						jebl.evolution.io.NexusExporter ne = new jebl.evolution.io.NexusExporter(fw);
						ne.exportTree(con.build());
						fw.close();
						ArrayList<jade.tree.Tree> ttrees = getNewickFromNexus(outfile+"temp");
						FileWriter outw = new FileWriter(outfile);
						for(int i=0;i<ttrees.size();i++){
							outw.write(ttrees.get(i).getRoot().getNewick(true)+";\n");
						}
						outw.close();
						toutf.delete();
					}else{
						BufferedWriter fw = new BufferedWriter(new FileWriter(outfile));
						jebl.evolution.io.NexusExporter ne = new jebl.evolution.io.NexusExporter(fw);
						ne.exportTree(con.build());
						fw.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			jebl.evolution.trees.Tree [] tr = new jebl.evolution.trees.Tree[intrees.size()];
			for(int i=0;i<tr.length;i++){
				tr[i] = intrees.get(i);
			}
			intrees = null;
			jebl.evolution.trees.GreedyUnrootedConsensusTreeBuilder con = new jebl.evolution.trees.GreedyUnrootedConsensusTreeBuilder(tr, null, threshold);
			if(outfile == null){
				log("place an outfile to get internal node values (-out)\n");
				System.out.println(jebl.evolution.trees.Utils.toNewick((jebl.evolution.trees.RootedTree)con.build()));
			}else{
				try {
					if(out_oth == true){
						File toutf = new File(outfile+"temp");
						BufferedWriter fw = new BufferedWriter(new FileWriter(toutf));
						jebl.evolution.io.NexusExporter ne = new jebl.evolution.io.NexusExporter(fw);
						ne.exportTree(con.build());
						fw.close();
						ArrayList<jade.tree.Tree> ttrees = getNewickFromNexus(outfile+"temp");
						FileWriter outw = new FileWriter(outfile);
						for(int i=0;i<ttrees.size();i++){
							outw.write(ttrees.get(i).getRoot().getNewick(true)+";\n");
						}
						outw.close();
						toutf.delete();
					}else{
						BufferedWriter fw = new BufferedWriter(new FileWriter(outfile));
						jebl.evolution.io.NexusExporter ne = new jebl.evolution.io.NexusExporter(fw);
						ne.exportTree(con.build());
						fw.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void reroot(){
		if(mrcanames == null){
			System.out.println("you not entered MRCA or tip names when performing a reroot analysis (-names), so phyutility will attempt an unroot");
			//System.exit(0);
		}
		ArrayList<jade.tree.Tree> intrees = new ArrayList<jade.tree.Tree>();
		ArrayList<jade.tree.Tree> alltrees = new ArrayList<jade.tree.Tree>();
		boolean testnex = false;
		for(int i=0;i<infiles.size();i++){
			testnex = testForNexus(infiles.get(i));
			if(testnex){
				if(derb == true){
					db = new WwdEmbedded("readtree");
					db.connectToDB();
					System.out.println("table made = "+db.makeTable(true));
					WwdEmbedded dbtemp = getNewickFromNexusDerby(infiles.get(i));
					for(int j=0;j<dbtemp.getTableTreeSize();j++){
						db.addTree(dbtemp.getTree(j)+";");
					}
				}else{
					alltrees = getNewickFromNexus(infiles.get(i));
					for(int j=0;j<alltrees.size();j++){
						intrees.add(alltrees.get(j));
					}
				}
			}else{
				try{
					BufferedReader br = new BufferedReader(new FileReader(infiles.get(i)));
					String str = "";
					jade.tree.TreeReader tr = new jade.tree.TreeReader();
					if(derb == true){
						db = new WwdEmbedded("readtree");
						db.connectToDB();
						System.out.println("table made = "+db.makeTable(true));
					}
					while((str = br.readLine())!=null){
						tr.setTree(str);
						if(derb == true){
							if(str.length()>1)
								db.addTree(tr.readTree().getRoot().getNewick(true)+";");
						}
						else
							intrees.add(tr.readTree());
					}
					if(derb ==true){
						System.out.println("read "+db.getTableTreeSize());
					}
					br.close();
				}catch(IOException ioe){}
			}
		}
		if(outfile == null){
			for(int i=0;i<intrees.size();i++){
				intrees.get(i).reRoot(intrees.get(i).getMRCA(mrcanames));
				System.out.println(intrees.get(i).getRoot().getNewick(true)+";");
			}
			log("finished reroot\n");
		}else{
			try{
				//nexus out
				if((testnex == true && out_oth != true)||
						(testnex == false && out_oth == true)){
					if(derb == true){
						if(mrcanames != null){
							Utils.newickToNexusRerootDerby(outfile, db, mrcanames);
						}else{
							Utils.newickToNexusUnrootDerby(outfile, db);
						}
						this.deleteDatabaseFolder(new File("null"));
					}else{
						ArrayList<String > nwa = new ArrayList<String>();
						for(int i=0;i<intrees.size();i++){
							if(mrcanames != null){
								intrees.get(i).reRoot(intrees.get(i).getMRCA(mrcanames));
							}else{
								intrees.get(i).unRoot(intrees.get(i).getRoot());
							}
							nwa.add(intrees.get(i).getRoot().getNewick(true)+";");
						}
						Utils.newickToNexus(outfile, nwa);
					}
				}else{
					if(derb == true){
						FileWriter outw = new FileWriter(outfile);
						for(int i=0;i<db.getTableTreeSize();i++){
							jade.tree.Tree x = db.getJadeTree(i);
							if(mrcanames != null){
								x.reRoot(x.getMRCA(mrcanames));
							}else{
								x.reRoot(x.getRoot());
							}
							outw.write(x.getRoot().getNewick(true)+";\n");
						}
						outw.close();
						this.deleteDatabaseFolder(new File("null"));
					}else{
						FileWriter outw = new FileWriter(outfile);
						for(int i=0;i<intrees.size();i++){
							if(mrcanames != null){
								intrees.get(i).reRoot(intrees.get(i).getMRCA(mrcanames));
							}else{
								intrees.get(i).unRoot(intrees.get(i).getRoot());
							}
							outw.write(intrees.get(i).getRoot().getNewick(true)+";\n");
						}
						outw.close();
					}
				}
			}catch(IOException ioe){};
		}
	}
	
	private void prune(){
		if(mrcanames == null){
			System.out.println("you have to enter tip names when performing a pruning analysis (-names)");
			System.exit(0);
		}
		ArrayList<jade.tree.Tree> intrees = new ArrayList<jade.tree.Tree>();
		ArrayList<jade.tree.Tree> alltrees = new ArrayList<jade.tree.Tree>();
		boolean testnex = false;
		for(int i=0;i<infiles.size();i++){
			testnex = testForNexus(infiles.get(i));
			if(testnex){
				alltrees = getNewickFromNexus(infiles.get(i));
				for(int j=0;j<alltrees.size();j++){
					intrees.add(alltrees.get(j));
				}
			}else{
				try{
					BufferedReader br = new BufferedReader(new FileReader(infiles.get(i)));
					String str = "";
					jade.tree.TreeReader tr = new jade.tree.TreeReader();
					while((str = br.readLine())!=null){
						tr.setTree(str);
						intrees.add(tr.readTree());
					}
					br.close();
				}catch(IOException ioe){};
			}
		}
		if(outfile == null){
			phyutility.pruner.Pruner pr = new phyutility.pruner.Pruner(intrees, mrcanames);
			pr.go();
			for(int i=0;i<intrees.size();i++){
				System.out.println(intrees.get(i).getRoot().getNewick(true)+";");
			}
			log("finished pruning\n");
		}else{
			try{
				//nexus out
				if((testnex == true && out_oth != true)||
						(testnex == false && out_oth == true)){
					phyutility.pruner.Pruner pr = new phyutility.pruner.Pruner(intrees, mrcanames);
					intrees = pr.go();
					ArrayList<String > nwa = new ArrayList<String>();
					for(int i=0;i<intrees.size();i++){
						nwa.add(intrees.get(i).getRoot().getNewick(true)+";");
					}
					Utils.newickToNexus(outfile, nwa);
				}else{
					FileWriter outw = new FileWriter(outfile);
					phyutility.pruner.Pruner pr = new phyutility.pruner.Pruner(intrees, mrcanames);
					intrees = pr.go();
					for(int i=0;i<intrees.size();i++){
						outw.write(intrees.get(i).getRoot().getNewick(true)+";\n");
					}
					outw.close();
				}
			}catch(IOException ioe){};
		}
	}
	
	private ArrayList<jade.tree.Tree> getNewickFromNexus(String filename){
		log("reading in nexus trees\n");
		return phyutility.jebl2jade.NexusToJade.getJadeFromJeblNexus(filename);
	}

	private WwdEmbedded getNewickFromNexusDerby(String filename){
		log("reading in nexus trees to derby\n");
		return phyutility.jebl2jade.NexusToJade.getJadeFromJeblNexusDerby(filename);
	}

	private jade.tree.Tree getJadeTreeFromJeblTree(jebl.evolution.trees.Tree intr){
		jade.tree.Tree rettr = null;
		String st = jebl.evolution.trees.Utils.toNewick((jebl.evolution.trees.RootedTree)intr);
		TreeReader tr = new TreeReader();
		BufferedReader br;
		br = new BufferedReader (new StringReader(st));
		String str = "";
		try {
			while((str = br.readLine())!=null){
				tr.setTree(str+";");
				rettr = tr.readTree();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rettr;
	}

	private ArrayList<jade.tree.Tree> simpleReadJADETrees(){
		ArrayList<jade.tree.Tree> intrees = new ArrayList<jade.tree.Tree>();
		ArrayList<jade.tree.Tree> alltrees = new ArrayList<jade.tree.Tree>();
		boolean testnex = false;
		for(int i=0;i<infiles.size();i++){
			testnex = testForNexus(infiles.get(i));
			if(testnex){
				if(derb == true){
					db = new WwdEmbedded("readtree");
					db.connectToDB();
					System.out.println("table made = "+db.makeTable(true));
					WwdEmbedded dbtemp = getNewickFromNexusDerby(infiles.get(i));
					for(int j=0;j<dbtemp.getTableTreeSize();j++){
						db.addTree(dbtemp.getTree(j)+";");
					}
				}else{
					alltrees = getNewickFromNexus(infiles.get(i));
					for(int j=0;j<alltrees.size();j++){
						intrees.add(alltrees.get(j));
					}
				}
			}else{
				try{
					BufferedReader br = new BufferedReader(new FileReader(infiles.get(i)));
					String str = "";
					jade.tree.TreeReader tr = new jade.tree.TreeReader();
					if(derb == true){
						db = new WwdEmbedded("readtree");
						db.connectToDB();
						System.out.println("table made = "+db.makeTable(true));
					}
					while((str = br.readLine())!=null){
						tr.setTree(str);
						if(derb == true){
							if(str.length()>1)
								db.addTree(tr.readTree().getRoot().getNewick(true)+";");
						}
						else
							intrees.add(tr.readTree());
					}
					if(derb ==true){
						System.out.println("read "+db.getTableTreeSize());
					}
					br.close();
				}catch(IOException ioe){}
			}
		}
		return intrees;
	}
	
	/*
	 * sequence tools
	 */

	/*
	 * takes fasta or nexus input (aligned)
	 * will output fasta or nexus (aligned)
	 * assumes different files are different genes
	 */
	private void concat(){
		Concat cc = new Concat(infiles,"test");
		if(outfile != null){
			if(out_oth == true){
				cc.printtofileFASTA(outfile);
			}else{//nexus
				cc.printtofileNEXUS(outfile);
			}
		}else{
			cc.printtoscreenNEXUS();
		}
	}
	
	/*
	 * utils
	 */
	private boolean testForNexus(String filename){
		boolean ret = false;
		String str = "";
		try{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			str = br.readLine();
			if(str.toUpperCase().trim().compareTo("#NEXUS")==0)
				ret = true;
			br.close();
		}catch(IOException ioe){}
		return ret;
	}
	
	private boolean deleteDatabaseFolder(File dir){
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDatabaseFolder(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	private ArrayList <jade.tree.Node> getExtNodesFromIntNode(jade.tree.Node node){
		ArrayList<jade.tree.Node> retnd = new ArrayList<jade.tree.Node>();
		if(node.isExternal()){
			retnd.add(node);
			return retnd;
		}
		
		Stack<jade.tree.Node> tempnd = new Stack<jade.tree.Node>();
		tempnd.add(node);
		jade.tree.Node curn = null;
		while(tempnd.isEmpty()==false){
			curn = tempnd.pop();
			if(curn.isInternal()){
				for(int i=0;i<curn.getChildCount();i++){
					tempnd.push(curn.getChild(i));
				}
			}else{
				retnd.add(curn);
			}
		}
		
		return retnd;
	}
	
	private ArrayList<jade.tree.Node> getDifferenceBetweenArray(ArrayList<jade.tree.Node> arr1,
		ArrayList<jade.tree.Node> arr2){
		
		ArrayList<jade.tree.Node> retnd = new ArrayList<jade.tree.Node>();
		for(int i=0;i<arr1.size();i++){
			boolean keep = true;
			for(int j=0;j<arr2.size();j++){
				if(arr1.get(i)==arr2.get(j)){
					keep = false;
					break;
				}
			}
			if(keep == true){
				retnd.add(arr1.get(i));
			}
		}
		for(int i=0;i<arr2.size();i++){
			boolean keep = true;
			for(int j=0;j<arr1.size();j++){
				if(arr2.get(i)==arr1.get(j)){
					keep = false;
					break;
				}
			}
			if(keep == true && retnd.contains(arr2.get(i))==false){
				retnd.add(arr2.get(i));
			}
		}
		return retnd;
	}
	
	private ArrayList<jade.tree.Node> getDifferenceBetweenArray2(ArrayList<jade.tree.Node> arr1,
		ArrayList<jade.tree.Node> arr2){
		ArrayList<jade.tree.Node> retnd1 = new ArrayList<jade.tree.Node>();
		retnd1.addAll(arr1);
		retnd1.removeAll(arr2);
		ArrayList<jade.tree.Node> retnd2 = new ArrayList<jade.tree.Node>();
		retnd2.addAll(arr2);
		retnd2.removeAll(arr1);
		ArrayList<jade.tree.Node> retnd = new ArrayList<jade.tree.Node>();
		retnd.addAll(retnd1);
		retnd.addAll(retnd2);
		return retnd;
	}
	
	private ArrayList<jade.tree.Node> combineLists(ArrayList<jade.tree.Node> arr1,
		ArrayList<jade.tree.Node> arr2){
		ArrayList<jade.tree.Node> retar = new ArrayList<jade.tree.Node>();
		retar.addAll(arr1);
		retar.addAll(arr2);
		return retar;
	}
	
	private boolean allNamesSame(ArrayList<jade.tree.Node>nodes){
		boolean same = true;
		String nodeName = nodes.get(0).getName().split("@")[0];
		for(int i=0;i<nodes.size();i++){
			if(nodeName.compareTo(nodes.get(i).getName().split("@")[0])!=0){
				same = false;
			}
		}
		return same;
	}
	
	private ArrayList<jade.tree.Node> leaveOne(ArrayList<jade.tree.Node> arr){
		ArrayList<String> names = new ArrayList<String> ();
		for(int i=0;i<arr.size();i++){
			if (containsName(arr.get(i).getName().split("@")[0],names)==false){
				names.add(arr.get(i).getName().split("@")[0]);
			}
		}
		for(int i=0;i<names.size();i++){
			for(int j=0;j<arr.size();j++){
				if(arr.get(j).getName().split("@")[0].compareTo(names.get(i))==0){
					System.out.println(arr.get(j).getName());
					arr.remove(j);
					break;
				}
			}
		}
		return arr;
	}
	
	private boolean containsName(String name, ArrayList<String> arr){
		boolean cont = false;
		for(int i=0;i<arr.size();i++){
			if(arr.get(i).compareTo(name)==0){
				cont = true;
				break;
			}
		}
		return cont;
	}
	
	private boolean containsAll(ArrayList<jade.tree.Node>nodes,ArrayList<jade.tree.Node>source){
		boolean same = true;
		for(int i=0;i<nodes.size();i++){
			if(source.contains(nodes.get(i))==false){
				same = false;
				return false;
			}
		}
		return same;
	}
	
	private boolean containsOnly(ArrayList<jade.tree.Node>nodes,ArrayList<String>source){
		if (nodes.size() != source.size()){
			return false;
		}
		int num = 0;
		for(int i=0;i<source.size();i++){
			for(int j=0;j<nodes.size();j++){
				if(nodes.get(j).getName().split("@")[0].compareTo(source.get(i))==0){
					num++;
					break;
				}
			}
		}
		if(num == nodes.size() && num == source.size())
			return true;
		else
			return false;
	}
	
	private ArrayList<String> nameDiffs(ArrayList<jade.tree.Node>nodes){
		ArrayList<String> diffs = new ArrayList<String>();
		for(int i=0;i<nodes.size();i++){
			boolean test = false;
			for(int j=0;j<diffs.size();j++){
				if(nodes.get(i).getName().split("@")[0].compareTo(
					diffs.get(j))==0){
					test = true;
				}
			}
			if(test == false){
				diffs.add(nodes.get(i).getName().split("@")[0]);
			}
		}
		return diffs;
	}
	
	private int numOfDiffs(ArrayList<jade.tree.Node>nodes){
		ArrayList<String> diffs = new ArrayList<String>();
		for(int i=0;i<nodes.size();i++){
			boolean test = false;
			for(int j=0;j<diffs.size();j++){
				if(nodes.get(i).getName().split("@")[0].compareTo(
					diffs.get(j))==0){
					test = true;
				}
			}
			if(test == false){
				diffs.add(nodes.get(i).getName().split("@")[0]);
			}
		}
		return diffs.size();
	}
	
	private boolean allUnique(ArrayList<jade.tree.Node>nodes){
		boolean ret = true;
		ArrayList<String> diffs = this.nameDiffs(nodes);
		return this.containsOnly(nodes, diffs);
	}
	
	private void startLogging(){
		try {
			if(logfile == null){
				/*java.util.Date dt = new java.util.Date();
				java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd.MM.yy.hh.mm");
				String datenewformat = formatter.format(dt);
				logfile = datenewformat+".txt";*/
				logfile = "phyutility.log";
			}
			fw = new FileWriter(logfile,true);
			java.util.Date dt = new java.util.Date();
			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yy hh:mm");
			String datenewformat = formatter.format(dt);
			log("starting log: "+datenewformat+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void log(String instring){
		try {
			fw.write(instring);
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopLogging(){
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void printUsage(){
		System.out.println("Phyutility (fyoo-til-i-te) v.2.2.1");
		System.out.println("Stephen A. Smith http://www.blackrim.org stephen.smith@yale.edu");
		System.out.println("help on a specific command use option -h <command>");
		System.out.println("commands:");
		System.out.println("trees:");
		System.out.println("	consensus");
		System.out.println("	convert");
		System.out.println("	leafstab");
		System.out.println("	linmove");		
		System.out.println("	prune");
		System.out.println("	reroot");
		System.out.println("	thin");	
		System.out.println("	treesupp");
		System.out.println("seqs:");
		System.out.println("	clean");		
		System.out.println("	concat");
		System.out.println("	ncbiget");
		System.out.println("	ncbisearch");		
		System.out.println("	parse");
		System.out.println("see documentation for more information");
	}
	
}
