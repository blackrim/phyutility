package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.trees.*;
import jebl.evolution.graphs.Node;
import jebl.gui.trees.treeviewer_dev.treelayouts.TreeLayout;
import jebl.gui.trees.treeviewer_dev.painters.*;
import jebl.gui.trees.treeviewer_dev.painters.NodeBarPainter;
import jebl.gui.trees.treeviewer_dev.painters.ScaleBarPainter;
import jebl.gui.trees.treeviewer_dev.decorators.Decorator;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.util.ArrayList;
import java.util.Collection;


/**
 * @author Andrew Rambaut
 * @version $Id: MultiPaneTreeViewer.java 537 2006-11-22 22:24:54Z rambaut $
 */
public class MultiPaneTreeViewer extends TreeViewer {

    private final static double MAX_ZOOM = 20;
    private final static double MAX_VERTICAL_EXPANSION = 20;

    /**
     * Creates new TreeViewer
     */
    public MultiPaneTreeViewer() {
        treePanes.add(new TreePane());

        setLayout(new BorderLayout());

        treePanePanel = new MultiPaneTreePanel();
        treePanePanel.setLayout(new BoxLayout(treePanePanel, BoxLayout.PAGE_AXIS));

        JScrollPane scrollPane = new JScrollPane(treePanePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setMinimumSize(new Dimension(150, 150));

        scrollPane.setBorder(null);
        viewport = scrollPane.getViewport();

        add(scrollPane, BorderLayout.CENTER);

    }

    public void setTree(Tree tree) {
        trees.clear();
        addTree(tree);
        showTree(0);
    }

    public void setTrees(Collection<? extends Tree> trees) {
        this.trees.clear();
        for (Tree tree : trees) {
            addTree(tree);
        }
        showTree(0);
    }

    protected void addTree(Tree tree) {
        this.trees.add(tree);
        showTree(trees.size() - 1);

        TreePane treePane = treePanes.get(0);
        if (treePane.getTipLabelPainter() != null) {
            treePane.getTipLabelPainter().setupAttributes(trees);
        }

        if (treePane.getBranchLabelPainter() != null) {
            treePane.getBranchLabelPainter().setupAttributes(trees);
        }

        if (treePane.getNodeLabelPainter() != null) {
            treePane.getNodeLabelPainter().setupAttributes(trees);
        }

        if (treePane.getNodeBarPainter() != null) {
            treePane.getNodeBarPainter().setupAttributes(trees);
        }
    }

    public void addTrees(Collection<? extends Tree> trees) {
        int count = getTreeCount();
        for (Tree tree : trees) {
            addTree(tree);
        }
        showTree(count);
    }

    public Tree getTree() {
        return trees.get(0);
    }

    public java.util.List<Tree> getTrees() {
        return trees;
    }

    public int getTreesPerPage() {
        return treesPerPage;
    }

    public void setTreesPerPage(int treesPerPage) {
        this.treesPerPage = treesPerPage;
        if (treePanes.size() < treesPerPage) {
            while (treePanes.size() < treesPerPage) {
                treePanes.add(new TreePane());
            }
        } else if (treePanes.size() > treesPerPage) {
            while (treePanes.size() > treesPerPage) {
                treePanes.remove(treePanes.size() - 1);
            }
        }
        showTree(currentTreeIndex);
    }

    private void setupTreePane(TreePane treePane) {
        treePane.setAutoscrolls(true); //enable synthetic drag events

        // This overrides MouseListener and MouseMotionListener to allow selection in the TreePane -
        // It installs itself within the constructor.
        treePaneSelector = new TreePaneSelector(treePane);
    }

    public Tree getCurrentTree() {
        return trees.get(currentTreeIndex);
    }


    public int getCurrentTreeIndex() {
        return currentTreeIndex;
    }

    public int getTreeCount() {
        if (trees == null) return 0;
        return trees.size();
    }

    public void showTree(int index) {
        int i = index;
        for (TreePane treePane : treePanes) {
            if (i < trees.size()) {
                Tree tree = trees.get(i);

                if (tree instanceof RootedTree) {
                    treePane.setTree((RootedTree)tree);
                } else {
                    treePane.setTree(Utils.rootTheTree(tree));
                }
            } else {
                treePane.setTree(null);
            }
            i++;
        }
        currentTreeIndex = index;

	    treePanePanel.removeAll();
	    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	    for (TreePane treePane : treePanes) {
	        treePanePanel.add(treePane);
	        setupTreePane(treePane);
	    }

        fireTreeChanged();
    }

    public void showNextTree() {
        if (currentTreeIndex < trees.size() - 1) {
            showTree(currentTreeIndex + 1);
        }
    }

    public void showPreviousTree() {
        if (currentTreeIndex > 0) {
            showTree(currentTreeIndex - 1);
        }
    }

    public void setTreeLayout(TreeLayout treeLayout) {
        for (TreePane treePane : treePanes) {
            treePane.setTreeLayout(treeLayout);
        }
    }

    private boolean zoomPending = false;
    private double zoom = 0.0, verticalExpansion = 0.0;

    public void setZoom(double zoom) {
        this.zoom = zoom * MAX_ZOOM;
        refreshZoom();
    }

    public void setVerticalExpansion(double verticalExpansion) {
        this.verticalExpansion = verticalExpansion * MAX_VERTICAL_EXPANSION;
        refreshZoom();
    }

    public boolean verticalExpansionAllowed() {
        return !treePanes.get(0).maintainAspectRatio();
    }

    private void refreshZoom() {
        setZoom(zoom, zoom + verticalExpansion);
    }

    private void setZoom(double xZoom, double yZoom) {

        Dimension viewportSize = viewport.getViewSize();
        Point position = viewport.getViewPosition();

        Dimension extentSize = viewport.getExtentSize();
        double w = extentSize.getWidth() * (1.0 + xZoom);
        double h = extentSize.getHeight() * (1.0 + yZoom);

        Dimension newSize = new Dimension((int) w, (int) h / treesPerPage);
        for (TreePane treePane : treePanes) {
            treePane.setPreferredSize(newSize);
            treePane.revalidate();
        }

        double cx = position.getX() + (0.5 * extentSize.getWidth());
        double cy = position.getY() + (0.5 * extentSize.getHeight());

        double rx = ((double) newSize.getWidth()) / viewportSize.getWidth();
        double ry = ((double) newSize.getHeight()) / viewportSize.getHeight();

        double px = (cx * rx) - (extentSize.getWidth() / 2.0);
        double py = (cy * ry) - (extentSize.getHeight() / 2.0);

        Point newPosition = new Point((int) px, (int) py);
        viewport.setViewPosition(newPosition);
    }

    public boolean hasSelection() {
        for (TreePane treePane : treePanes) {
            if (treePane.hasSelection()) return true;
        }
        return false;
    }

    public void selectTaxa(MultiPaneTreeViewer.SearchType searchType, String searchString, boolean caseSensitive) {
//        treePane.clearSelection();
//
//        if (searchType == MultiPaneTreeViewer.SearchType.MATCHES && !caseSensitive) {
//            throw new IllegalArgumentException("Regular expression matching cannot be case-insensitive");
//        }
//
//        String query = (caseSensitive ? searchString : searchString.toUpperCase());
//
//        Tree tree = treePane.getTree();
//
//        for (Node node : tree.getExternalNodes()) {
//            Taxon taxon = tree.getTaxon(node);
//            String target = (caseSensitive ? taxon.getName() : taxon.getName().toUpperCase());
//            switch (searchType) {
//                case CONTAINS:
//                    if (target.contains(query)) {
//                        treePane.addSelectedTip(node);
//                    }
//                    break;
//                case STARTS_WITH:
//                    if (target.startsWith(query)) {
//                        treePane.addSelectedTip(node);
//                    }
//                    break;
//                case ENDS_WITH:
//                    if (target.endsWith(query)) {
//                        treePane.addSelectedTip(node);
//                    }
//                    break;
//                case MATCHES:
//                    if (target.matches(query)) {
//                        treePane.addSelectedTip(node);
//                    }
//                    break;
//            }
//        }
    }

    public void selectNodes(String attribute, MultiPaneTreeViewer.SearchType searchType, String searchString, boolean caseSensitive) {
//        treePane.clearSelection();
//
//        if (searchType == MultiPaneTreeViewer.SearchType.MATCHES && !caseSensitive) {
//            throw new IllegalArgumentException("Regular expression matching cannot be case-insensitive");
//        }
//
//        String query = (caseSensitive ? searchString : searchString.toUpperCase());
//
//        Tree tree = treePane.getTree();
//
//        for (Node node : tree.getNodes()) {
//            Object value = node.getAttribute(attribute);
//
//            if (value != null) {
//                String target = (caseSensitive ?
//                        value.toString() : value.toString().toUpperCase());
//                switch (searchType) {
//                    case CONTAINS:
//                        if (target.contains(query)) {
//                            treePane.addSelectedNode(node);
//                        }
//                        break;
//                    case STARTS_WITH:
//                        if (target.startsWith(query)) {
//                            treePane.addSelectedNode(node);
//                        }
//                        break;
//                    case ENDS_WITH:
//                        if (target.endsWith(query)) {
//                            treePane.addSelectedNode(node);
//                        }
//                        break;
//                    case MATCHES:
//                        if (target.matches(query)) {
//                            treePane.addSelectedNode(node);
//                        }
//                        break;
//                }
//            }
//        }
    }

    public void collapseSelectedNodes() {
//         treePane.collapseSelectedNodes();
    }

    public void annotateSelectedNodes(String name, Object value) {
//        treePane.annotateSelectedNodes(name, value);
        fireTreeSettingsChanged();
    }

    public void annotateSelectedTips(String name, Object value) {
        //       treePane.annotateSelectedTips(name, value);
        fireTreeSettingsChanged();
    }

    public void selectAll() {
//        if (treePaneSelector.getSelectionMode() == TreePaneSelector.SelectionMode.TAXA) {
//            treePane.selectAllTaxa();
//        } else {
//            treePane.selectAllNodes();
//        }
    }

    public void clearSelectedTaxa() {
//        treePane.clearSelection();
    }

    public void addTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        for (TreePane treePane : treePanes) {
            treePane.addTreeSelectionListener(treeSelectionListener);
        }
    }

    public void removeTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        for (TreePane treePane : treePanes) {
            treePane.removeTreeSelectionListener(treeSelectionListener);
        }
    }

    public void setSelectionMode(TreePaneSelector.SelectionMode selectionMode) {
//        TreePaneSelector.SelectionMode oldSelectionMode = treePaneSelector.getSelectionMode();
//
//        if (selectionMode == oldSelectionMode) {
//            return;
//        }
//
//        if (oldSelectionMode == TreePaneSelector.SelectionMode.TAXA) {
//            treePane.selectNodesFromSelectedTips();
//        } else if (selectionMode == TreePaneSelector.SelectionMode.TAXA) {
//            treePane.selectTipsFromSelectedNodes();
//        } else if (selectionMode == TreePaneSelector.SelectionMode.CLADE) {
//            treePane.selectCladesFromSelectedNodes();
//        }
//        treePaneSelector.setSelectionMode(selectionMode);
    }

    public void setDragMode(TreePaneSelector.DragMode dragMode) {
        treePaneSelector.setDragMode(dragMode);
    }

    // A load of deligated method calls through to treePane (which is now hidden outside the package).
    public void setTipLabelPainter(LabelPainter<Node> tipLabelPainter) {
        for (TreePane treePane : treePanes) {
            treePane.setTipLabelPainter(tipLabelPainter);
        }
        tipLabelPainter.setupAttributes(trees);
        fireTreeSettingsChanged();
    }

    public void setNodeLabelPainter(LabelPainter<Node> nodeLabelPainter) {
        for (TreePane treePane : treePanes) {
            treePane.setNodeLabelPainter(nodeLabelPainter);
        }
        nodeLabelPainter.setupAttributes(trees);
        fireTreeSettingsChanged();
    }

    public void setNodeBarPainter(NodeBarPainter nodeBarPainter) {
        for (TreePane treePane : treePanes) {
            treePane.setNodeBarPainter(nodeBarPainter);
        }
        nodeBarPainter.setupAttributes(trees);
        fireTreeSettingsChanged();
    }

    //TEST
    public void setNodeHistPainter(NodeHistPainter nodeHistPainter) {
        for (TreePane treePane : treePanes) {
            treePane.setNodeHistPainter(nodeHistPainter);
        }
        nodeHistPainter.setupAttributes(trees);
        fireTreeSettingsChanged();
    }
    //END TEST
    public void setBranchLabelPainter(LabelPainter<Node> branchLabelPainter) {
        for (TreePane treePane : treePanes) {
            treePane.setBranchLabelPainter(branchLabelPainter);
        }
        branchLabelPainter.setupAttributes(trees);
        fireTreeSettingsChanged();
    }

    public void setScaleBarPainter(ScaleBarPainter scaleBarPainter) {
        for (TreePane treePane : treePanes) {
            treePane.setScaleBarPainter(scaleBarPainter);
        }
        fireTreeSettingsChanged();
    }

    public void setBranchDecorator(Decorator branchDecorator) {
        for (TreePane treePane : treePanes) {
            treePane.setBranchDecorator(branchDecorator);
        }
        fireTreeSettingsChanged();
    }

    public void setBranchColouringDecorator(String branchColouringAttribute, Decorator branchColouringDecorator) {
        for (TreePane treePane : treePanes) {
            treePane.setBranchColouringDecorator(branchColouringAttribute, branchColouringDecorator);
        }
        fireTreeSettingsChanged();
    }

    public void setSelectionPaint(Paint selectionPane) {
        for (TreePane treePane : treePanes) {
            treePane.setSelectionPaint(selectionPane);
        }
        fireTreeSettingsChanged();
    }

    public Paint getSelectionPaint() {
        return treePanes.get(0).getSelectionPaint();
    }

    public void setBranchStroke(BasicStroke branchStroke) {
        for (TreePane treePane : treePanes) {
            treePane.setBranchStroke(branchStroke);
        }
        fireTreeSettingsChanged();
    }

    public boolean isTransformBranchesOn() {
        return treePanes.get(0).isTransformBranchesOn();
    }

    public TransformedRootedTree.Transform getBranchTransform() {
        return treePanes.get(0).getBranchTransform();
    }

    public void setTransformBranchesOn(boolean transformBranchesOn) {
        for (TreePane treePane : treePanes) {
            treePane.setTransformBranchesOn(transformBranchesOn);
        }
        fireTreeSettingsChanged();
    }

    public void setBranchTransform(TransformedRootedTree.Transform transform) {
        for (TreePane treePane : treePanes) {
            treePane.setBranchTransform(transform);
        }
        fireTreeSettingsChanged();
    }

    public boolean isOrderBranchesOn() {
        return treePanes.get(0).isOrderBranchesOn();
    }

    public SortedRootedTree.BranchOrdering getBranchOrdering() {
        return treePanes.get(0).getBranchOrdering();
    }

    public void setOrderBranchesOn(boolean orderBranchesOn) {
        for (TreePane treePane : treePanes) {
            treePane.setOrderBranchesOn(orderBranchesOn);
        }
        fireTreeSettingsChanged();
    }

    public void setBranchOrdering(SortedRootedTree.BranchOrdering branchOrdering) {
        for (TreePane treePane : treePanes) {
            treePane.setBranchOrdering(branchOrdering);
        }
        fireTreeSettingsChanged();
    }

    public JComponent getContentPane() {
        return treePanePanel;
    }

    public void paint(Graphics g) {
        if( zoomPending  ) {
            refreshZoom();
            zoomPending = false;
        }
        super.paint(g);
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        return treePanePanel.print(g, pageFormat, pageIndex);
    }

    public void addTreeViewerListener(TreeViewerListener listener) {
        listeners.add(listener);
    }

    public void removeTreeViewerListener(TreeViewerListener listener) {
        listeners.remove(listener);
    }

    public void fireTreeChanged() {
        for (TreeViewerListener listener : listeners) {
            listener.treeChanged();
        }
    }

    public void fireTreeSettingsChanged() {
        for (TreeViewerListener listener : listeners) {
            listener.treeSettingsChanged();
        }
    }

    private java.util.List<TreeViewerListener> listeners = new ArrayList<TreeViewerListener>();

    private java.util.List<Tree> trees = new ArrayList<Tree>();
    private java.util.List<TreePane> treePanes = new ArrayList<TreePane>();
    private int currentTreeIndex = 0;
    private int treesPerPage = 1;

    private MultiPaneTreePanel treePanePanel;
    protected TreePaneSelector treePaneSelector;
    protected JViewport viewport;

    class MultiPaneTreePanel extends JPanel implements Printable {

        public int print(Graphics graphics, PageFormat pageFormat, int i) throws PrinterException {
            return 0;
        }
    }
}
