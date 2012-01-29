package org.virion.jam.disclosure;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import org.virion.jam.util.IconUtils;

/**
 * @author Andrew Rambaut
 * @version $Id: DisclosureButton.java 430 2006-08-26 19:19:18Z rambaut $
 */
public class DisclosureButton extends JToggleButton {

    private Timer timer = null;
    private boolean opening;
    private final List<DisclosureListener> listeners = new ArrayList<DisclosureListener>();
    private int animationSpeed;

    public DisclosureButton() {
        this(150);
    }
    public DisclosureButton(int animationSpeed) {
        this.animationSpeed = animationSpeed;
        putClientProperty("JButton.buttonType", "toolbar");
        setBorderPainted(false);
        // this is required on Windows XP platform -- untested on Macintosh
        setContentAreaFilled(false);

        setupIcon();

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // the button will already have changed state
                opening = isSelected();
                current = (opening ? 0: 2);
                startAnimation();
            }
        });
    }

    public void addDisclosureListener(DisclosureListener listener) {
        listeners.add(listener);
    }

    public void removeDisclosureListener(DisclosureListener listener) {
        listeners.remove(listener);
    }

    private void setupIcon() {
        if (isSelected()) {
            setIcon(downIcon);
            //setSelectedIcon(downPressedIcon);
        } else {
            setIcon(rightIcon);
            //setSelectedIcon(rightPressedIcon);
        }
    }

    /**
     * This overridden because when the button is programmatically selected,
     * we want to skip the animation and jump straight to the final icon.
     * @param isSelected
     */
    public void setSelected(boolean isSelected) {
        super.setSelected(isSelected);
        setupIcon();
    }

    private void startAnimation() {
        if (disclosureIcons == null) return;

        timer = new Timer(animationSpeed, listener);
        timer.setCoalesce(false);
        timer.start();
    }

    private void stopAnimation() {
        if (timer == null) return;
        timer.stop();
        setupIcon();
    }

    private int current;
    ActionListener listener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            if (opening) {
                current++;
            } else {
                current--;
            }
            if (current < 0 || current >= disclosureIcons.length) {
                stopAnimation();
                return;
            }
            if (current == 1) {
                if (opening) {
                    fireOpening();
                } else {
                    fireClosing();
                }
            } else {
                if (opening) {
                    fireOpened();
                } else {
                    fireClosed();
                }
            }
            setIcon(disclosureIcons[current]);
            paintImmediately(0, 0, getWidth(), getHeight());
        }
    };

    private void fireOpening() {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((DisclosureListener)iter.next()).opening(this);
        }
    }

    private void fireOpened() {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((DisclosureListener)iter.next()).opened(this);
        }
    }

    private void fireClosing() {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((DisclosureListener)iter.next()).closing(this);
        }
    }

    private void fireClosed() {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            ((DisclosureListener)iter.next()).closed(this);
        }
    }

    private static Icon[] disclosureIcons = null;
    private static Icon rightIcon = null;
    private static Icon rightPressedIcon = null;
    private static Icon rightDownIcon = null;
    private static Icon downIcon = null;
    private static Icon downPressedIcon = null;

    static {
        try {
            rightIcon = IconUtils.getIcon(DisclosureButton.class, "images/disclosureRightNormal.png");
            rightPressedIcon = IconUtils.getIcon(DisclosureButton.class, "images/disclosureRightPressed.png");

            rightDownIcon = IconUtils.getIcon(DisclosureButton.class, "images/disclosureRightDown.png");

            downIcon = IconUtils.getIcon(DisclosureButton.class, "images/disclosureDownNormal.png");
            downPressedIcon = IconUtils.getIcon(DisclosureButton.class, "images/disclosureDownPressed.png");

            disclosureIcons = new Icon[3];

            disclosureIcons[0] = rightPressedIcon;
            disclosureIcons[1] = rightDownIcon;
            disclosureIcons[2] = downPressedIcon;

        } catch (Exception e) {
            // no icons...
        }
    }

}
