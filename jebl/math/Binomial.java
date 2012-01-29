/*
 * Binomial.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.math;

/**
 * Binomial coefficients
 *
 * @version $Id: Binomial.java 305 2006-04-26 00:22:30Z rambaut $
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
public class Binomial
{
	//
	// Public stuff
	//

	/**
	 * Binomial coefficient n choose k
	 */
	public static double choose(double n, double k)
	{
		n = Math.floor(n + 0.5);
		k = Math.floor(k + 0.5);

		double lchoose = GammaFunction.lnGamma(n + 1.0) -
		GammaFunction.lnGamma(k + 1.0) - GammaFunction.lnGamma(n - k + 1.0);

		return Math.floor(Math.exp(lchoose) + 0.5);
	}

	/**
	 * get n choose 2
	 */
	public static double choose2(int n)
	{
		// not sure how much overhead there is with try-catch blocks
		// i.e. would an if statement be better?

		try {
			return choose2LUT[n];

		} catch (ArrayIndexOutOfBoundsException e) {

			while (maxN < n) {
				maxN += 1000;
			}

			initialize();
			return choose2LUT[n];
		}
	}

	private static void initialize() {
		choose2LUT = new double[maxN+1];
		choose2LUT[0] = 0;
		choose2LUT[1] = 0;
		choose2LUT[2] = 1;
		for (int i = 3; i <= maxN; i++) {
			choose2LUT[i] = ((double) (i*(i-1))) * 0.5;
		}
	}

	private static int maxN = 5000;
	private static double[] choose2LUT;

	static {
		initialize();
	}
}
