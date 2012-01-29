package org.virion.jam.util;

import javax.swing.*;
import java.awt.*;

public class Utils {

    /**
     * @return the value in a text field as a double.
     *         If the text field contents do not represent a valid double then the
     *         default value is inserted into the text field and returned.
     */
    public static double getDoubleFromTextField(JTextField textField, double defaultValue) {

        double value = defaultValue;
        try {
            value = Double.parseDouble(textField.getText());
        } catch (NumberFormatException nfe) {
            textField.setText(defaultValue + "");
        }
        return value;
    }

    /**
     * Center a component in reference to another,
     * if the reference is null or not visible the screen is used as reference
     */
    public static void centerComponent(Component component, Component reference) {
        Dimension componentSize = component.getSize();
        Dimension referenceSize;
        Point referencePosition;
        if (reference != null && reference.isShowing()) {
            referenceSize = reference.getSize();
            referencePosition = reference.getLocationOnScreen();
        } else {
            referenceSize = Toolkit.getDefaultToolkit().getScreenSize();
            referencePosition = new Point(0, 0);
        }
        component.setBounds(referencePosition.x
                + Math.abs(referenceSize.width - componentSize.width) / 2,
                referencePosition.y
                + Math.abs(referenceSize.height - componentSize.height) / 2,
                componentSize.width,
                componentSize.height);
    }

    public static void showWaitCursor(Component component) {
        showPredefinedCursor(component, Cursor.WAIT_CURSOR);
    }

    public static void showPredefinedCursor(Component component, int cursor) {
        component.setCursor(Cursor.getPredefinedCursor(cursor));
    }

    public static void showDefaultCursor(Component component) {
        showPredefinedCursor(component, Cursor.DEFAULT_CURSOR);
    }

    public static String getEnv(final String name) {
        java.util.Properties jvmEnv = System.getProperties();
        java.util.Properties envVars = new java.util.Properties();

        try
        {
            if ( jvmEnv.getProperty( "os.name" ).toLowerCase().indexOf( "win" ) != -1 ) {
                envVars.load( Runtime.getRuntime().exec( "set" ).getInputStream());
            }
            else {
                try  {
                // ( jvmEnv.getProperty( "os.name" ).equalsIgnoreCase( "Linux" ) )
                envVars.load( Runtime.getRuntime().exec( "/usr/bin/env").getInputStream() );
                } catch( Throwable t ) {
                   envVars.load( Runtime.getRuntime().exec( "/bin/env").getInputStream() );
                }
            }
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        return envVars.getProperty(name);
    }
}