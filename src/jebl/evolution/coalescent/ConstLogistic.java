// ConstLogistic.java
//
// (c) 2002-2004 BEAST Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package jebl.evolution.coalescent;

/**
 * This class models logistic growth from an initial population size.
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 *
 * @version $Id: ConstLogistic.java 390 2006-07-20 14:33:51Z rambaut $
 *
 */
public class ConstLogistic extends LogisticGrowth {

    /**
     * Construct demographic model with default settings
     */
    public ConstLogistic() {
        // empty constructor
    }

	/**
     * Construct demographic model with given settings
	 */
	public ConstLogistic(double N0, double r, double c, double N1) {

		super(N0, r, c);
        this.N1 = N1;
    }

	public double getN1() { return N1; }
	public void setN1(double N1) { this.N1 = N1; }

	// Implementation of abstract methods

	public double getDemographic(double t) {

		double nZero = getN0();
		double nOne = getN1();
		double r = getGrowthRate();
		double c = getShape();

		double common = Math.exp(-r*t);
		return nOne + ((nZero - nOne) * (1 + c) * common) / (c + common);
	}

	public double getIntensity(double t) {
        throw new UnsupportedOperationException();
	}

	public double getInverseIntensity(double x) {
        throw new UnsupportedOperationException();
	}

    public boolean hasIntegral() {
        return false;
    }

	public double getIntegral(double start, double finish) {
        throw new UnsupportedOperationException();
	}

    @Override
    public int getArgumentCount() {
        return 4;
    }

    @Override
    public String getArgumentName(int n) {
        switch (n) {
            case 0: return "N0";
            case 1: return "r";
            case 2: return "c";
            case 3: return "N1";
        }
        throw new IllegalArgumentException("Argument " + n + " does not exist");
    }

    @Override
    public double getArgument(int n) {
        switch (n) {
            case 0: return getN0();
            case 1: return getGrowthRate();
            case 2: return getShape();
            case 3: return getN1();
        }
        throw new IllegalArgumentException("Argument " + n + " does not exist");
    }

    @Override
    public void setArgument(int n, double value) {
        switch (n) {
            case 0: setN0(value); break;
            case 1: setGrowthRate(value); break;
            case 2: setShape(value); break;
            case 3: setN1(value); break;
            default: throw new IllegalArgumentException("Argument " + n + " does not exist");

        }
    }

    @Override
    public double getLowerBound(int n) {
        return 0.0;
    }

    @Override
    public double getUpperBound(int n) {
        return Double.POSITIVE_INFINITY;
    }

	//
	// private stuff
	//

	private double N1 = 0.0;
}
