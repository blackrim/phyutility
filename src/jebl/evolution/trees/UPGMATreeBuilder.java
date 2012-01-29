package jebl.evolution.trees;

import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.List;
import java.util.Arrays;

/**
 * constructs a UPGMA tree from pairwise distances
 *
 * @version $Id: UPGMATreeBuilder.java 301 2006-04-17 15:35:01Z rambaut $
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Joseph Heled
 *
 * Adapted from BEAST code.
 */

class UPGMATreeBuilder extends ClusteringTreeBuilder {
     // want a rooted tree
    private final SimpleRootedTree tree;

    /**
     * constructor UPGMA tree
     *
     * @param distanceMatrix distance matrix
     */
    public UPGMATreeBuilder(DistanceMatrix distanceMatrix) {
        super(distanceMatrix, 2);
        tree = new SimpleRootedTree();
    }

    //
    // Protected and Private stuff
    //

    protected Tree getTree() {
        return tree;
    }

    protected Node createExternalNode(Taxon taxon) {
        return tree.createExternalNode(taxon);
    }

    protected Node createInternalNode(Node[] nodes, double[] distances) {
        List<Node> a = Arrays.asList(nodes);
        Node node = tree.createInternalNode(a);
        tree.setHeight(node, distances[0]);
        return node;
    }

    protected double[] joinClusters() {
        Double d = getDist(besti, bestj) / 2.0;
        return new double[] {d};
    }

    protected double updatedDistance(int k) {
        int i = besti;
        int j = bestj;
        int ai = alias[i];
        int aj = alias[j];

        double tipSum = (double) (tipCount[ai] + tipCount[aj]);

        return 	(((double)tipCount[ai]) / tipSum) * getDist(k, i) +
                (((double)tipCount[aj]) / tipSum) * getDist(k, j);
    }
}
