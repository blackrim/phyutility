/*
 * Nucleotides.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Nucleotides.java 673 2007-03-28 04:35:52Z matt_kearse $
 */
public final class Nucleotides {
	public static final String NAME = "nucleotide";

    public static final int CANONICAL_STATE_COUNT = 4;
    public static final int STATE_COUNT = 17;

    public static final NucleotideState A_STATE = new NucleotideState("A", "A", 0);
    public static final NucleotideState C_STATE = new NucleotideState("C", "C", 1);
    public static final NucleotideState G_STATE = new NucleotideState("G", "G", 2);
    public static final NucleotideState T_STATE = new NucleotideState("T", "T", 3);

    // The following line has been removed since it's never used and if it
    // was used would cause all sorts of bugs with various analysis code.
    // T_STATE  represents either a U or a T depending on the context.
    //public static final NucleotideState U_STATE = new NucleotideState("U", "U", 3);

    public static final NucleotideState R_STATE = new NucleotideState("R", "R", 4, new NucleotideState[] {A_STATE, G_STATE});
    public static final NucleotideState Y_STATE = new NucleotideState("Y", "Y", 5, new NucleotideState[] {C_STATE, T_STATE});
    public static final NucleotideState M_STATE = new NucleotideState("M", "M", 6, new NucleotideState[] {A_STATE, C_STATE});
    public static final NucleotideState W_STATE = new NucleotideState("W", "W", 7, new NucleotideState[] {A_STATE, T_STATE});
    public static final NucleotideState S_STATE = new NucleotideState("S", "S", 8, new NucleotideState[] {C_STATE, G_STATE});
    public static final NucleotideState K_STATE = new NucleotideState("K", "K", 9, new NucleotideState[] {G_STATE, T_STATE});
    public static final NucleotideState B_STATE = new NucleotideState("B", "B", 10, new NucleotideState[] {C_STATE, G_STATE, T_STATE});
    public static final NucleotideState D_STATE = new NucleotideState("D", "D", 11, new NucleotideState[] {A_STATE, G_STATE, T_STATE});
    public static final NucleotideState H_STATE = new NucleotideState("H", "H", 12, new NucleotideState[] {A_STATE, C_STATE, T_STATE});
    public static final NucleotideState V_STATE = new NucleotideState("V", "V", 13, new NucleotideState[] {A_STATE, C_STATE, G_STATE});
    public static final NucleotideState N_STATE = new NucleotideState("N", "N", 14, new NucleotideState[] {A_STATE, C_STATE, G_STATE, T_STATE});

    public static final NucleotideState UNKNOWN_STATE = new NucleotideState("?", "?", 15, new NucleotideState[] {A_STATE, C_STATE, G_STATE, T_STATE});
    public static final NucleotideState GAP_STATE = new NucleotideState("-", "-", 16, new NucleotideState[] {A_STATE, C_STATE, G_STATE, T_STATE});

    public static final NucleotideState[] CANONICAL_STATES = new NucleotideState[] {
            A_STATE, C_STATE, G_STATE, T_STATE
    };

    public static final NucleotideState[] STATES = new NucleotideState[] {
        A_STATE, C_STATE, G_STATE, T_STATE,
        R_STATE, Y_STATE, M_STATE, W_STATE,
        S_STATE, K_STATE, B_STATE, D_STATE,
        H_STATE, V_STATE, N_STATE, UNKNOWN_STATE, GAP_STATE
    };

    public static final NucleotideState[] COMPLEMENTARY_STATES = new NucleotideState[]{
            T_STATE, G_STATE, C_STATE, A_STATE,
            Y_STATE, R_STATE, K_STATE, W_STATE,
            S_STATE, M_STATE, V_STATE, H_STATE,
            D_STATE, B_STATE, N_STATE, UNKNOWN_STATE, GAP_STATE
    };
    private static final int STATES_BY_CODE_SIZE = 128;

    // Static utility functions

	public static int getStateCount() { return STATE_COUNT; }

    /**
     *
     * @return A list of all possible states, including the gap and ambiguity states.
     */
    public static List<State> getStates() { return Collections.unmodifiableList(Arrays.asList((State[])STATES)); }

	public static int getCanonicalStateCount() { return CANONICAL_STATE_COUNT; }

	public static List<NucleotideState> getCanonicalStates() { return Collections.unmodifiableList(Arrays.asList(CANONICAL_STATES)); }

	public static NucleotideState getState(char code) {
        if (code < 0 || code >= STATES_BY_CODE_SIZE) {
            return null;
        }
        return statesByCode[code];
	}

    public static NucleotideState getState(String code) {
        return getState(code.charAt(0));
    }

    public static NucleotideState getState(int index) {
        return STATES[index];
    }

	public static NucleotideState getUnknownState() { return UNKNOWN_STATE; }

	public static NucleotideState getGapState() { return GAP_STATE; }

	public static boolean isUnknown(NucleotideState state) { return state == UNKNOWN_STATE; }

	public static boolean isGap(NucleotideState state) { return state == GAP_STATE; }

    // states must not be ambiguous/gaps nor equal
    // transition A<==>G or C<==>T
    public static boolean isTransition(State state1, State state2) {
        // use A,G is even and C,T are odd
        return ((state1.getIndex() + state2.getIndex()) & 0x1) == 0; // checks if sum is even
    }

    // states must not be ambiguous/gaps nor equal
    public static boolean isTransversion(State state1, State state2) {
        return !isTransition(state1, state2);
    }

    public static boolean isPurine(State state) {
        if (state.isAmbiguous()) {
            // return true only if all its ambiguities are isPurine()
            for (State state1 : state.getCanonicalStates()) {
                if(! isPurine(state1)) return false;
            }
            return true;
        }
        return state == A_STATE || state == G_STATE;
    }

    public static boolean isPyrimidine(State state) {
        if (state.isAmbiguous()) {
            // return true only if all its ambiguities are isPyrimidine()
            for (State state1 : state.getCanonicalStates()) {
                if (! isPyrimidine(state1)) return false;
            }
            return true;
        }
        return state == C_STATE || state == T_STATE;
    }
    public static boolean isGCstate(State state) {
        return ( state == G_STATE || state == C_STATE || state == S_STATE);
    }
    
    public static boolean isATstate(State state) {
        return (state == A_STATE || state == T_STATE || state == W_STATE);
    }

    public String getName() { return "Nucleotides"; }

	public static NucleotideState[] toStateArray(String sequenceString) {
		NucleotideState[] seq = new NucleotideState[sequenceString.length()];
		for (int i = 0; i < seq.length; i++) {
			seq[i] = getState(sequenceString.charAt(i));
		}
		return seq;
	}

	public static NucleotideState[] toStateArray(byte[] indexArray) {
	    NucleotideState[] seq = new NucleotideState[indexArray.length];
	    for (int i = 0; i < seq.length; i++) {
	        seq[i] = getState(indexArray[i]);
	    }
	    return seq;
	}

    private static final NucleotideState[] statesByCode;

    static {
        statesByCode = new NucleotideState[STATES_BY_CODE_SIZE];
        for (int i = 0; i < statesByCode.length; i++) {
            // Undefined characters are mapped to null
            statesByCode[i] = null;
        }

        for (NucleotideState state : STATES) {
            statesByCode[state.getCode().charAt(0)] = state;
            statesByCode[Character.toLowerCase(state.getCode().charAt(0))] = state;
        }

        statesByCode['u'] = T_STATE;
        statesByCode['U'] = T_STATE;
    }
}