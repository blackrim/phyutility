/*
 * RootedTree.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.trees;

import jebl.evolution.graphs.Node;

import java.util.List;

/**
 * A tree with a root (node with maximum height). This interface
 * provides the concept of a direction of time that flows from the
 * root to the tips. Each node in the tree has a node height that is
 * less than its parent's height and greater than it children's heights.
 *
 * @author rambaut
 * @author Alexei Drummond
 *
 * @version $Id: RootedTree.java 529 2006-11-14 01:13:52Z matt_kearse $
 */
public interface RootedTree extends Tree {

    /**
     * @param node the node whose children are being requested.
     * @return the list of nodes that are the children of the given node.
     * The set may be empty for a terminal node (a tip).
     */
    List<Node> getChildren(Node node);

    /**
     * @return Whether this tree has node heights available
     */
    boolean hasHeights();

    /**
     * @param node the node whose height is being requested.
     * @return the height of the given node. The height will be
     * less than the parent's height and greater than it children's heights.
     */
    double getHeight(Node node);

    /**
     * @return Whether this tree has branch lengths available
     */
    boolean hasLengths();

    /**
     * @param node the node whose branch length (to its parent) is being requested.
     * @return the length of the branch to the parent node (0.0 if the node is the root).
     */
    double getLength(Node node);

    /**
     * @param node the node whose parent is requested
     * @return the parent node of the given node, or null
     * if the node is the root node.
     */
    Node getParent(Node node);

    /**
     * The root of the tree has the largest node height of
     * all nodes in the tree.
     * @return the root of the tree.
     */
    Node getRootNode();

    /**
     * Due to current implementation limitations, trees store "branch" information in nodes. So, internally rooted trees
     * are genetrated when un-rooted would be more natural.
     *
     * This should be removed. If this is a rooted tree then it is rooted. This can really
     * only confuse things. Trees are unrooted, RootedTrees are rooted. This is not an implementation
     * limitation. It may be that a RootedTree has an arbitrary root but it is still rooted. With a rooted
     * tree, it is convenient to store branch information at the node (i.e., for the branch above the node)
     * because there is no "branch" object. Andrew.
     *
     * This function will probably become deprecated once the "development"
     * tree viewer becomes in sync with the main tree viewer branch and some
     * method of handling this concept has been introduced. Until then, this method remains.
     *
     * @return true if tree(s) are to be viewed as unrooted
     */
    boolean conceptuallyUnrooted();

	/**
	 * @param node the node
	 * @return true if the node is the root of this tree.
	 */
	boolean isRoot(Node node);
}