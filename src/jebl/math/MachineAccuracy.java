// MachineAccuracy.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package jebl.math;


/**
 * determines machine accuracy
 *
 * @version $Id: MachineAccuracy.java 650 2007-03-12 20:09:10Z twobeers $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class MachineAccuracy
{
	//
	// Public stuff
	//

	/** machine accuracy constant */
	public static double EPSILON = 2.220446049250313E-16;
	
	public static double SQRT_EPSILON = 1.4901161193847656E-8;
	public static double SQRT_SQRT_EPSILON = 1.220703125E-4;

	/** compute EPSILON from scratch */
	public static double computeEpsilon()
	{
		double eps = 1.0;

		while( eps + 1.0 != 1.0 )
		{
			eps /= 2.0;
		}
		eps *= 2.0;
		
		return eps;
	}

    /*

     // Convenience methods proposed by Tobias:
    public static boolean strictlySmaller(double a, double b) {
        return (a < b) && !same(a,b);
    }

    public static boolean strictlyLarger(double a, double b) {
        return strictlySmaller(b,a);
    }

    public static boolean smallerOrEqual(double a, double b) {
        // same as  !strictlyLarger(a,b)
        return (a < b) || MachineAccuracy.same(a,b);
    }

    public static boolean largerOrEqual(double a, double b) {
        return !smallerOrEqual(b,a);
    } */

    /**
	 * @return true if the relative difference between the two parameters
	 * is no larger than SQRT_EPSILON.
     *
     * (TT: I think this means (b / (1+SQRT_EPSILON)) <= a <= b * (1+SQRT_EPSILON) )
	 */
	public static boolean same(double a, double b) {
        // Tobias: I changed the formula on 2006-12-14. The old version had two
        // problems:
        //  1.) same(0,0) was false (because of a division by zero)
        //  2.) same() was asymmetric: let a = 1.0, b = 1.0 - MachineAccuracy.SQRT_EPSILON.
        //      Then same(a,b) == false and same(b,a) == true with the old version of same().
        //return Math.abs((a/b)-1.0) <= SQRT_EPSILON;
        if ((a < 0) != (b < 0)) {
            return false;
        } else {
            a = Math.abs(a);
            b = Math.abs(b);
            return ((b / (1.0+SQRT_EPSILON)) <= a) && (a <= (1+SQRT_EPSILON) * b);
        }
    }
}
