/*
 * Graph.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.graphs;

import jebl.util.Attributable;

import java.util.Set;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Graph.java 310 2006-05-02 09:37:07Z rambaut $
 */
public interface Graph extends Attributable {


    /**
     * Returns a list of edges connected to this node
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    List<Edge> getEdges(Node node);

    /**
     * Returns a list of nodes connected to this node by an edge
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    List<Node> getAdjacencies(Node node);

    /**
     * Returns the Edge that connects these two nodes
     * @param node1
     * @param node2
     * @return the edge object.
     * @throws NoEdgeException if the nodes are not directly connected by an edge.
     */
    Edge getEdge(Node node1, Node node2) throws NoEdgeException;

    /**
     * Returns the length of the edge that connects these two nodes
     * @param node1
     * @param node2
     * @return the edge length.
     * @throws NoEdgeException if the nodes are not directly connected by an edge.
     */
    double getEdgeLength(Node node1, Node node2) throws NoEdgeException;

	/**
	 * Returns an array of 2 nodes which are the nodes at either end of the edge.
	 * @param edge
	 * @return an array of 2 edges
	 */
	Node[] getNodes(Edge edge);

    /**
     * @return the set of all nodes in this graph.
     */
    Set<Node> getNodes();

    /**
     * @return the set of all edges in this graph.
     */
    Set<Edge> getEdges();

    /**
     * @param degree the number of edges connected to a node
     * @return a set containing all nodes in this graph of the given degree.
     */
    Set<Node> getNodes(int degree);

    /**
     * This class is thrown by getEdgeLength(node1, node2) if node1 and node2
     * are not directly connected by an edge.
     */
    public class NoEdgeException extends Exception {}

    public class Utils {

        /**
         * @param graph
         * @param node
         * @return the number of edges attached to this node.
         */
        public static int getDegree(Graph graph, Node node) {
            return graph.getAdjacencies(node).size();
        }
    }
}
