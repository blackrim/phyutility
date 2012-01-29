package jebl.evolution.trees;

import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.ArrayList;
import java.util.List;

/**
 * utilities for split systems
 *
 * @version $Id: SplitUtils.java 545 2006-11-28 00:08:34Z twobeers $
 *
 * @author Korbinian Strimmer
 */
public class SplitUtils {
	//
	// Public stuff
	//

	/**
	 * creates a split system from a tree
	 * (using tree-induced order of sequences)
	 *
	 * @param tree
	 */
	public static SplitSystem getSplits(Tree tree) {
		return getSplits(new ArrayList<Taxon>(tree.getTaxa()), tree);
	}

	/**
	 * creates a split system from a tree
	 * (using a pre-specified order of sequences)
	 *
	 * @param taxa the list of taxa (order is important)
	 * @param tree
	 */
	public static SplitSystem getSplits(List<Taxon> taxa, Tree tree)
	{
		int size = tree.getInternalEdges().size();
		SplitSystem splitSystem = new SplitSystem(taxa, size);

		boolean[][] splits = splitSystem.getSplitVector();

		int j = 0;
		for (Edge edge : tree.getInternalEdges()) {
			getSplit(taxa, tree, edge, splits[j]);
			j++;
		}

		return splitSystem;
	}


	/**
	 * get split for branch associated with internal node
	 *
	 * @param taxa order of labels
	 * @param tree Tree
	 * @param edge Edge
	 * @param split
	 */
	public static void getSplit(List<Taxon> taxa, Tree tree, Edge edge, boolean[] split) {

		// make sure split is reset
		for (int i = 0; i < split.length; i++) {
			split[i] = false;
		}

		// mark all leafs downstream of the node
		Node[] nodes = tree.getNodes(edge);

		markNode(taxa, tree, nodes[0], nodes[1], split);

		// standardize split (i.e. first index is alway true)
		if (split[0] == false) {
			for (int i = 0; i < split.length; i++) {
				if (split[i] == false)
					split[i] = true;
				else
					split[i] = false;
			}
		}
	}

	/**
     * Checks two splits for identity. This method assumes that the
     * two splits are of the same length and use the same leaf order/
	 *
	 * @param s1 split 1
	 * @param s2 split 2
     * @return true if the two splits are identical
     * @throws IllegalArgumentException if splits don't have the same length
	 */
	public static boolean isSame(boolean[] s1, boolean[] s2)
	{
		boolean reverse = (s1[0] != s2[0]);

        if (s1.length != s2.length)
			throw new IllegalArgumentException("Splits must be of the same length!");

		for (int i = 0; i < s1.length; i++) {
			if (reverse) {
                // splits not identical
				if (s1[i] == s2[i]) return false;
			}
            else {
                // splits not identical
				if (s1[i] != s2[i]) return false;
			}
		}

		return true;
	}

	//
	// Package stuff
	//

	static void markNode(List<Taxon> taxa, Tree tree, Node node, Node parent, boolean[] split) {
		if (tree.isExternal(node)) {
			Taxon taxon = tree.getTaxon(node);
			int index = taxa.indexOf(taxon);

			if (index < 0) {
                throw new IllegalArgumentException("INCOMPATIBLE IDENTIFIER (" + taxon + ")");
			}

			split[index] = true;
		}
		else {
			for (Node child : tree.getAdjacencies(node)) {
                if (child != parent) {
					markNode(taxa, tree, child, node, split);
				}
			}
		}
	}
}
