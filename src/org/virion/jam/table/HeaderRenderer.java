package org.virion.jam.table;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class HeaderRenderer extends DefaultTableCellRenderer {

    public HeaderRenderer(int alignment, Insets insets) {
        setHorizontalAlignment(alignment);
        setOpaque(true);

        // This call is needed because DefaultTableCellRenderer calls setBorder()
        // in its constructor, which is executed after updateUI()
        javax.swing.border.Border border = UIManager.getBorder("TableHeader.cellBorder");
        setBorder(new CompoundBorder(border, new EmptyBorder(insets)));
    }

    public void updateUI() {
        super.updateUI();
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean selected, boolean focused, int row, int column) {
        JTableHeader header;

        if (table != null && (header = table.getTableHeader()) != null) {
            setEnabled(header.isEnabled());
            setComponentOrientation(header.getComponentOrientation());

            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        } else {
            /* Use sensible values instead of random leftover values from the last call */
            setEnabled(true);
            setComponentOrientation(ComponentOrientation.UNKNOWN);

            setForeground(UIManager.getColor("TableHeader.foreground"));
            setBackground(UIManager.getColor("TableHeader.background"));
            setFont(UIManager.getFont("TableHeader.font"));
        }

        setValue(value);

        return this;
    }
} 