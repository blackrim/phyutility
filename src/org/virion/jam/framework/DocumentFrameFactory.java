/**
 * DocumentFrameFactory.java
 */

package org.virion.jam.framework;



public interface DocumentFrameFactory {

    DocumentFrame createDocumentFrame(Application app, MenuBarFactory menuBarFactory);
}