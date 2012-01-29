package org.virion.jam.toolbar;

import javax.swing.*;
import java.awt.*;

/**
 * @author rambaut
 *         Date: Oct 18, 2005
 *         Time: 10:09:21 PM
 */
public class GenericToolbarItem extends JPanel implements ToolbarItem {

    public GenericToolbarItem(String title, String toolTipText, JComponent component) {
        setLayout(new BorderLayout());
        add(component, BorderLayout.NORTH);

        label = new JLabel(title);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.SOUTH);
        setToolTipText(toolTipText);
    }

    public void setToolbarOptions(ToolbarOptions options) {
        switch (options.getDisplay()) {
            case ToolbarOptions.ICON_AND_TEXT:
            case ToolbarOptions.TEXT_ONLY:
                label.setVisible(true);
                break;
            case ToolbarOptions.ICON_ONLY:
                label.setVisible(false);
                break;
        }
    }

    public void setAction(Action action) {
        throw new UnsupportedOperationException("Method setAction() not supported in GenericToolBarItem");
    }

    private JLabel label;
}
