package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.util.NumberFormatter;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Andrew Rambaut
 * @version $Id: BasicLabelPainter.java 705 2007-05-09 02:32:33Z stevensh $
 */
public class BasicLabelPainter extends AbstractPainter<Node> {

    public static final String TAXON_NAMES = "Taxon Names";
    public static final String NODE_HEIGHTS = "Node Heights";
    public static final String BRANCH_LENGTHS = "Branch Lengths";


    public BasicLabelPainter(String title, RootedTree tree, PainterIntent intent) {
        this(title, tree, intent, 6);
    }


    public enum PainterIntent {
        NODE,
        BRANCH,
        TIP
    }

    public BasicLabelPainter(String title, RootedTree tree, PainterIntent intent, int defaultSize) {
        this.title = title;

        this.defaultFontSize = defaultSize;
        taxonLabelFont = new Font("sansserif", Font.PLAIN, defaultSize);

        this.tree = tree;

        hasNumericAttributes = false;

        Set<String> names = new TreeSet<String>();

        // by default, node properties are on nodes for rooted trees, on branches for unrooted trees
        this.attribute = null;

        List<String> sources = new ArrayList<String>();
        boolean wantHeightsIfPossible = false;
        boolean wantBranchesIfPossible = false;
        boolean addNodeAttributes = false;
        switch( intent ) {
            case TIP: {
                sources.add(TAXON_NAMES);
                wantHeightsIfPossible = true;
                for (Node node : tree.getExternalNodes() ) {
                    for(String s : node.getAttributeNames()){
                        if(!s.equalsIgnoreCase("size") && !s.equalsIgnoreCase("first residues"))
                            names.add(s);
                    }
                    //names.addAll(node.getAttributeNames());
                }
                break;
            }
            case NODE: {
                wantHeightsIfPossible = true;
                addNodeAttributes = !tree.conceptuallyUnrooted();
                break;
            }
            case BRANCH: {
                wantBranchesIfPossible = true;
                addNodeAttributes = tree.conceptuallyUnrooted();
                break;
            }
        }

        if( addNodeAttributes ) {
            for( Node node : tree.getInternalNodes() ) {
                names.addAll(node.getAttributeNames());
            }
        }

        if( wantHeightsIfPossible && tree.hasHeights() && !tree.conceptuallyUnrooted() ) {
            sources.add(NODE_HEIGHTS);
            hasNumericAttributes = true;
        }

        if( wantBranchesIfPossible && tree.hasLengths()) {
            sources.add(BRANCH_LENGTHS);
            hasNumericAttributes = true;
        }

        sources.addAll(names);

        if (this.attribute == null && sources.size() > 0) {
            this.attribute = sources.get(0);
        } else {
            this.attribute = "";
        }

        this.attributes = new String[sources.size()];
        sources.toArray(this.attributes);

        formatter = new NumberFormatter(4);
    }

    public void setTree(RootedTree tree) {
        this.tree = tree;
    }

    protected String getLabel(Node node) {
        String prefix = " ";
        String suffix = " ";
        if (attribute.equalsIgnoreCase(TAXON_NAMES)) {
            return prefix+tree.getTaxon(node).getName()+suffix;
        }

        if( tree instanceof RootedTree ) {
            final RootedTree rtree = (RootedTree) tree;

            if (attribute.equalsIgnoreCase(NODE_HEIGHTS) ) {
                return prefix+getFormattedValue(rtree.getHeight(node))+suffix;
            } else if (attribute.equalsIgnoreCase(BRANCH_LENGTHS) ) {
                return prefix+getFormattedValue(rtree.getLength(node))+suffix;
            }
        }

        final Object value = node.getAttribute(attribute);
        if (value != null) {
            if (value instanceof Double) {
                return prefix+formatter.getFormattedValue((Double) value)+suffix;
            }
            if(value instanceof Date){
                DateFormat format = new SimpleDateFormat("dd MMM yyyy h:mm a");
                return  prefix+format.format((Date)value)+suffix;
            }
            String s = value.toString();
            //limit node labels to 15 chars (plus ...)
            //if(s.length() > 15)
            //    return s.substring(0,15)+"...";
            return prefix+s+suffix;
        }
        return null;
    }

    private String getFormattedValue(double d){
        if(d == 0)
            return "0";
        return formatter.getFormattedValue(d);
    }

    public float getFontSize() {
        return defaultFontSize;
    }

    public float getFontMinSize() {
        return defaultMinFontSize;
    }

    private float defaultFontSize;
    private float defaultMinFontSize;
    private int defaultDigits = 4;

    public boolean isVisible() {
        return visible;
    }

    private static final String isOPenKey = "_isopen";

    public void setVisible(boolean visible) {
        this.visible = visible;
        firePainterChanged();
        PREFS.putBoolean(getTitle() + isOPenKey, visible);
    }

//    public void calibrate(Graphics2D g2, Node item) {
//        final Font oldFont = g2.getFont();
//        g2.setFont(taxonLabelFont);
//
//        final FontMetrics fm = g2.getFontMetrics();
//        preferredHeight = fm.getHeight();
//        preferredWidth = 0;
//
//        String label = getLabel(item);
//        if (label != null) {
//            Rectangle2D rect = fm.getStringBounds(label, g2);
//            preferredWidth = rect.getWidth();
//        }
//
//        yOffset = (float)fm.getAscent();
//
//        g2.setFont(oldFont);
//    }

     public void calibrate(Graphics2D g2) {
        final Font oldFont = g2.getFont();
        g2.setFont(taxonLabelFont);

        final FontMetrics fm = g2.getFontMetrics();
        preferredHeight = fm.getHeight();

        yOffset = (float)fm.getAscent();

        g2.setFont(oldFont);
    }

    public double getWidth(Graphics2D g2, Node item) {
        final String label = getLabel(item);
        if( label != null ) {
            final Font oldFont = g2.getFont();
            g2.setFont(taxonLabelFont);

            final FontMetrics fm = g2.getFontMetrics();
            Rectangle2D rect = fm.getStringBounds(label, g2);
            g2.setFont(oldFont);
            return rect.getWidth();
        }

        return 0.0;
    }

    public double getPreferredHeight() {
        return preferredHeight;
    }

    public double getHeightBound() {
        return preferredHeight + yOffset;
    }

    public boolean setFontSize(float size, boolean fire) {
        if( defaultFontSize != size ) {
            taxonLabelFont = taxonLabelFont.deriveFont(size);
            defaultFontSize = size;
            if( fire ) firePainterChanged();
            return true;
        }
        return false;
    }

    private boolean setFontMinSize(float fontsize, boolean fire) {
        if( defaultMinFontSize != fontsize ) {
            defaultMinFontSize = fontsize;
            if( fire ) firePainterChanged();
            return true;
        }
        return false;
    }

    private void setSignificantDigits(int digits) {
        assert formatter != null;
        
        formatter.setSignificantFigures(digits);
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

    public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
        final Font oldFont = g2.getFont();

        if (background != null) {
            g2.setPaint(background);
            g2.fill(bounds);
        }

        if (borderPaint != null && borderStroke != null) {
            g2.setPaint(borderPaint);
            g2.setStroke(borderStroke);
            g2.draw(bounds);
        }

        g2.setPaint(foreground);
        g2.setFont(taxonLabelFont);

        final String label = getLabel(item);
        if (label != null) {

            Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

            float xOffset = 0;
            float y = yOffset + (float) bounds.getY();
            switch (justification) {
                case CENTER:
                    //xOffset = (float)(-rect.getWidth()/2.0);
                    //y = yOffset + (float) rect.getY();
                   // y = (float)bounds.getHeight()/2;
                    //xOffset = (float) (bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
                    break;
                case FLUSH:
                case LEFT:
                    xOffset = (float) bounds.getX();
                    break;
                case RIGHT:
                    xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized alignment enum option");
            }

            g2.drawString(label, xOffset, y);
            //g2.draw(bounds);
        }

        g2.setFont(oldFont);
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
        firePainterChanged();
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // nothing to do
    }

    private static Preferences PREFS = Preferences.userNodeForPackage(BasicLabelPainter.class);

    public List<Controls> getControls(boolean detachPrimaryCheckbox) {

        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final JCheckBox showTextCHeckBox = new JCheckBox("Show " + getTitle());
            if (! detachPrimaryCheckbox) {
                optionsPanel.addComponent(showTextCHeckBox);
            }

            visible = PREFS.getBoolean(getTitle() + isOPenKey, isVisible());
            showTextCHeckBox.setSelected(visible);

            showTextCHeckBox.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final boolean selected = showTextCHeckBox.isSelected();
                    if( isVisible() != selected ) {
                        setVisible(selected);
                    }
                }
            });

            final String whatPrefKey = getTitle() + "_whatToDisplay";
            String[] attributes = getAttributes();
            final JComboBox combo1 = new JComboBox(attributes);
            combo1.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    String attribute = (String) combo1.getSelectedItem();
                    setAttribute(attribute);
                    PREFS.put(whatPrefKey, attribute);
                }
            });

            final String whatToDisplay = PREFS.get(whatPrefKey, null);
            if( whatToDisplay != null ) {
                int i = Arrays.asList(attributes).indexOf(whatToDisplay);
                if( i >= 0 ) {
                    combo1.setSelectedIndex(i);
                }
            }

            optionsPanel.addComponentWithLabel("Display:", combo1);
            final JSpinner fontSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultFontSize, 0.01, 48, 1));

            optionsPanel.addComponentWithLabel("Font Size:", fontSizeSpinner);
            //final boolean xselected = showTextCHeckBox.isSelected();
            //label1.setEnabled(selected);
            //fontSizeSpinner.setEnabled(selected);

            final String fontSizePrefKey = getTitle() + "_fontsize";
            final float fontsize = PREFS.getFloat(fontSizePrefKey, taxonLabelFont.getSize());
            setFontSize(fontsize, false);
            fontSizeSpinner.setValue(fontsize);

            fontSizeSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final float size = ((Double) fontSizeSpinner.getValue()).floatValue();
                    setFontSize(size, true);
                    PREFS.putFloat(fontSizePrefKey, size);
                }
            });


            //-----------------------------------------



            //final boolean xselected = showTextCHeckBox.isSelected();
            //label1.setEnabled(selected);
            //fontSizeSpinner.setEnabled(selected);

            final String fontMinSizePrefKey = getTitle() + "_fontminsize";
            final float size = PREFS.getFloat(fontMinSizePrefKey, 6);
            setFontMinSize(size, false);

            final JSpinner fontMinSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultMinFontSize, 0.01, 48, 1));
            optionsPanel.addComponentWithLabel("Minimum Size:", fontMinSizeSpinner);
            //fontMinSizeSpinner.setValue(size);

            fontMinSizeSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final float size = ((Double) fontMinSizeSpinner.getValue()).floatValue();
                    setFontMinSize(size, true);
                    PREFS.putFloat(fontMinSizePrefKey, size);
                }
            });
            //-------------------------
            final JSpinner digitsSpinner = new JSpinner(new SpinnerNumberModel(defaultDigits, 2, 14, 1));

            if( hasNumericAttributes ) {
                final JLabel label2 = optionsPanel.addComponentWithLabel("Significant Digits:", digitsSpinner);
                // label2.setEnabled(selected);
                //  digitsSpinner.setEnabled(selected);

                final String digitsPrefKey = getTitle() + "_sigDigits";
                final int digits = PREFS.getInt(digitsPrefKey, defaultDigits);
                setSignificantDigits(digits);
                digitsSpinner.setValue(digits);

                digitsSpinner.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        final int digits = (Integer)digitsSpinner.getValue();
                        setSignificantDigits(digits);
                        PREFS.putInt(digitsPrefKey, digits);
                    }
                });
            }

            controls = new Controls(getTitle(), optionsPanel, false, false, detachPrimaryCheckbox ? showTextCHeckBox : null);
        }

        controlsList.add(controls);

        return controlsList;
    }



    public void setSettings(ControlsSettings settings) {
    }

    public void getSettings(ControlsSettings settings) {
    }

    private Controls controls = null;

    public String getTitle() {
        return title;
    }

    private final String title;

    private Paint foreground = Color.BLACK;
    private Paint background = null;
    private Paint borderPaint = null;
    private Stroke borderStroke = null;

    private Font taxonLabelFont;
    //private double preferredWidth;
    private double preferredHeight;
    private float yOffset;

    private boolean visible = true;

    private NumberFormatter formatter = null;
    private boolean hasNumericAttributes = false;

    private Tree tree;
    protected String attribute;
    protected String[] attributes;
}
