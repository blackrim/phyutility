package jebl.evolution.sequences;

/**
 * @author rambaut
 *         Date: Jul 27, 2005
 *         Time: 12:48:31 AM
 */
public class TranslatedSequence extends FilteredSequence {

	public TranslatedSequence(Sequence source, GeneticCode geneticCode) {
		super(source);

		this.geneticCode = geneticCode;
	}

	protected State[] filterSequence(Sequence source) {
		return jebl.evolution.sequences.Utils.translate(source.getStates(), geneticCode);
	}

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    public SequenceType getSequenceType() {
        return SequenceType.AMINO_ACID;
    }

	private final GeneticCode geneticCode;

}
