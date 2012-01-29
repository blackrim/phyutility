package org.virion.jam.controlpalettes;

import javax.swing.*;
import java.util.Map;

/**
 * Date: 20/03/2006
 * Time: 10:23:21
 *
 * @author Joseph Heled
 * @version $Id: ControlPalette.java 485 2006-10-25 15:24:54Z rambaut $
 */
public interface ControlPalette {

    /**
     * get the panel that encloses the control palette
     * @return the panel
     */
    JPanel getPanel();

    /**
     * install a Controller into the palette
     * @param controller
     */
    void addController(Controller controller);

    /**
     * tell listeners that the palette has changed
     */
    void fireControlsChanged();

    /**
     * Add a listener to this palette
     * @param listener
     */
    void addControlPaletteListener(ControlPaletteListener listener);

    /**
     * Remove a listener fromm this palette
     * @param listener
     */
    void removeControlPaletteListener(ControlPaletteListener listener);

    /**
     * Initialize all controllers when a new document is created. At this
     * point, settings can be adjusted to match the contents of the document.
     */
    void initialize();

    /**
     * Gather up all the settings from all the controls in the palette.
     * This would usually called before saving them with the document
     * that the palette controls.
     * @param settings
     */
    void getSettings(Map<String,Object> settings);

    /**
     * Distribute all the settings to all the controls in the palette.
     * This would usually called after loading the document
     * that the palette controls.
     * @param settings
     */
    void setSettings(Map<String,Object> settings);
}
