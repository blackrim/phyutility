package jebl.evolution.align.scores;

public class Pam150 extends AminoAcidScores {

  private final float[][] residueScores = {

            /*  A   R   N   D   C   Q   E   G   H   I   L   K   M   F   P   S   T   W   Y   V */
            {   3},
            {  -2,  6},
            {   0, -1,  3},
            {   0, -2,  2,  4},
            {  -2, -4, -4, -6,  9},
            {  -1,  1,  0,  1, -6,  5},
            {   0, -2,  1,  3, -6,  2,  4},
            {   1, -3,  0,  0, -4, -2, -1,  4},
            {  -2,  1,  2,  0, -3,  3,  0, -3,  6},
            {  -1, -2, -2, -3, -2, -3, -2, -3, -3,  5},
            {  -2, -3, -3, -5, -6, -2, -4, -4, -2,  1,  5},
            {  -2,  3,  1, -1, -6,  0, -1, -2, -1, -2, -3,  4},
            {  -1, -1, -2, -3, -5, -1, -2, -3, -3,  2,  3,  0,  7},
            {  -4, -4, -4, -6, -5, -5, -6, -5, -2,  0,  1, -6, -1,  7},
            {   1, -1, -1, -2, -3,  0, -1, -1, -1, -3, -3, -2, -3, -5,  6},
            {   1, -1,  1,  0,  0, -1, -1,  1, -1, -2, -3, -1, -2, -3,  1,  2},
            {   1, -2,  0, -1, -3, -1, -1, -1, -2,  0, -2,  0, -1, -3,  0,  1,  4},
            {  -6,  1, -4, -7, -7, -5, -7, -7, -3, -5, -2, -4, -5, -1, -6, -2, -5, 12},
            {  -3, -4, -2, -4,  0, -4, -4, -5,  0, -2, -2, -4, -3,  5, -5, -3, -3, -1,  8},
            {   0, -3, -2, -3, -2, -2, -2, -2, -3,  3,  1, -3,  1, -2, -2, -1,  0, -6, -3,  4}};

  public Pam150() { buildScores(residueScores); }
}
