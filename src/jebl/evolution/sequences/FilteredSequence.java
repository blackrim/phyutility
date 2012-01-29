package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

import java.util.Map;
import java.util.Set;

/**
 * @author rambaut
 * @author Alexei Drummond
 * @version $Id: FilteredSequence.java 641 2007-02-16 11:56:21Z rambaut $
 */
public abstract class FilteredSequence implements Sequence {
    /**
     * Creates a FilteredSequence wrapper to the given source sequence.
     *
     * @param source
     */
    public FilteredSequence(Sequence source) {

        this.source = source;
    }

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    public SequenceType getSequenceType() {
        return source.getSequenceType();
    }

    /**
     * @return a string representing the sequence of symbols.
     */
    public String getString() {
        if (sequence == null) {
            sequence = filterSequence(source);
        }

        StringBuilder buffer = new StringBuilder();
        for (State aSequence : sequence) {
            buffer.append(aSequence.getCode());
        }
        return buffer.toString();
    }

    /**
     * @return an array of state objects.
     */
    public State[] getStates() {
        if (sequence == null) {
            sequence = filterSequence(source);
        }
        return sequence;
    }

    public byte[] getStateIndices() {
        if (sequence == null) {
            sequence = filterSequence(source);
        }
        byte[] stateIndices = new byte[sequence.length];
        int i = 0;
        for (State state : sequence) {
            stateIndices[i] = (byte)state.getIndex();
            i++;
        }
        return stateIndices;
    }

    /**
     * @return the state at site.
     */
    public State getState(int site) {
        if (sequence == null) {
            sequence = filterSequence(source);
        }
        return sequence[site];
    }

    /**
     * Returns the length of the sequence
     *
     * @return the length
     */
    public int getLength() {
        if (sequence == null) {
            sequence = filterSequence(source);
        }
        return sequence.length;
    }

    protected abstract State[] filterSequence(Sequence source);

    /**
     * @return that taxon that this sequence represents
     */
    public Taxon getTaxon() {
        return source.getTaxon();
    }

    public int compareTo(Object o) {
        return source.compareTo(o);
    }

    public String toString() {
        return getString();
    }

    // Attributable implementation

    public void setAttribute(String name, Object value) {
        source.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        return source.getAttribute(name);
    }

    public void removeAttribute(String name) {
        source.removeAttribute(name);
    }

    public Set<String> getAttributeNames() {
        return source.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        return source.getAttributeMap();
    }

    // private members

    private final Sequence source;
    private State[] sequence = null;
}
