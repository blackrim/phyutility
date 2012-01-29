package jade.data;

public interface DataType {
	char UNKNOWN_CHARACTER = '?';

	char DEFAULT_GAP_CHARACTER = '-';

	/*
	 * types of data
	 */
	int BINARYDATATYPE = 0;

	int NUCLEOTIDEDATATYPE = 1;

	int NUMERICDATATYPE = 2;

	int CONTDATATYPE = 3;

	int UNKNOWN = 666;

	char[] SUGGESTED_GAP_CHARACTERS = { DEFAULT_GAP_CHARACTER, '_', '.' };

	int SUGGESTED_GAP_STATE = -2;

	int SUGGESTED_UNKNOWN_STATE = -1;

	String NUCLEOTIDE_DESCRIPTION = "nucleotide";

	String BINARY_DESCRIPTION = "binary";

	String GAP_BALANCED_DESCRIPTION = "gap balanced";

	String getDescription();

	int getState(char c);

	char getChar(int state);

	int getNumStates();

	char getSuggestedChar(char c);

	int getTypeID();

	boolean isUnknownState(int state);

	boolean isUnknownChar(char c);

	boolean hasGapCharacter();

	boolean isGapChar(char c);

	boolean isGapState(int state);

	int getSuggestedGapState();

	int getSuggestedUnknownState();
}
