package org.virion.jam.controlpanels;

/**
 * @author Andrew Rambaut
 * @version $Id: ControlsSettings.java 298 2006-04-16 09:00:35Z rambaut $
 */
public interface ControlsSettings {

	void putSetting(String key, Object value);

	Object getSetting(String key);
}
