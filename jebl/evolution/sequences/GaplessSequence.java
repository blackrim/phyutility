package jebl.evolution.sequences;

/**
 * @author rambaut
 *         Date: Jul 27, 2005
 *         Time: 12:48:31 AM
 */
public class GaplessSequence extends FilteredSequence {

	public GaplessSequence(Sequence source) {
		super(source);
	}

	protected State[] filterSequence(Sequence source) {
		return jebl.evolution.sequences.Utils.stripGaps(source.getStates());
	}

}
