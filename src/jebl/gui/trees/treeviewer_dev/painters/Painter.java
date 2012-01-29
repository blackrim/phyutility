package jebl.gui.trees.treeviewer_dev.painters;

import jebl.gui.trees.treeviewer_dev.TreePane;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * A painter draws a particular decoration onto the tree within a
 * rectangle.
 * @author Andrew Rambaut
 * @version $Id: Painter.java 370 2006-06-29 18:57:56Z rambaut $
 */
public interface Painter<T> {

    public enum Orientation {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT
    }

    public enum Justification {
        FLUSH,
        LEFT,
        RIGHT,
        CENTER
    }

	/**
	 * Called when the painter is installed in a TreePane. Gives the
	 * painter a handle on the TreePane so that it get additional
	 * information.
	 * @param treePane
	 */
	void setTreePane(TreePane treePane);

	/**
	 * If this is false then the painter should not be displayed.
	 * @return is visible?
	 */
    boolean isVisible();

	/**
	 * Called to calibrate the painters for a given graphics context. This should
	 * work out the preferred width and height (perhaps for the current font).
	 * @param g2
	 * @param item
	 */
    Rectangle2D calibrate(Graphics2D g2, T item);

	/**
	 * Called to actually paint into the current graphics context. The painter should
	 * respect the bounds.
	 * @param g2
	 * @param item
	 * @param justification
	 * @param bounds
	 */
    void paint(Graphics2D g2, T item, Justification justification, Rectangle2D bounds);

    double getPreferredWidth();
    double getPreferredHeight();
    double getHeightBound();

    void addPainterListener(PainterListener listener);
    void removePainterListener(PainterListener listener);
}
