/**
 * AuxilaryFrame.java
 */

package org.virion.jam.framework;

import javax.swing.*;

public class AuxilaryFrame extends AbstractFrame {

    private DocumentFrame documentFrame;
    private JPanel contentsPanel;

    public AuxilaryFrame(DocumentFrame documentFrame) {
        super();

        this.documentFrame = documentFrame;
        this.contentsPanel = null;
    }

    public AuxilaryFrame(DocumentFrame documentFrame,
                         JPanel contentsPanel) {
        super();

        this.documentFrame = documentFrame;
        setContentsPanel(contentsPanel);
    }

    public void setContentsPanel(JPanel contentsPanel) {
        this.contentsPanel = contentsPanel;
        getContentPane().add(contentsPanel);
        pack();
    }

    public DocumentFrame getDocumentFrame() {
        return documentFrame;
    }

    protected void initializeComponents() {
    }

    public boolean requestClose() {
        return true;
    }

    public JComponent getExportableComponent() {
        return contentsPanel;
    }

    @SuppressWarnings({"deprecation"})
    public void doCloseWindow() {
        hide();
    }

    public Action getSaveAction() {
        return documentFrame.getSaveAction();
    }

    public Action getSaveAsAction() {
        return documentFrame.getSaveAsAction();
    }

}
