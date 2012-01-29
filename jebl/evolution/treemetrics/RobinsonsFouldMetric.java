package jebl.evolution.treemetrics;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.treemetrics.RootedTreeMetric;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class RobinsonsFouldMetric implements RootedTreeMetric {

	public RobinsonsFouldMetric() {
		taxonMap = null;
	}

	public RobinsonsFouldMetric(List<Taxon> taxa) {
		taxonMap = new HashMap<Taxon, Integer>();
		for (int i = 0; i < taxa.size(); i++) {
			taxonMap.put(taxa.get(i), i);
		}
	}

	public double getMetric(RootedTree tree1, RootedTree tree2) {

		Map<Taxon, Integer> tm = taxonMap;

		if (tm == null) {
			List<Taxon> taxa = new ArrayList<Taxon>(tree1.getTaxa());

			tm = new HashMap<Taxon, Integer>();
			for (int i = 0; i < taxa.size(); i++) {
				tm.put(taxa.get(i), i);
			}
		}

		Set<String> clades1 = getClades(tm, tree1);
		Set<String> clades2 = getClades(tm, tree2);

		clades1.removeAll(clades2);

		return clades1.size();
	}

	private Set<String> getClades(Map<Taxon, Integer> taxa, RootedTree tree) {

		Set<String> clades = new HashSet<String>();

		getTips(taxa, tree, tree.getRootNode(), clades);

		return clades;
	}

	private Set<Integer> getTips(Map<Taxon, Integer> taxa, RootedTree tree, Node node, Set<String> clades) {

		Set<Integer> tips = new TreeSet<Integer>();

		if (tree.isExternal(node)) {
			tips.add(taxa.get(tree.getTaxon(node)));
		} else {
			Node child1 = tree.getChildren(node).get(0);
			Set<Integer> tips1 = getTips(taxa, tree, child1, clades);

			Node child2 = tree.getChildren(node).get(1);
			Set<Integer> tips2 = getTips(taxa, tree, child2, clades);

			tips.addAll(tips1);
			tips.addAll(tips2);

			clades.add(getCladeString(tips));
		}

		return tips;
	}

	private static String getCladeString(Set<Integer> tips) {
		Iterator<Integer> iter = tips.iterator();
		StringBuffer buffer = new StringBuffer();
		buffer.append(iter.next());
		while (iter.hasNext()) {
			buffer.append(",");
			buffer.append(iter.next());
		}
		return buffer.toString();
	}

	private final Map<Taxon, Integer> taxonMap;
}
