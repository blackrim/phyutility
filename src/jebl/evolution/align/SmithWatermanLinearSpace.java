package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

public class SmithWatermanLinearSpace extends AlignLinearSpace {

    TracebackSimple[][] start = null; 	// Best alignment ending at (i,j) begins at start[i][j]
    float maxval;           	// Score of best alignment
    int start1, start2;   		// Best alignment begins at (start1, start2)
    int end1, end2;       		// Best alignment ends at (end1, end2)
    
    public SmithWatermanLinearSpace(Scores sub, float d) {
    	super(sub, d);
    }
    
    /**
	 * @param sq1
	 * @param sq2
	 */
    public void doAlignment(String sq1, String sq2) {
    	
    	super.prepareAlignment(sq1, sq2);
    	
        int n = this.n, m = this.m;
        float[][] score = sub.score;
        start = new TracebackSimple[2][sq2.length() + 1];
        
        char[] s1 = sq1.toCharArray();
        char[] s2 = sq2.toCharArray();

        maxval = Float.NEGATIVE_INFINITY;
        // Initialize first column (i=0):
        for (int j=0; j<=m; j++) {
            start[1][j] = new TracebackSimple(0, j);
        }
        for (int i=1; i<=n; i++) {
            swap01(F);
            swap01(start);
            // F[1] represents (new) column i and F[0] represents (old) column i-1
            // Initialize first row (j=0):
            start[1][0] = new TracebackSimple(i, 0);
            for (int j=1; j<=m; j++) {
                float s = score[s1[i-1]][s2[j-1]];
                float val = max(0, F[0][j-1]+s, F[0][j]-d, F[1][j-1]-d);
                F[1][j] = val;
                if (val == 0) {          // Best alignment starts (and ends) here
                    start[1][j] = new TracebackSimple(i, j);
                } else if (val == F[0][j-1]+s) {
                    start[1][j] = start[0][j-1];
                } else if (val == F[0][j]-d) {
                    start[1][j] = start[0][j];
                } else if (val == F[1][j-1]-d) {
                    start[1][j] = start[1][j-1];
                } else {
                    throw new Error("SWSmart 1");
                }
                if (val > maxval) {
                    maxval = val;
                    TracebackSimple sij = start[1][j];
                    start1 = sij.i; start2 = sij.j;
                    end1 = i; end2 = j;
                }
            }
        }
    }

    /**
     * @return the score of the best alignment
     */
    public float getScore() { return maxval; }

    /**
     * @return two-element array containing an alignment with maximal score
     */
    public String[] getMatch() {
        String subseq1 = seq1.substring(start1, end1);
        String subseq2 = seq2.substring(start2, end2);
        // The optimal local alignment between seq1 and seq2 is the
        // optimal global alignment between subseq1 and subseq2:
        NeedlemanWunschLinearSpace nwls1 = new NeedlemanWunschLinearSpace(sub, d);
        nwls1.doAlignment(subseq1, subseq2);
        return nwls1.getMatch();
    }

    public void traceback(TracebackPlotter plotter) {
        String subseq1 = seq1.substring(start1, end1);
        String subseq2 = seq2.substring(start2, end2);
        // The optimal local alignment between seq1 and seq2 is the
        // optimal global alignment between subseq1 and subseq2:
        NeedlemanWunschLinearSpace nwls1 = new NeedlemanWunschLinearSpace(sub, d);
        nwls1.doAlignment(subseq1, subseq2);
        nwls1.traceback(plotter, start1, start2, seq1, seq2);
    }



}
