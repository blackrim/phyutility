package org.virion.jam.table;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public class TableRenderer extends DefaultTableCellRenderer {
    protected Color bg1 = new Color(0xED, 0xF3, 0xFE);
    protected Color bg2 = Color.white;
    protected boolean striped;

    public TableRenderer(int alignment, Insets insets) {

        this(true, alignment, insets);
    }

    public TableRenderer(boolean striped, int alignment, Insets insets) {
        super();
        this.striped = striped;
        setOpaque(true);
        setHorizontalAlignment(alignment);
        if (insets != null) {
            setBorder(new BorderUIResource.EmptyBorderUIResource(insets));
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {

        if (value != null) {
            setText(value.toString());
        }
        setEnabled(table.isEnabled());
        setFont(table.getFont());

        // if cell is selected, set background color to default cell selection background color
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            if (striped) {
                if (row % 2 == 0) {
                    setBackground(bg1);
                } else {
                    setBackground(bg2);
                }
            } else {
                setBackground(table.getBackground());
            }
            setForeground(table.getForeground());
        }

        return this;
    }
}
