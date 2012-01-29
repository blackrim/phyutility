package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

// Overlap matching (simple gap costs)

public class OverlapAlign extends AlignSimple {
	
	public OverlapAlign(Scores sub, float d) {
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
    // F[0][0..m] = F[0..n][0] = 0 by construction
    for (int i=1; i<=n; i++)
      for (int j=1; j<=m; j++) {
        float s = score[seq1.charAt(i-1)][seq2.charAt(j-1)];
        float val = max(F[i-1][j-1]+s, F[i-1][j]-d, F[i][j-1]-d);
        F[i][j] = val;
        if (val == F[i-1][j-1]+s)
          B[i][j].setTraceback(i-1, j-1);
        else if (val == F[i-1][j]-d)
          B[i][j].setTraceback(i-1, j);
        else if (val == F[i][j-1]-d)
          B[i][j].setTraceback(i, j-1);
        else
          throw new Error("RM 1");
      }
    // Find maximal score on right-hand and bottom borders
    int maxi = -1, maxj = -1;
    float maxval = Float.NEGATIVE_INFINITY;
    for (int i=0; i<=n; i++)
      if (maxval < F[i][m]) {
        maxi = i;
        maxval = F[i][m];
      }
    for (int j=0; j<=m; j++)
      if (maxval < F[n][j]) {
        maxj = j;
        maxval = F[n][j];
      }
    if (maxj != -1)             // the maximum score was F[n][maxj]
      B0 = new TracebackSimple(n, maxj);
    else                        // the maximum score was F[maxi][m]
      B0 = new TracebackSimple(maxi, m);
  }
}
