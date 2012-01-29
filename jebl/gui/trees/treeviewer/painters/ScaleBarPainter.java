package jebl.gui.trees.treeviewer.painters;

import jebl.gui.trees.treeviewer.TreePane;
import jebl.gui.trees.treeviewer.TreeViewer;
import org.virion.jam.components.RealNumberField;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: ScaleBarPainter.java 674 2007-03-28 05:23:26Z stevensh $
 */
public class ScaleBarPainter extends AbstractPainter<TreePane> {
    private int defaultFontSize;
    private double scaleRange;
    private double userScaleRange = 0.0;

    public ScaleBarPainter() {
        this(0.0, 12);
    }

    public ScaleBarPainter(double scaleRange) {
        this(scaleRange, 12);
    }

    public ScaleBarPainter(int defaultSize) {
        this(0.0, defaultSize);
    }

    public ScaleBarPainter(double scaleRange, int defaultSize) {
        this.scaleRange = scaleRange;
        this.defaultFontSize = defaultSize;
        scaleFont = new Font("sansserif", Font.PLAIN, defaultFontSize);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        firePainterChanged();
    }

    public void calibrate(Graphics2D g2) {
        Font oldFont = g2.getFont();
        g2.setFont(scaleFont);

        FontMetrics fm = g2.getFontMetrics();

//        if( userScaleRange != 0.0 ) {
//            scaleRange = userScaleRange;
//        } else {
//            final double treeScale = treePane.getTreeScale();
//            if( treeScale == 0.0 ) {
//                scaleRange = 0.0;
//            } else {
//                int w10 = treePane.getWidth() / 10;
//
//                double low = w10 /treeScale;
//                double b = -(Math.ceil(Math.log10(low)) - 1);
//                for(int n = 0; n < 3; ++n) {
//                    double factor = Math.pow(10, b);
//                    double x = ((int)(low * factor) + 1)/factor;
//                    if( n == 2 || x < w10 * 2 ) {
//                        scaleRange = x;
//                        break;
//                    }
//                    ++b;
//                }
//            }
//        }

        final double labelHeight = fm.getHeight();

//        preferredWidth = treePane.getTreeScale() * scaleRange;
        preferredHeight = labelHeight + 4 + scaleBarStroke.getLineWidth();

        yOffset = (float) (fm.getAscent()) + 4 + scaleBarStroke.getLineWidth();

        g2.setFont(oldFont);
    }

    public void paint(Graphics2D g2, TreePane treePane, Justification justification, Rectangle2D bounds) {
        Font oldFont = g2.getFont();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();

        if (background != null) {
            g2.setPaint(background);
            g2.fill(bounds);
        }

        if (borderPaint != null && borderStroke != null) {
            g2.setPaint(borderPaint);
            g2.setStroke(borderStroke);
            g2.draw(bounds);
        }

        g2.setFont(scaleFont);

        // sets scale range
        final double preferredWidth = getWidth(g2, treePane);

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

        g2.setPaint(foreground);
        g2.setStroke(scaleBarStroke);

        g2.draw(new Line2D.Double(x1, bounds.getY(), x2, bounds.getY()));

        g2.drawString(label, xOffset, yOffset + (float) bounds.getY());

        g2.setFont(oldFont);
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
    }

    public double getWidth(Graphics2D g2, TreePane treePane) {
        Font oldFont = g2.getFont();
        g2.setFont(scaleFont);

        FontMetrics fm = g2.getFontMetrics();

        if( userScaleRange != 0.0 ) {
            scaleRange = userScaleRange;
        } else {
            final double treeScale = treePane.getTreeScale();
            if( treeScale == 0.0 ) {
                scaleRange = 0.0;
            } else {
                int w10 = treePane.getWidth() / 10;

                double low = w10 /treeScale;
                double b = -(Math.ceil(Math.log10(low)) - 1);
                for(int n = 0; n < 3; ++n) {
                    double factor = Math.pow(10, b);
                    double x = ((int)(low * factor) + 1)/factor;
                    if( n == 2 || x < w10 * 2 ) {
                        scaleRange = x;
                        break;
                    }
                    ++b;
                }
            }
        }

        double preferredWidth = treePane.getTreeScale() * scaleRange;
        g2.setFont(oldFont);
        return preferredWidth;
    }

    public double getPreferredHeight() {
        return preferredHeight;
    }

    public double getHeightBound() {
        return preferredHeight + yOffset;
    }

    public void setScaleRange(double scaleRange) {
        this.userScaleRange = scaleRange;
        firePainterChanged();
    }

    public void setFontSize(float size) {
        scaleFont = scaleFont.deriveFont(size);
        firePainterChanged();
    }

    public void setForeground(Paint foreground) {
        this.foreground = foreground;
        firePainterChanged();
    }

    public void setBackground(Paint background) {
        this.background = background;
        firePainterChanged();
    }

    public void setBorder(Paint borderPaint, Stroke borderStroke) {
        this.borderPaint = borderPaint;
        this.borderStroke = borderStroke;
        firePainterChanged();
    }

    public void setLineWeight(float weight) {
        this.scaleBarStroke = new BasicStroke(weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    public List<Controls> getControls(boolean detachPrimaryCheckbox) {
        final Preferences PREFS = Preferences.userNodeForPackage(TreeViewer.class);       
        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final JCheckBox showScaleBarCB = new JCheckBox("Show Scale Bar");
            if (! detachPrimaryCheckbox) {
                optionsPanel.addComponent(showScaleBarCB);
            }

            showScaleBarCB.setSelected(isVisible());

            final RealNumberField text1 = new RealNumberField(0.0, Double.MAX_VALUE);
            text1.setValue(scaleRange);

            text1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    Double value = text1.getValue();
                    if (value != null) {
                        setScaleRange(value);
                        PREFS.putDouble("scalebar_scaleRange",value);
                    }
                }
            });
            text1.setText(PREFS.getDouble("scalebar_scaleRange",0.0));
            final JLabel label1 = optionsPanel.addComponentWithLabel("Scale Range:", text1, true);

            final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(defaultFontSize, 0.01, 48.0, 1.0));

            spinner1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setFontSize(((Double) spinner1.getValue()).floatValue());
                    PREFS.putDouble("scalebar_fontSize",(Double)spinner1.getValue());
                }
            });
            spinner1.setValue(PREFS.getDouble("scalebar_fontSize",defaultFontSize));
            final JLabel label2 = optionsPanel.addComponentWithLabel("Font Size:", spinner1);

            final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

            spinner2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setLineWeight(((Double) spinner2.getValue()).floatValue());
                    PREFS.putDouble("scalebar_lineWeight",(Double)spinner2.getValue());
                }
            });
            spinner2.setValue(PREFS.getDouble("scalebar_lineWeight",1));
            final JLabel label3 = optionsPanel.addComponentWithLabel("Line Weight:", spinner2);

            final boolean isSelected = showScaleBarCB.isSelected();
            label1.setEnabled(isSelected);
            text1.setEnabled(isSelected);
            label2.setEnabled(isSelected);
            spinner1.setEnabled(isSelected);
            label3.setEnabled(isSelected);
            spinner2.setEnabled(isSelected);

            showScaleBarCB.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final boolean isSelected = showScaleBarCB.isSelected();
                    label1.setEnabled(isSelected);
                    text1.setEnabled(isSelected);
                    label2.setEnabled(isSelected);
                    spinner1.setEnabled(isSelected);
                    label3.setEnabled(isSelected);
                    spinner2.setEnabled(isSelected);

                    setVisible(isSelected);
                }
            });

            controls = new Controls("Scale Bar", optionsPanel, false, false, detachPrimaryCheckbox ? showScaleBarCB : null);
        }

        controlsList.add(controls);

        return controlsList;
    }

    public void setSettings(ControlsSettings settings) {
    }

    public void getSettings(ControlsSettings settings) {
    }

    private Controls controls = null;

    private boolean visible = true;

    private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;
    private BasicStroke scaleBarStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private Font scaleFont;
    private double preferredHeight;
    //private double preferredWidth;

    private float yOffset;
}