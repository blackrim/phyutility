package org.virion.jam.controlpalettes;

import org.virion.jam.disclosure.DisclosureListener;
import org.virion.jam.disclosure.DisclosurePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id: BasicControlPalette.java 485 2006-10-25 15:24:54Z rambaut $
 */
public class BasicControlPalette extends JPanel implements ControlPalette {

    public final static int DEFAULT_OPENING_SPEED = 50;

    public enum DisplayMode {
        DEFAULT_OPEN,
        INITIALLY_OPEN,
        INITIALLY_CLOSED,
        ONLY_ONE_OPEN
    }

    public BasicControlPalette(int preferredWidth) {
        this(preferredWidth, DisplayMode.ONLY_ONE_OPEN, DEFAULT_OPENING_SPEED);
    }

    public BasicControlPalette(int preferredWidth, DisplayMode displayMode) {
        this(preferredWidth, displayMode, DEFAULT_OPENING_SPEED);
    }

    public BasicControlPalette(int preferredWidth, DisplayMode displayMode, int openingSpeed) {
        this.preferredWidth = preferredWidth;
        this.displayMode = displayMode;
        this.openingSpeed = openingSpeed;
        BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(layout);
        setOpaque(true);
    }


    public Dimension getPreferredSize() {
        return new Dimension(preferredWidth, super.getPreferredSize().height);
    }

    public JPanel getPanel() {
        return this;
    }

    private ControllerListener controllerListener = new ControllerListener() {
        public void controlsChanged() {
            layoutControls();
        }
    };

    public void addController(Controller controller) {
        controllers.add(controller);
        controller.addControllerListener(controllerListener);
        setupControls();
    }

    public void addController(int position, Controller controller) {
        controllers.add(position, controller);
        controller.addControllerListener(controllerListener);
        setupControls();
    }

    public void removeController(Controller controller) {
        controller.removeControllerListener(controllerListener);
        controllers.remove(controller);
        setupControls();
    }

    public int getControllerCount() {
        return controllers.size();
    }

    public void fireControlsChanged() {
        for (ControlPaletteListener listener : listeners) {
            listener.controlsChanged();
        }
    }

    public void addControlPaletteListener(ControlPaletteListener listener) {
        listeners.add(listener);
    }

    public void removeControlPaletteListener(ControlPaletteListener listener) {
        listeners.remove(listener);
    }

    private final List<ControlPaletteListener> listeners = new ArrayList<ControlPaletteListener>();

    private void setupControls() {
        removeAll();
        disclosurePanels.clear();
        controlsStates.clear();

        for (Controller controller : controllers) {
            add(Box.createVerticalStrut(1));
            setupController(controller);
        }
        add(Box.createVerticalStrut(Integer.MAX_VALUE));
    }

    public void layoutControls() {
        for (DisclosurePanel panel : disclosurePanels) {
            panel.invalidate();
        }
        validate();
    }

    public void initialize() {
        for (Controller controller : controllers) {
            controller.initialize();
        }
    }

    public void getSettings(Map<String,Object> settings) {
        for (Controller controller : controllers) {
            controller.getSettings(settings);
        }
    }

    public void setSettings(Map<String,Object> settings) {
        for (Controller controller : controllers) {
            controller.setSettings(settings);
        }
    }

    private void setupController(Controller controller) {

        JPanel titlePanel = new JPanel(new BorderLayout(6, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(controller.getTitleComponent(), BorderLayout.CENTER);

        JPanel controllerPanel = controller.getPanel();
        controllerPanel.setOpaque(false);

		// This tells Quaqua L&F to use a small components (ignored otherwise)
        controller.getTitleComponent().setFont(UIManager.getFont("SmallSystemFont"));
        controller.getTitleComponent().setOpaque(false);

//		JCheckBox pinnedCheck = new JCheckBox();
//		pinnedCheck.setFocusPainted(false);
//
//		pinnedCheck.setSelected(controller.isInitiallyVisible());
//		titlePanel.add(pinnedCheck, BorderLayout.EAST);
        PinnedButton pinnedButton = new PinnedButton();

        // This tells Quaqua L&F to use a small check box (ignored otherwise)
        pinnedButton.setSelected(controller.isInitiallyVisible());
        titlePanel.add(pinnedButton, BorderLayout.EAST);

        final DisclosurePanel panel = new DisclosurePanel(
                titlePanel, controllerPanel, controller.isInitiallyVisible(), openingSpeed);

        if (displayMode == DisplayMode.ONLY_ONE_OPEN) {
            panel.addDisclosureListener(new DisclosureListener() {
                public void opening(Component component) {
                }

                public void opened(Component component) {
                    int newlyOpened = disclosurePanels.indexOf(component);
                    ControlsState controlsState = controlsStates.get(newlyOpened);

                    if (currentlyOpen >= 0) {
                        DisclosurePanel currentPanel = disclosurePanels.get(currentlyOpen);

                        ControlsState currentControls = controlsStates.get(currentlyOpen);
                        if (!currentControls.isPinned()) {
                            currentPanel.setOpen(false);
                            currentControls.setVisible(false);
                        }
                    }
                    currentlyOpen = newlyOpened;
                    controlsState.setVisible(true);
                }

                public void closing(Component component) {
                }

                public void closed(Component component) {
                    int newlyClosed = disclosurePanels.indexOf(component);
                    ControlsState controlsState = controlsStates.get(newlyClosed);
                    controlsState.setVisible(false);

                    if (newlyClosed == currentlyOpen) {
                        currentlyOpen = -1;
                    }
                }
            });
        }

        final ControlsState controlsState = new ControlsState(
                controller.isInitiallyVisible(),
                pinnedButton.isSelected());

        pinnedButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                controlsState.setPinned(itemEvent.getStateChange() == ItemEvent.SELECTED);
            }
        });
        disclosurePanels.add(panel);
        controlsStates.add(controlsState);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(panel);
    }

    private int preferredWidth;
    private DisplayMode displayMode;
    private int openingSpeed = 50;
    private int currentlyOpen = 0;
    private List<Controller> controllers = new ArrayList<Controller>();
    private List<DisclosurePanel> disclosurePanels = new ArrayList<DisclosurePanel>();
    private List<ControlsState> controlsStates = new ArrayList<ControlsState>();

    private class ControlsState {
        ControlsState(boolean visible, boolean pinned) {
            isVisible = visible;
            isPinned = pinned;
        }

        boolean isVisible() {
            return isVisible;
        }

        void setVisible(boolean visible) {
            isVisible = visible;
        }

        boolean isPinned() {
            return isPinned;
        }

        void setPinned(boolean pinned) {
            isPinned = pinned;
        }

        private boolean isVisible;
        private boolean isPinned;
    }
}