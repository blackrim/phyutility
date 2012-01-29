/*
 * AminoAcids.java
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
 * @version $Id: AminoAcids.java 602 2006-12-29 01:18:11Z twobeers $
 */
public final class AminoAcids {
    public static final String NAME = "amino acid";

    public static final int CANONICAL_STATE_COUNT = 20;
    public static final int STATE_COUNT = 26;

    public static final AminoAcidState A_STATE = new AminoAcidState("A", "A", 0);
    public static final AminoAcidState C_STATE = new AminoAcidState("C", "C", 1);
    public static final AminoAcidState D_STATE = new AminoAcidState("D", "D", 2);
    public static final AminoAcidState E_STATE = new AminoAcidState("E", "E", 3);
    public static final AminoAcidState F_STATE = new AminoAcidState("F", "F", 4);
    public static final AminoAcidState G_STATE = new AminoAcidState("G", "G", 5);
    public static final AminoAcidState H_STATE = new AminoAcidState("H", "H", 6);
    public static final AminoAcidState I_STATE = new AminoAcidState("I", "I", 7);
    public static final AminoAcidState K_STATE = new AminoAcidState("K", "K", 8);
    public static final AminoAcidState L_STATE = new AminoAcidState("L", "L", 9);
    public static final AminoAcidState M_STATE = new AminoAcidState("M", "M", 10);
    public static final AminoAcidState N_STATE = new AminoAcidState("N", "N", 11);
    public static final AminoAcidState P_STATE = new AminoAcidState("P", "P", 12);
    public static final AminoAcidState Q_STATE = new AminoAcidState("Q", "Q", 13);
    public static final AminoAcidState R_STATE = new AminoAcidState("R", "R", 14);
    public static final AminoAcidState S_STATE = new AminoAcidState("S", "S", 15);
    public static final AminoAcidState T_STATE = new AminoAcidState("T", "T", 16);
    public static final AminoAcidState V_STATE = new AminoAcidState("V", "V", 17);
    public static final AminoAcidState W_STATE = new AminoAcidState("W", "W", 18);
    public static final AminoAcidState Y_STATE = new AminoAcidState("Y", "Y", 19);
    // If you use these, make sure to change the numbers of B..- below to 22..27,
    // and increase CANONICAL_STATE_COUNT to 20, STATE_COUNT to 28
    //public static final AminoAcidState U_STATE = new AminoAcidState("U", "U", 20); // Selenocysteine
    // As of 2006-12-14 I think there is no IUPAC one letter code for Pyrrolysine, but BioJava uses "O"
    //public static final AminoAcidState O_STATE = new AminoAcidState("O", "O", 21); // Pyrrolysine

    public static final AminoAcidState[] CANONICAL_STATES = new AminoAcidState[]{
            A_STATE, C_STATE, D_STATE, E_STATE, F_STATE,
            G_STATE, H_STATE, I_STATE, K_STATE, L_STATE,
            M_STATE, N_STATE, P_STATE, Q_STATE, R_STATE,
            S_STATE, T_STATE, V_STATE, W_STATE, Y_STATE,
           // U_STATE, O_STATE,
    };

    public static final AminoAcidState B_STATE = new AminoAcidState("B", "B", 20, new AminoAcidState[]{D_STATE, N_STATE});
    public static final AminoAcidState Z_STATE = new AminoAcidState("Z", "Z", 21, new AminoAcidState[]{E_STATE, Q_STATE});
    public static final AminoAcidState X_STATE = new AminoAcidState("X", "X", 22, CANONICAL_STATES);
    public static final AminoAcidState UNKNOWN_STATE = new AminoAcidState("?", "?", 23, CANONICAL_STATES);
    public static final AminoAcidState STOP_STATE = new AminoAcidState("*", "*", 24, CANONICAL_STATES);
    public static final AminoAcidState GAP_STATE = new AminoAcidState("-", "-", 25, CANONICAL_STATES);

    public static final AminoAcidState[] STATES = new AminoAcidState[]{
            A_STATE, C_STATE, D_STATE, E_STATE, F_STATE,
            G_STATE, H_STATE, I_STATE, K_STATE, L_STATE,
            M_STATE, N_STATE, P_STATE, Q_STATE, R_STATE,
            S_STATE, T_STATE, V_STATE, W_STATE, Y_STATE,
            //U_STATE, O_STATE,
            B_STATE, Z_STATE, X_STATE, UNKNOWN_STATE,
            STOP_STATE, GAP_STATE
    };

    // Chemical classifications
    public static final StateClassification CHEMICAL_CLASSIFICATION = new StateClassification.Default("chemical",
            new String[]{"alphatic", "phenylalanine", "sulphur", "glycine", "hydroxyl", "tryptophan", "tyrosine", "proline", "acidic", "amide", "basic"},
            new State[][]{{A_STATE, V_STATE, I_STATE, L_STATE}, {F_STATE}, {C_STATE, M_STATE}, {G_STATE}, {S_STATE, T_STATE},
                    {W_STATE}, {Y_STATE}, {P_STATE}, {D_STATE, E_STATE}, {N_STATE, Q_STATE}, {H_STATE, K_STATE, R_STATE}});

    // Hydropathy classifications
    public static final StateClassification HYDROPATHY_CLASSIFICATION = new StateClassification.Default("hydropathy",
            // should the first entry be "hydrophobic" instead of "hydropathic" ?
            new String[]{"hydropathic", "neutral", "hydrophilic"},
            new State[][]{{I_STATE, V_STATE, L_STATE, F_STATE, C_STATE, M_STATE, A_STATE, W_STATE},
                    {G_STATE, T_STATE, S_STATE, Y_STATE, P_STATE, H_STATE},
                    {D_STATE, E_STATE, K_STATE, N_STATE, Q_STATE, R_STATE}});

    // TT: I think the unit used here may be Angstrom^3, but I'm not sure; see http://www.imb-jena.de/IMAGE_AA.html
    public static final StateClassification VOLUME_CLASSIFICATION = new StateClassification.Default("volume",
            new String[]{"60-90", "108-117", "138-154", "162-174", "189-228"},
            new State[][]{{G_STATE, A_STATE, S_STATE}, {C_STATE, D_STATE, P_STATE, N_STATE, T_STATE},
                    {E_STATE, V_STATE, Q_STATE, H_STATE}, {M_STATE, I_STATE, L_STATE, K_STATE, R_STATE},
                    {F_STATE, Y_STATE, W_STATE}});

    /**
     * This character represents the amino acid equivalent of a stop codon to cater for
     * situations arising from converting coding DNA to an amino acid sequences.
     */
    /**
     * A table to map state numbers (0-27) to their three letter codes.
     */
    private static final String[] AMINOACID_TRIPLETS = {
            // A     C      D      E      F      G      H      I      K
            "Ala", "Cys", "Asp", "Glu", "Phe", "Gly", "His", "Ile", "Lys",
            // L     M      N      P      Q      R      S      T      V
            "Leu", "Met", "Asn", "Pro", "Gln", "Arg", "Ser", "Thr", "Val",
            //W      Y      B
            "Trp", "Tyr", "Asx",

            //  U      O
            //"Sec", "Pyl",

            // Z      X      *      ?     -
            "Glx", " X ", " * ", " ? ", " - "
    };
    private static final int STATE_BY_CODE_SIZE = 128;

    public static int getStateCount() {
        return STATE_COUNT;
    }

    //public static List<State> getStates() { return Collections.unmodifiableList(Arrays.asList((State[])STATES)); }
    public static List<AminoAcidState> getStates() {
        return Collections.unmodifiableList(Arrays.asList(STATES));
    }

    public static int getCanonicalStateCount() {
        return CANONICAL_STATE_COUNT;
    }

    public static List<State> getCanonicalStates() {
        return Collections.unmodifiableList(Arrays.asList((State[]) CANONICAL_STATES));
    }

    public static AminoAcidState getState(char code) {
        if (code < 0 || code >= STATE_BY_CODE_SIZE) {
            return null;
        }
        return statesByCode[code];
    }

    public static AminoAcidState getState(String code) {
        return getState(code.charAt(0));
    }

    public static AminoAcidState getState(int index) {
        return STATES[index];
    }

    public static AminoAcidState getUnknownState() {
        return UNKNOWN_STATE;
    }

    public static AminoAcidState getGapState() {
        return GAP_STATE;
    }

    public static boolean isUnknown(AminoAcidState state) {
        return state == UNKNOWN_STATE;
    }

    public static boolean isGap(AminoAcidState state) {
        return state == GAP_STATE;
    }

    public static String getTripletCode(AminoAcidState state) {
        return AMINOACID_TRIPLETS[state.getIndex()];
    }

    public static AminoAcidState[] toStateArray(String sequenceString) {
        AminoAcidState[] seq = new AminoAcidState[sequenceString.length()];
        for (int i = 0; i < seq.length; i++) {
            seq[i] = getState(sequenceString.charAt(i));
        }
        return seq;
    }

    public static AminoAcidState[] toStateArray(byte[] indexArray) {
        AminoAcidState[] seq = new AminoAcidState[indexArray.length];
        for (int i = 0; i < seq.length; i++) {
            seq[i] = getState(indexArray[i]);
        }
        return seq;
    }

    private static final AminoAcidState[] statesByCode;

    static {
        statesByCode = new AminoAcidState[STATE_BY_CODE_SIZE];
        for (int i = 0; i < statesByCode.length; i++) {
            // Undefined characters are mapped to null
            statesByCode[i] = null;
        }

        for (AminoAcidState state : STATES) {
            final char code = state.getCode().charAt(0);
            statesByCode[code] = state;
            statesByCode[Character.toLowerCase(code)] = state;
        }
    }
}
