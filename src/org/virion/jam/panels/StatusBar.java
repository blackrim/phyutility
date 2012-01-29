package org.virion.jam.panels;

import javax.swing.*;
import java.awt.*;

/**
 * @author rambaut
 *         Date: Oct 12, 2004
 *         Time: 12:18:09 AM
 */
public class StatusBar extends StatusPanel {
	public StatusBar(String initialText) {
		super(initialText);

		setBorder(BorderFactory.createCompoundBorder(
		    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray),
		    BorderFactory.createEmptyBorder(2, 12, 2, 12)));
  //      panel.setBackground(new Color(0.0F, 0.0F, 0.0F, 0.05F));

    }

	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.05F));
	    g.fillRect(0, 0, getWidth(), getHeight());
	}

}
