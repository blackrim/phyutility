package jade.simulate;

import jade.tree.*;
import java.util.*;

public class BirthDeathTree extends Tree {
	private double birthrate = 0.4;

	private double relativebirthrate = 1.0;

	private double deathrate = 0.1;

	private double extantstop = 40;

	private double timestop = 0;

	private double sumrate = 1.0;

	private double currenttime = 0.0;

	private Tree finalT;

	private Random ran;

	private boolean purebirth = false;

	private ArrayList<Node> extantNodes;

	private int numExtantNodes;

	private static String BIRTHTIME = "birthtime";

	private static String DEATHTIME = "deathtime";

	public BirthDeathTree() {
		ran = new Random();
		numExtantNodes = 0;
		extantNodes = new ArrayList<Node>();
		start();
	}

	public void start() {
		// setup parameters
		if (purebirth == false) {
			sumrate = birthrate + deathrate;
			relativebirthrate = birthrate / sumrate;
		} else {
			sumrate = birthrate;
			relativebirthrate = 1.0;
		}
		// start
		finalT = new Tree();
		Node root = new Node();
		finalT.addExternalNode(root);
		numExtantNodes++;
		extantNodes.add(root);
		root.assocObject(BIRTHTIME, new Double(currenttime));
		root.setName("root");
		while (checkStopConditions()) {
			double dt = timeToNextEvent();
			currenttime += dt;

			event();

			if (numExtantNodes < 1) {
				System.out.println("no extant nodes");
				System.exit(0);
			}
		}

		int i, n = numExtantNodes;
		for (i = 0; i < n; i++) {
			Node x = extantNodes.get(i);
			x.assocObject(DEATHTIME, new Double(currenttime));
			x.setBL((Double) x.getObject(DEATHTIME)
			        - (Double) x.getObject(BIRTHTIME));
		}
		finalT.setRoot(root);
		finalT.processRoot();
		for (i = 0; i < finalT.getExternalNodeCount(); i++) {
			finalT.getExternalNode(i).setName("taxon_" + String.valueOf(i + 1));
		}
		TreePrinter tp = new TreePrinter();
		//PrintWriter pw = new PrintWriter(System.out);
		//tp.reportASCII(finalT, pw);
		//pw.flush();
		System.out.println(tp.printNH(finalT) + ";");
	}

	public void event() {
		int extant = ran.nextInt(numExtantNodes);

		if (purebirth || eventIsBirth())
			lineageBirth(extant, 2);
		else
			lineageDeath(extant);
	}

	public void lineageBirth(int index, int numofchild) {
		Node node1 = new Node(extantNodes.get(index));
		Node node2 = new Node(extantNodes.get(index));
		extantNodes.get(index).addChild(node1);
		extantNodes.get(index).addChild(node2);
		node1.assocObject(BIRTHTIME, new Double(currenttime));
		node2.assocObject(BIRTHTIME, new Double(currenttime));
		lineageDeath(index);
		finalT.addExternalNode(node1);
		finalT.addExternalNode(node2);
		extantNodes.add(node1);
		extantNodes.add(node2);
		numExtantNodes++;
		numExtantNodes++;
	}

	public void lineageDeath(int index) {
		Node x = extantNodes.get(index);
		x.assocObject(DEATHTIME, new Double(currenttime));
		x.setBL((Double) x.getObject(DEATHTIME)
		        - (Double) x.getObject(BIRTHTIME));
		extantNodes.remove(index);
		numExtantNodes--;
	}

	public boolean checkStopConditions() {
		boolean keepgoing = true;
		if (extantstop > 0 && numExtantNodes >= extantstop) {
			currenttime += timeToNextEvent();
			keepgoing = false;
		}
		if (timestop > 0.0 && currenttime >= timestop) {
			currenttime = timestop;
			keepgoing = false;
		}
		return keepgoing;
	}

	public double timeToNextEvent() {
		// return -log((double)MERandom::fRan()) /
		// (double(mPhylogeny->getNumExtantLineages()) * mSumRate);
		// return -Math.log(ran.nextDouble()) /
		// ((double)(finalT.getNumberOfExtantNodes()) * sumrate);
		// return -Math.log(fRan.fRan(1.0)) /
		// ((double)(finalT.getNumberOfExtantNodes()) * sumrate);
		return (-Math.log(ran.nextDouble()) * 1.0)
		        / ((double) (numExtantNodes) * sumrate);
	}

	public boolean eventIsBirth() {
		return (ran.nextDouble() < relativebirthrate ? true : false);
	}

	public static void main(String[] args) {
		for (int i = 0; i < 1; i++) {
			new BirthDeathTree();
		}
	}
}
