package org.virion.jam.panels;

/**
 * @author rambaut
 *         Date: Jul 27, 2004
 *         Time: 10:04:04 AM
 */
public interface StatusListener {

    /**
     * Called when the status is to be changed.
     * @param status the status constant
     * @param statusText the status text
     */
    void statusChanged(int status, String statusText);

}
