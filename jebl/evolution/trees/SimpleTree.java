package jebl.evolution.trees;

import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.*;

/**
 * A basic implementation on an unrooted tree.
 *
 * @author Joseph Heled
 * @version $Id: SimpleTree.java 687 2007-04-10 01:09:24Z stevensh $
 *
 */

public final class SimpleTree implements Tree {

    /**
     * Tree (to be constructed by subsequent calls).
     */
    public SimpleTree() {}

    /**
     *  Duplicate a tree.
     *
     * @param tree
     */
    public SimpleTree(Tree tree) {
        try {
            createTree(tree, tree.getExternalNodes().iterator().next(), null);
        } catch (NoEdgeException e) {
            throw new IllegalArgumentException("BUG: invalid tree");
        }
    }

    /**
     * Creates a new external node with the given taxon. See createInternalNode
     * for a description of how to use these methods.
     * @param taxon the taxon associated with this node
     * @return the created node reference
     */
    public Node createExternalNode(Taxon taxon) {
        SimpleNode node = new SimpleNode(taxon);
        externalNodes.put(taxon, node);
        return node;
    }

    /**
     * Once a SimpleTree has been created, the node stucture can be created by
     * calling createExternalNode and createInternalNode. First of all createExternalNode
     * is called giving Taxon objects for the external nodes. Then these are put into
     * sets and passed to createInternalNode to create new internal nodes.
     *
     * It is the caller responsibility to insure no cycles are created.
     *
     * @param adjacencies the child nodes of this node
     * @return the created node.
     */
    public Node createInternalNode(List<Node> adjacencies) {
        SimpleNode node = new SimpleNode(adjacencies);

        internalNodes.add(node);

        for( Node c : adjacencies ) {
            ((SimpleNode)c).addAdjacency(node);
        }
        return node;
    }

    /**
     * Set edge distance between two adjacent nodes.
     * @param node1
     * @param node2
     * @param length
     */
    public void setEdgeLength(final Node node1, final Node node2, final double length) {
        assert getAdjacencies(node1).contains(node2) && getAdjacencies(node2).contains(node1) && length >= 0;

        final Edge edge = new SimpleEdge(node1, node2, length);

        edges.put(new HashPair<Node>(node1, node2), edge);
        edges.put(new HashPair<Node>(node2, node1), edge);
    }

    /**
     * Change length of an existing edge.
     * @param edge
     * @param length
     */
    public void setEdgeLength(final Edge edge, final double length) {
       ((SimpleEdge)edge).length = length;
    }

    /**
     * Add a new edge between two existing (non adjacent yet)  nodes.
     * @param node1
     * @param node2
     * @param length
     */
    public void addEdge(Node node1, Node node2, double length) {
        assert !getAdjacencies(node1).contains(node2);

        ((SimpleNode)node1).addAdjacency(node2);
        ((SimpleNode)node2).addAdjacency(node1);
        setEdgeLength(node1, node2, length);
    }

    /**
     * Copy partition of source starting from node but skipping root.
     *
     * @param source to copy from
     * @param node  in source to copy
     * @param root  adjacent node already copied
     * @return copy of node inside tree
     * @throws NoEdgeException
     */
    private Node createTree(Tree source, Node node, Node root) throws NoEdgeException {
        Node h;
        if( source.isExternal(node) ) {
            h = createExternalNode(source.getTaxon(node));
        } else {
            h = createInternalNode(new ArrayList<Node>() );
        }

        final List<Node> adjacencies = source.getAdjacencies(node);

        for( Node c : adjacencies ) {
            if( c == root ) continue;
            final Node n = createTree(source, c, node);
            addEdge(h, n, source.getEdgeLength(node, c) );
        }
        return h;
    }

    /* Graph IMPLEMENTATION */

    /**
     * Returns a list of edges connected to this node
     *
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public List<Edge> getEdges(Node node) {
        //return null;
        List<Node> adjacencies = getAdjacencies(node);
        List<Edge> edges = new ArrayList<Edge>();
        for(Node adjNode : adjacencies){
            try{
                edges.add(getEdge(node,adjNode));
            }
            catch(NoEdgeException ex){/*do nothing*/}
        }
        return edges;
    }

    /**
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public List<Node> getAdjacencies(Node node) {
        return ((SimpleNode)node).getAdjacencies();
    }

    /**
     * Returns the Edge that connects these two nodes
     *
     * @param node1
     * @param node2
     * @return the edge object.
     * @throws jebl.evolution.graphs.Graph.NoEdgeException
     *          if the nodes are not directly connected by an edge.
     */
    public Edge getEdge(Node node1, Node node2) throws NoEdgeException {
        Edge edge = edges.get(new HashPair<Node>(node1, node2));
        if( edge == null ) {
            // not connected
            throw new NoEdgeException();
        }
        return edge;
    }

    /**
     * @return a set of all nodes that have degree 1.
     *         These nodes are often refered to as 'tips'.
     */
    public Set<Node> getExternalNodes() {
        return new HashSet<Node>(externalNodes.values());
    }

    /**
     * @return a set of all nodes that have degree 2 or more.
     *         These nodes are often refered to as internal nodes.
     */
    public Set<Node> getInternalNodes() {
        return new HashSet<Node>(internalNodes);
    }

	/**
	 * @return the set of taxa associated with the external
	 *         nodes of this tree. The size of this set should be the
	 *         same as the size of the external nodes set.
	 */
	public Set<Taxon> getTaxa() {
	    return new HashSet<Taxon>(externalNodes.keySet());
	}
    /**
     * @param node the node whose associated taxon is being requested.
     * @return the taxon object associated with the given node, or null
     *         if the node is an internal node.
     */
    public Taxon getTaxon(Node node) {
        return ((SimpleNode)node).getTaxon();
    }

    /**
     * @param node the node
     * @return true if the node is of degree 1.
     */
    public boolean isExternal(Node node) {
        return node.getDegree() == 1;
    }

	/**
	 * @param edge the edge
	 * @return true if the edge has a node of degree 1.
	 */
	public boolean isExternal(Edge edge) {
	    return ((SimpleEdge)edge).isExternal();
	}

    /**
     * @param taxon the taxon
     * @return the external node associated with the given taxon, or null
     *         if the taxon is not a member of the taxa set associated with this tree.
     */
    public Node getNode(Taxon taxon) {
        return externalNodes.get(taxon);
    }

    public void renameTaxa(Taxon from, Taxon to) {
        SimpleNode node = (SimpleNode)externalNodes.get(from);
        node.setTaxa(to);
    }

    /**
     * @param node1
     * @param node2
     * @return the length of the edge connecting node1 and node2.
     * @throws NoEdgeException if the nodes are not directly connected by an edge.
     */
    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        return getEdge(node1, node2).getLength();
    }

	/**
	 * Returns an array of 2 nodes which are the nodes at either end of the edge.
	 *
	 * @param edge
	 * @return an array of 2 edges
	 */
	public Node[] getNodes(Edge edge) {
		return new Node[] { ((SimpleEdge)edge).getNode1(), ((SimpleEdge)edge).getNode2() };
	}

	/**
	 * @return the set of all nodes in this graph.
	 */
	public Set<Node> getNodes() {
	    Set<Node> nodes = new HashSet<Node>(internalNodes);
	    nodes.addAll(externalNodes.values());
	    return nodes;
	}

    /**
     * @return the set of all edges in this graph.
     */
    public Set<Edge> getEdges() {
        return new HashSet<Edge>(edges.values());
    }

    /**
     * @param degree the number of edges connected to a node
     * @return a set containing all nodes in this graph of the given degree.
     */
    public Set<Node> getNodes(int degree) {
        Set<Node> nodes = new HashSet<Node>();
        for (Node node : getNodes()) {
            if (((SimpleNode)node).getDegree() == degree) nodes.add(node);
        }
        return nodes;
    }

	/**
	 * The set of external edges. This is a pretty inefficient implementation because
	 * a new set is constructed each time this is called.
	 * @return the set of external edges.
	 */
	public Set<Edge> getExternalEdges() {
		Set<Edge> externalEdges = new HashSet<Edge>();
		for (Edge edge : getEdges()) {
			if (((SimpleEdge)edge).isExternal()) {
 				externalEdges.add(edge);
			}
		}
		return externalEdges;
	}

	/**
	 * The set of internal edges. This is a pretty inefficient implementation because
	 * a new set is constructed each time this is called.
	 * @return the set of internal edges.
	 */
	public Set<Edge> getInternalEdges() {
		Set<Edge> internalEdges = new HashSet<Edge>();
		for (Edge edge : getEdges()) {
			if (!((SimpleEdge)edge).isExternal()) {
 				internalEdges.add(edge);
			}
		}
		return internalEdges;
	}

    // Attributable IMPLEMENTATION

    public void setAttribute(String name, Object value) {
        if (helper == null) {
            helper = new AttributableHelper();
        }
        helper.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        if (helper == null) {
            return null;
        }
        return helper.getAttribute(name);
    }

    public void removeAttribute(String name) {
        if( helper != null ) {
            helper.removeAttribute(name);
        }
    }

    public Set<String> getAttributeNames() {
        if (helper == null) {
            return Collections.emptySet();
        }
        return helper.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        if (helper == null) {
            return Collections.emptyMap();
        }
        return helper.getAttributeMap();
    }

    // PRIVATE members

    private AttributableHelper helper = null;
    private final Set<Node> internalNodes = new HashSet<Node>();
    private final Map<Taxon, Node> externalNodes = new HashMap<Taxon, Node>();
    /**
     * A mapping between edges and edge length.
     */
    Map<HashPair, Edge> edges = new HashMap<HashPair, Edge>();

    final class SimpleNode extends BaseNode {

        /**
         * A tip having a taxon
         * @param taxon
         */
        private SimpleNode(Taxon taxon) {
            this.adjacencies = Collections.unmodifiableList(new ArrayList<Node>());
            this.taxon = taxon;
        }

        /**
         * An internal node.
         * @param adjacencies set of adjacent noeds
         */
        private SimpleNode(List<Node> adjacencies) {
            this.adjacencies = Collections.unmodifiableList(adjacencies);
            this.taxon = null;
        }

        /**
         * Add an adjacency.
         * @param node
         */
        public void addAdjacency(Node node) {
            List<Node> a = new ArrayList<Node>(adjacencies);
            a.add(node);
            adjacencies = Collections.unmodifiableList(a);
        }

        public Taxon getTaxon() {
            return taxon;
        }

        public int getDegree() {
            return (adjacencies == null ? 0 : adjacencies.size());
        }

        public List<Node> getAdjacencies() {
            return adjacencies;
        }
      
        public void setTaxa(Taxon to) {
            taxon = to;
        }

        // PRIVATE members
        private List<Node> adjacencies;
        private Taxon taxon;

    }

	final class SimpleEdge extends BaseEdge {

		private SimpleEdge(Node node1, Node node2, double length) {
			this.node1 = node1;
			this.node2 = node2;
			this.length = length;
		}

		public Node getNode1() {
			return node1;
		}

		public Node getNode2() {
			return node2;
		}

		public double getLength() {
			return length;
		}

		private boolean isExternal() {
			return (node1.getDegree() == 1 || node2.getDegree() == 1);
		}

		private double length;
		Node node1, node2;
	}
}