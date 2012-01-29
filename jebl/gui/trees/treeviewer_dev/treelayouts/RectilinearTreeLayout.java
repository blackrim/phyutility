package jebl.gui.trees.treeviewer_dev.treelayouts;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: RectilinearTreeLayout.java 724 2007-06-11 16:25:39Z rambaut $
 */
public class RectilinearTreeLayout extends AbstractTreeLayout {

	private double curvature = 0.0;
	private double rootLength = 0.01;
	private boolean alignTipLabels = false;

	public AxisType getXAxisType() {
		return AxisType.CONTINUOUS;
	}

	public AxisType getYAxisType() {
		return AxisType.DISCRETE;
	}

	public boolean isShowingRootBranch() {
		return true;
	}

	public boolean maintainAspectRatio() {
		return false;
	}

	public double getHeightOfPoint(Point2D point) {
		return point.getX();
	}

	public Shape getHeightLine(double height) {
		double x = height;
		double y1 = 0.0;
		double y2 = 1.0;
		return new Line2D.Double(x, y1, x, y2);
	}

	public Shape getHeightArea(double height1, double height2) {
		double x = height1;
		double y = 0.0;
		double w = height2 - height1;
		double h = 1.0;
		return new Rectangle2D.Double(x, y, w, h);
	}

	public boolean isAlignTipLabels() {
		return alignTipLabels;
	}

	public double getRootLength() {
		return rootLength;
	}

	public double getCurvature() {
		return curvature;
	}

	public void setAlignTipLabels(boolean alignTipLabels) {
		this.alignTipLabels = alignTipLabels;
		fireTreeLayoutChanged();
	}

	public void setCurvature(double curvature) {
		this.curvature = curvature;
		fireTreeLayoutChanged();
	}

	public void setRootLength(double rootLength) {
		this.rootLength = rootLength;
		fireTreeLayoutChanged();
	}

	public void layout(RootedTree tree, TreeLayoutCache cache) {

		cache.clear();

		maxXPosition = 0.0;

		yPosition = 0.0;
		yIncrement = 1.0 / (tree.getExternalNodes().size() + 1);

		Node root = tree.getRootNode();
		double rl = rootLength * tree.getHeight(root);

		maxXPosition = 0.0;
		getMaxXPosition(tree, root, rl);

		Point2D rootPoint = constructNode(tree, root, rl, cache);

		// construct a root branch line
		Line2D line = new Line2D.Double(0.0, rootPoint.getY(), rootPoint.getX(), rootPoint.getY());

		// add the line to the map of branch paths
		cache.branchPaths.put(root, line);

	}

	private Point2D constructNode(RootedTree tree, Node node, double xPosition, TreeLayoutCache cache) {

		Point2D nodePoint;

		if (!tree.isExternal(node)) {

			if (collapsedAttributeName != null && node.getAttribute(collapsedAttributeName) != null) {
				nodePoint = constructCollapsedNode(tree, node, xPosition, cache);
			} else if (cartoonAttributeName != null && node.getAttribute(cartoonAttributeName) != null) {
				nodePoint = constructCartoonNode(tree, node, xPosition, cache);
			} else {
				double yPos = 0.0;

				List<Node> children = tree.getChildren(node);
				for (Node child : children) {

					double length = tree.getLength(child);
					Point2D childPoint = constructNode(tree, child, xPosition + length, cache);
					yPos += childPoint.getY();
				}

				// the y-position of the node is the average of the child nodes
				yPos /= children.size();

				nodePoint = new Point2D.Double(xPosition, yPos);

				for (Node child : children) {

					Point2D childPoint = cache.nodePoints.get(child);

					GeneralPath branchPath = new GeneralPath();

					// start point
					float x0 = (float) nodePoint.getX();
					float y0 = (float) nodePoint.getY();

					// end point
					float x1 = (float) childPoint.getX();
					float y1 = (float) childPoint.getY();

					if (curvature == 0.0) {
						Object[] colouring = null;
						if (branchColouringAttribute != null) {
							colouring = (Object[])child.getAttribute(branchColouringAttribute);
						}
						if (colouring != null) {
							// If there is a colouring, then we break the path up into
							// segments. This should allow use to iterate along the segments
							// and colour them as we draw them.


							float nodeHeight = (float) tree.getHeight(node);
							float childHeight = (float) tree.getHeight(child);

							// to help this, we are going to draw the branch backwards
							branchPath.moveTo(x1, y1);
							for (int i = 0; i < colouring.length - 1; i+=2) {
								float height = ((Number)colouring[i+1]).floatValue();
								float p = (height - childHeight) / (nodeHeight - childHeight);
								float x = x1 - ((x1 - x0) * p);
								branchPath.lineTo(x, y1);
							}
							branchPath.lineTo(x0, y1);
							branchPath.lineTo(x0, y0);
						} else {
							branchPath.moveTo(x0, y0);
							branchPath.lineTo(x0, y1);
							branchPath.lineTo(x1, y1);
						}
					} else if (curvature == 1.0) {
						// The extreme is to use a triangular look
						branchPath.moveTo(x0, y0);
						branchPath.lineTo(x1, y1);
					} else {
						// if the curvature is on then we simply don't
						// do tree colouring - I just can't be bothered to
						// implement it (and it would probably be confusing anyway).
						float x2 = x1 - ((x1 - x0) * (float) (1.0 - curvature));
						float y2 = y0 + ((y1 - y0) * (float) (1.0 - curvature));

						branchPath.moveTo(x0, y0);
						branchPath.lineTo(x0, y2);
						branchPath.quadTo(x0, y1, x2, y1);
						branchPath.lineTo(x1, y1);
					}


					// add the branchPath to the map of branch paths
					cache.branchPaths.put(child, branchPath);

					double x3 = (nodePoint.getX() + childPoint.getX()) / 2;
					Line2D branchLabelPath = new Line2D.Double(
							x3 - 1.0, childPoint.getY(),
							x3 + 1.0, childPoint.getY());

					cache.branchLabelPaths.put(child, branchLabelPath);
				}

				Line2D nodeLabelPath = new Line2D.Double(
						nodePoint.getX(), nodePoint.getY(),
						nodePoint.getX() + 1.0, nodePoint.getY());

				cache.nodeLabelPaths.put(node, nodeLabelPath);

				Line2D nodeBarPath = new Line2D.Double(
						nodePoint.getX(), nodePoint.getY(),
						nodePoint.getX() - 1.0, nodePoint.getY());

				cache.nodeBarPaths.put(node, nodeBarPath);
			}
		} else {

			nodePoint = new Point2D.Double(xPosition, yPosition);

			Line2D tipLabelPath;

			if (alignTipLabels) {

				tipLabelPath = new Line2D.Double(
						maxXPosition, nodePoint.getY(),
						maxXPosition + 1.0, nodePoint.getY());

				Line2D calloutPath = new Line2D.Double(
						nodePoint.getX(), nodePoint.getY(),
						maxXPosition, nodePoint.getY());

				cache.calloutPaths.put(node, calloutPath);

			} else {
				tipLabelPath = new Line2D.Double(
						nodePoint.getX(), nodePoint.getY(),
						nodePoint.getX() + 1.0, nodePoint.getY());

			}

			cache.tipLabelPaths.put(node, tipLabelPath);

			yPosition += yIncrement;

		}

		// add the node point to the map of node points
		cache.nodePoints.put(node, nodePoint);

		return nodePoint;
	}

	private Point2D constructCartoonNode(RootedTree tree, Node node, double xPosition, TreeLayoutCache cache) {

		Point2D nodePoint;

		Object[] values = (Object[])node.getAttribute(cartoonAttributeName);
		int tipCount = (Integer)values[0];
		double tipHeight = (Double)values[1];
		double height = tree.getHeight(node);
		double maxXPos = xPosition + height - tipHeight;

		double minYPos = yPosition;
		yPosition += yIncrement * (tipCount - 1);
		double maxYPos = yPosition;
		yPosition += yIncrement;

		// the y-position of the node is the average of the child nodes
		double yPos = (maxYPos + minYPos) / 2;

		nodePoint = new Point2D.Double(xPosition, yPos);

		GeneralPath collapsedShape = new GeneralPath();

		// start point
		float x0 = (float)nodePoint.getX();
		float y0 = (float)nodePoint.getY();

		// end point
		float x1 = (float)maxXPos;
		float y1 = (float)minYPos;

		float y2 = (float)maxYPos;

		collapsedShape.moveTo(x0, y0);
		collapsedShape.lineTo(x1, y1);
		collapsedShape.lineTo(x1, y2);
		collapsedShape.closePath();

		// add the collapsedShape to the map of branch paths
		cache.collapsedShapes.put(node, collapsedShape);

		Line2D nodeLabelPath = new Line2D.Double(
				nodePoint.getX(), nodePoint.getY(),
				nodePoint.getX() + 1.0, nodePoint.getY());

		cache.nodeLabelPaths.put(node, nodeLabelPath);

		Line2D nodeBarPath = new Line2D.Double(
				nodePoint.getX(), nodePoint.getY(),
				nodePoint.getX() - 1.0, nodePoint.getY());

		cache.nodeBarPaths.put(node, nodeBarPath);

		if (showingCartoonTipLabels) {
			constructCartoonTipLabelPaths(tree, node, maxXPos, new double[] { minYPos }, cache);
		}

		return nodePoint;
	}

	private void constructCartoonTipLabelPaths(RootedTree tree, Node node,
	                                           double xPosition, double[] yPosition,
	                                           TreeLayoutCache cache) {

		if (!tree.isExternal(node)) {
			for (Node child :  tree.getChildren(node)) {
				constructCartoonTipLabelPaths(tree, child, xPosition, yPosition, cache);
			}
		} else {

			Point2D nodePoint = new Point2D.Double(xPosition, yPosition[0]);

			Line2D tipLabelPath;

			if (alignTipLabels) {

				tipLabelPath = new Line2D.Double(
						maxXPosition, nodePoint.getY(),
						maxXPosition + 1.0, nodePoint.getY());

				Line2D calloutPath = new Line2D.Double(
						nodePoint.getX(), nodePoint.getY(),
						maxXPosition, nodePoint.getY());

				cache.calloutPaths.put(node, calloutPath);

			} else {
				tipLabelPath = new Line2D.Double(
						nodePoint.getX(), nodePoint.getY(),
						nodePoint.getX() + 1.0, nodePoint.getY());

			}

			cache.tipLabelPaths.put(node, tipLabelPath);

			yPosition[0] += yIncrement;

		}
	}

	private Point2D constructCollapsedNode(RootedTree tree, Node node, double xPosition, TreeLayoutCache cache) {

		Point2D nodePoint;

		Object[] values = (Object[])node.getAttribute(collapsedAttributeName);
		String tipName = (String)values[0];
		double tipHeight = (Double)values[1];
		double height = tree.getHeight(node);
		double maxXPos = xPosition + height - tipHeight;

		double minYPos = yPosition - (yIncrement * 0.5);
		double maxYPos = minYPos + yIncrement;
		yPosition += yIncrement;

		// the y-position of the node is the average of the child nodes
		double yPos = (maxYPos + minYPos) / 2;

		nodePoint = new Point2D.Double(xPosition, yPos);

		GeneralPath collapsedShape = new GeneralPath();

		// start point
		float x0 = (float)nodePoint.getX();
		float y0 = (float)nodePoint.getY();

		// end point
		float x1 = (float)maxXPos;
		float y1 = (float)minYPos;

		float y2 = (float)maxYPos;

		collapsedShape.moveTo(x0, y0);
		collapsedShape.lineTo(x1, y1);
		collapsedShape.lineTo(x1, y2);
		collapsedShape.closePath();

		// add the collapsedShape to the map of branch paths
		cache.collapsedShapes.put(node, collapsedShape);

		Line2D nodeLabelPath = new Line2D.Double(
				nodePoint.getX(), nodePoint.getY(),
				nodePoint.getX() + 1.0, nodePoint.getY());

		cache.nodeLabelPaths.put(node, nodeLabelPath);

		Line2D nodeBarPath = new Line2D.Double(
				nodePoint.getX(), nodePoint.getY(),
				nodePoint.getX() - 1.0, nodePoint.getY());

		cache.nodeBarPaths.put(node, nodeBarPath);

		Line2D tipLabelPath;

		if (alignTipLabels) {

			tipLabelPath = new Line2D.Double(
					maxXPosition, yPos,
					maxXPosition + 1.0, yPos);

			Line2D calloutPath = new Line2D.Double(
					maxXPos, yPos,
					maxXPosition, yPos);

			cache.calloutPaths.put(node, calloutPath);

		} else {
			tipLabelPath = new Line2D.Double(
					maxXPos, yPos,
					maxXPos + 1.0, yPos);

		}

		cache.tipLabelPaths.put(node, tipLabelPath);

		return nodePoint;
	}


	private void getMaxXPosition(RootedTree tree, Node node, double xPosition) {

		if (!tree.isExternal(node)) {

			List<Node> children = tree.getChildren(node);

			for (Node child : children) {
				double length = tree.getLength(child);
				getMaxXPosition(tree, child, xPosition + length);
			}

		} else {
			if (xPosition > maxXPosition) {
				maxXPosition = xPosition;
			}
		}
	}

	private double yPosition;
	private double yIncrement;

	private double maxXPosition;


}