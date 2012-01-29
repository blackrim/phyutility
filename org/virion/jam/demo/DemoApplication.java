package org.virion.jam.demo;

import org.virion.jam.framework.*;
import org.virion.jam.util.IconUtils;
import org.virion.jam.preferences.PreferencesSection;

import javax.swing.*;
import java.util.prefs.Preferences;
import java.awt.*;

public class DemoApplication extends MultiDocApplication {

    public DemoApplication(MenuBarFactory menuBarFactory, String nameString, String aboutString, Icon icon) {
        super(menuBarFactory, nameString, aboutString, icon);

        addPreferencesSection(new PreferencesSection() {
            Icon projectToolIcon = IconUtils.getIcon(this.getClass(), "images/prefsGeneral.png");

            public String getTitle() {
                return "General";
            }

            public Icon getIcon() {
                return projectToolIcon;
            }

            public JPanel getPanel() {
                JPanel panel = new JPanel();
                panel.add(generalCheck);
                return panel;
            }

            public void retrievePreferences() {
                Preferences prefs = Preferences.userNodeForPackage(DemoApplication.class);
                generalCheck.setSelected(prefs.getBoolean("general_check", true));
            }

            public void storePreferences() {
                Preferences prefs = Preferences.userNodeForPackage(DemoApplication.class);
                prefs.putBoolean("general_check", generalCheck.isSelected());
            }

            JCheckBox generalCheck = new JCheckBox("General preference");
        });

        addPreferencesSection(new PreferencesSection() {
            Icon projectToolIcon = IconUtils.getIcon(this.getClass(), "images/prefsAdvanced.png");


            public String getTitle() {
                return "Advanced";
            }

            public Icon getIcon() {
                return projectToolIcon;
            }

            public JPanel getPanel() {
                JPanel panel = new JPanel();
                panel.add(new JCheckBox("Advanced preference"));
                return panel;
            }

            public void retrievePreferences() {
                Preferences prefs = Preferences.userNodeForPackage(DemoApplication.class);
                advancedCheck.setSelected(prefs.getBoolean("advanced_check", true));
            }

            public void storePreferences() {
                Preferences prefs = Preferences.userNodeForPackage(DemoApplication.class);
                prefs.putBoolean("advanced_check", advancedCheck.isSelected());
            }

            JCheckBox advancedCheck = new JCheckBox("Advanced preference");
        });
    }

    // Main entry point
    static public void main(String[] args) {
        System.setProperty("com.apple.macos.useScreenMenuBar","true");
        System.setProperty("apple.laf.useScreenMenuBar","true");
        System.setProperty("apple.awt.showGrowBox","true");
        System.setProperty("apple.awt.antialiasing","on");
        System.setProperty("apple.awt.textantialiasing","on");
        System.setProperty("apple.awt.rendering","VALUE_RENDER_SPEED");

        // set the Quaqua Look and Feel in the UIManager
        try {
            //System.setProperty("Quaqua.Debug.showClipBounds","true");
            //System.setProperty("Quaqua.Debug.showVisualBounds","true");
            UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
            // set UI manager properties here that affect Quaqua
            UIManager.put("SystemFont", new Font("Lucida Grande", Font.PLAIN, 13));
            UIManager.put("SmallSystemFont", new Font("Lucida Grande", Font.PLAIN, 11));
        } catch (Exception e) {
            try {

                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        java.net.URL url = DemoApplication.class.getResource("/images/demo.png");
        Icon icon = null;

        if (url != null) {
            icon = new ImageIcon(url);
        }

        String nameString = "JAM Demo";
        String aboutString = "JAM Demo\nVersion 1.0\n \nCopyright 2006 Andrew Rambaut\nUniversity of Edinburgh\nAll Rights Reserved.";

        DemoApplication app = new DemoApplication(new DemoMenuBarFactory(), nameString, aboutString, icon);

        app.setDocumentFrameFactory(new DocumentFrameFactory() {
            public DocumentFrame createDocumentFrame(Application app, MenuBarFactory menuBarFactory) {
                return new DemoFrame("JAM Demo");
            }
        });

        app.initialize();

        if (args.length > 0) {
            for (String arg : args) {
                app.doOpen(arg);
            }
        }

        if (app.getUpperDocumentFrame() == null) {
            // If we haven't opened any files by now, prompt for one...
            app.doOpen();
        }
    }


}