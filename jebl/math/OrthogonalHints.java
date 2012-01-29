// OrthogonalHints.java
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
 * Provides a means for giving an Orthogonal base optimiser (IE, OrthognalMinimum)
 * hints about the function that may alow it to optimise better.
 *
 * @version $Id: OrthogonalHints.java 258 2006-03-16 23:45:00Z twobeers $
 *
 * @author Matthew Goode
 */

public interface OrthogonalHints {
	/**
	 * If there is a "best" ordering to use it can be specified here,
	 * if not should return null
	 * @param defaultOrdering The ordering suggested by the optimiser, may be null!
	 * @return null, or default ordering if no known best ordering
	 */
	public OrderEnumerator getSuggestedOrdering(OrderEnumerator defaultOrdering);
	/**
	 * A boundary is a value of a parameter for which values lower than the boundary and values
	 * higher than the boundary are better treated as two separate functions (IE, they
	 * are only piecewise connected), and minimisation should be performed over both ranges
	 * individually (and then the true minimum taken as the minimuma of the ranges)
	 * @return the number of boundary locations stored in storage, or -1 if not enough
	 * room, or 0 if there are no boundaries (other than the normal parameter range)
	 */
	public int getInternalParameterBoundaries(int parameter, double[] storage);

//=====================================================================================================
//================================= Utilities, and hidden classes =====================================
//=====================================================================================================

	public static class Utils {
		/**
		 * @return a new OrthogonalHints object base on toAdjust that works with parameters from adjustmentFactor + what toAdjust worked with
		 * That is if the value x is the parameter will be passed toAdjust as x-adjustmentFactor, and
		 * the suggested OrderEnumerator adjusts input x by adding adjustment factor before returning to
		 * the sub toAdjust Enumerator (if you know what I mean)
		 */
		public final static OrthogonalHints getAdjusted(OrthogonalHints toAdjust, int adjustmentFactor) {
			return new Adjusted(toAdjust,adjustmentFactor);
		}
		/**
		 * @return a new OrthogonalHints object that combines two sub OrthogonalHints objects so that
		 * all parameter information between 0 upto (but not including) numberOfFirstParameters is
		 * passed to first, and everything else is passed to second
		 * note: automatically adjusts second so assumes both first and second handle parameters in
		 * range 0..whatever (do not do preadjusment on second!)
		 */
		public final static OrthogonalHints getCombined(OrthogonalHints first, int numberOfFirstParameters, OrthogonalHints second, int numberOfSecondParameters) {
			return new Combined(first,numberOfFirstParameters,second,numberOfSecondParameters);
		}

		public final static double[] getInternalParameterBoundaries(OrthogonalHints base, int parameter) {
			double[] store = new double[100];

			int numberReturned = base.getInternalParameterBoundaries(parameter,store);
			while(numberReturned<0) {
				store = new double[store.length+10];
				numberReturned = base.getInternalParameterBoundaries(parameter,store);
			}
			double[] result = new double[numberReturned];
			System.arraycopy(store,0,result,0,numberReturned);
			return result;
		}

		/**
		 * @return an OrthogonalHints object that doesn't provide any hints
		 */
		public final static OrthogonalHints getNull() {
			return Null.INSTANCE;
		}

	// =======================================================================
		/**
		 * Implements a means for adjusting an orthogonal hints (that is introduce a simple
		 * mapping between given parameter indexes and used parameter indexes)
		 */
		private final static class Adjusted implements OrthogonalHints {
			OrthogonalHints toAdjust_;
			int adjustmentFactor_;
			public Adjusted(OrthogonalHints toAdjust, int adjustmentFactor) {
				this.toAdjust_ = toAdjust;
				this.adjustmentFactor_ = adjustmentFactor;
			}
			public OrderEnumerator getSuggestedOrdering(OrderEnumerator defaultOrdering) {
				OrderEnumerator sub = toAdjust_.getSuggestedOrdering(defaultOrdering);
				if(sub==null||sub==defaultOrdering) {
					return defaultOrdering;
				}
				return OrderEnumerator.Utils.getAdjusted(sub,adjustmentFactor_);
			}
			public int getInternalParameterBoundaries(int parameter, double[] storage) {
				return toAdjust_.getInternalParameterBoundaries(parameter-adjustmentFactor_,storage);
			}
		} //End of Adjusted

	// =======================================================================
		/**
		 * An OrthogonalHints object that provides no hints!
		 */
		private final static class Null implements OrthogonalHints {
			public static final Null INSTANCE = new Null();
			public Null() {	}
			public OrderEnumerator getSuggestedOrdering(OrderEnumerator defaultOrdering) {
				return defaultOrdering;
			}
			public int getInternalParameterBoundaries(int parameter, double[] storage) {
				return 0;
			}
		} //End of Null

	// =======================================================================
		/**
		 * Implements a means for combining two OrthogonalHints objects
		 */
		private final static class Combined implements OrthogonalHints {
			OrthogonalHints hintsOne_,hintsTwo_;
			int hintOneParameterCount_, hintTwoParameterCount_ ;
			/**
			 * @param hintOneParameterCount The number of parameters handled by hintsOne
			 * @param hintTwoParameterCount The number of parameters handled by hintsTwo
			 *
			 */
			public Combined(OrthogonalHints hintsOne, int hintOneParameterCount, OrthogonalHints hintsTwo, int hintTwoParameterCount) {
				this.hintsOne_ = hintsOne;
				this.hintOneParameterCount_ = hintOneParameterCount;
				this.hintTwoParameterCount_ = hintTwoParameterCount;

				this.hintsTwo_ = hintsTwo;
			}
			/**
			 * if no suggested ordering from either sub hints returns null, if only
			 * one hint has suggested ordering, creates an ordering where those parameters belonging to
			 * the respecitive hint are given by the given ordering and the remaining ordering information
			 * is shuffled.
			 */
			public OrderEnumerator getSuggestedOrdering(OrderEnumerator defaultOrdering) {
				OrderEnumerator oe1 = hintsOne_.getSuggestedOrdering(null);
				OrderEnumerator oe2 = hintsTwo_.getSuggestedOrdering(null);
				if(oe1==null&&oe2==null) {
					return defaultOrdering;
				}
				if(oe1==null&&oe2!=null) {
					if(defaultOrdering!=null) {
						return OrderEnumerator.Utils.getAlternating(
								OrderEnumerator.Utils.getRestricted(defaultOrdering,0,hintOneParameterCount_),
								OrderEnumerator.Utils.getAdjusted(oe2,hintOneParameterCount_)
							);

					}
					return OrderEnumerator.Utils.getAlternating(
						OrderEnumerator.Utils.getShuffled(hintOneParameterCount_),
						OrderEnumerator.Utils.getAdjusted(oe2,hintOneParameterCount_)
						);
				}
				if(oe2==null) {
					if(defaultOrdering!=null) {
						return OrderEnumerator.Utils.getAlternating(
							oe1,
							OrderEnumerator.Utils.getRestricted(
								defaultOrdering,
								hintOneParameterCount_,hintOneParameterCount_+hintTwoParameterCount_
							)
						);
					}
				 return OrderEnumerator.Utils.getAlternating(
						oe1,
						OrderEnumerator.Utils.getAdjusted(
							OrderEnumerator.Utils.getShuffled(hintTwoParameterCount_),
							hintOneParameterCount_
						)
					);
				}
				return OrderEnumerator.Utils.getAlternating(oe1,OrderEnumerator.Utils.getAdjusted(oe2,hintOneParameterCount_));
			}
			/**

			 */
			public int getInternalParameterBoundaries(int parameter, double[] storage) {
				if(parameter<hintOneParameterCount_) {
					return hintsOne_.getInternalParameterBoundaries(parameter,storage);
				}
				return hintsTwo_.getInternalParameterBoundaries(parameter-hintOneParameterCount_,storage);
			}
		} //End of Combined
	} //End of Utils

}