package jebl.gui.trees.treeviewer_dev.treelayouts;

import jebl.evolution.trees.RootedTree;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Andrew Rambaut
 * @version $Id: TreeLayout.java 724 2007-06-11 16:25:39Z rambaut $
 */
public interface TreeLayout {

	public enum AxisType {
        CONTINUOUS,
        DISCRETE
    }

	void layout(RootedTree tree, TreeLayoutCache cache);

    /**
     * Add a listener for this layout
     *
     * @param listener
     */
    void addTreeLayoutListener(TreeLayoutListener listener);

    /**
     * Remove a listener from this layout
     *
     * @param listener
     */
    void removeTreeLayoutListener(TreeLayoutListener listener);

    /**
     * Return whether the x axis is continuous or discrete
     *
     * @return the axis type
     */
    AxisType getXAxisType();

    /**
     * Return whether the y axis is continuous or discrete
     *
     * @return the axis type
     */
    AxisType getYAxisType();

    /**
     * Return whether this layout displays a root branch
     * @return showing root branch?
     */
    boolean isShowingRootBranch();

    /**
     * Return whether this layout is showing a branch colouring
     * @return showing colouring?
     */
    boolean isShowingColouring();

    /**
     * Return whether the two axis scales should be maintained
     * relative to each other
     *
     * @return a boolean
     */
    boolean maintainAspectRatio();

    double getHeightOfPoint(Point2D point);

    /**
     * Return a line that defines a particular height. Some layouts
     * won't be able to produce this and may throw an UnsupportedOperationException.
     *
     * @param height
     * @return the line
     */
    Shape getHeightLine(double height);

    /**
     * Return a shape that defines a particular height interval. Some layouts
     * won't be able to produce this and may throw an UnsupportedOperationException.
     *
     * @param height1
     * @param height2
     * @return the area
     */
    Shape getHeightArea(double height1, double height2);

    String getBranchColouringAttributeName();

    void setBranchColouringAttributeName(String colouringAttributeName);

	String getCartoonAttributeName();

    void setCartoonAttributeName(String cartoonAttributeName);

	String getCollapsedAttributeName();

    void setCollapsedAttributeName(String collapsedAttributeName);
}
