package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;
import jebl.util.ProgressListener;

public class SmithWaterman extends AlignSimple {

    public SmithWaterman(Scores sub, float d) {
        super(sub, d);
    }

    /**
     * @param sq1
     * @param sq2
     * @param progress
     */
    public void doAlignment(String sq1, String sq2, ProgressListener progress) {

        super.prepareAlignment(sq1, sq2);

        char[] s1 = sq1.toCharArray();
        char[] s2 = sq2.toCharArray();

        int n = this.n, m = this.m;
        float[][] score = sub.score;
        int maxi = n, maxj = m;
        float maxval = Float.NEGATIVE_INFINITY;
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=m; j++) {
                float s = score[s1[i-1]][s2[j-1]];
                float val = max(0, F[i-1][j-1]+s, F[i-1][j]-d, F[i][j-1]-d);
                F[i][j] = val;
                if (val == 0) {
                    B[i][j].setTraceback(-1,-1);
                } else if (val == F[i-1][j-1]+s) {
                    B[i][j].setTraceback(i-1, j-1);
                } else if (val == F[i-1][j]-d) {
                    B[i][j].setTraceback(i-1, j);
                } else if (val == F[i][j-1]-d) {
                    B[i][j].setTraceback(i, j-1);
                } else {
                    throw new Error("Error in SmithWaterman alignment.");
                }
                if (val > maxval) {
                    maxval = val;
                    maxi = i; maxj = j;
                }
            }
        }
        B0 = new TracebackSimple(maxi, maxj);
    }

    public void doAlignment(String sq1, String sq2) {
        doAlignment(sq1, sq2, null);
    }
}
