/*
 * SquareTreePainter.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.gui.trees.treecomponent;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * @author Alexei Drummond
 *
 * @version $Id: SquareTreePainter.java 309 2006-05-02 08:22:06Z rambaut $
 */
public class SquareTreePainter implements RootedTreePainter {

    protected Stroke lineStroke = new BasicStroke((float)2.0);
    protected Paint linePaint = Color.black;

    protected Stroke hilightStroke = new BasicStroke((float)2.0);
    protected Paint hilightPaint = Color.blue;

    protected int maxFontSize = 12;

    protected Font labelFont = new Font("Helvetica", Font.PLAIN, 12);
    protected Paint labelPaint = Color.black;

    protected Font hilightLabelFont = new Font("Helvetica", Font.PLAIN, 12);
    protected Paint hilightLabelPaint = Color.blue;

    public SquareTreePainter() {
        this.rememberYPositions = false;
    }

    public SquareTreePainter(boolean rememberYPositions) {
        this.rememberYPositions = rememberYPositions;
    }

    /**
    *	Set line style
    */
    public void setLineStyle(Stroke lineStroke, Paint linePaint) {
        this.lineStroke = lineStroke;
        this.linePaint = linePaint;
    }

    /**
    *	Set line style
    */
    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }


    /**
    *	Set hilight style
    */
    public void setHilightStyle(Stroke hilightStroke, Paint hilightPaint) {
        this.hilightStroke = hilightStroke;
        this.hilightPaint = hilightPaint;
    }

    public void setFontSize(int size) {
        maxFontSize = size;
    }

    /**
     *	Set label style.
     */
    public void setLabelStyle(Font labelFont, Paint labelPaint) {
        this.labelFont = labelFont;
        this.labelPaint = labelPaint;
    }

    /**
     *	Set hilight label style.
     */
    public void setHilightLabelStyle(Font hilightLabelFont, Paint hilightLabelPaint) {
        this.hilightLabelFont = hilightLabelFont;
        this.hilightLabelPaint = hilightLabelPaint;
    }

    public void setUserDefinedHeight(double height) {
        this.userDefinedHeight = height;
    }

    public void drawLabels(boolean drawLabels) {
        this.drawLabels = drawLabels;
    }

    public void drawHorizontals(boolean drawHorizontals) {
        this.drawHorizontals = drawHorizontals;
    }

    public void drawVerticals(boolean drawVerticals) {
        this.drawVerticals = drawVerticals;
    }

    /**
     * Do the actual painting.
     */
    public void paintTree(Graphics2D g2, Dimension size, RootedTree tree) {

        if (tree == null) return;

        scaleY = ((double)size.height) / (tree.getExternalNodes().size());

        double maxLabelHeight = scaleY;

        int fontSize = maxFontSize + 1;
        do {
            fontSize --;
            labelFont = new Font("Helvetica", Font.PLAIN, fontSize);
            g2.setFont(labelFont);
        } while (fontSize > 1 && g2.getFontMetrics().getAscent() > maxLabelHeight);

        hilightLabelFont = new Font("Helvetica", Font.PLAIN, fontSize);

        double maxLabelWidth = getMaxLabelWidth(g2, tree);

        currentY = 0.5;

        double treeHeight = tree.getHeight(tree.getRootNode());
        double height;
        if (userDefinedHeight < 0.0) {
            height = treeHeight;
        } else {
            height = userDefinedHeight;
        }

        scaleX = ((double)size.width - 4 - maxLabelWidth) / (height * 1.02);

        paintNode(g2, tree, tree.getRootNode(), 0.0, (height * 1.02)-treeHeight, false);
    }

    /**
     * Paint a node.
     */
    private double paintNode(Graphics2D g2, RootedTree tree, Node node,
                             double x0, double x1, boolean hilight) {

        double y;

        double ix0 = convertX(x0);
        double ix1 = convertX(x1);
        double iy;

        if (tree.isExternal(node)) {

            if (rememberYPositions) {
                // remember the y positions of taxa that you have seen before... AD
                String taxonId = tree.getTaxon(node).getName();
                Double pos = yPositionMap.get(taxonId);
                if (pos != null) {
                    y = pos;
                } else {
                    y = currentY;
                    currentY += 1.0;
                    yPositionMap.put(taxonId, y);
                }
            } else {
                y = currentY;
                currentY += 1.0;
            }

            if (hilight) {
                g2.setPaint(hilightLabelPaint);
                g2.setFont(hilightLabelFont);
            } else {
                g2.setPaint(labelPaint);
                g2.setFont(labelFont);
            }


            String label = tree.getTaxon(node).getName();
            double labelWidth = g2.getFontMetrics().stringWidth(label);
            double labelHeight = g2.getFontMetrics().getAscent();
            double labelOffset = labelHeight / 2;

            iy = convertY(y);

            if (label != null && label.length() > 0 && drawLabels) {
                g2.drawString(label, (float)(ix1 + 4), (float)(iy + labelOffset));
            }


            nodeRectVert.put(node,new Rectangle.Double(ix1 + 4, iy, labelWidth, labelHeight));

            if (hilight) {
                g2.setPaint(hilightPaint);
                g2.setStroke(hilightStroke);
            } else {
                // use tree color attribute if set
                g2.setPaint(linePaint);
                g2.setStroke(lineStroke);
            }

        } else {
            double y0, y1;

            List<Node> children = tree.getChildren(node);

            Node child = children.get(0);
            double length = tree.getHeight(node) - tree.getHeight(child);

            y0 = paintNode(g2, tree, child, x1, x1+length, hilight);
            y1 = y0;

            for (int i = 1; i < children.size(); i++) {
                child = children.get(i);
                length = tree.getHeight(node) - tree.getHeight(child);

                y1 = paintNode(g2, tree, child, x1, x1+length, hilight);
            }

            double iy0 = convertY(y0);
            double iy1 = convertY(y1);

            if (hilight) {
                g2.setPaint(hilightPaint);
                g2.setStroke(hilightStroke);
            } else {
                 // use tree color attribute if set
                 g2.setPaint(linePaint);
                 g2.setStroke(lineStroke);
            }

            if (drawHorizontals) {
                Line2D line = new Line2D.Double(ix1, iy0, ix1, iy1);
                g2.draw(line);
            }

            nodeRectVert.put(node, new Rectangle.Double(ix1-2, iy0-2, 5, (iy1 - iy0) + 4));

            y = (y1 + y0) / 2;
            iy = convertY(y);

        }

        if (drawVerticals) {
            Line2D line = new Line2D.Double(ix0, iy, ix1, iy);
            g2.draw(line);
        }

        nodeRectHoriz.put(node,new Rectangle.Double(ix0-2, iy-2, (ix1 - ix0) + 4, 5));

        return y;
    }

    private double convertX(double x) { return x * scaleX; }

    private double convertY(double y) { return y * scaleY; }

    /**
     * @return the maximum label width
     */
    private double getMaxLabelWidth(Graphics2D g, RootedTree tree) {
        double maxLabelWidth = 0.0;
        for (Taxon taxon : tree.getTaxa()) {
            String label = taxon.getName();
            double labelWidth = g.getFontMetrics().stringWidth(label);
            if (labelWidth > maxLabelWidth)
                maxLabelWidth = labelWidth;
        }
        return maxLabelWidth;
    }

    /**
    *	Find the node under point. Returns null if not found.
    */
    public final Node findNodeAtPoint(Point2D point) {

        for (Map.Entry entry : nodeRectVert.entrySet()) {
            Node node = (Node)entry.getKey();
            Rectangle rect = (Rectangle)entry.getValue();
            if (rect.contains(point)) return node;
        }
        for (Map.Entry entry : nodeRectHoriz.entrySet()) {
            Node node = (Node)entry.getKey();
            Rectangle rect = (Rectangle)entry.getValue();
            if (rect.contains(point)) return node;
        }

        return null;
    }

    // PRIVATE MEMBERS

    private double scaleX, scaleY, currentY;

    private Map<Node, Rectangle2D> nodeRectVert = new HashMap<Node, Rectangle2D>();
    private Map<Node, Rectangle2D> nodeRectHoriz = new HashMap<Node, Rectangle2D>();

    private Map<String, Double> yPositionMap = new HashMap<String, Double>();
    private boolean rememberYPositions = false;
    private double userDefinedHeight = -1.0;
    private boolean drawLabels = true;
    private boolean drawHorizontals = true;
    private boolean drawVerticals = true;
}
