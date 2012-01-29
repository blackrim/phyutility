package jebl.evolution.trees;

import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.*;

/**
 * A simple rooted tree providing some ability to manipulate the tree.
 *
 *   - Root an unrooted tree using an outgroup.
 *   - Remove internal node: all children of node are adopted by it's parent.
 *   - Split/Refine node by creating two new children and distributing the children to new nodes.
 *   - Re-root a rooted tree given an outgroup.

 * @author Joseph Heled
 * @version $Id: MutableRootedTree.java 627 2007-01-15 03:50:40Z pepster $
 *
 */

public class MutableRootedTree implements RootedTree {
    MutableRootedTree() {  super(); }

    /**
     * Construct a rooted tree from unrooted.
     *
     * @param tree      Unrooted tree to root
     * @param outGroup  Node in tree assumed to be the outgroup
     */
    public MutableRootedTree(Tree tree, Node outGroup) {
        if( ! tree.isExternal(outGroup) ) throw new IllegalArgumentException("Outgroup must be a tip");

        // Adjacency of node to become new root.
        Node root = tree.getAdjacencies(outGroup).get(0);

        try {
            MutableRootedNode newSubtreeRoot = rootAdjaceincesWith(tree, root, outGroup);

            
            // Add the outgroup in
            MutableRootedNode out = (MutableRootedNode)createExternalNode( tree.getTaxon(outGroup) );
            setLength(out, tree.getEdgeLength(outGroup, root));
            // Create new root
            ArrayList<MutableRootedNode> rootChildren = new ArrayList<MutableRootedNode>();
            rootChildren.add(out);
            rootChildren.add(newSubtreeRoot);
            //MutableRootedNode newRoot = 
            	this.createInternalNode( rootChildren );
            setLength(newSubtreeRoot,0);	
        } catch (NoEdgeException e) {
            // bug
        }
    }
    
  
    /**
     *  Remove internal node. Move all children to their grandparent.
     *  @param node  to be removed
     */
    public void removeInternalNode(Node node) {
        assert ! isExternal(node) && getRootNode() != node;

        MutableRootedNode parent = (MutableRootedNode)getParent(node);
        for( Node n : getChildren(node) ) {
            parent.addChild((MutableRootedNode)n);
        }
        parent.removeChild(node);
        internalNodes.remove(node);
    }

    /**
     *
     * @param node     Node to refine
     * @param leftSet  indices of children in the left new subtree.
     */
    public void refineNode(Node node, int[] leftSet) {
        List<Node> allChildren = getChildren(node);

        List<Node> left = new ArrayList<Node>();
        List<Node> right = new ArrayList<Node>();

        for( int n : leftSet ) {
            left.add(allChildren.get(n));
        }
        for( Node n : allChildren ) {
            if( !left.contains(n) ) {
                right.add(n);
            }
        }
        internalNodes.remove(node);
        MutableRootedNode saveRoot = rootNode;

        MutableRootedNode lnode = (left.size() > 1) ? createInternalNode(left) : (MutableRootedNode)left.get(0);
        MutableRootedNode rnode = (right.size() > 1) ? createInternalNode(right) : (MutableRootedNode)right.get(0);

        List<MutableRootedNode> nodes = new ArrayList<MutableRootedNode>(2);
        nodes.add(lnode);
        nodes.add(rnode);
        ((MutableRootedNode)node).replaceChildren(nodes);

        rootNode = saveRoot;
    }

    /**
     *  Re-root tree using an outgroup.
     * @param outGroup
     * @param attributeNames Move those attributes (if they exist in node) to their previous parent. The idea is to
     * preserve "branch" attributes which we now store in the child since only "node" properties are supported.
     */
    public void reRootWithOutgroup(Node outGroup, Set<String> attributeNames) {
        assert isExternal(outGroup);
        reRoot((MutableRootedNode)getAdjacencies(outGroup).get(0), attributeNames);
    }

    /**
     * Construct a rooted sub-tree from unrooted. Done recursivly: Given an internal node N and one adjacency A to become
     * the new parent, recursivly create subtrees for all adjacencies of N (ommiting A) using N as parent, and return
     * an internal node with all subtrees as children. A tip simply creates an external node and returns it.
     *
     * @param tree   Unrooted source tree
     * @param node   span sub-tree from this node
     * @param parent adjacency of node which serves as the parent.
     * @return  rooted subtree.
     * @throws NoEdgeException
     */
    private MutableRootedNode rootAdjaceincesWith(Tree tree, Node node, Node parent) throws NoEdgeException {
        if( tree.isExternal(node) ) {
            return (MutableRootedNode)createExternalNode( tree.getTaxon(node) );
        }

        List<Node> children = new ArrayList<Node>();
        for( Node adj : tree.getAdjacencies(node) ) {
            if( adj == parent ) continue;
            MutableRootedNode rootedAdj = rootAdjaceincesWith(tree, adj, node);
            setLength(rootedAdj, tree.getEdgeLength(adj, node));
            children.add(rootedAdj);
        }
        return createInternalNode(children);
    }

    /**
     * Similar to  rootAdjaceincesWith.
     * @param node
     * @param attributeNames
     */
    private void reRoot(MutableRootedNode node, Set<String> attributeNames) {
        MutableRootedNode parent = (MutableRootedNode)getParent(node);
        if( parent == null) {
            return;
        }
        double len = getLength(node);
        parent.removeChild(node);
        reRoot(parent, attributeNames);
        if( parent == getRootNode() ) {
            rootNode = node;
        }

        if( parent.getChildren().size() == 1 ) {
            parent = (MutableRootedNode)parent.getChildren().get(0);
            len += parent.getLength();
        }

        node.addChild(parent);
        parent.setLength(len);
        node.setParent(null);

        if( attributeNames != null ) {
            for( String name : attributeNames ) {
                Object s = node.getAttribute(name);
                if( s != null ) {
                    parent.setAttribute(name, s);
                    node.removeAttribute(name);
                }
            }
        }
    }

    public Node detachChildren(Node node, List<Integer> split) {
        assert( split.size() > 1 );

        List<Node> allChildren = getChildren(node);

        List<Node> detached = new ArrayList<Node>();

        for( int n : split ) {
            detached.add(allChildren.get(n));
        }

        MutableRootedNode saveRoot = rootNode;

        for( Node n : allChildren ) {
            if( detached.contains(n) ) {
                ((MutableRootedNode)node).removeChild(n);
            }
        }

        MutableRootedNode dnode = createInternalNode(detached);
        ((MutableRootedNode)node).addChild(dnode);

        rootNode = saveRoot;

        return dnode;
    }

    /**
     * Creates a new external node with the given taxon. See createInternalNode
     * for a description of how to use these methods.
     * @param taxon the taxon associated with this node
     * @return the created node reference
     */
    public Node createExternalNode(Taxon taxon) {
        MutableRootedNode node = new MutableRootedNode(taxon);
        externalNodes.put(taxon, node);
        return node;
    }

    /**
     * Once a SimpleRootedTree has been created, the node stucture can be created by
     * calling createExternalNode and createInternalNode. First of all createExternalNode
     * is called giving Taxon objects for the external nodes. Then these are put into
     * sets and passed to createInternalNode to create a parent of these nodes. The
     * last node created using createInternalNode is automatically the root so when
     * all the nodes are created, the tree is complete.
     *
     * @param children the child nodes of this nodes
     * @return the created node reference
     */
    public MutableRootedNode createInternalNode(List<? extends Node> children) {
        MutableRootedNode node = new MutableRootedNode(children);

        for (Node child : children) {
            ((MutableRootedNode)child).setParent(node);
        }

        internalNodes.add(node);

        rootNode = node;
        return node;
    }


    /**
     * @param node the node whose height is being set
     * @param height the height
     */
    public void setHeight(Node node, double height) {
        lengthsKnown = false;
        heightsKnown = true;

        // If a single height of a single node is set then
        // assume that all nodes have heights and by extension,
        // branch lengths as well as these will be calculated
        // from the heights
        hasLengths = true;
        hasHeights = true;

        ((MutableRootedNode)node).setHeight(height);
    }

    /**
     * @param node the node whose branch length (to its parent) is being set
     * @param length the length
     */
    public void setLength(Node node, double length) {
        heightsKnown = false;
        lengthsKnown = true;

        // If a single length of a single branch is set then
        // assume that all branch have lengths and by extension,
        // node heights as well as these will be calculated
        // from the lengths
        hasLengths = true;
        hasHeights = true;

        ((MutableRootedNode)node).setLength(length);
    }

    /**
     * @param node the node whose children are being requested.
     * @return the list of nodes that are the children of the given node.
     *         The list may be empty for a terminal node (a tip).
     */
    public List<Node> getChildren(Node node) {
        return new ArrayList<Node>(((MutableRootedNode)node).getChildren());
    }

    /**
     * @return Whether this tree has node heights available
     */
    public boolean hasHeights() {
        return hasHeights;
    }

    /**
     * @param node the node whose height is being requested.
     * @return the height of the given node. The height will be
     *         less than the parent's height and greater than it children's heights.
     */
    public double getHeight(Node node) {
        if (!hasHeights) throw new IllegalArgumentException("This tree has no node heights");
        if (!heightsKnown) calculateNodeHeights();
        return ((MutableRootedNode)node).getHeight();
    }

    /**
     * @return Whether this tree has branch lengths available
     */
    public boolean hasLengths() {
        return hasLengths;
    }

    /**
     * @param node the node whose branch length (to its parent) is being requested.
     * @return the length of the branch to the parent node (0.0 if the node is the root).
     */
    public double getLength(Node node) {
        if (!hasLengths) throw new IllegalArgumentException("This tree has no branch lengths");
        if (!lengthsKnown) calculateBranchLengths();
        return ((MutableRootedNode)node).getLength();
    }

    /**
     * @param node the node whose parent is requested
     * @return the parent node of the given node, or null
     *         if the node is the root node.
     */
    public Node getParent(Node node) {
        return ((MutableRootedNode)node).getParent();
    }

    /**
     * The root of the tree has the largest node height of
     * all nodes in the tree.
     *
     * @return the root of the tree.
     */
    public Node getRootNode() {
        return rootNode;
    }

	public boolean isRoot(Node node) {
		return node == rootNode;
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
        return ((MutableRootedNode)node).getTaxon();
    }

    /**
     * @param node the node
     * @return true if the node is of degree 1.
     */
    public boolean isExternal(Node node) {
        return ((MutableRootedNode)node).getChildren().size() == 0;
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
        MutableRootedNode node = (MutableRootedNode)externalNodes.get(from);
        node.setTaxa(to);
    }

    /**
     * Returns a list of edges connected to this node
     *
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public List<Edge> getEdges(Node node) {
        List<Edge> edges = new ArrayList<Edge>();
        for (Node adjNode : getAdjacencies(node)) {
            edges.add(((MutableRootedNode)adjNode).getEdge());

        }
        return edges;
    }

	/**
	 * Returns an array of 2 nodes which are the nodes at either end of the edge.
	 *
	 * @param edge
	 * @return an array of 2 edges
	 */
	public Node[] getNodes(Edge edge) {
		for (Node node : getNodes()) {
			if (((MutableRootedNode)node).getEdge() == edge) {
				return new Node[] { node, ((MutableRootedNode)node).getParent() };
			}
		}
		return null;
	}


    /**
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public List<Node> getAdjacencies(Node node) {
        return ((MutableRootedNode)node).getAdjacencies();
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
        if (((MutableRootedNode)node1).getParent() == node2) {
            return ((MutableRootedNode)node1).getEdge();
        } else if (((MutableRootedNode)node2).getParent() == node1) {
            return ((MutableRootedNode)node2).getEdge();
        } else {
            throw new NoEdgeException();
        }
    }

    /**
     * @param node1
     * @param node2
     * @return the length of the edge connecting node1 and node2.
     * @throws jebl.evolution.graphs.Graph.NoEdgeException
     *          if the nodes are not directly connected by an edge.
     */
    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        if (((MutableRootedNode)node1).getParent() == node2) {
            if (heightsKnown) {
                return ((MutableRootedNode)node2).getHeight() - ((MutableRootedNode)node1).getHeight();
            } else {
                return ((MutableRootedNode)node1).getLength();
            }
        } else if (((MutableRootedNode)node2).getParent() == node1) {
            if (heightsKnown) {
                return ((MutableRootedNode)node1).getHeight() - ((MutableRootedNode)node2).getHeight();
            } else {
                return ((MutableRootedNode)node2).getLength();
            }
        } else {
            throw new NoEdgeException();
        }
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
        Set<Edge> edges = new HashSet<Edge>();
        for (Node node : getNodes()) {
            if (node != getRootNode()) {
                edges.add(((MutableRootedNode)node).getEdge());
            }

        }
        return edges;
    }

	/**
	 * The set of external edges. This is a pretty inefficient implementation because
	 * a new set is constructed each time this is called.
	 * @return the set of external edges.
	 */
	public Set<Edge> getExternalEdges() {
		Set<Edge> edges = new HashSet<Edge>();
		for (Node node : getExternalNodes()) {
			edges.add(((MutableRootedNode)node).getEdge());
		}
		return edges;
	}

	/**
	 * The set of internal edges. This is a pretty inefficient implementation because
	 * a new set is constructed each time this is called.
	 * @return the set of internal edges.
	 */
	public Set<Edge> getInternalEdges() {
		Set<Edge> edges = new HashSet<Edge>();
		for (Node node : getInternalNodes()) {
			if (node != getRootNode()) {
			    edges.add(((MutableRootedNode)node).getEdge());
			}
		}
		return edges;
	}

    /**
     * @param degree the number of edges connected to a node
     * @return a set containing all nodes in this graph of the given degree.
     */
    public Set<Node> getNodes(int degree) {
        Set<Node> nodes = new HashSet<Node>();
        for (Node node : getNodes()) {
            // Account for no anncesstor of root, assumed by default in getDegree
            final int deg = ((MutableRootedNode)node).getDegree() - ((node == rootNode) ? 1 : 0);
            if (deg == degree) nodes.add(node);
        }
        return nodes;
    }

    /**
     * Set the node heights from the current branch lengths.
     */
    private void calculateNodeHeights() {

        if (!lengthsKnown) {
            throw new IllegalArgumentException("Can't calculate node heights because branch lengths not known");
        }

        nodeLengthsToHeights(rootNode, 0.0);

        double maxHeight = 0.0;
        for (Node externalNode : getExternalNodes()) {
            if (((MutableRootedNode)externalNode).getHeight() > maxHeight) {
                maxHeight = ((MutableRootedNode)externalNode).getHeight();
            }
        }

        for (Node node : getNodes()) {
            ((MutableRootedNode)node).setHeight(maxHeight - ((MutableRootedNode)node).getHeight());
        }

        heightsKnown = true;
    }

    /**
     * Set the node heights from the current node branch lengths. Actually
     * sets distance from root so the heights then need to be reversed.
     */
    private void nodeLengthsToHeights(MutableRootedNode node, double height) {

        double newHeight = height;

        if (node.getLength() > 0.0) {
            newHeight += node.getLength();
        }

        node.setHeight(newHeight);

        for (Node child : node.getChildren()) {
            nodeLengthsToHeights((MutableRootedNode)child, newHeight);
        }
    }

    /**
     * Calculate branch lengths from the current node heights.
     */
    protected void calculateBranchLengths() {

        if (!hasLengths) {
            throw new IllegalArgumentException("Can't calculate branch lengths because node heights not known");
        }

        nodeHeightsToLengths(rootNode, getHeight(rootNode));

        lengthsKnown = true;
    }

    /**
     * Calculate branch lengths from the current node heights.
     */
    private void nodeHeightsToLengths(MutableRootedNode node, double height) {
        final double h = node.getHeight();
        node.setLength(h >= 0 ? height - h : 1);

        for (Node child : node.getChildren()) {
            nodeHeightsToLengths((MutableRootedNode)child, node.getHeight());
        }

    }

    public void setConceptuallyUnrooted(boolean intent) {
        conceptuallyUnrooted = intent;
    }

    public boolean conceptuallyUnrooted() {
        return conceptuallyUnrooted;
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

    protected MutableRootedNode rootNode = null;
    protected final Set<Node> internalNodes = new HashSet<Node>();
    private final Map<Taxon, Node> externalNodes = new HashMap<Taxon, Node>();

    private boolean heightsKnown = false;
    private boolean lengthsKnown = false;

    private boolean hasHeights = false;
    private boolean hasLengths = false;

    private boolean conceptuallyUnrooted = false;

    private class MutableRootedNode extends BaseNode {
        public MutableRootedNode(Taxon taxon) {
            this.children = Collections.unmodifiableList(new ArrayList<Node>());
            this.taxon = taxon;
        }

        public MutableRootedNode(List<? extends Node> children) {
            this.children = Collections.unmodifiableList(new ArrayList<Node>(children));
            this.taxon = null;
        }


        public void removeChild(Node node) {
            List<Node> c = new ArrayList<Node>(children);
            c.remove(node);
            children = Collections.unmodifiableList(c);
        }

        public void addChild(MutableRootedNode node) {
            List<Node> c = new ArrayList<Node>(children);
            c.add(node);
            node.setParent(this);
            children = Collections.unmodifiableList(c);
        }

        public void replaceChildren(List<MutableRootedNode> nodes) {
            for( MutableRootedNode n : nodes ) {
                n.setParent(this);
            }
            children = Collections.unmodifiableList(new ArrayList<Node>(nodes));
        }


        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public List<Node> getChildren() {
            return children;
        }

        public double getHeight() {
            return height;
        }

        // height above latest tip
        public void setHeight(double height) {
            this.height = height;
        }

        // length of branch to parent
        public double getLength() {
            return length >= 0 ? length : 1.0;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public int getDegree() {
            return children.size() + 1;
        }

        /**
         * returns the edge connecting this node to the parent node
         * @return the edge
         */
        public Edge getEdge() {
            if (edge == null) {
                edge = new BaseEdge() {
                    public double getLength() {
                        return length;
                    }
                };
            }

            return edge;
        }

        /**
         * For a rooted tree, getting the adjacencies is not the most efficient
         * operation as it makes a new set containing the children and the parent.
         * @return the adjacaencies
         */
        public List<Node> getAdjacencies() {
            List<Node> adjacencies = new ArrayList<Node>();
            if (children != null) adjacencies.addAll(children);
            if (parent != null) adjacencies.add(parent);
            return adjacencies;
        }

        public Taxon getTaxon() {
            return taxon;
        }

        public void setTaxa(Taxon to) {
            taxon = to;
        }

        private List<Node> children;
        private Taxon taxon;

        private Node parent;
        private double height;
        private double length;

        private Edge edge = null;

    }
}
