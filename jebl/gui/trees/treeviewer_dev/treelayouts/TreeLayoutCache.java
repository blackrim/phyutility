package jebl.gui.trees.treeviewer_dev.treelayouts;

import jebl.evolution.graphs.Node;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeLayoutCache.java 535 2006-11-21 11:11:20Z rambaut $
 */
public class TreeLayoutCache {
    public Point2D getNodePoint(Node node) {
        return nodePoints.get(node);
    }

    public Shape getBranchPath(Node node) {
        return branchPaths.get(node);
    }

    public Map<Node, Shape> getBranchPathMap() {
        return branchPaths;
    }

    public Shape getCollapsedShape(Node node) {
        return collapsedShapes.get(node);
    }

    public Map<Node, Shape> getCollapsedShapeMap() {
        return collapsedShapes;
    }

    public Line2D getTipLabelPath(Node node) {
        return tipLabelPaths.get(node);
    }

    public Map<Node, Line2D> getTipLabelPathMap() {
        return tipLabelPaths;
    }

    public Line2D getBranchLabelPath(Node node) {
        return branchLabelPaths.get(node);
    }

    public Map<Node, Line2D> getBranchLabelPathMap() {
        return branchLabelPaths;
    }

    public Line2D getNodeLabelPath(Node node) {
        return nodeLabelPaths.get(node);
    }

    public Map<Node, Line2D> getNodeLabelPathMap() {
        return nodeLabelPaths;
    }

    public Line2D getNodeBarPath(Node node) {
        return nodeBarPaths.get(node);
    }

    public Map<Node, Line2D> getNodeBarPathMap() {
        return nodeBarPaths;
    }

    public Shape getCalloutPath(Node node) {
        return calloutPaths.get(node);
    }

    public Map<Node, Shape> getCalloutPathMap() {
        return calloutPaths;
    }

	public void clear() {
		nodePoints.clear();
        branchPaths.clear();
        collapsedShapes.clear();
        tipLabelPaths.clear();
		branchLabelPaths.clear();
        nodeLabelPaths.clear();
        nodeBarPaths.clear();
        calloutPaths.clear();
}

	protected Map<Node, Point2D> nodePoints = new HashMap<Node, Point2D>();
    protected Map<Node, Shape> branchPaths = new HashMap<Node, Shape>();
    protected Map<Node, Shape> collapsedShapes = new HashMap<Node, Shape>();
    protected Map<Node, Line2D> tipLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> branchLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> nodeLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> nodeBarPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Shape> calloutPaths = new HashMap<Node, Shape>();
}
