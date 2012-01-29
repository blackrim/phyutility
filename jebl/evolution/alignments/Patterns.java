/*
 * Patterns.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;

import java.util.List;

/**
 * An interface representing a set of site patterns.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Patterns.java 185 2006-01-23 23:03:18Z rambaut $
 */
public interface Patterns {

    int getPatternCount();

    int getPatternLength();

    /**
     * Get a list of all the patterns
     * @return the list
     */
    List<Pattern> getPatterns();

	/**
	 * @return the list of taxa that the state values correspond to.
	 */
	List<Taxon> getTaxa();

	/**
	 * @return the data type of the states in these site patterns.
	 */
	SequenceType getSequenceType();

}
