/*	Utils.java */

package org.virion.jam.mac;

import java.lang.reflect.Method;

public class Utils {

    protected static boolean MAC_OS_X;
    protected static String MAC_OS_X_VERSION;

    public static boolean isMacOSX() {
        return MAC_OS_X;
    }

    public static void macOSXRegistration(org.virion.jam.framework.Application application) {
        if (MAC_OS_X) {

            Class osxAdapter = null;

            try {
                osxAdapter = Class.forName("org.virion.jam.maconly.OSXAdapter");
            } catch (Exception e) {
                System.err.println("This version of Mac OS X does not support the Apple EAWT.");
            }

            try {
                if (osxAdapter != null) {

                    Class[] defArgs = {org.virion.jam.framework.Application.class};
                    Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);

                    if (registerMethod != null) {
                        Object[] args = {application};
                        registerMethod.invoke(osxAdapter, args);
                    }

                    // This is slightly gross.  to reflectively access methods with boolean args,
                    // use "boolean.class", then pass a Boolean object in as the arg, which apparently
                    // gets converted for you by the reflection system.
                    defArgs[0] = boolean.class;
                    Method prefsEnableMethod = osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
                    if (prefsEnableMethod != null) {
                        Object args[] = {Boolean.TRUE};
                        prefsEnableMethod.invoke(osxAdapter, args);
                    }
                }

            } catch (Exception e) {
                System.err.println("Exception while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
    }

    static {
        MAC_OS_X_VERSION = System.getProperty("mrj.version");
        MAC_OS_X = MAC_OS_X_VERSION != null;
    }
}