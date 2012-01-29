package jebl.evolution.trees;

import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for clustering algorithms from pairwise distances
 *
 * @version $Id: ClusteringTreeBuilder.java 662 2007-03-21 00:32:24Z twobeers $
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Joseph Heled
 *
 * Adapted from Alexei Drummond BEAST code.
 */

public abstract class ClusteringTreeBuilder<T extends Tree> implements TreeBuilder<T> {

    public T build() {
        init(distanceMatrix);

        // TT: This should probably be ints, and instead we should write
        // (double) progress / totalPairs to get a non-integer division below.
        double totalPairs = numClusters;
        double progress = 0.0;

        while (true) {
            findNextPair(); // calculates besti, bestj

            abi = alias[besti];
            abj = alias[bestj];
            if (numClusters < numberOfRootSubtrees)
                break;

            newCluster();
            assert( progress <= totalPairs );
            fireSetProgress(progress / totalPairs);
            progress++;
        }
        finish();

        return getTree();
    }

    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    public void fireSetProgress(double fractionCompleted) {
        for (ProgressListener listener : listeners) {
            listener.setProgress(fractionCompleted);
        }
    }

    // must be a type of list that supports the remove() operation
    private final List<ProgressListener> listeners = new ArrayList<ProgressListener>();

    /**
     * A factory method to create a ClusteringTreeBuilder
     * @param method build method to use.
     * @param distances Pre computed pairwise distances.
     * @return A tree builder using method and distance matrix
     */
    static public ClusteringTreeBuilder getBuilder(TreeBuilderFactory.Method method, DistanceMatrix distances) {
        ClusteringTreeBuilder builder;
        switch( method ) {
            case UPGMA:
            {
                builder = new UPGMATreeBuilder(distances);
                break;
            }
            case NEIGHBOR_JOINING:
            default:
            {
                builder = new NeighborJoiningTreeBuilder(distances);
                break;
            }
        }
        return builder;
    }

    //
    // Protected and Private stuff
    //

    /**
     * @param distanceMatrix pair distances to use when building
     * @param rootSubtrees Number of root subtrees. Typically 2 for rooted (completly bifurcating) or 3 for unrooted.
     * @throws IllegalArgumentException when number of taxa is not compatibale with rootSubtrees (i.e. #taxa < #subtrees)
     */
    protected ClusteringTreeBuilder(DistanceMatrix distanceMatrix, int rootSubtrees) throws IllegalArgumentException {
        this.distanceMatrix = distanceMatrix;

        this.numberOfRootSubtrees = rootSubtrees;

        if (distanceMatrix.getSize() < rootSubtrees) {
            throw new IllegalArgumentException("less than " + rootSubtrees + " taxa in distance matrix");
        }
    }

    protected abstract T getTree();

    protected abstract Node createExternalNode(Taxon taxon);

    protected abstract Node createInternalNode(Node[] nodes, double[] distances);

    /**
     * Inform derived class that clusters besti,bestj are being joinded into a new cluster.
     * New cluster will be (the smaller) besti while clusters greater than bestj are shifted one space back.
     *
     * @return  branch distances to new internal node [besti - dist , bestj dist]
     */
    protected abstract double[] joinClusters();

    /**
     * compute updated distance between the new cluster (besti,bestj)
     * to any other cluster k.
     *  (i,j,k) are cluster indices in [0..numClusters-1]
     */
    protected abstract double updatedDistance(int k);

    protected double getDist(int a, int b) {
        return distance[alias[a]][alias[b]];
    }

    protected void init(final DistanceMatrix distanceMatrix) {

        numClusters = distanceMatrix.getSize();
        clusters = new Node[numClusters];

        distance = new double[numClusters][numClusters];
        for (int i = 0; i < numClusters; i++) {
            for (int j = 0; j < numClusters; j++) {
                distance[i][j] = distanceMatrix.getDistance(i, j);
                assert (!Double.isNaN(distance[i][j]));
            }
        }

        final List<Taxon> taxa = distanceMatrix.getTaxa();
        for (int i = 0; i < numClusters; i++) {
            clusters[i] = createExternalNode(taxa.get(i));
        }

        alias = new int[numClusters];
        tipCount = new int[numClusters];

        for (int i = 0; i < numClusters; i++) {
            alias[i] = i;
            tipCount[i] = 1;
        }
    }

    /**
     * Find next two clusters to join. set shared best{i,j}.
     */

    protected void findNextPair() {
        besti = 0;
        bestj = 1;
        double dmin = getDist(0, 1);
        for (int i = 0; i < numClusters-1; i++) {
            for (int j = i+1; j < numClusters; j++) {
                final double dist = getDist(i, j);
                if (dist < dmin) {
                    dmin = dist;
                    besti = i;
                    bestj = j;
                }
            }
        }
    }

    protected void newCluster() {
        double[] d = joinClusters();
        Node[] n = { clusters[abi], clusters[abj] };
        newCluster = createInternalNode(n, d);

        clusters[abi] = newCluster;
        clusters[abj] = null;

        // Update distances
        for (int k = 0; k < numClusters; k++) {
            if (k != besti && k != bestj) {
                int ak = alias[k];
                distance[ak][abi] = distance[abi][ak] = updatedDistance(k);
                distance[ak][abj] = distance[abj][ak] = -1.0;
            }
        }
        distance[abi][abi] = 0.0;
        distance[abj][abj] = -1.0;

        // Update alias
        System.arraycopy(alias, bestj + 1, alias, bestj, numClusters - 1 - bestj);

        tipCount[abi] += tipCount[abj];
        tipCount[abj] = 0;

        numClusters--;
    }

    protected void finish() {
        distance = null;
    }

    final protected DistanceMatrix distanceMatrix;

    // Number of current clusters
    protected int numClusters;

    // length numClusters
    protected Node[] clusters;
    protected Node newCluster;

    // Indices of two clusters in [0 .. numClusters-1], besti < bestj
    protected int besti, bestj;

    //  Actual index of besti,bestj into arrays (clusters,tipCount,distance)
    private int abi, abj;

    // Number of tips in cluster
    protected int[] tipCount;

    // Convert from cluster number to index in arrays
    protected int[] alias;

    // Distance between clusters
    protected double[][] distance;

    protected int numberOfRootSubtrees;
}