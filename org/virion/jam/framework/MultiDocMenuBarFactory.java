/*
 * Copyright (c) 2005 Biomatters LTD. All Rights Reserved.
 */

package org.virion.jam.framework;

import org.virion.jam.mac.MacFileMenuFactory;
import org.virion.jam.mac.MacHelpMenuFactory;
import org.virion.jam.mac.MacWindowMenuFactory;


public class MultiDocMenuBarFactory extends DefaultMenuBarFactory {


	public MultiDocMenuBarFactory() {
		if (org.virion.jam.mac.Utils.isMacOSX()) {
			registerMenuFactory(new MacFileMenuFactory(true));
			registerMenuFactory(new DefaultEditMenuFactory());
			registerMenuFactory(new MacHelpMenuFactory());
			registerMenuFactory(new MacWindowMenuFactory());
		} else {
			registerMenuFactory(new DefaultFileMenuFactory(true));
			registerMenuFactory(new DefaultEditMenuFactory());
			registerMenuFactory(new DefaultHelpMenuFactory());
		}
	}
}