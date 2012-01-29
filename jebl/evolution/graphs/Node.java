/*
 * Node.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.graphs;

import jebl.util.Attributable;

/**
 * Represents a node in a graph or tree. In general it is
 * used only as a handle to traverse a graph or tree structure and
 * it has no methods or instance variables.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Node.java 295 2006-04-14 14:59:10Z rambaut $
 */
public interface Node extends Attributable {

    int getDegree();
}
