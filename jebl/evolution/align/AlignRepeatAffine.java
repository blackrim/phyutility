package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

abstract class AlignRepeatAffine extends AlignRepeat {

    float[][][] F = null;                    	// the matrix used to compute the alignment
    TracebackAffine[][][] B;             		// the traceback matrix
    float maxScore;
    float e;


    public AlignRepeatAffine(Scores sub, float d, float e, int T) {
        super(sub, d, T);
        setGapExtend(e);
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
            F = new float[3][n+1][m+1];
            B = new TracebackAffine[3][n+1][m+1];
            for(int k = 0; k < 3; k++) {
                for(int i = 0; i < n+1; i ++) {
                    for(int j = 0; j < m+1; j++)
                        B[k][i][j] = new TracebackAffine(0,0,0);
                }
            }
        }

        //alignment already been run and existing matrix is big enough to reuse.
        else if(seq1.length() <= n && seq2.length() <= m) {
            this.n = sq1.length(); this.m = sq2.length();
            this.seq1 = strip(sq1); this.seq2 = strip(sq2);
        }

        //alignment already been run but matrices not big enough for new alignment.
        //create all new matrices.
        else {
            this.n = sq1.length(); this.m = sq2.length();
            this.seq1 = strip(sq1); this.seq2 = strip(sq2);
            F = new float[3][n+1][m+1];
            B = new TracebackAffine[3][n+1][m+1];
            for(int k = 0; k < 3; k++) {
                for(int i = 0; i < n+1; i ++) {
                    for(int j = 0; j < m+1; j++)
                        B[k][i][j] = new TracebackAffine(0,0,0);
                }
            }
        }
    }

    public void setGapExtend(float e) {
        this.e = e;
    }

    /**
     * Get the next state in the traceback
     * 
     * @param tb current Traceback
     * @return next Traceback
     */
    public Traceback next(Traceback tb) {
        TracebackAffine tb3 = (TracebackAffine)tb;
        if(tb3.i + tb3.j + B[tb3.k][tb3.i][tb3.j].i + B[tb3.k][tb3.i][tb3.j].j == 0)
            return null;	//traceback has reached origin therefore stop.
        else
            return B[tb3.k][tb3.i][tb3.j];
    }

    /**
     * @return the score of the best alignment
     */
    public float getScore() { return F[((TracebackAffine)B0).k][B0.i][B0.j]; }

    /**
     * Print matrix used to calculate this alignment.
     * 
     * @param out Output to print to.
     */
    public void printf(Output out) {

        for (int k=0; k<3; k++) {
            out.println("F[" + k + "]:");
            for (int j=0; j<=m; j++) {
                for (int i=0; i<F[k].length; i++) {
                    out.print(padLeft(formatScore(F[k][i][j]), 5));
                }
                out.println();
            }
        }
    }
}