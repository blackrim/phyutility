/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.virion.jam.demo.menus;

import org.virion.jam.framework.MenuFactory;
import org.virion.jam.framework.AbstractFrame;

import javax.swing.*;

/**
 * @author rambaut
 *         Date: Feb 24, 2005
 *         Time: 5:12:11 PM
 */
public class DemoMenuFactory implements MenuFactory {

    public static final String FIRST = "First";
    public static final String SECOND = "Second";

    public String getMenuName() {
        return "Demo";
    }

    public void populateMenu(JMenu menu, AbstractFrame frame) {
        JMenuItem item;

        if (frame instanceof DemoMenuHandler) {
            item = new JMenuItem(((DemoMenuHandler)frame).getFirstAction());
            menu.add(item);

            item = new JMenuItem(((DemoMenuHandler)frame).getSecondAction());
            menu.add(item);
        } else {
            item = new JMenuItem(FIRST);
            item.setEnabled(false);
            menu.add(item);

            item = new JMenuItem(SECOND);
            item.setEnabled(false);
            menu.add(item);
        }

    }

    public int getPreferredAlignment() {
        return LEFT;
    }
}
