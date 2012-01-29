/*
 * Sequences.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

import java.util.Set;

/**
 * A set of sequence objects.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Sequences.java 185 2006-01-23 23:03:18Z rambaut $
 */
public interface Sequences {

    Set<Sequence> getSequences();

    Sequence getSequence(Taxon taxon);
}
