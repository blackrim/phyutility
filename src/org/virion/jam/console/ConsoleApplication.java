package org.virion.jam.console;

import org.virion.jam.framework.Application;
import org.virion.jam.framework.MenuBarFactory;
import org.virion.jam.framework.DocumentFrame;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class ConsoleApplication extends Application {

	private ConsoleFrame consoleFrame = null;
    private boolean dontAskSave;

    public ConsoleApplication(String nameString, String aboutString, Icon icon, boolean dontAskSave) throws IOException {
        this(new ConsoleMenuBarFactory(), nameString, aboutString, icon, dontAskSave);
    }

    public ConsoleApplication(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon, boolean dontAskSave) throws IOException {

		super(menuBarFactory, nameString, aboutString, icon);

        this.dontAskSave = dontAskSave;

		consoleFrame = new ConsoleFrame();
		consoleFrame.initialize();
		consoleFrame.setVisible(true);

		// event handling
		consoleFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}
		});
	}

    public void initialize() {
        if (org.virion.jam.mac.Utils.isMacOSX()) {
            // If this is a Mac application then register it at this point.
            // This will result in any events such as open file being executed
            // due to files being double-clicked or dragged on to the application.
            org.virion.jam.mac.Utils.macOSXRegistration(this);
        }
    }

    protected JFrame getDefaultFrame() { return consoleFrame; }

	public DocumentFrame doNew() {
		throw new RuntimeException("A ConsoleApplication cannot do a New command");
	}

	public DocumentFrame doOpenFile(File file) {
		throw new RuntimeException("A ConsoleApplication cannot do an Open command");
	}

	public void doCloseWindow() {
		doQuit();
	}

	public void doQuit() {
		if (dontAskSave || consoleFrame.requestClose()) {

			consoleFrame.setVisible(false);
			consoleFrame.dispose();
			System.exit(0);
		}
	}

    public void doPreferences() {
    }

    public void doStop() {
		doQuit();
    }

	// Close the window when the close box is clicked
	private void thisWindowClosing(java.awt.event.WindowEvent e) {
		doQuit();
	}

}