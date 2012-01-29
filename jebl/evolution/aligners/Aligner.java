package jebl.evolution.aligners;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;
import jebl.util.ProgressListener;

import java.util.Collection;

/**
 *
 * As of 2006-12-06, this interface is not used anywhere in JEBL, and it doesn't have
 * any implementing classes. It is only a proposed future alternative to the existing
 * abstract class Align
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Aligner.java 559 2006-12-06 22:20:38Z twobeers $
 */
public interface Aligner {

    Alignment alignSequences(Collection<Sequence> sequences);

	void addProgressListener(ProgressListener listener);

	void removeProgressListener(ProgressListener listener);
}
