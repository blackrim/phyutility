package jade.data;

import java.util.*;
import java.io.*;

abstract public class AbstractAlignment {
	/*
	 * the character for an unknown character
	 */
	static char UNKNOWN = DataType.UNKNOWN_CHARACTER;

	static char GAP = DataType.DEFAULT_GAP_CHARACTER;

	/** Characters that might be used as gaps */
	static String GAPS = "_-?.";

	/*
	 * abstract constructor
	 */
	public AbstractAlignment() {
	}

	/** number of sequences */
	protected int numSeqs;

	/** length of each sequence */
	protected int numSites;

	/** sequence identifiers */
	protected ArrayList<String> seqIDs;

	/** data type */
	private DataType dataType;

	/** sequence alignment at (sequence, site) */
	abstract public char getData(int seq, int site);

	/**
	 * returns true if there is a gap in the give position.
	 */
	public boolean isGap(int seq, int site) {
		return dataType.isGapChar(getData(seq, site));
	}

	public void guessDataType() {
		getSuitableInstance();
	}

	private void getSuitableInstance() {
		// count A, C, G, T, U, N
		long numNucs = 0;
		long numChars = 0;
		long numBins = 0;
		for (int i = 0; i < numSeqs; i++) {
			for (int j = 0; j < numSites; j++) {
				char c = getData(i, j);

				if (c == 'A' || c == 'C' || c == 'G' || c == 'T' || c == 'U'
				        || c == 'N')
					numNucs++;

				if (c != '-' && c != '?')
					numChars++;

				if (c == '0' || c == '1')
					numBins++;
			}
		}

		if (numChars == 0)
			numChars = 1;

		// more than 85 % frequency advocates nucleotide data
		if ((double) numNucs / (double) numChars > 0.85) {
			dataType = new NucleotideDataType();
		} else if ((double) numBins / (double) numChars > 0.2) {
			dataType = new BinaryDataType();
		}
	}

	/**
	 * Same as getDataType().getChar(state)
	 */
	protected final char getChar(int state) {
		return dataType.getChar(state);
	}

	/**
	 * Same as getDataType().getState(char)
	 */
	protected final int getState(char c) {
		return dataType.getState(c);
	}

	/**
	 * Same as getDataType().isUnknownState(state)
	 */
	protected final boolean isUnknownState(int state) {
		return dataType.isUnknownState(state);
	}

	/** Returns the datatype of this alignment */
	public final DataType getDataType() {
		return dataType;
	}

	/** Sets the datatype of this alignment */
	public final void setDataType(DataType d) {
		dataType = d;
	}

	/** returns representation of this alignment as a string */
	public String toString() {
		StringWriter sw = new StringWriter();
		// AlignmentUtils.print(this, new PrintWriter(sw));
		return sw.toString();
	}

	// interface Report

	public void report(PrintWriter out) {
		// AlignmentUtils.report(this, out);
	}

	/**
	 * Fills a [numsequences][length] matrix with indices. Each index represents
	 * the sequence state, -1 means a gap.
	 */
	public int[][] getStates() {

		int[][] indices = new int[numSeqs][numSites];

		for (int i = 0; i < numSeqs; i++) {
			// int seqcounter = 0;

			for (int j = 0; j < numSites; j++) {

				indices[i][j] = dataType.getState(getData(i, j));

				if (indices[i][j] >= dataType.getNumStates()) {
					indices[i][j] = -1;
				}
			}
		}

		return indices;
	}

	/**
	 * Return number of sites in this alignment
	 */
	public final int getLength() {
		return numSites;
	}

	/**
	 * Return number of sequences in this alignment
	 */
	public final int getSequenceCount() {
		return numSeqs;
	}

	/**
	 * Return number of sites for each sequence in this alignment
	 * 
	 * @note for people who like accessor methods over public instance
	 *       variables...
	 */
	public final int getSiteCount() {
		return numSites;
	}

	/**
	 * Returns a string representing a single sequence (including gaps) from
	 * this alignment.
	 */
	public String getAlignedSequenceString(int seq) {
		char[] data = new char[numSites];
		for (int i = 0; i < numSites; i++) {
			data[i] = getData(seq, i);
		}
		return new String(data);
	}

	// seq names
	public String getIdentifier(int i) {
		return seqIDs.get(i);
	}

	public void setIdentifier(int i, String name) {
		seqIDs.set(i,name);
	}

	public int getIdCount() {
		return seqIDs.size();
	}

	public int whichIdNumber(String name) {
		int j = 0;
		for (int i = 0; i < seqIDs.size(); i++) {
			if (seqIDs.get(i).compareTo(name) == 0)
				j = i;
		}
		return j;
	}

}
