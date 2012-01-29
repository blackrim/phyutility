package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.Nucleotides;

import java.util.List;

/**
 *
 * @author Joseph Heled
 * @version $Id: ModelBasedDistanceMatrix.java 389 2006-07-18 06:50:27Z twobeers $
 *
 */
public class ModelBasedDistanceMatrix  {
    protected static final double MAX_DISTANCE = 1000.0;

    protected  static double freqR, freqY;

    /**
     * @param sequences
     * @return array holding the count of each canonical state in the sequences.
     *         E.g. for nucleotide sequences, this array will have length 4.
     */
    private static int[] countStates(List<Sequence> sequences) {
        if (sequences.isEmpty()) {
            throw new IllegalArgumentException("No sequences passed in - unable to determine sequence type");
        }
        SequenceType sequenceType = sequences.get(0).getSequenceType();
        final int canonicalStateCount = sequenceType.getCanonicalStateCount();
        int[] counts = new int[canonicalStateCount];
        for( Sequence sequence : sequences ) {
            if (!sequence.getSequenceType().equals(sequenceType)) {
                throw new IllegalArgumentException("Sequences of mixed type");
            }
            for( int i : sequence.getStateIndices() ) {
                // ignore non definite states (ask alexei)
                if( i < canonicalStateCount ) {
                    ++counts[i];
                }
            }
        }
        return counts;
    }

    /**
     * Same as countStates, but if any of the counts would normally be 0, this
     * method adds 1 to each count to avoid counts of 0.
     *
     * @param sequences
     * @return approximation of state counts, each guaranteed to be > 0.
     */
    private static int[] countStatesSafe(List<Sequence> sequences) {
        int[] counts = countStates(sequences);
        int numSequences = counts.length;

        boolean anyZero = false;

        for (int i=0; i < numSequences; i++) {
            anyZero |= (counts[i] == 0);
        }

        // if any of the counts are 0, adjust all of them by 1 to avoid
        // division by 0 in extreme cases
        if (anyZero) {
            for (int i = 0; i < numSequences; ++i) {
                counts[i]++;
            }
        }
        return counts;
    }

    private static double[] getFrequenciesMaybeSafe(List<Sequence> sequences, boolean safe) {
        SequenceType sequenceType = sequences.get(0).getSequenceType();
        int[] counts = (safe ? countStatesSafe(sequences) : countStates(sequences));
        int canonicalStateCount = counts.length;
        double[] freqs = new double[canonicalStateCount];

        // calculate total number of residues
        long count = 0;
        for (int i=0; i < canonicalStateCount; i++) {
            count += counts[i];
        }
        for (int i=0; i < canonicalStateCount; i++) {
            freqs[i] = (double) counts[i] / (double) count;
        }

        if (sequenceType.equals(SequenceType.NUCLEOTIDE)) {
           freqR = freqs[Nucleotides.A_STATE.getIndex()] + freqs[Nucleotides.G_STATE.getIndex()];
           freqY = freqs[Nucleotides.C_STATE.getIndex()] + freqs[Nucleotides.T_STATE.getIndex()];
        }
        return freqs;
    }


    /**
     *
     * As a side effect, this method sets freqR and freqY if called on
     * nucleotide sequences.
     *
     * @param sequences A list of sequences of the same type
     * @return Approximation of the relative canonical state
     *         frequencies in the sequences; Each frequency
     *         is guaranteed to be > 0 (and therefore it can
     *         only be an approximation).
     */
    protected static double[] getFrequenciesSafe(List<Sequence> sequences) {
        return getFrequenciesMaybeSafe(sequences, true);
     }

    protected static double[] getFrequenciesSafe(Alignment alignment) {
        return getFrequenciesSafe(alignment.getSequenceList());
    }

    /**
     * As a side effect, this method sets freqR and freqY if called on
     * nucleotide sequences.
     *
     * @param sequences A list of sequences of the same type
     * @return Relative canonical state frequencies in the sequences;
     */
    protected static double[] getFrequencies(List<Sequence> sequences) {
        return getFrequenciesMaybeSafe(sequences, false);
     }

    protected static double[] getFrequencies(Alignment alignment) {
        return getFrequencies(alignment.getSequenceList());
    }
}