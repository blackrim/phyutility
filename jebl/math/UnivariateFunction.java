// UnivariateFunction.java
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
 * interface for a function of one variable
 *
 * @author Korbinian Strimmer
 */
public interface UnivariateFunction
{
	/**
	 * compute function value
	 *
	 * @param argument function argument
	 * 
	 * @return function value
	 */
	double evaluate(double argument);
	
	/**
	 * get lower bound of argument
	 *
	 * @return lower bound
	 */
	double getLowerBound();
	
	/**
	 * get upper bound of argument
	 *
	 * @return upper bound
	 */
	double getUpperBound();
}
