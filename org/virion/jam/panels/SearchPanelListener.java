package org.virion.jam.panels;

/**
 * An interface for listeners to the SearchPanel class.
 * @author Andrew Rambaut
 * Date: Jul 26, 2004
 * Time: 5:37:15 PM
 */
public interface SearchPanelListener {

    /**
     * Called when the user requests a search by pressing return having
     * typed a search string into the text field. If the continuousUpdate
     * flag is true then this method is called when the user types into
     * the text field.
     * @param searchString the user's search string
     */
    void searchStarted(String searchString);

    /**
     * Called when the user presses the cancel search button or presses
     * escape while the search is in focus.
     */
    void searchStopped();

}
