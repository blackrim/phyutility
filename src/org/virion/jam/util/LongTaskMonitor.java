package org.virion.jam.util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LongTaskMonitor {

    public final static int ONE_SECOND = 1000;

    private ProgressMonitor progressMonitor;
    private Timer timer;
    private LongTask task;
    private JComponent parent;
    private TaskListener listener;

    public LongTaskMonitor(LongTask task, JComponent parent, TaskListener listener) {

        this.task = task;
        this.parent = parent;
        this.listener = listener;

        //Create a timer.
        timer = new Timer(ONE_SECOND, new TimerListener());

        progressMonitor = new ProgressMonitor(parent,
                task.getDescription(),
                "", 0, task.getLengthOfTask());
        progressMonitor.setProgress(0);
        progressMonitor.setMillisToPopup(ONE_SECOND);

        task.go();
        timer.start();
    }

    /**
     * The actionPerformed method in this class
     * is called each time the Timer "goes off".
     */
    class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (progressMonitor.isCanceled() || task.done()) {

                boolean canceled = progressMonitor.isCanceled();
                progressMonitor.close();
                task.stop();
                timer.stop();
                if (canceled) {
                    listener.taskCanceled();
                } else {
                    listener.taskFinished();
                    //Toolkit.getDefaultToolkit().beep();
                }
            } else {
                progressMonitor.setNote(task.getMessage());
                progressMonitor.setProgress(task.getCurrent());
            }
        }
    }
}
