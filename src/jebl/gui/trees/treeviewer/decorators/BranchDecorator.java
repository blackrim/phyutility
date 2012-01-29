package jebl.gui.trees.treeviewer.decorators;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id: BranchDecorator.java 181 2006-01-23 17:31:10Z rambaut $
 */
public interface BranchDecorator {
    Paint getBranchPaint(Tree tree, Node node);
}
