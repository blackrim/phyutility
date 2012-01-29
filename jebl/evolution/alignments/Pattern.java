/*
 * Pattern.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;

import java.util.List;
import java.util.Set;

/**
 * An interface representing a list of states for a list of taxa
 * (e.g. an alignment column).
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Pattern.java 658 2007-03-20 03:27:20Z twobeers $
 */
public interface Pattern {

	/**
	 * @return the data type of the states in this pattern.
	 */
	SequenceType getSequenceType();

    int getLength();

	/**
	 * @return the list of taxa that the state values correspond to.
	 */
	List<Taxon> getTaxa();

	/**
	 * Get the state for the ith taxon
	 * @param index
	 * @return the state
	 */
	State getState(int index);

	/**
	 * @return the list of state values of this pattern.
	 */
	List<State> getStates();

	/**
	 * @return the set of state values of this pattern.
	 */
	Set<State> getStateSet();

	/**
	 * Get the weight of this pattern
	 * @return the weight
	 */
	double getWeight();

	/**
	 * Returns the most frequent state in this pattern
	 * @return the most frequent state
	 */
	State getMostFrequentState();

	/**
	 * Returns the frequent of the given state in this pattern
	 * @param state
	 * @return the frequency
	 */
	double getStateFrequency(State state);
}
