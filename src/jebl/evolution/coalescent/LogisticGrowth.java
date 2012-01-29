/*
 * LogisticGrowth.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * This class models logistic growth.
 *
 * @version $Id: LogisticGrowth.java 390 2006-07-20 14:33:51Z rambaut $
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public class LogisticGrowth extends ExponentialGrowth {

    /**
     * Construct demographic model with default settings
     */
    public LogisticGrowth() {
        // empty constructor
    }

	/**
	 * Construct demographic model with given settings
	 */
	public LogisticGrowth(double N0, double r, double c) {

		super(N0, r);
        this.c = c;
    }

	public void setShape(double value) { c = value; }
	public double getShape() { return c; }

	/**
	 * An alternative parameterization of this model. This
	 * function sets the time at which there is a 0.5 proportion
	 * of N0.
	 */
	public void setTime50(double time50) {

		c = 1.0 / (Math.exp(getGrowthRate() * time50) - 2.0);

        // The general form for any k where t50 is the time at which Nt = N0/k:
        //		c = (k - 1.0) / (Math.exp(getGrowthRate() * time50) - k);
	}

	// Implementation of abstract methods

    /**
     * Gets the value of the demographic function N(t) at time t.
     * @param t the time
     * @return the value of the demographic function N(t) at time t.
     */
	public double getDemographic(double t) {

		double nZero = getN0();
		double r = getGrowthRate();
		double c = getShape();

//		return nZero * (1 + c) / (1 + (c * Math.exp(r*t)));
//		AER rearranging this to use exp(-rt) may help
// 		with some overflow situations...

		double common = Math.exp(-r*t);
		return (nZero * (1 + c) * common) / (c + common);
	}

	/**
	 * Returns value of demographic intensity function at time t
	 * (= integral 1/N(x) dx from 0 to t).
	 */
	public double getIntensity(double t) {
        throw new UnsupportedOperationException();
	}

	/**
	 * Returns value of demographic intensity function at time t
	 * (= integral 1/N(x) dx from 0 to t).
	 */
	public double getInverseIntensity(double x) {
        throw new UnsupportedOperationException();
	}

    public boolean hasIntegral() {
        return true;
    }

	public double getIntegral(double start, double finish) {

		double intervalLength = finish - start;

		double nZero = getN0();
		double r = getGrowthRate();
		double c = getShape();
		double expOfMinusRT = Math.exp(-r*start);
		double expOfMinusRG = Math.exp(-r*intervalLength);

		double term1 = nZero*(1.0+c);
		assert(term1 > 0.0);

		double term2 = c*(1.0 - expOfMinusRG);

		double term3 = (term1*expOfMinusRT) * r * expOfMinusRG;

        assert(term2 > 0.0 || term3 > 0.0);

		double term4;
		if (term3!=0.0 && term2==0.0) {
            term4=0.0;
        } else if (term3==0.0 && term2==0.0) {
		    throw new RuntimeException("term3 and term2 are both zeros. N0=" + getN0() + " growthRate=" +  getGrowthRate() + "c=" + c);
		} else {
            term4 = term2 / term3;
        }

		double term5 = intervalLength / term1;

		return term5 + term4;
	}

	//
	// private stuff
	//

	private double c;
}
