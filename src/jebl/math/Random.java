/*
 * Random.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.math;

/**
 * Random number generation.
 *
 * @author Matthew Goode
 * @author Alexei Drummond
 *
 * @version $Id: Random.java 370 2006-06-29 18:57:56Z rambaut $
 */
public class Random {

	private Random() {}

	/**
	 * A random number generator that is initialized with the clock when this
	 * class is loaded into the JVM. Use this for all random numbers.
	 * Note: This method or getting random numbers in not thread-safe. Since
	 * MersenneTwisterFast is currently (as of 9/01) not synchronized using
	 * this function may cause concurrency issues. Use the static get methods of the
	 * MersenneTwisterFast class for access to a single instance of the class, that
	 * has synchronization.
	 */
	private static final MersenneTwisterFast random = new MersenneTwisterFast();

    // Chooses one category if a cumulative probability distribution is given
	public static int randomChoice(double[] cf)
	{

		double U = random.nextDouble();

		int s;
		if (U <= cf[0])
		{
			s = 0;
		}
		else
		{
			for (s = 1; s < cf.length; s++)
			{
				if (U <= cf[s] && U > cf[s-1])
				{
					break;
				}
			}
		}

		return s;
	}

	/**
	 * @return a new double array where all the values sum to 1.
	 * Relative ratios are preserved.
	 */
	public static double[] getNormalized(double[] array) {
		double[] newArray = new double[array.length];
		double total = getTotal(array);
		for(int i = 0 ; i < array.length ; i++) {
			newArray[i] = array[i]/total;
		}
		return newArray;
	}

	/**
	 * @param end the index of the element after the last one to be included
	 * @return the total of a the values in a range of an array
	 */
	public static double getTotal(double[] array, int start, int end) {
		double total = 0.0;
		for(int i = start ; i < end; i++) {
			total+=array[i];
		}
		return total;
	}

	/**
	 * @return the total of the values in an array
	 */
	public static double getTotal(double[] array) {
		return getTotal(array,0, array.length);

	}

    // ===================== Static access methods to the private random instance ===========

    /** Access a default instance of this class, access is synchronized */
    public static void setSeed(long seed) {
        synchronized(random) {
            random.setSeed(seed);
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static byte nextByte() {
        synchronized(random) {
            return random.nextByte();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static boolean nextBoolean() {
        synchronized(random) {
            return random.nextBoolean();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static void nextBytes(byte[] bs) {
        synchronized(random) {
            random.nextBytes(bs);
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static char nextChar() {
        synchronized(random) {
            return random.nextChar();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static double nextGaussian() {
        synchronized(random) {
            return random.nextGaussian();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static double nextDouble() {
        synchronized(random) {
            return random.nextDouble();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static float nextFloat() {
        synchronized(random) {
            return random.nextFloat();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static long nextLong() {
        synchronized(random) {
            return random.nextLong();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static short nextShort() {
        synchronized(random) {
            return random.nextShort();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static int nextInt() {
        synchronized(random) {
            return random.nextInt();
        }
    }
    /** Access a default instance of this class, access is synchronized */
    public static int nextInt(int n) {
        synchronized(random) {
            return random.nextInt(n);
        }
    }

    /**
     * Shuffles an array.
     */
    public static void shuffle(int[] array) {
        synchronized(random) {
            random.shuffle(array);
        }
    }
    /**
     * Shuffles an array. Shuffles numberOfShuffles times
     */
    public static void shuffle(int[] array, int numberOfShuffles) {
        synchronized(random) {
            random.shuffle(array, numberOfShuffles);
        }
    }
    /**
     * Returns an array of shuffled indices of length l.
     * @param l length of the array required.
     */
    public static int[] shuffled(int l) {
        synchronized(random) {
            return random.shuffled(l);
        }
    }
}
