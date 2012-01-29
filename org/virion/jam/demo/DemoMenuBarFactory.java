package org.virion.jam.demo;

import org.virion.jam.framework.*;
import org.virion.jam.mac.*;
import org.virion.jam.demo.menus.DemoMenuFactory;


public class DemoMenuBarFactory extends DefaultMenuBarFactory {

	public DemoMenuBarFactory() {
		if (org.virion.jam.mac.Utils.isMacOSX()) {
			registerMenuFactory(new MacFileMenuFactory(true));
			registerMenuFactory(new DefaultEditMenuFactory());
			registerMenuFactory(new DemoMenuFactory());
			registerMenuFactory(new MacWindowMenuFactory());
			registerMenuFactory(new MacHelpMenuFactory());
		} else {
			registerMenuFactory(new DefaultFileMenuFactory(true));
			registerMenuFactory(new DefaultEditMenuFactory());
			registerMenuFactory(new DemoMenuFactory());
			registerMenuFactory(new DefaultHelpMenuFactory());
		}
	}

}