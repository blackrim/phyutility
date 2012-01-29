package jebl.evolution.trees;

import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.*;

/**
 * Root an unrooted tree. This class works as a wrapper over any tree to root it. There are two
 * constructors, one which roots the tree at any internal node, the other roots the tree between any two
 * internal nodes. Be aware that rooting between nodes where one of them has less than 3 adjacencies may
 * be problematic when converting back from the Newick format.
 *
 * @author Joseph Heled
 * @version $Id: RootedFromUnrooted.java 627 2007-01-15 03:50:40Z pepster $
 *
 */

public class RootedFromUnrooted implements RootedTree {
    /**
     * The unrooted tree
     */
    private Tree source;

    /**
     * Root of rooted tree. Either an existing internal node or a new "synthetic" node.
     */
    private Node root;
    /**
     * Maps each nodes to its parent.
     */
    private Map<Node, Node> parents;

    /**
     *  Children of the synthetic root (when rooted between nodes)
     */
    private Node topLeft, topRight;
    /**
     * branch lengths from synthetic root to its children (when rooted between nodes)
     */
    private double rootToLeft, rootToRight;
    private boolean intentUnrooted;

    /**
     * Set <arg>parent</arg> as parent of <arg>node</arg>, and recursivly set parents for node subtree
     * (whose root is parent)
     * @param node
     * @param parent
     */
    private void setParent(Node node, Node parent) {
        parents.put(node, parent);
        for( Node adj : source.getAdjacencies(node) ) {
            if( adj != parent && ! (node == topLeft && adj == topRight) && !(node == topRight && adj == topLeft) ) {
                setParent(adj, node);
            }
        }
    }

    /**
     * Root tree at some internal node.
     *
     * @param source tree to root
     * @param root  internal node to root at
     * @param intentUnrooted
     */
    public RootedFromUnrooted(Tree source, Node root, boolean intentUnrooted) {
        this.source = source;
        this.root = root;
        this.intentUnrooted = intentUnrooted;
        topLeft = topRight  = null;
        rootToLeft = rootToRight = 0.0;
        parents = new HashMap<Node, Node>();
        for( Node adj : source.getAdjacencies(root) ) {
            setParent(adj, root);
        }
    }

    /**
     * Root source by creating a new internal node whose children are (the adjacent) left and right.
     * @param source
     * @param left
     * @param right
     * @param fromLeft branch from new root to left node.
     */
    public RootedFromUnrooted(Tree source, Node left, Node right, double fromLeft) {
        this.source = source;
        intentUnrooted = false;
        topLeft = left;
        topRight = right;
        rootToLeft = fromLeft;
        try {
            rootToRight = source.getEdgeLength(left, right) - rootToLeft;
        } catch (NoEdgeException e) {
            // bug
        }
        parents = new HashMap<Node, Node>();

        // This is just a handle used to refer to the root so create the simplest possible implementation...
        root = new BaseNode() { public int getDegree() { return 0; } };

        parents.put(root, null);
        setParent(left, root);
        setParent(right, root);
    }

    public List<Node> getChildren(Node node) {
        ArrayList<Node> s = new ArrayList<Node>(getAdjacencies(node));
        if( node != root ) {
            s.remove(getParent(node));
        }
        return s;
    }

    public boolean hasHeights() {
        return false;
    }

    private double findNodeHeightFromTips(Node node) {
        if( isExternal(node) ) return 0.0;

        double h = 0.0;
        for( Node n : getChildren(node) ) {
            h = Math.max(h, getLength(n) + findNodeHeightFromTips(n));
        }
        return h;
    }

    public double getHeight(Node node) {
        double hr = findNodeHeightFromTips(root);
        if( node == root ) {
            return hr;
        }

        double toRoot = 0.0;
        while( node != root ) {
            toRoot += getLength(node);
            node = getParent(node);
        }
        return hr - toRoot;
    }

    public boolean hasLengths() {
        return true;
    }

    public double getLength(Node node) {
        if( node == root ) return 0.0;
        if( node == topLeft ) return rootToLeft;
        if( node == topRight ) return rootToRight;
        double l = 0.0;
        try {
            l = source.getEdgeLength(node, getParent(node));
        } catch (NoEdgeException e) {
            // bug, should not happen
        }
        return l;
    }

    public Node getParent(Node node) {
        return parents.get(node);
    }

    public Node getRootNode() {
        return root;
    }

    public boolean conceptuallyUnrooted() {
        return intentUnrooted;
    }

    public Set<Node> getExternalNodes() {
        return source.getExternalNodes();
    }

    public Set<Node> getInternalNodes() {
        HashSet<Node> s = new HashSet<Node>(source.getInternalNodes());
        s.add(root);
        return s;
    }

    public Set<Taxon> getTaxa() {
        return source.getTaxa();
    }

    public Taxon getTaxon(Node node) {
        return source.getTaxon(node);
    }

    public boolean isExternal(Node node) {
        return node != root && source.isExternal(node);
    }

    public Node getNode(Taxon taxon) {
        return source.getNode(taxon);
    }

    public void renameTaxa(Taxon from, Taxon to) {
        source.renameTaxa(from, to);
    }

    /**
     * Returns a list of edges connected to this node
     *
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public List<Edge> getEdges(Node node) {
        return source.getEdges(node);
    }

	public Node[] getNodes(Edge edge) {
	    return source.getNodes(edge);
	}

    public List<Node> getAdjacencies(Node node) {
        // special case when syntetic root
        if( topLeft != null ) {
            if( node == root ) {
                Node[] d = {topLeft, topRight};
                return Arrays.asList(d);
            }
            if( node == topLeft || node == topRight ) {
                List<Node> s = new ArrayList<Node>(source.getAdjacencies(node));
                s.remove(node == topLeft ? topRight : topLeft);
                s.add(root);
                return s;
            }
        }
        return source.getAdjacencies(node);
    }

    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        // special case when syntetic root
        if( topLeft != null ) {
            if( node2 == root ) {
                Node tmp = node1;
                node1 = node2;
                node2 = tmp;
            }
            if( node1 == root ) {
                if( ! (node2 == topLeft || node2 == topRight) ) {
                    throw new NoEdgeException();
                }
                return node2 == topLeft ? rootToLeft : rootToRight;
            }
        }
        return source.getEdgeLength(node1, node2);
    }

    public Edge getEdge(Node node1, Node node2) throws NoEdgeException {
        return source.getEdge(node1, node2);
    }

    public Set<Node> getNodes() {
        Set<Node> nodes = new HashSet<Node>(getInternalNodes());
        nodes.addAll(getExternalNodes());
        if( topLeft != null ) {
            nodes.add(root);
        }
        return nodes;
    }

    /**
     * @return the set of all edges in this graph.
     */
    public Set<Edge> getEdges() {
        return source.getEdges();
    }

	/**
	 * The set of external edges.
	 * @return the set of external edges.
	 */
	public Set<Edge> getExternalEdges() {
		return source.getExternalEdges();
	}

	/**
	 * The set of internal edges.
	 * @return the set of internal edges.
	 */
	public Set<Edge> getInternalEdges() {
		return source.getInternalEdges();
	}

    public Set<Node> getNodes(int degree) {
        Set<Node> nodes = source.getNodes(degree);
        if( degree == 2 ) {
            nodes.add(root);
        }
        return nodes;
    }

	public boolean isRoot(Node node) {
		return node == root;
	}

    // Attributable IMPLEMENTATION

    public void setAttribute(String name, Object value) {
        source.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        return source.getAttribute(name);
    }

    public void removeAttribute(String name) {
        source.removeAttribute(name);
    }

    public Set<String> getAttributeNames() {
        return source.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        return source.getAttributeMap();
    }
}