package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;


/**
 * Alignment with affine gap costs
 */

abstract class AlignAffine extends Align {

    float e;                        	// gap extension cost
    float[][][] F = null;               // the matrices used to compute the alignment
    TracebackAffine[][][] B = null;     // the traceback matrix
    private int oldn = 0;
    private int oldm = 0;

    public AlignAffine(Scores sub, float openGapPenalty, float extendGapPenalty) {
        super(sub, openGapPenalty);
        setGapExtend(extendGapPenalty);
    }

    /**
     * Performs the alignment. Abstract.
     * 
     * @param sq1
     * @param sq2
     */
    public abstract void doAlignment(String sq1, String sq2);

    public void prepareAlignment(String sq1, String sq2) {

        n = sq1.length();
        m = sq2.length();
        this.seq1 = sq1;
        this.seq2 = sq2;

        //first time running this alignment. Create all new matrices.
        if(F == null) {
            F = new float[3][n+1][m+1];
            B = new TracebackAffine[3][n+1][m+1];
            for(int k = 0; k < 3; k++) {
                for(int i = 0; i < n+1; i ++) {
                    for(int j = 0; j < m+1; j++)
                        B[k][i][j] = new TracebackAffine(0,0,0);
                }
            }
            oldn = n;
            oldm = m;
        }

        //alignment already been run but matrices not big enough for new alignment.
        //create all new matrices.
        else if(sq1.length() > oldn || sq2.length() > oldm) {
            int extram = 5;
            int extran = 5;
//            System.out.println ("creating new arrays "+n+ "," +m+ " was " + oldn+ "," + oldm);
            F = new float[3][n+1+extran][m+1+extram];
            B = new TracebackAffine[3][n+1+extran][m+1+extram];
            for(int k = 0; k < 3; k++) {
                for(int i = 0; i < n+1+extran; i ++) {
                    for(int j = 0; j < m+1+extram;j++)
                        B[k][i][j] = new TracebackAffine(0,0,0);
                }
            }
            oldn = n + extran;
            oldm = m + extram;
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

        //traceback has reached the origin, therefore stop.
        if(tb3.i + tb3.j + B[tb3.k][tb3.i][tb3.j].i + B[tb3.k][tb3.i][tb3.j].j == 0)
            return null;

        else
            return B[tb3.k][tb3.i][tb3.j];
    }

    /**
     * @return score for this alignment
     */
    public float getScore() {
        return F[((TracebackAffine)B0).k][B0.i][B0.j];
    }

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

