package jade.simulate;

import jade.tree.*;

import java.util.*;
import java.io.*;

public class BioGeoTree2 {
	private ArrayList<Node> extantNodes;
	private ArrayList<Node> allNodes;
	private static String BIRTHTIME = "birthtime";
	private static String DEATHTIME = "deathtime";
	private static String SETAREAS = "set_areas";
	private static String CURAREAS = "cur_areas";
	private int failures;
	private int maxfail = 10000;
	private double relativedispersalrate ;
	private Tree finalT;
	private Random ran;
	public ArrayList<Area> areas;
	private double sumrate;
	private double currenttime;
	private int numofchanges;
	private ArrayList<Node> numdisp;
	private ArrayList<Node> numext;
	private ArrayList<Node> numapext;
	private ArrayList<Node> numapdisp;
	private boolean printnumCH = true;
	private Node dispnode ;
	public double apdisp;
	public double apext;
	public double redisp;
	public double reext;

	public double vag;// birth rate in an area
	public double birthrate;// speciation rate
	public double deathrate;// in an area, which translates to
	public double extantstop = 0;
	public double timestop = 0;
	public boolean DEBUG = false;

	/*
	 * added for constrained extinction
	 * the idea is that in addition to the global extinction rate, there is a localized extinction rate for different areas
	 * make sure the length is the right length (size of the areas)
	 */
	public boolean areaspecificextinction = false;
	public void setLocalExtinction(ArrayList<Double> arearates){
		areaspecificextinction = true;
		for(int i=0;i<arearates.size();i++){
			((Area)this.areas.get(i)).setLocalExtinctionRate(arearates.get(i));
		}
	}
	
	/*
	 * added for speciation linked to dispersal, if a species disperses then it speciates
	 */
	public boolean speciationlinked = false;
	public void setDispersalLinkedToSpeciation(boolean set){
		this.speciationlinked = true;
	}
	
	/*
	 * added for constrained dispersal
	 */
	
	/*
	 * used to count the number of changes
	 */
	private int ch;
	
	public BioGeoTree2(){
		failures = 0;
	}
	
	public BioGeoTree2(ArrayList<Area> areas){
		this.areas = areas;
	}

	public Tree makeTree(boolean showDead){
		this.setup_parameters();
		finalT = new Tree();
		Node root = new Node();
		finalT.setRoot(root);
		int [] ar = new int [areas.size()];
		for(int i=0;i<ar.length;i++){ar[i]=0;}
		ar[ran.nextInt(ar.length)] = 1;//for one area
		if(DEBUG){
			for(int i=0;i<ar.length;i++){System.out.print(ar[i]);}
			System.out.println();
		}
		root.assocObject(SETAREAS, (int [])ar.clone());
		root.setName(get_area_string((int [])root.getObject(SETAREAS)));
		root.assocObject(CURAREAS, ar);//to be changed
		root.assocObject(BIRTHTIME, new Double(currenttime));
		extantNodes.add(root);
		//nodeBirth(root);
		while (checkStopConditions()) {
			try{
			double dt = timeToNextSPEvent();
			double at = timeToNextBioGeoEvent();
			if (dt < at) {
				if(DEBUG)
					System.out.println("spevent");
				currenttime += dt;
				speciationEvent();
			}
			// else if speciation event is before biogeo event
			else {
				if(DEBUG)
					System.out.println("biogeoevent");
				currenttime += at;
				biogeoEvent();
			}
			for(int i=0;i<extantNodes.size();i++){
				if(jade.math.Utils.sum((int [])(extantNodes.get(i).getObject(CURAREAS)))<1){
					killNode(extantNodes.get(i));
					//System.out.println("a");
				}
			}
			if (extantNodes.size() < 1) {
				failures++;
				this.setup_parameters();
				finalT = new Tree();
				root = new Node();
				finalT.setRoot(root);
				ar = new int [areas.size()];
				for(int i=0;i<ar.length;i++){ar[i]=0;}
				ar[ran.nextInt(ar.length)] = 1;//for one area
				root.assocObject(SETAREAS, ar.clone());
				root.setName(get_area_string((int [])root.getObject(SETAREAS)));
				root.assocObject(CURAREAS, ar);//to be changed
				root.assocObject(BIRTHTIME, new Double(currenttime));
				extantNodes = new ArrayList<Node>();
				allNodes = new ArrayList<Node>();
				extantNodes.add(root);
				allNodes.add(root);
				//nodeBirth(root);
			}
			}catch(java.lang.IllegalArgumentException iae){
				failures++;
				if(failures > maxfail){
					System.out.println("change parameters");
					System.exit(0);
				}
				this.setup_parameters();
				finalT = new Tree();
				root = new Node();
				finalT.setRoot(root);
				ar = new int [areas.size()];
				for(int i=0;i<ar.length;i++){ar[i]=0;}
				ar[ran.nextInt(ar.length)] = 1;//for one area
				root.assocObject(SETAREAS, ar.clone());
				root.setName(get_area_string((int [])root.getObject(SETAREAS)));
				root.assocObject(CURAREAS, ar);//to be changed
				root.assocObject(BIRTHTIME, new Double(currenttime));
				extantNodes = new ArrayList<Node>();
				allNodes = new ArrayList<Node>();
				extantNodes.add(root);
				allNodes.add(root);
			}
		}
		finalT.processRoot();
		/*
		 * set internal branch lengths
		 */
		for(int i=0;i<finalT.getExternalNodeCount();i++){
			if(finalT.getExternalNode(i).getObject(DEATHTIME)==null){
				killNode(finalT.getExternalNode(i));
			}
			finalT.getExternalNode(i).setName(get_area_string((int [])finalT.getExternalNode(i).getObject(CURAREAS)));
		}double ortotallength = 0.0;
		if(printnumCH == true){
		    double totallength = 0.0;
		    for(int i = 0;i < finalT.getExternalNodeCount(); i++){
			totallength+= finalT.getExternalNode(i).getBL();
		    }for(int i = 0;i < finalT.getInternalNodeCount(); i++){
			totallength+= finalT.getInternalNode(i).getBL();
		    }
		    redisp = (numdisp.size()/totallength);
		    reext = (numext.size())/(totallength);
		    //System.out.print((numdisp.size()/(totallength)) +"\t"+((numext.size()+finalT.getInternalNodeCount())/(totallength)+"\t")
		//	    +"\t");
		    ortotallength = totallength;
		}
		if(showDead == false){
			deleteDeadNodes();
		}
		if(printnumCH == true){
		    double totallength = 0.0;
		    for(int i = 0;i < finalT.getExternalNodeCount(); i++){
			totallength+= finalT.getExternalNode(i).getBL();
			for(int j=0;j<numdisp.size();j++){
			    if(numdisp.get(j) == finalT.getExternalNode(i)){
				numapdisp.add(numdisp.get(j));
			    }
			}
			for(int j=0;j<numext.size();j++){
			    if(numext.get(j) == finalT.getExternalNode(i)){
				numapext.add(numext.get(j));
			    }
			}
		    }for(int i = 0;i < finalT.getInternalNodeCount(); i++){
			totallength+= finalT.getInternalNode(i).getBL();
			for(int j=0;j<numdisp.size();j++){
			    if(numdisp.get(j) == finalT.getInternalNode(i)){
				numapdisp.add(numdisp.get(j));
			    }
			}
			for(int j=0;j<numext.size();j++){
			    if(numext.get(j) == finalT.getInternalNode(i)){
				numapext.add(numext.get(j));
			    }
			}
		    }
		    apdisp = (numapdisp.size()/totallength);
		    apext = ((numapext.size())/totallength);//apext = ((numext.size())/totallength);
		    //System.out.print((numapdisp.size()/ortotallength)+"\t"+(((numext.size()/2)+numapext.size())/totallength)+"\n");
		}
		return finalT;
	}

	private boolean checkStopConditions() {
		boolean keepgoing = true;
		if (extantstop > 0 && extantNodes.size() >= extantstop) {
			currenttime += timeToNextSPEvent();
			keepgoing = false;
		}
		if (timestop > 0.0 && currenttime >= timestop) {
			currenttime = timestop;
			keepgoing = false;
		}
		return keepgoing;
	}

	private void setup_parameters(){
		numofchanges = 0;
		numdisp = new ArrayList<Node>();
		numext = new ArrayList<Node>();
		numapext = new ArrayList<Node>();
		numapdisp= new ArrayList<Node>();
		ran = new Random();
		currenttime = 0.0;
		relativedispersalrate = vag / (vag+deathrate);
		sumrate = vag + deathrate;
		extantNodes = new ArrayList<Node>();
		allNodes = new ArrayList<Node>();
	}

	private void speciationEvent(){
		/*
		 * pick a random lineage
		 */
		Node extant = extantNodes.get(ran.nextInt(extantNodes.size()));

		if (eventIsBirth()) {
			nodeBirth(extant);// real speciation
		}
	}

	private void biogeoEvent() {
		/*
		 * pick a random lineage
		 */
		Node extant = extantNodes.get(ran.nextInt(extantNodes.size()));
		if (eventIsDispersal()) {
			nodeDispersal(extant);
		} else {
			nodeExtinction(extant);
		}
	}

	private boolean eventIsDispersal() {
		return (ran.nextDouble() < relativedispersalrate ? true : false);
	}

	private boolean eventIsBirth() {
		return (ran.nextDouble() < birthrate ? true : false);
	}

	private void nodeBirth(Node node){
		Node left = new Node(node);
		Node right = new Node(node);
		node.addChild(left);
		node.addChild(right);
		left.assocObject(BIRTHTIME, new Double(currenttime));
		right.assocObject(BIRTHTIME, new Double(currenttime));
		killNode(node);
		extantNodes.add(left);
		extantNodes.add(right);
		allNodes.add(left);
		allNodes.add(right);
		node.setName(get_area_string((int [])node.getObject(CURAREAS)));
		//if(jade.math.Utils.sum((int [])node.getObject(CURAREAS))>1){
		//    numapext.add(node);
		//}
		int [][][] iter = jade.math.Utils.iter_splitranges((int [])node.getObject(CURAREAS));
		int x = ran.nextInt(iter.length);
		left.assocObject(SETAREAS, (int [])iter[x][0].clone());
		right.assocObject(SETAREAS, (int [])iter[x][1].clone());
		left.assocObject(CURAREAS, (int [])iter[x][0].clone());//to be changed
		right.assocObject(CURAREAS, (int[])iter[x][1].clone());//to be changed
		left.setName(get_area_string((int [])left.getObject(SETAREAS)));
		right.setName(get_area_string((int [])right.getObject(SETAREAS)));
	}

	/*
	 * node birth into specific area for speciation linked with dispersal
	 */
	private void nodeBirth(Node node, int sparea){
		Node left = new Node(node);
		Node right = new Node(node);
		node.addChild(left);
		node.addChild(right);
		left.assocObject(BIRTHTIME, new Double(currenttime));
		right.assocObject(BIRTHTIME, new Double(currenttime));
		killNode(node);
		extantNodes.add(left);
		extantNodes.add(right);
		allNodes.add(left);
		allNodes.add(right);
		node.setName(get_area_string((int [])node.getObject(CURAREAS)));
		
		int [] x = new int [ this.areas.size()];
		for(int i=0;i<x.length;i++){
			x[i] = 0;
			if(i == sparea)
				x[i] = 1;
		}
		left.assocObject(SETAREAS, ((int [])node.getObject(CURAREAS)).clone());
		right.assocObject(SETAREAS, (int [])x.clone());
		left.assocObject(CURAREAS, ((int [])node.getObject(CURAREAS)).clone());//to be changed
		right.assocObject(CURAREAS, (int[])x.clone());//to be changed
		left.setName(get_area_string((int [])left.getObject(SETAREAS)));
		right.setName(get_area_string((int [])right.getObject(SETAREAS)));
	}
	
	private void nodeExtinction(Node node){
		int [] ar = (int [])node.getObject(CURAREAS);
		int x = 0;
		if(areaspecificextinction == false){
			 x = ran.nextInt(jade.math.Utils.sum(ar));
		}else{
			double tx = ran.nextDouble();
			double start = 0;
			double end = 0;
			for(int i=0;i<areas.size();i++){
				end = start + areas.get(i).getLocalExtinctionRate();
				if(tx > start && tx <= end){
					x = i;
					break;
				}
				start = end;
			}
		}
		int cur = 0;
		for(int i=0;i<ar.length;i++){
			if(ar[i] == 1 && x == cur){
				ar[i] = 0;
				numext.add(node);
				break;
			}
			if(ar[i]==1){
				cur ++;
			}
		}
		if(jade.math.Utils.sum(ar)<1){
			killNode(node);
		}
		node.assocObject(CURAREAS, ar);
	}

	private void nodeDispersal(Node node){
		int [] ar = (int [])node.getObject(CURAREAS);
		/*
		 * new way allows for directionality
		 */
		if(jade.math.Utils.sum(ar)!=areas.size()){
			/*
			 * pick source area
			 */
			int x = ran.nextInt((jade.math.Utils.sum(ar)));
			int cur = 0;
			Area sa = null;
			int sarea = 0;
			for(int i=0;i<ar.length;i++){
				if(ar[i] == 1){
					if(x == cur){
						sa = areas.get(i);
						sarea = i;
					}
					cur++;
				}
			}
			/*
			 * process other areas
			 *//*
			double total = 0 ;
			for(int i=0;i<ar.length;i++){
				if(areas.get(i)!=sa && ar[i] == 0){
					total += areas.get(i).getLocalDispersalRate(sa);
				}
			}
			ArrayList<Double> probs = new ArrayList<Double>();
			for(int i=0;i<ar.length;i++){
				if(areas.get(i)!=sa && ar[i] == 0){
					probs.add(areas.get(i).getLocalDispersalRate(sa)/total);
				}else{
					probs.add(666.0);
				}
			}*/
			/*
			 * attempt dispersal
			 */
			double rt = ran.nextDouble();
			double start = 0;
			for(int i=0;i<ar.length;i++){
				if(areas.get(i)!=sa && ar [i] == 0){
					double end = start + sa.getLocalDispersalRate(areas.get(i),currenttime);
					if(rt > start && rt <= end && sa.getLocalDispersalRate(areas.get(i),currenttime)!= 0){
						if(this.speciationlinked == false){
							ar[i] = 1;
							sa.addSuccDisp(areas.get(i));
							//if(sarea == 0 && i ==2 && currenttime < 10)
							//	System.out.println("error");
						}
						numdisp.add(node);
						if(this.speciationlinked == true){
							nodeBirth(node,i);// real speciation
						}
						break;
					}
					start = end;
				}
			}
			if(this.speciationlinked == false)
				node.assocObject(CURAREAS, ar);
		}
		/*
		 * old way
		 */
		/*if(jade.math.Utils.sum(ar)!=areas.size()){
			int x = ran.nextInt(areas.size()-(jade.math.Utils.sum(ar)));
			int cur = 0;
			for(int i=0;i<ar.length;i++){
				if(ar[i] == 0 && x == cur){
					if(this.speciationlinked == false)
						ar[i] = 1;
					numdisp.add(node);
					if(this.speciationlinked == true){
						nodeBirth(node,i);// real speciation
					}
					break;
				}
				if(ar[i]==0){
					cur ++;
				}
			}
			if(this.speciationlinked == false)
				node.assocObject(CURAREAS, ar);
			
		}*/
	}

	private void killNode(Node node){
		node.assocObject(DEATHTIME, new Double(currenttime));
		double bl = (Double)node.getObject(DEATHTIME) - (Double)node.getObject(BIRTHTIME);
		node.setBL(bl);
		extantNodes.remove(node);
	}

	private double timeToNextSPEvent() {
		// return (-Math.log(fRan.fRan(0.1)) /
		// ((double)(numExtantNodes) * sumrate))*10;
		return (-Math.log(ran.nextDouble()))
		/ ((double) (extantNodes.size()) * birthrate);
	}

	private double timeToNextBioGeoEvent() {
		return (-Math.log(ran.nextDouble()))
		/ ((double) (extantNodes.size()) * sumrate);
	}

	private String get_area_string(int []ar){
		String ret = "";
		for(int i=0;i<ar.length;i++){
			ret = ret + String.valueOf(ar[i]);
		}
		return ret;
	}

	private void deleteDeadNodes(){
		ArrayList<Node> kill = new ArrayList<Node>();
		TreeUtils.setDistanceToTip(finalT);
		TreeUtils.setDistanceFromTip(finalT);
		for(int i=0;i<finalT.getExternalNodeCount();i++){
			//System.out.println(finalT.getExternalNode(i).getDistanceFromTip()+" "+
			//		finalT.getRoot().getDistanceToTip());
			if(finalT.getExternalNode(i).getDistanceFromTip() != 
					finalT.getRoot().getDistanceToTip()
					){
				kill.add(finalT.getExternalNode(i));
			}
		}
		//System.out.println(kill.size());
		for(int i=0;i<kill.size();i++){
			deleteANode(kill.get(i));
		}
		
	}

	private void deleteANode(Node node){
		Node parent = node.getParent();
		if(parent != finalT.getRoot()){
			Node child = null;
			for(int i=0;i<parent.getChildCount();i++){
				if(parent.getChild(i)!= node)
					child = parent.getChild(i);
			}
			Node pparent = parent.getParent();
			parent.removeChild(node);
			parent.removeChild(child);
			pparent.removeChild(parent);
			pparent.addChild(child);
			child.setParent(pparent);
			child.setBL(child.getBL()+parent.getBL());
			finalT.processRoot();
		}else{
			Node child = null;
			for(int i=0;i<parent.getChildCount();i++){
				if(parent.getChild(i)!= node)
					child = parent.getChild(i);
			}
			parent.removeChild(node);
			child.setParent(null);
			finalT.setRoot(child);
			finalT.processRoot();
		}
	}
	
	private void getNumOfNodeChanges(Node node) {
		String nodeR = node.getName();
		for (int i = 0; i < node.getChildCount(); i++) {
			if (node.getChild(i).isInternal()) {
				if (nodeR.compareTo(node.getChild(i).getName()) != 0) {
					ch++;
					// System.out.println("--"+nodeR+"
					// "+node.getChild(i).getString());
				} else {
					// System.out.println("++"+nodeR+"
					// "+node.getChild(i).getString());
				}
			} else {
				for (int j = 0; j < extantNodes.size(); j++) {
					if (( extantNodes.get(j)).getName()
							.compareTo(node.getChild(i).getName()) == 0) {
						if (( extantNodes.get(j)).getName()
								.compareTo(nodeR) != 0) {
							ch++;
							// System.out.println("--"+nodeR+"
							// "+((Lineage)tantlineages.get(j)).getString());
						} else {
							// System.out.println("++"+nodeR+"
							// "+((Lineage)tantlineages.get(j)).getString());
						}
					}
				}
			}
			getNumOfNodeChanges(node.getChild(i));
		}
	}
	
	/*
	 * main
	 */
	public static void main(String [] args){
		ArrayList<jade.simulate.Area> ar = new ArrayList<jade.simulate.Area>();
		jade.simulate.Area ar1 = new jade.simulate.Area(0);// EA
		jade.simulate.Area ar2 = new jade.simulate.Area(1);// WNA
		jade.simulate.Area ar3 = new jade.simulate.Area(2);// ENA
		//jade.simulate.Area ar4 = new jade.simulate.Area(3);// EU
		// 1
		ar1.addConnection(ar2, 1000, 0, 1);
		ar1.addConnection(ar3, 1000, 0, 1);
		//ar1.addConnection(ar4, 1000, 0, 1);
		// 2
		ar2.addConnection(ar1, 1000, 0, 1);
		ar2.addConnection(ar3, 1000, 0, 1);
		//ar2.addConnection(ar4, 1000, 0, 1);
		// 3
		ar3.addConnection(ar2, 1000, 0, 1);
		ar3.addConnection(ar1, 1000, 0, 1);
		//ar3.addConnection(ar4, 1000, 0, 1);
		// 4
		//ar4.addConnection(ar2, 1000, 0, 1);
		//ar4.addConnection(ar3, 1000, 0, 1);
		//ar4.addConnection(ar1, 1000, 0, 1);
		ar.add(ar1);
		ar.add(ar2);
		ar.add(ar3);
		//ar.add(ar4);
		BioGeoTree2 bgt= new BioGeoTree2(ar);
		bgt.birthrate = 0.4;
		bgt.extantstop = 100;
		bgt.vag = 0.02;
		bgt.deathrate = 0.02;
		Tree tree = bgt.makeTree(true);
		for(int j=0;j<tree.getExternalNodeCount();j++){
			System.out.print((j+1)+"\t"+tree.getExternalNode(j).getName()+"\n");
			tree.getExternalNode(j).setName(String.valueOf(j+1));
		}
		System.out.println(tree.getRoot().getNewick(true)+"\n");
	}
}
