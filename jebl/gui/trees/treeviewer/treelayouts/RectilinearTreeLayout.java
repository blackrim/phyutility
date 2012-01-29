package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Utils;
import jebl.gui.trees.treeviewer.TreeViewer;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id: RectilinearTreeLayout.java 674 2007-03-28 05:23:26Z stevensh $
 */
public class RectilinearTreeLayout extends AbstractTreeLayout {

    public AxisType getXAxisType() {
        return AxisType.CONTINUOUS;
    }

    public AxisType getYAxisType() {
        return AxisType.DISCRETE;
    }

    public boolean maintainAspectRatio() {
        return false;
    }

    public double getHeightOfPoint(Point2D point) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public Line2D getHeightLine(double height) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public Shape getHeightArea(double height1, double height2) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public boolean alignTaxa() {
        return alignTaxonLabels;
    }

    public Shape getCollapsedNode(Node node, double ratio) {
        Node first = node;
        while( ! tree.isExternal(first) ) {
            first = tree.getChildren(first).get(0);
        }
        Node last = node;
        while( ! tree.isExternal(last) ) {
            final List<Node> children = tree.getChildren(last);
            last = children.get(children.size()- 1);
        }

        final Point2D c1 = getNodePoint(first);
        final Point2D cn = getNodePoint(last);
        final Point2D n = getNodePoint(node);
        final double dy = cn.getY() - c1.getY();
        final double dx = cn.getX() - n.getX();   assert dx >= 0.0 && dy >= 0;

        return new Rectangle2D.Double(n.getX(), n.getY() + (c1.getY() - n.getY()) * ratio, dx * ratio, dy * ratio);
    }

    public int getNodeMarkerRadiusUpperLimit(Node node, AffineTransform transform) {
        //final Node parent = tree.getParent(node);
        double lim = Double.MAX_VALUE;
        final Point2D n = getNodePoint(node);
        for( Node a : tree.getAdjacencies(node) ) {
            //final Point2D n = getNodePoint(node);
            final Point2D loc = getNodePoint(a);
            final double d = Math.abs(n.getX() - loc.getX()) * transform.getScaleX();  
            lim = Math.min(lim, d);
        }

        final List<Node> nodes = tree.getChildren(node);
        final int nNodes = nodes.size();
        final double ratio = 0.10;
        if( nNodes >= 2 ) {
            final Point2D c1 = getNodePoint(nodes.get(0));
            final Point2D cn = getNodePoint(nodes.get(nodes.size()-1));

            final double d = ratio * (cn.getY() - c1.getY()) * transform.getScaleY();
            lim = Math.min(lim, d);
        } else if( nNodes == 0 ) {
            final Node[] rnode = {Utils.rightNb(tree, node), Utils.leftNb(tree, node)};
//            System.err.println("right/left of " + tree.getTaxon(node).getName() + " "
//                    + ((rnode[0] == null) ? " - " :  tree.getTaxon(rnode[0]).getName()) + " " +
//                      ((rnode[1] == null) ? " - " :  tree.getTaxon(rnode[1]).getName()) );

            for( Node nb : rnode ) {
                if( nb != null ) {
                    final Point2D pt = getNodePoint(nb);
                    final double dy = ratio * Math.abs((pt.getY() - n.getY())) * transform.getScaleY();
                    double d;
                    if( pt.getX() > n.getX() ) {
                        d = dy;
                    } else {
                        final double dx = ratio * Math.abs((pt.getX() - n.getX())) * transform.getScaleX();
                        d = Math.sqrt(dx*dx + dy*dy);
                    }

                    lim = Math.min(lim, d);
                }
            }
        }
        assert lim >= 0;
        return (int) lim;
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // do nothing...
    }

    public List<Controls> getControls(boolean detachPrimaryCheckbox) {
        final Preferences prefs = Preferences.userNodeForPackage(TreeViewer.class);
        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final int slider1max = 10000;
            if( !tree.conceptuallyUnrooted() ) {
                final JSlider slider1 = new JSlider(SwingConstants.HORIZONTAL, 0, slider1max, 0);
                slider1.setValue((int) (rootLength * slider1max));

                // don't make sense without setting spacing
                //slider1.setPaintTicks(true);
                //slider1.setPaintLabels(true);

                slider1.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        double value = slider1.getValue();
                        setRootLength(value / slider1max);
                        prefs.putInt("root length",slider1.getValue());
                    }
                });
                slider1.setValue(prefs.getInt("root length",0));
                optionsPanel.addComponentWithLabel("Root Length:", slider1, true);
            }

            final int slider2max = 100;
            final JSlider slider2 = new JSlider(SwingConstants.HORIZONTAL, 0, slider2max, 0);
            //slider2.setMajorTickSpacing(20);
            //slider2.setPaintTicks(true);
            //slider2.setPaintLabels(true);

            slider2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    double value = 1.0 - (((double) slider2.getValue()) / slider2max);
                    setBranchCurveProportion(value, value);
                    prefs.putInt("tree curvature",slider2.getValue());
                }
            });
            slider2.setValue(prefs.getInt("tree curvature",0));
            optionsPanel.addComponentWithLabel("Curvature:", slider2, true);

            final JCheckBox checkBox1 = new JCheckBox("Align Taxon Labels");

            setAlignTaxonLabels(prefs.getBoolean("align taxon labels",alignTaxonLabels));
            checkBox1.setSelected(alignTaxonLabels);
            checkBox1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setAlignTaxonLabels(checkBox1.isSelected());
                    prefs.putBoolean("align taxon labels",checkBox1.isSelected());
                }
            });

            optionsPanel.addComponent(checkBox1);

            controls = new Controls("Layout", optionsPanel, true, false, null);
        }

        controlsList.add(controls);

        return controlsList;
    }

    public void setSettings(ControlsSettings settings) {
    }

    public void getSettings(ControlsSettings settings) {
    }

    private Controls controls = null;

    public void setRootLength(double rootLength) {
        this.rootLength = rootLength;
        invalidate();
    }

    public void setBranchCurveProportion(double xProportion, double yProportion) {
        this.xProportion = xProportion;
        this.yProportion = yProportion;
        invalidate();
    }

    public void setAlignTaxonLabels(boolean alignTaxonLabels) {
        this.alignTaxonLabels = alignTaxonLabels;
        invalidate();
    }

    protected void validate() {
        nodePoints.clear();
        branchPaths.clear();
        taxonLabelPaths.clear();
        calloutPaths.clear();

        maxXPosition = 0.0;

        yPosition = 0.0;
        yIncrement = 1.0 / (tree.getExternalNodes().size() + 1);

        final Node root = this.tree.getRootNode();
        final double rl = rootLength * this.tree.getHeight(root);

        maxXPosition = 0.0;
        getMaxXPosition(root, rl);

        Point2D rootPoint = constructNode(root, rl);

        // construct a root branch line
        final Line2D line = new Line2D.Double(0.0, rootPoint.getY(), rootPoint.getX(), rootPoint.getY());

        // add the line to the map of branch paths
        branchPaths.put(root, line);
    }

    private Point2D constructNode(Node node, double xPosition) {

        Point2D nodePoint;

        if (!tree.isExternal(node)) {

            double yPos = 0.0;

            final List<Node> children = tree.getChildren(node);
            for (Node child : children) {
                final double length = tree.getLength(child);
                final Point2D childPoint = constructNode(child, xPosition + length);
                yPos += childPoint.getY();
            }

            // the y-position of the node is the average of the child nodes
            yPos /= children.size();

            nodePoint = new Point2D.Double(xPosition, yPos);
            final double x = xPosition;
            final double y = yPos;

            for( final Node child : children ) {

                final Point2D childPoint = nodePoints.get(child);

                GeneralPath branchPath = new GeneralPath();

                // start point
                final float x0 = (float) x;
                final float y0 = (float) y;

                // end point
                final double cx = childPoint.getX();
                final float x1 = (float) cx;
                final double yChild = childPoint.getY();
                final float y1 = (float) yChild;

                float x2 = x1 - ((x1 - x0) * (float) xProportion);
                float y2 = y0 + ((y1 - y0) * (float) yProportion);

                branchPath.moveTo(x0, y0);
                branchPath.lineTo(x0, y2);
                branchPath.quadTo(x0, y1, x2, y1);
                branchPath.lineTo(x1, y1);

                // add the branchPath to the map of branch paths
                branchPaths.put(child, branchPath);

                final boolean zeroBramch = cx == x;
                final double dd = 0.0001;

                Line2D branchLabelPath =
                        new Line2D.Double(zeroBramch ? x - dd :x, yChild, zeroBramch ? cx + dd : cx, yChild);

                branchLabelPaths.put(child, branchLabelPath);
            }

            Line2D nodeLabelPath = new Line2D.Double(x, y, x + 1.0, y);

            nodeLabelPaths.put(node, nodeLabelPath);

        } else {

            nodePoint = new Point2D.Double(xPosition, yPosition);
            final double x = xPosition;
            final double y = yPosition;

            Line2D taxonLabelPath;

            if (alignTaxonLabels) {

                taxonLabelPath = new Line2D.Double(maxXPosition, y, maxXPosition + 1.0, y);

                final Line2D calloutPath = new Line2D.Double(x, y, maxXPosition, y);

                calloutPaths.put(node, calloutPath);

            } else {
                taxonLabelPath = new Line2D.Double(x, y, x + 1.0, y);
            }

            taxonLabelPaths.put(node, taxonLabelPath);

            yPosition += yIncrement;

        }

        // add the node point to the map of node points
        nodePoints.put(node, nodePoint);

        return nodePoint;
    }

    public boolean smallSubTree(Node node, AffineTransform transform) {
        int th = 7;
        final List<Node> children = tree.getChildren(node);
        if( children.size() < 2 ) return false;

        final Node[] ch = {children.get(0), children.get(1)};
//        final Point2D[] loc = {new Point2D.Double(), new Point2D.Double()};
//        for(int k = 0; k < 2; ++k) {
//            final Point2D location = nodePoints.get(ch[k]);
//            transform.transform(location, loc[k]);
//        }
        final double d = (nodePoints.get(ch[1]).getY() - nodePoints.get(ch[0]).getY()) * transform.getScaleY();
        assert d >= 0;
        //final double d =  Math.abs(loc[0].getY() - loc[1].getY())
        return d <= th;
    }

    private void getMaxXPosition(final Node node, final double xPosition) {

        if ( tree.isExternal(node)) {
            if (xPosition > maxXPosition) {
                maxXPosition = xPosition;
            }
        } else {
            for (final Node child : tree.getChildren(node)) {
                getMaxXPosition(child, xPosition + tree.getLength(child));
            }
        }
    }

    private double yPosition;
    private double yIncrement;

    private double maxXPosition;

    private double xProportion = 1.0;
    private double yProportion = 1.0;

    private double rootLength = 0.01;

    private boolean alignTaxonLabels = false;
}