/*
 * BasicSequence.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A default implementation of the Sequence interface.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: BasicSequence.java 673 2007-03-28 04:35:52Z matt_kearse $
 */
public class BasicSequence implements Sequence {

    /**
     * Creates a sequence with a name corresponding to the taxon name.
     *
     * Use CharSequence so both a String and a StringBuilder are fine
     *
     * @param taxon
     * @param sequenceString
     */

    public BasicSequence(SequenceType sequenceType, Taxon taxon, CharSequence sequenceString) {

        if (sequenceType == null) {
            throw new IllegalArgumentException("sequenceType is not allowed to be null");
        }
        if (taxon == null) {
            throw new IllegalArgumentException("taxon is not allowed to be null");
        }

        this.sequenceType = sequenceType;
        this.taxon = taxon;
        final int len = sequenceString.length();
        this.sequenceCharacters = new byte[len];

        for (int i = 0; i < len; i++) {
            char c = sequenceString.charAt(i);
            State state = sequenceType.getState(c);

            if (state == null) {
                // Something is wrong. Keep original length by inserting an unknown state
                sequenceCharacters[i] ='?';
            }
            else {
                sequenceCharacters[i] = (byte)c;
            }
        }
    }

    /**
     * Creates a sequence with a name corresponding to the taxon name
     *
     * @param taxon
     * @param sequenceType
     * @param states
     */
    public BasicSequence(SequenceType sequenceType, Taxon taxon, State[] states) {

        this.sequenceType = sequenceType;
        this.taxon = taxon;
        this.sequenceCharacters = new byte[states.length];
        for (int i = 0; i < sequenceCharacters.length; i++) {
            sequenceCharacters[i] = (byte)states[i].getCode().charAt(0);
        }
    }

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    public SequenceType getSequenceType() {
        return sequenceType;
    }

    /**
     * @return a string representing the sequence of symbols.
     */
    public String getString() {
        StringBuilder buffer = new StringBuilder(sequenceCharacters.length);
        for (int i : sequenceCharacters) {
            buffer.append((char) i);
        }
        return buffer.toString();
    }

    public String getCleanString() {
        StringBuilder buffer = new StringBuilder(sequenceCharacters.length);
        for (int i : sequenceCharacters) {
            State state = sequenceType.getState((char)i);
            if (state.isAmbiguous() || state.isGap()) continue;
            buffer.append(sequenceType.getState(i).getCode());
        }
        return buffer.toString();
    }

    /**
     * @return an array of state objects.
     */
    public State[] getStates() {
        return sequenceType.toStateArray(getStateIndices());
    }

    public byte[] getStateIndices() {
        byte results[]=new byte[sequenceCharacters.length];
        for (int i = 0; i < sequenceCharacters.length; i++) {
             results [i] = (byte) getState(i).getIndex();
        }
        return results;
    }


    /**
     * Get the sequence characters representing the sequence.
     * This return is a byte[] rather than a char[]
     * to avoid using twice as much memory as necessary.
     * The individual elements of the returned array can be cast to chars.
     * @return the sequence characters as an array of characters.
     */
    public byte[] getSequenceCharacters() {
        return sequenceCharacters;
    }

    /**
     * @return the state at site.
     */
    public State getState(int site) {
        return sequenceType.getState((char)sequenceCharacters[site]);
    }

    /**
     * Returns the length of the sequence
     *
     * @return the length
     */
    public int getLength() {
        return sequenceCharacters.length;
    }

    /**
     * @return that taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    public Taxon getTaxon() {
        return taxon;
    }

    /**
     * Sequences are compared by their taxa
     *
     * @param o another sequence
     * @return an integer
     */
    public int compareTo(Object o) {
        return taxon.compareTo(((Sequence) o).getTaxon());
    }

    public String toString() {
        return getString();
    }
    
    // Attributable IMPLEMENTATION

    public void setAttribute(String name, Object value) {
        if (helper == null) {
            helper = new AttributableHelper();
        }
        helper.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        if (helper == null) {
            return null;
        }
        return helper.getAttribute(name);
    }

    public void removeAttribute(String name) {
        if (helper != null) {
            helper.removeAttribute(name);
        }
    }

    public Set<String> getAttributeNames() {
        if (helper == null) {
            return Collections.emptySet();
        }
        return helper.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        if (helper == null) {
            return Collections.emptyMap();
        }
        return helper.getAttributeMap();
    }

    private AttributableHelper helper = null;

    // private members

    private final Taxon taxon;
    private final SequenceType sequenceType;
    private final byte[] sequenceCharacters; // this is really an array of characters, but using bytes since we don't store high-ascii characters

   // private Map<String, Object> attributeMap = null;
}
