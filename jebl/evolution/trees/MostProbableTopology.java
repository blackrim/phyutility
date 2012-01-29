package jebl.evolution.trees;

import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.NexusExporter;
import jebl.evolution.taxa.Taxon;
import jebl.util.FixedBitSet;

import java.util.*;

/**
 *
 * Given a set of trees determine the most probable trees, i.e. the most frequent topologies.
 * Set branch lengths / node heights from set conditional on topology.
 *
 * @author Joseph Heled
 * @version $Id: MostProbableTopology.java 626 2007-01-14 20:30:21Z pepster $
 */
public class MostProbableTopology {
    /**
     * Set of input trees
     */
    final List<Tree> trees;

    /**
     * Rooted or non-rooted set
     */
    final boolean rootedSet;

    /**
     * Common taxa for all trees. Order in this array is used as the common base for the trees.
     */
    final private List<Taxon> taxa;

    final String consAttributeName = GreedyUnrootedConsensusTreeBuilder.DEFAULT_SUPPORT_ATTRIBUTE_NAME;

    /**
     *
     * @param trees
     */
    public MostProbableTopology(Collection<? extends Tree> trees) {
        this.trees = new ArrayList<Tree>(trees);
        Tree tree0 = this.trees.get(0);
        rootedSet = tree0 instanceof RootedTree && !((RootedTree)tree0).conceptuallyUnrooted();
        taxa = new ArrayList<Taxon>(trees.iterator().next().getTaxa());
    }

    /**
     * Entry for each unique topology in the set
     */
    private class TopologyEntry {
        // number of trees with this topology
        int count;

        // Index of one tree with topology
        int representativeIndex;

        /**
         *
         * @param nTree index of representative tree
         */
        public TopologyEntry(int nTree) {
            representativeIndex = nTree;
            count = 1;
        }
    }

    /**
     * Callback for iterator over nodes of a rooted tree
     */
    private interface NodeCallback {
        /**
         *
         * @param node visited node
         * @param tipSet Taxa in sub-tree beneath node. A set bit in the n'th position means taxa[n] is in.
         */
        void visit(Node node, FixedBitSet tipSet);
    }

    /**
     * Callback for iterator over edges of an unrooted tree
     */
    private interface EdgeCallback {
        /**
         *
         * @param edge visited edge
         * @param tipSet Taxa in one bi-partition when this edge is deleted.
         * A set bit in the n'th position means taxa[n] is in. Normalized so that the first (0'th) tip is always in.
         */
        void visit(Edge edge, FixedBitSet tipSet);
    }

    /**
     * Branch length / Node height data collected from the set of trees.
     *
     * Simply the average of the value in trees containing the sub-tree / partition.
     */
    private class ConditionalData {
        // accumulating sum
        double hSum;

        // number of trees contributing to sum
        int count;

        public ConditionalData() {
            hSum = 0.0;
            count = 0;
        }

        // add one more
        public void add(double v) {
            hSum += v;
            ++count;
        }

        // get current estimate
        public double length() {
            assert( count > 0);
            return hSum / count;
        }
    }

    /**
     * Entry for one probable tree.
     *
     * Used to collect conditional estimate of branch/node data from the set of trees.
     * Base for rooted/unrooted cases.
     */
    private interface Info {
        // Get the tree
        Tree getTree();

        // Using accumalated data, set branches/heights
        void setBranches();
    }

    /**
     * Helper in traversing unrooted trees.
     */
    private class TraversableTree {
        // the tree
        final Tree t;

        TraversableTree(Tree t) {
            this.t = t;
        }

        // Traberse the tree, calling call.visit for every edge.
        void traverse(EdgeCallback call) {
            final Node tip = t.getExternalNodes().iterator().next();
            traverse(call, t.getAdjacencies(tip).get(0), tip );
        }

        // Traverse all edges in partition containing n when edge (n, root) is removed.

        private FixedBitSet traverse(EdgeCallback call, Node n, Node root) {
            final FixedBitSet tipSet = new FixedBitSet(taxa.size());

            if( t.isExternal(n) ) {
                tipSet.set( taxa.indexOf( t.getTaxon(n) ) );
            } else {
                for( Node c : t.getAdjacencies(n) ) {
                    if( c == root ) continue;
                    final FixedBitSet cTips = traverse(call, c, n);
                    tipSet.union(cTips);
                }
            }

            final boolean needComplement = !tipSet.contains(0);
            if( needComplement )  {
                tipSet.complement();
            }

            try {
                call.visit( t.getEdge(n, root), tipSet);
            } catch (Graph.NoEdgeException e) {
                assert false;
            }

            // we can't simply flip back because map does not copy it's key.
            // second time I forget this
            if( needComplement ) {
                final FixedBitSet b = new FixedBitSet(tipSet);
                b.complement();
                return b;
            }
            return tipSet;
        }
    }

    private class UnrootedTreeInfo extends TraversableTree implements Info {
        public Tree getTree() {
            return t;
        }

        /**
         * Data for one edge
         */
        class EdgeInfo extends ConditionalData {
            Edge e;

            public EdgeInfo(Edge e) {
                super();
                this.e = e;
            }
        }

        // All edges, indexed the normalized partition when edge is removed (normalized == containing tip
        // of taxa[0].

        final public Map<FixedBitSet, EdgeInfo> m;

        public UnrootedTreeInfo(SimpleTree t) {
            super(t);
            m = new HashMap<FixedBitSet, EdgeInfo>();
            traverse(new EdgeCallback() {
                public void visit(Edge e, FixedBitSet tipSet) {
                     m.put(tipSet, new EdgeInfo(e));
                }
            } );
        }

        public void setBranches() {
            traverse(new EdgeCallback() {
                public void visit(Edge e, FixedBitSet tipSet) {
                    final EdgeInfo info = m.get(tipSet);                            assert(info != null);
                    final double h = info.length();

                    ((SimpleTree)t).setEdgeLength(info.e, h);

                    final double support = (100.0 * info.count) / trees.size();
                    info.e.setAttribute(consAttributeName, support);
                }
            } );
        }
    }

    /**
     * Helper in traversing rooted trees.
     */
    class TraversableRootedTree {
        final public RootedTree t;

        TraversableRootedTree(RootedTree t) {
            this.t = t;
        }

        // Traberse the tree, calling call.visit for every node.
        void traverse(NodeCallback call) {
            traverse(call, t.getRootNode());
        }

        // Traverse the subtree below n
        private FixedBitSet traverse(NodeCallback call, Node n) {
            final FixedBitSet tipSet = new FixedBitSet(taxa.size());

            if( t.isExternal(n) ) {
                tipSet.set( taxa.indexOf( t.getTaxon(n) ) );
            } else {
                for( Node c : t.getChildren(n) ) {
                    final FixedBitSet cTips = traverse(call, c);
                    tipSet.union(cTips);
                }
            }
            call.visit(n, tipSet);
            return tipSet;
        }
    }

    private class TreeInfo extends TraversableRootedTree implements Info {
         /**
         * Data for one node.
         */
        class NodeInfo extends ConditionalData {
            Node n;

            public NodeInfo(Node n) {
                super();
                this.n = n;
            }
        }

        // All nodes, indexed the set of tips in subtree below node
        final public Map<FixedBitSet, NodeInfo> m;

        TreeInfo(SimpleRootedTree t) {
            super(t);
            m = new HashMap<FixedBitSet, NodeInfo>();
            traverse(new NodeCallback() {
                public void visit(Node n, FixedBitSet tipSet) {
                     m.put(tipSet, new NodeInfo(n));
                }
            } );
        }

        public Tree getTree() {
            return t;
        }

        public void setBranches() {
            traverse(new NodeCallback() {
                public void visit(Node n, FixedBitSet tipSet) {
                    final NodeInfo info = m.get(tipSet);                                  assert(info != null);
                    double h = info.length();
                    for( Node c : t.getChildren(info.n) ) {
                        final double ch = t.getHeight(c);
                        if( ch > h ) {
                            h = ch;
                        }
                    }
                    ((SimpleRootedTree)t).setHeight(info.n, h);

                    info.n.setAttribute(consAttributeName,
                            (100.0 * info.count) / trees.size());
                }
            } );
        }
    }

    /**
     * Get the most probable tree(s)
     *
     * @param max At most this number of trees (max <= 0 is ignored)
     * @param threshold (in [01]) return first K topologies whose total frequencey is greater that threshold.
     * @return probable trees
     */
    public List<Tree> get(final int max, final double threshold) {
        final int nTrees = trees.size();
        // Generate a "standard" representation for each tree topology. For rooted trees this is the newick format
        // (leaving out any branch information), where the children at each node are sorted.
        // Unrooted trees are rooted at the internal node connected to the first tip (taxa[0]) and the rooted
        // method is applied to that.

        Map<String, TopologyEntry> m = new HashMap<String, TopologyEntry>(nTrees);
        for(int nTree = 0; nTree < nTrees; ++ nTree) {
            final Tree t = trees.get(nTree);
            final String rep = standardTopologyRepresentation(t);

            TopologyEntry e = m.get(rep);
            if( e == null ) {
                m.put(rep, new TopologyEntry(nTree));
            } else {
                e.count += 1;
            }
        }

        // sorts support from largest to smallest
        final Comparator<Map.Entry<String, TopologyEntry>> comparator = new Comparator<Map.Entry<String, TopologyEntry>>() {
            public int compare(Map.Entry<String, TopologyEntry> o1, Map.Entry<String, TopologyEntry> o2) {
                return o2.getValue().count - o1.getValue().count;
            }
        };

        // add everything to queue
        PriorityQueue<Map.Entry<String, TopologyEntry>> queue =
                new PriorityQueue<Map.Entry<String, TopologyEntry>>(m.size(), comparator);

        for (Map.Entry<String, TopologyEntry> s : m.entrySet()) {
            queue.add(s);
        }

        // collect candidates
        final List<Info> candidates = new ArrayList<Info>();

        //final int th = threshold > 0 ? (int)(threshold * nTrees) : 1;
        final int th = (int)(threshold * nTrees);

        while (queue.peek() != null && candidates.size() <= th && !(max > 0 && candidates.size() >= max) ) {
            Map.Entry<String, TopologyEntry> e = queue.poll();
            final MostProbableTopology.TopologyEntry info = e.getValue();

            // make a copy
            final Tree tree = trees.get(info.representativeIndex);

            Info candidate;
            if( rootedSet ) {
                final SimpleRootedTree r = new SimpleRootedTree((RootedTree) tree);
                candidate = new TreeInfo(r);
            } else {
                final SimpleTree t = new SimpleTree(tree);
                candidate = new UnrootedTreeInfo(t);
            }

            candidates.add(candidate);
            final Tree tree1 = candidate.getTree();
            tree1.setAttribute("Frequency", (100.0 * info.count) / nTrees);
            tree1.setAttribute(NexusExporter.treeNameAttributeKey, "topology_" + candidates.size());
        }

        // Now go over the set of trees, and for each node/edge record the value in all
        // candidate trees containng that node/edge.

        for(int nTree = 0; nTree < nTrees; ++ nTree) {
            if( rootedSet ) {
                final RootedTree t = (RootedTree)trees.get(nTree);
                new TraversableRootedTree(t).traverse(new NodeCallback() {
                    public void visit(Node n, FixedBitSet tipSet) {
                        final double height = t.getHeight(n);
                        for( Info ti : candidates ) {
                            final TreeInfo.NodeInfo ni = ((TreeInfo)ti).m.get(tipSet);

                            if( ni != null ) {
                                ni.add(height);
                            }
                        }
                    }
                } );
            } else {

                final Tree t = trees.get(nTree);
                new TraversableTree(t).traverse(new EdgeCallback() {
                    public void visit(Edge e, FixedBitSet tipSet) {
                        final double length = e.getLength();
                        for( Info ti : candidates ) {
                            final UnrootedTreeInfo.EdgeInfo ei = ((UnrootedTreeInfo)ti).m.get(tipSet);

                            if( ei != null ) {
                                ei.add(length);
                            }
                        }
                    }
                } );
            }
        }

        // Set heights/lengths from accumulated information
        List<Tree> results = new ArrayList<Tree>();
        for (final Info info : candidates) {
            info.setBranches();

            results.add(info.getTree());
        }
        return results;
    }

    // "Standard" string representation
    private String standardTopologyRepresentation(Tree t) {
        if( t instanceof RootedTree ) {
            final RootedTree r = (RootedTree) t;
            return standardTop(r, r.getRootNode());
        }

        // unrooted tree. Root at internal node near first taxa, call rooted method
        final List<Node> adj = t.getAdjacencies(t.getNode(taxa.get(0)));           assert( adj.size() == 1 );
        final RootedTree r = new RootedFromUnrooted(t, adj.get(0), true);
        return standardTop(r, r.getRootNode());
    }

    // "Standard" string representation of a rooted tree
    // Newick for taxa only, where children are sorted.
    private String standardTop(RootedTree t, Node n) {
        if( t.isExternal(n) ) {
            return Integer.toString(taxa.indexOf( t.getTaxon(n) ));
        }
        final List<Node> dec = t.getChildren(n);
        final String[] strings = new String[dec.size()];
        for(int k = 0; k < dec.size(); ++k) {
          strings[k] = standardTop(t, dec.get(k));
        }
        final List<String> list = Arrays.asList(strings);
        Collections.sort(list);
        StringBuilder sb = new StringBuilder();

        for( String s : strings ) {           
            sb.append(sb.length() == 0  ? '(' : ",");
            sb.append(s);
        }
        sb.append(')');
        return sb.toString();
    }
}
