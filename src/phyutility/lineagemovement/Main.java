package phyutility.lineagemovement;
import java.io.*;
import java.util.*;
import jade.tree.*;

public class Main {
	private String filename;//out_use
	private String tipname;
	private ArrayList<String> tipnames;
	private String mrcf;//0.5g
	private ArrayList<Tree> trees = new ArrayList<Tree>();
	private Tree mrct;
	private HashMap<Node, Double> values = new HashMap<Node, Double>();
	private boolean prune = false; //bad if node of interest is polytomy  
	
	public Main(String filename, String mrc, String tipname, String prune){
		this.filename = filename;
		this.tipname = tipname;
		this.mrcf = mrc;
		this.prune = true;
	}
	
	public Main(String filename, String mrc, String tipname){
		this.filename = filename;
		this.tipname = tipname;
		this.mrcf = mrc;
		this.prune = false;
	}
	
	//already read trees
	public Main(ArrayList<Tree> trees, Tree mrc, ArrayList<String> tipnames){
		this.trees = trees;
		this.tipnames = tipnames;
		this.mrct = mrc;
		this.prune = false;
	}
	
	
	public void readTrees(){
		TreeReader tr = new TreeReader();
		BufferedReader br;
		try{
			br = new BufferedReader (new FileReader(filename));
			String str = "";
			while((str = br.readLine())!=null){
				tr.setTree(str);
				trees.add(tr.readTree());
			}
		}catch (FileNotFoundException e){}catch(IOException ioe){}
	}
	
	public Tree run(){
		//readMRCTREE(); already read tree
		if(prune == true){
			for(int i=0;i<tipnames.size();i++){
				mrct.pruneExternalNode(mrct.getExternalNode(tipnames.get(i)));
			}
		}
		double x = 0;
		for(int i=0;i<trees.size();i++){
			Node parent = trees.get(i).getMRCA(tipnames).getParent();
			for(int j=0;j<parent.getChildCount();j++){
				if(parent.getChild(j)!=trees.get(i).getMRCA(tipnames) ){
					parent = parent.getChild(j);
					break;
				}
			}
			getAllTipNodesFromInternalNode(parent);
			for(int j=0;j<tempNs.size();j++){
				//System.out.println("- "+tempNs.get(j).getName());
			}
			//trees.get(i).pruneExternalNode(trees.get(i).getExternalNode(tipname));
			//parent = parent.getParent();
			getAllTipNodesFromInternalNode(parent);
			ArrayList<String> arr = new ArrayList<String>();
			for(int j=0;j<tempNs.size();j++){
				if(tempNs.get(j)!=trees.get(i).getMRCA(tipnames)){
					arr.add(tempNs.get(j).getName());
					//System.out.println("+ "+tempNs.get(j).getName());
				}
			}
			Node n = mrct.getMRCA(arr);
			if(values.get((Node)n) == null){
				values.put((Node)n, 1.0);
				x++;
			}else{
				values.put((Node)n, values.get((Node)n)+1.0);
				x++;
			}
		}
		Iterator it= values.keySet().iterator();
		while(it.hasNext()){
			Node a = ((Node)it.next());
			a.setName(a.getName()+"_"+(values.get((Node)a)/x));
		}
		//System.out.println(mrct.getRoot().getNewick(false)+";");
		return mrct;
	}
	
	
	public Tree oldrun(){
		//readMRCTREE(); already read tree
		if(prune == true){
			for(int i=0;i<tipnames.size();i++){
				mrct.pruneExternalNode(mrct.getExternalNode(tipnames.get(i)));
			}
		}
		double x = 0;
		for(int i=0;i<trees.size();i++){
			Node parent = trees.get(i).getExternalNode(tipname).getParent();
			for(int j=0;j<parent.getChildCount();j++){
				if(parent.getChild(j)!=trees.get(i).getExternalNode(tipname) ){
					parent = parent.getChild(j);
					break;
				}
			}
			getAllTipNodesFromInternalNode(parent);
			for(int j=0;j<tempNs.size();j++){
				//System.out.println("- "+tempNs.get(j).getName());
			}
			//trees.get(i).pruneExternalNode(trees.get(i).getExternalNode(tipname));
			//parent = parent.getParent();
			getAllTipNodesFromInternalNode(parent);
			ArrayList<String> arr = new ArrayList<String>();
			for(int j=0;j<tempNs.size();j++){
				if(tempNs.get(j)!=trees.get(i).getExternalNode(tipname)){
					arr.add(tempNs.get(j).getName());
					//System.out.println("+ "+tempNs.get(j).getName());
				}
			}
			Node n = mrct.getMRCA(arr);
			if(values.get((Node)n) == null){
				values.put((Node)n, 1.0);
				x++;
			}else{
				values.put((Node)n, values.get((Node)n)+1.0);
				x++;
			}
		}
		Iterator it= values.keySet().iterator();
		while(it.hasNext()){
			Node a = ((Node)it.next());
			a.setName(a.getName()+"_"+(values.get((Node)a)/x));
		}
		//System.out.println(mrct.getRoot().getNewick(false)+";");
		return mrct;
	}
	
	ArrayList<Node> tempNs;
	private void getAllTipNodesFromInternalNode(Node intree){
		tempNs  = new ArrayList<Node>();
		poGATNFIN(intree);
	}
	
	private void poGATNFIN(Node innode){
		for(int i=0;i<innode.getChildCount();i++){
			poGATNFIN(innode.getChild(i));
		}
		if(innode.isExternal()==true){
			tempNs.add(innode);
		}
	}
	
	private void readMRCTREE(){
		TreeReader tr = new TreeReader();
		BufferedReader br;
		try{
			br = new BufferedReader (new FileReader(mrcf));
			String str = "";
			while((str = br.readLine())!=null){
				tr.setTree(str);
				mrct = tr.readTree();
			}
		}catch (FileNotFoundException e){}catch(IOException ioe){}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length <3){
			System.out.println("trees contree taxon");
			System.out.println("or");
			System.out.println("trees contree taxon prune");
		}else if(args.length==3){
			Main m = new Main(args[0], args[1], args[2]);
			m.readTrees();
			m.run();
		}else if(args.length == 4){
			Main m = new Main(args[0], args[1], args[2], "prune");
			m.readTrees();
			m.run();
		}else{
			System.out.println("trees contree taxon");
			System.out.println("or");
			System.out.println("trees contree taxon prune");
		}
	}

}
