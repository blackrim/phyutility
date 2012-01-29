package jebl.evolution.trees;

import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: FilteredRootedTree.java 627 2007-01-15 03:50:40Z pepster $
 */
public abstract class FilteredRootedTree implements RootedTree {

    public FilteredRootedTree(final RootedTree source) {
        this.source = source;
    }

	public RootedTree getSource() {
		return source;
	}

    public boolean conceptuallyUnrooted() {
        return source.conceptuallyUnrooted();
    }

    public List<Node> getChildren(Node node) {
	    return source.getChildren(node);
    }

    public boolean hasHeights() {
        return source.hasHeights();
    }

    public double getHeight(Node node) {
        return source.getHeight(node);
    }

    public boolean hasLengths() {
        return source.hasLengths();
    }

    public double getLength(Node node) {
        return source.getLength(node);
    }

    public Node getParent(Node node) {
        return source.getParent(node);
    }

    public Node getRootNode() {
        return source.getRootNode();
    }

    public Set<Node> getExternalNodes() {
        return source.getExternalNodes();
    }

    public Set<Node> getInternalNodes() {
        return source.getInternalNodes();
    }

	public Set<Edge> getExternalEdges() {
		return source.getExternalEdges();
	}

	public Set<Edge> getInternalEdges() {
		return source.getInternalEdges();
	}

    public Node getNode(Taxon taxon) {
        return source.getNode(taxon);
    }

    public Set<Taxon> getTaxa() {
        return source.getTaxa();
    }

    public Taxon getTaxon(Node node) {
        return source.getTaxon(node);
    }

    public boolean isExternal(Node node) {
        return source.isExternal(node);
    }

    public List<Node> getAdjacencies(Node node) {
        return source.getAdjacencies(node);
    }

    public List<Edge> getEdges(Node node) {
        return source.getEdges(node);
    }

    public Set<Edge> getEdges() {
        return source.getEdges();
    }

	public Node[] getNodes(Edge edge) {
	    return source.getNodes(edge);
	}

    public Edge getEdge(Node node1, Node node2) throws NoEdgeException {
        return source.getEdge(node1, node2);
    }

    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        return source.getEdgeLength(node1, node2);
    }

    public Set<Node> getNodes() {
        return source.getNodes();
    }

    public Set<Node> getNodes(int degree) {
        return source.getNodes(degree);
    }

	public boolean isRoot(Node node) {
		return source.isRoot(node);
	}

    public void renameTaxa(Taxon from, Taxon to) {
        source.renameTaxa(from, to);
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

	// PRIVATE members

	protected final RootedTree source;
}