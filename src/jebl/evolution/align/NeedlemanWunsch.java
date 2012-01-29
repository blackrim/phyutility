package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

import java.util.ArrayList;
import java.util.List;

public class NeedlemanWunsch extends AlignSimple {

    private int prev = 0;
    private int curr = 1;
    private float maxScore = 0;

    public NeedlemanWunsch(Scores sub, float d) {
        super(sub, d);
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

        F[curr][0] = -d;
        for (int i=1; i<=n; i++) {
            B[i][0].setTraceback(i-1, 0);
        }
        for (int j=1; j<=m; j++) {
            F[prev][j] = -d * j;
            B[0][j].setTraceback(0, j-1);
        }
        float s, val;
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=m; j++) {
                s = score[s1[i-1]][s2[j-1]];
                val = max(F[prev][j-1]+s, F[prev][j]-d, F[curr][j-1]-d);
                F[curr][j] = val;
                if (val == F[prev][j-1]+s) {
                    B[i][j].setTraceback(i-1, j-1);
                } else if (val == F[prev][j]-d) {
                    B[i][j].setTraceback(i-1, j);
                } else if (val == F[curr][j-1]-d) {
                    B[i][j].setTraceback(i, j-1);
                } else {
                    throw new Error("Error in Needleman-Wunch pairwise alignment.");
                }
            }
            int temp = prev;
            prev = curr;
            curr = temp;
            F[curr][0] = -d * (i + 1);
        }
        B0 = new TracebackSimple(n, m);
        maxScore = F[curr][m];
    }

    List tracebackList(int startx, int starty) {

        List<TracebackSimple> tracebacks = new ArrayList<TracebackSimple>();

        Traceback tb = B0;
        while (tb != null) {
            tracebacks.add(0, new TracebackSimple(tb.i+startx,  tb.j+starty));
            tb = next(tb);
        }

        return tracebacks;
    }


    /**
     * @return the score of the best alignment
     */
    public float getScore() { return maxScore; }
}
