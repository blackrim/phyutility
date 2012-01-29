package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id: AbstractTreeLayout.java 181 2006-01-23 17:31:10Z rambaut $
 */
public abstract class AbstractTreeLayout implements TreeLayout {
    public void setTree(Tree tree) {
        this.tree = (RootedTree)tree;
	    invalidate();
    }

    public void invalidate() {
        invalid = true;
        fireTreeLayoutChanged();
    }

    public Point2D getNodePoint(Node node) {
        checkValidation();
        return nodePoints.get(node);
    }

    public Shape getBranchPath(Node node) {
        checkValidation();
        return branchPaths.get(node);
    }

    public Line2D getTaxonLabelPath(Node node) {
        checkValidation();
        return taxonLabelPaths.get(node);
    }

    public Line2D getBranchLabelPath(Node node) {
        checkValidation();
        return branchLabelPaths.get(node);
    }

    public Line2D getNodeLabelPath(Node node) {
        checkValidation();
        return nodeLabelPaths.get(node);
    }

	public Shape getCalloutPath(Node node) {
	    checkValidation();
	    return calloutPaths.get(node);
	}

    private void checkValidation() {
        if (invalid) {
            validate();
            invalid = false;
        }
    }

    public void addTreeLayoutListener(TreeLayoutListener listener) {
        listeners.add(listener);
    }

    public void removeTreeLayoutListener(TreeLayoutListener listener) {
        listeners.remove(listener);
    }

    protected void fireTreeLayoutChanged() {
        for (TreeLayoutListener listener : listeners) {
            listener.treeLayoutChanged();
        }
    }

    protected abstract void validate();

    private boolean invalid = true;
    protected RootedTree tree = null;
    protected Map<Node, Point2D> nodePoints = new HashMap<Node, Point2D>();
    protected Map<Node, Shape> branchPaths = new HashMap<Node, Shape>();
    protected Map<Node, Line2D> taxonLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> branchLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> nodeLabelPaths = new HashMap<Node, Line2D>();
	protected Map<Node, Shape> calloutPaths = new HashMap<Node, Shape>();

    private Set<TreeLayoutListener> listeners = new HashSet<TreeLayoutListener>();
}
