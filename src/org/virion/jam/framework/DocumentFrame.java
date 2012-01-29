/**
 * DocumentFrame.java
 */

package org.virion.jam.framework;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class DocumentFrame extends AbstractFrame {

    private File documentFile = null;

    public DocumentFrame() {
        super();
    }

    protected abstract void initializeComponents();

    protected abstract boolean readFromFile(File file) throws IOException;

    protected abstract boolean writeToFile(File file) throws IOException;

    public final boolean hasFile() {
        return documentFile != null;
    }

    public final File getFile() {
        return documentFile;
    }

	public final void clearFile() {
		documentFile = null;
	}

    public boolean requestClose() {
        if (isDirty()) {
            int option = JOptionPane.showConfirmDialog(this, "Do you wish to save?",
                    "Unsaved changes",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                return !doSave();
            } else if (option == JOptionPane.CANCEL_OPTION || option == -1) {
                return false;
            }
            return true;
        }
        return true;
    }

    public boolean openFile(File file) {

        try {
	        documentFile = file;
            if (readFromFile(file)) {
                clearDirty();
	            setFrameTitle();

                return true;
            }
        } catch (FileNotFoundException fnfe) {
            JOptionPane.showMessageDialog(this, "Unable to open file: File not found",
                    "Unable to open file",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Unable to read file: " + ioe,
                    "Unable to read file",
                    JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    public boolean doSave() {
        if (!hasFile()) {
            return doSaveAs();
        } else {
            try {
                if (writeToFile(documentFile)) {
                    clearDirty();

	                return true;
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, "Unable to save file: " + ioe,
                        "Unable to save file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        return false;
    }

    public boolean doSaveAs() {
        FileDialog dialog = new FileDialog(this,
                "Save Document As...",
                FileDialog.SAVE);

        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            // the dialog was cancelled...
            return false;
        }

        File file = new File(dialog.getDirectory(), dialog.getFile());

        try {
            if (writeToFile(file)) {

                clearDirty();
                documentFile = file;
	            setFrameTitle();

	            return true;
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "Unable to save file: " + ioe,
                    "Unable to save file",
                    JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

	protected final void setFrameTitle() {
		String title = getTitle();
		if (documentFile != null) {
			setTitle(title + " - " + documentFile.getName());
		}

	}

    public Action getSaveAction() {
        return saveAction;
    }

    public Action getSaveAsAction() {
        return saveAsAction;
    }

    private AbstractAction saveAction = new AbstractAction("Save") {
        public void actionPerformed(ActionEvent ae) {
            doSave();
        }
    };

    private AbstractAction saveAsAction = new AbstractAction("Save As...") {
        public void actionPerformed(ActionEvent ae) {
            doSaveAs();
        }
    };

}
