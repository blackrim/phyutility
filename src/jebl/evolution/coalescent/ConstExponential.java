/*
 * ConstExponential.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * This class models exponential growth from an initial population size.
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 *
 * @version $Id: ConstExponential.java 390 2006-07-20 14:33:51Z rambaut $
 *
 */
public class ConstExponential extends ExponentialGrowth {

    /**
     * Construct demographic model with default settings
     */
    public ConstExponential() {
        // empty constructor
    }

	/**
	 * Construct demographic model with given settings
	 */
	public ConstExponential(double N0, double r, double N1) {

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

		//return nOne + ((nZero - nOne) * Math.exp(-r*t));

		double time = Math.log(N0/N1)/r;

        if (t < time) return N0 * Math.exp(-r*t);

		return N1;
	}

	public double getIntensity(double t) {
        double r = getGrowthRate();
        double time = Math.log(getN0()/getN1())/r;

        if (r == 0.0) return t/getN0();

        if (t < time) {
            return super.getIntensity(t);
        } else {
            return super.getIntensity(time) + (t-time)/getN1();
        }
  	}

	public double getInverseIntensity(double x) {
        /* AER - I think this is right but until someone checks it...
            double nZero = getN0();
            double nOne = getN1();
            double r = getGrowthRate();

            if (r == 0) {
                return nZero*x;
            } else if (alpha == 0) {
                return Math.log(1.0+nZero*x*r)/r;
            } else {
                return Math.log(-(nOne/nZero) + Math.exp(nOne*x*r))/r;
            }
        */
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

	// private stuff
	//

	private double N1 = 0.0;
}
