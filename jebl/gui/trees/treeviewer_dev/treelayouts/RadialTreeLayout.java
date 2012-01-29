package jebl.gui.trees.treeviewer_dev.treelayouts;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: RadialTreeLayout.java 535 2006-11-21 11:11:20Z rambaut $
 */
public class RadialTreeLayout extends AbstractTreeLayout {

	private double spread = 0.0;

    public AxisType getXAxisType() {
        return AxisType.CONTINUOUS;
    }

    public AxisType getYAxisType() {
        return AxisType.CONTINUOUS;
    }

    public boolean isShowingRootBranch() {
        return false;
    }

    public boolean maintainAspectRatio() {
        return true;
    }

    public double getHeightOfPoint(Point2D point) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public Line2D getHeightLine(double height) {
        throw new UnsupportedOperationException("Method getHeightLine() is not supported in this TreeLayout");
    }

    public Shape getHeightArea(double height1, double height2) {
        throw new UnsupportedOperationException("Method getHeightArea() is not supported in this TreeLayout");
    }

    public double getSpread() {
        return spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
        fireTreeLayoutChanged();
    }

	public void layout(RootedTree tree, TreeLayoutCache cache) {
        cache.clear();

        try {
            final Node root = tree.getRootNode();

            constructNode(tree, root, 0.0, Math.PI * 2, 0.0, 0.0, 0.0, cache);

        } catch (Graph.NoEdgeException e) {
            e.printStackTrace();
        }
    }

    private Point2D constructNode(RootedTree tree, Node node,
                                  double angleStart, double angleFinish, double xPosition,
                                  double yPosition, double length,
                                  TreeLayoutCache cache) throws Graph.NoEdgeException {

        final double branchAngle = (angleStart + angleFinish) / 2.0;

        final double directionX = Math.cos(branchAngle);
        final double directionY = Math.sin(branchAngle);
        Point2D nodePoint = new Point2D.Double(xPosition + (length * directionX), yPosition + (length * directionY));

        // System.out.println("Node: " + Utils.DEBUGsubTreeRep(tree, node) + " at " + nodePoint);

        if (!tree.isExternal(node)) {

            List<Node> children = tree.getChildren(node);
            int[] leafCounts = new int[children.size()];
            int sumLeafCount = 0;

            int i = 0;
            for (Node child : children) {
                leafCounts[i] = jebl.evolution.trees.Utils.getExternalNodeCount(tree, child);
                sumLeafCount += leafCounts[i];
                i++;
            }

            double span = (angleFinish - angleStart);

            if (!tree.isRoot(node)) {
                span *= 1.0 + (spread / 10.0);
                angleStart = branchAngle - (span / 2.0);
                angleFinish = branchAngle + (span / 2.0);
            }

            double a2 = angleStart;

            i = 0;
            for (Node child : children) {

                final double childLength = tree.getLength(child);
                double a1 = a2;
                a2 = a1 + (span * leafCounts[i] / sumLeafCount);

                Point2D childPoint = constructNode(tree, child, a1, a2,
		                nodePoint.getX(), nodePoint.getY(), childLength, cache);

	            Line2D branchLine = new Line2D.Double(
				            nodePoint.getX(), nodePoint.getY(),
				            childPoint.getX(), childPoint.getY());

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

		            float x1 = (float)childPoint.getX();
		            float y1 = (float)childPoint.getY();
		            float x0 = (float)nodePoint.getX();
		            float y0 = (float)nodePoint.getY();

		            GeneralPath branchPath = new GeneralPath();

		            // to help this, we are going to draw the branch backwards
		            branchPath.moveTo(x1, y1);
		            for (int j = 0; j < colouring.length - 1; j+=2) {
			            float height = ((Number)colouring[j+1]).floatValue();
			            float p = (height - childHeight) / (nodeHeight - childHeight);
			            float x = x1 + ((x0 - x1) * p);
			            float y = y1 + ((y0 - y1) * p);
			            branchPath.lineTo(x, y);
		            }
		            branchPath.lineTo(x0, y0);

		            // add the branchPath to the map of branch paths
		            cache.branchPaths.put(child, branchPath);

	            } else {
		            // add the branchLine to the map of branch paths
		            cache.branchPaths.put(child, branchLine);
	            }

                cache.branchLabelPaths.put(child, (Line2D)branchLine.clone());
                i++;
            }

            Point2D nodeLabelPoint = new Point2D.Double(xPosition + ((length + 1.0) * directionX),
                    yPosition + ((length + 1.0) * directionY));

            Line2D nodeLabelPath = new Line2D.Double(nodePoint, nodeLabelPoint);
            cache.nodeLabelPaths.put(node, nodeLabelPath);

            Point2D nodeBarPoint = new Point2D.Double(xPosition + ((length - 1.0) * directionX),
                    yPosition + ((length - 1.0) * directionY));
            Line2D nodeBarPath = new Line2D.Double(nodePoint, nodeBarPoint);

            cache.nodeBarPaths.put(node, nodeBarPath);
        } else {

            Point2D taxonPoint = new Point2D.Double(xPosition + ((length + 1.0) * directionX),
                    yPosition + ((length + 1.0) * directionY));

            Line2D taxonLabelPath = new Line2D.Double(nodePoint, taxonPoint);
            cache.tipLabelPaths.put(node, taxonLabelPath);
        }

        // add the node point to the map of node points
        cache.nodePoints.put(node, nodePoint);

        return nodePoint;
    }

}