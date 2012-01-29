/*
 * Copyright (c) 2005 Biomatters LTD. All Rights Reserved.
 */
package org.virion.jam.framework;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author rambaut
 *         Date: Dec 26, 2004
 *         Time: 11:01:06 AM
 */
public class DefaultFileMenuFactory implements MenuFactory {

	private final boolean isMultiDocument;

	public DefaultFileMenuFactory(boolean isMultiDocument) {
		this.isMultiDocument = isMultiDocument;
	}

	public String getMenuName() {
		return "File";
	}

	public void populateMenu(JMenu menu, AbstractFrame frame) {

		JMenuItem item;

		Application application = Application.getApplication();
        menu.setMnemonic('F');

		if (isMultiDocument) {
			item = new JMenuItem(application.getNewAction());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MenuBarFactory.MENU_MASK));
			menu.add(item);
		}

		item = new JMenuItem(application.getOpenAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(frame.getSaveAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(frame.getSaveAsAction());
		menu.add(item);

		if (frame.getImportAction() != null || frame.getExportAction() != null) {
			menu.addSeparator();

			if (frame.getImportAction() != null) {
				item = new JMenuItem(frame.getImportAction());
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MenuBarFactory.MENU_MASK));
				menu.add(item);
			}

			if (frame.getExportAction() != null) {
				item = new JMenuItem(frame.getExportAction());
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, MenuBarFactory.MENU_MASK));
				menu.add(item);
			}
		}

		menu.addSeparator();

		item = new JMenuItem(frame.getPrintAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(application.getPageSetupAction());
		menu.add(item);

		menu.addSeparator();

		if (application.getRecentFileMenu() != null) {
			JMenu subMenu = application.getRecentFileMenu();
			menu.add(subMenu);

			menu.addSeparator();
		}

		item = new JMenuItem(application.getExitAction());
		menu.add(item);
	}

	public int getPreferredAlignment() {
		return LEFT;
	}
}
