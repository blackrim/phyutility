/*
 * Alignment.java
 *
 * (c) 2005-2006 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.Sequences;

import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Alignment.java 314 2006-05-03 01:21:14Z alexeidrummond $
 */
public interface Alignment extends Sequences, Patterns {

    List<Sequence> getSequenceList();

	int getSiteCount();
}
