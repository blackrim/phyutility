package org.virion.jam.controlpalettes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AbstractController.java 363 2006-06-27 16:26:09Z rambaut $
 */
public abstract class AbstractController implements Controller {
    /**
     * Add a ControllerListener to this controllers list of listeners
     * The main listener will be the ControlPalette itself which will use
     * this to resize the panels if the components changed
     *
     * @param listener
     */
    public void addControllerListener(ControllerListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     *
     * @param listener
     */
    public void removeControllerListener(ControllerListener listener) {
        listeners.remove(listener);
    }

    public void fireControllerChanged() {
        for (ControllerListener listener : listeners) {
            listener.controlsChanged();
        }
    }

    private final List<ControllerListener> listeners = new ArrayList<ControllerListener>();
}
