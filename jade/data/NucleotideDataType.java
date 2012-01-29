/*
 * NucleotideDataType.java
 *
 * Created on April 18, 2005, 8:26 PM
 */

package jade.data;

/**
 *
 * @author stephensmith
 */
public class NucleotideDataType implements DataType{
    
    public static final int A_STATE = 0;
    public static final int C_STATE = 1;
    public static final int G_STATE = 2;
    public static final int UT_STATE = 3;
    public final int getSuggestedGapState() { return SUGGESTED_GAP_STATE; }
    public final boolean isGapState(final int state) { return state==SUGGESTED_GAP_STATE; }
    //
    // Variables
    //
    
    private static final char[] DNA_CONVERSION_TABLE = {'A', 'C', 'G', 'T', UNKNOWN_CHARACTER};
    private static final char[] RNA_CONVERSION_TABLE = {'A', 'C', 'G', 'T', UNKNOWN_CHARACTER};
    
    //For faster conversion!
    boolean isRNA_;
    char[] conversionTable_;
    
    //Must stay after static CONVERSION_TABLE stuff!
    public static final NucleotideDataType DEFAULT_INSTANCE = new NucleotideDataType();
    
    public NucleotideDataType() {
        this(false);
    }
    
    /** If isRNA is true than getChar(state) will return a U instead of a T */
    public NucleotideDataType(boolean isRNA) {
        this.isRNA_ = isRNA;
        conversionTable_ = (isRNA_ ? RNA_CONVERSION_TABLE : DNA_CONVERSION_TABLE );
    }
    
    // Get number of bases
    public int getNumStates() {
        return 4;
    }
    
    /**
     * @return true if this state is an unknown state
     */
    protected final boolean isUnknownStateImpl(final int state) {
        return(state>=4)||(state<0);
    }
    
    /**
     * Get state corresponding to character c <BR>
     * <B>NOTE</B>: IF YOU CHANGE THIS IT MAY STOP THE NUCLEOTIDE TRANSLATOR FROM WORKING!
     * - It relies on the fact that all the states for 'ACGTU' are between [0, 3]
     */
    protected int getStateImpl(char c) {
        switch (c) {
            case 'A':
                return A_STATE;
            case 'C':
                return C_STATE;
            case 'G':
                return G_STATE;
            case 'T':
                return UT_STATE;
            case 'U':
                return UT_STATE;
            case UNKNOWN_CHARACTER:
                return 4;
            case 'a':
                return A_STATE;
            case 'c':
                return C_STATE;
            case 'g':
                return G_STATE;
            case 't':
                return UT_STATE;
            case 'u':
                return UT_STATE;
            default:
                return 4;
        }
    }
    
    /**
     * Get character corresponding to a given state
     */
    protected char getCharImpl(final int state) {
        if(state<conversionTable_.length&&state>=0){
            return conversionTable_[state];
        }
        return UNKNOWN_CHARACTER;
    }
    
    /**
     * @return a string describing the data type
     */
    public String getDescription()	{
        return NUCLEOTIDE_DESCRIPTION;
    }
    
    /**
     * @return the unique numerical code describing the data type
     */
    public int getTypeID() {
        return 0;
    }
    
    /**
     * @return true if A->G, G->A, C->T, or T->C
     * if firstState equals secondState returns FALSE!
     */
    public final boolean isTransitionByState(int firstState, int secondState) {
        switch(firstState) {
            case A_STATE: {
                if(secondState==G_STATE) {
                    return true;
                }
                return false;
            }
            case C_STATE : {
                if(secondState==UT_STATE) {
                    return true;
                }
                return false;
            }
            case G_STATE : {
                if(secondState==A_STATE) {
                    return true;
                }
                return false;
            }
            case UT_STATE : {
                if(secondState==C_STATE) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }
    
    /**
     * @return true if A->G, G->A, C->T, or T->C
     * if firstState equals secondState returns FALSE!
     * (I've renamed things to avoid confusion between java typing of ints and chars)
     */
    public final boolean isTransitionByChar(char firstChar, char secondChar) {
        //I'm leaving it open to a possible optimisation if anyone cares.
        return isTransitionByState(getState(firstChar), getState(secondChar));
    }
    public final int getState(char c) {
        if(isSuggestedGap(c)) { return SUGGESTED_GAP_STATE; }
        return getStateImpl(c);
    }
    /**
     * Handles gap state and then passes on to getStateImpl
     */
    public final char getChar(final int state) {
        if(state==SUGGESTED_GAP_STATE) { return DEFAULT_GAP_CHARACTER; }
        if(state<0) { return UNKNOWN_CHARACTER; }
        return getCharImpl(state);
    }
    public final char getSuggestedChar(final char c) {
        if(isGapChar(c)) {
            return DEFAULT_GAP_CHARACTER;
        }
        return getPreferredCharImpl(c);
    }
    protected char getPreferredCharImpl(final char c) {
        return getChar(getState(c));
    }
        
    //==========================================================
    //================ ResidueDataType stuff ===================
    //==========================================================
    
    /**
     * @return a copy of the input
     */
    public int[] getNucleotideStates(int[] array) {
        if(array == null) { return null; }
	int[] copy = new int[array.length];
	System.arraycopy(array,0,copy,0,array.length);
	return copy;
    }
    
    /**
     * @return the input
     */
    public int getRelavantLength(int numberOfStates) {
        return numberOfStates;
    }
    public int getSuggestedUnknownState() { return SUGGESTED_UNKNOWN_STATE; }
    
    public final boolean hasGapCharacter() { return true; }
    /**
     * @return true if this character is a '.' or a '_'
     */
    public final boolean isGapChar(final char c) {
        return isSuggestedGap(c);
    }
    
    public final boolean isUnknownChar(final char c) {
        return isUnknownState(getState(c));
    }
    public final boolean isUnknownState(final int state) {
        return(state==SUGGESTED_GAP_STATE||isUnknownStateCorr(state));
    }
    protected final boolean isUnknownStateCorr(final int state) {
        return(state>=2);
    }
    /**
     * @return a copy of theinput
     */
    public int[] getMolecularStatesFromSimpleNucleotides(int[] array, int startingIndex) {
        if(array == null) { return null; }
        int[] copy = new int[array.length-startingIndex];
        System.arraycopy(array,startingIndex,copy,0,array.length-startingIndex);
        return copy;
    }
    /**
     * @return a copy of the input
     */
    public int[] getMolecularStatesFromIUPACNucleotides(int[] array, int startingIndex) {
        if(array == null) { return null; }
        int[] copy = new int[array.length-startingIndex];
        System.arraycopy(array,startingIndex,copy,0,array.length-startingIndex);
        return copy;
    }

    /**
     * @return false Nucleotide data will suffice
     */
    public boolean isCreatesIUPACNuecleotides() {
        return false;
    }
    
    
    /**
     * @return 1
     */
    public final int getNucleotideLength() {
        return 1;
    }
    // ====================================================================
    // === Static utility methods
    /**
     * Obtain the complement state
     * @param baseState the base state to complement (may be IUPAC but IUPACness is lost)
     * @return the complement state
     */
    public static final int getComplementState(int baseState) {
        switch(baseState) {
            case A_STATE : return UT_STATE;
            case UT_STATE : return A_STATE;
            case G_STATE : return C_STATE;
            case C_STATE : return G_STATE;
            default: return UNKNOWN;
        }
    }
    /**
     * Obtain the complement of a sequence of nucleotides (or IUPACNucleotides - but IUPAC ness is lost)
     * @param sequence the sequence (of nucleotide states)
     * @return the complement
     */
    public static final int[] getSequenceComplement(int[] sequence) {
        final int[] result  = new int[sequence.length];
        for(int i = 0 ; i < sequence.length ; i++) {
            result[i] = getComplementState(sequence[i]);
        }
        return result;
    }
    
    /**
     * Complement of a sequence of nucleotides (or IUPACNucleotides - but IUPAC ness is lost)
     * @param sequence the sequence (of nucleotide states) (is modified)
     */
    public static final void complementSequence(int[] sequence) {
        for(int i = 0 ; i < sequence.length ; i++) {
            sequence[i] = getComplementState(sequence[i]);
        }
    }
    public static final boolean isSuggestedGap(char c) {
        for(int i = 0 ; i < SUGGESTED_GAP_CHARACTERS.length ; i++) {
            if(c==SUGGESTED_GAP_CHARACTERS[i]) { return true; }
        }
        return false;
    }
    
}
