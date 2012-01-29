/*
 * ConstantPopulation.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * This class models coalescent intervals for a constant population
 * (parameter: N0=present-day population size). <BR>
 * If time units are set to Units.EXPECTED_SUBSTITUTIONS then
 * the N0 parameter will be interpreted as N0 * mu. <BR>
 * Also note that if you are dealing with a diploid population
 * N0 will be out by a factor of 2.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: ConstantPopulation.java 390 2006-07-20 14:33:51Z rambaut $
 *
 */
public class ConstantPopulation implements DemographicFunction
{
	//
	// Public stuff
	//

    /**
     * Construct demographic model with default settings
     */
    public ConstantPopulation() {
        // empty constructor
    }

	/**
     * Construct demographic model with given settings
	 */
	public ConstantPopulation(double N0) {
        this.N0 = N0;
    }

	/**
	 * returns initial population size.
	 */
	public double getN0() { return N0; }

	/**
	 * sets initial population size.
	 */
	public void setN0(double N0) { this.N0 = N0; }


	// Implementation of abstract methods

	public double getDemographic(double t) { return getN0(); }
	public double getIntensity(double t) { return t/getN0(); }
	public double getInverseIntensity(double x) { return getN0()*x; }

    public boolean hasIntegral() {
        return true;
    }

    /**
	 * Calculates the integral 1/N(x) dx between start and finish. The
	 * inherited function in DemographicFunction.Abstract calls a
	 * numerical integrater which is unecessary.
	 */
	public double getIntegral(double start, double finish) {
		return getIntensity(finish) - getIntensity(start);
	}

    public int getArgumentCount() {
        return 1;
    }

    public String getArgumentName(int n) {
        return "N0";
    }

    public double getArgument(int n) {
        return getN0();
    }

    public void setArgument(int n, double value) {
        setN0(value);
    }

    public double getLowerBound(int n) {
        return 0.0;
    }

    public double getUpperBound(int n) {
        return Double.POSITIVE_INFINITY;
    }
	//
	// private stuff
	//

	private double N0;
}
