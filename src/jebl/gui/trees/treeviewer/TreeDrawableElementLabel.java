package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Joseph Heled
 * @version $Id$
 *          <p/>
 */
public abstract class TreeDrawableElementLabel extends TreeDrawableElement {
    protected Rectangle2D bounds;
    protected AffineTransform transform;
    private int priority;

    public TreeDrawableElementLabel(Node node, Rectangle2D labelBounds, AffineTransform transform, int priority) {
        super(node);
        this.bounds = labelBounds;
        this.transform = transform;
        this.priority = priority;
    }

    public boolean intersects(TreeDrawableElement e) {
        if( e instanceof TreeDrawableElementLabel ) {
            TreeDrawableElementLabel l = (TreeDrawableElementLabel)e;
            return intersects(this, l);
        }
        assert false;
        return false;
    }

    public boolean hit(Graphics2D g2, Rectangle rect) {
       return g2.hit(rect, getLableShape(), false);
    }
    
    public Rectangle2D getBounds() {
        return getLableShape().getBounds();
    }

    private Shape getLableShape() {
        return transform.createTransformedShape(bounds);
    }

    public int getPriority() {
        return priority;
    }

    static boolean intersects(TreeDrawableElementLabel l1, TreeDrawableElementLabel l2) {
     //  System.out.println(l1.getDebugName() + " transform " + l1.transform.toString());
     //  System.out.println(l2.getDebugName() + " transform " + l2.transform.toString());

        // todo (efficency) when lables are not rotated this is not required, as the bounds and real space taken
        // todo by label are the same
        final double[] l2p = l2.getPoints();

        final double[] l1p = l1.getPoints();

        Line2D[] ln1 = {
                new Line2D.Double(l1p[0], l1p[1], l1p[2], l1p[3]) ,
                new Line2D.Double(l1p[2], l1p[3], l1p[4], l1p[5]) ,
                new Line2D.Double(l1p[4], l1p[5], l1p[6], l1p[7]) ,
                new Line2D.Double(l1p[6], l1p[7], l1p[0], l1p[1]) };
         Line2D[] ln2 = {
                new Line2D.Double(l2p[0], l2p[1], l2p[2], l2p[3]) ,
                new Line2D.Double(l2p[2], l2p[3], l2p[4], l2p[5]) ,
                new Line2D.Double(l2p[4], l2p[5], l2p[6], l2p[7]) ,
                new Line2D.Double(l2p[6], l2p[7], l2p[0], l2p[1]) };

        for(int k = 0; k < 4; ++k) {
            for(int j = 0; j < 4; ++j) {
                if( ln1[k].intersectsLine(ln2[j]) ) {
                    return true;
                }
            }
        }
        return false;
    }

    // order must follow the path
    private double[] getPoints() {
        double[] xy = {
                bounds.getMinX(), bounds.getMinY(),
                bounds.getMinX(), bounds.getMaxY(),
                bounds.getMaxX(), bounds.getMaxY(),
                bounds.getMaxX(), bounds.getMinY()};

        double[] txy = new double[8];
        transform.transform(xy, 0, txy, 0, 4);
        return txy;
    }
}
