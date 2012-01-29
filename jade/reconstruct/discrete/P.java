package jade.reconstruct.discrete;

/*
 * currently does not take into account assymetrical rates
 * to do so, just need to do something with b
 */

public class P {
	public P(int size) {
		this.size = size;
		this.a = 0.1;
		this.b = 0.1;

	}

	public P(int size, double a, double b) {
		this.size = size;
		this.a = a;
		this.b = b;
		System.out.println(size);
	}

	public double[][] getP(double time) {
		double[][] p = new double[2][2];
		p = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (i == j) {
					p[i][j] = (1.0 / size)
					        + ((size - 1) / Double.valueOf(size))
					        * (Math.exp(-(size * a * time)));
				} else {
					p[i][j] = (1.0 / size) - (1.0 / size)
					        * (Math.exp(-(size * a * time)));
				}
			}
		}
		P = p;
		return p;
	}

	public void setRates(double a) {
		this.a = a;
		this.b = a;
	}

	public void setRates(double a, double b) {
		this.a = a;
		this.b = b;
	}

	public void printP() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				System.out.print(P[i][j] + "\t");
			}
			System.out.println();
		}

	}

	public static void main(String[] args) {
		P p = new P(2);
		p.getP(1);
		p.printP();
	}

	private double[][] P;

	private double a;

	private double b;

	private int size;
}
