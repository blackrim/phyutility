/*
 * CodonState.java
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
 * @version $Id: CodonState.java 225 2006-02-16 14:50:36Z rambaut $
 */
public final class CodonState extends State {

    CodonState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    CodonState(String name, String stateCode, int index, CodonState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

	public boolean isGap() {
		return this == Codons.GAP_STATE;
	}
}
