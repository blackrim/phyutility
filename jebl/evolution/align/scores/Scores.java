package jebl.evolution.align.scores;

/**
 * Base class for all score matrices in the package.
 *
 * @author Alexei Drummond
 *
 * @version $Id: Scores.java 638 2007-02-06 20:31:30Z matt_kearse $
 *
 * Based on code originally by Peter Setsoft. See package.html.
 */
public abstract class Scores implements ScoreMatrix {

    public float[][] score;
    private String extraResidues = "";

    /**
     * @param scores float[][] with position [i][j] holding the score for
     *        getScore(getAlphabet().charAt(i), getAlphabet().charAt(j)).
     */
    protected void buildScores(float[][] scores) {
        String states = getAlphabet().toUpperCase();
        // Allow lowercase and uppercase states (ASCII code <= 127):
        score = new float[127][127];
        for (int i=0; i<states.length(); i++) {
            char a = states.charAt(i);
            char lca = Character.toLowerCase(a);
            for (int j=0; j<=i; j++) {
                char b = states.charAt(j);
                char lcb = Character.toLowerCase(b);
                score[a][b] = score[b][a]
                    = score[a][lcb] = score[lcb][a]
                    = score[lca][b] = score[b][lca]
                    = score[lca][lcb] = score[lcb][lca]
                    = scores[i][j];
            }
        }
    }
    

    void buildScores(float match, float mismatch) {
        int states = getAlphabet().length();
        float[][] scores = new float[states][states];

        for (int i = 0; i < states; i++) {
            for (int j = 0; j < states; j++) {
                if (i == j) {
                    scores[i][j] = match;
                } else {
                    scores[i][j] = mismatch;
                }
            }
        }
        buildScores(scores);
    }

    public final float getScore(char x, char y) {
        return score[x][y];
    }

    public String toString() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf(".")+1);
    }

    /**
     * @param scoreMatrix A ScoreMatrix with only low ascii characters (< chr(127)) in the alphabet
     * @return A Scores instance corresponding to scoreMatrix.
     */
    public static Scores forMatrix(ScoreMatrix scoreMatrix) {
        final String alphabet = scoreMatrix.getAlphabet();
        final String name = scoreMatrix.getName();
        float[][] scores = new float[alphabet.length()][alphabet.length()];
        for (int i=0; i < alphabet.length(); i++) {
            char a = alphabet.charAt(i);
            for (int j=0; j < alphabet.length(); j++) {
                char b = alphabet.charAt(j);
                scores[i][j] = scoreMatrix.getScore(a,b);
            }
        }
        Scores result = new Scores() {
            public String getAlphabet() {
                return alphabet;
            }

            public String getName() {
                return name;
            }
        };
        result.buildScores(scores);
        return result;
    }

    public static Scores duplicate(Scores scores) {
        Scores result;
        if(scores instanceof AminoAcidScores) {
            result = new AminoAcidScores();
        } else if (scores instanceof NucleotideScores) {
            result = new NucleotideScores((NucleotideScores) scores);
        } else {
            // what was part of the extra residues in the original class now becomes normal residues,
            // just as in duplicate().
            final String alphabet = scores.getAlphabet();
            final String name = scores.getName();
            result = new Scores() {
                public String getAlphabet() {
                    return alphabet + getExtraResidues();
                }
                public String getName() {
                    return name;
                }
            };
        }
        result.extraResidues = scores.getExtraResidues();
        result.score = new float[127][127];
        for (int i = 0; i < 127; i++) {
            System.arraycopy(scores.score[i], 0, result.score[i], 0, 127);
        }
        return result;
    }

    /**
     *
     * @param scores
     * @param gapVersusResidueCost should be a negative value
     * @param gapVersusGapCost should be a positive value
     */
    public static Scores includeGaps(Scores scores, float gapVersusResidueCost, float gapVersusGapCost) {
//        System.out.println("cost =" + gapVersusResidueCost+ "," + gapVersusGapCost);
        Scores result = duplicate(scores);
        String states = scores.getAlphabet();
        for (int i = 0; i < states.length(); i++) {
            char res1 = states.charAt(i);
            result.score['-'] [res1] = gapVersusResidueCost;
            result.score[res1]['-'] = gapVersusResidueCost;
        }
        result.score['-']['-'] = gapVersusGapCost;
        return result;
    }

    /**
     * includes additional characters in the score matrix which will all have scored zero when compared to other
     * characters.
     *
     * Current system does not handle special characters well, such as ? Or "R" for NucleotideSequences,
     *   which represents a "A" or "G".
     *   Currently, we just add all characters to the allowed set of characters, and they are scored as
     *   zero cost when comparing to other characters, including themselves. One-day, we should probably
     *   introduce better scoring system so that "R" is a positive score compared to "A" or "G",
     *   but a negative score compared to "C" or "T".
     *
     * example usage:
     * scores = Scores.includeAdditionalCharacters(scores, "?ABCDEFGHIJKLMNOPQRSTUVWXYZ"); 
     * @param scores
     * @param characters
     * @return a new score matrix.
     */
    public static Scores includeAdditionalCharacters(Scores scores, String characters) {
        Scores result = duplicate(scores);
        String states = scores.getAlphabet();
        char[] unique = new char[characters.length ()];
        int index = 0;
        for (char character : characters.toCharArray()) {
            if(states.indexOf(character)< 0) unique[index++ ]= character;
        }
        result.extraResidues =result.extraResidues+ new String(unique, 0, index);
        // don't need to modify any of the "scores" values, since they all default to zero anyway.
        return result;
    }


    protected String getExtraResidues() {
         return extraResidues;
     }


    /**
     * extends the given score matrix to include gap Versus gap and gap Versus residue costs
     * The gap versus the gap cost is taken to be the same as the average residue match cost
     * The gap in versus residue cost is taken to be the same as the average residue mismatch cost
     * @param scores
     */
    // this function is a bad idea, don't use it.
/*    public static Scores includeGaps(Scores scores) {
        float totalMismatch = 0;
        float totalMatch = 0;
        int mismatchCount= 0;
        int matchCount = 0;
        String states = scores.getAlphabet();
        for (int i = 0; i < states.length(); i++) {
            char res1 = states.charAt(i);
            for (int j = 0; j < states.length(); j++) {
                char res2 = states.charAt(j);
                double score = scores.score[res1] [res2];
                if(i==j) {
                    totalMatch += score;
                    matchCount ++;
                }
                else {
                    totalMismatch += score;
                    mismatchCount ++;
                }
            }
        }
        return includeGaps(scores, totalMismatch/mismatchCount-0.1f, totalMatch/matchCount);
    }*/
}