/*
 * Expansion.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * This class models exponential growth from an initial ancestral population size.
 * (Parameters: N0=present-day population size; N1=ancestral population size; r=growth rate).
 * This model is nested with the exponential-growth population size model (N1=0).
 *
 * @version $Id: Expansion.java 390 2006-07-20 14:33:51Z rambaut $
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public class Expansion extends ExponentialGrowth {

    /**
     * Construct demographic model with default settings
     */
    public Expansion() {
        // empty constructor
    }

	/**
     * Construct demographic model with given settings
	 */
	public Expansion(double N0, double r, double N1) {

		super(N0, r);
        this.N1 = N1;
    }

	public double getN1() { return N1; }
	public void setN1(double N1) { this.N1 = N1; }

	public void setProportion(double p) { this.N1 = getN0() * p; }

	// Implementation of abstract methods

	public double getDemographic(double t) {

		double N0 = getN0();
		double N1 = getN1();
		double r = getGrowthRate();

		assert (N1 > N0);

		return N1 + ((N0 - N1) * Math.exp(-r*t));
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
        return 3;
    }

    @Override
    public String getArgumentName(int n) {
        switch (n) {
            case 0: return "N0";
            case 1: return "r";
            case 2: return "N1";
        }
        throw new IllegalArgumentException("Argument " + n + " does not exist");
    }

    @Override
    public double getArgument(int n) {
        switch (n) {
            case 0: return getN0();
            case 1: return getGrowthRate();
            case 2: return getN1();
        }
        throw new IllegalArgumentException("Argument " + n + " does not exist");
    }

    @Override
    public void setArgument(int n, double value) {
        switch (n) {
            case 0: setN0(value); break;
            case 1: setGrowthRate(value); break;
            case 2: setN1(value); break;
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
