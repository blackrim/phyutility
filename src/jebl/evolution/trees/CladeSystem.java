package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;

import java.util.*;

/**
 * Stores a set of unique clades for a tree
 *
 * @version $Id: CladeSystem.java 317 2006-05-03 23:42:12Z alexeidrummond $
 *
 * @author Andrew Rambaut
 */
public class CladeSystem
{
	//
	// Public stuff
	//

	public CladeSystem()
	{
	}

	/**
	 * @param tree
	 */
	public CladeSystem(RootedTree tree)
	{
		this.taxa = new TreeSet<Taxon>(tree.getTaxa());
		add(tree);
	}

	/** get number of unique clades */
	public int getCladeCount()
	{
		return clades.size();
	}

	public Set<Taxon> getClade(int index)
	{
		return clades.get(index).getTaxa();
	}

	public String getCladeString(int index)
	{
		StringBuffer buffer = new StringBuffer("{");
		boolean first = true;
		for (Taxon taxon: getClade(index)){
			if (!first) {
				buffer.append(", ");
			} else {
				first = false;
			}
			buffer.append(taxon.getName());
		}
		buffer.append("}");
		return buffer.toString();
	}

	/** get clade frequency */
	public double getCladeFrequency(int index)
	{
		return clades.get(index).getFrequency();
	}

	/** adds all the clades in the tree */
	public void add(RootedTree tree)
	{
		if (taxa == null) {
			taxa = new TreeSet<Taxon>(tree.getTaxa());
		}

		// Recurse over the tree and add all the clades (or increment their
		// frequency if already present). The root clade is not added.
		addClades(tree, tree.getRootNode(), null);
	}

	private void addClades(RootedTree tree, Node node, Set<Taxon> cladeTaxa) {

		if (tree.isExternal(node)) {
			cladeTaxa.add(tree.getTaxon(node));
		} else {

			Set<Taxon> childCladeTaxa= new HashSet<Taxon>();
			for (Node child : tree.getChildren(node)) {

				addClades(tree, child, childCladeTaxa);
			}

			clades.add(new Clade(childCladeTaxa));

			cladeTaxa.addAll(childCladeTaxa);
		}
	}

	private class Clade {
		public Clade(Set<Taxon> taxa) {
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

			final Clade clade = (Clade) o;

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
	private Set<Taxon> taxa = null;

	private final List<Clade> clades = new ArrayList<Clade>();
}

