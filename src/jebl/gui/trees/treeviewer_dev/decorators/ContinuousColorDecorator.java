package jebl.gui.trees.treeviewer_dev.decorators;

import jebl.util.Attributable;

import java.util.*;
import java.awt.*;

/**
 * This decorator takes an attribute name and a set of attibutable Objects. It
 * autodetects the type of attributes and then provides a colouring scheme for them
 * based on a gradient between color1 & color2.
 *
 * @author Andrew Rambaut
 * @version $Id: ContinuousColorDecorator.java 485 2006-10-25 15:24:54Z rambaut $
 */
public class ContinuousColorDecorator implements Decorator {

	enum SchemeType {
		DISCRETE,
		CONTINUOUS
	}

	public ContinuousColorDecorator(String attributeName,
	                                Set<? extends Attributable> items,
	                                Color color1, Color color2) throws NumberFormatException {
		this.attributeName = attributeName;
		this.color1 = new float[4];
		color1.getRGBComponents(this.color1);
		this.color2 = new float[4];
		color2.getRGBComponents(this.color2);

		// First collect the set of all attribute values
		Set<Object> values = new TreeSet<Object>();
		for (Attributable item : items) {
			Object value = item.getAttribute(attributeName);
			if (value != null) {
				values.add(value);
			}
		}

		boolean isNumber = true;

		// Find the range of numbers
		for (Object value : values) {
			double realValue = -1.0;

			if (value instanceof Boolean) {
				realValue = ((Boolean)value ? 1 : 0);
			} else if (value instanceof Number) {
				realValue = ((Number)value).doubleValue();
			} else if (value instanceof String) {
				// it is a string but it could still code for
				// a boolean, integer or real
				if (value.toString().equalsIgnoreCase("true")) {
					realValue = 1;
				} else if (value.toString().equalsIgnoreCase("false")) {
					realValue = 0;
				} else {
					try {
						realValue = Double.parseDouble(value.toString());
					} catch(NumberFormatException nfe) {
						isNumber = false;
					}
				}
			}

			if (isNumber) {
				if (realValue < minValue) {
					minValue = realValue;
				}
				if (realValue > maxValue) {
					maxValue = realValue;
				}

			}
		}

		if (!isNumber) {
			throw new NumberFormatException("One or more values for this attribute are not numbers");
		}

	}

	// Decorator INTERFACE
	public Paint getPaint(Paint paint) {
		if (this.paint == null) return paint;
		return this.paint;
	}

	public Paint getFillPaint(Paint paint) {
        if (this.fillPaint == null) return paint;
		return fillPaint;
	}

	public Stroke getStroke(Stroke stroke) {
		return stroke;
	}

	public Font getFont(Font font) {
		return font;
	}

	public void setItem(Object item) {
		if (item instanceof Attributable) {
			setAttributableItem((Attributable)item);
		}
	}

	// Private methods
	private void setAttributableItem(Attributable item) {
		paint = null;
		Object value = item.getAttribute(attributeName);

		if (value != null) {

			double number = 0.0;
			if (value instanceof Number) {
				number = ((Number)value).doubleValue();
			} else {
				number = Double.parseDouble(value.toString());
			}
			float p = (float)((number - minValue)/(maxValue - minValue));
			float q = 1.0F - p;

			paint = new Color(
					color1[0] * p + color2[0] * q,
					color1[1] * p + color2[1] * q,
					color1[2] * p + color2[2] * q,
					color1[3] * p + color2[3] * q);
            fillPaint = new Color(paint.getRed(), paint.getGreen(), paint.getBlue(), paint.getAlpha() / 2);
		}
	}

	private final String attributeName;

	private final float[] color1;
	private final float[] color2;

	private double minValue = Double.MAX_VALUE;
	private double maxValue = Double.MIN_VALUE;

	private Color paint = null;
    private Color fillPaint = null;
}
