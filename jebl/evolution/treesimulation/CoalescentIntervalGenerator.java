/*
 * CoalescentIntervalGenerator.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.treesimulation;

import jebl.evolution.coalescent.DemographicFunction;

/**
 * This is a class that draws coalescent intervals under the given demographic function. If
 * the demographic function class has an analytical function for the integral of 1/N(t) then
 * this is used otherwise a numerical integrator is used.
 *
 * To generate a tree using this class, see the TreeSimulator class in jebl.evolution.trees.
 *
 * Much of this class was derived from C++ code provided by Oliver Pybus.
 *
 * @author Andrew Rambaut
 * @author Oliver Pybus
 * @version $Id: CoalescentIntervalGenerator.java 563 2006-12-07 17:43:10Z rambaut $
 */
public class CoalescentIntervalGenerator implements IntervalGenerator {

    public CoalescentIntervalGenerator(DemographicFunction demographicFunction) {
        this.demographicFunction = demographicFunction;
    }

    protected DemographicFunction demographicFunction;

    private static final double LARGE_POSITIVE_NUMBER = 1.0e50;
    private static final double LARGE_NEGATIVE_NUMBER = -1.0e50;
    private static final double INTEGRATION_PRECISION = 1.0e-5;
    private static final double INTEGRATION_MAX_ITERATIONS = 50;

    public double getInterval(double criticalValue, int lineageCount, double currentHeight) {

        assert lineageCount >= 2;
        assert criticalValue > 0.0 && criticalValue < 1.0;

        // The simulation equation cannot be rearranged for g, and is therefore solved
        // numerically. The integration method is determined by 'numericalIntegration',
        // the correct method is selected within SolveForIntervalSize();
        double c = (-Math.log(criticalValue)) / ((0.5 * lineageCount * (lineageCount - 1)));
        return solveForIntervalSize(c, currentHeight);
    }

    /**
     * The integral of 1/N(x) between ti and gi always increases as gi increases (it never
     * decreases or stays constant). This is because 1/N(x) is always greater than zero. We use
     * this result to solve for gi. Let c=-ln(u)/(i choose 2). Given a value of gi, if the
     * integral>c, then gi is greater than the solution. If the integral<c, then gi is smaller
     * than the solution. This function finds values which bracket the solution, passes them
     * to FindSolution(), then returns the solved interval size.
     *
     * @param inC
     * @param inT time of last coalescence
     * @return the solution
     */
    private double solveForIntervalSize(double inC, double inT) {
        assert(inT >= 0);
        assert(inC >= 0);

        double constant = inC; // constant must be equal to -ln(U)/(i choose 2)
        double lowBracket = 0.0;
        double highBracket = 0.0;
        double factor=1.6;

        for (double gEst=1.0; gEst < LARGE_POSITIVE_NUMBER; gEst=gEst*factor) {

            if (getIntegral(gEst, inT) > constant) {	// solution must be smaller than gEst
                highBracket = gEst;

                if (gEst == 1.0) {
                    // solution is between 0 and 1
                    lowBracket = 0.0;
                    return findSolution(constant, lowBracket, highBracket, inT);
                } else {
                    // solution is between gEst/FACTOR and gEst
                    lowBracket = (gEst/factor);
                    return findSolution(constant, lowBracket, highBracket, inT);
                }
            }
        }

        throw new RuntimeException("Unable to bracket solution in solveForIntervalSize");
    }

    /** This function returns the solved interval size. inLB and inHB must bracket the solution.
     * The function is a straightforward bisection search which is continued until the
     * desired accuracy is reached.
     */
    private double findSolution(double inConst, double lowB, double highB, double t) {

        assert(t >= 0.0);

        double solutionAccuracy = 1.0E-5;
        double halfway;

        do {
            halfway = ((highB - lowB) / 2.0) + lowB;
            if (getIntegral(halfway, t) > inConst) {
                // solution must be smaller than halfway
                highB = halfway;
            } else {
                // solution must be larger than halfway
                lowB = halfway;
            }

            assert(highB >= lowB);

        } while ((highB - lowB) > solutionAccuracy);

        return lowB;
    }

    /**
     * Returns the integral of 1/N(x) between t and g+t, calling either the getAnalyticalIntegral or
     * getNumericalIntegral function as appropriate.
     */
    private double getIntegral(double g, double t) {

        if (g==0.0) {
            // integral value equals 0 if g=0
            return 0.0;
        }

        if (demographicFunction.hasIntegral()) {
            // Calculate integral analytically
            return demographicFunction.getIntegral(t, t + g);
        } else {
            // Calculate integral numerically
            return getNumericalIntegral(t, t + g);
        }

    }

    /**
     * Evaluates the definite integral of 1/N(x) between inLowBound and inHighBound.
     */
    private double getNumericalIntegral(double inLowBound, double inHighBound)
    {
        double lastST = LARGE_NEGATIVE_NUMBER;
        double lastS = LARGE_NEGATIVE_NUMBER;

        assert(inHighBound > inLowBound);

        for (int j = 1; j <= INTEGRATION_MAX_ITERATIONS; j++) {
            // iterate doTrapezoid() until answer obtained

            double st = doTrapezoid(j, inLowBound, inHighBound, lastST);
            double s = (4.0 * st - lastST) / 3.0;

            // If answer is within desired accuracy then return
            if (Math.abs(s - lastS) < INTEGRATION_PRECISION * Math.abs(lastS)) {
                return s;
            }
            lastS = s;
            lastST = st;
        }

        throw new RuntimeException("Too many iterations in getNumericalIntegral");
    }

    /**
     * Performs the trapezoid rule.
     */
    private double doTrapezoid(int n, double low, double high, double lastS) {

        double s;

        if (n == 1) {
            // On the first iteration s is reset
            double demoLow = demographicFunction.getDemographic(low); // Value of N(x) obtained here
            assert(demoLow > 0.0);

            double demoHigh = demographicFunction.getDemographic(high);
            assert(demoHigh > 0.0);

            s = 0.5 * (high - low) * ( (1.0 / demoLow) + (1.0 / demoHigh) );
        } else {
            int it=1;
            for (int j = 1; j < n - 1; j++) {
                it *= 2;
            }

            double tnm = it;	// number of points
            double del = (high - low) / tnm;	// width of spacing between points

            double x = low + 0.5 * del;

            double sum = 0.0;
            for (int j = 1; j <= it; j++) {
                double demoX = demographicFunction.getDemographic(x); // Value of N(x) obtained here
                assert(demoX > 0.0);

                sum += (1.0 / demoX);
                x += del;
            }
            s =  0.5 * (lastS + (high - low) * sum / tnm);	// New s uses previous s value
        }

        return s;
    }
}
