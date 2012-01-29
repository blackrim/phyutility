/*
 * RootedTreeComponent.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.gui.trees.treecomponent;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Vector;

/**
 * @author Alexei Drummond
 *
 * @version $Id: RootedTreeComponent.java 309 2006-05-02 08:22:06Z rambaut $
 */
public class RootedTreeComponent extends JComponent implements Printable {

    /** the tree */
    protected RootedTree tree = null;

    /** the tree painter */
    private RootedTreePainter treePainter = null;

    public RootedTreeComponent(RootedTreePainter treePainter) {
        this.treePainter = treePainter;
        init();
    }

    /**
     * @param tree the tree
     */
    public RootedTreeComponent(RootedTreePainter treePainter, RootedTree tree) {
        this.treePainter = treePainter;

        init();
        setTree(tree);
    }

    /**
     * Called by all constructors.
     */
    void init() {
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);

        // adds a mouse listener
        addMouseListener(new MListener());
    }

    /**
     * Set the tree.
     */
    public void setTree(RootedTree tree) {
        this.tree = tree;
        repaint();
    }

    /**
    *	Set line style
    */
    public void setLineStyle(Stroke lineStroke, Paint linePaint) {
        treePainter.setLineStyle(lineStroke, linePaint);
        repaint();
    }

    /**
    *	Set hilight style
    */
    public void setHilightStyle(Stroke hilightStroke, Paint hilightPaint) {
        treePainter.setHilightStyle(hilightStroke, hilightPaint);
        repaint();
    }

    /**
     *	Set label style.
     */
    public void setLabelStyle(Font labelFont, Paint labelPaint) {
        treePainter.setLabelStyle(labelFont, labelPaint);
        repaint();
    }

    /**
     *	Set hilight label style.
     */
    public void setHilightLabelStyle(Font hilightLabelFont, Paint hilightLabelPaint) {
        treePainter.setHilightLabelStyle(hilightLabelFont, hilightLabelPaint);
        repaint();
    }

    public void paintComponent(Graphics g) {

        if (tree == null) return;

        Dimension size = getSize();
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        treePainter.paintTree(g2d, size, tree);

    }

    //********************************************************************
    // Printable interface
    //********************************************************************

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return(NO_SUCH_PAGE);
        } else {
            Graphics2D g2d = (Graphics2D)g;

            double x0 = pageFormat.getImageableX();
            double y0 = pageFormat.getImageableY();

            double w0 = pageFormat.getImageableWidth();
            double h0 = pageFormat.getImageableHeight();

            double w1 = getWidth();
            double h1 = getHeight();

            double scale;

            if (w0 / w1 < h0 / h1) {
                scale = w0 / w1;
            } else {
                scale = h0 /h1;
            }

            g2d.translate(x0, y0);
            g2d.scale(scale, scale);

            // Turn off double buffering
            paint(g2d);
            // Turn double buffering back on
            return(PAGE_EXISTS);
        }
    }

    /**
     *	Add a plot listener
     */
    public void addListener(Listener listener) {

        listeners.add(listener);
    }

    /**
     * Tells tree listeners that a node has been clicked.
     */
    protected void fireNodeClickedEvent(Node node) {

        for (int i=0; i < listeners.size(); i++) {
            Listener listener = listeners.elementAt(i);
            listener.nodeClicked(tree, node);
        }
    }

    // Listeners

    private Vector<Listener> listeners = new Vector<Listener>();

    public interface Listener {

        public void nodeClicked(RootedTree tree, Node node);

    }

    public class Adaptor implements Listener {

        public void nodeClicked(RootedTree tree, Node node) { }

    }

    public class MListener extends MouseAdapter {

        public void mouseClicked(MouseEvent me) {

            Node node = treePainter.findNodeAtPoint(me.getPoint());

            fireNodeClickedEvent(node);
        }
    }
}
