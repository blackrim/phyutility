package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.State;
import jebl.util.ProgressListener;

/**
 *  Compute HKY corrected distance matrix
 *
 * @author Andrew Rambaut
 * @author Joseph Heled
 * @version $Id: HKYDistanceMatrix.java 715 2007-05-24 23:13:04Z matthew_cheung $
 *
 * Adapted from BEAST source code.
 *
 * The code in this file appeared originally in the file F84DistanceMatrix, and the comment said (as above) HKY.
 * Initially I thought it was one model under two different names, but that was my ignorance. While similar,
 * there is an small difference which I was able to understance only by examining the transition
 * probability and rate matrices for both models.
 *
 * Simply put, HKY assumes *the same* ratio of transitions/transversions for all bases, while F84 has a
 * different ratio for Purines and Pyrimidines. The confusion stems from the fact that those two different ratios
 * depend on just one parameter. If Kappa is the ratio for HKY then for F84
 * Kappa(Purine aka A,G) = 1 + Kappa/(pi(A)+pi(G)) and Kappa(Pyrimidine aka C,T) = 1 + Kappr/(pi(C)+pi(T)).
 *
 * This difference simplifies the estimation of evolutionary distance and Kappa from F84, since some
 * entries in the HKY transition matrix contain expressions involving exp(-t alpha), where alpha depend on both Kappa
 * and wheather the element is a Purine or a Pyrimidine.
 *
 * pi(A,C,G,T) = stationary frequencies
 *              F84                    HKY      Tamura-Nei
 * K(AG) = 1 + Kappa/(pi(A)+pi(G))     Kappa       a1
 *
 * K(CT) = 1 + Kappa/(pi(C)+pi(T))     Kappa       a2
 *
 *  b    =       1                       1         beta
 *
 * Rate Matrix
 *
 *      |   -     b   K(AG)   b   |   | pi(A)   0     0     0   |
 * Q =  |   b     -     b   K(CT) |   |  0    pi(C)   0     0   |
 *      | K(AG)   1     -     b   |   |  0      0   pi(G)   0   |
 *      |   b   K(CT)   b    -    |   |  0      0     0   pi(T) |
 *
 *
 * Transition Probability Matrix
 *
 * PI(A) = PI(G) = pi(A) + pi(G)
 * PI(C) = PI(T) = pi(C) + pi(T)
 *
 * Alpha(j) = 1 + Kappa        F84
 *            1 + PI(j)*(k-1)  HKY
 *
 * P(transversion) = pi,(i+1)%4 = pi,(i+3)%4 =  pi(i) * (1 - exp(-t))
 * P(transition)   = pi,(i+2)%4 = pi(i) + pi(j)(1/PI(j) - 1) * exp(-t)  -    pi(i)/PI(i)    * exp(-t * Alpha(i))
 * Pi,i =                         pi(i) + pi(i)(1/PI(i) - 1) * exp(-t)  - (pi(i)/PI(i) - 1) * exp(-t * Alpha(i))
 *
 *
 * From the above it is easy to see that for F84 Sum(all transversions) = 2 Sum(p(i)) (1 - exp(-t)) = 2(1-exp(-t))
 * which gives the best estimate of t as -log(1 - Sum(all transversions)/2).
 * When there are no transversions (say when sequences are very short or distance is very small) this estimate is 0
 * even when sequences are not identical. I am not sure if there is an easy way to fix this since in this case
 * Kappa is confused with t and only t*(Kappa+1) can be estimated.
 *
 * The code in this file estimates the "evolutionary distance", which is
 * (2*Kappa*(pi(A)*pi(G) + pi(T)*pi(C)) + 2*(pi(A) + PI(C))*beta) * t. (*)
 * This raises a question since the stationary frequencies are estimated from all the sequences, but
 * transition/transversion rates are done for each pair individually. The result is that distances are not neccesarily
 * scaled correctly for the matrix as a whole. I am still trying to figure this out.
 *
 * (*) The original code had a bug which counted only A<-->G substitutions while C<-->T where ignored.
 *
 */

public class HKYDistanceMatrix extends BasicDistanceMatrix {

    public HKYDistanceMatrix(Alignment alignment, ProgressListener progress) {
        super(alignment.getTaxa(), Initializer.getDistances(alignment, progress));
    }

    static class Initializer extends ModelBasedDistanceMatrix {
        //
        // Private stuff
        //
        private static Alignment alignment;

        //used in correction formula
        static private double constA, constB, constC;

        /**
         * Calculate the distance between two of the sequences from the alignment.
         */
        static private double calculatePairwiseDistance(int taxon1, int taxon2) {
            double sumTs = 0.0; // total weight of all columns that have a transition for these taxa
            double sumTv = 0.0; // total weight of all columns that have a transversion for these taxa
            double sumWeight = 0.0; // total weight of all columns (ignoring those with ambiguities, but
                                    // including identical columns (which have neither a transition nor a transversion) )
            boolean noGapsPairFound = false;

            for( Pattern pattern : alignment.getPatterns() ) {
                State state1 = pattern.getState(taxon1);
                State state2 = pattern.getState(taxon2);

                // ignore any ambiguous or gaps
                if( state1.isAmbiguous() || state2.isAmbiguous() ) {
                   continue;
                } else {
                    noGapsPairFound = true;
                }

                double weight = pattern.getWeight();
                // acgt
                if ( state1 != state2 ) {
                    if ( Nucleotides.isTransition(state1, state2) ) {
                        // it's a transition
                        sumTs += weight;
                    } else {
                        // it's a transversion
                        sumTv += weight;
                    }
                }
                sumWeight += weight; // this also includes the columns with state1 == state2
            }

            if(!noGapsPairFound)
                throw new CannotBuildDistanceMatrixException("It is not possible to compute the HKY genetic distance " +
                    "for these sequences because at least one pair of sequences do not overlap in the alignment.");

            if( sumWeight <= 0.0 ) {
                return MAX_DISTANCE;
            }
            
            while( true ) {

                double P = sumTs / sumWeight;
                double Q = sumTv / sumWeight;

                double a = 1.0 - (P / (2.0 * constA)) - (((constA - constB) * Q) / (2.0 * constA * constC));

                if( a <= 0 ) {
                    // minimum number of sites whose removal restores consistency. see comments
                    // in TamuraNei. 
                    final int adjustment = (int)(1 + (sumWeight * -a) / (1/(2.0*constA)  - 1));
                    sumTs -= adjustment;
                    if( sumTs < 0) {
                       break;
                    }
                    sumWeight -= adjustment;
                    continue;
                }

                double b = 1.0 - (Q / (2.0 * constC));
                if( b < 0 ) {
                    break;
                }

                final double distance = -(2.0 * constA * Math.log(a)) + (2.0 * (constA - constB - constC) * Math.log(b));

                return Math.min(distance, MAX_DISTANCE);
            }
            return MAX_DISTANCE;
        }


        static double[][] getDistances(Alignment alignment, ProgressListener progress) {
            Initializer.alignment = alignment;

            // ASK Alexei
            final int stateCount = alignment.getSequenceType().getCanonicalStateCount();

            if (stateCount != 4) {
                throw new IllegalArgumentException("HKYDistanceMatrix must have nucleotide patterns");
            }

            double[] freqs = getFrequenciesSafe(alignment);

            // Ask Alexei (mapping 0-a etc)
            double freqA = freqs[Nucleotides.A_STATE.getIndex()];
            double freqC = freqs[Nucleotides.C_STATE.getIndex()];
            double freqG = freqs[Nucleotides.G_STATE.getIndex()];
            double freqT = freqs[Nucleotides.T_STATE.getIndex()];

            constA = ((freqA * freqG) / freqR) + ((freqC * freqT) / freqY);
            constB = (freqA * freqG) + (freqC * freqT);
            constC = (freqR * freqY);

            int dimension = alignment.getTaxa().size();
            double[][] distances = new double[dimension][dimension];

            // TT: Whys is this a float? It's always a whole number! If the purpose
            // is to avoid integer division, we should rather cast to float or double
            // in the division below
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
