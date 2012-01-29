package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.util.FixedBitSet;

import java.util.*;

/**
 * Date: 5/03/2006
 * Time: 09:40:18
 *
 * @author Joseph Heled
 * @version $Id: GreedyRootedConsensusTreeBuilder.java 616 2007-01-09 21:55:13Z pepster $
 *          <p/>
 *          Implementation shares some code with GreedyUnrootedConsensusTreeBuilder (which preceded it), and perhaps I will
 *          find a way to merge the two at a later stage when I have the time.
 */
public class GreedyRootedConsensusTreeBuilder extends ConsensusTreeBuilder<RootedTree> {
    /**
     * Set of trees.
     */
    private final RootedTree[] rtrees;


    /**
     * Consensus contains only clades having at least that amount of support in set. Traditionally 50%
     */
    private final double supportThreshold;

    public GreedyRootedConsensusTreeBuilder(RootedTree[] trees, double supportThreshold) {
        super(trees);
        this.rtrees = trees;
        this.supportThreshold = supportThreshold;
    }

	public GreedyRootedConsensusTreeBuilder(RootedTree[] trees, double supportThreshold, String supportAttributeName, boolean asPercent) {
	    super(trees, supportAttributeName, asPercent);
	    this.rtrees = trees;
	    this.supportThreshold = supportThreshold;
	}

    public String getMethodDescription() {
        String supporDescription = getSupportDescription(supportThreshold);
        return supporDescription + " greedy clustering";
    }

    /**
     * One clade support.
     */
    static final class Support {
        /**
         * number of trees containing the clade.
         */
        private int nTreesWithClade;
        /**
         * Sum of node heights of trees containing the clade.
         */
        private double sumBranches;

        Support() {
            sumBranches = 0.0;
            nTreesWithClade = 0;
        }

        public final void add(double height) {
            sumBranches += height;
            ++nTreesWithClade;
        }
    }

    private final boolean debug = false;

    private String tipsAsText(FixedBitSet b) {
        String names = "(";
        for (int i = b.nextOnBit(0); i >= 0; i = b.nextOnBit(i + 1)) {
            names = names + taxons.get(i).getName() + ",";
        }
        return names + ")";
    }

    private FixedBitSet rootedSupport(RootedTree tree, Node node, Map<FixedBitSet, Support> support) {
        FixedBitSet clade = new FixedBitSet(nExternalNodes);
        if (tree.isExternal(node)) {
            clade.set(taxons.indexOf(tree.getTaxon(node)));
        } else {
            for (Node n : tree.getChildren(node)) {
                FixedBitSet childClade = rootedSupport(tree, n, support);
                clade.union(childClade);
            }
        }

        Support s = support.get(clade);
        if (s == null) {
            s = new Support();
            support.put(clade, s);
        }
        s.add(Utils.safeNodeHeight(tree, node));
        return clade;
    }

    /**
     * Make sure subtree below node has consistent heights, i.e. node height is higher than it's descendants
     *
     * @param tree
     * @param node
     * @return height of node
     */
    private double insureConsistency(MutableRootedTree tree, Node node) {
        double height = Utils.safeNodeHeight(tree, node);
        if (tree.isExternal(node)) {
            return height;
        } else {
            for (Node n : tree.getChildren(node)) {
                final double childHeight = insureConsistency(tree, n);
                height = Math.max(height, childHeight);
            }
        }

        tree.setHeight(node, height);
        return height;
    }


    public final RootedTree build() {

        // establish support
        Map<FixedBitSet, Support> support = new HashMap<FixedBitSet, Support>();
        int k = 0;
        for (RootedTree tree : rtrees) {
            if (debug) {
                System.out.println("Tree: " + Utils.DEBUGsubTreeRep(tree, tree.getRootNode()));
            }
            rootedSupport(tree, tree.getRootNode(), support);

            ++k;
            if (fireSetProgress( (0.9 * k) / rtrees.length)) {
		        return null;
	        }
        }

        final int nTrees = rtrees.length;

        MutableRootedTree consTree = new MutableRootedTree();

        // Contains all internal nodes in the tree so far, ordered so descendants
        // appear later than ancestors
        List<Node> internalNodes = new ArrayList<Node>(nExternalNodes);

        // For each internal node, a bit-set with the complete set of tips for it's clade
        List<FixedBitSet> internalNodesTips = new ArrayList<FixedBitSet>(nExternalNodes);
        assert taxons.size() == nExternalNodes;

        // establish a tree with one root having all tips as descendants
        internalNodesTips.add(new FixedBitSet(nExternalNodes));
        FixedBitSet rooNode = internalNodesTips.get(0);
        Node[] nodes = new Node[nExternalNodes];
        for (int nt = 0; nt < taxons.size(); ++nt) {
            nodes[nt] = consTree.createExternalNode(taxons.get(nt));
            rooNode.set(nt);
        }

        internalNodes.add(consTree.createInternalNode(Arrays.asList(nodes)));

        // sorts support from largest to smallest
        final Comparator<Map.Entry<FixedBitSet, Support>> comparator = new Comparator<Map.Entry<FixedBitSet, Support>>() {
            public int compare(Map.Entry<FixedBitSet, Support> o1, Map.Entry<FixedBitSet, Support> o2) {
                return o2.getValue().nTreesWithClade - o1.getValue().nTreesWithClade;
            }
        };

        // add everything to queue
        PriorityQueue<Map.Entry<FixedBitSet, Support>> queue =
                new PriorityQueue<Map.Entry<FixedBitSet, Support>>(support.size(), comparator);

        for (Map.Entry<FixedBitSet, Support> se : support.entrySet()) {
            Support s = se.getValue();
            FixedBitSet clade = se.getKey();
            final int cladeSize = clade.cardinality();
            if (cladeSize == nExternalNodes) {
                // root
                consTree.setHeight(consTree.getRootNode(), s.sumBranches / nTrees);
                continue;
            }

            if (s.nTreesWithClade == nTrees && cladeSize == 1) {
                // leaf/external node
                final int nt = clade.nextOnBit(0);
                final Node leaf = consTree.getNode(taxons.get(nt));
                consTree.setHeight(leaf, s.sumBranches / nTrees);
            } else {
                queue.add(se);
            }

	        if (fireSetProgress(0.95)) {
		        return null;
	        }
        }

        while (queue.peek() != null) {
            Map.Entry<FixedBitSet, Support> e = queue.poll();
            final Support s = e.getValue();

            final double psupport = (1.0 * s.nTreesWithClade) / nTrees;
            if (psupport < supportThreshold) {
                break;
            }

            final FixedBitSet cladeTips = e.getKey();

            if (debug) {
                System.out.println(100.0 * psupport + " Split: " + cladeTips + " " + tipsAsText(cladeTips));
            }

            boolean found = false;

            // locate the node containing the clade. going in reverse order insures the lowest one is hit first
            for (int nsub = internalNodesTips.size() - 1; nsub >= 0; --nsub) {

                FixedBitSet allNodeTips = internalNodesTips.get(nsub);

                // size of intersection between tips & split
                final int nSplit = allNodeTips.intersectCardinality(cladeTips);

                if (nSplit == cladeTips.cardinality()) {
                    // node contains all of clade

                    // Locate node descendants containing the split
                    found = true;
                    List<Integer> split = new ArrayList<Integer>();

                    Node n = internalNodes.get(nsub);
                    int l = 0;
                    List<Node> children = consTree.getChildren(n);
                    for (Node ch : children) {
                        if (consTree.isExternal(ch)) {
                            if (cladeTips.contains(taxons.indexOf(consTree.getTaxon(ch)))) {
                                split.add(l);
                            }
                        } else {
                            // internal
                            final int o = internalNodes.indexOf(ch);
                            final int i = internalNodesTips.get(o).intersectCardinality(cladeTips);
                            if (i == internalNodesTips.get(o).cardinality()) {
                                split.add(l);
                            } else if (i > 0) {
                                // Non compatible
                                found = false;
                                break;
                            }
                        }
                        ++l;
                    }


                    if (! (found && split.size() < children.size())) {
                        found = false;
                        break;
                    }

                    if (split.size() == 0) {
                        System.out.println("Bug??");
                        assert(false);
                    }

                    final Node detached = consTree.detachChildren(n, split);
                    final double height = s.sumBranches / s.nTreesWithClade;
                    consTree.setHeight(detached, height);

                    detached.setAttribute(getSupportAttributeName(), isSupportAsPercent() ? 100 * psupport : psupport);

                    if (debug) {
                        System.out.println("detached:" + Utils.DEBUGsubTreeRep(consTree, detached) + " len " + height + " sup " + psupport);
                        System.out.println("tree: " + Utils.toNewick(consTree));
                    }

                    // insert just after parent, so before any descendants
                    internalNodes.add(nsub + 1, detached);
                    internalNodesTips.add(nsub + 1, new FixedBitSet(cladeTips));

                    break;
                }
            }

            if (psupport >= .5 && ! found) {
                System.out.println("Bug??");
                assert(false);
            }

	        if (fireSetProgress(0.99)) {
		        return null;
	        }
        }

        insureConsistency(consTree, consTree.getRootNode());
        fireSetProgress(1.0);
        return consTree;
    }
}
