package jebl.evolution.align.scores;


/**
 * @author Andrew Rambaut
 *
 * @version $Id: Hamming.java 185 2006-01-23 23:03:18Z rambaut $
 */

public class Hamming extends NucleotideScores {

  private final float[][] residueScores = {

            /*  A   C   G   T   U */
            {   0},
            {  -1,  0},
            {  -1, -1,  0},
            {  -1, -1,  -1,  0},
            {  -1, -1,  -1,  0, 0}};

  public Hamming() { buildScores(residueScores); }

    public String toString() {
        return "Hamming (0/-1)";
    }
}