package phyutility.mainrunner;

import jade.tree.TreeReader;

import java.util.*;
import java.io.*;

import jebl.evolution.io.ImportException;
import phyutility.concat.Concat;
import phyutility.drb.WwdEmbedded;

public class Main {
	private boolean treesupp = false; //-ts
	private boolean linmovement = false; //-lm requires lmt
	private boolean prune = false; //use -names for which to prune
	private String tree = null; //-tree
	private boolean leafstab = false; //-ls
	private boolean convert = false; //-vert for newick to nex or nex to nex no trans
	private boolean consensus = false; //-con requires threshold
	private boolean out_oth = false; //-othout
	private double threshold = 0.0; //-t
	private boolean reroot = false; //-rr
	private boolean thinner = false;//-tt # every
	private double thin = 10;
	private boolean ltt = false;
	private boolean derb = false;
	//sequence functions
	private boolean concat = false;
	private boolean clean = false; 
	private double cleannum = 0.5;
	private boolean parse = false;
	private int parsenum = 1;
	private boolean ncbisearch = false;
	private String sterm = "";
	private int sdb = 1;//nuc = 1, prot = 2, genome = 3, tax = 4 
	private boolean ncbiget = false;
	private String outfor = "31";
	private String sep = "_";
	private int lengthlim;
	private boolean blast = false;//not implemented yet

	//db
	private WwdEmbedded db;

	
	private ArrayList<String> mrcanames; //-names
	private String logfile = null; //-log
	private String outfile = null; //-out
	private ArrayList<String> infiles; //-in
	private FileWriter fw;

	public Main(String [] args){
		if(args.length == 0){
			printUsage();
		}else if(args.length == 1 ){
			if(args[0].toUpperCase().compareTo("-I")== 0){
				goInteractive();
				runArgs();
			}
			else{
				processArgs(args);
				runArgs();
			}
		}else{
			processArgs(args);
			runArgs();
		}
	}

	private void processArgs(String [] args){
		/*
		 * catch the secret functions
		 */
		if(args[0].toLowerCase().compareTo("-sec")==0){
			TestFuncs tf = new TestFuncs(args);
			System.exit(0);
		}
		/*
		 * end catch the secrets
		 */ 
		//get help
		for(int i=0;i<args.length;i++){
			if(args[i].toLowerCase().compareTo("-h")==0){
				i++;
				if(args.length == i){
					System.err.println("you have to enter a command name after -h");
					printUsage();
					System.exit(0);
				}
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a command name after -h");
					printUsage();
					System.exit(0);
				}else{
					printHelp( args[i]);
				}
				System.exit(0);
			}
		}
		//get log
		for(int i=0;i<args.length;i++){
			if(args[i].toLowerCase().compareTo("-log")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a log filename after -log");
					System.exit(0);
				}else{
					logfile = args[i];
				}
			}
		}
		startLogging();
		for(int i=0;i<args.length;i++){
			if(args[i].toLowerCase().compareTo("-lm")==0){
				linmovement = true;
				log("lineage movement\n");
			}else if(args[i].toLowerCase().compareTo("-derb")==0){
				derb = true;
				log("trying to use derby database for trees\n");
			}else if(args[i].toLowerCase().compareTo("-ts")==0){
				treesupp = true;
				log("tree support\n");
			}else if(args[i].toLowerCase().compareTo("-ls")==0){
				leafstab = true;
				log("leaf stability\n");
			}else if(args[i].toLowerCase().compareTo("-rr")==0){
				reroot = true;
				log("reroot\n");
			}else if(args[i].toLowerCase().compareTo("-vert")==0){
				convert = true;
				log("tree convert\n");
			}else if(args[i].toLowerCase().compareTo("-con")==0){
				consensus = true;
				log("consensus\n");
			}else if(args[i].toLowerCase().compareTo("-lmt")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a consensus filename after -lmt");
					System.exit(0);
				}else{
					tree = args[i];
					log("tree filename "+args[i]+"\n");
				}
			}else if(args[i].toLowerCase().compareTo("-tree")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a tree filename after -tree");
					System.exit(0);
				}else{
					tree = args[i];
					log("tree filename "+args[i]+"\n");
				}
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
			}else if(args[i].toLowerCase().compareTo("-tt")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a thinning number after -tt");
					System.exit(0);
				}else{
					thin = Double.valueOf(args[i]);
					thinner = true;
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
			}else if(args[i].toLowerCase().compareTo("-es")==0){
				ncbisearch = true;
				log("ncbisearch\n");
			}else if(args[i].toLowerCase().compareTo("-term")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a search term after -term");
					System.exit(0);
				}else{
					sterm = args[i];
					log("term: "+sterm+"\n");
				}
			}else if(args[i].toLowerCase().compareTo("-db")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a database number after -db");
					System.exit(0);
				}else{
					sdb = Integer.valueOf(args[i]);
					log("database: "+sdb+"\n");
				}
			}else if(args[i].toLowerCase().compareTo("-ef")==0){
				ncbiget = true;
				log("ncbiget\n");
			}else if(args[i].toLowerCase().compareTo("-ll")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a length limit after -ll");
					System.exit(0);
				}else{
					this.lengthlim = Integer.valueOf(args[i]);
					log("length lim: "+this.lengthlim+"\n");
				}
			}else if(args[i].toLowerCase().compareTo("-outfor")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter numbers after -outfor");
					System.exit(0);
				}else{
					outfor = args[i];
					log("outfor: "+outfor+"\n");
				}
			}else if(args[i].toLowerCase().compareTo("-sep")==0){
				i++;
				if(args[i].charAt(0)=='-'){
					System.err.println("you have to enter a seperator after -sep");
					System.exit(0);
				}else{
					sep = args[i];
					log("sep: "+sep+"\n");
				}
			}
			//other
			else if(args[i].toLowerCase().compareTo("-othout")==0){
				out_oth = true;
			}else if(args[i].toLowerCase().compareTo("-out")==0){
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

	private void goInteractive(){

	}

	private void runArgs(){
		int x = 0;
		if(linmovement == true){
			x++;
		}if(treesupp == true){
			x++;
		}if(leafstab == true){
			x++;
		}if(convert == true){
			x++;
		}if(consensus == true){
			x++;
		}if(reroot == true){
			x++;
		}if(thinner == true){
			x++;
		}if(prune == true){
			x++;
		}
		//sequence
		if(concat == true){
			x++;
		}if(parse == true){
			x++;
		}if(clean == true){
			x++;
		}if(ncbisearch == true){
			x++;
		}if(ncbiget == true){
			x++;
		}if(blast == true){
			x++;
		}
		if(x == 0){
			System.out.println("you have to enter some sort of analysis");
			printUsage();
			System.exit(0);
		}if(x > 1){
			System.out.println("you can only do one thing at a time");
			printUsage();
			System.exit(0);
		}
		if(infiles == null && (ncbisearch == false && ncbiget == false)){
			System.out.println("you have to enter some infiles (-in)");
			printUsage();
			System.exit(0);
		}
		if(linmovement == true){
			lineageMove();
			stopLogging();
			System.exit(0);
		}if(treesupp == true){
			treeSupp();
			stopLogging();
			System.exit(0);
		}if(leafstab == true){
			leafStab();
			System.exit(0);
		}if(convert == true){
			convert();
			System.exit(0);
		}if(consensus == true){
			consensus();
			System.exit(0);
		}if(reroot == true){
			reroot();
			System.exit(0);
		}if(thinner == true){
			thin();
			System.exit(0);
		}if(prune == true){
			prune();
			System.exit(0);
		}
		//sequence
		if(concat == true){
			concat();
			System.exit(0);
		}if(parse == true){
			parse();
			System.exit(0);
		}if(clean == true){
			clean();
			System.exit(0);
		}if(ncbisearch == true){
			ncbisearch();
			System.exit(0);
		}if(ncbiget == true){
			ncbiget();
			System.exit(0);
		}if(blast == true){
			blast();
			System.exit(0);
		}
	}

	/*
	 * tree tools
	 */

	private void lineageMove(){
		if(tree == null){
			System.out.println("you have to enter a consensus tree when performing a lineage movement analysis (-tree)");
			System.exit(0);
		}
		if(mrcanames == null){
			System.out.println("you have to enter MRCA or tip names when performing a lineage movement analysis (-names)");
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
		log("reading in consensus tree\n");
		boolean nex = false;
		jade.tree.Tree contr = null;
		String str = "";
		try{
			BufferedReader br = new BufferedReader(new FileReader(tree));
			str = br.readLine();
			if(str.toUpperCase().trim().compareTo("#NEXUS")==0)
				nex = true;
			br.close();
		}catch(IOException ioe){}
		if(nex == true){
			contr = getNewickFromNexus(tree).get(0);
		}else{
			jade.tree.TreeReader tr = new jade.tree.TreeReader();
			tr.setTree(str);
			contr = tr.readTree();
		}
		phyutility.lineagemovement.Main lm = new phyutility.lineagemovement.Main(intrees,contr,mrcanames);
		if(outfile == null){
			System.out.println(lm.run().getRoot().getNewick(true));
		}else{
			try{
				//nexus out
				if((testnex == true && nex == true && out_oth != true)||
						(testnex == false || nex == false && out_oth == true)){
					String nw = lm.run().getRoot().getNewick(true)+";";
					ArrayList<String > nwa = new ArrayList<String>();
					nwa.add(nw);
					Utils.newickToNexus(outfile, nwa);
				}else{
					FileWriter outw = new FileWriter(outfile);
					outw.write(lm.run().getRoot().getNewick(true)+";");
					outw.close();
				}
			}catch(IOException ioe){};
		}
	}

	private void treeSupp(){
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
		log("reading in tree\n");
		boolean nex = false;
		jade.tree.Tree contr = null;
		String str = "";
		try{
			BufferedReader br = new BufferedReader(new FileReader(tree));
			str = br.readLine();
			if(str.toUpperCase().trim().compareTo("#NEXUS")==0)
				nex = true;
			br.close();
		}catch(IOException ioe){}
		if(nex == true){
			contr = getNewickFromNexus(tree).get(0);
		}else{
			jade.tree.TreeReader tr = new jade.tree.TreeReader();
			tr.setTree(str);
			contr = tr.readTree();
		}
		phyutility.treesupport.Runner ls = new phyutility.treesupport.Runner(intrees, contr);
		if(outfile == null){
			System.out.println(ls.run().getRoot().getNewick(true)+";");
			log("finished tree support\n");
		}else{
			try{
				//nexus out
				if((testnex == true && nex == true && out_oth != true)||
						(testnex == false || nex == false && out_oth == true)){
					String nw = ls.run().getRoot().getNewick(true)+";";
					ArrayList<String > nwa = new ArrayList<String>();
					nwa.add(nw);
					Utils.newickToNexus(outfile, nwa);
				}else{
					FileWriter outw = new FileWriter(outfile);
					outw.write(ls.run().getRoot().getNewick(true)+";");
					outw.close();
				}
			}catch(IOException ioe){};
		}
	}

	private void leafStab(){
		ArrayList<jade.tree.Tree> intrees = new ArrayList<jade.tree.Tree>();
		ArrayList<jade.tree.Tree> alltrees = new ArrayList<jade.tree.Tree>();
		for(int i=0;i<infiles.size();i++){
			if(testForNexus(infiles.get(i))){
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
		phyutility.leafstability.Runner ls = new phyutility.leafstability.Runner(intrees);
		ls.run();
		ls.printResults();
	}

	//nexus to newick
	//nexus to nexus tr with othout
	//newick to nexus 
	//newick to nexus tr with othout
	private void convert(){
		ArrayList<jebl.evolution.trees.Tree> alltrees = new ArrayList<jebl.evolution.trees.Tree>();
		if(infiles.size() > 1){
			System.err.println("Converting trees is done one file at a time, only the first file will be used.");
		}
		if(outfile == null){
			System.err.println("You must enter an outfile (-outfile).");
			System.exit(0);
		}
		if(testForNexus(infiles.get(0))){
			jebl.evolution.io.NexusImporter ni;
			try {
				ni = new jebl.evolution.io.NexusImporter(new FileReader(infiles.get(0)));
				alltrees =  (ArrayList<jebl.evolution.trees.Tree>)ni.importTrees();
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
			if(out_oth == true){
				FileWriter fw;
				try {
					fw = new FileWriter(outfile);
					jebl.evolution.io.NexusExporter ne = new jebl.evolution.io.NexusExporter(fw);
					HashMap <String, String> map = new HashMap<String, String>();
					Set<jebl.evolution.taxa.Taxon> tax = alltrees.get(0).getTaxa();
					Iterator<jebl.evolution.taxa.Taxon> it = tax.iterator();
					int i=1;
					while(it.hasNext()){
						map.put(String.valueOf(i),it.next().getName());
					}
					ne.exportTreesWithTranslation(alltrees,map);
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				try {
					BufferedWriter fw = new BufferedWriter (new FileWriter(outfile));
					jebl.evolution.io.NewickExporter ne = new jebl.evolution.io.NewickExporter(fw);
					ne.exportTrees(alltrees);
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			jebl.evolution.io.NewickImporter ni;
			try {
				ni = new jebl.evolution.io.NewickImporter(new FileReader(infiles.get(0)), true);
				alltrees =  (ArrayList<jebl.evolution.trees.Tree>)ni.importTrees();
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
			if(out_oth == true){
				FileWriter fw;
				try {
					fw = new FileWriter(outfile);
					jebl.evolution.io.NexusExporter ne = new jebl.evolution.io.NexusExporter(fw);
					HashMap <String, String> map = new HashMap<String, String>();
					Set<jebl.evolution.taxa.Taxon> tax = alltrees.get(0).getTaxa();
					Iterator<jebl.evolution.taxa.Taxon> it = tax.iterator();
					int i=1;
					while(it.hasNext()){
						map.put(String.valueOf(i),it.next().getName());
					}
					ne.exportTreesWithTranslation(alltrees,map);
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				try {
					BufferedWriter fw = new BufferedWriter(new FileWriter(outfile));
					jebl.evolution.io.NexusExporter ne = new jebl.evolution.io.NexusExporter(fw);
					ne.exportTrees(alltrees);
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
			System.out.println("rooted");
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

	private void thin(){
		log("thinning trees\n");
		ArrayList<jebl.evolution.trees.Tree> alltrees = new ArrayList<jebl.evolution.trees.Tree>();
		ArrayList<jebl.evolution.trees.Tree> thtrees = new ArrayList<jebl.evolution.trees.Tree>();
		if(infiles.size() > 1){
			System.err.println("Converting trees is done one file at a time, only the first file will be used.");
		}
		if(outfile == null){
			System.err.println("You must enter an outfile (-outfile).");
			System.exit(0);
		}
		log("thinning every "+thin+"th tree\n");
		if(testForNexus(infiles.get(0))){
			jebl.evolution.io.NexusImporter ni;
			try {
				if(derb == true){
					db = new WwdEmbedded("readtree");
					db.connectToDB();
					System.out.println("table made = "+db.makeTable(true));
					WwdEmbedded dbtemp = getNewickFromNexusDerby(infiles.get(0));
					for(int i=0;i<dbtemp.getTableTreeSize();i++){
						if(i%thin == 0){
							db.addTree(dbtemp.getTree(i)+";");
						}
					}
				}else{
					ni = new jebl.evolution.io.NexusImporter(new FileReader(infiles.get(0)));
					alltrees =  (ArrayList<jebl.evolution.trees.Tree>)ni.importTrees();
					for(int i=0;i<alltrees.size();i++){
						if(i%thin == 0){
							thtrees.add(alltrees.get(i));
						}
					}
					alltrees = null;
				}
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
			try {
				if(derb == true){
					Utils.newickToNexusDerby(outfile, db);
				}else{
					BufferedWriter fw = new BufferedWriter (new FileWriter(outfile));
					jebl.evolution.io.NexusExporter ne = new jebl.evolution.io.NexusExporter(fw);
					ne.exportTrees(thtrees);
					fw.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			jebl.evolution.io.NewickImporter ni;
			try {
				if(derb == true){
					try{
						BufferedReader br = new BufferedReader(new FileReader(infiles.get(0)));
						String str = "";
						jade.tree.TreeReader tr = new jade.tree.TreeReader();
						db = new WwdEmbedded("readtree");
						db.connectToDB();
						System.out.println("table made = "+db.makeTable(true));
						int i=0;
						while((str = br.readLine())!=null){
							tr.setTree(str);
							if(str.length()>1 && i%thin == 0)
								db.addTree(tr.readTree().getRoot().getNewick(true)+";");
							i++;
						}
						br.close();
					}catch(IOException ioe){}
				}else{
					ni = new jebl.evolution.io.NewickImporter(new FileReader(infiles.get(0)), true);
					alltrees =  (ArrayList<jebl.evolution.trees.Tree>)ni.importTrees();
					for(int i=0;i<alltrees.size();i++){
						if(i%thin == 0){
							thtrees.add(alltrees.get(i));
						}
					}
					alltrees = null;
				}
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
			try {
				if(derb == true){
					FileWriter fw = new FileWriter(outfile);
					for(int i=0;i<db.getTableTreeSize();i++){
						jade.tree.Tree x = db.getJadeTree(i);
						fw.write(x.getRoot().getNewick(true)+";\n");
					}
					fw.close();
					this.deleteDatabaseFolder(new File("null"));
				}else{
					BufferedWriter fw = new BufferedWriter (new FileWriter(outfile));
					jebl.evolution.io.NewickExporter ne = new jebl.evolution.io.NewickExporter(fw);
					ne.exportTrees(thtrees);
					fw.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	/*
	 * sequence tools
	 */

	/*
	 * takes fasta or nexus input (aligned)
	 * will output fasta or nexus (aligned)
	 * assumes different files are different genes
	 */
	private void concat(){
		Concat cc = new Concat(infiles);
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
	 * takes fasta as input (unaligned)
	 * will output fasta (unaligned)
	 * System.out.println("\tthis assumes that the file is a genbank fasta file\n" +
				"\tthe numbers correspond to these options for the names\n" +
				"\t1) gi number\n" +
				"\t2) gb number\n" +
				"\t3) taxon name\n" +
				"\t4) taxon_name\n" +
				"\t5) T_name\n" +
				"\t6) taxon_name_ginumber\n" +
				"\t7) taxon_name_gbnumber\n" +
                                "\t8) formatting for bioorganizer so type java -jar gbparser.jar 8 file regionname\n"+
				"\n" +
				"NOTE 1: if there are duplicate names, this will add numbers to the end\n" +
				"NOTE 2: some genbank entries are poorly formed and therefore do not get formatted correctly here, so double check!");
	 */
	private void parse(){
		if(parsenum != 666){
			if(outfile != null){
				FileWriter fw;
				try {
					fw = new FileWriter(outfile);
					for(int i=0;i<infiles.size();i++){
						jade.data.GBParser ca = new jade.data.GBParser(infiles.get(i), parsenum);
						ArrayList<jade.data.Sequence> seqs = ca.getSeqs();
						for(int j=0;j<seqs.size();j++){
							fw.write(">"+seqs.get(j).getID()+"\n");
							fw.write(seqs.get(j).getSeq()+"\n\n");
						}
					}
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				for(int i=0;i<infiles.size();i++){
					jade.data.GBParser ca;
					try {
						ca = new jade.data.GBParser(infiles.get(i), parsenum);
						ArrayList<jade.data.Sequence> seqs = ca.getSeqs();
						for(int j=0;j<seqs.size();j++){
							System.out.println(">"+seqs.get(j).getID());
							System.out.println(seqs.get(j).getSeq());
							System.out.println();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}else{

		}
	}

	/*
	 * takes fasta or nexus input (aligned)
	 * one file at a time
	 * will output fasta or nexus (aligned)
	 */
	private void clean(){
		if(infiles.size() > 1){
			System.err.println("Trimming is done one file at a time, only the first file will be used.");
		}
		phyutility.trimsites.TrimSites ts = new phyutility.trimsites.TrimSites(infiles.get(0));
		ts.trimAln(cleannum);
		if(testForNexus(infiles.get(0))){
			if(out_oth == true){
				ts.printFastaOutfile(outfile);
			}else{
				ts.printNexusOutfile(outfile);
			}
		}else{
			if(out_oth == false){
				ts.printFastaOutfile(outfile);
			}else{
				ts.printNexusOutfile(outfile);
			}
		}
	}

	/*
	 * incomplete
	 */

	private void ncbisearch(){
		if(sterm == "" || sterm.length() < 2){
			System.out.println("in order to search ncbi you much enter a search term (-term)");
		}
		phyutility.ncbi.Einfo einfo = new phyutility.ncbi.Einfo();
		if(sdb == 1){
			ArrayList<String> ids = einfo.search("nucleotide", sterm);
			log("Count = "+ids.size()+"\n");
			log("genbankids\n-----------\n");
			for(int i=0;i<ids.size();i++){
				log(ids.get(i)+"\n");
			}
			System.out.println("Count = "+ids.size());
		}else if(sdb == 2){
			ArrayList<String> ids = einfo.search("protein", sterm);
			log("Count = "+ids.size()+"\n");
			log("genbankids\n-----------\n");
			for(int i=0;i<ids.size();i++){
				log(ids.get(i)+"\n");
			}
			System.out.println("Count = "+ids.size());
		}else if(sdb == 3){
			ArrayList<String> ids = einfo.search("genome", sterm);
			log("Count = "+ids.size()+"\n");
			log("genbankids\n-----------\n");
			for(int i=0;i<ids.size();i++){
				log(ids.get(i)+"\n");
			}
			System.out.println("Count = "+ids.size());
		}else if(sdb == 4){
			ArrayList<String> ids = einfo.search("taxonomy", sterm);
			log("Count = "+ids.size()+"\n");
			log("genbankids\n-----------\n");
			for(int i=0;i<ids.size();i++){
				log(ids.get(i)+"\n");
			}
			System.out.println("Count = "+ids.size());
		}else if(sdb == 5){
			ArrayList<String> ids = einfo.search("nuccore", sterm);
			log("Count = "+ids.size()+"\n");
			log("genbankids\n-----------\n");
			for(int i=0;i<ids.size();i++){
				log(ids.get(i)+"\n");
			}
			System.out.println("Count = "+ids.size());
		}

	}

	private void ncbiget(){
		if(sterm == "" || sterm.length() < 2){
			System.out.println("in order to fetch from ncbi you much enter a search term (-term)");
		}
		phyutility.ncbi.Einfo einfo = new phyutility.ncbi.Einfo();
		if(sdb == 1){
			einfo.efetch("nucleotide", sterm, this.lengthlim, outfile, outfor, sep);
		}else if(sdb == 2){
			einfo.efetch("protein", sterm, this.lengthlim, outfile, outfor, sep);
		}else if(sdb == 3){
			einfo.efetch("genome", sterm, this.lengthlim, outfile, outfor, sep);
		}else if(sdb == 4){
			System.out.println("sorry, no taxonomy downloads");
		}
	}

	/*
	 * incomplete
	 * going to require blastcl3 or blast2, can i bundle this?
	 */
	private void blast(){

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
	
	/*
	 * prints a helpful comment with instructions and examples for
	 * all the commands (usage)
	 */
	private void printHelp(String cmd){
		if(cmd.compareTo("consensus") == 0){
			System.out.println("makes consensus trees from single or multiple files");
			System.out.println("options:");
			System.out.println("	-con | designates that you want to make a consensus");
			System.out.println("	-t <number> | threshold for consensus (1.0 = strict, 0.5 = majrule, 0 = allcompat)");
			System.out.println("	-in <file name> ... | input file names");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -con -t 0.5 -in testall.tre -out test.con");
		}else if(cmd.compareTo("convert") == 0){
			System.out.println("converts between tree file types");
			System.out.println("options:");
			System.out.println("	-vert | designates that you want to convert");
			System.out.println("	-in <file name> | input file name");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -vert -in test.tre -out testvert.nex");
		}else if(cmd.compareTo("leafstab") == 0){
			System.out.println("calculates leaf stability for all tips");
			System.out.println("options:");
			System.out.println("	-ls | designates that you want to do leaf stability index calculations");
			System.out.println("	-in <file name> ... | input file names");
			System.out.println("java -jar phyutility.jar -ls -in testall.tre");
		}else if(cmd.compareTo("linmove") == 0){
			System.out.println("conducts lineage movement procedure on tree");
			System.out.println("options:");
			System.out.println("	-lm | designates that you want to run lineage movement analysis");
			System.out.println(" 	-names <tip name> ... | names to check -- multiple form clade");
			System.out.println("	-tree <file name> | consensus file to map movement to");
			System.out.println("	-in <file name> ... | input tree file names");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -lm -in testall.tre -tree test.con -out testlm.tre -names three");
		}else if(cmd.compareTo("prune") == 0){
			System.out.println("prunes tips or clades -- has problems if node to prune is root");
			System.out.println("options:");
			System.out.println("	-pr | designates that you want to prune");
			System.out.println(" 	-names <tip name> ... | names to check -- multiple form clade");
			System.out.println("	-in <file name> ... | input file names");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -pr -in test.tre -out testpr.tre -names one");
		}else if(cmd.compareTo("reroot") == 0){
			System.out.println("reroots one or multiple trees");
			System.out.println("options:");
			System.out.println("	-rr | designates that you want to reroot");
			System.out.println(" 	-names <tip name> ... | names to check -- multiple form clade");
			System.out.println("	-in <file name> ... | input file names");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -rr -in test.tre -out testrr.tre -names one two");
		}else if(cmd.compareTo("thin") == 0){
			System.out.println("thins tree files");
			System.out.println("options:");
			System.out.println("	-tt # | designates that you want to thin followed by the number of thinning (sample every #)");
			System.out.println("	-in <file name> ... | input file names");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -tt 100 -in testall.tre -out testts.tre");
		}else if(cmd.compareTo("treesupp") == 0){
			System.out.println("allows to calculate support for a tree given a set of trees");
			System.out.println("options:");
			System.out.println("	-ts | designates that you want to calculate support for a tree given a set of trees");
			System.out.println("	-tree <file name> | tree to get support for");
			System.out.println("	-in <file name> ... | input file names");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -ts -in testall.tre -tree test.con -out testts.tre");
		}else if(cmd.compareTo("clean") == 0){
			System.out.println("trims sequences based on a threshhold");
			System.out.println("options:");
			System.out.println("	-clean # | designates that you want to trim and the threshold must follow");
			System.out.println("	-in <file name> ... | input file names");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -clean 0.5 -in test.nex -out test50.nex");
		}else if(cmd.compareTo("concat") == 0){
			System.out.println("concatenate alignments together");
			System.out.println("options:");
			System.out.println("	-concat | designates that you want to concatenate");
			System.out.println("	-in <file name> ... | input file names");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -concat -in test.aln test2.aln -out testall.aln");
		}else if(cmd.compareTo("ncbiget") == 0){
			System.out.println("fetch sequences from ncbi");
			System.out.println("options:");
			System.out.println("	-ef | designates that you want to perform a fetch");
			System.out.println("	-term <term> | the search term(s)");
			System.out.println("	-out <file name> | output file name");
			System.out.println(" 	-ll <length> | the length limit to return, meant to eliminate genomic sequences or sequences that are too long in the retrieval (should almost always use, especially use if you get a memory error) (optional,default = 10000)");
			System.out.println(" 	-outfor <format> | a way to customize the fasta line (1 = ginumber, 2 = taxid, 3 = orgname (with spaces replaced with whatever is sep), 4 = defline (with spaces replaced with sep), 5 = seqlength)(optional, default = 31)");
			System.out.println("	-sep <seperator> | seperator for between outfor and between orgname and deine (optional, default= _)");
			System.out.println("	-db # | corresponds to the database you want to search (nucleotide (1), protein (2)) (optional, nucleotide is default)");
			System.out.println(" java -jar phyutility.jar -ef -term lonicera+OR+viburnum+AND+rbcl -ll 3000 -log log.txt -out out.fasta -sep -outfor 13");
		}else if(cmd.compareTo("ncbisearch") == 0){
			System.out.println("found out how many sequences match a query -- probably before ncbifetch -- log file records the GI numbers");
			System.out.println("options:");
			System.out.println("	-es | designates that you want to perform a search");
			System.out.println("	-term <term> | the search term(s)");
			System.out.println("	-db # | corresponds to the database you want to search (right now nucleotide (1), protein (2), genome (3), taxonomy (4)) (optional, default = nucleotide)");
			System.out.println("	-log <file name> | log file name");
			System.out.println("java -jar phyutility.jar -es -term lonicera+OR+viburnum+AND+rbcl -log log.txt");
		}else if(cmd.compareTo("parse") == 0){
			System.out.println("parses fasta files downloaded from genbank");
			System.out.println("options:");
			System.out.println("	-parse # | designates that you want to parse and the option number must follow -- see documentation");
			System.out.println("	-in <file name> ... | input fasta files");
			System.out.println("	-out <file name> | output file name");
			System.out.println("java -jar phyutility.jar -parse 1 -in test.gb -out testgb1.fasta");
		}else{
			System.out.println("don't recognize command: "+cmd);
			printUsage();
			System.exit(0);
		}
	}

	public static void main(String [] args){
		Main m = new Main(args);
	}
}
