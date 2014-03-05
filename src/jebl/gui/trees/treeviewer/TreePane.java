package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SortedRootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import jebl.evolution.trees.Utils;
import jebl.gui.trees.treeviewer.decorators.BranchDecorator;
import jebl.gui.trees.treeviewer.painters.BasicLabelPainter;
import jebl.gui.trees.treeviewer.painters.Painter;
import jebl.gui.trees.treeviewer.painters.PainterListener;
import jebl.gui.trees.treeviewer.treelayouts.TreeLayout;
import jebl.gui.trees.treeviewer.treelayouts.TreeLayoutListener;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsProvider;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
//import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 *
 * @author Andrew Rambaut
 * @version $Id: TreePane.java 736 2007-07-19 01:40:22Z stevensh $
 */
public class TreePane extends JComponent implements ControlsProvider, PainterListener, Printable {

    public static boolean goBackwards = false;
    public TreePane() {
        setBackground(UIManager.getColor("TextArea.background"));
    }

    public RootedTree getTree() {
        return tree;
    }

    public void setTree(RootedTree tree, Collection<Node> selectedNodes) {
        this.originalTree = tree;
        if (!originalTree.hasLengths()) {
            transformBranches = true;
        }

        Painter<?>[] pl = { taxonLabelPainter, nodeLabelPainter, branchLabelPainter };
        for( Painter<?> p : pl ) {
            if( p instanceof BasicLabelPainter ) {
                ((BasicLabelPainter)p).setTree(tree);
            }
        }


        selectedTaxa.clear();
        if( selectedNodes != this.selectedNodes ) {
            this.selectedNodes.clear();
        }
        if( selectedNodes != null ) {
            this.selectedNodes.addAll(selectedNodes);
        }

        setupTree();
    }

    private void setupTree() {
        tree = originalTree;

        if (orderBranches) {
            tree = new SortedRootedTree(tree, branchOrdering);
        }

        if (transformBranches || !this.tree.hasLengths()) {
            tree = new TransformedRootedTree(tree, branchTransform);
        }
        
        nodesInOrder = Utils.getNodes(tree, tree.getRootNode());
        treeLayout.setTree(tree);

        calibrated = false;
        invalidate();
        repaint();
    }

    public void setTreeLayout(TreeLayout treeLayout) {

        this.treeLayout = treeLayout;
        treeLayout.setTree(tree);
        treeLayout.addTreeLayoutListener(new TreeLayoutListener() {
            public void treeLayoutChanged() {
                calibrated = false;
                repaint();
            }
        });
        if (controlPalette != null) controlPalette.fireControlsChanged();
        calibrated = false;
        invalidate();
        repaint();
    }
//
//    public Rectangle2D getTreeBounds() {
//        return treeBounds;
//    }

    /**
     * This returns the scaling factor between the graphical image and the branch
     * lengths of the tree
     *
     * @return the tree scale
     */
    public double getTreeScale() {
        return treeScale;
    }

    public void painterChanged() {
        calibrated = false;
        repaint();
    }

    public void setBranchOrdering(boolean orderBranches, SortedRootedTree.BranchOrdering branchOrdering) {
        if( this.orderBranches != orderBranches || this.branchOrdering != branchOrdering ) {
            this.orderBranches = orderBranches;
            this.branchOrdering = branchOrdering;
            setupTree();
            PREFS.getBoolean(orderBranchesPREFSkey, orderBranches);
        }
    }

    public void setBranchTransform(boolean transformBranches, TransformedRootedTree.Transform branchTransform) {
        if( transformBranches != this.transformBranches || branchTransform != this.branchTransform ) {
            this.transformBranches = transformBranches;
            this.branchTransform = branchTransform;
            setupTree();
            PREFS.putBoolean(transformBanchesPREFSkey, transformBranches);
        }
    }

    public boolean isShowingRootBranch() {
        return showingRootBranch;
    }

    public void setShowingRootBranch(boolean showingRootBranch) {
        if( this.showingRootBranch != showingRootBranch ) {
            this.showingRootBranch = showingRootBranch;
            calibrated = false;
            repaint();
            PREFS.putBoolean(showRootPREFSkey, showingRootBranch);
        }
    }

    public void setAutoExpansion(final boolean auto) {
        this.autoExpantion = auto;
        setTreeAttributesForAutoExpansion();
        //calibrated = false;
        repaint();
        PREFS.putBoolean(autoExPREFSkey, auto);
    }

    public boolean isShowingTaxonCallouts() {
        return showingTaxonCallouts;
    }

    public void setShowingTaxonCallouts(boolean showingTaxonCallouts) {
        this.showingTaxonCallouts = showingTaxonCallouts;
        calibrated = false;
        repaint();
    }

    public void setSelectedNode(Node selectedNode) {
        selectedNodes.clear();
        selectedTaxa.clear();
        addSelectedNode(selectedNode);
    }

    public void setSelectedTaxon(Taxon selectedTaxon) {
        selectedNodes.clear();
        selectedTaxa.clear();
        addSelectedTaxon(selectedTaxon);
    }

    public void setSelectedClade(Node[] selectedNode) {
        selectedNodes.clear();
        selectedTaxa.clear();
        addSelectedClade(selectedNode, true);
    }

    public void setSelectedTaxa(Node selectedNode) {
        selectedNodes.clear();
        selectedTaxa.clear();
        addSelectedTaxa(selectedNode);
    }

    private boolean canSelectNode(final Node selectedNode) {
        return selectedNode != null && isNodeVisible(selectedNode);
    }

    public void addSelectedNode(Node selectedNode, boolean add) {
        if ( canSelectNode(selectedNode) ) {
            if( add ) {
                selectedNodes.add(selectedNode);
            } else {
                selectedNodes.remove(selectedNode);
            }
        }
        fireSelectionChanged();
        repaint();
    }

    public void addSelectedNode(Node selectedNode) {
       addSelectedNode(selectedNode, true);
    }

    public void addSelectedTaxon(Taxon selectedTaxon) {
        if (selectedTaxon != null) {
            selectedTaxa.add(selectedTaxon);
        }
        fireSelectionChanged();
        repaint();
    }

    /**
     *
     * @param selectedNode
     * @param add  true to add, false to remove existing selection
     */
    public void addSelectedClade(Node[] selectedNode, boolean add) {
        if ( canSelectNode(selectedNode[0]) ) {
            addSelectedChildClades(selectedNode, add);
        }

        if( viewSubtree ) calibrated = false;

        fireSelectionChanged();
        repaint();
    }

    private void addSelectedChildClades(Node[] selectedNode, boolean add) {
        if( selectedNode[1] == null ) {
            addSelectedChildClades(selectedNode[0], null, add);
        } else {
           addSelectedChildClades(tree.getRootNode(), selectedNode[0], add);
        }
    }

    private void addSelectedChildClades(Node selectedNode, Node exclude, boolean add) {
        if( selectedNode == exclude ) return;

        if( add ) {
            selectedNodes.add(selectedNode);
        } else {
            selectedNodes.remove(selectedNode);
        }

        for (Node child : tree.getChildren(selectedNode)) {
            addSelectedChildClades(child, exclude, add);
        }
    }

    public void addSelectedTaxa(Node selectedNode) {
        if (selectedNode != null) {
            addSelectedChildTaxa(selectedNode);
        }
        fireSelectionChanged();
        repaint();
    }

    private void addSelectedChildTaxa(Node selectedNode) {
        if (tree.isExternal(selectedNode)) {
            selectedTaxa.add(tree.getTaxon(selectedNode));
        }
        for (Node child : tree.getChildren(selectedNode)) {
            addSelectedChildTaxa(child);
        }
    }

    public void clearSelection() {
        selectedNodes.clear();
        selectedTaxa.clear();
        fireSelectionChanged();
        repaint();
    }

    public void annotateSelectedNodes(String name, Object value) {
        for (Node selectedNode : selectedNodes) {
            selectedNode.setAttribute(name, value);
        }
        repaint();
    }

    public void annotateSelectedTaxa(String name, Object value) {
        for (Taxon selectedTaxon : selectedTaxa) {
            selectedTaxon.setAttribute(name, value);
        }
        repaint();
    }

    final private String clpsdName = "&collapsed";
    final private String visibleAttributeName = "&visible";

    private boolean isNodeVisible(Node node) {
        return node.getAttribute(visibleAttributeName) == null;
    }

    private boolean isNodeCollapsed(Node node) {
         return node.getAttribute(clpsdName) != null;
    }

    private void setCladeVisisblty(final Node node, final boolean visible) {
        for( final Node child : tree.getChildren(node) ) {
            if( visible ) {
                child.removeAttribute(visibleAttributeName);
            } else {
                child.setAttribute(visibleAttributeName, Boolean.TRUE);
            }
            // leave collapsed subtress alone

            if( ! isNodeCollapsed(child) ) {
                setCladeVisisblty(child, visible);
            }
        }
    }


    private void expandContract(final Node selectedNode) {
         assert selectedNode != null;

        // no point for non internal nodes
        if( tree.isExternal(selectedNode) ) return;

        final boolean wasCollapsed = selectedNode.getAttribute(clpsdName) != null;
        if( wasCollapsed )  {
            selectedNode.removeAttribute(clpsdName);
        }
        // node should not be in collapsed mode when calling this
        setCladeVisisblty(selectedNode, wasCollapsed);
        if( !wasCollapsed ) {
            selectedNode.setAttribute(clpsdName, Boolean.TRUE);
        }
        //fireSelectionChanged();
        calibrated = false; // labels visibility may change 
        repaint();
    }

    void toggleExpandContract(final Node selectedNode) {
        if( canSelectNode(selectedNode) ) {
            autoExpantion = false;
            autoEx.setSelected(false);
            expandContract(selectedNode);
        }
    }

    private void setTreeAttributesForAutoExpansion() {
        for( Node node : nodesInOrder ) {
            node.removeAttribute(clpsdName);
            node.removeAttribute(visibleAttributeName);
        }

        if( autoExpantion ) {
            Set<Node> ignore = new HashSet<Node>();
            for( Node node : nodesInOrder ) {
                if( !ignore.contains(node) && node.getAttribute(clpsdName + "-auto") != null ) {
                    expandContract(node);   // todo problem (repints)
                    ignore.addAll(Utils.getNodes(tree, node));
                }
            }
        }
    }

    /**
     * Return whether the two axis scales should be maintained
     * relative to each other
     *
     * @return a boolean
     */
    public boolean maintainAspectRatio() {
        return treeLayout.maintainAspectRatio();
    }

    public void setTaxonLabelPainter(Painter<Node> taxonLabelPainter) {
        if (this.taxonLabelPainter != null) {
            this.taxonLabelPainter.removePainterListener(this);
        }
        this.taxonLabelPainter = taxonLabelPainter;
        if (this.taxonLabelPainter != null) {
            this.taxonLabelPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public Painter<Node> getTaxonLabelPainter() {
        return taxonLabelPainter;
    }

    public void setNodeLabelPainter(Painter<Node> nodeLabelPainter) {
        if (this.nodeLabelPainter != null) {
            this.nodeLabelPainter.removePainterListener(this);
        }
        this.nodeLabelPainter = nodeLabelPainter;
        if (this.nodeLabelPainter != null) {
            this.nodeLabelPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public Painter<Node> getNodeLabelPainter() {
        return nodeLabelPainter;
    }

    public void setBranchLabelPainter(Painter<Node> branchLabelPainter) {
        if (this.branchLabelPainter != null) {
            this.branchLabelPainter.removePainterListener(this);
        }
        this.branchLabelPainter = branchLabelPainter;
        if (this.branchLabelPainter != null) {
            this.branchLabelPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public Painter<Node> getBranchLabelPainter() {
        return branchLabelPainter;
    }

    public void setScaleBarPainter(Painter<TreePane> scaleBarPainter) {
        if (this.scaleBarPainter != null) {
            this.scaleBarPainter.removePainterListener(this);
        }
        this.scaleBarPainter = scaleBarPainter;
        if (this.scaleBarPainter != null) {
            this.scaleBarPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public Painter<TreePane> getScaleBarPainter() {
        return scaleBarPainter;
    }

    public void setBranchDecorator(BranchDecorator branchDecorator) {
        this.branchDecorator = branchDecorator;
        calibrated = false;
        repaint();
    }

    private boolean setBranchLineWeightValues(float weight) {
        if( ((BasicStroke)branchLineStroke).getLineWidth() != weight ) {
            branchLineStroke = new BasicStroke(weight);
            selectionStroke = new BasicStroke(Math.max(weight + 4.0F, weight * 1.5F), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            PREFS.putFloat(branchWeightPREFSkey, weight);
            return true;
        }
        return false;
    }

    public void setBranchLineWeight(float weight) {
        if( setBranchLineWeightValues(weight) ) {
          repaint();
        }
    }

    public void setPreferredSize(Dimension dimension) {
        if (treeLayout.maintainAspectRatio()) {
            super.setPreferredSize(new Dimension(dimension.width, dimension.height));
        } else {
            super.setPreferredSize(dimension);
        }

        calibrated = false;
    }

    public double getHeightAt(Graphics2D graphics2D, Point2D point) {
        try {
            point = transform.inverseTransform(point, null);
        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        return treeLayout.getHeightOfPoint(point);
    }


    final private int circDiameter = 9;

    //this method is for figuring out which node the user clicked on (if any)
    // result[0] is the selected node
    // result[1] is the parent if tree is unrooted and selection is of the clade *away* from the currect
    // direction, null otherwise
    Node[] getNodeAt(final Point point) {
        final Graphics2D g2 = (Graphics2D)getGraphics();
        Node[] result = new Node[2];

        Rectangle rect = new Rectangle(point.x - 1, point.y - 1, 3, 3);

        //todo: this allows clicking on node/branch labels to select nodes.  At this point this behaviour is considered undesirable
        /*for( TreeDrawableElement e : treeElements ) {
            Node node = e.getNode();
            if( node != null ) {
                if( e.hit(g2, rect) ) {
                   result[0] = node;
                   return result;
                }
            }
        }*/

        // this piece of code must run in reverse of how the nodes are drawn so that if the user clicks on 
        // overlapping nodes, the top node is selected
        Node rootNode = tree.getRootNode();
        if( ! hideNode(rootNode) && checkNodeIntersects(rootNode, point)) {
            result[0] = rootNode;
            return result;
        }
        List<Node> nodesInOrder = Utils.getNodes(tree, tree.getRootNode());
        for (int i = 0; i < nodesInOrder.size(); i++) {
            if( !isNodeVisible(nodesInOrder.get(i)) ) continue;
            if( hideNode(nodesInOrder.get(i)) ) continue;
            if( tree.isExternal(nodesInOrder.get(i)) ) continue;
            if( checkNodeIntersects(nodesInOrder.get(i), point)) {
                result[0] = nodesInOrder.get(i);
                return result;
            }
        }
        Node[] externalNodes = tree.getExternalNodes().toArray(new Node[0]);
        for(int i = externalNodes.length-1; i >= 0; i--){
            if( !isNodeVisible(externalNodes[i]) ) continue;
            if( hideNode(externalNodes[i]) ) continue;
            if( checkNodeIntersects(externalNodes[i], point)) {
                result[0] = externalNodes[i];
                return result;
            }
        }
        return result;
    }

    private boolean checkNodeIntersects(Node node, Point point){
        final Point2D.Double coord = nodeCoord(node);
        final double v = coord.distanceSq(point);
        return v < circDiameter * circDiameter;
    }

    /**
     * This is used for calculating which nodes are selected by dragging
     * (with a selection rectangle)
     * @param g2
     * @param rect
     * @return
     */
    Set<Node> getNodesAt(Graphics2D g2, Rectangle rect) {

        Set<Node> nodes = new HashSet<Node>();

        //todo: this allows clicking on node/branch labels to select nodes.  At this point this behaviour is considered undesirable
        /*for( TreeDrawableElement e : treeElements ) {
            Node node = e.getNode();
            if( node != null ) {
                if( e.hit(g2, rect) ) {
                    nodes.add(node);
                }
            }
        }*/

//        for (Node node : tree.getExternalNodes()) {
//            //  incorrect - some lables may have been reduced in size
//            // need to get rid of taxonLabelBounds, ormake sure it is correct
//            Shape taxonLabelBound = taxonLabelBounds.get(tree.getTaxon(node));
//            if (taxonLabelBound != null && g2.hit(rect, taxonLabelBound, false)) {
//                nodes.add(node);
//            }
//        }

        Node[] allNodes = tree.getNodes().toArray(new Node[0]);
        for(int i=allNodes.length-1; i >= 0; i--){
            if(rect.contains(transform.transform(treeLayout.getNodePoint(allNodes[i]),null))){
                nodes.add(allNodes[i]);
            }
        }

        return nodes;
    }

    public Set<Node> getSelectedNodes() {
        return selectedNodes;
    }

    public Set<Taxon> getSelectedTaxa() {
        return selectedTaxa;
    }

    public Rectangle2D getDragRectangle() {
        return dragRectangle;
    }

    public void setDragRectangle(Rectangle2D dragRectangle) {
        this.dragRectangle = dragRectangle;
        repaint();
    }

    public void setRuler(double rulerHeight) {
        this.rulerHeight = rulerHeight;
    }

    public void scrollPointToVisible(Point point) {
        scrollRectToVisible(new Rectangle(point.x, point.y, 0, 0));
    }

    public void setControlPalette(ControlPalette controlPalette) {
        this.controlPalette = controlPalette;
    }

    private ControlPalette controlPalette = null;

    private JCheckBox autoEx;

    public List<Controls> getControls(boolean detachPrimaryCheckbox) {

        List<Controls> controlsList = new ArrayList<Controls>();

        controlsList.addAll(treeLayout.getControls(detachPrimaryCheckbox));

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            transformCheck = new JCheckBox("Transform branches");
            optionsPanel.addComponent(transformCheck);

            transformBranches = PREFS.getBoolean(transformBanchesPREFSkey, transformBranches);

            transformCheck.setSelected(transformBranches);
            if (!originalTree.hasLengths()) {
                transformCheck.setEnabled(false);
            }

            final JComboBox combo1 = new JComboBox(TransformedRootedTree.Transform.values());
            combo1.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    PREFS.putInt(branchTransformTypePREFSkey, combo1.getSelectedIndex());
                    setBranchTransform(true, (TransformedRootedTree.Transform) combo1.getSelectedItem());
                }
            });
            combo1.setSelectedIndex(PREFS.getInt(branchTransformTypePREFSkey, 0));
            branchTransform = (TransformedRootedTree.Transform) combo1.getSelectedItem();           
            final JLabel label1 = optionsPanel.addComponentWithLabel("Transform:", combo1);
            label1.setEnabled(transformCheck.isSelected());
            combo1.setEnabled(transformCheck.isSelected());

            transformCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final boolean selected = transformCheck.isSelected();
                    // only on a real change
                    label1.setEnabled(selected);
                    combo1.setEnabled(selected);

                    setBranchTransform(selected, (TransformedRootedTree.Transform) combo1.getSelectedItem());
                }
            });

            final JCheckBox checkBox2 = new JCheckBox("Order branches");
            optionsPanel.addComponent(checkBox2);

            orderBranches = PREFS.getBoolean(orderBranchesPREFSkey, orderBranches);
            checkBox2.setSelected(orderBranches);

            final JComboBox combo2 = new JComboBox(SortedRootedTree.BranchOrdering.values());
            combo2.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    if(orderBranches){
                        setBranchOrdering(true, (SortedRootedTree.BranchOrdering) combo2.getSelectedItem());
                        PREFS.putInt(branchOrderingPREFSkey,combo2.getSelectedIndex());
                    }
                }
            });
            combo2.setSelectedIndex(PREFS.getInt(branchOrderingPREFSkey,0));

            final JLabel label2 = optionsPanel.addComponentWithLabel("Ordering:", combo2);
            label2.setEnabled(checkBox2.isSelected());
            combo2.setEnabled(checkBox2.isSelected());

            checkBox2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    label2.setEnabled(checkBox2.isSelected());
                    combo2.setEnabled(checkBox2.isSelected());

                    setBranchOrdering(checkBox2.isSelected(),
                            (SortedRootedTree.BranchOrdering) combo2.getSelectedItem());
                    PREFS.putBoolean(orderBranchesPREFSkey, orderBranches);
                }
            });

            if( ! tree.conceptuallyUnrooted() ) {
                final JCheckBox checkBox3 = new JCheckBox("Show Root Branch");
                optionsPanel.addComponent(checkBox3);

                showingRootBranch = PREFS.getBoolean(showRootPREFSkey, isShowingRootBranch());
                checkBox3.setSelected(showingRootBranch);
                checkBox3.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        setShowingRootBranch(checkBox3.isSelected());
                    }
                });
            } else {
                // no root for unrooted
                showingRootBranch = false;
            }

            final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

            final float weight = PREFS.getFloat(branchWeightPREFSkey, 1.0F);
            setBranchLineWeightValues(weight);
            spinner.setValue(weight);

            spinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setBranchLineWeight(((Double) spinner.getValue()).floatValue());
                }
            });
            optionsPanel.addComponentWithLabel("Line Weight:", spinner);

            autoEx = new JCheckBox("Auto subtree contract");
            autoEx.setToolTipText("Automatically contract subtrees when there is not enough space on-screen");
            optionsPanel.addComponent(autoEx);

            autoExpantion = PREFS.getBoolean(autoExPREFSkey, false);
            autoEx.setSelected(autoExpantion);
            autoEx.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    // we don't need to do this if we hover, right :)
                    final boolean b = autoEx.isSelected();
                    if( b != autoExpantion ) {
                        setAutoExpansion(b);
                    }
                }
            });

            final JCheckBox subTreeShowJB = new JCheckBox("Show selected subtree only");
            subTreeShowJB.setToolTipText("Only the selected part of the tree is shown");
            viewSubtree = PREFS.getBoolean(viewSubtreePREFSkey, false);
            subTreeShowJB.setSelected(viewSubtree);
            subTreeShowJB.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final boolean b = subTreeShowJB.isSelected();
                    PREFS.putBoolean(viewSubtreePREFSkey, subTreeShowJB.isSelected());
                    if( viewSubtree != b ) {
                        viewSubtree = b;
                        calibrated = false;
                        repaint();
                    }
                }
            });

            optionsPanel.addComponent(subTreeShowJB);

            controls = new Controls("Formatting", optionsPanel, true);
        }
        controlsList.add(controls);

        if (getTaxonLabelPainter() != null) {
            controlsList.addAll(getTaxonLabelPainter().getControls(detachPrimaryCheckbox));
        }

        if (getNodeLabelPainter() != null) {
            controlsList.addAll(getNodeLabelPainter().getControls(detachPrimaryCheckbox));
        }

        if (getBranchLabelPainter() != null) {
            controlsList.addAll(getBranchLabelPainter().getControls(detachPrimaryCheckbox));
        }

        if (getScaleBarPainter() != null) {
            controlsList.addAll(getScaleBarPainter().getControls(detachPrimaryCheckbox));
        }

        setupTree();
        return controlsList;
    }

    public void setSettings(ControlsSettings settings) {
        transformCheck.setSelected((Boolean) settings.getSetting("Transformed"));
    }

    public void getSettings(ControlsSettings settings) {
        settings.putSetting("Transformed", transformCheck.isSelected());
    }

    private JCheckBox transformCheck;

    private Controls controls = null;

    private final Set<TreeSelectionListener> treeSelectionListeners = new HashSet<TreeSelectionListener>();

    public void addTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        treeSelectionListeners.add(treeSelectionListener);
    }

    public void removeTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        treeSelectionListeners.remove(treeSelectionListener);
    }

    private void fireSelectionChanged() {
        for (TreeSelectionListener treeSelectionListener : treeSelectionListeners) {
            treeSelectionListener.selectionChanged();
        }
    }

    public void paint(Graphics graphics) {
        if (tree == null) return;

        final Graphics2D g2 = (Graphics2D) graphics;
        if (!calibrated) calibrate(g2, getWidth(), getHeight());

        final Paint oldPaint = g2.getPaint();
        final Stroke oldStroke = g2.getStroke();

        // todo disable since drawTree clears it anyway now
//        if( false ) {
//            for (Node selectedNode : selectedNodes) {
//                Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(selectedNode));
//                if (branchPath == null) continue;
//                g2.setPaint(selectionPaint);
//                g2.setStroke(selectionStroke);
//                g2.draw(branchPath);
//            }
//
//            for (Taxon selectedTaxon : selectedTaxa) {
//                g2.setPaint(selectionPaint);
//                Shape labelBounds = taxonLabelBounds.get(selectedTaxon);
//                if (labelBounds != null) {
//                    g2.fill(labelBounds);
//                }
//            }
//        }

        long start = System.currentTimeMillis();
        drawTree(g2, true, true, getWidth(), getHeight());
        System.err.println("tree draw " + (System.currentTimeMillis() - start) + "ms");
        
        if (dragRectangle != null) {
            g2.setPaint(new Color(128, 128, 128, 128));
            g2.fill(dragRectangle);

            g2.setStroke(new BasicStroke(2.0F));
            g2.setPaint(new Color(255, 255, 255, 128));
            g2.draw(dragRectangle);

            g2.setPaint(oldPaint);
            g2.setStroke(oldStroke);
        }
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        if (tree == null || pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        calibrated = false;
        setDoubleBuffered(false);

        drawTree(g2, false, false, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());

        setDoubleBuffered(true);
        calibrated = false;

        return PAGE_EXISTS;
    }

    private Point2D.Double nodeCoord(final Node node) {
        final Point2D nodePoint = treeLayout.getNodePoint(node);
        final Point2D.Double result = new Point2D.Double();
        transform.transform(nodePoint, result);
        return result;
    }

    private void nodeMarker(Graphics2D g2, Node node) {
        final Point2D.Double nodeLocation = nodeCoord(node);
        final boolean isSelected = selectedNodes.contains(node);
        final Paint color = g2.getPaint();

        if( isNodeCollapsed(node) ) {
           // final Color c = isSelected ? selectionPaint : branch;
          //  g2.setColor(c);
            if( isSelected ) g2.setPaint(selectionPaint);
            final Shape cn = treeLayout.getCollapsedNode(node, .25);
            final Shape transformedShape = transform.createTransformedShape(cn);

            final Stroke save = g2.getStroke();
            g2.setStroke(collapsedStroke);
            g2.draw(transformedShape);            
            g2.setStroke(save);

//            if( false) {
//            Line2D labelPath = treeLayout.getBranchLabelPath(node);
//            if( labelPath == null ) {
//                // root only?
//                labelPath = new Line2D.Double(0,0, 1,0);
//            }
//            final Point2D d1 = labelPath.getP2();
//            final Point2D d2 = labelPath.getP1();
//            final double dx = d1.getX() - d2.getX();
//            final double dy = d1.getY() - d2.getY();
//            final double branchLength = Math.sqrt(dx*dx + dy*dy);
//
//            final double sint = dy / branchLength;
//            final double cost = dx / branchLength;
//
//            final int r = circRadius;
//            final int h = 172*r/200;
//            int[] xp = {0, h, h};
//            int[] yp = {0, r/2, -r/2};
//
//            for(int k = 0; k < 3; ++k) {
//                final double rx = x + xp[k] * cost - yp[k] * sint;
//                final double ry = y + xp[k] * sint + yp[k] * cost;
//                xp[k] = (int)(rx + 0.5);
//                yp[k] = (int)(ry + 0.5);
//            }
//            g2.drawPolygon(xp, yp, 3);
//            }
        }

        final Paint c = isSelected ? selectionPaint : Color.LIGHT_GRAY;
        g2.setPaint(c);

        final int rlimit = treeLayout.getNodeMarkerRadiusUpperLimit(node, transform);

        final double x = nodeLocation.getX();
        final int ix1 = (int) Math.round(x);
        final double y = nodeLocation.getY();
        final int iy1 = (int) Math.round(y);

        int d = circDiameter;
        if( rlimit >= 0 ) {
            d = Math.min(2*rlimit+1, d);
        }

        final int r = (d-1)/2;

        g2.fillOval(ix1 - r, iy1 - r, d, d);
        g2.setColor(Color.black);
        g2.drawOval(ix1 - r, iy1 - r, d, d);
        g2.setPaint(color);
    }

    boolean viewSubtree;

    private boolean hideNode(Node node) {
        return viewSubtree && selectedNodes.size() > 0 && !selectedNodes.contains(node);
    }

    boolean preElementDrawCode = false;

    public void drawTree(Graphics2D g2, boolean drawNodes, boolean clipOfscreenShapes, double width, double height) {

        // this is a problem since paint draws some stuff before which print does not
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, (int)width, (int)height);

        final RenderingHints rhints = g2.getRenderingHints();
        final boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
        if( ! antialiasOn ) {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        if (!calibrated) calibrate(g2, width, height);

         // save graphics state which draw changes so that upon exit it can be restored

        final AffineTransform oldTransform = g2.getTransform();
        final Paint oldPaint = g2.getPaint();
        final Stroke oldStroke = g2.getStroke();
        final Font oldFont = g2.getFont();

        final Set<Node> externalNodes = tree.getExternalNodes();
        final boolean showingTaxonLables = taxonLabelPainter != null && taxonLabelPainter.isVisible();

        final boolean alignedTaxa = treeLayout.alignTaxa();

        for (Node node : externalNodes) {
            if( !isNodeVisible(node) ) continue;
            if( hideNode(node) ) continue;
            
            final Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));

            if (showingTaxonCallouts && showingTaxonLables) {
                final Shape calloutPath = transform.createTransformedShape(treeLayout.getCalloutPath(node));
                if (calloutPath != null) {
                    g2.setStroke(taxonCalloutStroke);
                    g2.draw(calloutPath);
                }
            }

            final Paint paint = (branchDecorator != null) ? branchDecorator.getBranchPaint(tree, node) : Color.BLACK;
            g2.setPaint(paint);

            g2.setStroke(branchLineStroke);
            g2.draw(branchPath);

            if(drawNodes)
                nodeMarker(g2, node);

            if( preElementDrawCode ) {
                if (showingTaxonLables) {
                    final Taxon taxon = tree.getTaxon(node);
                    if( ! alignedTaxa ) {
                        taxonLabelPainter.calibrate(g2);
                        taxonLabelWidth = taxonLabelPainter.getWidth(g2, node);
                    }
                    AffineTransform taxonTransform = taxonLabelTransforms.get(taxon);
                    Painter.Justification taxonLabelJustification = taxonLabelJustifications.get(taxon);
                    g2.transform(taxonTransform);

                    final Rectangle2D.Double bounds = new Rectangle2D.Double(0.0, 0.0, taxonLabelWidth, taxonLabelPainter.getPreferredHeight());
                    taxonLabelPainter.paint(g2, node, taxonLabelJustification, bounds);

                    g2.setTransform(oldTransform);
                }
            }
        }

        final Node rootNode = tree.getRootNode();
        final boolean nodesLables = nodeLabelPainter != null && nodeLabelPainter.isVisible();
        final boolean branchLables = branchLabelPainter != null && branchLabelPainter.isVisible();

        for(int nn = nodesInOrder.size()-1; nn >= 0; --nn) {
            final Node node = nodesInOrder.get(nn);

            if (showingRootBranch || node != rootNode) {
                if( !isNodeVisible(node) ) continue;
                if( hideNode(node) ) continue;

                if( !tree.isExternal(node) ) {
                    final Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
                    g2.setStroke(branchLineStroke);

                    final Paint paint =
                            branchDecorator != null ? branchDecorator.getBranchPaint(tree, node) : Color.BLACK;

                    g2.setPaint(paint);

                    // todo: although this fix is only an if != null check, this is ok because the missing node is a root node,
                    // todo: which should not be drawn on an unrooted view anyway
                    if(branchPath == null)
                        continue;

                    g2.draw(branchPath);

                    if(drawNodes)
                        nodeMarker(g2, node);

                    if (preElementDrawCode && nodesLables) {
                        final AffineTransform nodeTransform = nodeLabelTransforms.get(node);
                        if (nodeTransform != null) {
                            final Painter.Justification nodeLabelJustification = nodeLabelJustifications.get(node);
                            g2.transform(nodeTransform);

                            final Rectangle2D.Double bounds = new Rectangle2D.Double(0.0, 0.0,
                                    nodeLabelPainter.getWidth(g2, node), nodeLabelPainter.getPreferredHeight());
                            nodeLabelPainter.paint(g2, node, nodeLabelJustification, bounds);

                            g2.setTransform(oldTransform);
                        }
                    }
                }


                if ( branchLables && preElementDrawCode ) {
                    final AffineTransform branchTransform = branchLabelTransforms.get(node);
                    if (branchTransform != null) {
                        g2.transform(branchTransform);

                        branchLabelPainter.calibrate(g2);
                        final double preferredWidth = branchLabelPainter.getWidth(g2, node);
                        final double preferredHeight = branchLabelPainter.getPreferredHeight();

                        branchLabelPainter.paint(g2, node, Painter.Justification.CENTER,
                                new Rectangle2D.Double(0, 0, preferredWidth, preferredHeight));

                        g2.setTransform(oldTransform);
                    }
                }
            }
        }

        if( ! preElementDrawCode ) {
            for( TreeDrawableElement e : treeElements ) {
                if(e.isVisible())
                    e.draw(g2, clipOfscreenShapes ? viewport : null);
            }
        }

        if( ! hideNode(rootNode) && drawNodes ) {
          g2.setStroke(branchLineStroke);
          nodeMarker(g2, rootNode);
        }

        if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
            scaleBarPainter.paint(g2, this, Painter.Justification.CENTER, scaleBarBounds);
        }

        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
        g2.setFont(oldFont);

        if( ! antialiasOn ) {
           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    private void calibrate(Graphics2D g2, double width, double height) {
        long start = System.currentTimeMillis();

        // First of all get the bounds for the unscaled tree
        Rectangle2D treeBounds = null;

        final Node rootNode = tree.getRootNode();

        // todo efficency create a list once of none hidden nodes etc

        // bounds on branches
        for (Node node : tree.getNodes()) {
            if( hideNode(node) )  continue;
            // no root branch for unrooted trees
            if( !(tree.conceptuallyUnrooted() && (node == rootNode)) ) {
                final Shape branchPath = treeLayout.getBranchPath(node);
                // Add the bounds of the branch path to the overall bounds
                final Rectangle2D branchBounds = branchPath.getBounds2D();
                if (treeBounds == null) {
                    treeBounds = branchBounds;
                } else {
                    treeBounds.add(branchBounds);
                }
            }
        }

        assert treeBounds != null; //the code below is a hack to make this not crash for users
        if(treeBounds == null){
            if(calibrated)
                return;
            treeBounds = new Rectangle2D.Double(0,0,100,100); //this is here so the treeViewer draws somethihng...
        }

        boolean oldScaleCode = false;

        // oldScaleCode too
        final Rectangle2D bounds = treeBounds.getBounds2D(); // (JH) same as (Rectangle2D) treeBounds.clone();

        double scaleHeight = 0;
        if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
            scaleBarPainter.calibrate(g2);
            scaleHeight = scaleBarPainter.getPreferredHeight();
        }

        // area available for drawing
        final double availableW = width - (insets.left + insets.right);
        final double availableH = height - (insets.top + insets.bottom + scaleHeight);

        final Set<Node> externalNodes = tree.getExternalNodes();
        Node nodeWithLongestTaxon = null;

        TreeBoundsHelper tbh =
                new TreeBoundsHelper(externalNodes.size() + 2*tree.getNodes().size(), availableW, availableH,
                        treeBounds);

        if (taxonLabelPainter != null && taxonLabelPainter.isVisible()) {

            taxonLabelWidth = 0.0;
            taxonLabelPainter.calibrate(g2);

            if( treeLayout.alignTaxa() ) {
                // Find the longest taxon label
                for (Node node : externalNodes) {
                    final double preferredWidth = taxonLabelPainter.getWidth(g2, node);
                    if( preferredWidth > taxonLabelWidth ) {
                        taxonLabelWidth = preferredWidth;
                        nodeWithLongestTaxon = node;
                    }
                }
            }

            final double labelHeight = taxonLabelPainter.getPreferredHeight();

            for (Node node : externalNodes) {
                if( hideNode(node) ) continue;

                 if( nodeWithLongestTaxon == null ) {
                    taxonLabelPainter.calibrate(g2);
                    taxonLabelWidth = taxonLabelPainter.getWidth(g2, node);
                }
                // Get the line that represents the orientation for the taxon label
                final Line2D taxonPath = treeLayout.getTaxonLabelPath(node);

                //System.out.println("For " + tree.getTaxon(node).getName())
                tbh.addBounds(taxonPath, labelHeight, labelXOffset + taxonLabelWidth, false);

                if( oldScaleCode ) {
                    final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, taxonLabelWidth, labelHeight);

                    // Work out how it is rotated and create a transform that matches that
                    AffineTransform taxonTransform = calculateTransform(null, taxonPath, taxonLabelWidth, labelHeight, true);

                    // and add the translated bounds to the overall bounds
                    bounds.add(taxonTransform.createTransformedShape(labelBounds).getBounds2D());
                }
            }
        }


        if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {

            for( Node node : tree.getNodes() ) {
                if( hideNode(node) ) continue;

                // Get the line that represents the label orientation
                final Line2D labelPath = treeLayout.getNodeLabelPath(node);

                if (labelPath != null) {
                    nodeLabelPainter.calibrate(g2);
                    final double labelHeight = nodeLabelPainter.getPreferredHeight();
                    final double labelWidth = nodeLabelPainter.getWidth(g2, node);

                    tbh.addBounds(labelPath, labelHeight, labelXOffset + labelWidth, false);

                    if( oldScaleCode ) {
                        Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                        // Work out how it is rotated and create a transform that matches that
                        AffineTransform labelTransform = calculateTransform(null, labelPath, labelWidth, labelHeight, true);

                        // and add the translated bounds to the overall bounds
                        bounds.add(labelTransform.createTransformedShape(labelBounds).getBounds2D());
                    }
                }
            }
        }

        if (branchLabelPainter != null && branchLabelPainter.isVisible()) {
            // Iterate though the nodes
            for (Node node : tree.getNodes()) {
                if( hideNode(node) ) continue;

                // Get the line that represents the path for the branch label
                final Line2D labelPath = treeLayout.getBranchLabelPath(node);

                if (labelPath != null) {
                    branchLabelPainter.calibrate(g2);
                    final double labelHeight = branchLabelPainter.getHeightBound();
                    final double labelWidth = branchLabelPainter.getWidth(g2, node);

                    tbh.addBounds(labelPath, labelHeight, labelXOffset + labelWidth, true);

                    if( oldScaleCode ) {
                        Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                        // Work out how it is rotated and create a transform that matches that
                        AffineTransform labelTransform = calculateTransform(null, labelPath, labelWidth, labelHeight, false);

                        // and add the translated bounds to the overall bounds
                        bounds.add(labelTransform.createTransformedShape(labelBounds).getBounds2D());
                    }
                }
            }
        }

        if( oldScaleCode ) {
            if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
                scaleBarPainter.calibrate(g2);
                scaleBarBounds = new Rectangle2D.Double(treeBounds.getX(), treeBounds.getY(),
                        treeBounds.getWidth(), scaleBarPainter.getPreferredHeight());
                bounds.add(scaleBarBounds);
            }
        }

        if( oldScaleCode ) {
          assert false;
            //availableH = height - insets.top - insets.bottom;
        }

        final double[] doubles = tbh.getOrigionAndScale(false);
        double yorigion = doubles[0];
        double yScale = doubles[1];

        final double[] xdoubles = tbh.getOrigionAndScale(true);
        double xorigion = xdoubles[0];
        double xScale = xdoubles[1];

        // oldscalecode too  ************************** vvvvvvvvvvvvvvvvvvvvvvvvvvv

        // get the difference between the tree's bounds and the overall bounds, i.e. the amount (in pixels) required
        // to hold non-scaling stuff located outside the tree

        double xDiff = bounds.getWidth() - treeBounds.getWidth();
        double yDiff = bounds.getHeight() - treeBounds.getHeight();
        assert xDiff >= 0 && yDiff >= 0;

        // small tree, long labels, label bounds may get larger that window, protect against that

        if( xDiff >= availableW ) {
           xDiff = Math.min(availableW, bounds.getWidth()) - treeBounds.getWidth();
        }

        if( yDiff >= availableH ) {
           yDiff = Math.min(availableH, bounds.getHeight()) - treeBounds.getHeight();
        }

        // Get the amount of canvas that is going to be taken up by the tree -
        // The rest is taken up by taxon labels which don't scale
        final double w = availableW - xDiff;
        final double h = availableH - yDiff;
        // oldscalecode too  ************************** ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

        double xOffset = 0.0;
        double yOffset = 0.0;

        if (treeLayout.maintainAspectRatio()) {
            // If the tree is layed out in both dimensions then we
            // need to find out which axis has the least space and scale
            // the tree to that (to keep the aspect ratio.
            if( oldScaleCode ) {
                final boolean widthLimit = (w / treeBounds.getWidth()) < (h / treeBounds.getHeight());
                final double scale = widthLimit ? w / treeBounds.getWidth() : h / treeBounds.getHeight();
                treeScale = xScale = yScale = scale;

                // and set the origin so that the center of the tree is in
                // the center of the canvas
                xOffset = ((width - (treeBounds.getWidth() * xScale)) / 2) - (treeBounds.getX() * xScale);
                yOffset = ((height - (treeBounds.getHeight() * yScale)) / 2) - (treeBounds.getY() * yScale);
            } else {

                if( tbh.getRange(true, xorigion, yScale) <= availableW ) {
                //if( xorigion + yScale * treeBounds.getWidth() <= availableW ) {
                    xorigion = tbh.getOrigion(true, yScale);
                    treeScale = yScale;
                } else {
                    double size = 0;
                    int count = 0;
                    String oldValues = "";
                    //count is here to make sure we don't get an infinite loop if there is no scale that will
                    //allow the tree to be contained in the current view
                    while((size = tbh.getRange(false, yorigion, xScale)) > availableH && count < 10){
                        xScale *= availableH/size;
                        yorigion = yOffset;
                        count++;
                    }
                    //todo: this was removed assert tbh.getRange(false, yorigion, xScale) <= availableH : tbh.getRange(false, yorigion, xScale)+" : "+availableH+" : "+oldValues;
                    //assert yorigion + xScale * treeBounds.getHeight() <= availableH;
                    yorigion = tbh.getOrigion(false, xScale);
                    treeScale  = xScale;
                }

                //System.out.println("xs/ys " + xScale + "/" + yScale +  " (" + treeScale + ")" + " xo/yo " + xorigion + "/" + yorigion);
                xScale = yScale = treeScale;

                xOffset = xorigion - treeBounds.getX() * treeScale;
                yOffset = yorigion - treeBounds.getY() * treeScale;
                double xRange = tbh.getRange(true, xorigion, treeScale);
                final double dx = (availableW - xRange)/2;
                xOffset += dx;
                double yRange = tbh.getRange(false, yorigion, treeScale);
                final double dy = (availableH - yRange)/2;
                yOffset += dy; //  > 0 ? dy : 0;
                //System.out.println("xof/yof " + xOffset + "/" + yOffset);
            }

        } else {
            // Otherwise just scale both dimensions

//            System.out.println("old/new xs " + (w / treeBounds.getWidth()) + "/" + xScale
//                    + " ys " + (h / treeBounds.getHeight()) + "/" + yScale
//                    + " y0 " + -bounds.getY() + "/" + yOffset + " x0 " + -bounds.getX() + "/" + xorigion);
            if( oldScaleCode ) {
                xScale = w / treeBounds.getWidth();
                yScale = h / treeBounds.getHeight();

                // and set the origin in the top left corner
                xOffset = -bounds.getX();
                yOffset = -bounds.getY();
            } else {
                xOffset = xorigion - treeBounds.getX() * xScale;
                yOffset = yorigion - treeBounds.getY() * yScale;         
            }

            treeScale = xScale;
        }

        assert treeScale > 0;

        // Create the overall transform
        transform = new AffineTransform();
        transform.translate(xOffset + insets.left, yOffset + insets.top);
        transform.scale(xScale, yScale);
        
        final double xl = transform.getTranslateX() + transform.getScaleX() * treeBounds.getX();
        final double xh = transform.getTranslateX() + transform.getScaleX() * treeBounds.getMaxX();

        // Get the bounds for the actual scaled tree (not anymore)
        //treeBounds = null;
        {
            Set<Node> small = new HashSet<Node>();
            for (Node node : tree.getNodes()) {
                if( hideNode(node) ) continue;

//                if (showingRootBranch || node != rootNode) {
//                    final Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
//                    final Rectangle2D bounds2D = branchPath.getBounds2D();
//                    if (treeBounds == null) {
//                        treeBounds = bounds2D;
//                    } else {
//                        treeBounds.add(bounds2D);
//                    }
//                }

                node.removeAttribute(clpsdName + "-auto");
                if( ! small.contains(node) && treeLayout.smallSubTree(node, transform) ) {
                    node.setAttribute(clpsdName + "-auto", Boolean.TRUE);
                    small.addAll(Utils.getNodes(tree, node));
                }
            }
        }
        
        // Clear previous values of taxon label bounds and transforms
        taxonLabelBounds.clear();
        taxonLabelTransforms.clear();
        taxonLabelJustifications.clear();
        treeElements.clear();
        
        if (taxonLabelPainter != null && taxonLabelPainter.isVisible()) {
            final double labelHeight = taxonLabelPainter.getPreferredHeight();
            Rectangle2D labelBounds = (nodeWithLongestTaxon == null) ? null :
                    new Rectangle2D.Double(0.0, 0.0, taxonLabelWidth, labelHeight);

            // Iterate though the external nodes
            for (Node node : externalNodes) {
                if( hideNode(node) || !isNodeVisible(node) ) continue;

                final Taxon taxon = tree.getTaxon(node);
                if( nodeWithLongestTaxon == null ) {
                    taxonLabelPainter.calibrate(g2);
                    taxonLabelWidth = taxonLabelPainter.getWidth(g2, node);
                    labelBounds = new Rectangle2D.Double(0.0, 0.0, taxonLabelWidth, labelHeight);
                }
                // Get the line that represents the path for the taxon label
                final Line2D taxonPath = treeLayout.getTaxonLabelPath(node);

                // Work out how it is rotated and create a transform that matches that
                AffineTransform taxonTransform = calculateTransform(transform, taxonPath, taxonLabelWidth, labelHeight, true);

                // Store the alignment in the map for use when drawing
                final Painter.Justification just = (taxonPath.getX1() < taxonPath.getX2()) ?
                        Painter.Justification.LEFT : Painter.Justification.RIGHT;

                if( preElementDrawCode ) {
                    // Store the transformed bounds in the map for use when selecting
                    taxonLabelBounds.put(taxon, taxonTransform.createTransformedShape(labelBounds));

                    // Store the transform in the map for use when drawing
                    taxonLabelTransforms.put(taxon, taxonTransform);

                    taxonLabelJustifications.put(taxon, just);
                }

                final TreeDrawableElementNodeLabel e =
                        new TreeDrawableElementNodeLabel(tree, node, just, labelBounds, taxonTransform, 10,
                                                         nodeWithLongestTaxon, (BasicLabelPainter) taxonLabelPainter,
                        null);

                treeElements.add(e);
            }
        }

        // Clear the map of individual node label bounds and transforms
        nodeLabelBounds.clear();
        nodeLabelTransforms.clear();
        nodeLabelJustifications.clear();

        if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
            final double labelHeight = nodeLabelPainter.getPreferredHeight();

            // Iterate though all nodes
            for (Node node : tree.getNodes()) {
                if( hideNode(node) || !isNodeVisible(node) ) continue;

                // Get the line that represents the orientation of node label
                final Line2D labelPath = treeLayout.getNodeLabelPath(node);

                if (labelPath != null) {
                     final double labelWidth = nodeLabelPainter.getWidth(g2, node);
                     final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                    // Work out how it is rotated and create a transform that matches that
                    AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, true);

                    // Store the alignment in the map for use when drawing

                    Painter.Justification justification =
                            (labelPath.getX1() < labelPath.getX2()) ? Painter.Justification.LEFT : Painter.Justification.RIGHT;

                    if( preElementDrawCode ) {
                        // Store the transformed bounds in the map for use when selecting
                        nodeLabelBounds.put(node, labelTransform.createTransformedShape(labelBounds));
                        // Store the transform in the map for use when drawing
                        nodeLabelTransforms.put(node, labelTransform);
                        nodeLabelJustifications.put(node, justification);
                    }

                    final TreeDrawableElementNodeLabel e =
                        new TreeDrawableElementNodeLabel(tree, node, justification, labelBounds, labelTransform, 9,
                                                          null, ((BasicLabelPainter) nodeLabelPainter), "node");

                    treeElements.add(e);
                }
            }
        }

        if (branchLabelPainter != null && branchLabelPainter.isVisible()) {

           // float sss = ((BasicLabelPainter)branchLabelPainter).getFontSize();
       //     for(int k = 0; k < 2; ++k) {
//                if(k == 0)  ((BasicLabelPainter)branchLabelPainter).setFontSize(((BasicLabelPainter)branchLabelPainter).getFontMinSize(), false) ;
//                if(k == 1)  ((BasicLabelPainter)branchLabelPainter).setFontSize(sss, false) ;

            branchLabelPainter.calibrate(g2);
            final double labelHeight = branchLabelPainter.getPreferredHeight();

            //System.out.println("transform " + transform);

            for( Node node : tree.getNodes() ) {
                if( hideNode(node) || !isNodeVisible(node) ) continue;

                // Get the line that represents the path for the branch label
                final Line2D labelPath = treeLayout.getBranchLabelPath(node);

                if (labelPath != null) {
                    final double labelWidth = branchLabelPainter.getWidth(g2, node);
                    final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                    final double branchLength = labelPath.getP2().distance(labelPath.getP1());

                    final Painter.Justification just = labelPath.getX1() < labelPath.getX2() ? Painter.Justification.LEFT :
                            Painter.Justification.RIGHT;

                    // Work out how it is rotated and create a transform that matches that
                    AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, false);

//                    System.out.print( Utils.DEBUGsubTreeRep(Utils.rootTheTree(tree), node)
//                            + " " + labelWidth + "x" + labelHeight + " " +
//                            ((BasicLabelPainter)branchLabelPainter).getFontSize() + " " + labelTransform);

                    // move to middle of branch - since the move is before the rotation
                    // and center label by moving an extra half width of label
                    final double direction = just == Painter.Justification.RIGHT ? 1 : -1;
                    labelTransform.translate(-labelWidth/2 + -direction * xScale * branchLength / 2, -5 - labelHeight/2);

                    if( preElementDrawCode ) {
                        // Store the transformed bounds in the map for use when selecting  (not anymore JH)
                        final Shape value = labelTransform.createTransformedShape(labelBounds);
                        //
                        branchLabelBounds.put(node, value);
                        // Store the transform in the map for use when drawing
                        branchLabelTransforms.put(node, labelTransform);
                        // unused at the moment
                        // Store the alignment in the map for use when drawing
                        //branchLabelJustifications.put(node, just);
                    }
                  //  System.out.println(" -> " + labelTransform);
                  //  if( k == 0 ) continue;
                    
                    final TreeDrawableElementNodeLabel e =
                        new TreeDrawableElementNodeLabel(tree, node, Painter.Justification.CENTER, labelBounds, labelTransform, 8,
                                                          null, ((BasicLabelPainter) branchLabelPainter), "branch");

                    treeElements.add(e);
                //}
                }
            }
        }

        if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
            scaleBarPainter.calibrate(g2);
            final double h1 = scaleBarPainter.getPreferredHeight();
            final double x = xl; //  treeBounds.getX()
            final double wid = xh - xl; // treeBounds.getWidth();
            scaleBarBounds = new Rectangle2D.Double(x, height - h1, wid, h1);
        }

        // unused at the moment
        //calloutPaths.clear();

        if( autoExpantion ) {
            setTreeAttributesForAutoExpansion();
            // some nodes may switched to non visible
            for(int k = 0; k < treeElements.size(); ++k) {
                final TreeDrawableElement e = treeElements.get(k);
                assert ! hideNode(e.getNode());
                if( !isNodeVisible(e.getNode() ) ) {
                    treeElements.remove(k);
                    --k;
                }
            }
        }

        long now = System.currentTimeMillis();
        TreeDrawableElement.setOverlappingVisiblitiy(treeElements, g2);
        System.err.println("Clash " + (System.currentTimeMillis() - now));

        calibrated = true;

        System.err.println("Calibrate " + (System.currentTimeMillis() - start));
    }


    private AffineTransform calculateTransform(AffineTransform globalTransform, Line2D line,
                                               double width, double height, boolean just) {
        final Point2D origin = line.getP1();
        if (globalTransform != null) {
            globalTransform.transform(origin, origin);
        }

        // Work out how it is rotated and create a transform that matches that
        AffineTransform lineTransform = new AffineTransform();

        final double dy = line.getY2() - line.getY1();
        // efficency
        if( dy != 0.0 ) {
            final double dx = line.getX2() - line.getX1();
            final double angle = dx != 0.0 ? Math.atan(dy / dx) : 0.0;
            lineTransform.rotate(angle, origin.getX(), origin.getY());
        }

        // Now add a translate to the transform - if it is on the left then we need
        // to shift it by the entire width of the string.
        final double ty = origin.getY() - (height / 2.0);
        double tx = origin.getX();
        if( just) {
            if (!just || line.getX2() > line.getX1()) {
                tx += labelXOffset;
            } else {
                tx -= (labelXOffset + width);
            }
        }
        lineTransform.translate(tx, ty);
        return lineTransform;
    }

    public void setViewPort(JViewport viewport) {
        this.viewport = viewport;
    }

    TreeLayout getTreeLayout() {
        return treeLayout;
    }

    private class TreeBoundsHelper {
        private double[] xbounds;
        private double[] ybounds;
        int nv;
        double availableW;
        double availableH;
        Rectangle2D treeBounds;

        public TreeBoundsHelper(int nValues, double availableW, double availableH, Rectangle2D treeBounds) {
            // each value imposes a constraints (3 numbers) + 2 for raw height and width
            nValues = 3 * (nValues + 2);
            xbounds = new double[nValues];
            ybounds = new double[nValues];
            nv = 0;
            this.availableH = availableH;
            this.availableW = availableW;
            this.treeBounds = treeBounds;

            xbounds[nv] = treeBounds.getWidth();
            xbounds[nv+1] = availableW;
            xbounds[nv+2] = 0;
            ybounds[nv] = treeBounds.getHeight();
            ybounds[nv+1] = availableH;
            ybounds[nv+2] = 0;
            nv += 3;
            xbounds[nv] = 0;
            xbounds[nv+1] = availableW;
            xbounds[nv+2] = 0;
            ybounds[nv] = 0;
            ybounds[nv+1] = availableH;
            ybounds[nv+2] = 0;
            nv += 3;
        }

        private int quadrantOf(Line2D line, double[] sincos) {
            Point2D start = line.getP1();
            Point2D end = line.getP2();
            double dy = end.getY() - start.getY();
            double dx = end.getX() - start.getX();
            double r = Math.sqrt(dx * dx + dy * dy);
            sincos[0] = dy / r;
            sincos[1] = dx / r;
            return (dy>=0 ? 0 : 2) + ((dy>=0) == (dx>=0) ? 0 : 1);
        }

        //  v, height - y-extra(max), -y-extra(min)
        void addBounds(Line2D taxonPath, double labelHeight, double labelWidth, boolean centered)  {
            double[] sincos = {0.0, 1.0};

            int quad = quadrantOf(taxonPath, sincos); // sine and cosine of the inclination of the taxonPath vector
            final double yHigh = labelHeight / 2;
            final double xHigh = centered ? labelWidth / 2 : labelWidth;
            final double xLow =  centered ? -xHigh : 0;
            // origin here before rotate is midpoint on left edge for non centered, center for centered
            // order is counter-clockwise from upper right corner, which makes the corner number match the quadrant number
            // for max X. other limits are relative to that.

            double[] pts = {xHigh, yHigh, xLow, yHigh, xLow, -yHigh, xHigh, -yHigh}; // four corners of bounding box
            int ixmax = 2*quad;
            int ixmin = 2*((quad+2) & 0x3);
            int iymax = 2*((quad+3) & 0x3);
            int iymin = 2*((quad+1) & 0x3);

            final double thesin = -sincos[0];
            final double thecos = sincos[1];

            double dx = thecos * pts[ixmax] -  thesin * pts[ixmax + 1];
            if( Double.isNaN(dx) ) {
              assert dx >= 0 : dx + " " + thecos + " " + thesin;
            }

            final Point2D start = taxonPath.getP1();
            final Point2D end = taxonPath.getP2();
            final double xInTreeAbs = centered ? (start.getX() + end.getX()) / 2 : start.getX();
            // yikes - some code determining the paths uses floats, so when used with doubles small discrapencies can make
            // valus small negatives
            double x = (float)xInTreeAbs - (float)treeBounds.getMinX();            assert x >= 0 : x;
            xbounds[nv] = x;
            xbounds[nv+1] = availableW - dx;
            dx = -(thecos * pts[ixmin] - thesin * pts[ixmin + 1]);                 assert dx >= 0 : dx;
            xbounds[nv+2] = dx;

            double dy = -(thesin * pts[iymax] + thecos * pts[iymax+1]);            assert dy >= 0;
            // y0 + scale(y) * y + y-extra <= height
            // y0 + scale(y) * y + y-extra >= 0

            final double yTreeAbs = centered ? (start.getY() + end.getY()) / 2 : start.getY();
            double y = (float) yTreeAbs - (float)treeBounds.getMinY();             assert y >= 0 : y;
            ybounds[nv] = y;
            ybounds[nv+1] = availableH - dy;

            dy = thesin * pts[iymin] + thecos * pts[iymin+1];                      assert dy >= 0;
            ybounds[nv+2] = dy;

            nv += 3;
        }

        double[] getOrigionAndScale(boolean isx) {
            double[] values = isx ? xbounds : ybounds;

            double scale = Double.MAX_VALUE;
            double origin = 0.0;
            double minOrigin = Double.MAX_VALUE;
            for(int k = 0; k < nv; k += 3) {
                if( values[k] == 0.0 ) {
                    origin = Math.max(origin, values[k+2]);
                }
                minOrigin = Math.min(minOrigin, values[k+1]);
            }

            if( origin > minOrigin ) {
                origin = minOrigin/2;
            }

            int nit = 0;
            while( nit < 100 ) {
                ++nit; // safty net, should converge long before that
                double scaleMin = -Double.MAX_VALUE;
                for(int k = 0; k < nv; k += 3) {
                    if( values[k] != 0.0 ) {
                        double lim = Math.abs((values[k+1] - origin) / values[k]);
                        scale = Math.min(scale, lim);
                    }

                    double d = (values[k + 2] - origin);
                    // do limit only if y0 is no suffcient in this case
                    if( d > 0 ) {
                        double lim = d / values[k];
                        scaleMin = Math.max(scaleMin, lim);
                    }
                }
           
                boolean b = scaleMin <= scale || nit > 10 &&  scaleMin - scale < 1e-5;
                if( origin < minOrigin && b) {
                    break;
                }
                origin = -Double.MAX_VALUE;
                for(int k = 0; k < nv; k += 3) {
                    double l = values[k + 2] - scale * values[k];
                    origin = Math.max(origin, l);
                }
            }
            assert scale > 0 : scale + " " + nit;
            assert origin >= 0.0 : origin + " " + nit;
            double[] r = {origin, scale};
            return r;
        }

        double getOrigion(boolean isx, double scale) {
            double origin = -Double.MAX_VALUE;
            double[] values = isx ? xbounds : ybounds;
            for(int k = 0; k < nv; k += 3) {
                double l = values[k + 2] - scale * values[k];
                origin = Math.max(origin, l);
            }
            return origin;
        }

        double getRange(boolean isx, double origin, double scale) {
            double[] values = isx ? xbounds : ybounds;
            double target = isx ? availableW : availableH;

            double mx = -Double.MAX_VALUE, mn = Double.MAX_VALUE;

            for(int k = 0; k < nv; k += 3) {
                final double v = origin + values[k] * scale;
                mx = Math.max(mx, v + (target - values[k+1]));
                mn = Math.min(mn, v - values[k+2]);
            }
            return mx - mn;
        }
    }


    // Overridden methods to recalibrate tree when bounds change
    public void setBounds(int x, int y, int width, int height) {
        // when moving the viewport x/y change
        final Rectangle rectangle = getBounds();
        calibrated = calibrated && width == rectangle.width && height == rectangle.height;
        super.setBounds(x, y, width, height);
    }

    public void setBounds(Rectangle rectangle) {
        calibrated = false;
        super.setBounds(rectangle);
    }

    public void setSize(Dimension dimension) {
        calibrated = false;
        super.setSize(dimension);
    }

    public void setSize(int width, int height) {
        calibrated = false;
        super.setSize(width, height);
    }

    private JViewport viewport = null;

    // Tree passed in
    private RootedTree originalTree = null;
    //  Tree possibly transformed by the viewer
    private RootedTree tree = null;
    private List<Node> nodesInOrder;

    private TreeLayout treeLayout = null;

    private boolean orderBranches = false;
    private SortedRootedTree.BranchOrdering branchOrdering = SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY;

    private boolean transformBranches = false;
    private TransformedRootedTree.Transform branchTransform = TransformedRootedTree.Transform.CLADOGRAM;


    private double treeScale;

    //private Insets margins = new Insets(6, 6, 6, 6);
    private Insets insets = new Insets(6, 6, 6, 6);

    private Set<Node> selectedNodes = new HashSet<Node>();
    private Set<Taxon> selectedTaxa = new HashSet<Taxon>();

    private double rulerHeight = -1.0;
    private Rectangle2D dragRectangle = null;

    private BranchDecorator branchDecorator = null;

    private float labelXOffset = 5.0F;
    private Painter<Node> taxonLabelPainter = null;
    private double taxonLabelWidth;
    private Painter<Node> nodeLabelPainter = null;
    private Painter<Node> branchLabelPainter = null;

    private Painter<TreePane> scaleBarPainter = null;
    private Rectangle2D scaleBarBounds = null;

    private Stroke branchLineStroke = new BasicStroke(1.0F);
    private Stroke collapsedStroke = new BasicStroke(1.5F);
    private Stroke taxonCalloutStroke = new BasicStroke(0.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{0.5f, 2.0f}, 0.0f);
    private Stroke selectionStroke = new BasicStroke(6.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private Paint selectionPaint = Color.BLUE; // new Color(180, 213, 254);
    //private Color selectionPaint = new Color(180, 213, 254);
    private boolean calibrated = false;

    // Transform which scales the tree from it's own units to pixles and moves it to center of window
    private AffineTransform transform = null;

    private boolean showingRootBranch = true;
    private boolean autoExpantion = false;
    private boolean showingTaxonCallouts = true;

    private Map<Taxon, AffineTransform> taxonLabelTransforms = new HashMap<Taxon, AffineTransform>();
    private Map<Taxon, Shape> taxonLabelBounds = new HashMap<Taxon, Shape>();
    private Map<Taxon, Painter.Justification> taxonLabelJustifications = new HashMap<Taxon, Painter.Justification>();

    private Map<Node, AffineTransform> nodeLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> nodeLabelBounds = new HashMap<Node, Shape>();
    private Map<Node, Painter.Justification> nodeLabelJustifications = new HashMap<Node, Painter.Justification>();

    private Map<Node, AffineTransform> branchLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> branchLabelBounds = new HashMap<Node, Shape>();

    private List<TreeDrawableElement> treeElements = new ArrayList<TreeDrawableElement>();

    // unused at the moment
    //private Map<Node, Painter.Justification> branchLabelJustifications = new HashMap<Node, Painter.Justification>();

    // unused at the moment
    // private Map<Taxon, Shape> calloutPaths = new HashMap<Taxon, Shape>();

    private String transformBanchesPREFSkey = "transformBranches";
    private String branchTransformTypePREFSkey = "branchTransformType";
    private String branchOrderingPREFSkey = "branchOrdering";
    private String orderBranchesPREFSkey = "orderBranches";
    private String showRootPREFSkey = "showRootBranch";
    private String autoExPREFSkey = "autoExpansion";
    private String viewSubtreePREFSkey = "viewSubtree";
    private String branchWeightPREFSkey = "branchWeight";
    private static Preferences PREFS = Preferences.userNodeForPackage(TreePane.class);
}
