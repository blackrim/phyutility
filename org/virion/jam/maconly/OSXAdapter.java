/*	OSXAdapter.java 

package org.virion.jam.maconly;

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

public class OSXAdapter extends ApplicationAdapter {

    // pseudo-singleton model; no point in making multiple instances
    // of the EAWT application or our adapter
    private static OSXAdapter theAdapter;
    private static com.apple.eawt.Application theApplication;

    // reference to the app where the existing quit, about, prefs code is
    private org.virion.jam.framework.Application application;

    private OSXAdapter(org.virion.jam.framework.Application application) {
        this.application = application;
    }

    // implemented handler methods.  These are basically hooks into existing
    // functionality from the main app, as if it came over from another platform.
    public void handleAbout(ApplicationEvent ae) {
        if (application != null) {
            ae.setHandled(true);
            application.doAbout();
        } else {
            throw new IllegalStateException("handleAbout: Application instance detached from listener");
        }
    }

    public void handlePreferences(ApplicationEvent ae) {
        if (application != null) {
            application.doPreferences();
            ae.setHandled(true);
        } else {
            throw new IllegalStateException("handlePreferences: Application instance detached from listener");
        }
    }

    public void handleQuit(ApplicationEvent ae) {
        if (application != null) {
            
            ae.setHandled(false);
            application.doQuit();
        } else {
            throw new IllegalStateException("handleQuit: Application instance detached from listener");
        }
    }


    // The main entry-point for this functionality.  This is the only method
    // that needs to be called at runtime, and it can easily be done using
    // reflection.
    public static void registerMacOSXApplication(org.virion.jam.framework.Application application) {
        if (theApplication == null) {
            theApplication = new com.apple.eawt.Application();
        }

        if (theAdapter == null) {
            theAdapter = new OSXAdapter(application);
        }
        theApplication.addApplicationListener(theAdapter);
    }

    // Another static entry point for EAWT functionality.  Enables the
    // "Preferences..." menu item in the application menu.
    public static void enablePrefs(boolean enabled) {
        if (theApplication == null) {
            theApplication = new com.apple.eawt.Application();
        }
        theApplication.setEnabledPreferencesMenu(enabled);
    }

	public void handleOpenFile(ApplicationEvent ae) {
        if (application != null) {
            application.doOpen(ae.getFilename());
            ae.setHandled(true);
        } else {
            throw new IllegalStateException("handleOpenFile: Application instance detached from listener");
        }
        throw new RuntimeException("handleOpenFile: " + ae.getFilename());
    }
}*/