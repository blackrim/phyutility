package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * Build a consensus tree for a set of trees. Base class just check for consistency
 * Work in progress.
 *
 * @author Joseph Heled
 * @version $Id: ConsensusTreeBuilder.java 616 2007-01-09 21:55:13Z pepster $
 */

public abstract class ConsensusTreeBuilder<T extends Tree> implements TreeBuilder<T> {

    /** Name of attribute specifing amount of support for branch */
    final static public String DEFAULT_SUPPORT_ATTRIBUTE_NAME = "Consensus support(%)";

	private String supportAttributeName;
	private boolean supportAsPercent;

    /**
     * Supported consesus methods.
     */
    public enum Method { GREEDY("Greedy"), MRCAC("MRCA Clustering");

        Method(String name) {
           this.name = name;
        }

        public String toString() {
            return getName();
        }

        public String getName() {
            return name;
        }

        private String name;
    }

    /** Number of external nodes/taxa */
    protected final int nExternalNodes;

    /** List of common taxa in all trees */
    protected List<Taxon> taxons;


	/**
	 * Check for consistancy and establish the common taxa
	 * @param trees
	 */
	ConsensusTreeBuilder(Tree[] trees) {
		this(trees, DEFAULT_SUPPORT_ATTRIBUTE_NAME, true);
	}

    /**
     * Check for consistmcy and establish the common taxa
     * @param trees to build summary tree from
     * @param supportAttributeName name of attribute describing amount of support
     * @param asPercent when true, support is in percent (0 - 100), otherwise in number of trees
     * from the set.
     */
    ConsensusTreeBuilder(Tree[] trees, String supportAttributeName, boolean asPercent) {
        Tree first = trees[0];
	    this.supportAttributeName = supportAttributeName;
	    this.supportAsPercent = asPercent;

        nExternalNodes = first.getExternalNodes().size();

        final Set<Taxon> taxa = first.getTaxa();
        taxons = new ArrayList<Taxon>(taxa);

        for (Tree t : trees) {
            final int nExternal = t.getExternalNodes().size();
            if (nExternal != nExternalNodes || !t.getTaxa().containsAll(taxa)) {
                throw new IllegalArgumentException("Non compatible trees");
            }
        }
    }

    abstract public String getMethodDescription(); 

    protected String getSupportDescription(double supportThreshold) {
        String supporDescription;
        if( supportThreshold == 1.0 ) {
           supporDescription = "Strict";
        } else if( supportThreshold == .5 ) {
           supporDescription = "Majority";
        } else {
            supporDescription = "Above " + (100*supportThreshold) + "% support";
        }
        return supporDescription;
    }

    public String getSupportAttributeName() {
		return supportAttributeName;
	}

	public boolean isSupportAsPercent() {
		return supportAsPercent;
	}

    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    protected boolean fireSetProgress(double fractionCompleted) {
	    boolean requestStop = false;
        for (ProgressListener listener : listeners) {
            if (listener.setProgress(fractionCompleted)) {
	            requestStop = true;
            }
        }
	    return requestStop;
    }

    private final List<ProgressListener> listeners = new ArrayList<ProgressListener>();
}