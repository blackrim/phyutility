package jade.math;

public class AP_Basic {
	public static void inivec(int l, int u, double a[], double x) {
		for (; l <= u; l++)
			a[l] = x;
	}

	public static void inimat(int lr, int ur, int lc, int uc, double a[][],
	        double x) {
		int j;
		for (; lr <= ur; lr++)
			for (j = lc; j <= uc; j++)
				a[lr][j] = x;
	}

	public static void dupvec(int l, int u, int shift, double a[], double b[]) {
		for (; l <= u; l++)
			a[l] = b[l + shift];
	}

	public static void dupcolvec(int l, int u, int j, double a[][], double b[]) {
		for (; l <= u; l++)
			a[l][j] = b[l];
	}

	public static void dupmat(int l, int u, int i, int j, double a[][],
	        double b[][]) {
		int k;
		for (; l <= u; l++)
			for (k = i; k <= j; k++)
				a[l][k] = b[l][k];
	}

	public static void mulrow(int l, int u, int i, int j, double a[][],
	        double b[][], double x) {
		for (; l <= u; l++)
			a[i][l] = b[j][l] * x;
	}

	public static void mulcol(int l, int u, int i, int j, double a[][],
	        double b[][], double x) {
		for (; l <= u; l++)
			a[l][i] = b[l][j] * x;
	}

	public static double vecvec(int l, int u, int shift, double a[], double b[]) {
		int k;
		double s;
		s = 0.0;
		for (k = l; k <= u; k++)
			s += a[k] * b[k + shift];
		return (s);
	}

	public static double tammat(int l, int u, int i, int j, double a[][],
	        double b[][]) {
		int k;
		double s;
		s = 0.0;
		for (k = l; k <= u; k++)
			s += a[k][i] * b[k][j];
		return (s);
	}

	public static double mattam(int l, int u, int i, int j, double a[][],
	        double b[][]) {
		int k;
		double s;
		s = 0.0;
		for (k = l; k <= u; k++)
			s += a[i][k] * b[j][k];
		return (s);
	}

	public static void ichrowcol(int l, int u, int i, int j, double a[][]) {
		double r;
		for (; l <= u; l++) {
			r = a[i][l];
			a[i][l] = a[l][j];
			a[l][j] = r;
		}
	}

	public static void elmveccol(int l, int u, int i, double a[], double b[][],
	        double x) {
		for (; l <= u; l++)
			a[l] += b[l][i] * x;
	}

	public static int qrisngval(double a[][], int m, int n, double val[],
	        double em[]) {
		int i;
		double b[] = new double[n + 1];
		hshreabid(a, m, n, val, b, em);
		i = qrisngvalbid(val, b, n, em);
		return i;
	}

	public static int qrisngvalbid(double d[], double b[], int n, double em[]) {
		int n1, k, k1, i, i1, count, max, rnk;
		double tol, bmax, z, x, y, g, h, f, c, s, min;
		tol = em[2] * em[1];
		count = 0;
		bmax = 0.0;
		max = (int) em[4];
		min = em[6];
		rnk = n;
		do {
			k = n;
			n1 = n - 1;
			while (true) {
				k--;
				if (k <= 0)
					break;
				if (Math.abs(b[k]) >= tol) {
					if (Math.abs(d[k]) < tol) {
						c = 0.0;
						s = 1.0;
						for (i = k; i <= n1; i++) {
							f = s * b[i];
							b[i] *= c;
							i1 = i + 1;
							if (Math.abs(f) < tol)
								break;
							g = d[i1];
							d[i1] = h = Math.sqrt(f * f + g * g);
							c = g / h;
							s = -f / h;
						}
						break;
					}
				} else {
					if (Math.abs(b[k]) > bmax)
						bmax = Math.abs(b[k]);
					break;
				}
			}
			if (k == n1) {
				if (d[n] < 0.0)
					d[n] = -d[n];
				if (d[n] <= min)
					rnk--;
				n = n1;
			} else {
				count++;
				if (count > max)
					break;
				k1 = k + 1;
				z = d[n];
				x = d[k1];
				y = d[n1];
				g = (n1 == 1) ? 0.0 : b[n1 - 1];
				h = b[n1];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
				g = Math.sqrt(f * f + 1.0);
				f = ((x - z) * (x + z) + h
				        * (y / ((f < 0.0) ? f - g : f + g) - h))
				        / x;
				c = s = 1.0;
				for (i = k1 + 1; i <= n; i++) {
					i1 = i - 1;
					g = b[i1];
					y = d[i];
					h = s * g;
					g *= c;
					z = Math.sqrt(f * f + h * h);
					c = f / z;
					s = h / z;
					if (i1 != k1)
						b[i1 - 1] = z;
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y *= c;
					d[i1] = z = Math.sqrt(f * f + h * h);
					c = f / z;
					s = h / z;
					f = c * g + s * y;
					x = c * y - s * g;
				}
				b[n1] = f;
				d[n] = x;
			}
		} while (n > 0);
		em[3] = bmax;
		em[5] = count;
		em[7] = rnk;
		return n;
	}

	public static void hshreabid(double a[][], int m, int n, double d[],
	        double b[], double em[]) {
		int i, j, i1;
		double norm, machtol, w, s, f, g, h;
		norm = 0.0;
		for (i = 1; i <= m; i++) {
			w = 0.0;
			for (j = 1; j <= n; j++)
				w += Math.abs(a[i][j]);
			if (w > norm)
				norm = w;
		}
		machtol = em[0] * norm;
		em[1] = norm;
		for (i = 1; i <= n; i++) {
			i1 = i + 1;
			s = tammat(i1, m, i, i, a, a);
			if (s < machtol)
				d[i] = a[i][i];
			else {
				f = a[i][i];
				s += f * f;
				d[i] = g = (f < 0.0) ? Math.sqrt(s) : -Math.sqrt(s);
				h = f * g - s;
				a[i][i] = f - g;
				for (j = i1; j <= n; j++)
					elmcol(i, m, j, i, a, a, tammat(i, m, i, j, a, a) / h);
			}
			if (i < n) {
				s = mattam(i1 + 1, n, i, i, a, a);
				if (s < machtol)
					b[i] = a[i][i1];
				else {
					f = a[i][i1];
					s += f * f;
					b[i] = g = (f < 0.0) ? Math.sqrt(s) : -Math.sqrt(s);
					h = f * g - s;
					a[i][i1] = f - g;
					for (j = i1; j <= m; j++)
						elmrow(i1, n, j, i, a, a, mattam(i1, n, i, j, a, a) / h);
				}
			}
		}
	}

	public static void elmcol(int l, int u, int i, int j, double a[][],
	        double b[][], double x) {
		for (; l <= u; l++)
			a[l][i] += b[l][j] * x;
	}

	public static void elmrow(int l, int u, int i, int j, double a[][],
	        double b[][], double x) {
		for (; l <= u; l++)
			a[i][l] += b[j][l] * x;
	}

	public static int qrisngvaldec(double a[][], int m, int n, double val[],
	        double v[][], double em[]) {
		int i;
		double b[] = new double[n + 1];
		hshreabid(a, m, n, val, b, em);
		psttfmmat(a, n, v, b);
		pretfmmat(a, m, n, val);
		i = qrisngvaldecbid(val, b, m, n, a, v, em);
		return i;
	}

	public static int qrisngvaldecbid(double d[], double b[], int m, int n,
	        double u[][], double v[][], double em[]) {
		int n0, n1, k, k1, i, i1, count, max, rnk;
		double tol, bmax, z, x, y, g, h, f, c, s, min;
		tol = em[2] * em[1];
		count = 0;
		bmax = 0.0;
		max = (int) em[4];
		min = em[6];
		rnk = n0 = n;
		do {
			k = n;
			n1 = n - 1;
			while (true) {
				k--;
				if (k <= 0)
					break;
				if (Math.abs(b[k]) >= tol) {
					if (Math.abs(d[k]) < tol) {
						c = 0.0;
						s = 1.0;
						for (i = k; i <= n1; i++) {
							f = s * b[i];
							b[i] *= c;
							i1 = i + 1;
							if (Math.abs(f) < tol)
								break;
							g = d[i1];
							d[i1] = h = Math.sqrt(f * f + g * g);
							c = g / h;
							s = -f / h;
							rotcol(1, m, k, i1, u, c, s);
						}
						break;
					}
				} else {
					if (Math.abs(b[k]) > bmax)
						bmax = Math.abs(b[k]);
					break;
				}
			}
			if (k == n1) {
				if (d[n] < 0.0) {
					d[n] = -d[n];
					for (i = 1; i <= n0; i++)
						v[i][n] = -v[i][n];
				}
				if (d[n] <= min)
					rnk--;
				n = n1;
			} else {
				count++;
				if (count > max)
					break;
				k1 = k + 1;
				z = d[n];
				x = d[k1];
				y = d[n1];
				g = (n1 == 1) ? 0.0 : b[n1 - 1];
				h = b[n1];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
				g = Math.sqrt(f * f + 1.0);
				f = ((x - z) * (x + z) + h
				        * (y / ((f < 0.0) ? f - g : f + g) - h))
				        / x;
				c = s = 1.0;
				for (i = k1 + 1; i <= n; i++) {
					i1 = i - 1;
					g = b[i1];
					y = d[i];
					h = s * g;
					g *= c;
					z = Math.sqrt(f * f + h * h);
					c = f / z;
					s = h / z;
					if (i1 != k1)
						b[i1 - 1] = z;
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y *= c;
					rotcol(1, n0, i1, i, v, c, s);
					d[i1] = z = Math.sqrt(f * f + h * h);
					c = f / z;
					s = h / z;
					f = c * g + s * y;
					x = c * y - s * g;
					rotcol(1, m, i1, i, u, c, s);
				}
				b[n1] = f;
				d[n] = x;
			}
		} while (n > 0);
		em[3] = bmax;
		em[5] = count;
		em[7] = rnk;
		return n;
	}

	public static void pretfmmat(double a[][], int m, int n, double d[]) {
		int i, i1, j;
		double g, h;
		for (i = n; i >= 1; i--) {
			i1 = i + 1;
			g = d[i];
			h = g * a[i][i];
			for (j = i1; j <= n; j++)
				a[i][j] = 0.0;
			if (h < 0.0) {
				for (j = i1; j <= n; j++)
					elmcol(i, m, j, i, a, a, tammat(i1, m, i, j, a, a) / h);
				for (j = i; j <= m; j++)
					a[j][i] /= g;
			} else
				for (j = i; j <= m; j++)
					a[j][i] = 0.0;
			a[i][i] += 1.0;
		}
	}

	public static void psttfmmat(double a[][], int n, double v[][], double b[]) {
		int i, i1, j;
		double h;
		i1 = n;
		v[n][n] = 1.0;
		for (i = n - 1; i >= 1; i--) {
			h = b[i] * a[i][i1];
			if (h < 0.0) {
				for (j = i1; j <= n; j++)
					v[j][i] = a[i][j] / h;
				for (j = i1; j <= n; j++)
					elmcol(i1, n, j, i, v, v, matmat(i1, n, i, j, a, v));
			}
			for (j = i1; j <= n; j++)
				v[i][j] = v[j][i] = 0.0;
			v[i][i] = 1.0;
			i1 = i;
		}
	}

	public static double matmat(int l, int u, int i, int j, double a[][],
	        double b[][]) {
		int k;
		double s;
		s = 0.0;
		for (k = l; k <= u; k++)
			s += a[i][k] * b[k][j];
		return (s);
	}

	public static void rotcol(int l, int u, int i, int j, double a[][],
	        double c, double s) {
		double x, y;
		for (; l <= u; l++) {
			x = a[l][i];
			y = a[l][j];
			a[l][i] = x * c + y * s;
			a[l][j] = y * c - x * s;
		}
	}

}
