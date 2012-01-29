package org.virion.jam.mac;

import org.virion.jam.framework.MenuFactory;
import org.virion.jam.framework.AbstractFrame;
import org.virion.jam.framework.Application;
import org.virion.jam.framework.MenuBarFactory;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;

/**
 * @author rambaut
 *         Date: Dec 26, 2004
 *         Time: 11:02:45 AM
 */
public class MacFileMenuFactory implements MenuFactory {

	private final boolean isMultiDocument;

	public MacFileMenuFactory(boolean isMultiDocument) {
		this.isMultiDocument = isMultiDocument;
	}

	public String getMenuName() {
		return "File";
	}

	public void populateMenu(JMenu menu, AbstractFrame frame) {

		Application application = Application.getApplication();
		JMenuItem item;

		if (isMultiDocument) {
			item = new JMenuItem(application.getNewAction());
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MenuBarFactory.MENU_MASK));
			menu.add(item);
		}

		item = new JMenuItem(application.getOpenAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MenuBarFactory.MENU_MASK));
		menu.add(item);

		if (application.getRecentFileMenu() != null) {
			JMenu subMenu = application.getRecentFileMenu();
			menu.add(subMenu);
		}

		menu.addSeparator();

		item = new JMenuItem(frame.getCloseWindowAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(frame.getSaveAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MenuBarFactory.MENU_MASK));
		menu.add(item);

		item = new JMenuItem(frame.getSaveAsAction());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MenuBarFactory.MENU_MASK + ActionEvent.SHIFT_MASK));
		menu.add(item);

		item = new JMenuItem("Revert to Saved");
		item.setEnabled(false);
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
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, MenuBarFactory.MENU_MASK + ActionEvent.SHIFT_MASK));
		menu.add(item);

	}

	public int getPreferredAlignment() {
		return LEFT;
	}
}
