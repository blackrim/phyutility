/*
 * TreeSimulator.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.treesimulation;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.NexusExporter;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;
import jebl.math.Random;

import java.io.*;
import java.util.*;

/**
 * This class provides the framework for (backwards-through-time) tree simulation. Basically,
 * this takes a set of tips (optionally at different dates) and repeatedly coalesces them together
 * until the MRCA is reached and the tree is returned. The time intervals between nodes are provided
 * by the IntervalGenerator and an implementation of this is the CoalescentIntervalGenerator in
 * the jebl.evolution.coalescent package.
 * @author Andrew Rambaut
 * @version $Id: TreeSimulator.java 563 2006-12-07 17:43:10Z rambaut $
 */
public class TreeSimulator {

	/**
	 * A constructor for a given number of taxa, all sampled at the same time
	 * @param intervalGenerator
	 * @param taxonCount
	 */
	public TreeSimulator(IntervalGenerator intervalGenerator, String taxonPrefix, int taxonCount) {
		this(intervalGenerator, taxonPrefix, new int[] { taxonCount }, new double[] { 0.0 } );
	}

	public TreeSimulator(IntervalGenerator intervalGenerator, String taxonPrefix, double[] samplingTimes) {
		this.intervalGenerator = intervalGenerator;

		List<Taxon> taxonList = new ArrayList<Taxon>();
		for (int i = 0; i < samplingTimes.length; i++) {
			Taxon taxon = Taxon.getTaxon(taxonPrefix + Integer.toString(i + 1) + "_" + Double.toString(samplingTimes[i]));
			taxon.setAttribute("height", samplingTimes[i]);
			taxonList.add(taxon);
		}

		setTaxa(taxonList, "height");
	}

	public TreeSimulator(IntervalGenerator intervalGenerator, String taxonPrefix, int[] samplingCounts, double[] samplingTimes) {
		this.intervalGenerator = intervalGenerator;

		List<Taxon> taxonList = new ArrayList<Taxon>();
		int k =0;
		for (int i = 0; i < samplingCounts.length; i++) {
			for (int j = 0; j < samplingCounts[i]; j++) {
				Taxon taxon = Taxon.getTaxon(taxonPrefix + Integer.toString(k + 1) + "_" + Double.toString(samplingTimes[i]));
				taxon.setAttribute("height", samplingTimes[i]);
				taxonList.add(taxon);
				k++;
			}
		}

		setTaxa(taxonList, "height");
	}

	/**
	 * A constructor for a given collection of taxa. If the taxa have the attribute given by heightAttributeName then
	 * this will be used, otherwise a height of 0.0 will be assumed.
	 * @param intervalGenerator
	 * @param taxa
	 */
	public TreeSimulator(IntervalGenerator intervalGenerator, final Collection<Taxon> taxa, final String heightAttributeName) {
		this.intervalGenerator = intervalGenerator;
		List<Taxon> taxonList = new ArrayList<Taxon>();
		for (Taxon taxon : taxa) {
			taxonList.add(taxon);
		}
		setTaxa(taxonList, heightAttributeName);
	}

	private void setTaxa(List<Taxon> taxa, final String heightAttributeName) {
		this.taxa = taxa;
		this.heightAttributeName = heightAttributeName;
		Collections.sort(this.taxa, new Comparator<Taxon>() {
			public int compare(Taxon taxon1, Taxon taxon2) {
				double height1 = 0.0;
				double height2 = 0.0;

				Double attr = (Double)taxon1.getAttribute(heightAttributeName);
				if (attr != null) {
					height1 = attr.doubleValue();
				}
				attr = (Double)taxon1.getAttribute(heightAttributeName);
				if (attr != null) {
					height2 = attr.doubleValue();
				}
				return Double.compare(height1, height2);
			}
		});
	}

	public Tree simulate() {
		return simulate(false);
	}

	public Tree simulate(boolean medianHeights) {
		SimpleRootedTree tree = new SimpleRootedTree();

		Node[] tipNodes = new Node[taxa.size()];
		int i = 0;
		// create all the tips
		for (Taxon taxon : taxa) {
			Node tip = tree.createExternalNode(taxon);
			tree.setHeight(tip, (Double)taxon.getAttribute(heightAttributeName));

			tipNodes[i] = tip;

			i++;
		}

		List<Node> activeNodes = new ArrayList<Node>();

		double currentHeight = 0.0;
		double nextHeight = 0.0;

		// get at least two tips
		int nextSampleNode = 0;

		do {

			// if there are less than 2 lineages available or
			// if the new height is older than some samples,
			// then add the samples and reset the process
			while (nextSampleNode < tipNodes.length &&
					(activeNodes.size() < 2 || nextHeight >= tree.getHeight(tipNodes[nextSampleNode]))) {

				// Current height is now the height of the sampled node
				currentHeight = tree.getHeight(tipNodes[nextSampleNode]);

				// add the sampled node
				activeNodes.add(tipNodes[nextSampleNode]);
				nextSampleNode ++;
			}

			if (!medianHeights) {
				// draw a new height
				double U = Random.nextDouble();
				nextHeight = currentHeight + intervalGenerator.getInterval(U, activeNodes.size(), currentHeight);
			} else {
				// obtain the median height
				nextHeight = currentHeight + intervalGenerator.getInterval(0.5, activeNodes.size(), currentHeight);
			}

			if (nextSampleNode >= tipNodes.length || nextHeight < tree.getHeight(tipNodes[nextSampleNode])) {
				// draw two nodes from the list of those available and remove them
				Node leftNode = activeNodes.remove(Random.nextInt(activeNodes.size()));
				Node rightNode = activeNodes.remove(Random.nextInt(activeNodes.size()));

				Node node = coalesce(leftNode, rightNode, tree, nextHeight);
				activeNodes.add(node);

				currentHeight = nextHeight;
			}

		} while (nextSampleNode < tipNodes.length || activeNodes.size() > 1);

		return tree;
	}

	private Node coalesce(Node leftNode, Node rightNode, SimpleRootedTree tree, double height) {

		Node node = null;

		node = tree.createInternalNode(Arrays.asList(new Node[] { leftNode, rightNode }));
		tree.setHeight(node, height);

		return node;
	}

	private final IntervalGenerator intervalGenerator;
	private List<Taxon> taxa;
	private String heightAttributeName;

	/**
	 * A main() to test the tree simulation classes. In this case the interval generator is a simple
	 * anonymous class that simply returns the uniform random deviate that it is passed.
	 * @param args
	 */
	public static void main(String[] args) {

		double[] samplingTimes = new double[] {
				0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0
		};

		// A simple uniform interval generator..
		IntervalGenerator intervals = new IntervalGenerator() {
			public double getInterval(double criticalValue, int lineageCount, double currentHeight) {
				return criticalValue;
			}
		};
		TreeSimulator sim = new TreeSimulator(intervals, "tip", samplingTimes);

		int REPLICATE_COUNT = 100;

		try {
			Tree[] trees = new Tree[REPLICATE_COUNT];

			System.err.println("Simulating " + REPLICATE_COUNT + " trees of " + samplingTimes.length + " tips:");
			System.err.print("[");
			for (int i = 0; i < REPLICATE_COUNT; i++) {

				trees[i] = sim.simulate();
				if (i != 0 && i % 100 == 0) {
					System.err.print(".");
				}
			}
			System.err.println("]");

			Writer writer = new FileWriter("simulated.trees");
			NexusExporter exporter = new NexusExporter(new OutputStreamWriter(System.out));

			exporter.exportTrees(Arrays.asList(trees));

			writer.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
