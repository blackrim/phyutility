package jade.simulate;

/*
 * right now this should just include the model implemented by Ree et al. 2005
 * 
 * need to fix the adding of areas to take effect at the end (the speciation event)
 */

import jade.tree.*;

import java.util.*;
import java.io.*;

public class BioGeoTree {

	private ArrayList<Node> extantNodes;

	private static String BIRTHTIME = "birthtime";

	private static String DEATHTIME = "deathtime";

	private int failure = 0;

	public double deathrate = 0.01;// in an area, which translates to

	// extinction if they go extinct in an area

	public double vag = 0.01;// birth rate in an area

	private double relativedispersalrate = 0;

	public double birthrate = 0.2;// speciation rate

	private Tree finalT;

	private Random ran;

	private int numareas = 4;// reset when set model

	/*
	 * all lineages
	 */
	private ArrayList<Lineage> lineages;

	/*
	 * extant lineages
	 */
	private ArrayList<Lineage> tantlineages;

	/*
	 * extinct EXTERNAL nodes
	 */
	private ArrayList<Lineage> extinctlineages;

	public double extantstop = 20;

	private double timestop = 0;

	private double sumrate = 1.0;

	private double currenttime = 0.0;

	private int curdead = 0;

	/*
	 * stop the options for simulation
	 */
	/*
	 * this options has to do with the final tree that is printed true will
	 * print the extinct species false will remove the extinct species
	 */
	private boolean showExtinct = false;

	/*
	 * used for the model
	 */
	private ArrayList<Area> areas;

	/*
	 * used for output file
	 */
	private FileWriter fw;

	private FileWriter fwt;

	private FileWriter fwp;

	private FileWriter fwc;

	/*
	 * used to count the number of changes
	 */
	private int ch;

	public BioGeoTree() {
		ran = new Random();
		lineages = new ArrayList<Lineage>();
		tantlineages = new ArrayList<Lineage>();
		extinctlineages = new ArrayList<Lineage>();
		extantNodes = new ArrayList<Node>();
	}

	/*
	 * takes in already set up areas, which should have already set up
	 * connections and extinction functions
	 */
	public void setModel(ArrayList<Area> areas) {
		this.areas = areas;
		numareas = this.areas.size();
	}

	public void start() {
		// setup parameters
		sumrate = vag + deathrate;
		relativedispersalrate = vag / sumrate;
		// start
		finalT = new Tree();
		Node root = new Node();
		finalT.addExternalNode(root);
		Lineage rt = new Lineage(root, numareas);
		rt.addArea(0);// rt.addArea(1);//rt.addArea(2);rt.addArea(3);
		lineages.add(rt);
		tantlineages.add(rt);
		extantNodes.add(root);
		// set the time at the beginning of the node
		root.assocObject(BIRTHTIME, new Double(currenttime));
		// set the name of the root node
		root.setName("root");
		root.setName(((Lineage) tantlineages.get(0)).getString());
		speciationEvent();
		/*
		 * stop conditions includes (these are or) 1) extant numbers match those
		 * desired 2) stop time has been reached
		 */
		while (checkStopConditions()) {
			double dt = timeToNextSPEvent();
			// System.out.println(timeToNextSPEvent());
			double at = timeToNextBioGeoEvent();
			// System.out.println(at);
			// if biogeo event is before the speciation event
			if (dt < at) {
				currenttime += dt;
				speciationEvent();
			}
			// else if speciation event is before biogeo event
			else {
				currenttime += at;
				biogeoEvent();
			}
			if (extantNodes.size() < 1) {
				failure++;
				// start
				curdead = 0;
				currenttime = 0.0;
				lineages = new ArrayList<Lineage>();
				tantlineages = new ArrayList<Lineage>();
				extantNodes = new ArrayList<Node>();
				finalT = new Tree();
				root = new Node();
				finalT.addExternalNode(root);
				rt = new Lineage(root, numareas);
				rt.addArea(0);
				lineages.add(rt);
				tantlineages.add(rt);
				extantNodes.add(root);
				// set the time at the beginning of the node
				root.assocObject(BIRTHTIME, new Double(currenttime));
				// set the name of the root node
				root.setName("root");
			}
			if (failure >= 1000) {
				System.out
				.println("should probably change the parameters because this has looped 1000 times without a successful time");
				System.exit(0);
			}
		}
		/*
		 * just post simulation wrap-up
		 */
		int i, n = extantNodes.size();
		for (i = 0; i < n; i++) {
			Node x = extantNodes.get(i);
			x.assocObject(DEATHTIME, new Double(currenttime));
			x.setBL((Double) x.getObject(DEATHTIME)
					- (Double) x.getObject(BIRTHTIME));
		}
		finalT.setRoot(root);
		finalT.processRoot();
		for (i = 0; i < finalT.getExternalNodeCount(); i++) {
			finalT.getExternalNode(i).setName(String.valueOf(i + 1));
		}
		/*
		 * print the stuff at the end
		 */
		TreePrinter tp = new TreePrinter();
		System.out.println(tp.printNH(finalT) + ";");

		/*
		 * write the output files for area
		 */
		try {
			fw = new FileWriter(
			"/home/smitty/programming/netbeans/NuAReA/test/test.aln");
			fw.append(tantlineages.size() + " " + numareas + "\n");
			fwt = new FileWriter(
			"/home/smitty/programming/netbeans/NuAReA/test/test.tre");// added
			// true
			// to
			// append
		} catch (IOException ioe) {
		}
		for (i = 0; i < tantlineages.size(); i++) {
			System.out.println(((Lineage) tantlineages.get(i)).getNode()
					.getName()
					+ " " + ((Lineage) tantlineages.get(i)).getString());
			try {
				fw.append(((Lineage) tantlineages.get(i)).getNode().getName() + " " + ((Lineage) tantlineages.get(i)).getString() + "\n");
			} catch (IOException ioe) {
			}
		}
		System.out.println("-----");
		// for(i=0;i<lineages.size();i++){
		// System.out.println(((Lineage)lineages.get(i)).getNode().getString()+"
		// "+
		// ((Lineage)lineages.get(i)).getString());
		// }
		if (showExtinct == false) {
			for (i = 0; i < extinctlineages.size(); i++) {
				System.out.println("dead "
						+ ((Lineage) extinctlineages.get(i)).getNode()
						.getName());
			}
			for (i = 0; i < extinctlineages.size(); i++) {
				deathLineageTotal(((Lineage) extinctlineages.get(i)).getNode());
				finalT.processRoot();
			}

			// pts.report(finalT, 100);
			finalT.getRoot().setName(((Lineage) lineages.get(0)).getString());
			/*
			 * print out the number of nodes that change / try{ //fwp = new
			 * FileWriter("/home/smitty/programming/working_copy/bgsim/stuff/test5.run",true);
			 * fwc = new
			 * FileWriter("/home/smitty/programming/working_copy/bgsim/stuff/changes",true);
			 * }catch(IOException ioe){}; ch = 0;
			 * getNumOfNodeChanges(finalT.getRoot()); System.out.println("number
			 * of changed nodes "+ch); try{ //fwp.append("ch =
			 * "+((double)ch/(finalT.getExternalNodeCount()+finalT.getInternalNodeCount()))+"\n");
			 * fwc.append(((double)ch/(finalT.getExternalNodeCount()+finalT.getInternalNodeCount()))+"\n");
			 * //fwp.flush(); //fwp.close(); fwc.flush(); fwc.close();
			 * }catch(IOException ioe){} ///* end nodes that change
			 */
			// System.out.println(pt.printNH()+":"+((Lineage)lineages.get(0)).getString()+";");
			System.out.println(tp.printNH(finalT)
					+ ((Lineage) lineages.get(0)).getString() + ";");
			try {
				fwt.append(tp.printNH(finalT)
						+ ((Lineage) lineages.get(0)).getString() + ";" + "\n");
			} catch (IOException ioe) {
			}
		} else {// showextinct == false
			System.out.println(tp.printNH(finalT) + ";");
			try {
				fwt.append(tp.printNH(finalT) + ";" + "\n");
			} catch (IOException ioe) {
			}
		}
		/*
		 * close the file for writing
		 */
		try {
			fw.flush();
			fwt.flush();
			fw.close();
			fwt.close();
		} catch (IOException ioe) {
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
				for (int j = 0; j < tantlineages.size(); j++) {
					if (((Lineage) tantlineages.get(j)).getNode().getName()
							.compareTo(node.getChild(i).getName()) == 0) {
						if (((Lineage) tantlineages.get(j)).getString()
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
	 * these stop conditions are
	 */
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

	private boolean eventIsBirth() {
		return (ran.nextDouble() < birthrate ? true : false);
	}

	private void speciationEvent() {
		/*
		 * pick a random lineage
		 */
		int extant = ran.nextInt(extantNodes.size());

		if (eventIsBirth()) {
			lineageBirth(extant, 2);// real speciation
		}
	}

	private boolean eventIsDispersal() {
		return (ran.nextDouble() < relativedispersalrate ? true : false);
	}

	private void biogeoEvent() {
		/*
		 * pick a random lineage
		 */
		int extant = ran.nextInt(extantNodes.size());
		if (eventIsDispersal()) {
			lineageDispersal(extant);
		} else {
			localExtinction(extant);
		}
	}

	/*
	 * takes into account only available connections
	 */
	private void lineageDispersal(int index) {
		if (((Lineage) tantlineages.get(index)).getCurNumAreas() < numareas) {
			// get current area//could add population size here
			int ar = ((Lineage) tantlineages.get(index)).getRandomArea();
			Area a = areas.get(ar);
			// test for a relevant to area
			Connection con = a.getRandomRelaventConnection(currenttime);
			if (con != null) {
				int[] ars = ((Lineage) tantlineages.get(index)).getAreas();
				// test for successful dispersal
				double rn = ran.nextDouble();
				if (rn < con.getFunc()) {
					ars[con.getDest().getIndex()] = 1;
					// System.out.println("ss "+con.getDest().getIndex());
				}
				((Lineage) tantlineages.get(index)).setAreas(ars);
			}
		}
	}

	/*
	 * should create lineages and place lineages in areas in concordance with
	 * Ree et al. 2005 therefore you either choose sympatric or vicariance
	 */
	private void lineageBirth(int index, int numofchild) {
		/*
		 * if there is only one area, then the speciation mode is sympatric both
		 * children inherit the parent area
		 */
		Node node1 = new Node(extantNodes.get(index));
		Node node2 = new Node(extantNodes.get(index));
		extantNodes.get(index).addChild(node1);
		extantNodes.get(index).addChild(node2);
		node1.assocObject(BIRTHTIME, new Double(currenttime));
		node2.assocObject(BIRTHTIME, new Double(currenttime));
		extantNodes.get(index).setName("in" + curdead);
		extantNodes.get(index).setName(
				((Lineage) tantlineages.get(index)).getString());
		// System.out.println("in"+curdead);
		curdead++;
		int [][][] iters = jade.math.Utils.iter_splitranges(((Lineage) tantlineages.get(index)).getAreas());
		int x = ran.nextInt(iters.length);
		deathLineage(index);
		tantlineages.remove(index);
		finalT.addExternalNode(node1);
		finalT.addExternalNode(node2);
		extantNodes.add(node1);
		extantNodes.add(node2);
		Lineage rt = new Lineage(node1, numareas);
		rt.setAreas(iters[x][0]);
		lineages.add(rt);
		tantlineages.add(rt);
		rt = new Lineage(node2, numareas);
		rt.setAreas(iters[x][1]);
		lineages.add(rt);
		tantlineages.add(rt);
		/*
		if (((Lineage) tantlineages.get(index)).getCurNumAreas() == 1) {
			deathLineage(index);
			int newA1 = ((Lineage) tantlineages.get(index)).getRandomArea();
			// extinctlineages.add(tantlineages.get(index));
			tantlineages.remove(index);
			finalT.addExternalNode(node1);
			finalT.addExternalNode(node2);
			extantNodes.add(node1);
			extantNodes.add(node2);
			Lineage rt = new Lineage(node1, numareas);
			rt.addArea(newA1);// one inherits the old area
			lineages.add(rt);
			tantlineages.add(rt);
			rt = new Lineage(node2, numareas);
			rt.addArea(newA1);// one randomly disperses
			lineages.add(rt);
			tantlineages.add(rt);
		} else {// decide between sympatric or vicariance
			double vic = ran.nextDouble();
			int newA2 = ((Lineage) tantlineages.get(index)).getRandomArea();
			int[] newA1 = ((Lineage) tantlineages.get(index)).getAreas();
			int[] cnewA1 = (int[]) newA1.clone();
			deathLineage(index);
			// extinctlineages.add(tantlineages.get(index));
			tantlineages.remove(index);
			// does not correctly seperate the lineages for vicariance
			finalT.addExternalNode(node1);
			finalT.addExternalNode(node2);
			extantNodes.add(node1);
			extantNodes.add(node2);
			Lineage rt = new Lineage(node1, numareas);
			rt.setAreas(cnewA1);// one inherits the old areas
			lineages.add(rt);
			tantlineages.add(rt);
			if (vic < 0.5) {
				rt.removeArea(newA2);
				// System.out.println(newA2);
			}
			Lineage rt2 = new Lineage(node2, numareas);
			rt2.addArea(newA2);// one takes a random one
			lineages.add(rt2);
			tantlineages.add(rt2);
		}*/
	}

	private void localExtinction(int index) {
		if (((Lineage) tantlineages.get(index)).getCurNumAreas() > 1) {
			((Lineage) tantlineages.get(index)).randomRemoveArea();
			if (((Lineage) tantlineages.get(index)).getCurNumAreas() == 0) {
				deathLineage(index);
				extinctlineages.add(tantlineages.get(index));
				tantlineages.remove(index);
			}
		} else {
			deathLineage(index);
			extinctlineages.add(tantlineages.get(index));
			tantlineages.remove(index);
		}
		// System.out.println("x");
	}

	private void deathLineage(int index) {
		Node x = extantNodes.get(index);
		x.assocObject(DEATHTIME, new Double(currenttime));
		x.setBL((Double) x.getObject(DEATHTIME)
				- (Double) x.getObject(BIRTHTIME));
		extantNodes.remove(index);
	}

	private void deathLineageTotal(Node innode) {
		Node parent = null;
		if (innode.isTheRoot()) {
			parent = innode;
		} else {
			parent = innode.getParent();
		}

		/*
		 * fix for if the parent is the root
		 */
		Node node2 = null;
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChild(i) != innode)
				node2 = parent.getChild(i);
		}
		if (parent.isTheRoot()) {
			/*
			 * assumes there are at least three taxa in the tree
			 */
			finalT.setRoot(node2);
		} else {
			Node node3 = parent.getParent();
			/*
			 * error here
			 */
			node3.removeChild(parent);
			node3.addChild(node2);
			node2.setParent(node3);
		}
	}

	public static void main(String[] args) {
		Area ar1 = new Area(0);// EA
		Area ar2 = new Area(1);// WNA
		Area ar3 = new Area(2);// ENA
		Area ar4 = new Area(3);// EU
		// 1
		ar1.addConnection(ar2, 1000, 0, 1);
		ar1.addConnection(ar3, 1000, 0, 1);
		ar1.addConnection(ar4, 1000, 0, 1);
		// 2
		ar2.addConnection(ar1, 1000, 0, 1);
		ar2.addConnection(ar3, 1000, 0, 1);
		ar2.addConnection(ar4, 1000, 0, 1);
		// 3
		ar3.addConnection(ar2, 1000, 0, 1);
		ar3.addConnection(ar1, 1000, 0, 1);
		ar3.addConnection(ar4, 1000, 0, 1);
		// 4
		ar4.addConnection(ar2, 1000, 0, 1);
		ar4.addConnection(ar3, 1000, 0, 1);
		ar4.addConnection(ar1, 1000, 0, 1);
		BioGeoTree bgt = new BioGeoTree();
		ArrayList<Area> ar = new ArrayList<Area>();
		ar.add(ar1);
		ar.add(ar2);
		ar.add(ar3);
		ar.add(ar4);
		bgt.setModel(ar);
		bgt.start();
	}

}
