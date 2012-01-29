package jade.math;

public final class PrecisionCalculator {

	/** Radix used by floating-point numbers. */
	private final static int radix = computeRadix();

	/** Largest positive value which, when added to 1.0, yields 0 */
	private final static double machinePrecision = computeMachinePrecision();

	/** Typical meaningful precision for numerical calculations. */
	private final static double defaultNumericalPrecision = Math
	        .sqrt(machinePrecision);

	private static int computeRadix() {
		int radix = 0;
		double a = 1.0d;
		double tmp1, tmp2;
		do {
			a += a;
			tmp1 = a + 1.0d;
			tmp2 = tmp1 - a;
		} while (tmp2 - 1.0d != 0.0d);
		double b = 1.0d;
		while (radix == 0) {
			b += b;
			tmp1 = a + b;
			radix = (int) (tmp1 - a);
		}
		return radix;
	}

	private static double computeMachinePrecision() {
		double floatingRadix = getRadix();
		double inverseRadix = 1.0d / floatingRadix;
		double machinePrecision = 1.0d;
		double tmp = 1.0d + machinePrecision;
		while (tmp - 1.0d != 0.0d) {
			machinePrecision *= inverseRadix;
			tmp = 1.0d + machinePrecision;
		}
		return machinePrecision;
	}

	public static int getRadix() {
		return radix;
	}

	public static double getMachinePrecision() {
		return machinePrecision;
	}

	public static double defaultNumericalPrecision() {
		return defaultNumericalPrecision;
	}

}
