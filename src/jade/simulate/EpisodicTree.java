package jade.simulate;

import jade.math.fRan;

import java.util.*;

import jade.tree.*;

public class EpisodicTree {
	private static String BIRTHTIME = "birthtime";

	private static String DEATHTIME = "deathtime";

	private ArrayList<Node> extantNodes;

	private int numepisodes = 0;

	private ArrayList<String> episodes;

	private int numgrowth = 0;

	private ArrayList<GrowthEpisode> growthepisodes;

	private int numconstant = 0;

	private ArrayList<ConstantEpisode> constantepisodes;

	private int numextinction = 0;

	private ArrayList<ExtinctionEpisode> extinctionepisodes;

	private int lastextantlineages = 0;

	private double lastepisodetime = 0.0;

	private double lastcurrenttime = 0.0;

	private double birthrate = 1.0;

	private double relativebirthrate = 1.0;

	private double deathrate = 0.2;

	private double sumrate = 1.0;

	private double currenttime = 0.0;

	private Tree finalT;

	private Random ran;

	public EpisodicTree() {
		extantNodes = new ArrayList<Node>();
		ran = new Random();
		// addextinction fraction=0.1
		// addgrowth birth=0.2 death=0.1 extant=1000
		growthepisodes = new ArrayList<GrowthEpisode>();
		extinctionepisodes = new ArrayList<ExtinctionEpisode>();
		episodes = new ArrayList<String>();
		// one growth
		GrowthEpisode ge = new GrowthEpisode();
		ge.birthrate = 1;
		ge.deathrate = 0;
		ge.stoptype = "extantstop";
		ge.extantstop = 20;
		numgrowth++;
		numepisodes++;
		growthepisodes.add(ge);
		episodes.add("growth");
		// two extinct
		ExtinctionEpisode ee = new ExtinctionEpisode();
		ee.fraction = 0.5;
		ee.type = "fraction";
		ee.number = 0;
		extinctionepisodes.add(ee);
		numextinction++;
		numepisodes++;
		episodes.add("extinction");
		// three growth
		GrowthEpisode ge2 = new GrowthEpisode();
		ge2.birthrate = 1;
		ge2.deathrate = 0;
		ge2.stoptype = "extantstop";
		ge2.extantstop = 40;
		numgrowth++;
		numepisodes++;
		growthepisodes.add(ge2);
		episodes.add("growth");
		start();
	}

	public void start() {
		lastextantlineages = 0;

		lastepisodetime = 0.0;
		lastcurrenttime = 0.0;

		// start
		finalT = new Tree();
		Node root = new Node();
		finalT.addExternalNode(root);
		extantNodes.add(root);
		root.assocObject(BIRTHTIME, new Double(currenttime));
		root.setName("root");

		int cge = 0;
		int cce = 0;
		int cee = 0;
		for (int i = 0; i < numepisodes; i++) {
			if (episodes.get(i) == "growth") {
				simulateGrowthEpisode(i, (GrowthEpisode) growthepisodes
				        .get(cge));
				lastepisodetime = currenttime - lastcurrenttime;
				cge++;
			} else if (episodes.get(i) == "constant") {
				simulateConstantEpisode(i, (ConstantEpisode) constantepisodes
				        .get(cce));
				cce++;
				lastepisodetime = currenttime - lastcurrenttime;
			} else if (episodes.get(i) == "extinction") {
				simulateExtinctionEpisode(i,
				        (ExtinctionEpisode) extinctionepisodes.get(cee));
				cee++;
				// don't set mLastEpisodeTime because an extinction is
				// instantaneous.
			}
			lastextantlineages = extantNodes.size();
			lastcurrenttime = currenttime;
		}

		int n = extantNodes.size();
		for (int i = 0; i < n; i++) {
			Node x = extantNodes.get(i);
			x.assocObject(DEATHTIME, new Double(currenttime));
			x.setBL((Double) x.getObject(DEATHTIME)
			        - (Double) x.getObject(BIRTHTIME));
		}
		finalT.setRoot(root);
		finalT.processRoot();
		for (int i = 0; i < finalT.getExternalNodeCount(); i++) {
			finalT.getExternalNode(i).setName(String.valueOf(i + 1));
		}
		TreePrinter tp = new TreePrinter();
		//PrintWriter pw = new PrintWriter(System.out);
		//tp.reportASCII(finalT, pw);
		//pw.flush();
		System.out.println(tp.printNH(finalT) + ";");
	}

	private void simulateGrowthEpisode(int inEpisodeNo, GrowthEpisode inEpisode) {
		double dt;

		setBirthDeathRates(inEpisode.birthrate, inEpisode.deathrate);

		while (checkStopConditionsGrowth(inEpisodeNo, inEpisode)) {
			dt = timeToNextEvent();

			currenttime += dt;

			int extant = ran.nextInt(extantNodes.size());

			if (eventIsBirth())
				lineageBirth(extant, 2);
			else
				lineageDeath(extant);

			if (extantNodes.size() < 1) {
				System.out.println("no extant nodes: growth");
				System.exit(0);
			}
		}
	}

	private void simulateConstantEpisode(int inEpisodeNo,
	        ConstantEpisode inEpisode) {
		double dt;
		if (inEpisode.stochastic)
			setBirthDeathRates(inEpisode.birthdeathrate,
			        inEpisode.birthdeathrate);
		else
			setBirthDeathRates(inEpisode.birthdeathrate, 0.0);

		while (checkStopConditionsConstant(inEpisodeNo, inEpisode)) {
			dt = timeToNextEvent();

			currenttime += dt;

			int extant = ran.nextInt(extantNodes.size());

			if (inEpisode.stochastic) {
				if (eventIsBirth())
					lineageBirth(extant, 2);
				else
					lineageDeath(extant);
			} else {
				lineageBirth(extant, 2);

				extant = ran.nextInt(extantNodes.size());
				lineageDeath(extant);
			}

			if (extantNodes.size() < 1) {
				System.out.println("no extant nodes");
				System.exit(0);
			}
		}
	}

	private void simulateExtinctionEpisode(int inEpisodeNo,
	        ExtinctionEpisode inEpisode) {
		if (inEpisode.type == "probability") {
			int i, n = extantNodes.size();
			for (i = 0; i < n; i++) {
				if (ran.nextDouble() < inEpisode.probability ? true : false)
					lineageDeath(i);
			}
		} else {
			int target, extant;

			if (inEpisode.type == "fraction")
				target = (int) ((1.0 - inEpisode.fraction) * extantNodes.size());
			else
				target = (int) ((extantNodes.size() - inEpisode.number));

			while (extantNodes.size() > target) {
				extant = ran.nextInt(extantNodes.size());
				lineageDeath(extant);
			}
		}
	}

	private void setBirthDeathRates(double inBirthRate, double inDeathRate) {
		if (inDeathRate == 0.0) {
			deathrate = 0.0;
			birthrate = inBirthRate;
			sumrate = inBirthRate;
			relativebirthrate = 1.0;
		} else {
			birthrate = inBirthRate;
			deathrate = inDeathRate;
			sumrate = birthrate + deathrate;
			relativebirthrate = birthrate / sumrate;
		}
	}

	private boolean checkStopConditionsGrowth(int inEpisodeNo,
	        GrowthEpisode inEpisode) {
		boolean go = true;
		if (inEpisode.stoptype == "extantstop") {
			if (extantNodes.size() >= inEpisode.extantstop) {
				if (inEpisodeNo == numgrowth - 1)
					currenttime += timeToNextEvent();
				go = false;
			}
		} else if (inEpisode.stoptype == "byextantstop") {
			long change = extantNodes.size() - lastextantlineages;
			if (change < 0.0)
				change *= -1.0;
			if (change >= inEpisode.byextantstop) {
				if (inEpisodeNo == numgrowth - 1)
					currenttime += timeToNextEvent();
				go = false;
			}
		} else if (inEpisode.stoptype == "timestop") {
			if (currenttime >= inEpisode.timestop) {
				currenttime = inEpisode.timestop;
				go = false;
			}
		} else if (inEpisode.stoptype == "bytimestop") {
			double change = Math.abs(currenttime - lastcurrenttime);
			if (change >= inEpisode.bytimestop) {
				go = false;
				currenttime = lastcurrenttime + inEpisode.bytimestop;
			}
		} else if (inEpisode.stoptype == "multiplestop") {
			double change = Math.abs(currenttime - lastcurrenttime);
			if (change >= inEpisode.multipletimestop * lastepisodetime) {
				go = false;
				currenttime = lastcurrenttime
				        + (inEpisode.multipletimestop * lastepisodetime);
			}

		}
		return go;
	}

	private boolean checkStopConditionsConstant(int inEpisodeNo,
	        ConstantEpisode inEpisode) {
		boolean go = true;
		if (inEpisode.stoptype == "extantstop") {
			if (extantNodes.size() >= inEpisode.extantstop) {
				if (inEpisodeNo == numconstant - 1)
					currenttime += timeToNextEvent();
				go = false;
			}
		} else if (inEpisode.stoptype == "byextantstop") {
			long change = extantNodes.size() - lastextantlineages;
			if (change < 0.0)
				change *= -1.0;
			if (change >= inEpisode.byextantstop) {
				if (inEpisodeNo == numconstant - 1)
					currenttime += timeToNextEvent();
				go = false;
			}
		} else if (inEpisode.stoptype == "timestop") {
			if (currenttime >= inEpisode.timestop) {
				currenttime = inEpisode.timestop;
				go = false;
			}
		} else if (inEpisode.stoptype == "bytimestop") {
			double change = Math.abs(currenttime - lastcurrenttime);
			if (change >= inEpisode.bytimestop) {
				go = false;
				currenttime = lastcurrenttime + inEpisode.bytimestop;
			}
		} else if (inEpisode.stoptype == "multiplestop") {
			double change = Math.abs(currenttime - lastcurrenttime);
			if (change >= inEpisode.multipletimestop * lastepisodetime) {
				go = false;
				currenttime = lastcurrenttime
				        + (inEpisode.multipletimestop * lastepisodetime);
			}
		}
		return go;
	}

	public boolean eventIsBirth() {
		return (ran.nextDouble() < relativebirthrate ? true : false);
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
	}

	public void lineageDeath(int index) {
		Node x = extantNodes.get(index);
		x.assocObject(DEATHTIME, new Double(currenttime));
		x.setBL((Double) x.getObject(DEATHTIME)
		        - (Double) x.getObject(BIRTHTIME));
		extantNodes.remove(index);
	}

	public double timeToNextEvent() {
		// return -log((double)MERandom::fRan()) /
		// (double(mPhylogeny->getNumExtantLineages()) * mSumRate);
		// return -Math.log(ran.nextDouble()) /
		// ((double)(finalT.getNumberOfExtantNodes()) * sumrate);
		return -Math.log(fRan.fRan(1.0))
		        / ((double) (extantNodes.size()) * sumrate);
		// return 5;
	}

	public static void main(String[] args) {
		for (int i = 0; i < 1; i++) {
			new EpisodicTree();
		}
	}
}
