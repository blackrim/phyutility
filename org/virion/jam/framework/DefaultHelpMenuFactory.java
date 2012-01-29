/*
 * Copyright (c) 2005 Biomatters LTD. All Rights Reserved.
 */
package org.virion.jam.framework;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author rambaut
 *         Date: Dec 26, 2004
 *         Time: 11:02:20 AM
 */
public class DefaultHelpMenuFactory implements MenuFactory {
	public String getMenuName() {
		return "Help";
	}

	public void populateMenu(JMenu menu, AbstractFrame frame) {

        menu.setMnemonic('H');

		JMenuItem item;

		Application application = Application.getApplication();

		item = new JMenuItem(application.getAboutAction());
		menu.add(item);

		if (frame.getHelpAction() != null) {
			item = new JMenuItem(frame.getHelpAction());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, MenuBarFactory.MENU_MASK));
			menu.add(item);

			menu.addSeparator();
		}

		if (application.getHelpAction() != null) {
			item = new JMenuItem(application.getHelpAction());
			menu.add(item);

			menu.addSeparator();
		}

		if (application.getWebsiteAction() != null) {
			item = new JMenuItem("Website");
		}
	}

	public int getPreferredAlignment() {
		return RIGHT;
	}
}
