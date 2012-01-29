package org.virion.jam.toolbar;

/**
 * @author rambaut
 *         Date: Oct 18, 2005
 *         Time: 10:23:01 PM
 */
public final class ToolbarOptions {

	public static final int ICON_AND_TEXT = 0;
	public static final int ICON_ONLY = 1;
	public static final int TEXT_ONLY = 2;

	public ToolbarOptions(int display, boolean smallSize) {
		this.display = display;
		this.smallSize = smallSize;
	}

	public int getDisplay() {
		return display;
	}

	public boolean getSmallSize() {
		return smallSize;
	}

	private int display = ICON_AND_TEXT;
	private boolean smallSize = false;
}
