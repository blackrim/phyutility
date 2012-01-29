package jebl.evolution.trees;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.FixedBitSet;

import java.util.*;

/**
 * Builds greedy consensus tree given a set of unrooted trees.
 * <p/>
 * Each edge in a tree "supports" a split, i.e. the partition of the taxa into two clades
 * (which you get by deleting the edge). A Majority consensus tree is built by finding
 * clades appearing in at least 50% of the trees or more. A Greedy consensus tree is a
 * refinement of a Majority tree where the splits are sorted by amount of support, and are
 * applied in order only if they are compatible (consistent) with the tree at that
 * stage. A user supplied threshold gives a lower bound on amount of support.  If set t0
 * 50% the Greedy method reduces to the majority consensus tree. At 100% it reduces to the
 * Strict consensus tree.
 * <p/>
 * The implementation is relatively simple but tricky in parts. Each tree is scanned, and
 * support for each split/clade is collected in one table. The clade is represented by a
 * bitset, which always contains the (arbitrary) first node. The scan is made by going
 * over all nodes ordered in such a way that the subtree of exactly one edge of the node
 * has been completely scanned, so the node "knows" the set of tips of that subtree without
 * needing to re-scan the tree.
 * <p/>
 * After all trees are scanned an initial consensus tree is constructed with one root and
 * all tips as children. The split set is scanned in order of decreasing support, and each
 * supported clade refines the tree by creating a new descendant for the node containing
 * the clade and re-attaching the clade to that new node. This is done only if the split
 * is compatible with the tree, i.e.  only if the split is completely contained in a proper
 * subset of descendants of one node. This process continues until only clades with support
 * lower that the threshold are left.
 * <p/>
 * The length of the consensus tree branches is computed from the average over all trees
 * containing the clade. The lengths of tip branches are computed by averaging over all
 * trees.
 * <p/>
 * While the consensus tree is logically unrooted, we generate a rooted tree because we
 * can store attributes such as support only for nodes.
 *
 * @author Joseph Heled
 * @version $Id: GreedyUnrootedConsensusTreeBuilder.java 616 2007-01-09 21:55:13Z pepster $
 */

public final class GreedyUnrootedConsensusTreeBuilder extends ConsensusTreeBuilder<Tree> {
    /**
     * Set of trees.
     */
    private final Tree[] trees;

    /**
     * Outgroup, if any. Currently used only for display purposes.
     */
    private final Taxon outGroup;

    /**
     * Consensus contains only clades having at least that amount of support in set. Traditionally 50%
     */
    private final double supportThreshold;

	public GreedyUnrootedConsensusTreeBuilder(Tree[] trees, Taxon outGroup, double supportThreshold) {
	    super(trees);
	    this.trees = trees;
	    this.outGroup = outGroup;
	    this.supportThreshold = supportThreshold;
	}

    GreedyUnrootedConsensusTreeBuilder(Tree[] trees, Taxon outGroup, double supportThreshold, String supportAttributeName, boolean asPercent) {
        super(trees, supportAttributeName, asPercent);
        this.trees = trees;
        this.outGroup = outGroup;
        this.supportThreshold = supportThreshold;
    }

    public String getMethodDescription() {
        return getSupportDescription(supportThreshold) + " greedy clustering";
    }

    /**
     * One clade support.
     */
    static final class Support {
        /**
         * number of trees containing this clade.
         */
        private int nTreesWithClade;
        /**
         * Sum of branch length separating clade from the rest of taxa (in trees containing the clade).
         */
        private double sumBranches;

        Support() {
            sumBranches = 0.0;
            nTreesWithClade = 0;
        }

        public final void add(double branch) {
            sumBranches += branch;
            ++nTreesWithClade;
        }
    }

    private final boolean debug = false;

    // debug
    private String subTreeRep(Tree t, Node n, Node root) {

        if (t.isExternal(n)) {
            return t.getTaxon(n).getName();
        }
        StringBuilder b = new StringBuilder();
        for (Node x : t.getAdjacencies(n)) {
            if (x == root) continue;
            if (b.length() > 0) b.append(",");
            b.append(subTreeRep(t, x, n));
        }
        return '(' + b.toString() + ')';
    }

    private String tipsAsText(FixedBitSet b) {
        String names = "(";
        for (int i = b.nextOnBit(0); i >= 0; i = b.nextOnBit(i + 1)) {
            names = names + taxons.get(i).getName() + ",";
        }
        return names + ")";
    }

    // Scan method:
    //
    // loop on all trees t
    //   establish support :
    //    for e : external nodes of t - add adjacencies of e to scan set and add e to done set
    //
    //    while n in scan set:
    //       if all adj of n done - remove n from scan. add n to done. continue
    //       if all adj of n sans one are done:
    //          get set of subtree tips from done nodes
    //          add new split with branch length to support set
    //          add not done adj to scan list
    //          remove n from scan and add it to done list

    public final Tree build() {

        try {
            Map<FixedBitSet, Support> support = new HashMap<FixedBitSet, Support>();
            double[] sumBranchesOfExternal = new double[taxons.size()];

            int nTree = 0;
            for (Tree tree : trees) {
                int initialCapacity = tree.getNodes().size();
                Set<Node> scanSet = new LinkedHashSet<Node>(initialCapacity);
                Map<Node, FixedBitSet> doneSet = new HashMap<Node, FixedBitSet>(initialCapacity);

                for (Node n : tree.getExternalNodes()) {
                    FixedBitSet b = new FixedBitSet(nExternalNodes);
                    final int position = taxons.indexOf(tree.getTaxon(n));
                    b.set(position);

                    if (debug)
                        System.out.print(taxons.indexOf(tree.getTaxon(n)) + ":" + tree.getTaxon(n).getName() + " ");
                    doneSet.put(n, b);
                    for (Node a : tree.getAdjacencies(n)) {
                        scanSet.add(a);
                    }
                    sumBranchesOfExternal[position] += tree.getEdgeLength(n, tree.getAdjacencies(n).get(0));
                }

                int nInternalEdges = nExternalNodes - 3;

                List<Node> intr = new ArrayList<Node>(tree.getInternalNodes());

                if (debug) System.out.println("\ntree " + Utils.toNewick(Utils.rootTheTree(tree)));

                while (scanSet.size() > 0) {
                    Set<Node> nextScanSet = new LinkedHashSet<Node>(initialCapacity);

                    for (Node n : scanSet) {
                        if (debug) System.out.println("scan " + intr.indexOf(n));
                        int nDone = 0;
                        List<Node> adjacencies = tree.getAdjacencies(n);
                        for (Node a : adjacencies) {
                            if (doneSet.containsKey(a)) ++nDone;
                        }

                        if (nDone + 1 < adjacencies.size()) {
                            if (debug) System.out.println("add to next " + intr.indexOf(n));
                            nextScanSet.add(n);
                            continue;
                        }

                        if (nDone < adjacencies.size()) {

                            FixedBitSet b = new FixedBitSet(nExternalNodes);
                            Node notDone = null;
                            for (Node a : adjacencies) {
                                if (doneSet.containsKey(a)) {
                                    FixedBitSet subSet = doneSet.get(a);
                                    if (subSet == null) {
                                        if (debug) System.out.println(a + " " + subTreeRep(tree, n, notDone));
                                        assert(false);
                                    }
                                    b.union(subSet);
                                } else {
                                    notDone = a;
                                }
                            }

                            final double branch;

                            branch = tree.getEdgeLength(n, notDone);

                            doneSet.put(n, new FixedBitSet(b));
                            // in case it has been added by a previous node
                            nextScanSet.remove(n);
                            // support keys always contains the (arbitrary) tip 0
                            if (! b.contains(0)) {
                                b.complement();
                            }
                            Support s = support.get(b);
                            if (s == null) {
                                s = new Support();
                                support.put(b, s);
                            }
                            if (debug) {
                                System.out.println("add " + b + "<" + subTreeRep(tree, n, notDone) + ">"
                                        + " " + s.nTreesWithClade + "/" + s.sumBranches + " " + branch);
                            }

                            s.add(branch);
                            --nInternalEdges;

                            if (debug) System.out.println("add to next " + intr.indexOf(notDone));

                            nextScanSet.add(notDone);
                        } else {
                            if (debug) {
                                for (Node x : tree.getAdjacencies(n)) {
                                    System.out.println(subTreeRep(tree, x, n) + " is done " + intr.indexOf(n));
                                }
                            }
                            doneSet.put(n, null);
                        }
                    }

                    scanSet = nextScanSet;
                }
                if (debug) System.out.println(nInternalEdges);

                 ++nTree;
                if( fireSetProgress((0.9 * nTree)/ trees.length) ) {
                    return null;
                }
            }

            // sorts support from largest to smallest
            final Comparator<Map.Entry<FixedBitSet, Support>> comparator = new Comparator<Map.Entry<FixedBitSet, Support>>() {
                public int compare(Map.Entry<FixedBitSet, Support> o1, Map.Entry<FixedBitSet, Support> o2) {
                    return o2.getValue().nTreesWithClade - o1.getValue().nTreesWithClade;
                }
            };

            // add everything to queue
            PriorityQueue<Map.Entry<FixedBitSet, Support>> queue =
                    new PriorityQueue<Map.Entry<FixedBitSet, Support>>(support.size(), comparator);

            for (Map.Entry<FixedBitSet, Support> s : support.entrySet()) {
                queue.add(s);
            }

            MutableRootedTree consTree = new MutableRootedTree();

            // Contains all internal nodes in the tree so far, ordered so descendants
            // appear later than ancestors
            List<Node> internalNodes = new ArrayList<Node>(nExternalNodes);

            // For each internal node, a bit set with the complete set of tips for it's clade
            List<FixedBitSet> internalNodesTips = new ArrayList<FixedBitSet>(nExternalNodes);
            assert taxons.size() == nExternalNodes;

            // establish a tree with one root having all tips as descendants
            internalNodesTips.add(new FixedBitSet(nExternalNodes));
            Node[] nodes = new Node[nExternalNodes];
            for (int nt = 0; nt < taxons.size(); ++nt) {
                nodes[nt] = consTree.createExternalNode(taxons.get(nt));
                internalNodesTips.get(0).set(nt);
            }

            internalNodes.add(consTree.createInternalNode(Arrays.asList(nodes)));

            while (queue.peek() != null) {
                Map.Entry<FixedBitSet, Support> e = queue.poll();
                final Support s = e.getValue();

                final double psupport = (1.0 * s.nTreesWithClade) / trees.length;

                if (psupport < supportThreshold) {
                    break;
                }

                final FixedBitSet splitTips = e.getKey();

                if (debug) {
                    System.out.println(100.0 * psupport + " Split: " + splitTips + " "
                            + tipsAsText(splitTips) + "/" + tipsAsText(FixedBitSet.complement(splitTips)));
                }

                boolean found = false;

                // locate the node containing the split. going in reverse order insures the lowest one is hit first
                for (int nsub = internalNodesTips.size() - 1; nsub >= 0; --nsub) {
                    // size of intersection between tips & split
                    final int nSplit = internalNodesTips.get(nsub).intersectCardinality(splitTips);

                    FixedBitSet allNodeTips = internalNodesTips.get(nsub);
                    if (nSplit > 0 && nSplit < allNodeTips.cardinality()) {
                        // if split is actually with complement of arbitrary representation - use complement
                        FixedBitSet sharedTips = new FixedBitSet(allNodeTips);
                        sharedTips.intersect(splitTips);
                        if (! sharedTips.equals(splitTips)) {
                            sharedTips.complement();
                            sharedTips.intersect(allNodeTips);
                            if (! sharedTips.equals(FixedBitSet.complement(splitTips))) {
                                continue;
                            }
                        }

                        // Locate node descendants containing the split
                        found = true;
                        List<Integer> split = new ArrayList<Integer>();

                        Node n = internalNodes.get(nsub);
                        int l = 0;
                        List<Node> children = consTree.getChildren(n);
                        for (Node ch : children) {
                            if (consTree.isExternal(ch)) {
                                if (sharedTips.contains(taxons.indexOf(consTree.getTaxon(ch)))) {
                                    split.add(l);
                                }
                            } else {
                                // internal
                                int o = internalNodes.indexOf(ch);
                                int i = internalNodesTips.get(o).intersectCardinality(sharedTips);
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

                        Node detached = consTree.detachChildren(n, split);
                        final double length = s.sumBranches / s.nTreesWithClade;
                        consTree.setLength(detached, length);

	                    detached.setAttribute(getSupportAttributeName(), isSupportAsPercent() ? 100 * psupport : psupport);

                        if (debug) {
                            System.out.println("detached:" + subTreeRep(consTree, detached, n) + " len " + length + " sup " + psupport);
                            System.out.println("tree: " + Utils.toNewick(consTree));
                        }

                        // insert just after parent, so before any descendants
                        internalNodes.add(nsub + 1, detached);
                        internalNodesTips.add(nsub + 1, new FixedBitSet(sharedTips));

                        break;
                    }
                }

                if (psupport >= .5 && ! found) {
                    System.out.println("Bug??");
                    assert(false);
                }
            }

            // establish length for tips
            for (int nt = 0; nt < taxons.size(); ++nt) {
                final Node n = consTree.getNode(taxons.get(nt));
                consTree.setLength(n, sumBranchesOfExternal[nt] / trees.length);
            }

            if (outGroup != null) {
                Node out = consTree.getNode(outGroup);
                Set<String> a = new HashSet<String>();
                a.add(getSupportAttributeName());
                consTree.reRootWithOutgroup(out, a);
            }

            consTree.setConceptuallyUnrooted(true);

            fireSetProgress(1.0);

            return consTree;
        } catch (Graph.NoEdgeException e) {
            // bug
        }
        return null;
    }
}
