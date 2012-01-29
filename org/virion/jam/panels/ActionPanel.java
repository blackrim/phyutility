package org.virion.jam.panels;

import org.virion.jam.util.IconUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Creates a panel consisting of three buttons: an add button, a remove button
 * and an action button. At present these will look similar to buttons found in
 * Apple Mac OS X applications such as Mail. In future this class could be given
 * appropriate LAF classes to render appropriate to the platform.
 * @author rambaut
 *         Date: Jul 29, 2004
 *         Time: 9:38:58 AM
 */
public class ActionPanel extends JPanel {
    private JButton addButton;
    private JButton removeButton;
    private JButton actionButton;

    private Icon addIcon;
    private Icon removeIcon;
    private Icon actionIcon;

    public ActionPanel() {
        this(true);
    }

    public ActionPanel(boolean useActionButton) {
        setLayout(new FlowLayout(java.awt.FlowLayout.LEFT,0,0));
        setOpaque(false);

        addButton = new JButton("+");
        addButton.putClientProperty("JButton.buttonType", "toolbar");

        addIcon = IconUtils.getIcon(ActionPanel.class, "images/add/addButton.png");
	    if (addIcon != null) {
		    addButton.setIcon(addIcon);
		    addButton.setPressedIcon(IconUtils.getIcon(ActionPanel.class, "images/add/addButtonPressed.png"));
		    addButton.setDisabledIcon(IconUtils.getIcon(ActionPanel.class, "images/add/addButtonInactive.png"));
		    addButton.setText(null);
		    addButton.setPreferredSize(new Dimension(addIcon.getIconWidth(), addIcon.getIconHeight()));
	    }
        addButton.setBorderPainted(false);
        addButton.setOpaque(false);
        // this is required on Windows XP platform -- untested on Macintosh
        addButton.setContentAreaFilled(false);

        removeButton = new JButton("-");
        removeButton.putClientProperty("JButton.buttonType", "toolbar");

        removeIcon = IconUtils.getIcon(ActionPanel.class, "images/remove/removeButton.png");
	    if (removeIcon != null) {
		    removeButton.setIcon(removeIcon);
		    removeButton.setPressedIcon(IconUtils.getIcon(ActionPanel.class, "images/remove/removeButtonPressed.png"));
		    removeButton.setDisabledIcon(IconUtils.getIcon(ActionPanel.class, "images/remove/removeButtonInactive.png"));
		    removeButton.setText(null);
		    removeButton.setPreferredSize(new Dimension(removeIcon.getIconWidth(), removeIcon.getIconHeight()));
	    }
        removeButton.setBorderPainted(false);
        removeButton.setOpaque(false);
        // this is required on Windows XP platform -- untested on Macintosh
        removeButton.setContentAreaFilled(false);

        add(addButton);
        add(removeButton);

        if (useActionButton) {
            actionButton = new JButton("*");
            actionButton.putClientProperty("JButton.buttonType", "toolbar");

            actionIcon = IconUtils.getIcon(ActionPanel.class, "images/action/actionButton.png");
	        if (actionIcon != null) {
		        actionButton.setIcon(actionIcon);
		        actionButton.setPressedIcon(IconUtils.getIcon(ActionPanel.class, "images/action/actionButtonPressed.png"));
		        actionButton.setDisabledIcon(IconUtils.getIcon(ActionPanel.class, "images/action/actionButtonInactive.png"));
		        actionButton.setText(null);
		        actionButton.setPreferredSize(new Dimension(actionIcon.getIconWidth(), actionIcon.getIconHeight()));
	        }
            actionButton.setBorderPainted(false);
            actionButton.setOpaque(false);
            // this is required on Windows XP platform -- untested on Macintosh
            actionButton.setContentAreaFilled(false);

            add(new JToolBar.Separator(new Dimension(6,6)));
            add(actionButton);
        }
    }

    public void setAddAction(Action action) {
        addButton.setAction(action);
        addButton.setIcon(addIcon);
        addButton.setText(null);
    }

    public void setAddToolTipText(String text) {
        addButton.setToolTipText(text);
    }

    public void setRemoveAction(Action action) {
        removeButton.setAction(action);
        removeButton.setIcon(removeIcon);
        removeButton.setText(null);
    }

    public void setRemoveToolTipText(String text) {
        removeButton.setToolTipText(text);
    }

    public void setActionAction(Action action) {
        actionButton.setAction(action);
        actionButton.setIcon(actionIcon);
        actionButton.setText(null);
    }

    public void setActionToolTipText(String text) {
        actionButton.setToolTipText(text);
    }

}
