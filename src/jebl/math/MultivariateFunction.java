// MultivariateFunction.java
//
// (c) 2006-     JEBL development team
//
// based on LGPL code from the Phylogenetic Analysis Library (PAL),
// http://www.cebl.auckland.ac.nz/pal-project/
// which is (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package jebl.math;


/**
 * interface for a function of several variables
 *
 * @author Korbinian Strimmer
 */
public interface MultivariateFunction
{
	/**
	 * compute function value
	 *
	 * @param argument  function argument (vector)
	 *
	 * @return function value
	 */
	double evaluate(double[] argument);


	/**
	 * get number of arguments
	 *
	 * @return number of arguments
	 */
	 int getNumArguments();

	/**
	 * get lower bound of argument n
	 *
	 * @param n argument number
	 *
	 * @return lower bound
	 */
	double getLowerBound(int n);

	/**
	 * get upper bound of argument n
	 *
	 * @param n argument number
	 *
	 * @return upper bound
	 */
	double getUpperBound(int n);

	/**
	 * @return an Orthogonal Hints object that can be used by Orthogonal based optimisers
	 * to get information about the function
	 * @return if no such information just return null!
	 */
	OrthogonalHints getOrthogonalHints();


}
