package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexei Drummond
 *
 * @version $Id: NeedlemanWunschLinearSpace.java 186 2006-01-24 00:41:22Z pepster $
 */
public class NeedlemanWunschLinearSpace extends AlignLinearSpace {

    int u;     // Halfway through seq1
    int[][] c; // Best alignment from (0,0) to (i,j) passes through (u, c[i][j])

    public NeedlemanWunschLinearSpace(Scores sub, float d) {
        super(sub, d);
    }

    /**
     * @param sq1
     * @param sq2
     */
    public void doAlignment(String sq1, String sq2) {

        super.prepareAlignment(sq1, sq2);

        char[] s1 = sq1.toCharArray();
        char[] s2 = sq2.toCharArray();

        int n = this.n, m = this.m;
        u = n/2;
        c = new int[2][m+1];
        float[][] score = sub.score;
        for (int j=0; j<=m; j++) {
            F[1][j] = -d * j;
        }
        float s, val;
        for (int i=1; i<=n; i++) {
            swap01(F); swap01(c);
            // F[1] represents (new) column i and F[0] represents (old) column i-1
            F[1][0] = -d * i;
            for (int j=1; j<=m; j++) {
                s = score[s1[i-1]][s2[j-1]];
                val = max(F[0][j-1]+s, F[0][j]-d, F[1][j-1]-d);
                F[1][j] = val;
                if (i == u) {
                    c[1][j] = j;
                } else {
                    if (val == F[0][j-1]+s) {
                        c[1][j] = c[0][j-1];
                    } else if (val == F[0][j]-d) {
                        c[1][j] = c[0][j];
                    } else if (val == F[1][j-1]-d) {
                        c[1][j] = c[1][j-1];
                    } else {
                        throw new Error("NWSmart 1");
                    }
                }
            }
        }
    }

    public int getV() { return c[1][m]; }

    public String[] getMatch() {
        int v = getV();
        if (n > 1 && m > 1) {
            NeedlemanWunschLinearSpace al1, al2;
            al1 = new NeedlemanWunschLinearSpace(sub, d);
            al1.doAlignment(seq1.substring(0, u), seq2.substring(0, v));
            al2 = new NeedlemanWunschLinearSpace(sub, d);
            al2.doAlignment(seq1.substring(u),    seq2.substring(v));
            String[] match1 = al1.getMatch();
            String[] match2 = al2.getMatch();
            return new String[] { match1[0] + match2[0], match1[1] + match2[1] };
        } else {
            NeedlemanWunsch al = new NeedlemanWunsch(sub, d);
            al.doAlignment(seq1, seq2);
            return al.getMatch();
        }
    }

    public void traceback(TracebackPlotter plotter) {

        traceback(plotter, 0, 0, seq1, seq2);
    }

    public void traceback(TracebackPlotter plotter, int startx, int starty, String sq1, String sq2) {

        List tracebacks = tracebackList(startx,starty);

        plotter.newTraceBack(sq1, sq2);

        for (int i = 0; i < tracebacks.size(); i++) {
            Traceback traceback = (Traceback)tracebacks.get(i);
            plotter.traceBack((Traceback)tracebacks.get(i));
        }
        plotter.finishedTraceBack();
    }


    private List tracebackList(int startx, int starty) {

        List tracebacks = new ArrayList();

        int v = getV();
        if (n > 1 && m > 1) {
            NeedlemanWunschLinearSpace al1, al2;
            al1 = new NeedlemanWunschLinearSpace(sub, d);
            al1.doAlignment(seq1.substring(0, u), seq2.substring(0, v));
            al2 = new NeedlemanWunschLinearSpace(sub, d);
            al2.doAlignment(seq1.substring(u), seq2.substring(v));
            List tracebackList1 = al1.tracebackList(startx, starty);
            List tracebackList2 = al2.tracebackList(startx+u,starty+v);
            tracebackList1.addAll(tracebackList2);
            return  tracebackList1;
        } else {
            NeedlemanWunsch al = new NeedlemanWunsch(sub, d);
            al.doAlignment(seq1, seq2);
            return al.tracebackList(startx, starty);
        }

    }



    public float getScore() { return F[1][m]; }
}
