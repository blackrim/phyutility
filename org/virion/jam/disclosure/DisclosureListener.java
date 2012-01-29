package org.virion.jam.disclosure;

import java.awt.*;

/**
 * @author rambaut
 *         Date: May 25, 2005
 *         Time: 11:17:04 PM
 */
public interface DisclosureListener {
	void opening(Component component);
	void opened(Component component);

	void closing(Component component);
	void closed(Component component);
}
