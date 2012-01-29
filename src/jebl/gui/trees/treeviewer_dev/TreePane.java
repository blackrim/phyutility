package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.*;
import jebl.gui.trees.treeviewer_dev.decorators.Decorator;
import jebl.gui.trees.treeviewer_dev.painters.*;
import jebl.gui.trees.treeviewer_dev.treelayouts.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id: TreePane.java 724 2007-06-11 16:25:39Z rambaut $
 */
public class TreePane extends JComponent implements PainterListener, Printable {

	public final String CARTOON_ATTRIBUTE_NAME = "!cartoon";
	public final String COLLAPSE_ATTRIBUTE_NAME = "!collapse";

	public TreePane() {
	}

	public RootedTree getTree() {
		return tree;
	}

	public void setTree(RootedTree tree) {
		if (tree != null) {
			this.originalTree = tree;
			if (!originalTree.hasLengths()) {
				transformBranchesOn = true;
			}
			setupTree();
		} else {
			originalTree = null;
			this.tree = null;
			invalidate();
			repaint();
		}
	}

	private void setupTree() {
		tree = originalTree;

		if (orderBranchesOn) {
			tree = new SortedRootedTree(tree, branchOrdering);
		}

		if (transformBranchesOn || !this.tree.hasLengths()) {
			tree = new TransformedRootedTree(tree, branchTransform);
		}

		calibrated = false;
		invalidate();
		repaint();
	}

	public TreeLayout getTreeLayout() {
		return treeLayout;
	}

    public TreeLayoutCache getTreeLayoutCache() {
        return treeLayoutCache;
    }

	public void setTreeLayout(TreeLayout treeLayout) {

		this.treeLayout = treeLayout;

		treeLayout.setCartoonAttributeName(CARTOON_ATTRIBUTE_NAME);
		treeLayout.setCollapsedAttributeName(COLLAPSE_ATTRIBUTE_NAME);
		treeLayout.setBranchColouringAttributeName(branchColouringAttribute);

		treeLayout.addTreeLayoutListener(new TreeLayoutListener() {
			public void treeLayoutChanged() {
				calibrated = false;
				repaint();
			}
		});
		calibrated = false;
		invalidate();
		repaint();
	}

	public void setBranchDecorator(Decorator branchDecorator) {
		this.branchDecorator = branchDecorator;
		repaint();
	}

	public void setBranchColouringDecorator(String branchColouringAttribute, Decorator branchColouringDecorator) {
		this.branchColouringAttribute = branchColouringAttribute;
		treeLayout.setBranchColouringAttributeName(branchColouringAttribute);
		this.branchColouringDecorator = branchColouringDecorator;
		repaint();
	}

	public Rectangle2D getTreeBounds() {
		return treeBounds;
	}

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

	public void painterSettingsChanged() {
		calibrated = false;
		repaint();
	}

	public BasicStroke getBranchStroke() {
		return branchLineStroke;
	}

	public void setBranchStroke(BasicStroke stroke) {
		branchLineStroke = stroke;
		float weight = stroke.getLineWidth();
		selectionStroke = new BasicStroke(Math.max(weight + 4.0F, weight * 1.5F), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		repaint();
	}

	public BasicStroke getCalloutStroke() {
		return calloutStroke;
	}

	public void setCalloutStroke(BasicStroke calloutStroke) {
		this.calloutStroke = calloutStroke;
	}

	public Paint getSelectionPaint() {
		return selectionPaint;
	}

	public void setSelectionPaint(Paint selectionPaint) {
		this.selectionPaint = selectionPaint;
	}

	public boolean isTransformBranchesOn() {
		return transformBranchesOn;
	}

	public void setTransformBranchesOn(boolean transformBranchesOn) {
		this.transformBranchesOn = transformBranchesOn;
		setupTree();
	}

	public TransformedRootedTree.Transform getBranchTransform() {
		return branchTransform;
	}

	public void setBranchTransform(TransformedRootedTree.Transform branchTransform) {
		this.branchTransform = branchTransform;
		setupTree();
	}

	public boolean isOrderBranchesOn() {
		return orderBranchesOn;
	}

	public void setOrderBranchesOn(boolean orderBranchesOn) {
		this.orderBranchesOn = orderBranchesOn;
		setupTree();
	}

	public SortedRootedTree.BranchOrdering getBranchOrdering() {
		return branchOrdering;
	}

	public void setBranchOrdering(SortedRootedTree.BranchOrdering branchOrdering) {
		this.branchOrdering = branchOrdering;
		setupTree();
	}

	public RootedTree getOriginalTree() {
		return originalTree;
	}

	public boolean isShowingTipCallouts() {
		return showingTipCallouts;
	}

	public void setShowingTipCallouts(boolean showingTipCallouts) {
		this.showingTipCallouts = showingTipCallouts;
		calibrated = false;
		repaint();
	}

	public void setSelectedNode(Node selectedNode) {
		selectedNodes.clear();
		selectedTips.clear();
		addSelectedNode(selectedNode);
	}

	public void setSelectedTip(Node selectedTip) {
		selectedNodes.clear();
		selectedTips.clear();
		addSelectedTip(selectedTip);
	}

	public void setSelectedClade(Node selectedNode) {
		selectedNodes.clear();
		selectedTips.clear();
		addSelectedClade(selectedNode);
	}

	public void setSelectedTips(Node selectedNode) {
		selectedNodes.clear();
		selectedTips.clear();
		addSelectedTips(selectedNode);
	}

	private boolean canSelectNode(Node selectedNode) {
		return selectedNode != null;
	}
	public void addSelectedNode(Node selectedNode) {
		if ( canSelectNode(selectedNode) ) {
			selectedNodes.add(selectedNode);
		}
		fireSelectionChanged();
		repaint();
	}

	public void addSelectedTip(Node selectedTip) {
		if (selectedTip != null) {
			this.selectedTips.add(selectedTip);
		}
		fireSelectionChanged();
		repaint();
	}

	public void addSelectedClade(Node selectedNode) {
		if ( canSelectNode(selectedNode) ) {
			addSelectedChildClades(selectedNode);
		}
		fireSelectionChanged();
		repaint();
	}

	private void addSelectedChildClades(Node selectedNode) {
		selectedNodes.add(selectedNode);
		for (Node child : tree.getChildren(selectedNode)) {
			addSelectedChildClades(child);
		}
	}

	public void addSelectedTips(Node selectedNode) {
		if (selectedNode != null) {
			addSelectedChildTips(selectedNode);
		}
		fireSelectionChanged();
		repaint();
	}

	private void addSelectedChildTips(Node selectedNode) {
		if (tree.isExternal(selectedNode)) {
			selectedTips.add(selectedNode);
		}
		for (Node child : tree.getChildren(selectedNode)) {
			addSelectedChildTips(child);
		}
	}

	public void selectCladesFromSelectedNodes() {
		Set<Node> nodes = new HashSet<Node>(selectedNodes);
		selectedNodes.clear();
		for (Node node : nodes) {
			addSelectedClade(node);
		}
		fireSelectionChanged();
		repaint();
	}

	public void selectTipsFromSelectedNodes() {
		for (Node node : selectedNodes) {
			addSelectedChildTips(node);
		}
		selectedNodes.clear();
		fireSelectionChanged();
		repaint();
	}

	public void selectNodesFromSelectedTips() {
		if (selectedTips.size() > 0) {
			Node node = RootedTreeUtils.getCommonAncestorNode(tree, selectedTips);
			addSelectedClade(node);
		}

		selectedTips.clear();
		fireSelectionChanged();
		repaint();
	}

	public void selectAllTaxa() {
		selectedTips.addAll(tree.getExternalNodes());
		fireSelectionChanged();
		repaint();
	}

	public void selectAllNodes() {
		selectedNodes.addAll(tree.getNodes());
		fireSelectionChanged();
		repaint();
	}

	public void clearSelection() {
		selectedNodes.clear();
		selectedTips.clear();
		fireSelectionChanged();
		repaint();
	}

	public boolean hasSelection() {
		return selectedNodes.size() > 0 || selectedTips.size() > 0;
	}

	public void cartoonSelectedNodes() {
		cartoonSelectedNodes(tree.getRootNode());
	}

	private void cartoonSelectedNodes(Node node) {

		if (!tree.isExternal(node)) {
			if (selectedNodes.contains(node)) {
				if (node.getAttribute(CARTOON_ATTRIBUTE_NAME) != null) {
					node.removeAttribute(CARTOON_ATTRIBUTE_NAME);
				} else {
					int tipCount = RootedTreeUtils.getTipCount(tree, node);
					double height = RootedTreeUtils.getMinTipHeight(tree, node);
					Object[] values = new Object[] { tipCount, height };
					node.setAttribute(CARTOON_ATTRIBUTE_NAME, values);
				}
				calibrated = false;
				repaint();
			} else {
				for (Node child : tree.getChildren(node)) {
					cartoonSelectedNodes(child);
				}
			}
		}
	}

	public void collapseSelectedNodes() {
		collapseSelectedNodes(tree.getRootNode());
	}

	private void collapseSelectedNodes(Node node) {

		if (!tree.isExternal(node)) {
			if (selectedNodes.contains(node)) {
				if (node.getAttribute(COLLAPSE_ATTRIBUTE_NAME) != null) {
					node.removeAttribute(COLLAPSE_ATTRIBUTE_NAME);
				} else {
					String tipName = "collapsed";
					double height = RootedTreeUtils.getMinTipHeight(tree, node);
					Object[] values = new Object[] { tipName, height };
					node.setAttribute(COLLAPSE_ATTRIBUTE_NAME, values);
				}
				calibrated = false;
				repaint();
			} else {
				for (Node child : tree.getChildren(node)) {
					collapseSelectedNodes(child);
				}
			}
		}
	}

	public void annotateSelectedNodes(String name, Object value) {
		for (Node selectedNode : selectedNodes) {
			selectedNode.setAttribute(name, value);
		}
		repaint();
	}

	public void annotateSelectedTips(String name, Object value) {
		for (Node selectedTip : selectedTips) {
			Taxon selectedTaxon = tree.getTaxon(selectedTip);
			selectedTaxon.setAttribute(name, value);
		}
		repaint();
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

	public void setTipLabelPainter(LabelPainter<Node> tipLabelPainter) {
		tipLabelPainter.setTreePane(this);
		if (this.tipLabelPainter != null) {
			this.tipLabelPainter.removePainterListener(this);
		}
		this.tipLabelPainter = tipLabelPainter;
		if (this.tipLabelPainter != null) {
			this.tipLabelPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public LabelPainter<Node> getTipLabelPainter() {
		return tipLabelPainter;
	}

	public void setNodeLabelPainter(LabelPainter<Node> nodeLabelPainter) {
		nodeLabelPainter.setTreePane(this);
		if (this.nodeLabelPainter != null) {
			this.nodeLabelPainter.removePainterListener(this);
		}
		this.nodeLabelPainter = nodeLabelPainter;
		if (this.nodeLabelPainter != null) {
			this.nodeLabelPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public LabelPainter<Node> getNodeLabelPainter() {
		return nodeLabelPainter;
	}

	public void setBranchLabelPainter(LabelPainter<Node> branchLabelPainter) {
		branchLabelPainter.setTreePane(this);
		if (this.branchLabelPainter != null) {
			this.branchLabelPainter.removePainterListener(this);
		}
		this.branchLabelPainter = branchLabelPainter;
		if (this.branchLabelPainter != null) {
			this.branchLabelPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public LabelPainter<Node> getBranchLabelPainter() {
		return branchLabelPainter;
	}

	public void setNodeBarPainter(NodeBarPainter nodeBarPainter) {
		nodeBarPainter.setTreePane(this);
		if (this.nodeBarPainter != null) {
			this.nodeBarPainter.removePainterListener(this);
		}
		this.nodeBarPainter = nodeBarPainter;
		if (this.nodeBarPainter != null) {
			this.nodeBarPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public NodeBarPainter getNodeBarPainter() {
		return nodeBarPainter;
	}

	//TEST
	public void setNodeHistPainter(NodeHistPainter nodeHistPainter) {
		nodeHistPainter.setTreePane(this);
		if (this.nodeHistPainter != null) {
			this.nodeHistPainter.removePainterListener(this);
		}
		this.nodeHistPainter = nodeHistPainter;
		if (this.nodeHistPainter != null) {
			this.nodeHistPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public NodeHistPainter getNodeHistPainter() {
		return nodeHistPainter;
	}
	//END TEST
	
	public void setScaleBarPainter(Painter<TreePane> scaleBarPainter) {
		scaleBarPainter.setTreePane(this);
		if (this.scaleBarPainter != null) {
			this.scaleBarPainter.removePainterListener(this);
		}
		this.scaleBarPainter = scaleBarPainter;
		if (this.scaleBarPainter != null) {
			this.scaleBarPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public Painter<TreePane> getScaleBarPainter() {
		return scaleBarPainter;
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

	public Node getNodeAt(Graphics2D g2, Point point) {
		Rectangle rect = new Rectangle(point.x - 1, point.y - 1, 3, 3);

		for (Node node : tree.getExternalNodes()) {
			Shape taxonLabelBound = tipLabelBounds.get(node);

			if (taxonLabelBound != null && g2.hit(rect, taxonLabelBound, false)) {
				return node;
			}
		}

		for (Node node : tree.getNodes()) {
			Shape branchPath = transform.createTransformedShape(treeLayoutCache.getBranchPath(node));
			if (branchPath != null && g2.hit(rect, branchPath, true)) {
				return node;
			}
			Shape collapsedShape = transform.createTransformedShape(treeLayoutCache.getCollapsedShape(node));
			if (collapsedShape != null && g2.hit(rect, collapsedShape, false)) {
				return node;
			}
		}

		return null;
	}

	public Set<Node> getNodesAt(Graphics2D g2, Rectangle rect) {

		Set<Node> nodes = new HashSet<Node>();
		for (Node node : tree.getExternalNodes()) {
			Shape taxonLabelBound = tipLabelBounds.get(node);
			if (taxonLabelBound != null && g2.hit(rect, taxonLabelBound, false)) {
				nodes.add(node);
			}
		}

		for (Node node : tree.getNodes()) {
			Shape branchPath = transform.createTransformedShape(treeLayoutCache.getBranchPath(node));
			if (branchPath != null && g2.hit(rect, branchPath, true)) {
				nodes.add(node);
			}
			Shape collapsedShape = transform.createTransformedShape(treeLayoutCache.getCollapsedShape(node));
			if (collapsedShape != null && g2.hit(rect, collapsedShape, false)) {
				nodes.add(node);
			}
		}

		return nodes;
	}

	public Set<Node> getSelectedNodes() {
		return selectedNodes;
	}

	public Set<Node> getSelectedTips() {
		return selectedTips;
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

		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		for (Node selectedNode : selectedNodes) {
			Shape branchPath = treeLayoutCache.getBranchPath(selectedNode);
			if (branchPath != null) {
				Shape transPath = transform.createTransformedShape(branchPath);
				g2.setPaint(selectionPaint);
				g2.setStroke(selectionStroke);
				g2.draw(transPath);
			}
			Shape collapsedShape = treeLayoutCache.getCollapsedShape(selectedNode);
			if (collapsedShape != null) {
				Shape transPath = transform.createTransformedShape(collapsedShape);
				g2.setPaint(selectionPaint);
				g2.setStroke(selectionStroke);
				g2.draw(transPath);
			}
		}

		for (Node selectedTip : selectedTips) {
			g2.setPaint(selectionPaint);
			Shape labelBounds = tipLabelBounds.get(selectedTip);
			if (labelBounds != null) {
				g2.fill(labelBounds);
			}
		}

		g2.setPaint(oldPaint);
		g2.setStroke(oldStroke);

		drawTree(g2, getWidth(), getHeight());

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

		drawTree(g2, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());

		setDoubleBuffered(true);
		calibrated = false;

		return PAGE_EXISTS;
	}

	public void drawTree(Graphics2D g2, double width, double height) {

		if (!calibrated) calibrate(g2, width, height);

		AffineTransform oldTransform = g2.getTransform();
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();
		Font oldFont = g2.getFont();

		g2.setStroke(branchLineStroke);

		// Paint collapsed nodes
		for (Node node : treeLayoutCache.getCollapsedShapeMap().keySet() ) {
			Shape collapsedShape = treeLayoutCache.getCollapsedShape(node);

			Shape transShape = transform.createTransformedShape(collapsedShape);
			Paint paint = Color.BLACK;
			Paint fillPaint = null;
			if (branchDecorator != null) {
				branchDecorator.setItem(node);
				paint = branchDecorator.getPaint(paint);
				fillPaint = branchDecorator.getFillPaint(fillPaint);
			}

			if (fillPaint != null) {
				g2.setPaint(fillPaint);
				g2.fill(transShape);
			}

			g2.setPaint(paint);
			g2.draw(transShape);
		}

		// Paint branches
		for (Node node : treeLayoutCache.getBranchPathMap().keySet() ) {

			Object[] branchColouring = null;
			if (treeLayout.isShowingColouring() && branchColouringAttribute != null) {
				branchColouring = (Object[])node.getAttribute(branchColouringAttribute);
			}

			Shape branchPath = treeLayoutCache.getBranchPath(node);

			if (branchColouring != null) {
				PathIterator iter = branchPath.getPathIterator(transform);

				float[] coords1 = new float[2];
				iter.currentSegment(coords1);

				for (int i = 0; i < branchColouring.length - 1; i+=2) {
					iter.next();
					float[] coords2 = new float[2];
					iter.currentSegment(coords2);

					int colour = ((Number)branchColouring[i]).intValue();
					branchColouringDecorator.setItem(colour);
					g2.setPaint(branchColouringDecorator.getPaint(Color.BLACK));
					g2.draw(new Line2D.Float(coords1[0], coords1[1], coords2[0], coords2[1]));

					coords1 = coords2;
				}

				// Draw the remaining branch as a path so it has proper line joins...
				int colour = ((Number)branchColouring[branchColouring.length - 1]).intValue();
				branchColouringDecorator.setItem(colour);
				g2.setPaint(branchColouringDecorator.getPaint(Color.BLACK));

				GeneralPath path = new GeneralPath();
				path.moveTo(coords1[0], coords1[1]);
				iter.next();
				while (!iter.isDone()) {
					iter.currentSegment(coords1);
					path.lineTo(coords1[0], coords1[1]);
					iter.next();
				}
				g2.draw(path);

			} else {
				Shape transPath = transform.createTransformedShape(branchPath);
				if (branchDecorator != null) {
					branchDecorator.setItem(node);
					g2.setPaint(branchDecorator.getPaint(Color.BLACK));
				} else {
					g2.setPaint(Color.BLACK);
				}
				g2.draw(transPath);
			}
		}

		// Paint node bars
		if (nodeBarPainter != null && nodeBarPainter.isVisible()) {
			for (Node node : nodeBars.keySet() ) {
				Shape nodeBar = nodeBars.get(node);
				nodeBar = transform.createTransformedShape(nodeBar);
				nodeBarPainter.paint(g2, node, NodePainter.Justification.CENTER, nodeBar);
			}
		}

		// Paint tip labels
		if (tipLabelPainter != null && tipLabelPainter.isVisible()) {

			for (Node node : tipLabelTransforms.keySet()) {

				AffineTransform tipLabelTransform = tipLabelTransforms.get(node);

				Painter.Justification tipLabelJustification = tipLabelJustifications.get(node);
				g2.transform(tipLabelTransform);

				tipLabelPainter.paint(g2, node, tipLabelJustification,
						new Rectangle2D.Double(0.0, 0.0, tipLabelWidth, tipLabelPainter.getPreferredHeight()));

				g2.setTransform(oldTransform);

				if (showingTipCallouts) {
					Shape calloutPath = transform.createTransformedShape(treeLayoutCache.getCalloutPath(node));
					if (calloutPath != null) {
						g2.setStroke(calloutStroke);
						g2.draw(calloutPath);
					}
				}
			}
		}

		// Paint node labels
		if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
			for (Node node : nodeLabelTransforms.keySet() ) {

				AffineTransform nodeTransform = nodeLabelTransforms.get(node);

				Painter.Justification nodeLabelJustification = nodeLabelJustifications.get(node);
				g2.transform(nodeTransform);

				nodeLabelPainter.paint(g2, node, nodeLabelJustification,
						new Rectangle2D.Double(0.0, 0.0, nodeLabelPainter.getPreferredWidth(), nodeLabelPainter.getPreferredHeight()));

				g2.setTransform(oldTransform);
			}
		}

		// Paint branch labels
		if (branchLabelPainter != null && branchLabelPainter.isVisible()) {

			for (Node node : branchLabelTransforms.keySet() ) {

				AffineTransform branchTransform = branchLabelTransforms.get(node);

				g2.transform(branchTransform);

				branchLabelPainter.calibrate(g2, node);
				final double preferredWidth = branchLabelPainter.getPreferredWidth();
				final double preferredHeight = branchLabelPainter.getPreferredHeight();

				//Line2D labelPath = treeLayout.getBranchLabelPath(node);

				branchLabelPainter.paint(g2, node, Painter.Justification.CENTER,
						//new Rectangle2D.Double(-preferredWidth/2, -preferredHeight, preferredWidth, preferredHeight));
						new Rectangle2D.Double(0, 0, preferredWidth, preferredHeight));

				g2.setTransform(oldTransform);
			}
		}

		// Paint scale bar
		if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
			scaleBarPainter.paint(g2, this, Painter.Justification.CENTER, scaleBarBounds);
		}

		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
		g2.setFont(oldFont);
	}

	private void calibrate(Graphics2D g2, double width, double height) {

        treeLayout.layout(tree, treeLayoutCache);

        // First of all get the bounds for the unscaled tree
		treeBounds = null;
		boolean showingRootBranch = treeLayout.isShowingRootBranch();

		Node rootNode = tree.getRootNode();

		// There are two sets of bounds here. The treeBounds are the bounds of the elements
		// that make up the actual tree. These are scaled from branch length space

		// The bounds are then the extra stuff that doesn't get scaled with the tree such
		// as labels and the like.

		// bounds on branches
		for (Shape branchPath : treeLayoutCache.getBranchPathMap().values()) {
			// Add the bounds of the branch path to the overall bounds
			final Rectangle2D branchBounds = branchPath.getBounds2D();
			if (treeBounds == null) {
				treeBounds = branchBounds;
			} else {
				treeBounds.add(branchBounds);
			}
		}

		for (Shape collapsedShape : treeLayoutCache.getCollapsedShapeMap().values()) {
			// Add the bounds of the branch path to the overall bounds
			final Rectangle2D branchBounds = collapsedShape.getBounds2D();
			if (treeBounds == null) {
				treeBounds = branchBounds;
			} else {
				treeBounds.add(branchBounds);
			}
		}

		// bounds on nodeShapes
		if (nodeBarPainter != null && nodeBarPainter.isVisible()) {
			nodeBars.clear();
			// Iterate though the nodes
			for (Node node : tree.getInternalNodes()) {

				Rectangle2D shapeBounds = nodeBarPainter.calibrate(g2, node);
				treeBounds.add(shapeBounds);
				nodeBars.put(node, nodeBarPainter.getNodeBar());
			}
		}

		// adjust the bounds so that the origin is at 0,0
		//treeBounds = new Rectangle2D.Double(0.0, 0.0, treeBounds.getWidth(), treeBounds.getHeight());

		// add the tree bounds
		final Rectangle2D bounds = treeBounds.getBounds2D(); // (YH) same as (Rectangle2D) treeBounds.clone();

		final Set<Node> externalNodes = tree.getExternalNodes();

		if (tipLabelPainter != null && tipLabelPainter.isVisible()) {

			tipLabelWidth = 0.0;

			// Find the longest taxon label
			for (Node node : externalNodes) {

				tipLabelPainter.calibrate(g2, node);
				tipLabelWidth = Math.max(tipLabelWidth, tipLabelPainter.getPreferredWidth());
			}

			final double tipLabelHeight = tipLabelPainter.getPreferredHeight();

			// Iterate though the nodes with tip labels
			for (Node node : treeLayoutCache.getTipLabelPathMap().keySet()) {
				Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, tipLabelWidth, tipLabelHeight);

				// Get the line that represents the path for the taxon label
				Line2D taxonPath = treeLayoutCache.getTipLabelPath(node);

				// Work out how it is rotated and create a transform that matches that
				AffineTransform taxonTransform = calculateTransform(null, taxonPath, tipLabelWidth, tipLabelHeight, true);

				// and add the translated bounds to the overall bounds
				bounds.add(taxonTransform.createTransformedShape(labelBounds).getBounds2D());
			}
		}

		if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
			// Iterate though the nodes with node labels
			for (Node node : treeLayoutCache.getNodeLabelPathMap().keySet()) {
				// Get the line that represents the path for the taxon label
				final Line2D labelPath = treeLayoutCache.getNodeLabelPath(node);

				nodeLabelPainter.calibrate(g2, node);
				final double labelHeight = nodeLabelPainter.getPreferredHeight();
				final double labelWidth = nodeLabelPainter.getPreferredWidth();
				Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

				// Work out how it is rotated and create a transform that matches that
				AffineTransform labelTransform = calculateTransform(null, labelPath, labelWidth, labelHeight, true);

				// and add the translated bounds to the overall bounds
				bounds.add(labelTransform.createTransformedShape(labelBounds).getBounds2D());
			}
		}

		if (branchLabelPainter != null && branchLabelPainter.isVisible()) {
			// Iterate though the nodes with branch labels
			for (Node node : treeLayoutCache.getBranchLabelPathMap().keySet()) {
				// Get the line that represents the path for the branch label
				final Line2D labelPath = treeLayoutCache.getBranchLabelPath(node);

				branchLabelPainter.calibrate(g2, node);
				final double labelHeight = branchLabelPainter.getHeightBound();
				final double labelWidth = branchLabelPainter.getPreferredWidth();

				Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

				// Work out how it is rotated and create a transform that matches that
				AffineTransform labelTransform = calculateTransform(null, labelPath, labelWidth, labelHeight, false);

				// and add the translated bounds to the overall bounds
				bounds.add(labelTransform.createTransformedShape(labelBounds).getBounds2D());
			}
		}

		if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
			scaleBarPainter.calibrate(g2, this);
			scaleBarBounds = new Rectangle2D.Double(treeBounds.getX(), treeBounds.getY(),
					treeBounds.getWidth(), scaleBarPainter.getPreferredHeight());
			bounds.add(scaleBarBounds);
		}

		final double avilableW = width - insets.left - insets.right;
		final double avaiableH = height - insets.top - insets.bottom;

		// get the difference between the tree's bounds and the overall bounds

		double xDiff = bounds.getWidth() - treeBounds.getWidth();
		double yDiff = bounds.getHeight() - treeBounds.getHeight();
		assert xDiff >= 0 && yDiff >= 0;

		// small tree, long labels, label bounds may get larger that window, protect against that

		if( xDiff >= avilableW ) {
			xDiff = Math.min(avilableW, bounds.getWidth()) - treeBounds.getWidth();
		}

		if( yDiff >= avaiableH ) {
			yDiff = Math.min(avaiableH, bounds.getHeight()) - treeBounds.getHeight();
		}
		// Get the amount of canvas that is going to be taken up by the tree -
		// The rest is taken up by taxon labels which don't scale

		final double w = avilableW - xDiff;
		final double h = avaiableH - yDiff;

		double xScale;
		double yScale;

		double xOffset = 0.0;
		double yOffset = 0.0;

		if (treeLayout.maintainAspectRatio()) {
			// If the tree is layed out in both dimensions then we
			// need to find out which axis has the least space and scale
			// the tree to that (to keep the aspect ratio.
			if ((w / treeBounds.getWidth()) < (h / treeBounds.getHeight())) {
				xScale = w / treeBounds.getWidth();
				yScale = xScale;
			} else {
				yScale = h / treeBounds.getHeight();
				xScale = yScale;
			}

			treeScale = xScale;   assert treeScale > 0;

			// and set the origin so that the center of the tree is in
			// the center of the canvas
			xOffset = ((width - (treeBounds.getWidth() * xScale)) / 2) - (treeBounds.getX() * xScale);
			yOffset = ((height - (treeBounds.getHeight() * yScale)) / 2) - (treeBounds.getY() * yScale);

		} else {
			// Otherwise just scale both dimensions
			xScale = w / treeBounds.getWidth();
			yScale = h / treeBounds.getHeight();

			// and set the origin in the top left corner
			xOffset = - (treeBounds.getX() * xScale);
			yOffset = - bounds.getY();

			treeScale = xScale;   assert treeScale > 0;
		}

		// Create the overall transform
		transform = new AffineTransform();
		transform.translate(xOffset + insets.left, yOffset + insets.top);
		transform.scale(xScale, yScale);

		// Get the bounds for the newly scaled tree
		treeBounds = null;

		// bounds on branches
		for (Shape branchPath : treeLayoutCache.getBranchPathMap().values()) {
			// Add the bounds of the branch path to the overall bounds
			final Rectangle2D branchBounds = transform.createTransformedShape(branchPath).getBounds2D();
			if (treeBounds == null) {
				treeBounds = branchBounds;
			} else {
				treeBounds.add(branchBounds);
			}
		}

		// Clear the map of individual taxon label bounds and transforms
		tipLabelBounds.clear();
		tipLabelTransforms.clear();
		tipLabelJustifications.clear();

		if (tipLabelPainter != null && tipLabelPainter.isVisible()) {
			final double labelHeight = tipLabelPainter.getPreferredHeight();
			Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, tipLabelWidth, labelHeight);

			// Iterate though the external nodes with tip labels
			for (Node node : treeLayoutCache.getTipLabelPathMap().keySet()) {
				// Get the line that represents the path for the tip label
				Line2D tipPath = treeLayoutCache.getTipLabelPath(node);

				// Work out how it is rotated and create a transform that matches that
				AffineTransform taxonTransform = calculateTransform(transform, tipPath, tipLabelWidth, labelHeight, true);

				// Store the transformed bounds in the map for use when selecting
				tipLabelBounds.put(node, taxonTransform.createTransformedShape(labelBounds));

				// Store the transform in the map for use when drawing
				tipLabelTransforms.put(node, taxonTransform);

				// Store the alignment in the map for use when drawing
				final Painter.Justification just = (tipPath.getX1() < tipPath.getX2()) ?
						Painter.Justification.LEFT : Painter.Justification.RIGHT;
				tipLabelJustifications.put(node, just);
			}
		}

		// Clear the map of individual node label bounds and transforms
		nodeLabelBounds.clear();
		nodeLabelTransforms.clear();
		nodeLabelJustifications.clear();

		if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
			final double labelHeight = nodeLabelPainter.getPreferredHeight();
			final double labelWidth = nodeLabelPainter.getPreferredWidth();
			final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

			// Iterate though the external nodes with node labels
			for (Node node : treeLayoutCache.getNodeLabelPathMap().keySet()) {
				// Get the line that represents the path for the node label
				final Line2D labelPath = treeLayoutCache.getNodeLabelPath(node);

				// Work out how it is rotated and create a transform that matches that
				AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, true);

				// Store the transformed bounds in the map for use when selecting
				nodeLabelBounds.put(node, labelTransform.createTransformedShape(labelBounds));

				// Store the transform in the map for use when drawing
				nodeLabelTransforms.put(node, labelTransform);

				// Store the alignment in the map for use when drawing
				if (labelPath.getX1() < labelPath.getX2()) {
					nodeLabelJustifications.put(node, Painter.Justification.LEFT);
				} else {
					nodeLabelJustifications.put(node, Painter.Justification.RIGHT);
				}
			}
		}

		branchLabelBounds.clear();
		branchLabelTransforms.clear();
		branchLabelJustifications.clear();

		if (branchLabelPainter != null && branchLabelPainter.isVisible()) {

			// Iterate though the external nodes with branch labels
			for (Node node : treeLayoutCache.getBranchLabelPathMap().keySet()) {

				// Get the line that represents the path for the branch label
				Line2D labelPath = treeLayoutCache.getBranchLabelPath(node);

				// AR - I don't think we need to recalibrate this here
				// branchLabelPainter.calibrate(g2, node);
				final double labelHeight = branchLabelPainter.getPreferredHeight();
				final double labelWidth = branchLabelPainter.getPreferredWidth();
				final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

				final double dx = labelPath.getP2().getX() - labelPath.getP1().getX();
				final double dy = labelPath.getP2().getY() - labelPath.getP1().getY();
				final double branchLength = Math.sqrt(dx*dx + dy*dy);

				final Painter.Justification just = labelPath.getX1() < labelPath.getX2() ? Painter.Justification.LEFT :
						Painter.Justification.RIGHT;

				// Work out how it is rotated and create a transform that matches that
				AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, false);
				// move to middle of branch - since the move is before the rotation
				final double direction = just == Painter.Justification.RIGHT ? 1 : -1;
				labelTransform.translate(-direction * xScale * branchLength /2, 0);

				// Store the transformed bounds in the map for use when selecting
				branchLabelBounds.put(node, labelTransform.createTransformedShape(labelBounds));

				// Store the transform in the map for use when drawing
				branchLabelTransforms.put(node, labelTransform);

				// Store the alignment in the map for use when drawing
				branchLabelJustifications.put(node, just);
			}
		}

		if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
			scaleBarPainter.calibrate(g2, this);
			final double h1 = scaleBarPainter.getPreferredHeight();
			scaleBarBounds = new Rectangle2D.Double(treeBounds.getX(), height - h1, treeBounds.getWidth(), h1);
		}

		calloutPaths.clear();

		calibrated = true;
	}

	private AffineTransform calculateTransform(AffineTransform globalTransform, Line2D line, double width, double height, boolean just) {
		// Work out how it is rotated and create a transform that matches that
		AffineTransform lineTransform = new AffineTransform();

		final Point2D origin = line.getP1();
		if (globalTransform != null) {
			globalTransform.transform(origin, origin);
		}

		final double dx = line.getX2() - line.getX1();
		final double angle = dx != 0.0 ? Math.atan((line.getY2() - line.getY1()) / dx) : 0.0;
		lineTransform.rotate(angle, origin.getX(), origin.getY());

		// Now add a translate to the transform - if it is on the left then we need
		// to shift it by the entire width of the string.
		final double ty = origin.getY() - (height / 2.0);
		if (!just || line.getX2() > line.getX1()) {
			lineTransform.translate(origin.getX() + labelXOffset, ty);
		} else {
			lineTransform.translate(origin.getX() - (labelXOffset + width), ty);
		}

		return lineTransform;
	}

	// Overridden methods to recalibrate tree when bounds change
	public void setBounds(int x, int y, int width, int height) {
		calibrated = false;
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

	private RootedTree originalTree = null;
	private RootedTree tree = null;
	private TreeLayout treeLayout = null;
	private TreeLayoutCache treeLayoutCache = new TreeLayoutCache();

	private boolean orderBranchesOn = false;
	private SortedRootedTree.BranchOrdering branchOrdering = SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY;

	private boolean transformBranchesOn = false;
	private TransformedRootedTree.Transform branchTransform = TransformedRootedTree.Transform.CLADOGRAM;

	private Rectangle2D treeBounds = new Rectangle2D.Double();
	private double treeScale;

	//private Insets margins = new Insets(6, 6, 6, 6);
	private Insets insets = new Insets(6, 6, 6, 6);

	private Set<Node> selectedNodes = new HashSet<Node>();
	private Set<Node> selectedTips = new HashSet<Node>();

	private double rulerHeight = -1.0;
	private Rectangle2D dragRectangle = null;

	private Decorator branchDecorator = null;
	private Decorator branchColouringDecorator = null;
	private String branchColouringAttribute = null;

	private float labelXOffset = 5.0F;
	private LabelPainter<Node> tipLabelPainter = null;
	private double tipLabelWidth;
	private LabelPainter<Node> nodeLabelPainter = null;
	private LabelPainter<Node> branchLabelPainter = null;

	private NodeBarPainter nodeBarPainter = null;

	//TEST
	private NodeHistPainter nodeHistPainter = null;
	//END TEST
	
	private Painter<TreePane> scaleBarPainter = null;
	private Rectangle2D scaleBarBounds = null;

	private BasicStroke branchLineStroke = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	private BasicStroke calloutStroke = new BasicStroke(0.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{0.5f, 2.0f}, 0.0f);
	private Stroke selectionStroke = new BasicStroke(6.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private Paint selectionPaint;

	private boolean calibrated = false;
	private AffineTransform transform = null;

	private boolean showingTipCallouts = true;

	private Map<Node, AffineTransform> tipLabelTransforms = new HashMap<Node, AffineTransform>();
	private Map<Node, Shape> tipLabelBounds = new HashMap<Node, Shape>();
	private Map<Node, Painter.Justification> tipLabelJustifications = new HashMap<Node, Painter.Justification>();

	private Map<Node, AffineTransform> nodeLabelTransforms = new HashMap<Node, AffineTransform>();
	private Map<Node, Shape> nodeLabelBounds = new HashMap<Node, Shape>();
	private Map<Node, Painter.Justification> nodeLabelJustifications = new HashMap<Node, Painter.Justification>();

	private Map<Node, AffineTransform> branchLabelTransforms = new HashMap<Node, AffineTransform>();
	private Map<Node, Shape> branchLabelBounds = new HashMap<Node, Shape>();
	private Map<Node, Painter.Justification> branchLabelJustifications = new HashMap<Node, Painter.Justification>();

	private Map<Node, Shape> nodeBars = new HashMap<Node, Shape>();

	private Map<Node, Shape> calloutPaths = new HashMap<Node, Shape>();

}