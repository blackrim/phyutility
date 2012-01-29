/*
 * Coalescent.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

import jebl.math.*;
import jebl.evolution.trees.RootedTree;

/**
 * A likelihood function for the coalescent. Takes a tree and a demographic model.
 *
 * Parts of this class were derived from C++ code provided by Oliver Pybus.
 *
 * @version $Id: Coalescent.java 390 2006-07-20 14:33:51Z rambaut $
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class Coalescent implements MultivariateFunction {

	// PUBLIC STUFF

	public Coalescent(RootedTree tree, DemographicFunction demographicFunction) {
		this(new Intervals(tree), demographicFunction);
	}

	public Coalescent(IntervalList intervals, DemographicFunction demographicFunction) {

		this.intervals = intervals;
		this.demographicFunction = demographicFunction;
	}


	/**
	 * Calculates the log likelihood of this set of coalescent intervals,
	 * given a demographic model.
	 */
	public double calculateLogLikelihood() {

		return calculateLogLikelihood(intervals, demographicFunction);
	}

	/**
	 * Calculates the log likelihood of this set of coalescent intervals,
	 * given a demographic model.
	 */
	public static final double calculateLogLikelihood(IntervalList intervals,
	                                                  DemographicFunction demographicFunction) {

		double logL = 0.0;

		double startTime = 0.0;

		for (int i = 0, n = intervals.getIntervalCount(); i < n; i++) {

			double duration = intervals.getInterval(i);
			double finishTime = startTime + duration;

			double intervalArea = demographicFunction.getIntegral(startTime, finishTime);
			int lineageCount = intervals.getLineageCount(i);

			if (intervals.getIntervalType(i) == IntervalList.IntervalType.COALESCENT) {

				logL += -Math.log(demographicFunction.getDemographic(finishTime)) -
									(Binomial.choose2(lineageCount)*intervalArea);

			} else { // SAMPLE or NOTHING

				logL += -(Binomial.choose2(lineageCount)*intervalArea);
			}

			startTime = finishTime;
		}

		return logL;
	}

	/**
	 * Calculates the log likelihood of this set of coalescent intervals,
	 * using an analytical integration over theta.
	 */
	public static final double calculateAnalyticalLogLikelihood(IntervalList intervals) {

		if (!intervals.isCoalescentOnly()) {
			throw new IllegalArgumentException("Can only calculate analytical likelihood for pure coalescent intervals");
		}

		double lambda = getLambda(intervals);
		int n = intervals.getSampleCount();

		double logL = 0.0;

		// assumes a 1/theta prior
		//logLikelihood = Math.log(1.0/Math.pow(lambda,n));

		// assumes a flat prior
		logL = Math.log(1.0/Math.pow(lambda,n-1));
		return logL;
	}

	/**
	 * Returns a factor lambda such that the likelihood can be expressed as
	 * 1/theta^(n-1) * exp(-lambda/theta). This allows theta to be integrated
	 * out analytically. :-)
	 */
	private static final double getLambda(IntervalList intervals) {
		double lambda = 0.0;
		for (int i= 0; i < intervals.getIntervalCount(); i++) {
			lambda += (intervals.getInterval(i) * intervals.getLineageCount(i));
		}
		lambda /= 2;

		return lambda;
	}

    // **************************************************************
    // MultivariateFunction IMPLEMENTATION
    // **************************************************************

	public double evaluate(double[] argument) {
		for (int i = 0; i < argument.length; i++) {
			demographicFunction.setArgument(i, argument[i]);
		}

		return calculateLogLikelihood();
	}

	public int getNumArguments() {
		return demographicFunction.getArgumentCount();
	}

	public double getLowerBound(int n) {
		return demographicFunction.getLowerBound(n);
    }

	public double getUpperBound(int n) {
		return demographicFunction.getUpperBound(n);
    }

	public OrthogonalHints getOrthogonalHints() {
		return null;
	}

	/** The demographic function. */
	private final DemographicFunction demographicFunction;

	/** The intervals. */
	private final IntervalList intervals;
}