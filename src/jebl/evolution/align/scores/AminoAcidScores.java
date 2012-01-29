package jebl.evolution.align.scores;

/**
 * @author Alexei Drummond
 *
 * @version $Id: AminoAcidScores.java 360 2006-06-22 07:42:48Z pepster $
 */
public class AminoAcidScores extends Scores {

    private String residues = "ARNDCQEGHILKMFPSTWYV";

    public String getName() {
        return toString();
    }

    public final String getAlphabet() { return residues + getExtraResidues(); }


    public AminoAcidScores() {
    }

    public AminoAcidScores(float m, float n) {
        buildScores(m, n);
    }
}
