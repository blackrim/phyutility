/*
 * ParsimonyCriterion.java
 *
 * (c) 2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.parsimony;

import jebl.evolution.graphs.Node;
import jebl.evolution.sequences.State;
import jebl.evolution.trees.Tree;

/**
 * @author rambaut
 * @author Alexei Drummond
 *
 * Date: Jun 20, 2005
 * Time: 4:56:34 PM
 *
 * @version $Id: ParsimonyCriterion.java 185 2006-01-23 23:03:18Z rambaut $
 */
public interface ParsimonyCriterion {

    /**
     * Calculates the minimum number of steps for the parsimony reconstruction for the given tree.
     * It is expected that the implementation's constructor will be set up with the characters so
     * that repeated calls can be made to this function to evaluate different trees.
     * @param tree a tree object to reconstruct the characters on
     * @return an array containing the parsimony score for each site
     */
    double[] getSiteScores(Tree tree);

    /**
     * Calculates the minimum number of steps for the parsimony reconstruction for the given tree.
     * It is expected that the implementation's constructor will be set up with the characters so
     * that repeated calls can be made to this function to evaluate different trees.
     * @param tree a tree object to reconstruct the characters on
     * @return the total score
     */
    double getScore(Tree tree);

    /**
     * Returns the reconstructed character states for a given node in the tree.
     * @param tree a tree object to reconstruct the characters on
     * @param node the node of the tree
     * @return an array containing the reconstructed states for this node
     */
    State[] getStates(Tree tree, Node node);
}
