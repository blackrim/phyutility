package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.util.FixedBitSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Construct a consensus tree for a set of rooted trees. The construction is done via clustering. For any two
 * clusters/clades, their distance is the height of their most recent common ancesstor (computed as an average over all
 * trees). This seem natural as it connectes clades in reverse time order.
 *
 * @author Joseph Heled
 * @version $Id: MRCACConsensusTreeBuilder.java 616 2007-01-09 21:55:13Z pepster $
 **/

public class MRCACConsensusTreeBuilder extends ConsensusTreeBuilder<RootedTree> {
    RootedTree[] trees;
    private double supportThreshold;
    private List<FixedBitSet> tipsInCluster;

    private TreeInfo[] info;

    private boolean debug = false;

	public MRCACConsensusTreeBuilder(Tree[] trees, double supportThreshold) {
	    this(trees, supportThreshold, DEFAULT_SUPPORT_ATTRIBUTE_NAME, true);
	}

    public MRCACConsensusTreeBuilder(Tree[] trees, double supportThreshold, String supportAttributeName, boolean asPercent) {
        super(trees, supportAttributeName, asPercent);
        this.trees = new RootedTree[trees.length];
        for(int i = 0; i < trees.length; ++i) {
            this.trees[i] = (RootedTree)trees[i];
        }
        this.supportThreshold = supportThreshold;

        info = new TreeInfo[trees.length];
        for(int iTree = 0; iTree < trees.length; ++iTree) {
            info[iTree] = new TreeInfo(this.trees[iTree]);
        }
    }

    public RootedTree build() {
        return earliestCommonAncestorClustering(supportThreshold);
    }

    public String getMethodDescription() {
        return getSupportDescription(supportThreshold) + " MRCACC";
    }


    class TreeInfo {
        // for each tree, establish a postorder order, and in each internal node the subsets of decendentants
        FixedBitSet[] nodesTipSet;

        int[] postorder;
        int[][] nodeChildren;

        private Node[] allNodes;
        TreeInfo(RootedTree tree) {

            allNodes = new Node[tree.getNodes().size()];

            for( Node node : tree.getExternalNodes() ) {
                int i = taxons.indexOf( tree.getTaxon(node) );
                allNodes[i] = node;
            }

            int k = nExternalNodes;
            for( Node node : tree.getInternalNodes() ) {
                allNodes[k] = node;
                ++k;
            }

            postorder = new int[allNodes.length - nExternalNodes];
            nodesTipSet = new FixedBitSet[allNodes.length];
            nodeChildren = new int [allNodes.length][];

            inPostorder(tree, tree.getRootNode(), 0);
        }

        private int inPostorder(RootedTree tree, Node node, int nPost) {
            List<Node> all = Arrays.asList(allNodes);
            FixedBitSet b = new FixedBitSet(allNodes.length);
            final int c = all.indexOf(node);
            if( tree.isExternal(node) ) {
                b.set(c);
            } else {

                List<Node> children = tree.getChildren(node);
                nodeChildren[c] = new int[children.size()];
                int nc = 0;
                for( Node child : children ) {
                    nPost = inPostorder(tree, child, nPost);
                    int c1 = all.indexOf(child);
                    nodeChildren[c][nc] = c1;
                    ++nc;
                    b.union(nodesTipSet[c1]);
                }
                postorder[nPost] = c;
                ++nPost;
            }
            nodesTipSet[c] = b;
            return nPost;
        }
    }

    // Height of minimal clade containing clusters i&j (i < j) is height[i][j].
    // (Only upper right of matrix is used)
    double[][] height;

    void setupPairs() {
        height = new double [nExternalNodes][nExternalNodes];

        if( debug )  {
            for(int k = 0; k < taxons.size(); ++k) {
                System.out.print(taxons.get(k).getName() + ":" + k + ", ");
            }
            System.out.println();
        }

        for(int iTree = 0; iTree < trees.length; ++iTree) {
            if( debug ) System.out.println("tree " + Utils.toNewick(trees[iTree]));

            TreeInfo info = this.info[iTree];
            for (final int nodeIndex : info.postorder) {
                final int leftChildIndex = info.nodeChildren[nodeIndex][0];
                final int rightChildIndex = info.nodeChildren[nodeIndex][1];
                final FixedBitSet left = info.nodesTipSet[leftChildIndex];
                final FixedBitSet right = info.nodesTipSet[rightChildIndex];

                for (int lTip = left.nextOnBit(0); lTip >= 0; lTip = left.nextOnBit(lTip + 1)) {
                    for (int rTip = right.nextOnBit(0); rTip >= 0; rTip = right.nextOnBit(rTip + 1)) {

                        final double h = trees[iTree].getHeight(info.allNodes[nodeIndex]);
                        height[Math.min(lTip, rTip)][Math.max(lTip, rTip)] += h;
                    }
                }
            }
        }

        for(int d = 0; d < nExternalNodes; ++d) {
            for(int d1 = d+1; d1 < nExternalNodes; ++d1) {
                height[d][d1] /= trees.length;
            }
        }
    }

    private double[] updateHeights(int i, int j) {
        final int nClusters = tipsInCluster.size();
        double[] distances = new double[nClusters +1];

        // tips in clustes i & j
        FixedBitSet joined = new FixedBitSet(tipsInCluster.get(i));
        joined.union(tipsInCluster.get(j));

        int numberOfTreesSupportClade = 0;

        if( nClusters == 2 ) {
            for (RootedTree tree : trees) {
                distances[1] += tree.getHeight(tree.getRootNode());
            }
            distances[2] = 1;
        }   else {
            for(int l = 0; l < nClusters; ++l) {
                if( (l == i || l == j) ) {
                    continue;
                }

                // Tips in i, j & l
                FixedBitSet joinedWithL = new FixedBitSet(joined);
                joinedWithL.union(tipsInCluster.get(l));

                for(int iTree = 0; iTree < trees.length; ++iTree) {
                    RootedTree tree = trees[iTree];
                    TreeInfo info = this.info[iTree];
                    boolean commonAnncestorFound = false;

                    for (int nodeIndex : info.postorder) {
                        FixedBitSet nodeBS = info.nodesTipSet[nodeIndex];

                        if (!commonAnncestorFound && joined.setInclusion(nodeBS)) {

                            final int tipsInSubtree = nodeBS.cardinality();
                            final int tipsInClusters = joined.cardinality();

                            numberOfTreesSupportClade += ((tipsInSubtree == tipsInClusters) ? 1 : 0);
                            commonAnncestorFound = true;
                        }

                        if( joinedWithL.setInclusion(nodeBS) ) {
                            distances[l] += tree.getHeight(info.allNodes[nodeIndex]);
                            break;
                        }
                    }
                }
            }
        }

        for(int l = 0; l < nClusters; ++l) {
            distances[l] /= trees.length;
        }

        distances[nClusters] = (double)numberOfTreesSupportClade / trees.length;
        return distances;
    }

    private RootedTree earliestCommonAncestorClustering(double supportThreshold) {

        MutableRootedTree consensus = new MutableRootedTree();
        List<Node> subTrees = new ArrayList<Node>(nExternalNodes);

        // compute average length of branch from tip over all trees
        double[] tipHeights = new double[taxons.size()];
        for (RootedTree tree : trees) {
            for (Node e : tree.getExternalNodes()) {
                final int i = taxons.indexOf(tree.getTaxon(e));
                tipHeights[i] += tree.getHeight(e);
            }
        }

        // Start with each tip in it's own cluster
        tipsInCluster = new ArrayList<FixedBitSet>(nExternalNodes);
        for(int k = 0; k < nExternalNodes; ++k) {
            FixedBitSet b = new FixedBitSet(nExternalNodes);
            b.set(k);
            tipsInCluster.add(b);
            final Node externalNode = consensus.createExternalNode(taxons.get(k));
            consensus.setHeight(externalNode, tipHeights[k]/trees.length);
            subTrees.add(externalNode);
        }

        // set up distances between all tips
        setupPairs();

        for(int nClusters = nExternalNodes; nClusters > 1; --nClusters) {

            // Find most recent ancesstor of two clusters from distance matrix
            double mostRecentAnncestorHeight = Double.MAX_VALUE;
            int besti = 0, bestj = 0;
            for(int d = 0; d < nClusters; ++d) {
                for(int d1 = d+1; d1 < nClusters; ++d1) {
                    if( height[d][d1] < mostRecentAnncestorHeight ) {
                        mostRecentAnncestorHeight = height[d][d1];
                        besti = d; bestj = d1;
                    }
                }
            }

            // Join clusters
            double[] newHeights = updateHeights(besti, bestj);
            double supportForClade = newHeights[newHeights.length-1];

            final Node[] children = {subTrees.get(besti), subTrees.get(bestj)};
            final double maxChild = Math.max(consensus.getHeight(children[0]), consensus.getHeight(children[1]));
            mostRecentAnncestorHeight = Math.max(mostRecentAnncestorHeight, maxChild);

            final Node sub = consensus.createInternalNode(Arrays.asList(children));

            consensus.setHeight(sub, mostRecentAnncestorHeight);
            // root always 100%
            if( nClusters > 2 ) {
	            sub.setAttribute(getSupportAttributeName(), isSupportAsPercent() ? 100 * supportForClade : supportForClade);
            }
            subTrees.set(besti, sub);
            tipsInCluster.get(besti).union(tipsInCluster.get(bestj));
            tipsInCluster.remove(bestj);
            subTrees.remove(bestj);

            // compress distance matrix
            for(int i = 0; i < nClusters; ++i) {
                if( besti < i ) {
                    height[besti][i] = newHeights[i];
                } else {
                    height[i][besti] = newHeights[i];
                }
            }

            for(int i = 0; i < nClusters-1; ++i) {
                for(int j = bestj; j < nClusters-1; ++j) {
                    height[i][j] = height[i + (i >=bestj ? 1 : 0)][j+1];
                }
            }
        }

        // Remove nodes with low support
        if (isSupportAsPercent()) {
            supportThreshold *= 100;
        }
        for( Node node : consensus.getInternalNodes() ) {
            Object sup = node.getAttribute(getSupportAttributeName());
            if( sup != null && (Double)sup < supportThreshold ) {
                consensus.removeInternalNode(node);
            }
        }
        return consensus;
    }
}
