package org.virion.jam.toolbar;

import javax.swing.*;

/**
 * @author rambaut
 *         Date: Oct 18, 2005
 *         Time: 10:09:21 PM
 */
public class ToolbarButton extends JButton implements ToolbarItem {

	public ToolbarButton(ToolbarAction action) {
		super(action);

		setHorizontalTextPosition(SwingConstants.CENTER);
		setVerticalTextPosition(SwingConstants.BOTTOM);
		putClientProperty("JButton.buttonType", "toolbar");
		setBorderPainted(false);

	    setToolTipText(action.getToolTipText());

		setDisabledIcon(action.getDisabledIcon());
		setPressedIcon(action.getPressedIcon());
	}

	public void setToolbarOptions(ToolbarOptions options) {
		switch (options.getDisplay()) {
			case ToolbarOptions.ICON_AND_TEXT:
				setText(action.getLabel());
				setIcon(action.getIcon());
				setDisabledIcon(action.getDisabledIcon());
				setPressedIcon(action.getPressedIcon());
				break;
			case ToolbarOptions.ICON_ONLY:
				setText(null);
				setIcon(action.getIcon());
				setDisabledIcon(action.getDisabledIcon());
				setPressedIcon(action.getPressedIcon());
				break;
			case ToolbarOptions.TEXT_ONLY:
				setText(action.getLabel());
				setIcon(null);
				setDisabledIcon(null);
				setPressedIcon(null);
				break;
		}
	}

	public void setAction(Action action) {
		super.setAction(action);
		if (action instanceof ToolbarAction) {
			this.action = (ToolbarAction)action;
		}
	}

	private ToolbarAction action;
}
