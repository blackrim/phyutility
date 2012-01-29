package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.State;
import jebl.util.ProgressListener;

/**

 * @author Joseph Heled
 * @version $Id: F84DistanceMatrix.java 658 2007-03-20 03:27:20Z twobeers $
 *
 * See the detailed comment in {@link HKYDistanceMatrix} on the model and the formula used for estimating the distance.
 */


public class F84DistanceMatrix extends BasicDistanceMatrix {

    public F84DistanceMatrix(Alignment alignment, ProgressListener progress) {
        super(alignment.getTaxa(), Initialaizer.getDistances(alignment, progress));
    }

    public F84DistanceMatrix(Alignment alignment) {
        super(alignment.getTaxa(), Initialaizer.getDistances(alignment, null));
    }

    static class Initialaizer {

    //
    // Private stuff
    //
    private static final double MAX_DISTANCE = 1000.0;
    private static Alignment alignment;

	/**
	 * Calculate a pairwise distance
	 */
	static private double calculatePairwiseDistance(int taxon1, int taxon2) {

        double[] total = new double [4];
        double[] transversions = new double [4];

        for( Pattern pattern : alignment.getPatterns() ) {
            State state1 = pattern.getState(taxon1);
            State state2 = pattern.getState(taxon2);

            double weight = pattern.getWeight();
            if (!state1.isAmbiguous() && !state2.isAmbiguous() ) {
                total[state1.getIndex()] += weight;

                if( Nucleotides.isTransversion(state1, state2) ) {
                    transversions[state1.getIndex()] += weight;
                }
            }
        }

        double totalTransversions = 0.0;
        for(int i = 0; i < 4; ++i) {
            if( total[i] > 0 ) {
                totalTransversions += transversions[i]/total[i];
            }
        }
        double expDist = 1.0 - (totalTransversions / 2.0);
        return expDist > 0 ? -Math.log( expDist) : MAX_DISTANCE;
    }


        synchronized static double[][] getDistances(Alignment alignment, ProgressListener progress) {
            Initialaizer.alignment = alignment;

            final int stateCount = alignment.getSequenceType().getCanonicalStateCount();

            if (stateCount != 4) {
                throw new IllegalArgumentException("F84DistanceMatrix must have nucleotide patterns");
            }

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
}