/**
 * Application.java
 */

package org.virion.jam.framework;

import org.virion.jam.html.HTMLViewer;
import org.virion.jam.preferences.PreferencesDialog;
import org.virion.jam.preferences.PreferencesSection;
import org.virion.jam.util.BrowserLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.*;

/*
 * @todo Implement a list of open windows
 * @todo Implement the recent files menu (persistance)
 */

public abstract class Application {

    private static MenuBarFactory menuBarFactory;
    private static Icon icon;
    private static String nameString;
    private static String aboutString;
    private static String websiteURLString;
    private static String helpURLString;

    private static Application application = null;

    private JMenu recentFileMenu = null;

    private PreferencesDialog preferencesDialog = null;

    public static Application getApplication() {
        return application;
    }

    public static MenuBarFactory getMenuBarFactory() {
        return menuBarFactory;
    }

    public static Icon getIcon() {
        return icon;
    }

    public static String getNameString() {
        return nameString;
    }

    public static String getAboutString() {
        return aboutString;
    }

    public static String getWebsiteURLString() {
        return websiteURLString;
    }

    public static String getHelpURLString() {
        return helpURLString;
    }

    public Application(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon) {
    	this(menuBarFactory, nameString, aboutString, icon, null, null);
    }

    public Application(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon,
    					String websiteURLString, String helpURLString) {

        Application.menuBarFactory = menuBarFactory;
        Application.nameString = nameString;
        Application.aboutString = aboutString;
        Application.websiteURLString = websiteURLString;
        Application.helpURLString = helpURLString;
        Application.icon = icon;

        aboutAction = new AbstractAction("About " + nameString + "...") {
            public void actionPerformed(ActionEvent ae) {
                doAbout();
            }
        };

        if (application != null) {
            throw new RuntimeException("Only on instance of Application is allowed");
        }
        application = this;

        preferencesDialog = new PreferencesDialog(getDefaultFrame());
    }

	public abstract void initialize();

	public void addMenuFactory(MenuFactory menuFactory) {
		getMenuBarFactory().registerMenuFactory(menuFactory);
	}

	private final static int MAX_RECENT_FILES = 20;

    public JMenu getRecentFileMenu() {
        if (recentFileMenu == null) {
            recentFileMenu = new JMenu("Recent Files");

	        // LOAD from preferences here?
        }
	    recentFileMenu.setEnabled(getOpenAction().isEnabled());
        return recentFileMenu;
    }

    public void addRecentFile(File file) {

        if (recentFileMenu != null) {
            if (recentFileMenu.getItemCount() == MAX_RECENT_FILES) {
                recentFileMenu.remove(MAX_RECENT_FILES - 1);
            }
	        recentFileMenu.insert(new RecentFileAction(file), 0);

	        // WRITE to preferences here
        }
    }

	private class RecentFileAction extends AbstractAction {
		public RecentFileAction(File recentFile) {
			super(recentFile.getName());
			this.recentFile = recentFile;
		}

		public void actionPerformed(ActionEvent actionEvent) {
			doOpenFile(recentFile);
		}

		private final File recentFile;
	}

    protected abstract JFrame getDefaultFrame();

    public void doAbout() {
        AboutBox aboutBox = new AboutBox(getNameString(), getAboutString(), getIcon());
        //aboutBox.initialize();        //causes about frame to have the menu system from the main frame.
        aboutBox.setVisible(true);
    }

    public void doHelp() {
    	if (helpURLString != null) {
    		displayURL(helpURLString);
    	} else {
	        try {
	            InputStream in = getClass().getResourceAsStream("/help/application.help");
	            if (in == null) return;
	            Reader reader = new InputStreamReader(in);
	            StringWriter writer = new StringWriter();
	            int c;
	            while ((c = reader.read()) != -1) writer.write(c);
	            reader.close();
	            writer.close();
	            JFrame frame = new HTMLViewer(getNameString() + " Help", writer.toString());
	            frame.setVisible(true);
	        } catch (IOException ignore) {
	        }
		}
    }

    public void doWebsite() {
    	if (websiteURLString != null) {
    		displayURL(websiteURLString);
    	}
    }

    public void displayURL(String urlString) {
        try {
            BrowserLauncher.openURL(urlString);
        } catch (IOException ioe) {
            // do nothing
        }
    }

    public void doPageSetup() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.pageDialog(new PageFormat());
    }

    public DocumentFrame doOpen() {
        Frame frame = getDefaultFrame();
        if (frame == null) {
            frame = new JFrame();
        }

        FileDialog dialog = new FileDialog(frame,
                "Open Document",
                FileDialog.LOAD);
        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());
            DocumentFrame doc = doOpenFile(file);
            addRecentFile(file);
            return doc;
        }
        return null;
    }

	public DocumentFrame doOpen(String fileName) {
        if (fileName != null && fileName.length() > 0) {
            File file = new File(fileName);
            DocumentFrame doc = doOpenFile(file);
            addRecentFile(file);
            return doc;
        }
        return null;
	}

    public abstract DocumentFrame doNew();

    public abstract DocumentFrame doOpenFile(File file);

    public abstract void doQuit();

    public void doPreferences() {
        preferencesDialog.showDialog();
    }

    public void addPreferencesSection(PreferencesSection preferencesSection) {
        preferencesDialog.addSection(preferencesSection);
    }

    public Action getNewAction() {
        return newAction;
    }

    public Action getOpenAction() {
        return openAction;
    }

    public Action getPageSetupAction() {
        return pageSetupAction;
    }

    public Action getExitAction() {
        return exitAction;
    }

    public Action getAboutAction() {
        return aboutAction;
    }

    public Action getPreferencesAction() {
        return preferencesAction;
    }

    public Action getHelpAction() {
        return helpAction;
    }

    public Action getWebsiteAction() {
        return websiteAction;
    }

    protected AbstractAction newAction = new AbstractAction("New") {
        public void actionPerformed(ActionEvent ae) {
            doNew();
        }
    };

    protected AbstractAction openAction = new AbstractAction("Open...") {
        public void actionPerformed(ActionEvent ae) {
            doOpen();
        }
    };

    protected AbstractAction pageSetupAction = new AbstractAction("Page Setup...") {
        public void actionPerformed(ActionEvent ae) {
            doPageSetup();
        }
    };

    protected AbstractAction exitAction = new AbstractAction("Exit") {
        public void actionPerformed(ActionEvent ae) {
            doQuit();
        }
    };

    protected AbstractAction aboutAction = null;

    protected AbstractAction preferencesAction = new AbstractAction("Preferences...") {
        public void actionPerformed(ActionEvent ae) {
            doPreferences();
        }
    };

    protected AbstractAction helpAction = new AbstractAction("Help...") {
        public void actionPerformed(ActionEvent ae) {
            doHelp();
        }
    };

    protected AbstractAction websiteAction = new AbstractAction("Website...") {
        public void actionPerformed(ActionEvent ae) {
            doWebsite();
        }
    };

}
