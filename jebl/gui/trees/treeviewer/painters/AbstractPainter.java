package jebl.gui.trees.treeviewer.painters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: AbstractPainter.java 181 2006-01-23 17:31:10Z rambaut $
 */
public abstract class AbstractPainter<T> implements Painter<T> {
    public void addPainterListener(PainterListener listener) {
        listeners.add(listener);
    }

    public void removePainterListener(PainterListener listener) {
        listeners.remove(listener);
    }

    public void firePainterChanged() {
        for (PainterListener listener : listeners) {
            listener.painterChanged();
        }
    }
    private final List<PainterListener> listeners = new ArrayList<PainterListener>();
}
