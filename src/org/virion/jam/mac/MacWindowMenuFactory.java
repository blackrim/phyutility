package org.virion.jam.mac;

import org.virion.jam.framework.MenuFactory;
import org.virion.jam.framework.AbstractFrame;
import org.virion.jam.framework.Application;
import org.virion.jam.framework.MenuBarFactory;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author rambaut
 *         Date: Dec 26, 2004
 *         Time: 11:03:39 AM
 */
public class MacWindowMenuFactory implements MenuFactory {
	public String getMenuName() {
		return "Window";
	}

	public void populateMenu(JMenu menu, AbstractFrame frame) {

		Application application = Application.getApplication();

		JMenuItem item;

		item = new JMenuItem(frame.getZoomWindowAction());
		menu.add(item);

		item = new JMenuItem(frame.getMinimizeWindowAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, MenuBarFactory.MENU_MASK));
		menu.add(item);

	}

	public int getPreferredAlignment() {
		return RIGHT;
	}
}
