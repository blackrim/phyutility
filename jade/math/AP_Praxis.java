package jade.math;

import java.util.*;

public class AP_Praxis {
	public static void praxis(int n, double x[], AP_praxis_method method,
	        double in[], double out[])

	{
		boolean illc, emergency;
		int i, j, k, k2, maxf, kl, kt, ktm;
		double s, sl, dn, dmin, f1, lds, ldt, sf, df, qf1, qd0, qd1, m2, m4, small, vsmall, large, vlarge, scbd, ldfac, t2, macheps, reltol, abstol, h, l;
		int nl[] = new int[1];
		int nf[] = new int[1];
		double em[] = new double[8];
		double d[] = new double[n + 1];
		double y[] = new double[n + 1];
		double z[] = new double[n + 1];
		double q0[] = new double[n + 1];
		double q1[] = new double[n + 1];
		double v[][] = new double[n + 1][n + 1];
		double a[][] = new double[n + 1][n + 1];
		double qa[] = new double[1];
		double qb[] = new double[1];
		double qc[] = new double[1];
		double fx[] = new double[1];
		double tmp1[] = new double[1];
		double tmp2[] = new double[1];
		double tmp3[] = new double[1];
		Random ran = new Random(1);
		macheps = in[0];
		reltol = in[1];
		abstol = in[2];
		maxf = (int) in[5];
		h = in[6];
		scbd = in[7];
		ktm = (int) in[8];
		illc = in[9] < 0.0;
		small = macheps * macheps;
		vsmall = small * small;
		large = 1.0 / small;
		vlarge = 1.0 / vsmall;
		m2 = reltol;
		m4 = Math.sqrt(m2);
		ldfac = (illc ? 0.1 : 0.01);
		kt = nl[0] = 0;
		nf[0] = 1;
		out[3] = qf1 = fx[0] = method.funct(n, x);
		abstol = t2 = small + Math.abs(abstol);
		dmin = small;
		if (h < abstol * 100.0)
			h = abstol * 100;
		ldt = h;
		AP_Basic.inimat(1, n, 1, n, v, 0.0);
		for (i = 1; i <= n; i++)
			v[i][i] = 1.0;
		d[1] = qd0 = qd1 = 0.0;
		AP_Basic.dupvec(1, n, 0, q1, x);
		AP_Basic.inivec(1, n, q0, 0.0);
		emergency = false;
		while (true) {
			sf = d[1];
			d[1] = s = 0.0;
			tmp1[0] = d[1];
			tmp2[0] = s;
			praxismin(1, 2, tmp1, tmp2, fx, false, n, x, v, qa, qb, qc, qd0,
			        qd1, q0, q1, nf, nl, fx, m2, m4, dmin, ldt, reltol, abstol,
			        small, h, method);
			d[1] = tmp1[0];
			s = tmp2[0];
			if (s <= 0.0)
				AP_Basic.mulcol(1, n, 1, 1, v, v, -1.0);
			if (sf <= 0.9 * d[1] || 0.9 * sf >= d[1])
				AP_Basic.inivec(2, n, d, 0.0);
			for (k = 2; k <= n; k++) {
				AP_Basic.dupvec(1, n, 0, y, x);
				sf = fx[0];
				illc = (illc || kt > 0);
				while (true) {
					kl = k;
					df = 0.0;
					if (illc) {
						/* random stop to get off resulting valley */
						for (i = 1; i <= n; i++) {
							s = z[i] = (0.1 * ldt + t2 * Math.pow(10.0, kt))
							        * (ran.nextDouble() - 0.5);
							AP_Basic.elmveccol(1, n, i, x, v, s);
						}
						fx[0] = method.funct(n, x);
						nf[0]++;
					}
					for (k2 = k; k2 <= n; k2++) {
						sl = fx[0];
						s = 0.0;
						tmp1[0] = d[k2];
						tmp2[0] = s;
						praxismin(k2, 2, tmp1, tmp2, fx, false, n, x, v, qa,
						        qb, qc, qd0, qd1, q0, q1, nf, nl, fx, m2, m4,
						        dmin, ldt, reltol, abstol, small, h, method);
						d[k2] = tmp1[0];
						s = tmp2[0];
						s = illc ? d[k2] * (s + z[k2]) * (s + z[k2]) : sl
						        - fx[0];
						if (df < s) {
							df = s;
							kl = k2;
						}
					}
					if (!illc && df < Math.abs(100.0 * macheps * fx[0]))
						illc = true;
					else
						break;
				}
				for (k2 = 1; k2 <= k - 1; k2++) {
					s = 0.0;
					tmp1[0] = d[k2];
					tmp2[0] = s;
					praxismin(k2, 2, tmp1, tmp2, fx, false, n, x, v, qa, qb,
					        qc, qd0, qd1, q0, q1, nf, nl, fx, m2, m4, dmin,
					        ldt, reltol, abstol, small, h, method);
					d[k2] = tmp1[0];
					s = tmp2[0];
				}
				f1 = fx[0];
				fx[0] = sf;
				lds = 0.0;
				for (i = 1; i <= n; i++) {
					sl = x[i];
					x[i] = y[i];
					y[i] = sl -= y[i];
					lds += sl * sl;
				}
				lds = Math.sqrt(lds);
				if (lds > small) {
					for (i = kl - 1; i >= k; i--) {
						for (j = 1; j <= n; j++)
							v[j][i + 1] = v[j][i];
						d[i + 1] = d[i];
					}
					d[k] = 0.0;
					AP_Basic.dupcolvec(1, n, k, v, y);
					AP_Basic.mulcol(1, n, k, k, v, v, 1.0 / lds);
					tmp1[0] = d[k];
					tmp2[0] = lds;
					tmp3[0] = f1;
					praxismin(k, 4, tmp1, tmp2, tmp3, true, n, x, v, qa, qb,
					        qc, qd0, qd1, q0, q1, nf, nl, fx, m2, m4, dmin,
					        ldt, reltol, abstol, small, h, method);
					d[k] = tmp1[0];
					lds = tmp2[0];
					f1 = tmp3[0];
					if (lds <= 0.0) {
						lds = -lds;
						AP_Basic.mulcol(1, n, k, k, v, v, -1.0);
					}
				}
				ldt *= ldfac;
				if (ldt < lds)
					ldt = lds;
				t2 = m2 * Math.sqrt(AP_Basic.vecvec(1, n, 0, x, x)) + abstol;
				kt = (ldt > 0.5 * t2) ? 0 : kt + 1;
				if (kt > ktm) {
					out[1] = 0.0;
					emergency = true;
				}
			}
			if (emergency)
				break;
			/* quad */
			s = fx[0];
			fx[0] = qf1;
			qf1 = s;
			qd1 = 0.0;
			for (i = 1; i <= n; i++) {
				s = x[i];
				x[i] = l = q1[i];
				q1[i] = s;
				qd1 += (s - l) * (s - l);
			}
			l = qd1 = Math.sqrt(qd1);
			s = 0.0;
			if ((qd0 * qd1 > Double.MIN_VALUE) && (nl[0] >= 3 * n * n)) {
				tmp1[0] = s;
				tmp2[0] = l;
				tmp3[0] = qf1;
				praxismin(0, 2, tmp1, tmp2, tmp3, true, n, x, v, qa, qb, qc,
				        qd0, qd1, q0, q1, nf, nl, fx, m2, m4, dmin, ldt,
				        reltol, abstol, small, h, method);
				s = tmp1[0];
				l = tmp2[0];
				qf1 = tmp3[0];
				qa[0] = l * (l - qd1) / (qd0 * (qd0 + qd1));
				qb[0] = (l + qd0) * (qd1 - l) / (qd0 * qd1);
				qc[0] = l * (l + qd0) / (qd1 * (qd0 + qd1));
			} else {
				fx[0] = qf1;
				qa[0] = qb[0] = 0.0;
				qc[0] = 1.0;
			}
			qd0 = qd1;
			for (i = 1; i <= n; i++) {
				s = q0[i];
				q0[i] = x[i];
				x[i] = qa[0] * s + qb[0] * x[i] + qc[0] * q1[i];
			}
			/* end of quad */
			dn = 0.0;
			for (i = 1; i <= n; i++) {
				d[i] = 1.0 / Math.sqrt(d[i]);
				if (dn < d[i])
					dn = d[i];
			}
			for (j = 1; j <= n; j++) {
				s = d[j] / dn;
				AP_Basic.mulcol(1, n, j, j, v, v, s);
			}
			if (scbd > 1.0) {
				s = vlarge;
				for (i = 1; i <= n; i++) {
					sl = z[i] = Math.sqrt(AP_Basic.mattam(1, n, i, i, v, v));
					if (sl < m4)
						z[i] = m4;
					if (s > sl)
						s = sl;
				}
				for (i = 1; i <= n; i++) {
					sl = s / z[i];
					z[i] = 1.0 / sl;
					if (z[i] > scbd) {
						sl = 1.0 / scbd;
						z[i] = scbd;
					}
					AP_Basic.mulrow(1, n, i, i, v, v, sl);
				}
			}
			for (i = 1; i <= n; i++)
				AP_Basic.ichrowcol(i + 1, n, i, i, v);
			em[0] = em[2] = macheps;
			em[4] = 10 * n;
			em[6] = vsmall;
			AP_Basic.dupmat(1, n, 1, n, a, v);
			if (AP_Basic.qrisngvaldec(a, n, n, d, v, em) != 0) {
				out[1] = 2.0;
				emergency = true;
			}
			if (emergency)
				break;
			if (scbd > 1.0) {
				for (i = 1; i <= n; i++)
					AP_Basic.mulrow(1, n, i, i, v, v, z[i]);
				for (i = 1; i <= n; i++) {
					s = Math.sqrt(AP_Basic.tammat(1, n, i, i, v, v));
					d[i] *= s;
					s = 1.0 / s;
					AP_Basic.mulcol(1, n, i, i, v, v, s);
				}
			}
			for (i = 1; i <= n; i++) {
				s = dn * d[i];
				d[i] = (s > large) ? vsmall : ((s < small) ? vlarge
				        : 1.0 / (s * s));
			}
			/* sort */
			for (i = 1; i <= n - 1; i++) {
				k = i;
				s = d[i];
				for (j = i + 1; j <= n; j++)
					if (d[j] > s) {
						k = j;
						s = d[j];
					}
				if (k > i) {
					d[k] = d[i];
					d[i] = s;
					for (j = 1; j <= n; j++) {
						s = v[j][i];
						v[j][i] = v[j][k];
						v[j][k] = s;
					}
				}
			}
			/* end of sort */
			dmin = d[n];
			if (dmin < small)
				dmin = small;
			illc = (m2 * d[1]) > dmin;
			if (nf[0] >= maxf) {
				out[1] = 1.0;
				break;
			}
		}
		out[2] = fx[0];
		out[4] = nf[0];
		out[5] = nl[0];
		out[6] = ldt;
	}

	static private void praxismin(int j, int nits, double d2[], double x1[],
	        double f1[], boolean fk, int n, double x[], double v[][],
	        double qa[], double qb[], double qc[], double qd0, double qd1,
	        double q0[], double q1[], int nf[], int nl[], double fx[],
	        double m2, double m4, double dmin, double ldt, double reltol,
	        double abstol, double small, double h, AP_praxis_method method) {
		/* this procedure is internally used by PRAXIS */
		boolean loop, dz;
		int k;
		double x2, xm, f0, f2, fm, d1, t2, s, sf1, sx1;
		f2 = x2 = 0.0;
		sf1 = f1[0];
		sx1 = x1[0];
		k = 0;
		xm = 0.0;
		f0 = fm = fx[0];
		dz = d2[0] < reltol;
		s = Math.sqrt(AP_Basic.vecvec(1, n, 0, x, x));
		t2 = m4 * Math.sqrt(Math.abs(fx[0]) / (dz ? dmin : d2[0]) + s * ldt)
		        + m2 * ldt;
		s = s * m4 + abstol;
		if (dz && (t2 > s))
			t2 = s;
		if (t2 < small)
			t2 = small;
		if (t2 > 0.01 * h)
			t2 = 0.01 * h;
		if (fk && (f1[0] <= fm)) {
			xm = x1[0];
			fm = f1[0];
		}
		if (!fk || (Math.abs(x1[0]) < t2)) {
			x1[0] = (x1[0] > 0.0) ? t2 : -t2;
			f1[0] = praxisflin(x1[0], j, n, x, v, qa, qb, qc, qd0, qd1, q0, q1,
			        nf, method);
		}
		if (f1[0] <= fm) {
			xm = x1[0];
			fm = f1[0];
		}
		loop = true;
		while (loop) {
			if (dz) {
				/*
				 * evaluate praxisflin at another point and estimate the second
				 * derivative
				 */
				x2 = (f0 < f1[0]) ? -x1[0] : x1[0] * 2.0;
				f2 = praxisflin(x2, j, n, x, v, qa, qb, qc, qd0, qd1, q0, q1,
				        nf, method);
				if (f2 <= fm) {
					xm = x2;
					fm = f2;
				}
				d2[0] = (x2 * (f1[0] - f0) - x1[0] * (f2 - f0))
				        / (x1[0] * x2 * (x1[0] - x2));
			}
			/* estimate first derivative at 0 */
			d1 = (f1[0] - f0) / x1[0] - x1[0] * d2[0];
			dz = true;
			x2 = (d2[0] <= small) ? ((d1 < 0.0) ? h : -h) : -0.5 * d1 / d2[0];
			if (Math.abs(x2) > h)
				x2 = (x2 > 0.0) ? h : -h;
			while (true) {
				f2 = praxisflin(x2, j, n, x, v, qa, qb, qc, qd0, qd1, q0, q1,
				        nf, method);
				if (k < nits && f2 > f0) {
					k++;
					if (f0 < f1[0] && x1[0] * x2 > 0.0)
						break;
					x2 = 0.5 * x2;
				} else {
					loop = false;
					break;
				}
			}
		}
		nl[0]++;
		if (f2 > fm)
			x2 = xm;
		else
			fm = f2;
		d2[0] = (Math.abs(x2 * (x2 - x1[0])) > small) ? ((x2 * (f1[0] - f0) - x1[0]
		        * (fm - f0)) / (x1[0] * x2 * (x1[0] - x2)))
		        : ((k > 0) ? 0.0 : d2[0]);
		if (d2[0] <= small)
			d2[0] = small;
		x1[0] = x2;
		fx[0] = fm;
		if (sf1 < fx[0]) {
			fx[0] = sf1;
			x1[0] = sx1;
		}
		if (j > 0)
			AP_Basic.elmveccol(1, n, j, x, v, x1[0]);
	}

	static private double praxisflin(double l, int j, int n, double x[],
	        double v[][], double qa[], double qb[], double[] qc, double qd0,
	        double qd1, double q0[], double q1[], int nf[],
	        AP_praxis_method method) {
		/* this procedure is internally used by PRAXISMIN */
		int i;
		double result;
		double t[] = new double[n + 1];
		if (j > 0)
			for (i = 1; i <= n; i++)
				t[i] = x[i] + l * v[i][j];
		else {
			/* search along parabolic space curve */
			qa[0] = l * (l - qd1) / (qd0 * (qd0 + qd1));
			qb[0] = (l + qd0) * (qd1 - l) / (qd0 * qd1);
			qc[0] = l * (l + qd0) / (qd1 * (qd0 + qd1));
			for (i = 1; i <= n; i++)
				t[i] = qa[0] * q0[i] + qb[0] * x[i] + qc[0] * q1[i];
		}
		nf[0]++;
		result = method.funct(n, t);
		return result;
	}


}
