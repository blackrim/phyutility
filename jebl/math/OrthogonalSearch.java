// OrthogonalSearch.java
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
 * minimization of a real-valued function of
 * several variables without using derivatives, using the simple
 * strategy of optimizing variables one by one.
 *
 * @author Korbinian Strimmer
 * @author Matthew Goode
 */

public class OrthogonalSearch extends MultivariateMinimum
{
	//
	// Public stuff
	//


	//
	// Private stuff
	//
	private OrderEnumerator.OEFactory orthogonalOrderingFactory_;

	/** Use the current value of dimension in univariate minimisation, or ignore (original method) */
	private boolean useCurrentInUnivariateMinimisation_ = false;
	/** Sometimes the minimum gained through the single variate minimisation is
	 *  worse than the minimum currently found (in that it has found another minimum
	 *  which is not the original, and is not as minimumal).
	 *  This can cause convergence problems, if this is true than the original minima
	 *  will be kept if it is more minimal than the new minimuma. This ensures convergence.
	 *  In the future a possible strategy might be SimulatedAnealing with regard to accepting,
	 *  or rejecting new minima.
	 */
	private boolean ignoreNonMinimalUnivariateMinimisations_ = true;

	/**
	 * If true, print out debug info...
	 */
	private boolean debug_ = false;

	/**
	 * If true calls MinimiserMonitor methods after each orthogonal update, otherwise after each round
	 */
	private boolean frequentMonitoring_ = true;

	/**
	 * Initialization
	 */
	public OrthogonalSearch() {
		//this(OrderUtils.getBiasAlternatingFactory( OrderUtils.getOrderedFactory(), OrderUtils.getZeroFactory()));
		this(OrderEnumerator.Utils.getOrderedFactory());
	}
	/**
	 * Initialization
	 * @param shuffle If true uses shuffling, else uses ascending order, when choosing next parameter to optimse
	 * (true means equivalent to old StochasticOSearch)
	 */
	public OrthogonalSearch(boolean shuffle) {
		//this(OrderUtils.getBiasAlternatingFactory( OrderUtils.getOrderedFactory(), OrderUtils.getZeroFactory()));
		this(shuffle? OrderEnumerator.Utils.getShuffledFactory() : OrderEnumerator.Utils.getOrderedFactory());
	}
	/**
	 * Initialization
	 */
	public OrthogonalSearch(OrderEnumerator.OEFactory orderingFactory) {
		this.orthogonalOrderingFactory_ = orderingFactory;
	}

	/**
	 *
	 */
	public void setUseCurrentInUnivariateMinimisation(boolean value) {
		this.useCurrentInUnivariateMinimisation_ = value;
	}
	/**
	 * Should we ignore new minisations that are not as minimal as the current one?
	 */
	public void setIgnoreNonMinimalUnivariateMinimisations(boolean value) {
		this.ignoreNonMinimalUnivariateMinimisations_ = value;
	}

	// implementation of abstract method

	public void optimize(MultivariateFunction f, double[] xvec, double tolfx, double tolx) {
		optimize(f,xvec,tolfx,tolx,null);
	}
	public void optimize(MultivariateFunction f, double[] xvec, double tolfx, double tolx, MinimiserMonitor monitor) {
		int numArgs = f.getNumArguments();

		numFun = 1;
		double fx = f.evaluate(xvec);

		stopCondition(fx, xvec, tolfx, tolx, true);

		RoundOptimiser od = generateOrthogonalRoundOptimiser(f);
		UnivariateMinimum um = generateUnivariateMinimum();
		double lastFX;
		while (true) {
			lastFX = fx;
            fx = od.doRound(xvec,um,tolx,fx, (frequentMonitoring_ ? monitor : null));
			if(monitor!=null) {
				monitor.newMinimum(fx,xvec,f);
                if (maxFun > 0) {
                    monitor.updateProgress((double) numFun / maxFun);
                }
            }
			debug("Round fx:"+fx);

			if (stopCondition(fx, xvec, tolfx, tolx, false) ||
				(maxFun > 0 && numFun > maxFun) ||
				 numArgs == 1)	{
				break;
			}
		}
	}

	//============ Static Methods ====================

	/**
	 * Generate a MultivariateMinimum.Factory for an OrthogonalSearch
	 * @param shuffle if true shuffles order for each round (see OrthogonalSearch constructors)
	 */
	public static final Factory generateFactory(boolean shuffle) {	return new SearchFactory(shuffle);	}

	//============ For sub classes ===================

	protected UnivariateMinimum generateUnivariateMinimum() {
		return new UnivariateMinimum();
	}
	protected boolean isFrequentMonitoring() {
		return frequentMonitoring_;
	}
	protected RoundOptimiser generateOrthogonalRoundOptimiser(MultivariateFunction mf) {
		OrthogonalHints hints = mf.getOrthogonalHints();
		if(hints!=null) {
			return new OrthogonalHintsDirection(mf,hints,orthogonalOrderingFactory_);
		}
		return new OrthogonalDirection(mf,orthogonalOrderingFactory_);
	}

	protected interface RoundOptimiser {
		/**
		 * @param monitor - may be null;
		 */
		public double doRound(double[] xvec, UnivariateMinimum um, double tolx,double fx, MinimiserMonitor monitor);
	}

	protected final boolean isUseCurrentInUnivariateMinimisation() {
		return this.useCurrentInUnivariateMinimisation_;
	}
	/**
	 * Should we ignore new minisations that are not as minimal as the current one?
	 */
	protected final boolean isIgnoreNonMinimalUnivariateMinimisations() {
		return this.ignoreNonMinimalUnivariateMinimisations_;
	}
	protected void debug(Object output) {
		if(debug_) {
			System.out.println(output);
		}
	}
	protected boolean isDebug() {
		return debug_;
	}
	// ============ The Factory Class for Orthogonal Searches ===================
	private static final class SearchFactory implements Factory {
		boolean shuffle_;
		private SearchFactory(boolean shuffle) {	this.shuffle_ = shuffle;	}
		public MultivariateMinimum generateNewMinimiser() {		return new OrthogonalSearch(shuffle_);	}
	}
	//============== A means for doing Orthogonal optimisation ==================
	private class OrthogonalDirection implements RoundOptimiser {
		OrderEnumerator order_;
		OrthogonalLineFunction olf_;
		MultivariateFunction base_;

		public OrthogonalDirection(MultivariateFunction mf, OrderEnumerator.OEFactory orderFactory) {
			base_ = mf;
			olf_ = new OrthogonalLineFunction(base_);
			this.order_ = orthogonalOrderingFactory_.createOrderEnumerator(base_.getNumArguments());
		}
		public double doRound(double[] xvec, UnivariateMinimum um, double tolx,double fx, MinimiserMonitor monitor) {
			olf_.setAllArguments(xvec);

			order_.reset();
			while(order_.hasMore())	{
					int argument = order_.getNext();
					olf_.selectArgument(argument);
					double newArgValue =
					(
						useCurrentInUnivariateMinimisation_ ?
							um.optimize(xvec[argument], olf_, tolx) :
							um.optimize(olf_, tolx)
					);
				//If we actually found a better minimum...
				if(um.fminx<=fx) {
					xvec[argument] = newArgValue;
					olf_.setArgument(newArgValue);
					fx = um.fminx;
				}
				if(monitor!=null) {
					monitor.newMinimum(fx,xvec,base_);
				}
				debug(argument+":"+um.fminx+"  "+fx);
				numFun += um.numFun;

			}
			return fx;
		}
	}
	//============== A means for doing Orthogonal optimisation ==================
	private class OrthogonalHintsDirection implements RoundOptimiser {
		OrderEnumerator order_;
		OrthogonalLineFunction olf_;
		OrthogonalHints hints_;
		double[] store_ = new double[100];
		MultivariateFunction base_;
		public OrthogonalHintsDirection(MultivariateFunction mf, OrthogonalHints hints, OrderEnumerator.OEFactory orderFactory) {
			base_ = mf;
			olf_ = new OrthogonalLineFunction(base_);

			this.hints_ = hints;
			this.order_ = orthogonalOrderingFactory_.createOrderEnumerator(base_.getNumArguments());
		}
		private final double getNormalMin(UnivariateMinimum um, double argumentValue, double tolx) {
				return (
								useCurrentInUnivariateMinimisation_ ?
									um.optimize(argumentValue, olf_, tolx) :
									um.optimize(olf_, tolx)
							);
		}
		private final double getBoundedMin(UnivariateMinimum um, double argumentValue, double tolx,double min, double max) {
			if(useCurrentInUnivariateMinimisation_ && (min<=argumentValue&&max>=argumentValue)) {
				return um.optimize(argumentValue, olf_, tolx,min,max);
			}
			return um.optimize(olf_, tolx,min,max);
		}


		public double doRound(double[] xvec, UnivariateMinimum um, double tolx,double fx, MinimiserMonitor monitor) {
			olf_.setAllArguments(xvec);

			order_.reset();
			while(order_.hasMore())	{
				int argument = order_.getNext();
				olf_.selectArgument(argument);
				int numberOfHints= hints_.getInternalParameterBoundaries(argument,store_);
				//Yes this expensive, but will not happen very often (and only at beging of optimisation)
				while(numberOfHints<0) {
					store_ = new double[store_.length+10];
					numberOfHints= hints_.getInternalParameterBoundaries(argument,store_);
				}
				double newArgValue;
				double newFX;
				if(numberOfHints==0) {
					 newArgValue= getNormalMin(um,xvec[argument],tolx);
					 newFX = um.fminx;

				} else {
					debug("Number of hints:"+numberOfHints);
					//System.out.println("Store:"+pal.misc.Utils.toString(store_,numberOfHints));
					double min = olf_.getLowerBound();
					double x = xvec[argument];
					newArgValue = xvec[argument];
					newFX = Double.POSITIVE_INFINITY;
					for(int i = 0 ; i < numberOfHints ; i++) {
						x =getBoundedMin(um, xvec[argument], tolx, min,store_[i]);
						if(um.fminx<newFX) {
							newArgValue=x;	newFX = um.fminx;
						}
						min = store_[i];
					}
					double max = olf_.getUpperBound();
					if(min!=max) {
						x =getBoundedMin(um, xvec[argument], tolx, min,max);
						if(um.fminx<newFX) {
							newArgValue=x;	newFX = um.fminx;
						}
					}
				}
				//If we actually found a better minimum...
				if(newFX<=fx) {
					if(debug_&&numberOfHints>0) {
						//Do it old school!
						getNormalMin(um,xvec[argument],tolx);
					}
					xvec[argument] = newArgValue;
					olf_.setArgument(newArgValue);
					if(monitor!=null) {
						monitor.newMinimum(newFX,xvec,base_);
					}
					fx = newFX;


				}
				if(debug_) {
					System.out.println(argument+":"+newFX+"  "+fx+"   "+um.fminx+"    "+(um.fminx-newFX)+"   "+((um.fminx<newFX) ? "Bad" : "Good!"));
				}
				numFun += um.numFun;
			}
			return fx;
		}
	}

}
