package jebl.evolution.treemetrics;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;
import jebl.evolution.treemetrics.RootedTreeMetric;

import java.util.*;

/**
 * For each clade_j in treeB, find the MRCA_j of the taxa in clade_j in treeA.
 *
 * obviously, if clade_i exists in treeB then clade_i == MRCA_i.
 *
 * Then find the sum of squares of the differences in height:
 *
 * d = sqrt( sum across i[ (height(clade_i) - height(MRCA_i))^2 ] +
 * 		sum across j[ (height(clade_j) - height(MRCA_j))^2 ] )
 *
 * The rationale is that if a clade moves then this includes the size of movement
 * across the MRCA node and down again. I.e., a clade that moves from one side
 * of the tree to the other scores the difference between the height of that node
 * and the root and back down to the height of the node in the other tree.
 *
 *   +---------A               +---------A
 * +-+                       +-+
 * | +---------B             | |+--------B
 * +                 ====>   + ++
 * | +---------C             |  +--------C
 * +-+                       |
 *   +---------D             +-----------D
 *
 * 	height(tree1)	tmrca(tree2)	diff
 * AB 	10		10		0
 * CD	10		12		2
 * ABCD	12		12		0
 *
 * 	height(tree1)	tmrca(tree2)	diff
 * BC 	9		12		3
 * ABC	10		10		0
 * ABCD	12		12		0
 *
 * So the score is sqrt(2^2 + 3^2) = sqrt(13)
 *
 * Scores much less than this:
 *
 *   +----------A               +----------A
 * + +                        +-+
 * | +----------B             | |        +-B
 * +                 ====>    + +--------+
 * |        +---C             |          +-C
 * +--------+                 |
 *          +---D             +------------D
 *
 * 	height(tree1)	tmrca(tree2)	diff
 * AB 	10		10		0
 * CD	4		12		8
 * ABCD	12		12		0
 *
 * 	height(tree1)	tmrca(tree2)	diff
 * BC 	2		12		10
 * ABC	10		10		0
 * ABCD	12		12		0
 *
 * So the score is sqrt(8^2 + 10^2) = sqrt(164)
 *
 * @author Andrew Rambaut
 * @version $Id$
 */
public class CladeHeightMetric implements RootedTreeMetric {

	public CladeHeightMetric() {
		taxonMap = null;
	}

	public CladeHeightMetric(List<Taxon> taxa) {
		taxonMap = new HashMap<Taxon, Integer>();
		for (int i = 0; i < taxa.size(); i++) {
			taxonMap.put(taxa.get(i), i);
		}
	}

	public double getMetric(RootedTree tree1, RootedTree tree2) {

		if (!tree1.getTaxa().equals(tree2.getTaxa())) {
		    throw new IllegalArgumentException("Trees contain different taxa");
		}

		Map<Taxon, Integer> tm = taxonMap;

		if (tm == null) {
			List<Taxon> taxa = new ArrayList<Taxon>(tree1.getTaxa());

			if (!tree2.getTaxa().equals(taxa))
			tm = new HashMap<Taxon, Integer>();
			for (int i = 0; i < taxa.size(); i++) {
				tm.put(taxa.get(i), i);
			}
		}

		List<Clade> clades1 = new ArrayList<Clade>();
		getClades(tm, tree1, tree1.getRootNode(), clades1, null);
		Collections.sort(clades1);

		List<Clade> clades2 = new ArrayList<Clade>();
		getClades(tm, tree2, tree2.getRootNode(), clades2, null);
		Collections.sort(clades2);

		return getDistance(clades1, clades2);
	}

	private void getClades(Map<Taxon, Integer> taxonMap, RootedTree tree, Node node,
	                       List<Clade> clades, BitSet bits) {

	    BitSet bits2 = new BitSet();

	    if (tree.isExternal(node)) {

	        int index = taxonMap.get(tree.getTaxon(node));
	        bits2.set(index);

	    } else {

	        for (Node child : tree.getChildren(node)) {
	            getClades(taxonMap, tree, child, clades, bits2);
	        }
	        clades.add(new Clade(bits2, tree.getHeight(node)));
	    }


	    if (bits != null) {
	        bits.or(bits2);
	    }
	}

	private double getDistance(List<Clade> clades1, List<Clade> clades2) {

	    double distance = 0.0;

	    for (Clade clade1 : clades1) {
	        double height1 = clade1.getHeight();

	        Clade clade2 = findMRCA(clade1, clades2);
	        double height2 = clade2.getHeight();

	        distance += (height1 - height2) * (height1 - height2);
	    }

	    for (Clade clade2 : clades2) {
	        double height2 = clade2.getHeight();

	        Clade clade1 = findMRCA(clade2, clades1);
	        double height1 = clade1.getHeight();

	        distance += (height1 - height2) * (height1 - height2);
	    }

	    return Math.sqrt(distance);
	}

	private Clade findMRCA(Clade clade1, List<Clade> clades) {

	    for (Clade clade2 : clades) {
	        if (isMRCA(clade1, clade2)) {
	            return clade2;
	        }
	    }

	    return null;
	}

	private boolean isMRCA(Clade clade1, Clade clade2) {
	    if (clade1.getSize() > clade2.getSize()) {
	        return false;
	    }

	    tmpBits.clear();
	    tmpBits.or(clade1.getBits());
	    tmpBits.and(clade2.getBits());

	    return tmpBits.cardinality() == clade1.getSize();
	}

	BitSet tmpBits = new BitSet();

	private class Clade implements Comparable<Clade> {
	    public Clade(final BitSet bits, final double height) {
	        this.bits = bits;
	        this.height = height;
	        size = bits.cardinality();
	    }

	    public BitSet getBits() {
	        return bits;
	    }

	    public double getHeight() {
	        return height;
	    }

	    public int getSize() {
	        return size;
	    }

	    public int compareTo(Clade clade) {
	        int i = bits.cardinality();
	        int j = clade.bits.cardinality();
	        return (i < j ? -1 : (i > j ? 1 : 0));
	    }

	    private final BitSet bits;
	    private final double height;
	    private final int size;

	}

	private final Map<Taxon, Integer> taxonMap;
}
