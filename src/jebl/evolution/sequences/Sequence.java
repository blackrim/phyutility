/*
 * Sequence.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;
import jebl.util.Attributable;

/**
 * A biomolecular sequence.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Sequence.java 365 2006-06-28 07:34:56Z pepster $
 */
public interface Sequence extends Attributable, Comparable {

    /**
     * @return the taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    Taxon getTaxon();

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    SequenceType getSequenceType();

    /**
     * @return a string representing the sequence of symbols.
     */
    String getString();

	/**
	 * @return an array of state objects.
	 */
	State[] getStates();

	/**
	 * @return an array of state indices.
	 */
	byte[] getStateIndices();

    /**
     * @return the state at site.
     */
    State getState(int site);

	/**
	 * Get the length of the sequence
	 * @return the length
	 */
    int getLength();
}
