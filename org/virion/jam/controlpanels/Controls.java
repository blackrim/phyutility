package org.virion.jam.controlpanels;

import javax.swing.*;

/**
 * @author Andrew Rambaut
 * @version $Id: Controls.java 276 2006-03-23 00:37:40Z pepster $
 */
public class Controls {


    public Controls(String title, JPanel panel, boolean isVisible) {
        this(title, panel, isVisible, false, null);
    }

    /**
     * @param title
     * @param panel
     * @param isVisible
     * @param isPinned
     * @param primaryCheckbox the "main" on/off toggle, if any.
     */
    public Controls(String title, JPanel panel, boolean isVisible, boolean isPinned, JCheckBox primaryCheckbox) {
        this.title = title;
        this.panel = panel;
        this.isVisible = isVisible;
        this.isPinned = isPinned;
        this.primaryCheckbox = primaryCheckbox;
    }

    public String getTitle() {
        return title;
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public JCheckBox getPrimaryCheckbox() {
        return primaryCheckbox;
    }

    private String title;
    private JPanel panel;
    private JCheckBox primaryCheckbox;
    private boolean isVisible;

    private boolean isPinned;
}
