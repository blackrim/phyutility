/*
 * IntervalList.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * An interface for a set of coalescent intevals.
 *
 * @version $Id: IntervalList.java 305 2006-04-26 00:22:30Z rambaut $
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public interface IntervalList {

	public enum IntervalType {

		/**
		 * Denotes an interval at the end of which a new sample addition is
		 * observed (i.e. the number of lineages is larger in the next interval).
		 */
		SAMPLE("sample"),

		/** Denotes an interval after which a coalescent event is observed
		 * (i.e. the number of lineages is smaller in the next interval) */
		COALESCENT("coalescent"),

		/**
		 * Denotes an interval at the end of which a migration event occurs.
		 * This means that the colour of one lineage changes.
		 */
		MIGRATION("migration"),

		/**
		 * Denotes an interval at the end of which nothing is
		 * observed (i.e. the number of lineages is the same in the next interval).
		 */
		NOTHING("nothing");

		/**
		 * private constructor.
		 */
		private IntervalType(String name) {
			this.name = name;
		}

		public String toString() { return name; }

		private final String name;
	}

	/**
	 * get number of intervals
	 */
	int getIntervalCount();

	/**
	 * get the total number of sampling events.
	 */
	int getSampleCount();

	/**
	 * Gets an interval.
	 */
	double getInterval(int i);

	/**
	 * Returns the number of uncoalesced lineages within this interval.
	 * Required for s-coalescents, where new lineages are added as
	 * earlier samples are come across.
	 */
	int getLineageCount(int i);

	/**
	 * Returns the number coalescent events in an interval
	 */
	int getCoalescentEvents(int i);

	/**
	 * Returns the type of interval observed.
	 */
	IntervalType getIntervalType(int i);

	/**
	 * get the total duration of these intervals.
	 */
	double getTotalDuration();

	/**
	 * Checks whether this set of coalescent intervals is fully resolved
	 * (i.e. whether is has exactly one coalescent event in each
	 * subsequent interval)
	 */
	boolean isBinaryCoalescent();

	/**
	 * Checks whether this set of coalescent intervals coalescent only
	 * (i.e. whether is has exactly one or more coalescent event in each
	 * subsequent interval)
	 */
	boolean isCoalescentOnly();


	public class Utils {

		/**
		 * @return the number of lineages at time t.
		 * @param t the time that you are counting the number of lineages
		 */
		public static int getLineageCount(IntervalList intervals, double t) {

			int i = 0;
			while (i < intervals.getIntervalCount() && t > intervals.getInterval(i)) {
				t -= intervals.getInterval(i);
				i+= 1;
			}
			if (i == intervals.getIntervalCount()) return 1;
			return intervals.getLineageCount(i);
		}
	};
}