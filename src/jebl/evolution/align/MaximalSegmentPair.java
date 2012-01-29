package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

/**
 * @author Alexei Drummond
 *
 * @version $Id: MaximalSegmentPair.java 185 2006-01-23 23:03:18Z rambaut $
 */
public class MaximalSegmentPair extends AlignSimple {
	
	public MaximalSegmentPair(Scores sub) {
		super(sub, Integer.MAX_VALUE);
	}
    
	/**
	 * @param sq1
	 * @param sq2
	 */
    public final void doAlignment(String sq1, String sq2) {
    	
    	super.prepareAlignment(sq1, sq2);

        int n = this.n, m = this.m;
        float[][] score = sub.score;
        int maxi = n, maxj = m;
        float maxval = Float.NEGATIVE_INFINITY;
        float val;
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=m; j++) {
                float s = score[seq1.charAt(i-1)][seq2.charAt(j-1)];
                val = max(0.0f, F[i-1][j-1]+s);
                F[i][j] = val;
                if (val == 0.0f) {
                    B[i][j].setTraceback(-1,-1);
                } else {
                    B[i][j].setTraceback(i-1, j-1);
                }
                if (val > maxval) {
                    maxval = val;
                    maxi = i; maxj = j;
                }
            }
        }
        B0 = new TracebackSimple(maxi, maxj);
    }

    public final Traceback next(Traceback tb) {

        Traceback next = super.next(tb);
        if (next != null) {
            if (next.i - tb.i != next.j - tb.j) return null;
        }
        return next;
    }
}
