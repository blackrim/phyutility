package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

import java.util.ArrayList;

/**
 * Performs recursive local alignments. each time splitting the longer of the two sequences
 * into two subsequences either side of the local alignment and aligning those.
 * Uses SmithWatermanLinearSpaceAffine. Stores all of the local alignments and their scores.
 * Threshold T is the minimum score that an alignment must be for inclusion.
 *
 * @author Richard Moir
 *
 * @version $Id: NonOverlapMultipleLocalAffine.java 370 2006-06-29 18:57:56Z rambaut $
 *
 */

public class NonOverlapMultipleLocalAffine extends AlignRepeatAffine {

	private ArrayList<LocalAlignment> localAligns = new ArrayList<LocalAlignment>();
	private SmithWatermanLinearSpaceAffine swlsa;

	public NonOverlapMultipleLocalAffine(Scores sub, float d, float e, int T) {
		super(sub, d, e, T);
	}

	/**
     * @param sq1
     * @param sq2
     */
	public void doAlignment(String sq1, String sq2) {

		if(sq1.length() < sq2.length())	{			//sq2 is the shorter seq (is not dissected).
			String temp = sq2;
			sq2 = sq1;
			sq1 = temp;
		}
		this.seq1 = sq1;
		this.seq2 = sq2;

		swlsa = new SmithWatermanLinearSpaceAffine(sub, d, e);
		swlsa.doAlignment(sq1, sq2);

		String[] match = swlsa.getMatch();
		if(swlsa.getScore() >= T) {
			localAligns.add(new LocalAlignment(match[0],match[1],swlsa.start1,swlsa.end1 - 1,swlsa.getScore()));
			if(swlsa.start1 != 0)
				recurseAlignment(sq1.substring(0, swlsa.start1), 0);
			if(swlsa.end1 != sq1.length())
				recurseAlignment(sq1.substring(swlsa.end1, sq1.length()), swlsa.end1);
		}
	}

	public void recurseAlignment(String sq1, int leftIndex) {

		swlsa.doAlignment(sq1, seq2);

		String match[] = swlsa.getMatch();
		if(swlsa.getScore() >= T) {
			localAligns.add(new LocalAlignment(match[0],match[1],swlsa.start1 + leftIndex,swlsa.end1 - 1 + leftIndex,swlsa.getScore()));
		}
		else return;

		if(swlsa.start1 != 0)
			recurseAlignment(sq1.substring(0, swlsa.start1), leftIndex);
		if(swlsa.end1 != sq1.length())
			recurseAlignment(sq1.substring(swlsa.end1, sq1.length()), leftIndex + swlsa.end1);

	}

	/**
     * @return two-element array containing all of the local alignments separated by " - ";
     */
	public String[] getMatch() {
		String sq1 = "";
		String sq2 = "";
        for (LocalAlignment localAlign : localAligns) {
            sq1 += localAlign.sq1 + " - ";
            sq2 += localAlign.sq2 + " - ";
        }
		return new String[] {sq1.substring(0,sq1.length() - 3), sq2.substring(0,sq1.length() - 3)};
	}

	/**
	 * @param width length to trim lines to. -1 = infinite width.
	 * @return String containing all local alignments with the score next to them.
	 */
	public String getMatchScores(int width) {
		String sq1;
		String sq2;
		float score;
		String matchScores = "";
        for (LocalAlignment la : localAligns) {
            sq1 = la.sq1;
            sq2 = la.sq2;
            score = la.score;
            if (la.sq1.length() < width) {
                matchScores += "Score: " + score + "\n";
                matchScores += sq1 + "\n";
                matchScores += sq1 + "\n\n\n";
            } else {
                matchScores += "Score: " + score + "\n";
                int size = sq1.length();
                for (int i = width; i < size + width; i += width) {
                    if (i > size) {
                        matchScores += sq1.substring(i - width, size) + "\n";
                        matchScores += sq2.substring(i - width, size) + "\n\n";
                    } else {
                        matchScores += sq1.substring(i - width, i) + "\n";
                        matchScores += sq2.substring(i - width, i) + "\n\n";
                    }
                }
                matchScores += "\n";
            }
        }
		return matchScores;
	}

	/**
     * @return the score of the best alignment
     */
	public float getScore() {
		float score = 0;
        for (LocalAlignment la : localAligns) {
            score += la.score;
        }
		return score;
	}

	/**
	 * The indices for these correspond to those for the getAlignments() matrix.
	 *
	 * @return a matrix of scores for all the alignments.
	 */
	public float[] getScores() {
		float[] scores = new float[localAligns.size()];
		int i = 0;
        for (LocalAlignment la : localAligns) {
            scores[i] = la.score;
            i++;
        }
		return scores;
	}

	/**
	 * The indices for these correspond to those for the getScoreMatrix() matrix.
	 *
	 * @return a 2d matrix of alignments. first index is the alignment number. second is the sequence number (0 or 1)
	 */
	public String[][] getAlignments() {
		String[][] aligns = new String[localAligns.size()][2];
		int i = 0;
        for (Object localAlign : localAligns) {
            LocalAlignment la = (LocalAlignment) localAlign;
            aligns[i][0] = la.sq1;
            aligns[i][2] = la.sq2;
            i++;
        }
		return aligns;
	}

	/**
     * Print matrix used to calculate this alignment.
     *
     * @param out Output to print to.
     */
    public void printf(Output out) {
    	swlsa.printf(out);
    }
}

/**
 *  Class used to store all of the local alignments.
 */
class LocalAlignment {

	public String sq1;
	public String sq2;
	public int start;
	public int end;
	public float score;

	public LocalAlignment(String sq1, String sq2, int start, int end, float score) {
		this.sq1 = sq1;
		this.sq2 = sq2;
		this.start = start;
		this.end = end;
		this.score = score;
	}
}