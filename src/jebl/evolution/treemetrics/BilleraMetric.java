package jebl.evolution.treemetrics;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.TreeBiPartitionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Billera tree distance - sum of change in branch lengths required to transform one tree to the second
 *
 * Note that this interface is not optimal for a large set where all pairs are required.
 * Creating TreeBiPartitionInfo's as a pre step is better unless memory is an issue.
 * 
 * @author Joseph Heled
 * @version $Id$
 */
public class BilleraMetric implements RootedTreeMetric {
    public double getMetric(RootedTree tree1, RootedTree tree2) {
        List<Taxon> taxa = new ArrayList<Taxon>(tree1.getTaxa());
        TreeBiPartitionInfo p1 = new TreeBiPartitionInfo(tree1, taxa);
        TreeBiPartitionInfo p2 = new TreeBiPartitionInfo(tree2, taxa);
        return TreeBiPartitionInfo.distance(p1, p2, TreeBiPartitionInfo.DistanceNorm.NORM1);
    }
}
