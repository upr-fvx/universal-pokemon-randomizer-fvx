package com.dabomstew.pkrandom.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;

import com.dabomstew.pkrandom.GFXFunctions;
import com.dabomstew.pkrandom.graphics.palettes.Color;

/**
 * A 2bpp image used by a Gen 1 or Gen 2 game. 
 */
public class GBCImage {
	
	// TODO: see if this limitation exists in Gen 2 as well (probably)
	private static final int MAX_WIDTH_IN_TILES = 15;
	private static final int MAX_WIDTH_IN_PIXELS = MAX_WIDTH_IN_TILES * 8;

	/**
	 * Returns a copy of the input {@link BufferedImage} with "fixed" 2bpp color
	 * indexing. I.e. the returned BufferedImage uses an {@link IndexColorModel}
	 * with four colors, sorted from lightest/white to darkest/black.
	 * 
	 * @param bim The original BufferedImage.
	 */
	private static BufferedImage fixed2bppIndexing(BufferedImage bim) {
		final int bpp = 2;

		List<Integer> colors = new ArrayList<>();
		for (int x = 0; x < bim.getWidth(); x++) {
			for (int y = 0; y < bim.getHeight(); y++) {
				int color = bim.getRGB(x, y);
				if (!colors.contains(color)) {
					colors.add(color);
				}
			}
		}
		colors.sort((c1, c2) -> Double.compare(new com.dabomstew.pkrandom.graphics.palettes.Color(c2).toHSV()[2], new Color(c1).toHSV()[2]));

		BufferedImage fixed = new BufferedImage(bim.getWidth(), bim.getHeight(), BufferedImage.TYPE_BYTE_INDEXED,
				GFXFunctions.indexColorModelFromPalette(toArray(colors), bpp));
		Graphics2D g = fixed.createGraphics();
		g.drawImage(bim, 0, 0, null);

		return fixed;
	}

	private static int[] toArray(List<Integer> colors) {
		int[] palette = new int[colors.size()];
		for (int i = 0; i < colors.size(); i++) {
			palette[i] = colors.get(i);
		}
		return palette;
	}

	private final BufferedImage image;
	private byte[] data;

	private boolean bitplanesPrepared;
	private BufferedImage bitplane1Image;
	private BufferedImage bitplane2Image;
	private byte[] bitplane1Data;
	private byte[] bitplane2Data;

	public GBCImage(BufferedImage bim) {
		this.image = fixed2bppIndexing(bim);
		if (getWidthInTiles() > MAX_WIDTH_IN_TILES || getHeightInTiles() > MAX_WIDTH_IN_TILES) { // allows non-square images
			throw new IllegalArgumentException("Width or height of image exceeds " + MAX_WIDTH_IN_TILES + " tiles ("
					+ MAX_WIDTH_IN_PIXELS + " pixels).");
		}
	}


	public BufferedImage getImage() { //TODO: should GBCimage extend BufferedImage instead?
		return image;
	}

	public int getWidthInTiles() {
		return image.getWidth() / 8;
	}
	
	public int getHeightInTiles() {
		return image.getHeight() / 8;
	}

	public byte[] getData() {
		if (data == null) {
			prepareBitplanes();
			data = new byte[bitplane1Data.length * 2];
			for (int i = 0; i < bitplane1Data.length; i++) {
				data[i * 2] = bitplane1Data[i];
				data[i * 2 + 1] = bitplane2Data[i];
			}
		}
		return this.data;
	}

	public byte[] getFlattenedData() {
		return null;
		// TODO: what does this even mean?
	}

	public byte[] getBitplane1Data() {
		prepareBitplanes();
		return bitplane1Data;
	}

	public byte[] getBitplane2Data() {
		prepareBitplanes();
		return bitplane2Data;
	}

	public BufferedImage getBitplane1Image() {
		prepareBitplanes();
		return bitplane1Image;
	}

	public BufferedImage getBitplane2Image() {
		prepareBitplanes();
		return bitplane2Image;
	}

	private void prepareBitplanes() {
		if (bitplanesPrepared) {
			return;
		}

		bitplane1Image = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_BYTE_BINARY);
		bitplane2Image = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_BYTE_BINARY);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int sample = image.getData().getSample(x, y, 0);
				if (sample % 2 == 1) {
					bitplane1Image.setRGB(x, y, 0xffffffff);
				}
				if (sample >= 2) {
					bitplane2Image.setRGB(x, y, 0xffffffff);
				}
			}
		}
		bitplane1Data = GFXFunctions.readTiledImageData(bitplane1Image, 1);
		bitplane2Data = GFXFunctions.readTiledImageData(bitplane2Image, 1);

		bitplanesPrepared = true;
	}

}
