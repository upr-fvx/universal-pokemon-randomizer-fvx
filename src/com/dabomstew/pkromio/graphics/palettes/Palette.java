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

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * A palette, containing multiple {@link Color}s.
 * <p>
 * This class has constructors and methods for converting from/to formats used
 * by the ROMs, and from images, but not for handling compression.
 */
public class Palette implements Cloneable {

    private final static int DEFAULT_PALETTE_SIZE = 16;

    private final Color[] colors;

    /**
     * Reads a Palette from a file. The file has to be in JASC format.
     */
    public static Palette readFromFile(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            if (!br.readLine().equals("JASC-PAL")) {
                throw new IOException("Not JASC-formatted palette.");
            }
            br.readLine(); // I do not know what the second line in JASC files means, the "0100".
            int length = Integer.parseInt(br.readLine());
            Color[] colors = new Color[length];
            for (int i = 0; i < length; i++) {
                colors[i] = new Color(br.readLine());
            }
            return new Palette(colors);
        } catch (Exception e) {
            throw new IOException("Palette format is invalid or corrupt. Only JASC-formatted palettes can be read.", e);
        }
    }

    /**
     * Reads the palette of an image, returning a {@link Palette} object
     * of set size.
     *
     * @param bim An image with either indexed colors or not.
     * @param size The number of colors in the palette. Truncates the factual
     *             palette of the image if it is less than its length, or fills it
     *             out with the default Color if it is more.
     */
    public static Palette readImagePalette(BufferedImage bim, int size) {
        Palette palette;
        if (bim.getColorModel() instanceof IndexColorModel) {
            palette = readImagePaletteFromIndexedColorModel(bim);
        } else {
            palette = readImagePaletteFromPixels(bim);
        }
        palette = new Palette(palette, size);

        return palette;
    }

    /**
     * Reads the palette of an image with indexed colors.
     *
     * @param bim  An image with indexed colors.
     */
    public static Palette readImagePaletteFromIndexedColorModel(BufferedImage bim) {
        if (bim.getRaster().getNumBands() != 1) {
            throw new IllegalArgumentException(
                    "Invalid input; image must have indexed colors (e.g. come from a .bmp file).");
        }

        Palette palette = new Palette(imagePaletteSize(bim));
        for (int i = 0; i < palette.size(); i++) {
            int argb = bim.getColorModel().getRGB(i);
            palette.set(i, new Color(argb));
        }

        return palette;
    }

    private static int imagePaletteSize(BufferedImage bim) {
        int bitsPerColor = bim.getRaster().getSampleModel().getSampleSize(0);
        return 1 << bitsPerColor;
    }

    /**
     * Reads the palette of a {@link BufferedImage}, by scanning the pixels left-to-right, top-to-bottom,
     * to see which colors are used. May return a smaller Palette when used on an image with indexed colors,
     * in case not all of them are used in practice.
     *
     * @param bim An image, either with or without indexed colors.
     */
	public static Palette readImagePaletteFromPixels(BufferedImage bim) {
		List<Integer> colors = new ArrayList<>();
		for (int x = 0; x < bim.getWidth(); x++) {
			for (int y = 0; y < bim.getHeight(); y++) {
				int color = bim.getRGB(x, y);
				if (!colors.contains(color)) {
					colors.add(color);
				}
			}
		}
		return new Palette(colors.stream().mapToInt(Integer::intValue).toArray());
	}

    public static Palette read3DSIconPalette(byte[] iconBytes) {
        int paletteCount = readWord(iconBytes, 2);
        byte[] rawPalette = Arrays.copyOfRange(iconBytes, 4, 4 + paletteCount * 2);
        int[] RGBValues = bytes3DSToARGBValues(rawPalette);
        return new Palette(RGBValues);
    }

    private static int[] bytesToARGBValues(byte[] bytes, Function<Integer, Integer> convWordToARGBFunction) {
        int[] ARGBValues = new int[bytes.length / 2];
        for (int i = 0; i < ARGBValues.length; i++) {
            ARGBValues[i] = convWordToARGBFunction.apply(readWord(bytes, i * 2));
        }
        return ARGBValues;
    }

    private static int[] bytesToARGBValues(byte[] bytes) {
        return bytesToARGBValues(bytes, Color::convHighColorWordToARGB);
    }

    private static int[] bytes3DSToARGBValues(byte[] bytes) {
        return bytesToARGBValues(bytes, Color::conv3DSColorWordToARGB);
    }

    private static int readWord(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    public Palette() {
        this(DEFAULT_PALETTE_SIZE);
    }

    public Palette(int size) {
        this.colors = new Color[size];
        for (int i = 0; i < size; i++) {
            this.colors[i] = new Color();
        }
    }

    public Palette(int size, Color color) {
        this.colors = new Color[size];
        for (int i = 0; i < size; i++) {
            this.colors[i] = color;
        }
    }

    public Palette(Color[] colors) {
        this.colors = colors;
    }

    public Palette(int[] RGBValues) {
        this.colors = new Color[RGBValues.length];
        for (int i = 0; i < colors.length; i++) {
            this.colors[i] = new Color(RGBValues[i]);
        }
    }

    public Palette(byte[] bytes) {
        this(bytesToARGBValues(bytes));
    }

    public Palette(Palette original) {
        this(original, original.size());
    }

    public Palette(Palette original, int size) {
        this.colors = new Color[size];
        for (int i = 0; i < Math.min(size, original.size()); i++) {
            this.colors[i] = new Color(original.get(i));
        }
        for (int i = Math.min(size, original.size()); i < size; i++) {
            this.colors[i] = new Color();
        }
    }

    /**
     * Gets the {@link Color} at index i.
     * @param i index of the color
     */
    public Color get(int i) {
        return colors[i];
    }

    /**
     * Sets the {@link Color} at index i.
     * @param i index to set
     * @param c new Color
     */
    public void set(int i, Color c) {
        colors[i] = c;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[colors.length * 2];
        for (int i = 0; i < colors.length; i++) {
            byte[] colorBytes = colors[i].toBytes();
            bytes[i * 2] = colorBytes[0];
            bytes[i * 2 + 1] = colorBytes[1];
        }
        return bytes;
    }

    public int[] toARGB() {
        int[] ARGB = new int[colors.length];
        for (int i = 0; i < colors.length; i++) {
            ARGB[i] = colors[i].toARGB();
        }
        return ARGB;
    }

    public int size() {
        return colors.length;
    }

    @Override
    public String toString() {
        return Arrays.toString(colors);
    }

    @Override
    @Deprecated
    public Palette clone() {
        System.out.println("Palette.clone() is deprecated. Use copy constructor instead");
        Palette palette = new Palette(colors.length);
        for (int i = 0; i < colors.length; i++) {
            palette.set(i, colors[i].clone());
        }
        return palette;
    }

}
