package org.virion.jam.table;

import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.event.MouseInputListener;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * @author rambaut
 *         Date: Oct 20, 2004
 *         Time: 10:16:52 PM
 */
public class AdvancedTableUI extends BasicTableUI {

    public void installUI(JComponent c) {
        super.installUI(c);

        if (org.virion.jam.mac.Utils.isMacOSX()) {
            c.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
        }
    }

	protected MouseInputListener createMouseInputListener() {
		return new AdvancedTableUI.AdvancedMouseInputHandler();
	}

	class AdvancedMouseInputHandler extends MouseInputHandler {
		public void mousePressed(MouseEvent e) {
			Point origin = e.getPoint();
			int row = table.rowAtPoint(origin);
			int column = table.columnAtPoint(origin);
			if (row != -1 && column != -1) {
				if (table.isCellSelected(row, column)) {
					e.consume();
				}
			}

			super.mousePressed(e);
		}
	}
}
