// RateMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package jebl.evolution.substmodel;

import jebl.evolution.sequences.SequenceType;

/**
 * abstract base class for all rate matrices
 *
 * @version $Id: AbstractRateMatrix.java 185 2006-01-23 23:03:18Z rambaut $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
abstract public class AbstractRateMatrix implements RateMatrix
{

    //
    // Public stuff
    //

    // Constraints and conventions:
    // - first argument: row
    // - second argument: column
    // - transition: from row to column
    // - sum of every row = 0
    // - sum of frequencies = 1
    // - frequencies * rate matrix = 0 (stationarity)
    // - expected number of substitutions = 1 (Sum_i pi_i*R_ii = 0)

    /** dimension */
    private int dimension;

    /** stationary frequencies (sum = 1.0) */
    private double[] frequency;

    /**
     * rate matrix (transition: from 1st index to 2nd index)
     */
    private double[][] rate;

    /** data type */
    private SequenceType sequenceType;

    //
    // Private Stuff
    //

    private transient MatrixExponential matrixExp_;

    /* The following is set to true in parameterChange(), and reset to false
     * in setDistance()
     */
    private transient boolean rebuildModel_ = false;

    private double[] parameterStore_ = null;

    // Constructor
    protected AbstractRateMatrix(int dim) {
        dimension = dim;
        frequency = new double[dim];
        rate = new double[dim][dim];
        scheduleRebuild();
    }

    private void scheduleRebuild() {
        rebuildModel_ = true;
    }

    public int getDimension() {   return dimension;  }

    /**
     * @return stationary frequencies (sum = 1.0)
     */
    public double[] getEquilibriumFrequencies() {  return frequency;  }

    /**
        * @return stationary frequencie (sum = 1.0) for ith state
        */
    public double getEquilibriumFrequency(int i) {   return frequency[i];  }

    public SequenceType getSequenceType() {   return sequenceType;  }

    protected final void setSequenceType(SequenceType dt) { this.sequenceType = dt; }

    /**
     * @return rate matrix (transition: from 1st index to 2nd index)
     */
    public double[][] getRelativeRates() {  return rate;  }

    /**
     * @return the probability of going from one state to another
     * given the current distance
     * @param fromState The state from which we are starting
     * @param toState The resulting state
     */
    public double getTransitionProbability(int fromState, int toState) {
        return matrixExp_.getTransitionProbability(fromState,toState);
    }

    private void handleRebuild() {
        if(matrixExp_==null) {
            matrixExp_ = new MatrixExponential(this);
        }
        if(rebuildModel_) {
            rebuildRateMatrix(rate,parameterStore_);
            fromQToR();
        }
    }

    public final void rebuild() {}

    /** Sets the distance (such as time/branch length) used when calculating
     *       	the probabilities.
     */
    public final void setDistance(double distance) {
        handleRebuild();
        matrixExp_.setDistance(distance);
    }

    /** Sets the distance (such as time/branch length) used when calculating
     *  the probabilities. The resulting transition probabilities will be in reverse
     *  (that is in the matrix instead of [from][to] it's [to][from])
     */
    public final void setDistanceTranspose(double distance) {
        handleRebuild();
        matrixExp_.setDistanceTranspose(distance);
    }

    /** A utility method for speed, transfers trans prob information quickly
     *       	into store
     */
    public final void getTransitionProbabilities(double[][] probabilityStore) {
        matrixExp_.getTransitionProbabilities(probabilityStore);
    }

    public void scale(double scale) {
        normalize(scale);
        updateMatrixExp();
    }

    protected void setFrequencies(double[] f) {
        System.arraycopy(f, 0, frequency, 0, dimension);
        checkFrequencies();
        scheduleRebuild();
    }
    public double setParametersNoScale(double[] parameters) {
        rebuildRateMatrix(rate,parameters);
        double result = incompleteFromQToR();
        rebuildModel_ = false;
        return result;
    }

    /** Computes normalized rate matrix from Q matrix (general reversible model)
     * - Q_ii = 0
     * - Q_ij = Q_ji
     * - Q_ij is stored in R_ij (rate)
     * - only upper triangular is used
     * Also updates related MatrixExponential
     */
    private void fromQToR() {
        double q;
        for (int i = 0; i < dimension; i++)  {
            for (int j = i + 1; j < dimension; j++) {
                q = rate[i][j];
                rate[i][j] = q*frequency[j];
                rate[j][i] = q*frequency[i];
            }
        }
        makeValid();
        normalize();
        updateMatrixExp();
    }

    private double incompleteFromQToR() {
        double q;
        for (int i = 0; i < dimension; i++) {
            for (int j = i + 1; j < dimension; j++) {
                q = rate[i][j];
                rate[i][j] = q*frequency[j];
                rate[j][i] = q*frequency[i];
            }
        }
        return makeValid();
    }

    abstract protected void rebuildRateMatrix(double[][] rate, double[] parameters);

    protected void updateMatrixExp() {
        if(matrixExp_==null) {
            matrixExp_ = new MatrixExponential(this);
        } else {
            matrixExp_.setMatrix(this);
        }
    }
    //
    // Private stuff
    //

    /** Make it a valid rate matrix (make sum of rows = 0)
        * @return current rate scale
        */
    private double makeValid() {
        double total = 0;
        for (int i = 0; i < dimension; i++){
            double sum = 0.0;
            for (int j = 0; j < dimension; j++)
            {
                if (i != j)
                {
                    sum += rate[i][j];
                }
            }
            rate[i][i] = -sum;
            total+=frequency[i]*sum;
         }
         return total;
    }

    // Normalize rate matrix to one expected substitution per unit time
    private void normalize()
    {
        double subst = 0.0;

        for (int i = 0; i < dimension; i++)
        {
            subst += -rate[i][i]*frequency[i];
        }
        for (int i = 0; i < dimension; i++)
        {
            for (int j = 0; j < dimension; j++)
            {
                rate[i][j] = rate[i][j]/subst;
            }
        }
    }
     // Normalize rate matrix by a certain scale to acheive an overall scale (used with a complex site class model)
    private void normalize(double substitutionScale)  {
        for (int i = 0; i < dimension; i++)  {
            for (int j = 0; j < dimension; j++)  {
                rate[i][j] = rate[i][j]/substitutionScale;
            }
        }
    }

    /**
     * ensures that frequencies are not smaller than MINFREQ and
     * that two frequencies differ by at least 2*MINFDIFF.
     * This avoids potentiak problems later when eigenvalues
     * are computed.
     */
    private void checkFrequencies()
    {
        // required frequency difference
        double MINFDIFF = 1e-10;

        // lower limit on frequency
        double MINFREQ = 1e-10;

        int maxi = 0;
        double sum = 0.0;
        double maxfreq = 0.0;
        for (int i = 0; i < dimension; i++)
        {
            double freq = frequency[i];
            if (freq < MINFREQ) frequency[i] = MINFREQ;
            if (freq > maxfreq)
            {
                maxfreq = freq;
                maxi = i;
            }
            sum += frequency[i];
        }
        frequency[maxi] += 1.0 - sum;

        for (int i = 0; i < dimension - 1; i++)
        {
            for (int j = i+1; j < dimension; j++)
            {
                if (frequency[i] == frequency[j])
                {
                    frequency[i] += MINFDIFF;
                    frequency[j] -= MINFDIFF;
                }
            }
        }
    }

// ============================================================================
// ==== Protected Stuff ==========
    protected final double[] getFrequencies() {  return frequency; }
}
