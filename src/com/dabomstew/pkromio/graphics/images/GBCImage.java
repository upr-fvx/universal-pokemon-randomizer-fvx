package com.dabomstew.pkromio.graphics.images;

import com.dabomstew.pkromio.graphics.palettes.Color;
import com.dabomstew.pkromio.graphics.palettes.Palette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A 2bpp image used by a Gen 1 or Gen 2 game. The GBCImage itself is a {@link BufferedImage} with an
 * {@link IndexColorModel} of 4 colors, but can be converted from/to a byte array format used by the games.
 * <a href=https://www.huderlem.com/demos/gameboy2bpp.html>That format is explained here</a>.
 */
public class GBCImage extends TiledImage {

    private static final int TILE_SIZE = 8;
    private static final int BPP = 2;
    private static final int PALETTE_SIZE = 4;

    public static final Palette DEFAULT_PALETTE = new Palette(new Color[]{
            Color.WHITE, new Color(0xFFAAAAAA), new Color(0xFF666666), Color.BLACK});

    public static class Builder extends TiledImage.Builder<GBCImage> {

        public Builder(int width, int height, Palette palette) {
            super(width, height, palette);
        }

        public Builder(int width, int height, Palette palette, byte[] data) {
            super(width, height, palette, data);
        }

        public Builder(BufferedImage bim) {
            super(bim);
        }

        public Builder(File file) throws IOException {
            super(file);
        }

        /**
         * Fills out the palette from the image out to have 4 colors if there are less,
         * and reorders them from brightest to darkest. True white (=FFFFFF) is always put at the start of the palette,
         * and true black (=000000) is always put at the end.
         */
        @Override
        protected Palette preparePalette(BufferedImage bim) {
            Palette palette = Palette.readImagePaletteFromPixels(bim);
            if (palette.size() > 4) {
                throw new IllegalArgumentException("palette.size()=" + palette.size() + " exceeds max palette size "
                        + PALETTE_SIZE + ".");
            }
            List<Integer> colors = new ArrayList<>(Arrays.stream(palette.toARGB()).boxed().collect(Collectors.toList()));
            colors.sort((c1, c2) -> compareColors(new Color(c1), new Color(c2)));
            Palette fixed = new Palette(DEFAULT_PALETTE);
            for (int i = 0; i < colors.size(); i++) {
                Color color = new Color(colors.get(i));
                if (!color.equals(Color.WHITE) && !color.equals(Color.BLACK)) {
                    fixed.set(i, color);
                }
            }

            return fixed;
        }

        private static int compareColors(Color a, Color b) {
            double[] aHSV = a.toHSV();
            double[] bHSV = b.toHSV();
            int compare = Double.compare(bHSV[2], aHSV[2]);
            if (compare == 0) {
                compare = Double.compare(aHSV[1], bHSV[1]);
                if (compare == 0) {
                    compare = Double.compare(aHSV[0], bHSV[0]);
                }
            }
            return compare;
        }

        @Override
        protected int getBPP() {
            return BPP;
        }

        @Override
        protected GBCImage init(int width, int height, IndexColorModel colorModel, boolean columnMode) {
            return new GBCImage(width, height, colorModel, columnMode);
        }
    }

    protected GBCImage(int width, int height, IndexColorModel colorModel, boolean columnMode) {
        super(width, height, colorModel, columnMode);
    }

    private boolean bitplanesPrepared;
    private BufferedImage bitplane1Image;
    private BufferedImage bitplane2Image;

    @Override // TODO: very temp until these methods are rewritten so the base case isn't 16 colors
    protected int[] initColors() {
        int[] colors = new int[4];
        IndexColorModel colorModel = (IndexColorModel) getColorModel();
        colorModel.getRGBs(colors);
        return colors;
    }

    @Override
    protected void drawTileData(byte[] data) {
        int dataNumTiles = data.length / TILE_SIZE / BPP;
        int imageNumTiles = getWidthInTiles() * getHeightInTiles();
        for (int tile = 0; tile < Math.min(dataNumTiles, imageNumTiles); tile++) {
            int tileX = columnMode ? tile / getWidthInTiles() : tile % getWidthInTiles();
            int tileY = columnMode ? tile % getWidthInTiles() : tile / getWidthInTiles();
            for (int yT = 0; yT < 8; yT++) {
                int lowByte = data[(tile * 8 + yT) * 2];
                int highByte = data[(tile * 8 + yT) * 2 + 1];
                for (int xT = 0; xT < 8; xT++) {
                    int low = (lowByte >>> (7 - xT)) & 1;
                    int high = (highByte >>> (7 - xT)) & 1;
                    int colorIndex = (high << 1) + low;
                    setColor(tileX * 8 + xT, tileY * 8 + yT, colorIndex);
                }
            }
        }
    }

    @Override
    protected void drawImage(BufferedImage bim) {
        Graphics2D g = createGraphics();
        g.drawImage(bim, 0, 0, null);
    }

    @Override
    public byte[] toBytes() {
        byte[] data = new byte[getWidthInTiles() * getHeightInTiles() * TILE_SIZE * BPP];
        int numTiles = getWidthInTiles() * getHeightInTiles();

        for (int tile = 0; tile < numTiles; tile++) {
            int tileX = columnMode ? tile / getHeightInTiles() : tile % getWidthInTiles();
            int tileY = columnMode ? tile % getHeightInTiles() : tile / getWidthInTiles();
            for (int yT = 0; yT < 8; yT++) {
                int lowByte = 0;
                int highByte = 0;
                for (int xT = 0; xT < 8; xT++) {
                    int sample = getData().getSample(tileX * 8 + xT, tileY * 8 + yT, 0);
                    lowByte |= (sample & 1) << (7 - xT);
                    highByte |= ((sample >>> 1) & 1) << (7 - xT);
                }
                data[(tile * 8 + yT) * 2] = (byte) lowByte;
                data[(tile * 8 + yT) * 2 + 1] = (byte) highByte;
            }
        }

        return data;
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

        bitplane1Image = new BufferedImage(getWidth(), getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);
        bitplane2Image = new BufferedImage(getWidth(), getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g1 = bitplane1Image.createGraphics();
        g1.setColor(java.awt.Color.WHITE);
        g1.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2 = bitplane2Image.createGraphics();
        g2.setColor(java.awt.Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int sample = getData().getSample(x, y, 0);
                if (sample % 2 == 1) {
                    bitplane1Image.setRGB(x, y, 0xff000000);
                }
                if (sample >= 2) {
                    bitplane2Image.setRGB(x, y, 0xff000000);
                }
            }
        }

        bitplanesPrepared = true;
    }

    public Palette getPalette() {
        return new Palette(colors);
    }

    @Override
    public GBCImage getSubimageFromTileRange(int from, int to) {
        GBCImage subimage = new GBCImage.Builder(to - from, 1, getPalette()).build();
        Graphics2D g = subimage.createGraphics();
        for (int tile = from; tile < to; tile++) {
            int tileX = columnMode ? tile / getWidthInTiles() : tile % getWidthInTiles();
            int tileY = columnMode ? tile % getWidthInTiles() : tile / getWidthInTiles();
            g.drawImage(this, (tile - from) * TILE_SIZE, 0, (tile - from + 1) * TILE_SIZE, TILE_SIZE,
                    tileX * TILE_SIZE, tileY * TILE_SIZE, (tileX + 1) * TILE_SIZE,
                    (tileY + 1) * TILE_SIZE, null);
        }
        return subimage;
    }

    @Override
    public GBCImage getSubimageFromTileRect(int x, int y, int w, int h) {
        BufferedImage subimage = getSubimage(x * TILE_SIZE, y * TILE_SIZE, w * TILE_SIZE, h * TILE_SIZE);
        return new GBCImage.Builder(subimage).columnMode(columnMode).build();
    }

    @Override
    public TiledImage getSubimageFromFrame(int i) {
        if (frameWidth == 0 || frameHeight == 0) {
            throw new IllegalStateException("Must set the dimensions first, or use getSubimageFromFrame(i, w, h).");
        }
        return getSubimageFromFrame(i, frameWidth, frameHeight);
    }

    @Override
    public TiledImage getSubimageFromFrame(int i, int w, int h) {
        if (getWidthInTiles() % w != 0 || getHeightInTiles() % h != 0) {
            throw new IllegalArgumentException("Image cannot be split into frames that are " + w + "x" + h + " tiles.");
        }
        int x = (i * w) % getWidthInTiles();
        int y = ((i * w) / getWidthInTiles()) * h;
        return getSubimageFromTileRect(x, y, w, h);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GBCImage) {
            GBCImage otherImage = (GBCImage) other;
            return Arrays.equals(getRasterData(), otherImage.getRasterData());
        }
        return false;
    }

    private int[] getRasterData() {
        Raster raster = getRaster();
        return raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(),
                new int[raster.getWidth() * raster.getHeight()]);
    }

}
