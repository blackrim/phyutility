package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

abstract class AlignSimple extends Align {

    float[][] F = null;                 // the matrix used to compute the alignment
    TracebackSimple[][] B;             	// the traceback matrix

    public AlignSimple(Scores sub, float d) {
        super(sub, d);
    }

    /**
     * Performs the alignment. Abstract.
     * 
     * @param sq1
     * @param sq2
     */
    public abstract void doAlignment(String sq1, String sq2);

    public void prepareAlignment(String sq1, String sq2) {

        //first time running this alignment. Create all new matrices.
        if(F == null) {
            this.n = sq1.length(); this.m = sq2.length();
            this.seq1 = strip(sq1); this.seq2 = strip(sq2);
            F = new float[n+1][m+1];
            B = new TracebackSimple[n+1][m+1];
            for(int i = 0; i < n+1; i ++) {
                for(int j = 0; j < m+1; j++)
                    B[i][j] = new TracebackSimple(0,0);
            }
        }

        //alignment already been run and existing matrix is big enough to reuse.
        else if(seq1.length() <= this.n && seq2.length() <= this.m) {
            this.n = sq1.length(); this.m = sq2.length();
            this.seq1 = strip(sq1); this.seq2 = strip(sq2);
        }

        //alignment already been run but matrices not big enough for new alignment.
        //create all new matrices.
        else {
            this.n = sq1.length(); this.m = sq2.length();
            this.seq1 = strip(sq1); this.seq2 = strip(sq2);
            F = new float[n+1][m+1];
            B = new TracebackSimple[n+1][m+1];
            for(int i = 0; i < n+1; i ++) {
                for(int j = 0; j < m+1; j++)
                    B[i][j] = new TracebackSimple(0,0);
            }
        }
    }

    /**
     * Get the next state in the traceback
     * 
     * @param tb current Traceback
     * @return next Traceback
     */
    public Traceback next(Traceback tb) {
        TracebackSimple tb2 = (TracebackSimple)tb;
        if((tb.i == 0 && tb.j == 0 && B[tb2.i][tb2.j].i == 0 && B[tb2.i][tb2.j].j == 0) || tb.i == -1)
            return null;
        else
            return B[tb2.i][tb2.j];
    }

    /**
     * @return the score of the best alignment
     */
    public float getScore() { return F[B0.i][B0.j]; }

    /**
     * Print matrix used to calculate this alignment.
     * 
     * @param out Output to print to.
     */
    public void printf(Output out) {

        for (int j=0; j<=m; j++) {
            for (int i = 0; i < F.length; i++) {
                float[] f = F[i];
                out.print(padLeft(formatScore(f[j]), 5));
            }
            out.println();
        }
    }
}