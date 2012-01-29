package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NexusImporter;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: msuchard
 * Date: Dec 18, 2006
 * Time: 1:13:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalculateSplitRates {

	List<RootedTree> treeList;
	NexusImporter importer;

	public final String INDICATOR = "changed";
	public final String RATE = "rate";
	private List<Clade> cladeList;
	private List<List<TimeInterval>> intervalList;
	private DensityMap densityMap;

	public CalculateSplitRates(NexusImporter importer) {
		this.importer = importer;
		treeList = new ArrayList<RootedTree>(100);
		cladeList = new ArrayList<Clade>(100);
		intervalList = new ArrayList<List<TimeInterval>>(100);
		densityMap = new DensityMap(70, 20, 0, 0, 70, 5);
	}

//	private int maxTrees = 40;
//	private int burnIn = 0;

	public void loadTrees(int maxTrees, int burnIn) throws IOException, ImportException {
		int cnt = 0;
		int cntBurnin = 0;
		while (importer.hasTree() && cnt < maxTrees) {
			RootedTree tree = (RootedTree) importer.importNextTree();
			if (cntBurnin > burnIn) {
				treeList.add(tree);
				cnt++;
//			addCladeRateInforamtion(tree);
//			intervalList.add( getTimeIntervals(tree) );
//				addTreeToDensityMap(tree);
			}
			cntBurnin++;
		}
	}


	/**
	 * @param numRateBoxes Voxel count in rate dimension
	 * @param numTimeBoxes Voxel count in time dimension
	 * @return A density map of proper dimension
	 */
	private DensityMap createDensityMap(int numRateBoxes, int numTimeBoxes) {
		double maxTreeHeight = 0;
		double minRate = 1;
		double maxRate = 1;
		for (RootedTree tree : treeList) {
			double thisHeight = tree.getHeight(tree.getRootNode());
			if (thisHeight > maxTreeHeight)
				maxTreeHeight = thisHeight;
			Set<Node> nodeList = tree.getNodes();
			for (Node node : nodeList) {
				if (node != tree.getRootNode()) {
					double rate = getRate(node);
					if (rate < minRate)
						minRate = rate;
					if (rate > maxRate)
						maxRate = rate;
				}
			}
		}
		maxTreeHeight *= 1.0 + edgeFraction;
		double rateSpread = maxRate - minRate;
		minRate -= rateSpread * edgeFraction;
		if (minRate < 0)
			minRate = 0;
		System.out.println("real max = " + maxRate);
		maxRate += rateSpread * edgeFraction;
		System.out.println("new  max = " + maxRate);
//		System.out.println("max treeheight = "+maxTreeHeight);
//		System.out.println("min rate = "+minRate);
//		System.out.println("max rate = "+maxRate);
//		System.exit(-1);
		DensityMap densityMap =
				new DensityMap(numTimeBoxes, numRateBoxes, 0,
						minRate, maxTreeHeight, maxRate);
		for (RootedTree tree : treeList) {
			addTreeToDensityMap(densityMap, tree);
		}
		return densityMap;
	}

	private void displayLongestDwellTimeInfo() {
		for (RootedTree tree : treeList) {
			double longestDwell = getLongestClockDwellTime(
					getClockDwellTimes(tree));
			double treeLenght = getTreeLength(tree);
			System.out.printf("%5.4f\n", (longestDwell / treeLenght));
		}
	}

	public void displayStatistics() {
//		if( treeList.size() == 0 ) {
//			System.out.println("No trees available.");
//			return;
//		}
//		System.out.println("Analyzed "+treeList.size()+" trees.");

		/*	if (cladeList.size() > 0) {
			System.out.println("Clade Information:");
			System.out.println("\t" + cladeList.size() + " observed unique clades.");
			Collections.sort(cladeList, new CladeFrequencyComparator());
			for (Clade clade : cladeList)
				System.out.println("\t\t" + clade.getCount() + " : " + clade.getName());

		}
		displayIntervals();*/

		//	displayLongestDwellTimeInfo();

//		System.out.println(densityMap.toString());
//

	}


	public void addTreeToDensityMap(DensityMap densityMap, RootedTree tree) {
		Set<Node> nodeList = tree.getNodes();
		for (Node node : nodeList) {
			if (node != tree.getRootNode())
				densityMap.addTreeBranch(tree.getHeight(node),
						tree.getHeight(tree.getParent(node)), getRate(node));
		}
	}

	public void displayIntervals() {
		if (intervalList.size() == 0) {
//			System.out.println("No intervals available.");
			return;
		}
		System.out.println("Interval counts:");
		for (List<TimeInterval> timesList : intervalList) {
			System.out.println("\t" + timesList.size() + " " + getLongestInterval(timesList));
		}

	}


	public void findTimeIntervals() {


	}


	private double getRate(Node node) {
		Double rateDouble = (Double) node.getAttribute(RATE);
		return rateDouble.doubleValue();
	}

	private boolean rateChanged(Node node) {
		Integer changedInt = (Integer) node.getAttribute(INDICATOR);
		return changedInt.intValue() == 1 ? true : false;
	}

	private List<TimeInterval> getTimeIntervals(RootedTree tree) {
		return getTimeIntervals(tree, tree.getRootNode(),
				tree.getHeight(tree.getRootNode()), new ArrayList<TimeInterval>());

	}


	private double getLongestClockDwellTime(Map<Double, Double> dwellTimes) {
		double time = 0;
		Set<Double> rates = dwellTimes.keySet();
		for (Double rate : rates) {
			Double dwell = dwellTimes.get(rate);
			if (dwell > time)
				time = dwell;
		}
		return time;
	}

	private Map<Double, Double> getClockDwellTimes(RootedTree tree) {
		Map<Double, Double> rateDwellTimes = new HashMap<Double, Double>();
		Set<Node> nodes = tree.getNodes();
		for (Node node : nodes) {
			if (node != tree.getRootNode()) {
				double branchLength = tree.getLength(node);
				double rate = getRate(node);
				Double thisRate = new Double(rate);
				Double dwellTime = rateDwellTimes.get(thisRate);
				if (dwellTime == null)                //   {
					rateDwellTimes.put(thisRate, new Double(branchLength));
				else
					rateDwellTimes.put(thisRate, new Double(dwellTime.doubleValue() + branchLength));
			}
		}
		// Test
/*		System.out.println("# of unique rates: "+rateDwellTimes.size());
		Set<Double> keySet = rateDwellTimes.keySet();
		double total = 0;
		for (Double key : keySet) {
			Double dwell = rateDwellTimes.get(key);
			System.out.printf("%5.4f : %5.4f\n",key,dwell);
			total += dwell;
		}
		System.out.printf("Sum of dwell = %5.4f\n",total);*/
		return rateDwellTimes;


	}

	private double getTreeLength(RootedTree tree) {
		double total = 0;
		Set<Node> nodes = tree.getNodes();
		for (Node node : nodes)
			total += tree.getLength(node);
		return total;
	}

	/*private List<DwellTime> getDwellTimes(RootedTree tree) {
		return getDwellTimes(tree, tree.getRootNode(),
				tree.getHeight(tree.getRootNode()), 0.0, new ArrayList<DwellTime>());
	}

	private List<DwellTime> getDwellTimes(RootedTree tree, Node node, double startTime, double currentLenght, List<DwellTime> dwellTimes) {
		if (tree.isExternal(node)) {
			DwellTime dwellTime = new DwellTime(
				startTime, currentLenght, getRate(node)
			);
			dwellTimes.add(dwellTime);
			return null;
		}
		List<Node> children = tree.getChildren(node);
		for (Node child : children) {
			double branchLength = tree.getHeight(node) - tree.getHeight(child);
			if (rateChanged(child)) { // end dwell
				DwellTime dwellTime = new DwellTime(startTime,currentLenght,getRate(node));
				dwellTimes.add(dwellTime);

				getDwellTimes(tree,child,tree.getHeight(node),
						branchLength, dwellTimes);
			}  else {
				getDwellTimes(tree,child,startTime,currentLenght+branchLength, dwellTimes);
			}
		}
		return dwellTimes;
	}*/

	private List<TimeInterval> getTimeIntervals(RootedTree tree, Node node, double startTime, List<TimeInterval> intervals) {
		if (tree.isExternal(node)) {
			TimeInterval timeInterval = new TimeInterval(
					startTime, tree.getHeight(node), getRate(node));
			intervals.add(timeInterval);
			return null;
		}
		List<Node> children = tree.getChildren(node);
		for (Node child : children) {
			if (rateChanged(child)) { // end interval
				TimeInterval timeInterval = new TimeInterval(
						startTime, tree.getHeight(node), getRate(node));
				intervals.add(timeInterval);
				getTimeIntervals(tree, child, tree.getHeight(node), intervals);
			} else {
				getTimeIntervals(tree, child, startTime, intervals);
			}
		}
		return intervals;
	}


	private void addCladeRateInforamtion(RootedTree tree) {
		for (Node node : tree.getInternalNodes()) {
			addCladeRateInformation(tree, node);
		}
		for (Node node : tree.getExternalNodes()) {
			addCladeRateInformation(tree, node);
		}
	}

//	private void addCladeRateInformationTest(RootedTree tree, Node node) {
//		Set<String> attributeNames = node.getAttributeNames();
//		for( String name : attributeNames )
//			System.out.println("n: "+name);
//	}

	private double getLongestInterval(List<TimeInterval> intervals) {
		Collections.sort(intervals);
		return (intervals.get(intervals.size() - 1).getLength());
	}

	private void addCladeRateInformation(RootedTree tree, Node node) {
		if (tree.getRootNode() != node) {
			Integer changedInt = (Integer) node.getAttribute(INDICATOR);
			Double rateDouble = (Double) node.getAttribute(RATE);
			String name = constructUniqueName(tree, node);
//			System.out.println(name + ": "+changedInt.toString() + " " + rateDouble.toString());
			//if( cladeList == null )
			Clade newClade = new Clade(name);
			int index = cladeList.indexOf(newClade);
			if (index == -1) {
				index = cladeList.size();
				cladeList.add(newClade);
			}
			cladeList.get(index).addValues(changedInt, rateDouble);
		}

	}

//	private BitSet constructUniqueID(RootedTree tree, Node node) {
//		Set<Node> taxa = RootedTreeUtils.getDescendantTips(tree,node);
//		BitSet bitSet = new BitSet(taxa.size());
//		for(Node tip : taxa)
//			bitSet.set(tree.get)
//
//
//	}

	private String constructUniqueName(RootedTree tree, Node node) {
		if (tree.isExternal(node))
			return tree.getTaxon(node).getName();
		Set<Node> taxa = RootedTreeUtils.getDescendantTips(tree, node);
		List<String> nameList = new ArrayList<String>(taxa.size());
		for (Node tip : taxa)
			nameList.add(tree.getTaxon(tip).getName());
		Collections.sort(nameList);
		StringBuffer sb = new StringBuffer();
		int cnt = 0;
		for (String name : nameList) {
			if (cnt != 0)
				sb.append(",");
			sb.append(name);
			cnt++;
		}
		return sb.toString();
	}


	private class DoubleStatistic {

		private List<Double> data;
		private double total;

		public DoubleStatistic() {
			data = new ArrayList<Double>(1000);
			total = 0;
		}

		public void add(double d) {
			data.add(d);
			total += d;

		}

		public double getMean() {
			return total / data.size();
		}
	}

	private class DwellTime implements Comparable<DwellTime> {

		private double start;
		private double rate;
		private double length;

		public DwellTime(double start, double length, double rate) {
			this.start = start;
			this.length = length;
			this.rate = rate;
		}

		public int compareTo(DwellTime dwellTime) {
			return (int) (getLength() - dwellTime.getLength());
		}

		public double getLength() {
			return length;
		}
	}

	private class TimeInterval implements Comparable<TimeInterval> {

		private double start;
		private double end;
		private double rate;

		public TimeInterval(double start, double end, double rate) {
			this.start = start;
			this.end = end;
			this.rate = rate;
		}


		public int compareTo(TimeInterval timeInterval) {
			//return (int) (rate - timeInterval.getRate());
			return (int) (getLength() - timeInterval.getLength());
		}

		public double getLength() {
			return start - end;
		}

		public double getStart() {
			return start;
		}

		public double getEnd() {
			return end;
		}

		public double getRate() {
			return rate;
		}

	}

	private class DensityMap {

		private final String SEP = "\t";
		private final String DBL = "%5.4f";

		private int binX;
		private int binY;

		private int[][] data;
		private int[] counts;
		private double startX;
		private double startY;
		private double scaleX;
		private double scaleY;
		private int total = 0;


		public DensityMap(int binX, int binY,
		                  double startX, double startY,
		                  double endX, double endY) {
			this.binX = binX;
			this.binY = binY;
			data = new int[binX][binY];
			counts = new int[binX];
			this.startX = startX;
			this.startY = startY;
			scaleX = (endX - startX) / (double) binX;
			scaleY = (endY - startY) / (double) binY;
		}

		public void addTreeBranch(double start, double end, double y) {
			// determine bin for y
			int Y = (int) ((y - startY) / scaleY);
			// determine start and end bin for x
			int START = (int) ((start - startX) / scaleX);
			int END = (int) ((end - startX) / scaleX);
//			System.out.println(start+":"+end+" -> "+START+":"+END);
			for (int i = START; i <= END; i++) {
				data[i][Y] += 1;
				counts[i] += 1;
				total += 1;
			}
		}

		public String toString() {
//			double dblTotal = (double) total;
			StringBuilder sb = new StringBuilder();
			sb.append("0.0");
			for (int i = 0; i < binX; i++) {
				sb.append(SEP);
				sb.append(String.format("%3.1f", startX + scaleX * i));
			}
			sb.append("\n");
			for (int i = 0; i < binY; i++) {
				sb.append(String.format("%3.1f", startY + scaleY * i));
				//double dblCounts = (double)counts[i];
				for (int j = 0; j < binX; j++) {
					sb.append(SEP);
					double dblCounts = (double) counts[j];
					if (dblCounts > 0)
						sb.append(String.format(DBL,
								(double) data[j][i] / (double) counts[j]
						));
					else
						sb.append(String.format(DBL, 0.0));
				}
				sb.append("\n");
			}
			return sb.toString();
		}


	}


	private class Clade implements Comparable<Clade> {

		private String name;
		private List<Integer> indicatorValues;
		private List<Double> rateValues;
		private int count = 0;

		public Clade(String name) {
			this.name = name;
			indicatorValues = new ArrayList<Integer>(1000);
			rateValues = new ArrayList<Double>(1000);
		}

		public int compareTo(Clade clade) {
			System.out.println("co");
			return name.compareTo(clade.getName());
		}

		public void addValues(Integer inInt, Double inDouble) {
			indicatorValues.add(inInt);
			rateValues.add(inDouble);
			count++;
		}

		public boolean equals(Object obj) {
			Clade c = (Clade) obj;
			return (name.compareTo(c.getName()) == 0);
		}

		public String getName() {
			return name;
		}

		public int getCount() {
			return count;
		}

		public double getIndicatorProbability() {
			int sum = 0;
			for (Integer i : indicatorValues)
				sum += i;
			return (double) sum / (double) count;
		}
	}

	private class CladeFrequencyComparator implements Comparator<Clade> {

		public int compare(Clade cladeA, Clade cladeB) {
			if (cladeA.getCount() > cladeB.getCount())
				return -1;
			if (cladeA.getCount() < cladeB.getCount())
				return 1;
			return cladeA.getName().compareTo(cladeB.getName());
		}
	}

	/**
	 * @param args args[0] = Beast tree log,
	 *             args[1] = max number of trees to read
	 *             args[2] = number of burnin trees to discard
	 *             args[3] = proportion file name,
	 *             args[4] = density map filename
	 */

	public static void main(String[] args) {
		try {
			NexusImporter importer = new NexusImporter(
					new BufferedReader(new FileReader(new File(args[0]))));

			CalculateSplitRates calculator =
					new CalculateSplitRates(importer);
			calculator.loadTrees(
					Integer.parseInt(args[1]),
					Integer.parseInt(args[2])
			);
			//	calculator.getLongestClock();

//			try {
			PrintWriter printWriter = new PrintWriter(args[3]);
			calculator.writeLongestDwellTimeInfo(printWriter);
			printWriter.close();

//			}

			printWriter = new PrintWriter(args[4]);
			calculator.writeDensityMap(printWriter);
			printWriter.close();

			//calculator.displayStatistics();

		} catch (FileNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (ImportException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		System.exit(0);
	}

	public int numRateBoxes = 25;
	public int numTimeBoxes = 100;
	public double edgeFraction = 0.05;

	private void writeDensityMap(PrintWriter printWriter) {
		densityMap = createDensityMap(numRateBoxes, numTimeBoxes);
		printWriter.println(densityMap.toString());

	}

	private void writeLongestDwellTimeInfo(PrintWriter printWriter) {
		printWriter.print("DwellTime\tTreeLength\tProportion\n");
		for (RootedTree tree : treeList) {
			Map<Double, Double> map = getClockDwellTimes(tree);
			double longestDwell = getLongestClockDwellTime(map);
			//double rate = map.
			double treeLenght = getTreeLength(tree);
			double proportion = longestDwell / treeLenght;
			printWriter.printf("%5.4f\t%5.4f\t%5.4f\n", longestDwell, treeLenght, proportion);
		}
	}

}


