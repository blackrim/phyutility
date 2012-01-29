package org.virion.jam.framework;

import javax.swing.*;
import java.util.*;

/**
 * @author rambaut
 *         Date: Dec 26, 2004
 *         Time: 10:55:55 AM
 */
public class DefaultMenuBarFactory implements MenuBarFactory {

    private final List<MenuFactory> menuFactories = new ArrayList<MenuFactory>();
    private final List<MenuFactory> permanentMenuFactories = new ArrayList<MenuFactory>();
    private boolean populatedMenu = false;
    private JMenuBar menuBar = null;
    AbstractFrame frame = null;

    public final void populateMenuBar(JMenuBar menuBar, AbstractFrame frame) {
        this.menuBar = menuBar;
        this.frame = frame;
        Map<String, JMenu> menus = new HashMap<String, JMenu>();
        Map<Integer, String> order = new TreeMap<Integer, String>();

        int leftOrder = 0;
        int centerOrder = 1000;
        int rightOrder = 10000;

	    List<MenuFactory> l = new ArrayList<MenuFactory>();
	    l.addAll(menuFactories);
	    l.addAll(permanentMenuFactories);

        for (MenuFactory menuFactory : l) {
            String name = menuFactory.getMenuName();
            JMenu menu = (JMenu)menus.get(name);
            if (menu == null) {
                int alignment = menuFactory.getPreferredAlignment();
                menu = new JMenu(name);
                menus.put(name, menu);
                switch (alignment) {
                    case MenuFactory.LEFT:
                        order.put(new Integer(leftOrder), name);
                        leftOrder++;
                    break;
                    case MenuFactory.CENTER:
                        order.put(new Integer(centerOrder), name);
                        centerOrder++;
                    break;
                    case MenuFactory.RIGHT:
                        order.put(new Integer(rightOrder), name);
                        rightOrder++;
                    break;
                }
            }

            menuFactory.populateMenu(menu, frame);
        }

	    for (int i : order.keySet()) {
            String name = (String)order.get(i);
            JMenu menu = (JMenu)menus.get(name);
            menuBar.add(menu);
        }
        populatedMenu = true;
    }

    public final void deregisterMenuFactories() {
        menuFactories.removeAll(menuFactories);
    }

    public final void registerPermanentMenuFactory(MenuFactory menuFactory) {
        permanentMenuFactories.add(menuFactory);
        if(populatedMenu) {
            menuBar.removeAll();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    populateMenuBar(menuBar, frame);
                    frame.setJMenuBar(menuBar);
                }
            });
        }
    }

    public final void registerMenuFactory(MenuFactory menuFactory) {
        menuFactories.add(menuFactory);
        if(populatedMenu) {
            menuBar.removeAll();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    populateMenuBar(menuBar, frame);
                    frame.setJMenuBar(menuBar);
                }
            });
        }
    }

}
