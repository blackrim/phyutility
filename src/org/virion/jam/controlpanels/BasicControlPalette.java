package org.virion.jam.controlpanels;

import org.virion.jam.disclosure.DisclosureListener;
import org.virion.jam.disclosure.DisclosurePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: BasicControlPalette.java 276 2006-03-23 00:37:40Z pepster $
 */
public class BasicControlPalette extends JPanel implements ControlPalette {

    public enum DisplayMode {
        DEFAULT_OPEN,
        INITIALLY_OPEN,
        INITIALLY_CLOSED,
        ONLY_ONE_OPEN
    }

    public BasicControlPalette(int preferredWidth) {
        this(preferredWidth, DisplayMode.DEFAULT_OPEN, false);
    }

    /**
     * todo We should probably dump this constructor. We don't really want a different constructor
     * for each style that might be desired. The opening speed is OK, but I don't like passing a
     * title colour. I suggest that properties might be used for this.
     */
    public BasicControlPalette(int preferredWidth, DisplayMode displayMode, boolean fastBlueStyle) {
        this(preferredWidth, displayMode, fastBlueStyle ? 10 : 150);
        this.titleColor = fastBlueStyle ? Color.BLUE : null;
    }

    public BasicControlPalette(int preferredWidth, DisplayMode displayMode, int openingSpeed) {
        this.preferredWidth = preferredWidth;
        this.displayMode = displayMode;
        this.openingSpeed = openingSpeed;
        BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(layout);
    }


    public Dimension getPreferredSize() {
        return new Dimension(preferredWidth, super.getPreferredSize().height);
    }

    public JPanel getPanel() {
        return this;
    }

    public void addControlsProvider(ControlsProvider provider, boolean addAtStart) {
        provider.setControlPalette(this);
        providers.add(addAtStart ? 0 : providers.size(), provider);
    }

    public void fireControlsChanged() {
        for (ControlPaletteListener listener : listeners) {
            listener.controlsChanged();
        }
    }

    public void addControlPanelListener(ControlPaletteListener listener) {
        listeners.add(listener);
    }

    public void removeControlPanelListener(ControlPaletteListener listener) {
        listeners.remove(listener);
    }

    private final List<ControlPaletteListener> listeners = new ArrayList<ControlPaletteListener>();

    public void setupControls() {
        removeAll();
        disclosurePanels.clear();
        controlsList.clear();

        for (ControlsProvider provider : providers) {
            for (Controls controls : provider.getControls(false)) {
                add(Box.createVerticalStrut(1));
                addControls(controls);
            }
        }
        add(Box.createVerticalStrut(Integer.MAX_VALUE));
    }

    private void addControls(final Controls controls) {

        JPanel titlePanel = new JPanel(new BorderLayout(6, 0));
        titlePanel.add(new JLabel(controls.getTitle()), BorderLayout.CENTER);

        JCheckBox pinnedCheck = new JCheckBox();
        pinnedCheck.setFocusPainted(false);

        // This tells Quaqua L&F to use a small check box (ignored otherwise)
        pinnedCheck.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
        pinnedCheck.setSelected(controls.isPinned());
        titlePanel.add(pinnedCheck, BorderLayout.EAST);

        final DisclosurePanel panel = new DisclosurePanel(
                titlePanel, controls.getPanel(), controls.isVisible(), openingSpeed);

        // @todo this is an ugly hack - see comment on Constructor
        if (titleColor != null) {
            panel.getTitleComponent().setForeground(titleColor);
        }

        if (displayMode == DisplayMode.ONLY_ONE_OPEN) {
            panel.addDisclosureListener(new DisclosureListener() {
                public void opening(Component component) {
                }

                public void opened(Component component) {
                    int newlyOpened = disclosurePanels.indexOf(component);
                    if (currentlyOpen >= 0) {
                        DisclosurePanel currentPanel = disclosurePanels.get(currentlyOpen);

                        Controls currentControls = controlsList.get(currentlyOpen);
                        if (!currentControls.isPinned()) {
                            currentPanel.setOpen(false);
                            currentControls.setVisible(false);
                        }
                    }
                    currentlyOpen = newlyOpened;
                    controls.setVisible(true);
                }

                public void closing(Component component) {
                }

                public void closed(Component component) {
                    controls.setVisible(false);
                    int newlyClosed = disclosurePanels.indexOf(component);
                    if (newlyClosed == currentlyOpen) {
                        currentlyOpen = -1;
                    }
                }
            });
        }

        pinnedCheck.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                controls.setPinned(itemEvent.getStateChange() == ItemEvent.SELECTED);
            }
        });
        disclosurePanels.add(panel);
        controlsList.add(controls);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(panel);
    }

    private int preferredWidth;
    private DisplayMode displayMode;
    private int openingSpeed = 50;
    private Color titleColor = null;
    private int currentlyOpen = 0;
    private List<ControlsProvider> providers = new ArrayList<ControlsProvider>();
    private List<DisclosurePanel> disclosurePanels = new ArrayList<DisclosurePanel>();
    private List<Controls> controlsList = new ArrayList<Controls>();

}