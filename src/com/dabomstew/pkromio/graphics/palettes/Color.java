package com.dabomstew.pkromio.graphics.palettes;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer" by Dabomstew                   --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2012.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An RGB color, usually to be contained by a {@link Palette}, and methods for
 * converting to/from other formats. These formats include 32-bit ARGB, and high
 * color words (16 bits, little endian) as used by the ROMs.
 */
public class Color implements Cloneable {

	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color BLACK = new Color(0, 0, 0);

	private static final Color DEFAULT_COLOR = WHITE;

	private static final String HEX_REGEX = "[0-9abcdefABCDEF]{6}";

	private static Color colorFromString(String string) {
		Matcher hexMatcher = Pattern.compile(HEX_REGEX).matcher(string);
		if (hexMatcher.find()) {
			String hex = hexMatcher.group();
			return new Color(Integer.parseInt(hex, 16));
		}

		// Yes, this is kind of ugly, but Matcher doesn't allow a straight-forward
		// way of counting the matches...
		Matcher intMatcher = Pattern.compile("[0-9]+").matcher(string);
		int r = 0, g = 0, b = 0;
		if (intMatcher.find())
			r = Integer.parseInt(intMatcher.group());
		if (intMatcher.find())
			g = Integer.parseInt(intMatcher.group());
		if (intMatcher.find()) {
			b = Integer.parseInt(intMatcher.group());
			return new Color(r, g, b);
		}

		throw new IllegalArgumentException("No color value found in \"" + string + "\".");
	}

	/**
	 * Returns a new Color from hue, saturation, and value.
	 * 
	 * @param h hue (0.0 - 360.0)
	 * @param s saturation (0.0 - 1.0)
	 * @param v value (0.0 - 1.0)
	 */
	public static Color colorFromHSV(double h, double s, double v) {
		// using this formula: https://www.rapidtables.com/convert/color/hsv-to-rgb.html
		if (h < 0.0 || h > 360.0) {
			throw new IllegalArgumentException("hue argument \"h\" out of bounds (0.0 - 360.0)");
		}
		if (s < 0.0 || s > 1.0) {
			throw new IllegalArgumentException("saturation argument \"s\" out of bounds (0.0 - 1.0)");
		}
		if (v < 0.0 || v > 1.0) {
			throw new IllegalArgumentException("value argument \"v\" out of bounds (0.0 - 1.0)");
		}

		double c = v * s;
		double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
		double m = v - c;

		double[][] rgbBases = new double[][] { { c, x, 0 }, { x, c, 0 }, { 0, c, x }, { 0, x, c }, { x, 0, c },
				{ c, 0, x } };
		double[] rgbBase = rgbBases[(int) (h / 60)];

		int r = (int) ((rgbBase[0] + m) * 255);
		int g = (int) ((rgbBase[1] + m) * 255);
		int b = (int) ((rgbBase[2] + m) * 255);
		return new Color(r, g, b);
	}

	public static int convHighColorWordToARGB(int word) {
		int red = (int) ((word & 0x1F) * 8.25);
		int green = (int) (((word & 0x3E0) >> 5) * 8.25);
		int blue = (int) (((word & 0x7C00) >> 10) * 8.25);
		return 0xFF000000 | (red << 16) | (green << 8) | blue;
	}

	public static int conv3DSColorWordToARGB(int word) {
		int alpha = (word & 0x1) * 0xFF;
		int blue = (int) (((word & 0x3E) >> 1) * 8.25);
		int green = (int) (((word & 0x7C0) >> 6) * 8.25);
		int red = (int) (((word & 0xF800) >> 11) * 8.25);
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	private int r, g, b;

	public Color() {
		this(DEFAULT_COLOR.r, DEFAULT_COLOR.b, DEFAULT_COLOR.g);
	}

	/**
	 * The primary Color constructor, taking an int each for red, green, and blue.
	 * All other constructors should go through this somehow, as it is the only one
	 * that does bounds-checking.
	 * 
	 * @param r red value (0-255)
	 * @param g green value (0-255)
	 * @param b blue value (0-255)
	 */
	public Color(int r, int g, int b) {
		if (r < 0 || r > 255) {
			throw new IllegalArgumentException("red value out of bounds (0-255)");
		}
		if (g < 0 || g > 255) {
			throw new IllegalArgumentException("green value out of bounds (0-255)");
		}
		if (b < 0 || b > 255) {
			throw new IllegalArgumentException("blue value out of bounds (0-255)");
		}
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public Color(int hex) {
		this((hex & 0xFF0000) >> 16, (hex & 0xFF00) >> 8, (hex & 0xFF));
	}

	public Color(String string) {
		Color color = colorFromString(string);
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
	}

	public Color(Color original) {
		this.r = original.r;
		this.g = original.g;
		this.b = original.b;
	}

	@Override
	@Deprecated
	public Color clone() {
		System.out.println("Color.clone() is deprecated. Use copy constructor instead.");
		return new Color(r, g, b);
	}

	@Override
	public String toString() {
		return "(" + r + ", " + g + ", " + b + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Color) {
			Color otherColor = (Color) other;
			return r == otherColor.r && g == otherColor.g && b == otherColor.b;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(r, g, b);
	}

	public byte[] toBytes() {
		int red = Math.floorDiv(r, 8);
		int green = Math.floorDiv(g, 8);
		int blue = Math.floorDiv(b, 8);
		int bytesSum = blue * 1024 + red + green * 32;
		return new byte[] { (byte) bytesSum, (byte) Math.floorDiv(bytesSum, 256) };
	}

	public int toARGB() {
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	public int[] toInts() {
		return new int[] { r, g, b };
	}

	public double[] toHSV() {
		// using this formula: https://www.rapidtables.com/convert/color/rgb-to-hsv.html
		double rFloat = ((double) r) / 255;
		double gFloat = ((double) g) / 255;
		double bFloat = ((double) b) / 255;

		double cMax = Math.max(Math.max(rFloat, gFloat), bFloat);
		double cMin = Math.min(Math.min(rFloat, gFloat), bFloat);
		double delta = cMax - cMin;

		double h;
		if (delta == 0) {
			h = 0;
		} else if (cMax == rFloat) {
			h = 60 * (((gFloat - bFloat) / delta) % 6);
		} else if (cMax == gFloat) {
			h = 60 * (((bFloat - rFloat) / delta) + 2);
		} else { // cMax == bFloat
			h = 60 * (((rFloat - gFloat) / delta) + 4);
		} 
		double s = cMax == 0 ? 0 : delta / cMax;
		double v = cMax;

		return new double[] { h, s, v };
	}

	public int getComp(int i) {
		switch (i) {
			case 0:
				return r;
			case 1:
				return g;
			case 2:
				return b;
			default:
				throw new IndexOutOfBoundsException(i + " out of bounds for RGB color (0=r, 1=g, 2=b).");
		}
	}

	public void setComp(int i, int value) {
		switch (i) {
			case 0:
				r = value;
				break;
			case 1:
				g = value;
				break;
			case 2:
				b = value;
				break;
			default:
				throw new IndexOutOfBoundsException(i + " out of bounds for RGB color (0=r, 1=g, 2=b).");
		}
	}

	@FunctionalInterface
	public interface ColorChange {
		int change(Color orig, int i);
	}

	public void setComps(ColorChange cc) {
		for (int i = 0; i < 3; i++) {
			int value = cc.change(this, i);
			value = Math.max(value, 0);
			value = Math.min(value, 255);
			this.setComp(i, value);
		}
	}

}
