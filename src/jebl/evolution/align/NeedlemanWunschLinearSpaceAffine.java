package jebl.evolution.align;

import jebl.evolution.align.scores.NucleotideScores;
import jebl.evolution.align.scores.Scores;
import jebl.evolution.align.scores.ScoresFactory;
import jebl.evolution.alignments.BasicAlignment;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceTester;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;

// Global alignment using the Needleman-Wunsch algorithm (affine gap costs)
// uses linear space.

public class NeedlemanWunschLinearSpaceAffine extends AlignLinearSpaceAffine implements PairwiseAligner {
    private float resultScore;
    static final int RECURSION_THRESHOLD = 6;
    private boolean debug = false;
    private boolean freeGapsAtEnds;

    public NeedlemanWunschLinearSpaceAffine(Scores sub, float openGapPenalty, float extendGapPenalty) {
        this(sub, openGapPenalty, extendGapPenalty, false);
    }

    
    public NeedlemanWunschLinearSpaceAffine(Scores sub, float d, float e, boolean freeGapsAtEnds) {
        this(sub, d, e,freeGapsAtEnds,false);
//        quadraticAlign = new NeedlemanWunschAffine(sub, d, e);
    }

    /**
     *
    * @param applyGapExtendCostToFirstGapResidue Generally there is an ambiguity in bioinformatics whether "gap opening" already includes the first gap character - in other words, whether a gap of length N has
     <pre>
     (a) a cost of gapOpen + N * gapExtend
     or
     (b) gapOpen + (N-1) * gapExtend.
     </pre>.
     <code>applyGapExtendCostToFirstGapResidue</code> should be true if using interpretation (a).


     * @param sub
     * @param d
     * @param e
     * @param freeGapsAtEnds
     */
    public NeedlemanWunschLinearSpaceAffine(Scores sub, float d, float e, boolean freeGapsAtEnds, boolean applyGapExtendCostToFirstGapResidue) {
        super(Scores.includeAdditionalCharacters(sub,"_"), d+ (applyGapExtendCostToFirstGapResidue?e:0), e);
        this.freeGapsAtEnds = freeGapsAtEnds;
//        quadraticAlign = new NeedlemanWunschAffine(sub, d, e);
    }

//    private NeedlemanWunschAffine quadraticAlign;//we use the quadratic
    //  algorithm to calculate the alignment as the base recursion case.
    String[] matchResult;
    private static final int TYPE_ANY = 0;
    private static final int TYPE_X = 1;
    private static final int TYPE_Y = 2;
    private int C[][][];
    private int Ctype[][][];
    private int previousm = -1, previousn = -1;

    private ProgressListener progress;
    private long totalProgress;
    private long currentProgress;
    private boolean cancelled;

    int[][][] Bi, Bj, Bk;
    private int allocatedn = -1;
    private int allocatedm = -1;

    public void allocateMatrices(int n, int m) {
        //first time running this alignment. Create all new matrices.
        if (n > allocatedn || m > allocatedm) {
            n = maxi(n, allocatedn + 5);
            m = maxi(m, allocatedm + 5);
            allocatedn = n;
            allocatedm = m;
            Bi = new int[3][n + 1][m + 1];
            Bj = new int[3][n + 1][m + 1];
            Bk = new int[3][n + 1][m + 1];
        }

    }

    private int convertType(int type) {
        if (type == TYPE_ANY) return type;
        if (type == TYPE_X) return TYPE_Y;
        if (type == TYPE_Y) return TYPE_X;
        throw new RuntimeException("invalid type");
    }

    private boolean addProgress(long value) {
        currentProgress += value;
        if (progress != null) {
            double fraction = ((double) currentProgress) / totalProgress;
            cancelled = progress.setProgress(fraction);
        }
        return cancelled;
    }


    public void doAlignment(String sq1, String sq2) {
        doAlignment(sq1, sq2, null);
    }

//    private AlignmentResult result1, result2;

    public void doAlignment(String sq1, String sq2, ProgressListener progress, boolean scoreOnly) {
        this.progress = progress;

        sq1 = strip(sq1);
        sq2 = strip(sq2);
//        prepareAlignment (sq1,sq2);
        //we initialise the following arrays here rather than in prepareAlignment
        //so that we do not have to create them again during recursion.
        Profile profile1 = new Profile(0, sq1);
        Profile profile2 = new Profile(0, sq2);
        AlignmentResult[] results = doAlignment(profile1, profile2, progress, scoreOnly);
        matchResult = new String[2];
        if (cancelled) return;
        matchResult[0] = Profile.buildAlignmentString(sq1, results[0]);
        matchResult[1] = Profile.buildAlignmentString(sq2, results[1]);
    }

    public void doAlignment(String sq1, String sq2, ProgressListener progress) {
        doAlignment(sq1, sq2, progress, false);
    }

    // todo: return null when progress canceled, and document and handle this behaviour
    public AlignmentResult[] doAlignment(Profile profile1, Profile profile2,
                                         ProgressListener progress, boolean scoreOnly) {
        this.progress = progress;
        if (freeGapsAtEnds && (profile1.getNumberOfSequences()>1 || profile2.getNumberOfSequences()>1)) {
            profile1 = profile1.supportFreeEndGaps();
            profile2 = profile2.supportFreeEndGaps();
        }
        this.n = profile1.length();
        this.m = profile2.length();
//        System.out.println("aligning " + n + "," + m);
        if (n > previousn || m > previousm) {
            int maximum = Math.max(m,n);//would normally use "m", but "invert = true;" later on requires taking the maximum.

            F = new float[3][2][ maximum + 1];
            C = new int [3] [ 2] [maximum + 1];
            Ctype = new int [3] [3] [maximum + 1];
            previousn = n;
            previousm = m;
        }
        totalProgress = ((long) n) * m * 2;
//        System.out.println("total =" + totalProgress + "," +n+ "," +m);
        currentProgress = 0;
        cancelled = false;
        int maximumResultLength = m + n;
        AlignmentResult result1 = new AlignmentResult(maximumResultLength);
        AlignmentResult result2 = new AlignmentResult(maximumResultLength);
        resultScore = doAlignment(profile1, profile2, 0, 0, n, m, TYPE_ANY, TYPE_ANY, result1, result2, scoreOnly, freeGapsAtEnds, freeGapsAtEnds);
        return new AlignmentResult[]{result1, result2};
    }

    public String[] getMatch() {
        return matchResult;
    }

    private static float gapFraction(ProfileCharacter character) {
        float result = character.gapFraction();
        //assert result < 1.0; //should not be calling this function on a profile that contains all gap Characters at one location.
        // We do want to support stand-alone sequences with gaps. And it is possible for an extracted alignment to have all gaps. So this assertion has been removed. 
        return result;
    }

    private float doAlignment(Profile profile1, Profile profile2,
                              int offset1, int offset2, int n, int m, int startType, int endType,
                              AlignmentResult result1, AlignmentResult result2, boolean scoreOnly, boolean freeStartGap, boolean freeEndGap) {
        this.n = n;
        this.m = m;

        boolean gapCostProduction = true;
//        int n = this.n, m = this.m;
//        float[][] score = sub.score;
        float[][] M = F[0], Ix = F[1], Iy = F[2];
        int[][] cm = C[0], cx = C[1], cy = C[2];
        int[][] cmtype = Ctype[0], cxtype = Ctype[1], cytype = Ctype[2];
        float val;
        float s, a, b, c;
        boolean calculateResults = false;
        boolean invert = false;
        if (debug) {

            System.out.println("start =" + startType+ ", end=" + endType+ " free =" + freeStartGap+ "," + freeEndGap);
            System.out.println("align from " + offset1 + " to " + (offset1 + n - 1) + " with from " + offset2 + " to " + (offset2 + m - 1));
            System.out.println("s1=" + profile1.toString(offset1, n));
            System.out.println("s2=" + profile2.toString(offset2, m));
        }

        if (n < RECURSION_THRESHOLD || m < RECURSION_THRESHOLD) {
            calculateResults = true;
            if (n > m) {
                //swap the ordering, to prevent nasty allocation of matrices.
                // for example, if we do a 100000 by 10 alignment, followed by a 10 x 100000 alignment, we end up
                // allocating a 100000 x 100000 matrix
//                System.out.println("invert = true at " + offset1+","+offset2+ "," +n+ "," +m);
                invert = true;
                int temp;
                temp = m;
                m = n;
                n = temp;
                temp = offset1;
                offset1 = offset2;
                offset2 = temp;
                startType = convertType(startType);
                endType = convertType(endType);
                Profile tempSequence;
                tempSequence = profile1;
                profile1 = profile2;
                profile2 = tempSequence;

            }
            allocateMatrices(n, m);
        }

        int u = n / 2;

        if (debug) {
            System.out.println(" u=" + u);
        }

        //all that the remainder of this function does is to calculate the midpoint
        //(u,v) that the optimal alignment passes through, along with the
        //type of extension applied at the midpoint, in case it is the gap extension,
        //in which case the recursion functions need to know that.

        if (calculateResults) {
            for (int i = 1; i <= n; i++) {
                Bk[1][i][0] = 1;
                Bi[1][i][0] = i - 1;
                Bj[1][i][0] = 0;
            }
        }
        //i corresponds to TYPE_X
        //j corresponds to TYPE_Y
        for (int j = 1; j <= m; j++) {
            float base = d;
            if (startType == TYPE_Y)
                base = e;//if startType IS TYPE_Y then we were already in a
            // gap, so we can use gap extension penalty rather than gap starting penalty
            Iy[0][j] = - base - e * (j - 1);
            if (freeStartGap) Iy[0][j] = 0;
            Ix[0][j] = M[0][j] = Float.NEGATIVE_INFINITY;
            cy[0][j] = 0;
            cytype[0][j] = TYPE_Y;
            if (calculateResults) {
                Bk[2][0][j] = 2;
                Bi[2][0][j] = 0;
                Bj[2][0][j] = j - 1;
            }
        }
        Ix[0][0] = Iy[0][0] = Float.NEGATIVE_INFINITY;
        M[0][0] = 0;
        cmtype[0][0] = 0;
        cm[0][0] = 0;

        swap01(Ix);
        swap01(Iy);
        swap01(M);
        swap01(cm);
        swap01(cx);
        swap01(cy);
        swap01(cmtype);
        swap01(cxtype);
        swap01(cytype);

        for (int i = 1; i <= n; i++) {
            swap01(Ix);
            swap01(Iy);
            swap01(M);
            swap01(cm);
            swap01(cx);
            swap01(cy);
            swap01(cmtype);
            swap01(cxtype);
            swap01(cytype);

            M[1][0] = Float.NEGATIVE_INFINITY;
            Iy[1][0] = Float.NEGATIVE_INFINITY;
            float base = d;
            if (startType == TYPE_X) base = e;
            Ix[1][0] = - base - e * (i - 1);
            if (freeStartGap) Ix[1][0] = 0;
            cxtype[1][0] = TYPE_X;
            cx[1][0] = 0;
            for (int j = 1; j <= m; j++) {
                if (cancelled) return 0;
                s = ProfileCharacter.score(profile1.profile[offset1 + i - 1], profile2.profile[offset2 + j - 1], sub);
                if (debug) {
                    System.out.println("loc=" + j + "," + i + " p1=" + profile1.profile[offset1 + i - 1] +
                            " p2=" + profile2.profile[offset2 + j - 1] + " score=" + s);
                }
                /* char c1= s1[i - 1];
              char c2= s2[j - 1];

              s = score[c1][c2];*/
                a = M[0][j - 1] + s;
                b = Ix[0][j - 1] + s;
                c = Iy[0][j - 1] + s;

                val = M[1][j] = max(a, b, c);
                if (calculateResults) {
                    int k = 0;
                    if (val == b) k = 1;
                    if (val == c) k = 2;
                    Bi[0][i][j] = i - 1;
                    Bj[0][i][j] = j - 1;
                    Bk[0][i][j] = k;
                }
                if (i == u) {
                    cm[1][j] = j;
                    cmtype[1][j] = TYPE_ANY;
                } else if (i > u) {
                    if (val == a) {
                        cm[1][j] = cm[0][j - 1];
                        cmtype[1][j] = cmtype[0][j - 1];
                    } else if (val == b) {
                        cm[1][j] = cx[0][j - 1];
                        cmtype[1][j] = cxtype[0][j - 1];
                    } else if (val == c) {
                        cm[1][j] = cy[0][j - 1];
                        cmtype[1][j] = cytype[0][j - 1];
                    } else {
                        throw new Error("NWAffine 1");
                    }
                }

                //introduce a gap by skipping a character in the i direction

                float xd = d;
                float xe = e;
                if (j == m && freeEndGap) {
                    xd = 0;
                    xe = 0;
                }
                float gapFraction = gapFraction(profile1.profile[offset1 + i - 1]);
                float ownGapFraction = gapFraction(profile2.profile[offset2 + j - 1]);
                if (gapCostProduction && gapFraction > 0) {
                    // if the other sequence that we are aligning a gap to
                    // already had some gaps in it, proportionally reduce the gap cost.
//                    xd= xd*(1-  gapFraction);
                    xd = xd - xe * gapFraction;
                    xe = xe * (1 - gapFraction);
                }
                if (ownGapFraction > 0) {
                    //if our own sequence already has some gaps following the proposed
                    // insertion of a gap at this point, then reduce the gap opening
                    // penalty to a point such that if the sequence contained entirely Characters
                    // at the next position (which is impossible) then the
                    // gap opening cost would reduce to the same as the gap Extension cost
//                    xd= xe+(xd-xe)*(1- ownGapFraction);
                }
                a = M[0][j] - xd;
                b = Ix[0][j] - xe;
                c = Iy[0][j] - xd;
                val = Ix[1][j] = max(a, b, c);
                if (calculateResults) {
                    int k = 0;
                    if (val == b) k = 1;
                    if (val == c) k = 2;
                    Bi[1][i][j] = i - 1;
                    Bj[1][i][j] = j;
                    Bk[1][i][j] = k;
                }
                if (i == u) {
                    cx[1][j] = j;
                    cxtype[1][j] = TYPE_X;
                } else if (i > u) {
                    if (val == a) {
                        cx[1][j] = cm[0][j];
                        cxtype[1][j] = cmtype[0][j];
                    } else if (val == b) {
                        cx[1][j] = cx[0][j];
                        cxtype[1][j] = cxtype[0][j];
                    } else if (val == c) {
                        cx[1][j] = cy[0][j];
                        cxtype[1][j] = cytype[0][j];
                    } else {
                        throw new Error("NWAffine 2");
                    }
                }

                //introduce a gap by skipping a character in the j direction
                float yd = d;
                float ye = e;
                if (i == n && freeEndGap) {
                    yd = 0;
                    ye = 0;
                }
                ownGapFraction = gapFraction(profile1.profile[offset1 + i - 1]);
                gapFraction = gapFraction(profile2.profile[offset2 + j - 1]);
                if (gapCostProduction && gapFraction > 0) {
                    // if the other sequence that we are aligning a gap to
                    // already had some gaps in it, proportionally reduce the gap cost.
//                    yd = yd * (1 - gapFraction);
                    yd = yd - ye * gapFraction;
                    ye = ye * (1 - gapFraction);

                }
                if (ownGapFraction > 0) {
                    //if our own sequence already has some gaps following the proposed
                    // insertion of a gap at this point, then reduce the gap opening
                    // penalty to a point such that if the sequence contained entirely Characters
                    // at the next position (which is impossible) then the
                    // gap opening cost would reduce to the same as the gap Extension cost
//                    yd = ye + (yd - ye) * (1 - ownGapFraction);
                }
                a = M[1][j - 1] - yd;
                b = Iy[1][j - 1] - ye;
                c = Ix[1][j - 1] - yd;
                val = Iy[1][j] = max(a, b, c);
                if (calculateResults) {
                    int k = 0;
                    if (val == b) k = 2;
                    if (val == c) k = 1;
                    Bi[2][i][j] = i;
                    Bj[2][i][j] = j - 1;
                    Bk[2][i][j] = k;
                }
                if (i == u) {
                    cy[1][j] = j;
                    cytype[1][j] = TYPE_Y;
                } else if (i > u) {
                    if (val == a) {
                        cy[1][j] = cm[1][j - 1];
                        cytype[1][j] = cmtype[1][j - 1];
                    } else if (val == b) {
                        cy[1][j] = cy[1][j - 1];
                        cytype[1][j] = cytype[1][j - 1];
                    } else if (val == c) {
                        cy[1][j] = cx[1][j - 1];
                        cytype[1][j] = cxtype[1][j - 1];
                    } else {
                        throw new Error("NWAffine 3");
                    }
                }

            }
            if (addProgress(m)) return 0;
        }
        // Find maximal score
        int bestk = 0;
        for (int k = 1; k < 3; k++) {
            if (F[k][1][m] > F[bestk][1][m])
                bestk = k;
        }

        //if the alignment must end with a particular type, force that type to be selected:
        //System.out.println("end type ="+endType);
        if (endType == TYPE_X)
            bestk = 1;
        if (endType == TYPE_Y)
            bestk = 2;
        assert F[bestk][1][m]>Float.NEGATIVE_INFINITY;

        int v = C[bestk][1][m];
        int vtype = Ctype[bestk][1][m];
        if (debug) {
            System.out.println("bestk=" + bestk + " v=" + v + " vtype =" + vtype);
        }
        float finalScore = F[bestk][1][m];


        if (freeEndGap && n == 0) finalScore = 0;
        if (freeStartGap && n == 0) finalScore = 0;

        if (scoreOnly) return finalScore;

        if (calculateResults) {
            //System.out.println("append results " +n+ "," +m+ "," +bestk);
            appendResults(invert, result1, result2, n, m, bestk);
        } else {
            boolean propagateFreeEndGap = freeEndGap && (u == n || v == m);
            boolean propagateFreeStartGap = freeStartGap && (u == 0 || v == 0);
            float score1=doAlignment(profile1, profile2, offset1, offset2, u, v, startType, vtype, result1, result2, false, freeStartGap, propagateFreeEndGap);
            if (cancelled) return 0;
            float score2=doAlignment(profile1, profile2, offset1 + u, offset2 + v, n - u, m - v, vtype, endType, result1, result2, false, propagateFreeStartGap, freeEndGap);
            if (cancelled) return 0;
            float combinedScore = score1+ score2;
            if ( finalScore != 0 && Math.abs((combinedScore - finalScore)/finalScore) > 1e-5 ) {
                // multiple alignment scoring is not necessarily valid.
                if (profile1.sequenceCount==1 && profile2.sequenceCount==1) {
                    //todo: work out why this happens sometimes, possibly just cumulative floating point error.
                    System.out.println("free =" + freeStartGap+ "," + freeEndGap);
                    System.out.println("offset1="+ offset1+" offset2="+ offset2+" u="+u+" v="+v);
                    System.out.println(""+ score1+ "+" + score2+"!="+ finalScore);
                }
            }
        }
        //System.out.println("free =" +freeStartGap+ "," + freeEndGap+ ",score =" + finalScore);

        /* String sequence1a = sq1.substring(0, u);
   String sequence2a = sq2.substring(0, v);
   String[] match1= doAlignment(sequence1a,sequence2a,startType,vtype );
   if(cancelled) return null;
   float match1Score= getScore();
   String sequence1b = sq1.substring(u);
   String sequence2b = sq2.substring(v);
   String[] match2= doAlignment(sequence1b,sequence2b,vtype, endType );
   if (cancelled) return null;
   float match2Score = getScore();
   float combineScore = match1Score + match2Score;*/

        //I thought the following would be a good idea to test how well it is working,
        //  but in practice
        // the floatingpoint error builds up to exceed small amounts
        // even on my test caseof only a few hundred characters
        /*
        if (Math.abs(combineScore - resultScore)> 0.0001f) {
            System.out.println (sequence1a+ "+" + sequence1b);
            System.out.println (sequence2a+ "+" + sequence2b);

            String message = "final score doesn't match (" + match1Score + "+" + match2Score + "=" + (match2Score + match1Score)+ "!=" + resultScore + ")";
            System.out.println (message);
            System.out.println (match1[0]);
            System.out.println (match1[1]);
            System.out.println (match2[0]);
            System.out.println (match2[1]);
            NeedlemanWunschAffine align = new NeedlemanWunschAffine(sub, d, e);
            align.doAlignment(sq1, sq2, startType, endType);
            System.out.println ("score from quadratic algorithm =" +align.getScore());
            String[] match3=align.getMatch();
            System.out.println(match3[0]);
            System.out.println(match1[0] + match2[0]);
            System.out.println(match3[1]);
            System.out.println(match1[1]+match2[1]);


            throw new Error (message);
        }
        */
//        setScore (resultScore);

//        setScore (combineScore);
//        return new String[] {match1[0]+ match2[0], match1[1]+ match2[1]};
        return finalScore;
    }

    private void appendResults(boolean invert, AlignmentResult result1, AlignmentResult result2, int n, int m, int bestk) {

        StringBuilder res1 = new StringBuilder();
        StringBuilder res2 = new StringBuilder();
        int tbi, tbj, tbk;

        int i = n;
        int j = m;
        int k = bestk;
        while (i != 0 || j != 0) {
            tbi = Bi[k][i][j];
            tbj = Bj[k][i][j];
            tbk = Bk[k][i][j];

            if (i == tbi) {
                res1.append('-');
            } else {
                res1.append('X');
            }
            if (j == tbj) {
                res2.append('-');
            } else {
                res2.append('X');
            }
            i = tbi;
            j = tbj;
            k = tbk;
        }
        String string1 = res1.reverse().toString();
        String string2 = res2.reverse().toString();
//        System.out.println("string 1 =" + string1);
//        System.out.println("string 2 =" + string2);
        if (invert) {
            result1.append(string2);
            result2.append(string1);
        } else {
            result1.append(string1);
            result2.append(string2);
        }
//        result1.print ();
//        result2.print ();

    }

    @Override
    public float getScore() {
        return resultScore;
    }

    /* private void setScore(float resultScore) {
        this.resultScore=resultScore;
    }*/

    public static void main(String[] arguments) {
        Scores scores = ScoresFactory.generateScores("Blosum45");

        String sequence1 = SequenceTester.getTestSequence1(arguments);
        String sequence2 = SequenceTester.getTestSequence2(arguments);

        sequence1="GTGGCAA---------AAAACATTCAAGCCATTCGCGGCATGAACGATTACCTGCCTGGCGAA---------------------ACGGCCATCTGGCAGCGCATTGAAGGCACACTGAAAAACGTGCTCGGCAGCTACGGTTACAGTGAAATCCGCTTGCCGATTGTAGAGCAGACCCCGCTATTCAAACGTGCGATTGGTGAAGTCACCGACGTGGTTGAAAAAGAGATGTACACCTTTGAGGATCGCAATGGCGACAG---CCTGACTCTGCGCCCTGAAGGGACGGCGGGCTGTGTACGCGCCGGCATCGAGCATGGTCTTCTGTACAAT---CAGGAACAGCGTCTGTGGTATATCGGGCCGATGTTCCGTCACGAGCGTCCGCAGAAAGGGCGTTATCGTCAGTTCCATCAGTTGGGCTGCGAAGTTTTCGGTCTGCAAGGTCCGGATATCGACGCTGAACTGATTATGCTCACTGCCCGCTGGTGGCGCGCGCTGGGTATTTCCGAGCACGTAACTCTTGAGCTGAACTCTATCGGTTCGCTGGAAGCACGCGCCAATTACCGCGATGCGCTGGTGGCATTCCTTGAGCAGCATAAAGAAAAGCTGGACGAAGACTGCAAACGCCGCATGTACACTAACCCGCTGCGCGTGCTGGATTCAAAAAATCCGGAAGTGCAGGCGCTTCTCAACGACGCTCCGGCATTAGGTGACTATCTGGACGAGG------------------------AATCTCGTGAGCATTTTGCCGGTCTGTGCAAACTGCTGGAGAGCGCGGGGAT---------------------------------------CGCTTACACCGTAAACCAGCGTCTGGTGCGTGGTCTGGATTACTACAACCGTACCGTTTTCGAGTGGGTGACTAACAGTCTCGGCTCCCAGGGCACCGTGTGTGCAGGCGGTCGTTATGACGGTCTTGTGGAACAACTGGGCGGTCGTGCAACACCGGCTGTCGGTTTTGCTATGGGCCTCGAACGTCTTGTATTGTTAGTACAGGCCGTTAATCCG---GAATTTAAAGCCGATCCTGTTGTCGATATATACCTGGTGGCTTCAGGTGCTGATACACAATCTGCGGCTATGGCATTAGCTGAGCGTCTGCGTGATGAATTACCGGGCGTGAAATTGATGACCAACCACGGCGGCGGCAACTTTAAGAAACAGTTTGCCCGTGCTGATAAATGGGGTGCCCGCGTTGCTGTGGTGCTGGGTGAGTCTGAAGTGGCTAACGGCACAGCAGTAGTGAAGGATTTGCGCTCTGGTGAGCAAACGGCAGTTGCGCAGGATAGCGTAGCCGCGCATTTGC--------------GCACGTT-------------------------------------------------------------------------ACTGGGTTAA";
        sequence1 = "GTGGCAAAAAACATTCAAGCCATTCGCGGCA";//TGAACGATTACCTGCCTGGCGAAACGGCCATCTGGCAGCGCATTGAAGGCACACTGAAAAACGTGCTCGGCAGCTACGGTTACAGTGAAATCCGCTTGCCGATTGTAGAGCAGACCCCGCTATTCAAACGTGCGATTGGTGAAGTCACCGACGTGGTTGAAAAAGAGATGTACACCTTTGAGGATCGCAATGGCGACAGCCTGACTCTGCGCCCTGAAGGGACGGCGGGCTGTGTACGCGCCGGCATCGAGCATGGTCTTCTGTACAATCAGGAACAGCGTCTGTGGTATATCGGGCCGATGTTCCGTCACGAGCGTCCGCAGAAAGGGCGTTATCGTCAGTTCCATCAGTTGGGCTGCGAAGTTTTCGGTCTGCAAGGTCCGGATATCGACGCTGAACTGATTATGCTCACTGCCCGCTGGTGGCGCGCGCTGGGTATTTCCGAGCACGTAACTCTTGAGCTGAACTCTATCGGTTCGCTGGAAGCACGCGCCAATTACCGCGATGCGCTGGTGGCATTCCTTGAGCAGCATAAAGAAAAGCTGGACGAAGACTGCAAACGCCGCATGTACACTAACCCGCTGCGCGTGCTGGATTCAAAAAATCCGGAAGTGCAGGCGCTTCTCAACGACGCTCCGGCATTAGGTGACTATCTGGACGAGGAATCTCGTGAGCATTTTGCCGGTCTGTGCAAACTGCTGGAGAGCGCGGGGATCGCTTACACCGTAAACCAGCGTCTGGTGCGTGGTCTGGATTACTACAACCGTACCGTTTTCGAGTGGGTGACTAACAGTCTCGGCTCCCAGGGCACCGTGTGTGCAGGCGGTCGTTATGACGGTCTTGTGGAACAACTGGGCGGTCGTGCAACACCGGCTGTCGGTTTTGCTATGGGCCTCGAACGTCTTGTATTGTTAGTACAGGCCGTTAATCCGGAATTTAAAGCCGATCCTGTTGTCGATATATACCTGGTGGCTTCAGGTGCTGATACACAATCTGCGGCTATGGCATTAGCTGAGCGTCTGCGTGATGAATTACCGGGCGTGAAATTGATGACCAACCACGGCGGCGGCAACTTTAAGAAACAGTTTGCCCGTGCTGATAAATGGGGTGCCCGCGTTGCTGTGGTGCTGGGTGAGTCTGAAGTGGCTAACGGCACAGCAGTAGTGAAGGATTTGCGCTCTGGTGAGCAAACGGCAGTTGCGCAGGATAGCGTAGCCGCGCATTTGCGCACGTTACTGGGTTAA";
        sequence2="GCCTGTCGCCCGACAACATCATCCTGTCGTGCAAGGTCAGCAATGTGCAGGACCTGATCAGCGTC";//TACCGCGAGCTCGGCGGTCGCTGCGACTACCCGCTGCACCTGGGCCTGACCGAGGCCGGCATGGGCAGCAAGGGCATCGTCGCCTCCAGCGCCGCGCTGGCCGTGTTGTTGCAGGAAGGCATAGGCGACACCATCCGCATCTCGCTGACGCCGCAGCCGGGCGAGGCGCGCACCAAGGAGGTGGTGGTCGCGCAGGAGTTGCTGCAGACCATGGGCCTGCGCAGCTTCACGCCGCTGGTCACCGCCTGTCCGGGCTGCGGCCGCACCACCAGCACCTTCTTCCAGGAGCTGGCCGATCATATCCAGAGCTATCTGCGCGAGCGGATGCCGGTGTGGCGGCTGCAGTATCCGGGCGTCGAAGACATGAAGGTGGCGGTGATGGGCTGCGTGGTCAACGGTCCGGGCGAGTCCAAGCTGGCCGACATCGGCATTTCGTTGCCTGGCACCGGCGAAGTGCCGGTGGCGCCGGTCTACGTGGACGGCCAGAAGGATGTGACGCTGAAGGGCGATAACATTCCGGCGGAATTCACCGCCATCGTCGACAACTATGTGAAAACGCGTTATGGCGAGGGCGGGGCCAAGCGCCGCGAGGTCGCCAGCCGCACGATTCCGATCCGGCCGGTGAAGGCCTGACGGAAACAGAATCCAGCTGATACAAGAAGATAGCAATGGCTCAGAAATACCAAGCGGTCAAAGGCATGAACGATGTGCTGCCGGCCGAATCCTACCAGTGGGAATACTTTGAAGAGGCGCTGCGCAAGCTGCTGGCCGACTACGGCTACCAGAACATCCGCACCCCCATCGTCGAAGGCACGCCGCTGTTCGTGCGTTCCATCGGCGAAGTGACCGACATCGTCGAGAAGGAAATGTACACCTTCGTCGACAGCCTGAACGGCGACAGCCTGACGCTGCGCCCGGAAGGCACCGCCGGCACGCTGCGCGCGGTGGTCGAGCATAATCTGCTGTACAACGCCACGCCCAAGCTGTGGTACATGGGCCCGATGTACCGCCACGAGCGCCCGCAGAAGGGCCGCTACCGCCAATTCCACCAGGTGGGCGTGGAGGCGCTGGGCCTGGCCGGTCCGGACATCGACGCGGAAATCATCGCGATGACCGCCGACCTCTGGCGCCGCCTTGGCATCAGCCAGTATGTGCGGCTGGAGATCAACTCGCTGGGCAACGCCGAAGAGCGCGCCGCCCACCGCGAGGCGCTGATCGCCTACCTGGAACGCCACGTCGACATCCTGGACGAGGACGGCAAGCGCCGGATGCACACCAACCCGCTGCGCGTGCTGGACACCAAGAACCCGGCCTTGCAGGAAATGGCCAACGCCGCGCCCAAGCTGTCCGACTACCTGGGCGAGGAGTCGCGCGCCCATTACGAGGGCTGGAAGGCGATGATCGCCGCGCTGGGCATCGAGTACATCGAGAATCCGCGCCTGGTGCGCGGCCTCGACTACTACAACCGCTCGGTGTTCGAGTGGGTGACCTCCGAGCTGGGCGCCCAGGGCACCATCTGCGCCGGCGGCCGTTACGACGGCCTGATCGAGCAGCTGGGCGGCAAGGCCGCGTCGGGCATCGGCTTCGGCATGGGCATGGAGCGCGTGCTGCTGTTGCTGCAGGACAAGGGCCTGCTGCCGGCGCAGCGCAGCGTCGACGTGTTTCTGGTCAACCAGGGCGAGGGCGCCGGCCTGTACTCGATGAAACTGGCTCAAACCCTGCGCGCCGCCGGCTATTCGGTGGTGCAGCACCTGGGCGAGGCCAGCTTCAAGTCGCAGATGAAAAAGGCCGACGGCAGCGGCGCGGAATTCGCGCTGATCGTCGGCGAAAACGAAATCCAGGCCGGTCAGGTGGTGGTGAAGGCGCTGCGCGCCGACGTCGCCCAGCAGACCGTGGCGGCCGATGCGGTTCTGGCCACCCTCGCCACTCTGAAAGCTTGATAGGAAGGGGGAACGCGATGGCGTTCGACTTGCAGGAACAGGAACAGATCGATTCTCTGAAGGCATTCTGGCAGCAGTGGGGCAAGCTGATCGGCGGCGCCGTGCTGGCCGTCA";
        scores = new NucleotideScores(5,-4);

        float e = 0.1f;
        float d = 1.0f;

        System.out.println("aligning sequence of length " + sequence1.length() + " with sequence of length " + sequence2.length());


        long start;
        long end;
        final int repeat = 1;

        start = System.currentTimeMillis();
        String[] results2 = null, results = null, results3 = null;
        NeedlemanWunschAffine align2 = null;
        NeedlemanWunschLinearSpaceAffine align = null;
        OldNeedlemanWunschAffine align3 = null;

//        for (int i = 0; i < repeat; i++) {
//            align2 = new NeedlemanWunschAffine(scores, d, e);
//            align2.doAlignment(sequence1, sequence2);
//
//            results2 = align2.getMatch();
//        }
        end = System.currentTimeMillis();
        System.out.println("quadratic space took " + (end - start) + " milliseconds");

        start = System.currentTimeMillis();
        for (int i = 0; i < repeat; i++) {
            align = new NeedlemanWunschLinearSpaceAffine(scores, d, e);
            align.doAlignment(sequence1, sequence2);
            results = align.getMatch();
        }
        end = System.currentTimeMillis();
        System.out.println("linear space took " + (end - start) + " milliseconds");
//if(true)return;
        start = System.currentTimeMillis();
        for (int i = 0; i < repeat; i++) {
            align3 = new OldNeedlemanWunschAffine(scores, d, e);
            align3.doAlignment(sequence1, sequence2);

            results3 = align3.getMatch();
        }
        end = System.currentTimeMillis();
        System.out.println("old quadratic space took " + (end - start) + " milliseconds");
        System.out.println(results[0]);
        System.out.println(results3[0]);
//        System.out.println(results3[0]);
        System.out.println(results[1]);
        System.out.println(results3[1]);
//        System.out.println (results3[1]);
        float score = align.getScore();
        float score3 = align3.getScore();
        if (results[0].equals(results3[0]) && results[1].equals(results3[1]))
            System.out.println("results are the same");
        else
            System.out.println("results are different");
        System.out.println("score 1 =" + score);
        System.out.println("score 2 =" + score3);

        SmithWatermanLinearSpaceAffine align4 = null;
        start = System.currentTimeMillis();
        for (int i = 0; i < repeat; i++) {
            align4 = new SmithWatermanLinearSpaceAffine(scores, d, e);
            align4.doAlignment(sequence1, sequence2);
            align4.getMatch();
        }
        end = System.currentTimeMillis();
        System.out.println("SmithWaterman linear space affine space took " + (end - start) + " milliseconds");
    }

    public void setDebug(boolean display) {
        debug = display;
    }

    public Result doAlignment(Sequence seq1, Sequence seq2, ProgressListener progress) {
        doAlignment(seq1.getString(), seq2.getString(), progress);
        if (progress.setProgress(1)) return null;
        List<Sequence> seqs = new ArrayList<Sequence>(2);
        String[] results = getMatch();
        seqs.add(new BasicSequence(seq1.getSequenceType(), seq1.getTaxon(), results[0]));
        seqs.add(new BasicSequence(seq2.getSequenceType(), seq2.getTaxon(), results[1]));
        return new Result(new BasicAlignment(seqs), getScore());
    }

    public double getScore(Sequence seq1, Sequence seq2) {
        doAlignment(seq1.getString(), seq2.getString(), null, true);
        return getScore();
    }
}


