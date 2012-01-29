package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

// Global alignment using the Needleman-Wunsch algorithm (affine gap costs)

public class OldNeedlemanWunschAffine extends AlignAffine {

	public OldNeedlemanWunschAffine(Scores sub, float d, float e) {
		super(sub, d, e);
	}

	/**
	 * @param sq1
	 * @param sq2
	 */
    public void doAlignment(String sq1, String sq2) {

    	prepareAlignment(sq1, sq2);

        char[] s1 = sq1.toCharArray();
        char[] s2 = sq2.toCharArray();

        int n = this.n, m = this.m;
        float[][] score = sub.score;
        float[][] M = F[0], Ix = F[1], Iy = F[2];
        float val;
        float s, a, b, c;

        for (int i=1; i<=n; i++) {
            Ix[i][0] = -d - e * (i-1);
            B[1][i][0].setTraceback(1, i-1, 0);
        }

        for (int i=1; i<=n; i++) {
            Iy[i][0] = M[i][0] = Float.NEGATIVE_INFINITY;
        }

        for (int j=1; j<=m; j++) {
            Iy[0][j] = -d - e * (j-1);
            B[2][0][j].setTraceback(2, 0, j-1);
        }

        for (int j=1; j<=m; j++) {
            Ix[0][j] = M[0][j] = Float.NEGATIVE_INFINITY;
        }

        for (int i=1; i<=n; i++) {

            for (int j=1; j<=m; j++) {
                s = score[s1[i-1]][s2[j-1]];
                a = M[i-1][j-1]+s;
                b = Ix[i-1][j-1]+s;
                c = Iy[i-1][j-1]+s;

                val = M[i][j] = max(a, b, c);
                if (val == a) {
                    B[0][i][j].setTraceback(0, i-1, j-1);
                } else if (val == b) {
                    B[0][i][j].setTraceback(1, i-1, j-1);
                } else if (val == c) {
                    B[0][i][j].setTraceback(2, i-1, j-1);
                } else {
                    throw new Error("NWAffine 1");
                }

                a = M[i-1][j]-d;
                b = Ix[i-1][j]-e;
                c = Iy[i-1][j]-d;
                val = Ix[i][j] = max(a, b, c);

                if (val == a) {
                    B[1][i][j].setTraceback(0, i-1, j);
                } else if (val == b) {
                    B[1][i][j].setTraceback(1, i-1, j);
                } else if (val == c) {
                    B[1][i][j].setTraceback(2, i-1, j);
                } else {
                    throw new Error("NWAffine 2");
                }

                a = M[i][j-1]-d;
                b = Iy[i][j-1]-e;
                c = Ix[i][j-1]-d;
                val = Iy[i][j] = max(a, b, c);
                if (val == a) {
                    B[2][i][j].setTraceback(0, i, j-1);
                } else if (val == b) {
                    B[2][i][j].setTraceback(2, i, j-1);
                } else if (val == c) {
                    B[2][i][j].setTraceback(1, i, j-1);
                } else {
                    throw new Error("NWAffine 3");
                }
            }
        }
        // Find maximal score
        int maxk = 0;
        float maxval = F[0][n][m];
        for (int k=1; k<3; k++) {
            if (maxval < F[k][n][m]) {
                maxval = F[k][n][m];
                maxk = k;
            }
        }
        B0 = new TracebackAffine(maxk, n, m);
    }
}
