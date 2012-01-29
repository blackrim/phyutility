/*
 * Codons.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Codons.java 668 2007-03-26 06:47:40Z matt_kearse $
 */
public final class Codons {
	public static final String NAME = "codon";

    public static final int CANONICAL_STATE_COUNT = 64;
    public static final int STATE_COUNT = 66;

    public static final CodonState[] CANONICAL_STATES;
    public static final CodonState[] STATES;

    // This bit of static code creates the 64 canonical codon states
    static {
        CANONICAL_STATES = new CodonState[CANONICAL_STATE_COUNT];
        char[] nucs = new char[] { 'A', 'C', 'G', 'T' };
        int x = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    String code = "" + nucs[i] + nucs[j] + nucs[k];
                    CANONICAL_STATES[x] = new CodonState(code, code, x);
                    x++;
                }
            }
        }

    }

    public static final CodonState UNKNOWN_STATE = new CodonState("?", "???", 64, CANONICAL_STATES);
    public static final CodonState GAP_STATE = new CodonState("-", "---", 65, CANONICAL_STATES);

    public static int getStateCount() { return STATE_COUNT; }

    public static List<State> getStates() { return Collections.unmodifiableList(Arrays.asList((State[])STATES)); }

    public static int getCanonicalStateCount() { return CANONICAL_STATE_COUNT; }

    public static List<State> getCanonicalStates() { return Collections.unmodifiableList(Arrays.asList((State[])CANONICAL_STATES)); }

	public static CodonState getState(NucleotideState nucleotide1, NucleotideState nucleotide2, NucleotideState nucleotide3) {
		if (nucleotide1.isGap() && nucleotide2.isGap() && nucleotide3.isGap()) {
			return GAP_STATE;
		}

		if (nucleotide1.isAmbiguous() || nucleotide2.isAmbiguous() || nucleotide3.isAmbiguous()) {
			return UNKNOWN_STATE;
		}

	    String code = nucleotide1.getCode() + nucleotide2.getCode() + nucleotide3.getCode();
	    return statesByCode.get(code);
	}

    /**
     * Gets the state object for the given code. Returns null if the code is illegal.
     * @param code a three-character string of nucleotides in uppercase
     * @return the state
     */
    public static CodonState getState(String code) {
        code=code.toUpperCase().replace('U','T');
        return statesByCode.get(code);
	}

	public static CodonState getState(int index) {
	    return STATES[index];
	}

	public static CodonState getUnknownState() { return UNKNOWN_STATE; }

	public static CodonState getGapState() { return GAP_STATE; }

	public static boolean isUnknown(CodonState state) { return state == UNKNOWN_STATE; }

	public static boolean isGap(CodonState state) { return state == GAP_STATE; }

	public static NucleotideState[] toNucleotides(CodonState state) {
		NucleotideState[] nucs = new NucleotideState[3];
		String code = state.getCode();
		nucs[0] = Nucleotides.getState(code.charAt(0));
		nucs[1] = Nucleotides.getState(code.charAt(1));
		nucs[2] = Nucleotides.getState(code.charAt(2));
		return nucs;
	}

	public static CodonState[] toStateArray(String sequenceString) {
		int n = sequenceString.length() / 3;
		CodonState[] seq = new CodonState[n];
		for (int i = 0; i < n; i++) {
			seq[i] = getState(sequenceString.substring(i * 3, (i * 3) + 3));
		}
		return seq;
	}

	public static CodonState[] toStateArray(byte[] indexArray) {
	    CodonState[] seq = new CodonState[indexArray.length];
	    for (int i = 0; i < seq.length; i++) {
	        seq[i] = getState(indexArray[i]);
	    }
	    return seq;
	}

    private static final Map<String, CodonState> statesByCode;

    // now create the complete codon state array
    static {
        STATES = new CodonState[STATE_COUNT];
        for (int i = 0; i < 64; i++) {
            STATES[i] = CANONICAL_STATES[i];
        }
        STATES[64] = UNKNOWN_STATE;
        STATES[65] = GAP_STATE;

        statesByCode = new HashMap<String, CodonState>();
        for (int i = 0; i < STATES.length; i++) {
            statesByCode.put(STATES[i].getCode(), STATES[i]);
        }
    }

}
