/*
 * Utils.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.graphs;

/**
 * A collection of utility functions for graphs.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Utils.java 185 2006-01-23 23:03:18Z rambaut $
 */
public class Utils {

    /**
     * @param graph
     * @return true if the given graph is acyclic.
     */
    public boolean isAcyclical(Graph graph) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @param graph
     * @return true if the given graph is fully connected.
     */
    public boolean isConnected(Graph graph) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @param graph
     * @return true if the given graph is a tree, i.e. is acyclic
     * and fully connected.
     */
    public final boolean isTree(Graph graph) {
        return isAcyclical(graph) && isConnected(graph);
    }
}
