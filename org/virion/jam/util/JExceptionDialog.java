package org.virion.jam.util;

/**
 * @author adru001
 */
public class JExceptionDialog extends javax.swing.JDialog {

    /**
     * Creates new form JExceptionDialog
     */
    public JExceptionDialog(java.awt.Frame parent, boolean modal, String title, String text) {
        super(parent, modal);
        initComponents();
        pack();

        setTitle(title);
        setText(text);
        setVisible(true);
    }

    public void setText(String text) {
        exceptionTextArea.setText(text);
        repaint();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        exceptionTextArea = new javax.swing.JTextArea();
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(2, 5, 5));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);


        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);


        jScrollPane1.setPreferredSize(new java.awt.Dimension(500, 200));

        exceptionTextArea.setEnabled(false);
        jScrollPane1.setViewportView(exceptionTextArea);


        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog

    private void doClose() {
        setVisible(false);
        dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new JExceptionDialog(new javax.swing.JFrame(), true, "Exception", "test").setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea exceptionTextArea;
    // End of variables declaration//GEN-END:variables

}
