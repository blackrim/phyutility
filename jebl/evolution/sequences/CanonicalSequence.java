package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.Set;
import java.util.Collections;
import java.util.Map;

/**
 * A default implementation of the Sequence interface
 * that converts sequence characters to
 * States  such that calling getString() will always return
 * uppercase residues with nucleotide U residues converted to T
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: BasicSequence.java 641 2007-02-16 11:56:21Z rambaut $
 */
public class CanonicalSequence implements Sequence {

    /**
     * Creates a sequence with a name corresponding to the taxon name.
     *
     * Use CharSequence so both a String and a StringBuilder are fine
     *
     * @param taxon
     * @param sequenceString
     */

    public CanonicalSequence(SequenceType sequenceType, Taxon taxon, CharSequence sequenceString) {

        if (sequenceType == null) {
            throw new IllegalArgumentException("sequenceType is not allowed to be null");
        }
        if (taxon == null) {
            throw new IllegalArgumentException("taxon is not allowed to be null");
        }

        this.sequenceType = sequenceType;
        this.taxon = taxon;
        final int len = sequenceString.length();
        this.sequence = new byte[len];

        for (int i = 0; i < len; i++) {
            State state = sequenceType.getState(sequenceString.charAt(i));

            if (state == null) {
                // Something is wrong. Keep original length by inserting an unknown state
                state = sequenceType.getUnknownState();
            }
            sequence[i] = (byte)state.getIndex();
        }
    }

    /**
     * Creates a sequence with a name corresponding to the taxon name
     *
     * @param taxon
     * @param sequenceType
     * @param states
     */
    public CanonicalSequence(SequenceType sequenceType, Taxon taxon, State[] states) {

        this.sequenceType = sequenceType;
        this.taxon = taxon;
        this.sequence = new byte[states.length];
        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = (byte)states[i].getIndex();
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
        StringBuilder buffer = new StringBuilder(sequence.length);
        for (int i : sequence) {
            buffer.append(sequenceType.getState(i).getCode());
        }
        return buffer.toString();
    }

    public String getCleanString() {
        StringBuilder buffer = new StringBuilder(sequence.length);
        for (int i : sequence) {
            State state = sequenceType.getState(i);
            if (state.isAmbiguous() || state.isGap()) continue;
            buffer.append(sequenceType.getState(i).getCode());
        }
        return buffer.toString();
    }

    /**
     * @return an array of state objects.
     */
    public State[] getStates() {
        return sequenceType.toStateArray(sequence);
    }

    public byte[] getStateIndices() {
        return sequence;
    }

    /**
     * @return the state at site.
     */
    public State getState(int site) {
        return sequenceType.getState(sequence[site]);
    }

    /**
     * Returns the length of the sequence
     *
     * @return the length
     */
    public int getLength() {
        return sequence.length;
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
    private final byte[] sequence;

   // private Map<String, Object> attributeMap = null;
}
