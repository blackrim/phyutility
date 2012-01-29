/**
 * MenuBarFactory.java
 */

package org.virion.jam.framework;

import javax.swing.*;

public interface MenuFactory {

	public final static int LEFT = 0;
	public final static int CENTER = 1;
	public final static int RIGHT = 2;

	/**
	 * Give the name of this menu. If multiple MenuFactories are
	 * registered with the same name, then these will be appended
	 * into a single actual menu.
	 */
	String getMenuName();

	/**
	 * This method should populate the menu with menu items. Reference
	 * can be made to the frame in order to get Actions.
	 * @param menu
	 * @param frame
	 */
    void populateMenu(JMenu menu, AbstractFrame frame);

	/**
	 * Returns the preferred alignment of the menu in the menu bar. This
	 * should be one of MenuFactory.LEFT, MenuFactory.CENTER or MenuFactory.RIGHT.
	 * @return the alignment
	 */
	int getPreferredAlignment();
}