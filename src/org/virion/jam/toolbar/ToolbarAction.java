package org.virion.jam.toolbar;

import javax.swing.*;

/**
 * @author rambaut
 *         Date: Oct 18, 2005
 *         Time: 10:10:52 PM
 */
public abstract class ToolbarAction extends AbstractAction {

	protected ToolbarAction(String label, String toolTipText, Icon icon) {
		this(label, toolTipText, icon, null, null);
	}

	protected ToolbarAction(String label, String toolTipText, Icon icon, Icon disabledIcon, Icon pressedIcon) {
		super(label, icon);

		this.label = label;
		this.toolTipText = toolTipText;
		this.icon = icon;
		this.disabledIcon = disabledIcon;
		this.pressedIcon = pressedIcon;
	}

	public String getLabel() {
		return label;
	}

	public Icon getIcon() {
		return icon;
	}

	public Icon getDisabledIcon() {
		return disabledIcon;
	}

	public Icon getPressedIcon() {
		return pressedIcon;
	}

	public String getToolTipText() {
		return toolTipText;
	}

	private String label;
	private String toolTipText;
	private Icon icon;
	private Icon disabledIcon;
	private Icon pressedIcon;
}
