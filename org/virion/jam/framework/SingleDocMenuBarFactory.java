/*
 * Copyright (c) 2005 Biomatters LTD. All Rights Reserved.
 */

package org.virion.jam.framework;

import org.virion.jam.mac.MacFileMenuFactory;
import org.virion.jam.mac.MacHelpMenuFactory;
import org.virion.jam.mac.MacWindowMenuFactory;


public class SingleDocMenuBarFactory extends DefaultMenuBarFactory {

	public SingleDocMenuBarFactory() {
		if (org.virion.jam.mac.Utils.isMacOSX()) {
			registerMenuFactory(new MacFileMenuFactory(false));
			registerMenuFactory(new DefaultEditMenuFactory());
			registerMenuFactory(new MacWindowMenuFactory());
			registerMenuFactory(new MacHelpMenuFactory());
		} else {
			registerMenuFactory(new DefaultFileMenuFactory(false));
			registerMenuFactory(new DefaultEditMenuFactory());
			registerMenuFactory(new DefaultHelpMenuFactory());
		}
	}

}