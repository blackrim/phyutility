package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores a set of unique clades for a tree
 *
 * @author Marc A. Suchard
 * @version $Id: CladeSystem.java 317 2006-05-04 11:42:12 +1200 (Thu, 04 May 2006) alexeidrummond $
 */
public class AttributedCladeSystem extends CladeSystem {

	//
	// Public stuff
	//

	public AttributedCladeSystem(String name) {
		attributeName = name;

	}

	/**
	 * @param tree
	 */
	public AttributedCladeSystem(String name, RootedTree tree) {
		super(tree);
		attributeName = name;

	}

	private void addClades(RootedTree tree, Node node, Set<Taxon> cladeTaxa) {

		if (tree.isExternal(node)) {
			cladeTaxa.add(tree.getTaxon(node));
		} else {

			Set<Taxon> childCladeTaxa = new HashSet<Taxon>();
			for (Node child : tree.getChildren(node)) {

				addClades(tree, child, childCladeTaxa);
			}

			clades.add(new AttributedClade("tmp", childCladeTaxa));

			cladeTaxa.addAll(childCladeTaxa);
		}
	}

	private class AttributedClade {

		private String attributeName;
		private final List<Double> values = new ArrayList<Double>();

		public AttributedClade(String name, Set<Taxon> taxa) {
			this.attributeName = name;
			this.taxa = taxa;
			this.frequency = 1.0;
		}

		public double getFrequency() {
			return frequency;
		}

		public void setFrequency(double frequency) {
			this.frequency = frequency;
		}

		private double frequency;

		public Set<Taxon> getTaxa() {
			return taxa;
		}

		private final Set<Taxon> taxa;

		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			final AttributedClade clade = (AttributedClade) o;

			if (!taxa.equals(clade.taxa)) return false;

			return true;
		}

		public int hashCode() {
			return taxa.hashCode();
		}
	}


	//
	// Private stuff
	//
	private final List<AttributedClade> clades = new ArrayList<AttributedClade>();
	private String attributeName;
}

