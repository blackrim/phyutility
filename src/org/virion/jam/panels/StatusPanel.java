package org.virion.jam.panels;

import org.virion.jam.util.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Andrew Rambaut
 * Date: Jul 26, 2004
 * Time: 5:11:59 PM
 */
public class StatusPanel extends JPanel implements StatusListener {

    public static final int NORMAL = 0;
    public static final int WORKING = 1;
    public static final int WARNING = 2;
    public static final int ERROR = 3;

    private JLabel statusLabel = null;
    private String statusText = " ";
    private JButton statusButton;
    private int status = - 1;

    private StatusProvider statusProvider = null;
    private Timer timer = null;
    private long lastUpdate;
    private boolean pendingUpdate = false;
    private String pendingText ="";

    public StatusPanel() {
        this(null);
    }

    public StatusPanel(String initialText) {

        setLayout(new BorderLayout(4,4));

        if (initialText != null) {
            statusText = initialText;

            statusLabel = new JLabel(statusText);
            add(statusLabel, BorderLayout.CENTER);

            // Added this to reduce size of status bar (this looks nice on a Mac - not tested on Windows).
            statusLabel.setFont(UIManager.getFont("SmallSystemFont"));

        }

        statusButton = new JButton(normalStatusIcon);
        statusButton.setPreferredSize(new Dimension(16,16));

        statusButton.putClientProperty("JButton.buttonType", "toolbar");
        statusButton.setBorderPainted(false);
        statusButton.setOpaque(false);
        statusButton.setRolloverEnabled(true);
        // this is required on Windows XP platform -- untested on Macintosh
        // ... seems to be fine on a Macintosh (AR).
        statusButton.setContentAreaFilled(false);

        statusButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusButtonPressed();
            }
        });

        add(statusButton, BorderLayout.WEST);
    }

    public void setStatusProvider(StatusProvider statusProvider) {
        stopAnimation();

        if (this.statusProvider != null) {
            this.statusProvider.removeStatusListener(this);
        }
        this.statusProvider = statusProvider;
        if (this.statusProvider != null) {
            this.statusProvider.addStatusListener(this);
        }
    }

    public void statusChanged(int status, String statusText) {
        //updating the status text too often is a very bad idea.
        //on my machine doing 30 updates per second fully utilises the CPU.
        //so we limit it here to five updates per second.
        boolean updateText = (System.currentTimeMillis() - lastUpdate)> 200;
        if(this.status != status) updateText = true;
        if(statusText.length()> 0 && (this.statusText.length() == 0 || this.statusText.equals(" ")))
            updateText = true;
        setStatus(status);
        if(updateText) {
            setStatusText(statusText);
            lastUpdate = System.currentTimeMillis();
        }
        else {
            pendingUpdate = true;
            pendingText = statusText;
        }
    }

    private void setStatusText(final String statusText) {

        if (statusText == null || statusText.length() == 0) {
            this.statusText = " ";
        } else {
            this.statusText = statusText;
        }
        if (statusLabel != null) {
            //it is only safe to call "statusLabel.setText(statusText);" in the AWT thread:
            invokeNow(new Runnable() {
                public void run() {
                    statusLabel.setText(statusText);
                }
            });

        }
    }

    private void invokeNow(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            /*try {*/
                EventQueue.invokeLater(runnable);
            /*} catch (InterruptedException e) {
            } catch (InvocationTargetException e) {
            }*/
        }
    }


    private void setStatus(final int status) {
        if(status == this.status) return;
        this.status = status;
        stopAnimation();
        Runnable runnable =new Runnable() {
            public void run() {
                switch (status) {
                    case NORMAL:
                        statusButton.setIcon(normalStatusIcon);
                        statusButton.setPressedIcon(normalStatusPressedIcon);
                        statusButton.setRolloverIcon(normalStatusRolloverIcon);
                        break;
                    case WORKING:
                        startAnimation();
                        break;
                    case WARNING:
                        statusButton.setIcon(warningStatusIcon);
                        statusButton.setPressedIcon(warningStatusPressedIcon);
                        statusButton.setRolloverIcon(warningStatusRolloverIcon);
                        break;
                    case ERROR:
                        statusButton.setIcon(errorStatusIcon);
                        statusButton.setPressedIcon(errorStatusPressedIcon);
                        statusButton.setRolloverIcon(errorStatusRolloverIcon);
                        break;
                }
            }
        };
        invokeNow(runnable);
    }

    private void statusButtonPressed() {
        if (statusProvider != null) {
            statusProvider.fireStatusButtonPressed();
        }
    }

    private void startAnimation() {
        if (workingStatusIcons == null) return;
        listener.actionPerformed(null);

        timer = new javax.swing.Timer(100, listener);
        timer.setCoalesce(true);
        timer.start();
    }
    private void pendingCheck () {

        if (pendingUpdate) {
            setStatusText(pendingText);
        }
        pendingUpdate = false;
    }

    private void stopAnimation() {
        pendingCheck ();
        current = 0;
        if (timer == null) return;
        timer.stop();
    }

    private int current = 0;
    ActionListener listener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            statusButton.setIcon(workingStatusIcons[current]);
            current++;
            if (current >= workingStatusIcons.length) current = 0;
            pendingCheck ();
        }
    };

    private static Icon normalStatusIcon = null;
    private static Icon normalStatusPressedIcon = null;
    private static Icon normalStatusRolloverIcon = null;

    private static Icon[] workingStatusIcons = null;

    private static Icon warningStatusIcon = null;
    private static Icon warningStatusPressedIcon = null;
    private static Icon warningStatusRolloverIcon = null;

    private static Icon errorStatusIcon = null;
    private static Icon errorStatusPressedIcon = null;
    private static Icon errorStatusRolloverIcon = null;

    static {
         try {
             workingStatusIcons = new Icon[12];

             workingStatusIcons[0] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity1.png");
             workingStatusIcons[1] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity2.png");
             workingStatusIcons[2] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity3.png");
             workingStatusIcons[3] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity4.png");
             workingStatusIcons[4] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity5.png");
             workingStatusIcons[5] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity6.png");
             workingStatusIcons[6] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity7.png");
             workingStatusIcons[7] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity8.png");
             workingStatusIcons[8] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity9.png");
             workingStatusIcons[9] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity10.png");
             workingStatusIcons[10] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity11.png");
             workingStatusIcons[11] = IconUtils.getIcon(StatusPanel.class, "images/activity/activity12.png");
        } catch (Exception e) {
             workingStatusIcons = null;
        }
    }
}
