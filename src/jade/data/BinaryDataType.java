/*
 * BinaryDataType.java
 *
 * Created on April 18, 2005, 8:26 PM
 */

package jade.data;

/**
 *
 * @author stephensmith
 */
public class BinaryDataType implements DataType{
    
    /** Creates a new instance of BinaryDataType */
    public BinaryDataType() {}
    public static final BinaryDataType DEFAULT_INSTANCE = new BinaryDataType();
    
    public int getNumStates() {return 2;}
    
    // Get state corresponding to character c
    public int getStateCorr(char c) {
        switch (c) {
            case '0':
                return 0;
            case '1':
                return 1;
                
            case UNKNOWN_CHARACTER:
                return 2;
                
            default:
                return 2;
        }
    }
    

    protected final boolean isUnknownStateCorr(final int state) {
        return(state>=2);
    }
    
    // Get character corresponding to a given state
    protected char getCharCorr(final int state) {
        switch (state) {
            case 0:
                return '0';
            case 1:
                return '1';
                
            case 2:
                return UNKNOWN_CHARACTER;
                
            default:
                return UNKNOWN_CHARACTER;
        }
    }
    
    // String describing the data type
    public String getDescription() {
        return BINARY_DESCRIPTION;
    }
    
    // Get numerical code describing the data type
    public int getTypeID() {
        return 2;
    }
    /**
     * Handles gap char and then passes on to getStateImpl
     */
    public final int getState(char c) {
        if(isSuggestedGap(c)) { return SUGGESTED_GAP_STATE; }
        return getStateCorr(c);
    }
    /**
     * Handles gap state and then passes on to getStateImpl
     */
    public final char getChar(final int state) {
        if(state==SUGGESTED_GAP_STATE) { return DEFAULT_GAP_CHARACTER; }
        if(state<0) { return UNKNOWN_CHARACTER; }
        return getCharCorr(state);
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
    
    public final boolean isUnknownChar(final char c) {
        return isUnknownState(getState(c));
    }

    public final boolean isUnknownState(final int state) {
        return(state==SUGGESTED_GAP_STATE||isUnknownStateCorr(state));
    }

    public String toString() { return getDescription(); }

    public int getSuggestedUnknownState() { return SUGGESTED_UNKNOWN_STATE; }
    
    public final boolean hasGapCharacter() { return true; }
    /**
     * @return true if this character is a '.' or a '_'
     */
    public final boolean isGapChar(final char c) {
        return isSuggestedGap(c);
    }
    
    /**
     * @return true if state is gap state (-2), false other wise
     */
    public final boolean isGapState(final int state) { return state==SUGGESTED_GAP_STATE; }
    
    /**
     * @return GAP_STATE (-2)
     */
    public final int getSuggestedGapState() { return SUGGESTED_GAP_STATE; }
    //method used above
    public static final boolean isSuggestedGap(char c) {
        for(int i = 0 ; i < SUGGESTED_GAP_CHARACTERS.length ; i++) {
            if(c==SUGGESTED_GAP_CHARACTERS[i]) { return true; }
        }
        return false;
    }

}