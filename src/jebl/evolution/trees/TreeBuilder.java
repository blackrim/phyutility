package jebl.evolution.trees;

import jebl.util.ProgressListener;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: TreeBuilder.java 301 2006-04-17 15:35:01Z rambaut $
 */
public interface TreeBuilder<T extends Tree> {

    T build();

    void addProgressListener(ProgressListener listener);

    void removeProgressListener(ProgressListener listener);
}
