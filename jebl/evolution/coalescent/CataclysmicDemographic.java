/*
 * CataclysmicDemographic.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * This class models an exponentially growing (or shrinking) population
 * (Parameters: N0=present-day population size; r=growth rate).
 * This model is nested with the constant-population size model (r=0).
 *
 * @version $Id: CataclysmicDemographic.java 586 2006-12-15 15:49:15Z twobeers $
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public class CataclysmicDemographic extends ExponentialGrowth {

	/**
	 * Construct demographic model with default settings
	 */
	public CataclysmicDemographic() {
	    // empty constructor
	}

	/**
	 * Construct demographic model with given settings
     * @param N0 present-day population size
     * @param r growth rate
	 */
	public CataclysmicDemographic(double N0, double r, double d, double t) {

		super(N0, r);
        this.d = d;
        this.catTime = t;
    }

	/**
	 * returns the positive-valued decline rate
	 */
	public final double getDeclineRate() { return d; }

	/**
	 * sets the decline rate.
	 */
	public void setDeclineRate(double d) {
		if (d <= 0) throw new IllegalArgumentException();
		this.d = d;
	}

	public final double getCataclysmTime() { return catTime; }

	public final void setCataclysmTime(double t) {
		if (t <= 0) throw new IllegalArgumentException();
		catTime = t;
	}

	/**
	 * An alternative parameterization of this model. This
	 * function sets the decline rate using N0 & t which must
	 * already have been set.
	 */
	public final void setSpikeFactor(double f) {
		setDeclineRate( Math.log(f) / catTime );
	}

	// Implementation of abstract methods

	public double getDemographic(double t) {

		double d = getDeclineRate();

		if (t < catTime) {
			return getN0() * Math.exp(t * d);
		} else {
			double spikeHeight = getN0() * Math.exp(catTime * d);
			//System.out.println("Spike height = " + spikeHeight);
			t -= catTime;

			double r = getGrowthRate();
			if (r == 0) {
				return spikeHeight;
			} else {
				return spikeHeight * Math.exp(-t * r);
			}
		}
	}

	public double getIntensity(double t) {

		double d = getDeclineRate();
		double r = getGrowthRate();
		if (t < catTime) {
			return (Math.exp(t*-d)-1.0)/getN0()/-d;
		} else {

			double intensityUpToSpike = (Math.exp(catTime*-d)-1.0)/getN0()/-d;

			double spikeHeight = getN0() * Math.exp(catTime * d);
			t -= catTime;
			//System.out.println("Spike height = " + spikeHeight);

			if (r == 0) {
				return t/spikeHeight + intensityUpToSpike;
			} else {
				return (Math.exp(t*r)-1.0)/spikeHeight/r + intensityUpToSpike;
			}
		}
	}

	public double getInverseIntensity(double x) {

		throw new UnsupportedOperationException();
	}

    public boolean hasIntegral() {
        return false;
    }


	//
	// private stuff
	//

	private double d;
	private double catTime;
}
