/*
 * NucleotideState.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: NucleotideState.java 267 2006-03-21 02:36:55Z twobeers $
 */
public final class NucleotideState extends State {

    NucleotideState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    NucleotideState(String name, String stateCode, int index, NucleotideState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

    public int compareTo(Object o) {
        // throws ClassCastException on across-class comparison
        NucleotideState that = (NucleotideState) o;
        return super.compareTo(that);
    }

    // we do not need to override equals because there is only one
    // unique instance of each nucleotide state - i.e. we can use ==
    /*
    public boolean equals(Object o) {
        if (!(o instanceof NucleotideState))
            return false;
        return super.equals(o);
    } */

    public int hashCode() {
        return 23 * super.hashCode() + 17;
    }

    public boolean isGap() {
		return this == Nucleotides.GAP_STATE;
	}
}
