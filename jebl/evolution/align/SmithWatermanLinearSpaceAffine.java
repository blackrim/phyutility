package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;
import jebl.util.ProgressListener;

/**
 * @author Alexei Drummond
 *
 * @version $Id: SmithWatermanLinearSpaceAffine.java 384 2006-07-17 07:17:39Z pepster $
 */
public class SmithWatermanLinearSpaceAffine extends AlignLinearSpaceAffine {

    TracebackSimple[][] start;	// Best alignment ending at (i,j) begins at start[i][j]
    float maxval;           	// Score of best alignment
    int start1, start2;   		// Best alignment begins at (start1, start2)
    int end1, end2;       		// Best alignment ends at (end1, end2)

    public SmithWatermanLinearSpaceAffine(Scores sub, float d, float e) {
        super(sub, d, e);
    }

    /**
     * @param sq1
     * @param sq2
     */
    public void doAlignment(String sq1, String sq2, ProgressListener progress) {

        prepareAlignment(sq1, sq2);

        char[] s1 = sq1.toCharArray();
        char[] s2 = sq2.toCharArray();

        int n = this.n, m = this.m;
        float[][] score = sub.score;
        float[][] M = F[0], Ix = F[1], Iy = F[2];
        start = new TracebackSimple[2][m+1];
        maxval = Float.NEGATIVE_INFINITY;
        // Initialize first column (i=0); score is zero:
        for (int j=0; j<=m; j++) {
            start[1][j] = new TracebackSimple(0, j);
        }


        for (int i=1; i<=n; i++) {
            if( progress != null && progress.setProgress((double)i/n) ) {
               return; 
            }

            swap01(M); swap01(Ix); swap01(Iy); swap01(start);
            // F[k][1] represents (new) col i and F[k][0] represents (old) col i-1
            // Initialize first row (j=0):
            start[1][0] = new TracebackSimple(i, 0);
            for (int j=1; j<=m; j++) {
                float s = score[s1[i-1]][s2[j-1]];
                float val, valm, valix, valiy;
                valm  = M[1][j]  = max(0, M[0][j-1]+s, Ix[0][j-1]+s, Iy[0][j-1]+s);
                valix = Ix[1][j] = max(M[0][j]-d, Ix[0][j]-e);
                valiy = Iy[1][j] = max(M[1][j-1]-d, Iy[1][j-1]-e);

                val = max(valm, valix, valiy);
                if (val == 0) {
                    start[1][j] = new TracebackSimple(i, j);
                } else if (val == valm) {
                    start[1][j] = start[0][j-1];
                } else if (val == valix) {
                    start[1][j] = start[0][j];
                } else if (val == valiy) {
                    start[1][j] = start[1][j-1];
                } else {
                    throw new Error("SWSmartAffine 1");
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


    public void doAlignment(String sequence1, String sequence2) {
        doAlignment(sequence1, sequence2, null);
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
        NeedlemanWunschLinearSpaceAffine nwa1 = new NeedlemanWunschLinearSpaceAffine(sub, d, e);
        nwa1.doAlignment(subseq1, subseq2);
        return nwa1.getMatch();
    }

    public void traceback(TracebackPlotter plotter) {

        String subseq1 = seq1.substring(start1, end1);
        String subseq2 = seq2.substring(start2, end2);

        // The optimal local alignment between seq1 and seq2 is the
        // optimal global alignment between subseq1 and subseq2:
        NeedlemanWunschAffine nwa1 = new NeedlemanWunschAffine(sub, d, e);
        nwa1.doAlignment(subseq1, subseq2);

        nwa1.traceback(plotter);
    }

}

