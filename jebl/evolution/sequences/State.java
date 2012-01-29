/*
 * State.java
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
 * @version $Id: State.java 650 2007-03-12 20:09:10Z twobeers $
 */
public abstract class State implements Comparable {

    State(String name, String stateCode, int index) {
        this.name = name;
        this.stateCode = stateCode;

        // TT: is there any reason why instead of the next three lines we don't just say 
        // this.ambiguities = Collections.singleton(this)  ?
        List<State> ambiguities = new ArrayList<State>();
        ambiguities.add(this);
        this.ambiguities = Collections.unmodifiableSortedSet(new TreeSet<State>(ambiguities));
        this.index = index;
    }

    State(String name, String stateCode, int index, State[] ambiguities) {
        this.name = name;
        this.stateCode = stateCode;
        this.ambiguities = Collections.unmodifiableSortedSet(new TreeSet<State>(Arrays.asList(ambiguities)));
        this.index = index;
    }

    public String getCode() {
        return stateCode;
    }

    public int getIndex() {
        return index;
    }

    public String getName() { return name; }

    public boolean isAmbiguous() {
        return getCanonicalStates().size() > 1;
    }

    public Set<State> getCanonicalStates() {
        return ambiguities;
    }

    /**
     * @param other another state to check for the quality with.
     * @return true if the other state is or possibly is equal to this state, taking ambiguities into account,
     *         i.e. if the ambiguity sets of this and the other state intersect.
     */
    public boolean possiblyEqual(State other) {
        for (State state : getCanonicalStates()) {
            for (State state1 : other.getCanonicalStates()) {
                if(state.equals (state1)) return true;
            }
        }
        return false;
    }

    public int compareTo(Object o) {
        return index - ((State)o).index;
    }

    public boolean equals(Object o) {
        return (o instanceof State) && (this.index == ((State) o).index);
    }

    public int hashCode() {
        return index;
    }

    public String toString() { return stateCode; }

	public abstract boolean isGap();

	private String stateCode;
    private String name;
    private Set<State> ambiguities;
    private int index;
}
