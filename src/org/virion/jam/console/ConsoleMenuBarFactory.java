/**
* ConsoleMenuBarFactory.java
*/

package org.virion.jam.console;

import org.virion.jam.framework.*;
import org.virion.jam.mac.MacFileMenuFactory;
import org.virion.jam.mac.MacHelpMenuFactory;
import org.virion.jam.mac.MacWindowMenuFactory;

public class ConsoleMenuBarFactory extends DefaultMenuBarFactory {

	public ConsoleMenuBarFactory() {
        // org.virion stuff shouldn't be called from here - it's a separate project!

		// no its not. This class is part of JAM.
        if (org.virion.jam.mac.Utils.isMacOSX()) {
        //if (System.getProperty("mrj.version") != null) {
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