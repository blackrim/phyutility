package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

// Alignment with affine gap costs; smart linear-space algorithm

abstract class AlignLinearSpaceAffine extends AlignAffine {

    float[][][] F;  // the matrices used to compute the alignment
    
    public AlignLinearSpaceAffine(Scores sub, float openGapPenalty, float extendGapPenalty) {
    	super(sub, openGapPenalty, extendGapPenalty);
    }
    
    /**
     * Performs the alignment. Abstract.
     * 
     * @param sq1
     * @param sq2
     */
    public abstract void doAlignment(String sq1, String sq2);
    
    public void prepareAlignment(String sq1, String sq2) {
    	this.n = sq1.length(); this.m = sq2.length();
		this.seq1 = sq1;
        this.seq2 = sq2;
    	F = new float[3][2][m+1];
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
    			for (int i=0; i<F[k].length; i++)
    				out.print(padLeft(formatScore(F[k][i][j]), 5));
    			out.println();
    		}
    	}
    }

    static void swap01(Object[] A)
    { Object tmp = A[1]; A[1] = A[0]; A[0] = tmp; }
}