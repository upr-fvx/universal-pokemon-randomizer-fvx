package com.dabomstew.pkromio.graphics.images;

import com.dabomstew.pkromio.graphics.palettes.Palette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

/**
 * An image for GBA games with 16-color palettes. The GBAImage itself is a {@link BufferedImage} with an
 * {@link IndexColorModel} of 16 colors, but can be converted from/to a byte array format used by the games.
 */
public class GBAImage extends TiledImage {

    private static final int BPP = 4;

    public static class Builder extends TiledImage.Builder<GBAImage> {

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

        @Override
        protected Palette preparePalette(BufferedImage bim) {
            return Palette.readImagePalette(bim, 16);
        }

        @Override
        protected int getBPP() {
            return BPP;
        }

        @Override
        protected GBAImage init(int width, int height, IndexColorModel colorModel, boolean columnMode) {
            return new GBAImage(width, height, colorModel, columnMode);
        }
    }

    protected GBAImage(int width, int height, IndexColorModel colorModel, boolean columnMode) {
        super(width, height, colorModel, columnMode);
    }

    @Override
    protected void drawTileData(byte[] data) {
        int dataNumTiles = data.length / TILE_SIZE / BPP;
        int imageNumTiles = getWidthInTiles() * getHeightInTiles();
        int next = 0;
        for (int tile = 0; tile < Math.min(dataNumTiles, imageNumTiles); tile++) {
            int tileX = columnMode ? tile / getHeightInTiles() : tile % getWidthInTiles();
            int tileY = columnMode ? tile % getHeightInTiles() : tile / getWidthInTiles();
            for (int yT = 0; yT < TILE_SIZE; yT++) {
                for (int xT = 0; xT < TILE_SIZE; xT += 2) {
                    int pixel1 = data[next] & 0xF;
                    int pixel2 = (data[next] >>> BPP) & 0xF;
                    setColor(tileX * TILE_SIZE + xT, tileY * TILE_SIZE + yT, pixel1);
                    setColor(tileX * TILE_SIZE + xT + 1, tileY * TILE_SIZE + yT, pixel2);
                    next++;
                }
            }
        }
    }

    @Override
    protected void drawImage(BufferedImage bim) {
        if (bim.getColorModel() instanceof IndexColorModel) {
            IndexColorModel indexColorModel = (IndexColorModel) bim.getColorModel();
            if (indexColorModel.isCompatibleRaster(getData())) {
                Raster from = bim.getData();
                WritableRaster to = getRaster();
                for (int x = 0; x < getWidth(); x++) {
                    for (int y = 0; y < getHeight(); y++) {
                        int s = from.getSample(x, y, 0);
                        to.setSample(x, y, 0, s);
                    }
                }
            }
        } else {
            Graphics2D g = createGraphics();
            g.drawImage(bim, 0, 0, null);
        }
    }

    @Override
    public byte[] toBytes() {
        byte[] data = new byte[getWidthInTiles() * getHeightInTiles() * TILE_SIZE * BPP];
        int numTiles = getWidthInTiles() * getHeightInTiles();

        int next = 0;
        for (int tile = 0; tile < numTiles; tile++) {
            int tileX = columnMode ? tile / getHeightInTiles() : tile % getWidthInTiles();
            int tileY = columnMode ? tile % getHeightInTiles() : tile / getWidthInTiles();

            for (int yT = 0; yT < TILE_SIZE; yT++) {
                for (int xT = 0; xT < TILE_SIZE; xT += 2) {
                    int pixel1 = getData().getSample(tileX * TILE_SIZE + xT, tileY * TILE_SIZE + yT, 0);
                    int pixel2 = getData().getSample(tileX * TILE_SIZE + xT + 1, tileY * TILE_SIZE + yT, 0);
                    data[next] = (byte) ((pixel2 << 4) + pixel1);
                    next++;
                }
            }
        }

        return data;
    }

    @Override
    public GBAImage getSubimageFromTileRange(int from, int to) {
        // TODO: make this use raster drawing instead
        GBAImage subimage = new Builder(to - from, 1, getPalette()).build();
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
    public GBAImage getSubimageFromTileRect(int x, int y, int w, int h) {
        BufferedImage subimage = getSubimage(x * TILE_SIZE, y * TILE_SIZE, w * TILE_SIZE, h * TILE_SIZE);
        return new Builder(subimage).columnMode(columnMode).build();
    }

    @Override
    public GBAImage getSubimageFromFrame(int i) {
        if (frameWidth == 0 || frameHeight == 0) {
            throw new IllegalStateException("Must set the dimensions first, or use getSubimageFromFrame(i, w, h).");
        }
        return getSubimageFromFrame(i, frameWidth, frameHeight);
    }

    @Override
    public GBAImage getSubimageFromFrame(int i, int w, int h) {
        if (getWidthInTiles() % w != 0 || getHeightInTiles() % h != 0) {
            throw new IllegalArgumentException("Image cannot be split into frames that are " + w + "x" + h + " tiles.");
        }
        int x = (i * w) % getWidthInTiles();
        int y = ((i * w) / getWidthInTiles()) * h;
        return getSubimageFromTileRect(x, y, w, h);
    }

}
