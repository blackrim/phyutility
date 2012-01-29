package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.State;
import jebl.util.ProgressListener;

/**
 * Compute jukes-cantor corrected distance matrix for a set of aligned sequences.
 * Adapted from BEAST code by joseph.
 *
 * @author Andrew Rambaut
 * @author Korbinian Strimmer
 * @author Joseph Heled
 *
 * @version $Id: JukesCantorDistanceMatrix.java 715 2007-05-24 23:13:04Z matthew_cheung $
 */

public class JukesCantorDistanceMatrix extends BasicDistanceMatrix {

    public JukesCantorDistanceMatrix(Alignment alignment, ProgressListener progress) {
        super(alignment.getTaxa(), getDistances(alignment, progress));
    }

    // Helpers during construction
    private static double maxTheoreticalSubsRate;
    private static Alignment alignment;
    private static final double MAX_DISTANCE = 1000.0;

    /**
     * Calculate number of substitution between sequences as a ratio.
     */
    static private double anySubstitutionRatio(int taxon1, int taxon2) {
        double distance;
        double sumDistance = 0.0;
        double sumWeight = 0.0;
        boolean noGapsPairFound = false;

        // If both sequences are of zero length then the substitution ratio is zero because they are identical
        if(alignment.getPatterns().size() == 0)
            return 0.0;

        for( Pattern pattern : alignment.getPatterns() ) {
            State state1 = pattern.getState(taxon1);
            State state2 = pattern.getState(taxon2);

            final double weight = pattern.getWeight();

            if(!state1.isGap() && !state2.isGap())
                noGapsPairFound = true;

            if (!state1.isAmbiguous() && !state2.isAmbiguous()) {
                if(state1 != state2)
                    sumDistance += weight;
            }
            sumWeight += weight;
        }

        if(!noGapsPairFound)
            throw new CannotBuildDistanceMatrixException("It is not possible to compute the Jukes-Cantor genetic distance " +
                    "for these sequences because at least one pair of sequences do not overlap in the alignment.");

        distance = sumDistance / sumWeight;

        return distance;
    }


    /**
     * Calculate a pairwise distance between the i'th and j'th taxons/sequences
     */
    static private double calculatePairwiseDistance(int taxon1, int taxon2) {
        double obsDist = anySubstitutionRatio(taxon1, taxon2);

        if (obsDist == 0.0) return 0.0;

        // protect against log(negative number)
        if (obsDist >= maxTheoreticalSubsRate) {
            return MAX_DISTANCE;
        }

        double expDist = -maxTheoreticalSubsRate * Math.log(1.0 - ((1/maxTheoreticalSubsRate) * obsDist));

        return Math.min(expDist, MAX_DISTANCE);
    }

    synchronized static double[][] getDistances(Alignment alignment, ProgressListener progress) {
        JukesCantorDistanceMatrix.alignment = alignment;

        // ASK Alexei
        int stateCount = alignment.getSequenceType().getCanonicalStateCount();

        maxTheoreticalSubsRate = ((double)stateCount - 1) / stateCount;

        int dimension = alignment.getTaxa().size();
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