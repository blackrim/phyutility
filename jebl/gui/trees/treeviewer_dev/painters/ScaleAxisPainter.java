package jebl.gui.trees.treeviewer_dev.painters;

import jebl.gui.trees.treeviewer_dev.TreePane;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.*;
import java.util.Collection;

import org.virion.jam.controlpalettes.ControlPalette;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: ScaleBarPainter.java,v 1.7 2006/11/21 16:10:24 rambaut Exp $
 */
public class ScaleAxisPainter extends LabelPainter<TreePane> {

    public ScaleAxisPainter() {
        this(0.0);
    }

    public ScaleAxisPainter(double scaleRange) {
        this.scaleRange = scaleRange;
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Rectangle2D calibrate(Graphics2D g2, TreePane treePane) {
        Font oldFont = g2.getFont();
        g2.setFont(getFont());

        FontMetrics fm = g2.getFontMetrics();
        double labelHeight = fm.getHeight();

        preferredWidth = treePane.getTreeScale() * scaleRange;
        preferredHeight = labelHeight + topMargin + bottomMargin + scaleBarStroke.getLineWidth();

        yOffset = (float) (fm.getAscent() + topMargin + bottomMargin) + scaleBarStroke.getLineWidth();

        g2.setFont(oldFont);

        return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
    }

    public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {
        Font oldFont = g2.getFont();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

        if (getBackground() != null) {
            g2.setPaint(getBackground());
            g2.fill(bounds);
        }

        if (getBorderPaint() != null && getBorderStroke() != null) {
            g2.setPaint(getBorderPaint());
            g2.setStroke(getBorderStroke());
            g2.draw(bounds);
        }

        g2.setFont(getFont());

        // we don't need accuracy but a nice short number
        final String label = Double.toString(scaleRange);

        Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

        double x1, x2;
        float xOffset;
        switch (justification) {
            case CENTER:
                xOffset = (float) (bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
                x1 = (bounds.getX() + (bounds.getWidth() - preferredWidth) / 2.0);
                x2 = x1 + preferredWidth;
                break;
            case FLUSH:
            case LEFT:
                xOffset = (float) bounds.getX();
                x1 = bounds.getX();
                x2 = x1 + preferredWidth;
                break;
            case RIGHT:
                xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
                x2 = bounds.getX() + bounds.getWidth();
                x1 = x2 - preferredWidth;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized alignment enum option");
        }

        g2.setPaint(getForeground());
        g2.setStroke(getScaleBarStroke());

        g2.draw(new Line2D.Double(x1, bounds.getY() + topMargin, x2, bounds.getY() + topMargin));

        g2.drawString(label, xOffset, yOffset + (float) bounds.getY());

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
    }
    /**
    *	Get the maximum width of the labels of an axis
    */
    protected double getMaxTickLabelWidth(Graphics2D g2)
    {
        String label;
        double width;
        double maxWidth = 0;

        if (axis.getLabelFirst()) { // Draw first minor tick as a major one (with a label)
            label = axis.getFormatter().format(axis.getMinorTickValue(0, -1));
            width = g2.getFontMetrics().stringWidth(label);
            if (maxWidth < width)
                maxWidth = width;
        }
        int n = axis.getMajorTickCount();
        for (int i = 0; i < n; i++) {
            label = axis.getFormatter().format(axis.getMajorTickValue(i));
            width = g2.getFontMetrics().stringWidth(label);
            if (maxWidth < width)
                maxWidth = width;
        }
        if (axis.getLabelLast()) { // Draw first minor tick as a major one (with a label)
            label = axis.getFormatter().format(axis.getMinorTickValue(0, n - 1));
            width = g2.getFontMetrics().stringWidth(label);
            if (maxWidth < width)
                maxWidth = width;
        }

        return maxWidth;
    }

//    protected void paintAxis(Graphics2D g2)
//    {
//        int n1 = axis.getMajorTickCount();
//        int n2, i, j;
//
//        n2 = axis.getMinorTickCount(-1);
//        if (axis.getLabelFirst()) { // Draw first minor tick as a major one (with a label)
//
//            paintMajorTick(g2, axis.getMinorTickValue(0, -1));
//
//            for (j = 1; j < n2; j++) {
//                paintMinorTick(g2, axis.getMinorTickValue(j, -1));
//            }
//        } else {
//
//            for (j = 0; j < n2; j++) {
//                paintMinorTick(g2, axis.getMinorTickValue(j, -1));
//            }
//        }
//
//        for (i = 0; i < n1; i++) {
//
//            paintMajorTick(g2, axis.getMajorTickValue(i));
//            n2 = axis.getMinorTickCount(i);
//
//            if (i == (n1-1) && axis.getLabelLast()) { // Draw last minor tick as a major one
//
//                paintMajorTick(g2, axis.getMinorTickValue(0, i));
//
//                for (j = 1; j < n2; j++) {
//                    paintMinorTick(g2, axis.getMinorTickValue(j, i));
//                }
//            } else {
//
//                for (j = 0; j <  n2; j++) {
//                    paintMinorTick(g2, axis.getMinorTickValue(j, i));
//                }
//            }
//        }
//    }
//
//    protected void paintMajorTick(Graphics2D g2, double value)
//    {
//        g2.setPaint(getForeground());
//        g2.setStroke(getScaleBarStroke());
//
//            String label = axis.getFormatter().format(value);
//            double pos = transformX(value);
//
//            Line2D line = new Line2D.Double(pos, plotBounds.getMaxY(), pos, plotBounds.getMaxY() + majorTickSize);
//            g2.draw(line);
//
//            g2.setPaint(getForeground());
//            double width = g2.getFontMetrics().stringWidth(label);
//            g2.drawString(label, (float)(pos - (width / 2)), (float)(plotBounds.getMaxY() + (majorTickSize * 1.25) + xTickLabelOffset));
//    }
//
//    protected void paintMinorTick(Graphics2D g2, double value)
//    {
//
//        g2.setPaint(getForeground());
//        g2.setStroke(getScaleBarStroke());
//
//            double pos = transformX(value);
//
//            Line2D line = new Line2D.Double(pos, plotBounds.getMaxY(), pos, plotBounds.getMaxY() + minorTickSize);
//            g2.draw(line);
//    }
//
//    /**
//    *	Transform a chart co-ordinates into a drawing co-ordinates
//    */
//    protected double transformX(double value) {
//        return ((axis.transform(value) - axis.transform(axis.getMinAxis())) * xScale) + xOffset;
//    }
//

    public double getPreferredWidth() {
        return preferredWidth;
    }

    public double getPreferredHeight() {
        return preferredHeight;
    }

    public double getHeightBound() {
        return preferredHeight + yOffset;
    }

    public BasicStroke getScaleBarStroke() {
        return scaleBarStroke;
    }

    public void setScaleBarStroke(BasicStroke scaleBarStroke) {
        this.scaleBarStroke = scaleBarStroke;
        firePainterChanged();
    }

    public double getScaleRange() {
        return scaleRange;
    }

    public void setScaleRange(double scaleRange) {
        this.userScaleRange = scaleRange;
        calculateScaleRange();
        firePainterChanged();
    }

    public void setAutomaticScale(boolean automaticScale) {
        this.automaticScale = automaticScale;
        calculateScaleRange();
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    public void calculateScaleRange() {
        if( !automaticScale && userScaleRange != 0.0 ) {
            scaleRange = userScaleRange;
        } else {
            RootedTree tree = treePane.getTree();
            if (tree != null) {
                final double treeHeight = tree.getHeight(tree.getRootNode());

                if( treeHeight == 0.0 ) {
                    scaleRange = 0.0;
                } else {

                    double low = treeHeight / 10.0;
                    double b = -(Math.ceil(Math.log10(low)) - 1);
                    for(int n = 0; n < 3; ++n) {
                        double factor = Math.pow(10, b);
                        double x = ((int)(low * factor) + 1)/factor;
                        if( n == 2 || x < treeHeight / 5.0 ) {
                            scaleRange = x;
                            break;
                        }
                        ++b;
                    }
                }
            }
        }

    }

    public String[] getAttributes() {
        return new String[0];
    }

    public void setupAttributes(Collection<? extends Tree> trees) {
        // nothing to do...
    }

    public void setDisplayAttribute(String displayAttribute) {
    }

    private BasicStroke scaleBarStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private ScaleBarAxis axis = new ScaleBarAxis(ScaleBarAxis.AT_DATA, ScaleBarAxis.AT_DATA);

    private double scaleRange;
    private double topMargin = 4.0;
    private double bottomMargin = 4.0;
    private double userScaleRange = 0.0;
    private boolean automaticScale = true;

    private double preferredHeight;
    private double preferredWidth;

    private float yOffset;

    protected TreePane treePane;
}
