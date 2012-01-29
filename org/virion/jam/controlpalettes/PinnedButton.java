package org.virion.jam.controlpalettes;

import org.virion.jam.util.IconUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Andrew Rambaut
 * @version $Id: PinnedButton.java 430 2006-08-26 19:19:18Z rambaut $
 */
public class PinnedButton extends JToggleButton {

	public PinnedButton() {

		putClientProperty("JButton.buttonType", "toolbar");
		setBorderPainted(false);
		// this is required on Windows XP platform -- untested on Macintosh
		setContentAreaFilled(false);

		setupIcon();
		setRolloverEnabled(true);

		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// the button will already have changed state
				setupIcon();
			}
		});
	}

	private void setupIcon() {
		if (isSelected()) {
			setIcon(PinnedButton.pinInIcon);
			setRolloverIcon(pinInRolloverIcon);
		} else {
			setIcon(PinnedButton.pinOutIcon);
			setRolloverIcon(pinOutRolloverIcon);
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

	private static Icon pinOutIcon = null;
	private static Icon pinOutRolloverIcon = null;
	private static Icon pinInIcon = null;
	private static Icon pinInRolloverIcon = null;

	static {
		PinnedButton.pinOutIcon = IconUtils.getIcon(PinnedButton.class, "images/pinOut.png");
		PinnedButton.pinOutRolloverIcon = IconUtils.getIcon(PinnedButton.class, "images/pinOutRollover.png");
		PinnedButton.pinInIcon = IconUtils.getIcon(PinnedButton.class, "images/pinIn.png");
		PinnedButton.pinInRolloverIcon = IconUtils.getIcon(PinnedButton.class, "images/pinInRollover.png");
	}

}
