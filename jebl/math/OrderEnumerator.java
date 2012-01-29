// OrderEnumerator.java
//
// (c) 2006-     JEBL development team
//
// based on LGPL code from the Phylogenetic Analysis Library (PAL),
// http://www.cebl.auckland.ac.nz/pal-project/
// which is (c) 1999-2002 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package jebl.math;


/**
 * A means for describing odering information, and Utilities for creating such Orderings
 *
 * @version $Id: OrderEnumerator.java 258 2006-03-16 23:45:00Z twobeers $
 *
 * @author Matthew Goode
 */

public interface OrderEnumerator {
	/**
	 * If hasMore returns false reset should be called
	 */
	boolean hasMore();
	/**
	 * The next value in the enumeration
	 */
	int getNext();
	/**
	 * Reset back to starting state, may have a differnet number of values, and a different ordering after a reset!
	 */
	void reset();

	public static interface OEFactory {
		/**
		 * For generating an ordering from 0..size-1. Enumerator doesn't have to actually produce
		 */
		public OrderEnumerator createOrderEnumerator(int size);
	}

//=====================================================================================================
//================================= Utilities, and hidden classes =====================================
//=====================================================================================================

	public static class Utils {
		private static final Constant ZERO = new Constant(0);
		/**
		 * @param index The index to always return
		 * @return an OrderEnumerator object that always returns 'index'
		 */
		public static final OrderEnumerator getConstant(int index) {
			return new Constant(index);
		}

		/**
		 * @param size the number of different indexes returned (between 0 and size-1)
		 * @return an OrderEnumerator object returns index in order between a certain range
		 */
		public static final OrderEnumerator getOrdered(int size) {
			return new Ordered(size);
		}
		/**
		 * @param size the number of different indexes returned (between 0 and size-1)
		 * @return an OrderEnumerator object returns index in random order between a certain range (order changes with each reset)
		 */
		public static final OrderEnumerator getShuffled(int size) {
			return new Shuffled(size);
		}
		/**
		 * @param primary The primary OrderEnumerator, one index is taken from this enumertor than an entire sequence of the secondary is taken
		 * @param secondary The primary OrderEnumerator, the entire sequence of a secondary enumerator is taken for every single index from the primary enumerator
		 *
		 * @return an OrderEnumerator object that combines two sub enumerators
		 */
		public static final OrderEnumerator getBiasAlternating(OrderEnumerator primary, OrderEnumerator secondary) {
			return new BiasAlternate(primary,secondary);
		}
		/**
		 * @param primary The primary OrderEnumerator
		 * @param secondary The primary OrderEnumerator
		 *
		 * @return an OrderEnumerator object that combines two sub enumerators, by alternating between outputs
		 */
		public static final OrderEnumerator getAlternating(OrderEnumerator primary, OrderEnumerator secondary) {
			return new Alternate(primary,secondary);
		}
		/**
		 * @return OrderEnumerator that always returns 0 (zero)
		 */
		public static final OrderEnumerator getZero() {
			return ZERO;
		}
		/**
		 * @param minimum minmim value released
		 * @param range range of values released (that is values go between minimum (inclusive) and minimum+range(exclusive)
		 *
		 * @return an OrderEnumerator that is restricted in indexes it returns based on base Enumerator
		 *
		 */
		public static final OrderEnumerator getRestricted(OrderEnumerator toRestrict, int minimum, int range) {
			return new Restricted(toRestrict,minimum, range);
		}

		/**
		 * @return OrderEnumerator that always returns 0 (zero)
		 */
		public static final OrderEnumerator getAdjusted(OrderEnumerator toAdjust, int adjustmentFactor) {
			return new Adjust(toAdjust,adjustmentFactor);
		}

		//============================================================
		//=================== Factory Stuff ==========================
		/**
		 * @return OrderEnumerator that always returns 0 (zero)
		 */
		public static final OrderEnumerator.OEFactory getZeroFactory() {
			return ZERO;
		}
		/**
		 * @param index The index to always return
		 * @return an OrderEnumerator object that always returns 'index'
		 */
		public static final OrderEnumerator.OEFactory getConstantFactory(int index) {
			return new Constant(index);
		}

		/**
		 * @return an OrderEnumerator object returns index in order between a certain range
		 */
		public static final OrderEnumerator.OEFactory getOrderedFactory() {
			return Ordered.Factory.INSTANCE;
		}
		/**
		 * @return an OrderEnumerator object returns index in random order between a certain range (order changes with each reset)
		 */
		public static final OrderEnumerator.OEFactory getShuffledFactory() {
			return Shuffled.Factory.INSTANCE;
		}
		/**
		 * @param adjustmentFactor If to adjust returns x, adjusted will return x+adjustmentFactory (it's that simple)
		 * @return an OrderEnumerator that returns indexes adjusted from a base enumerator
		 *
		 */
		public static final OrderEnumerator.OEFactory getAdjustedFactory(OrderEnumerator.OEFactory toAdjust, int adjustmentFactor) {
			return new Adjust.Factory(toAdjust,adjustmentFactor);
		}
		/**
		 * @param minimum minmim value released
		 * @param range range of values released (that is values go between minimum (inclusive) and minimum+range(exclusive)
		 *
		 * @return an OrderEnumerator that is restricted in indexes it returns based on base Enumerator
		 *
		 */
		public static final OrderEnumerator.OEFactory getRestrictedFactory(OrderEnumerator.OEFactory toRestrict, int minimum, int range) {
			return new Restricted.Factory(toRestrict,minimum, range);
		}

		/**
		 * @return an OrderEnumerator object that alternates outputs between two base enumerator
		 */
		public static final OrderEnumerator.OEFactory getAlternatingFactory(OrderEnumerator.OEFactory primary, OrderEnumerator.OEFactory secondary) {
			return new Alternate.Factory(primary,secondary);
		}
		/**
		 * @return an OrderEnumerator object that alternates outputs between two base enumerator
		 * (takes one from primary, than all from secondary, one from primary, all from secondary)
		 */
		public static final OrderEnumerator.OEFactory getBiasAlternatingFactory(OrderEnumerator.OEFactory primary, OrderEnumerator.OEFactory secondary) {
			return new BiasAlternate.Factory(primary,secondary);
		}
		//=======================================================

		/**
		 * Returns the same index ever call
		 */
		private static class Constant implements OrderEnumerator, OrderEnumerator.OEFactory {
			int index_;
			boolean hasMore_;
			public Constant(int index) {
				this.index_ = index;
			}
			public boolean hasMore() { return hasMore_; }
			public int getNext() { hasMore_ = false; return index_; }
			public void reset() { hasMore_ = true; }
			/**
				* For generating an ordering from 0..size-1. Enumerator doesn't have to actually produce
				*/
			public OrderEnumerator createOrderEnumerator(int size) {
				return this;
			}

		} //End of Constant
		//=======================================================

		/**
		 * Incrementally returns indexes
		 */
		private static class Ordered implements OrderEnumerator {
			int index_;
			int size_;
			public Ordered(int size) {
				this.size_= size;
				reset();
			}
			public boolean hasMore() {
				return index_<size_;
			}
			public int getNext() { return index_++; }
			public void reset() { index_ = 0; }
			//=====================================================
			//================= Factory ===========================
			static class Factory implements OrderEnumerator.OEFactory {
				static final Factory INSTANCE = new Factory();
				public OrderEnumerator createOrderEnumerator(int size) {
					return new Ordered(size);
				}
			} //End of Ordered.Factory
		} //End of Ordered
		//=======================================================
		/**
		 * Returns indexes between 0 and size but in a shuffled order. Order changes with each reset.
		 */
		private static class Shuffled implements OrderEnumerator {
			int currentIndex_;
			int[] indexes_;
			int size_;
			MersenneTwisterFast random_ = new MersenneTwisterFast();
			public Shuffled(int size) {
				this.size_= size;
				this.indexes_ = new int[size];
				for(int i = 0 ; i < size ;  i++) {
					indexes_[i] = i;
				}
				reset();
			}
			public boolean hasMore() {
				return currentIndex_<size_;
			}
			public int getNext() { return indexes_[currentIndex_++]; }
			public void reset() { currentIndex_ = 0; random_.shuffle(indexes_); }

			//=====================================================
			//================= Factory ===========================
			static class Factory implements OrderEnumerator.OEFactory {
				static final Factory INSTANCE = new Factory();

				public OrderEnumerator createOrderEnumerator(int size) {
					return new Shuffled(size);
				}
			} //End of Shuffled.Factory
		} //End of Shuffled
		//=======================================================
		/**
		 * Restricts indexes output by a sub OrderEnumerator by to between a certain range,
		 * skips over indexes outside range
		 */
		private static class Restricted implements OrderEnumerator {
			OrderEnumerator toAdjust_;
			int minimum_;
			int top_;
			int next_;
			boolean hasMore_;

			public Restricted(OrderEnumerator toAdjust, int minimum, int range) {
				this.toAdjust_ = toAdjust;
				this.minimum_ = minimum;
				this.top_ = minimum+range;
				reset();
			}
			public boolean hasMore() {
				return hasMore_;
			}
			private void updateNext() {
				hasMore_ = false;
				while(toAdjust_.hasMore()) {
					int i = toAdjust_.getNext();
					if(i>=minimum_&&i<top_) {
						next_ = i;
						hasMore_ = true;
						break;
					}
				}
			}
			public int getNext() { int myNext = next_; updateNext(); return myNext; }
			public void reset() { toAdjust_.reset(); updateNext(); }

			//=====================================================
			//================= Factory ===========================
			static class Factory implements OrderEnumerator.OEFactory {
				OrderEnumerator.OEFactory toAdjustFactory_;
				int minimum_, range_;
				public Factory(OrderEnumerator.OEFactory toAdjustFactory, int minimum, int range) {
					this.toAdjustFactory_ = toAdjustFactory;
					this.minimum_ = minimum;
					this.range_ = range;
				}
				public OrderEnumerator createOrderEnumerator(int size) {
					return new Restricted(toAdjustFactory_.createOrderEnumerator(size),minimum_,range_);
				}
			} //End of Restricted.Factory
		} //End of Restricted
		/**
		 * Alters indexes output by a sub OrderEnumerator by constant amount
		 */
		private static class Adjust implements OrderEnumerator {
			OrderEnumerator toAdjust_;
			int adjustmentAmount_;
			/**
			 * @param adjustmentAmount - how much adjust a value by, should be positive
			 *    (for example adjustment of 1 will mean when sub returns 5, this will return 6)
			 */
			public Adjust(OrderEnumerator toAdjust, int adjustmentAmount) {
				this.toAdjust_ = toAdjust;
				this.adjustmentAmount_ = adjustmentAmount;
				reset();
			}
			public boolean hasMore() {
				return toAdjust_.hasMore();
			}
			public int getNext() { return toAdjust_.getNext()+adjustmentAmount_; }
			public void reset() { toAdjust_.reset(); }

			//=====================================================
			//================= Factory ===========================
			static class Factory implements OrderEnumerator.OEFactory {
				OrderEnumerator.OEFactory toAdjustFactory_;
				int adjustmentAmount_;
				public Factory(OrderEnumerator.OEFactory toAdjustFactory, int adjustmentAmount) {
					this.toAdjustFactory_ = toAdjustFactory;
					this.adjustmentAmount_ = adjustmentAmount;
				}
				public OrderEnumerator createOrderEnumerator(int size) {
					return new Adjust(toAdjustFactory_.createOrderEnumerator(size-adjustmentAmount_),adjustmentAmount_);
				}
			} //End of Adjust.Factory
		} //End of Adjust

		//=======================================================
		/**
		 * Returns alternating between each enumerator.
		 */
		private static class Alternate implements OrderEnumerator {
			OrderEnumerator primary_;
			OrderEnumerator secondary_;
			boolean onSecondary_; //Only have this instead of just using secondary_.hasMore() for very first iteration!
			/**
			 * Alternates from each untill both used up
			 */
			public Alternate(OrderEnumerator primary, OrderEnumerator secondary) {
				this.primary_ = primary;
				this.secondary_ = secondary;
				reset();
			}
			public boolean hasMore() {
				return primary_.hasMore()||secondary_.hasMore();
			}
			public int getNext() {
				int toReturn;
				if(onSecondary_&&secondary_.hasMore()) {
					toReturn = secondary_.getNext();
					onSecondary_ = !primary_.hasMore();
				} else {
					toReturn = primary_.getNext();
					onSecondary_ = secondary_.hasMore();
					return toReturn;
				}
				return toReturn;
			}
			public void reset() {
				primary_.reset();
				secondary_.reset();
				onSecondary_ = false;
			}
			//=====================================================
			//================= Factory ===========================
			static class Factory implements OrderEnumerator.OEFactory {
				OrderEnumerator.OEFactory primary_;
				OrderEnumerator.OEFactory secondary_;
				public Factory(OrderEnumerator.OEFactory primary, OrderEnumerator.OEFactory secondary) {
					this.primary_ = primary;
					this.secondary_ = secondary;
				}
				public OrderEnumerator createOrderEnumerator(int size) {
					return new Alternate(primary_.createOrderEnumerator(size),secondary_.createOrderEnumerator(size));
				}
			} //End of Atlernate.Factory
		} //End of Alternate

		//=======================================================
		/**
		 * Returns indexes between 0 and size but in a shuffled order. Order changes with each reset.
		 */
		private static class BiasAlternate implements OrderEnumerator {
			OrderEnumerator primary_;
			OrderEnumerator secondary_;
			boolean onSecondary_; //Only have this instead of just using secondary_.hasMore() for very first iteration!
			/**
			 * For every index taken from primary, an entire round of secondary will be taken
			 */
			public BiasAlternate(OrderEnumerator primary, OrderEnumerator secondary) {
				this.primary_ = primary;
				this.secondary_ = secondary;
				reset();
			}

			public boolean hasMore() {
				return(primary_.hasMore()||(onSecondary_&&secondary_.hasMore()));
			}
			public int getNext() {
				if(onSecondary_) {
					int toReturn = secondary_.getNext();
					onSecondary_ = secondary_.hasMore();
					return toReturn;
				} else {
					secondary_.reset();
					onSecondary_ = secondary_.hasMore();
					return primary_.getNext();
				}
			}
			public void reset() {
				primary_.reset();
				onSecondary_ = false;
			}
			//=====================================================
			//================= Factory ===========================
			static class Factory implements OrderEnumerator.OEFactory {
				OrderEnumerator.OEFactory primary_;
				OrderEnumerator.OEFactory secondary_;
				public Factory(OrderEnumerator.OEFactory primary, OrderEnumerator.OEFactory secondary) {
					this.primary_ = primary;
					this.secondary_ = secondary;
				}
				public OrderEnumerator createOrderEnumerator(int size) {
					return new BiasAlternate(primary_.createOrderEnumerator(size),secondary_.createOrderEnumerator(size));
				}
			} //End of BiasAlternate.Factory
		} //End of BiasAlternate

	} //End of Utils
}