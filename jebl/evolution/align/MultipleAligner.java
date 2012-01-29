package jebl.evolution.align;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.trees.RootedTree;
import jebl.util.ProgressListener;

import java.util.List;

/**
 * @author Joseph Heled
 * @version $Id: MultipleAligner.java 482 2006-10-25 06:30:57Z twobeers $
 *          Date: 6/05/2006 Time: 08:08:22
 */
public interface MultipleAligner {
    Alignment doAlign(List<Sequence> seqs, RootedTree guideTree, ProgressListener progress);

    Alignment doAlign(Alignment a1, Alignment a2, ProgressListener progress);

    Alignment doAlign(Alignment alignment, Sequence sequence, ProgressListener progress);
}
