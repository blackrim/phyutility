package jebl.gui.trees.treeviewer.decorators;

import jebl.evolution.taxa.Taxon;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id: TaxonDecorator.java 181 2006-01-23 17:31:10Z rambaut $
 */
public interface TaxonDecorator {
    Paint getTaxonPaint(Taxon taxon);
    Font getTaxonFont(Taxon taxon, Font font);
}
