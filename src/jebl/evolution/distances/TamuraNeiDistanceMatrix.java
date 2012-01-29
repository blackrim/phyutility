package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.State;
import jebl.util.ProgressListener;

/**
 * Date: 22/01/2006
 * Time: 17:28:59
 *
 * @author Joseph Heled
 * @version $Id: TamuraNeiDistanceMatrix.java 715 2007-05-24 23:13:04Z matthew_cheung $
 *
 *  Estimation of the Number of Nucleotide Substitutions in
 *  the Control Region of Mitochondrial DNA in Humans and
 *  Chimpanzees. Koichiro Tamura and Masatoshi Nei, 1993
 *
 * Estimated Distance is d = 2 (pi(A) pi(G) k_R + pi(T) pi(C) k_Y + PI(A)PI(C)) t, where k_R/k_Y are the rates of
 * Purine/Pyrimidine respectivly.
 *
 * When distances grow large, the formulas break as estimates of number of transition/transversion becomes inconsistent,
 * which results in negative logs. Returning "infinity" is not optimal as it breaks operations such as constructing
 * consensus trees where distances for resampled sequences vary between (say) 2-3 and out "infinity" 10.
 * As a workaround I try to reduce the number of transitions/transversions by the minimum amout which brings the estimates
 * back to a consistent state.
 */

public class TamuraNeiDistanceMatrix extends BasicDistanceMatrix {

    public TamuraNeiDistanceMatrix(Alignment alignment, ProgressListener progress) {
        super(alignment.getTaxa(), Initializer.getDistances(alignment, progress));
    }

    static class Initializer extends ModelBasedDistanceMatrix {

        private static Alignment alignment;

        // used in correction formula
        private static double constA1, constA2, constC;

        /**
         * Calculate a pairwise distance
         */
        static private double calculatePairwiseDistance(int taxon1, int taxon2) {

            double sumTsAG = 0.0;
            double sumTsCT = 0.0;
            double sumTv = 0.0;
            double sumWeight = 0.0;
            boolean noGapsPairFound = false;

            for( Pattern pattern : alignment.getPatterns() ) {
                State state1 = pattern.getState(taxon1);
                State state2 = pattern.getState(taxon2);

                double weight = pattern.getWeight();
                // acgt

                // ignore any ambiguous states or gaps
                if( state1.isAmbiguous() || state2.isAmbiguous() ) {
                    continue;
                } else {
                    noGapsPairFound = true;
                }


                if ( state1 != state2 ) {
                    if ( Nucleotides.isTransition(state1, state2) ) {
                        // it's a transition
                        if( Nucleotides.isPurine(state1) ) {
                            sumTsAG += weight;
                        } else {
                            sumTsCT += weight;
                        }
                    } else {
                        // it's a transversion
                        sumTv += weight;
                    }
                }
                sumWeight += weight;
            }

            if(! noGapsPairFound ) {
                throw new CannotBuildDistanceMatrixException("It is not possible to compute the Tamura-Nei genetic distance " +
                        "for these sequences because at least one pair of sequences do not overlap in the alignment.");
            }

            // Unfortuanetly adjusting number of sites for Purine/Pyrimidine may turn the other into negative - so
            // we iterate untile both estimates are consistent
            while( true ) {

                double P1 = sumTsAG / sumWeight;
                double P2 = sumTsCT / sumWeight;
                double Q  = sumTv / sumWeight;

                double a1 = 1.0 - P1 * (1 / (2 * constA1)) - Q * (1 / (2 * freqR));
                double a2 = 1.0 - P2 * (1 / (2 * constA2)) - Q * (1 / (2 * freqY));

                if( a1 <= 0 ) {
                    // smallest number of sites to remove which makes a1 positive.
                    int adjustment = (int)(1 + (sumWeight * -a1) / ((1 / (2 * constA1)) - 1));
                    sumTsAG -= adjustment;
                    if( sumTsAG < 0 )  break;
                    sumWeight -= adjustment;
                    continue;
                }

                if( a2 <= 0 ) {
                    // smallest number of sites to remove which makes a2 positive.
                    int adjustment = (int)(1 + (sumWeight * -a2) / ((1 / (2 * constA2)) - 1));
                    sumTsCT -= adjustment;
                    if( sumTsCT < 0 )  break;
                    sumWeight -= adjustment;
                    continue;
                }

                double b = 1.0 - (Q / (2.0 * constC));
                if( b <= 0 ) {
                    break;
                }

                double distance = -2.0 * ((constC - constA1*freqY - constA2*freqR) * Math.log(b)
                        + constA1 * Math.log(a1) + constA2 * Math.log(a2));

                return Math.min(distance, MAX_DISTANCE);
            }
            return MAX_DISTANCE;
        }


        static double[][] getDistances(Alignment alignment, ProgressListener progress) {
            Initializer.alignment = alignment;

            // ASK Alexei
            final int stateCount = alignment.getSequenceType().getCanonicalStateCount();

            if (stateCount != 4) {
                throw new IllegalArgumentException("Tamura NeiDistanceMatrix must have nucleotide patterns");
            }

            double[] freqs = getFrequenciesSafe(alignment);

            double freqA = freqs[Nucleotides.A_STATE.getIndex()];
            double freqC = freqs[Nucleotides.C_STATE.getIndex()];
            double freqG = freqs[Nucleotides.G_STATE.getIndex()];
            double freqT = freqs[Nucleotides.T_STATE.getIndex()];

            // avoid arithmetic underflow by dividing first
            constA1 = freqA * (freqG / freqR);
            constA2 = freqT * (freqC / freqY);
            constC =  (freqR * freqY);

            assert(constA1 > 0.0 && constA2 > 0.0 && constC > 0.0);

            final int dimension = alignment.getTaxa().size();
            double[][] distances = new double[dimension][dimension];

            float tot = (dimension * (dimension - 1)) / 2;
            int done = 0;
            for(int i = 0; i < dimension; ++i) {
                for(int j = i+1; j < dimension; ++j) {
                    distances[i][j] = calculatePairwiseDistance(i, j);
                    distances[j][i] = distances[i][j];
                    if( progress != null ) progress.setProgress( ++done / tot);
                }
            }
            return distances;
        }
    }
}