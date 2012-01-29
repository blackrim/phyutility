package org.virion.jam.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Richard Moir
 * @version $Id: SimpleListenerManager.java 636 2007-01-31 03:15:55Z matt_kearse $
 */
public class SimpleListenerManager {

    private List<SimpleListener> listeners = new ArrayList<SimpleListener>();

    public SimpleListenerManager(SimpleListenerManager manager) {
        this.listeners = new ArrayList<SimpleListener>(manager.listeners);
    }

    public SimpleListenerManager() {
    }

    public synchronized void add(SimpleListener listener) {
        listeners.add(listener);
    }

    public synchronized void remove(SimpleListener listener) {
        listeners.remove(listener);
    }


    /**
     * calls {@link org.virion.jam.util.SimpleListener#objectChanged()}  on all listeners added using
     * {@link #add(SimpleListener)} .
     */
    public synchronized void fire() {
        for (SimpleListener simpleListener : listeners) {
            simpleListener.objectChanged();
        }
    }

    /**
     * Get the number of listeners (those added, but not yet removed)
     * @return
     */
    public synchronized int size () {
        return listeners.size ();
    }
}
