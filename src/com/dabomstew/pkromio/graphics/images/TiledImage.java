package com.dabomstew.pkromio.graphics.images;

import com.dabomstew.pkromio.GFXFunctions;
import com.dabomstew.pkromio.graphics.palettes.Palette;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public abstract class TiledImage extends BufferedImage {

    protected static final int TILE_SIZE = 8;

    public abstract static class Builder<T extends TiledImage> {

        private final int width;
        private final int height;
        private Palette palette;

        private byte[] data;
        private BufferedImage bim;

        private boolean columnMode;
        private boolean transparent; // TODO: implement background/first palette index transparency

        public Builder(int width, int height, Palette palette) {
            this.width = width;
            this.height = height;
            this.palette = palette;
        }

        public Builder(int width, int height, Palette palette, byte[] data) {
            this(width, height, palette);
            this.data = data;
        }

        public Builder(BufferedImage bim) {
            if (bim.getWidth() % TILE_SIZE != 0 || bim.getHeight() % TILE_SIZE != 0) {
                throw new IllegalArgumentException(bim + " has invalid dimensions " + bim.getWidth() + "x" +
                        bim.getHeight() + " pixels. Must be multiples of " + TILE_SIZE);
            }
            this.width = bim.getWidth() / 8;
            this.height = bim.getHeight() / 8;
            this.bim = bim;
        }

        public Builder(File file) throws IOException {
            this(ImageIO.read(file));
        }

        /**
         * @param columnMode If true, the data will be column-by-column, instead of row-by-row.
         *                   This affects the data reading, as well as the output from {@link #toBytes()}.
         *                   Mirrors <a href=https://rgbds.gbdev.io/docs/v0.6.1/rgbgfx.1/#Z>"rgbgfx -Z"</a>.
         */
        public Builder<T> columnMode(boolean columnMode) {
            this.columnMode = columnMode;
            return this;
        }

        public Builder<T> transparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }

        public T build() {
            if (bim != null) {
                palette = preparePalette(bim);
            }
            IndexColorModel colorModel = GFXFunctions.indexColorModelFromPalette(palette, getBPP());
            T t = init(width, height, colorModel, columnMode);

            if (data != null) {
                t.drawTileData(data);
            } else if (bim != null) {
                t.drawImage(bim);
            }

            return t;
        }

        protected abstract Palette preparePalette(BufferedImage image);

        protected abstract int getBPP();

        protected abstract T init(int width, int height, IndexColorModel colorModel, boolean columnMode);
    }

    protected final boolean columnMode;
    protected final int[] colors;

    protected int frameWidth;
    protected int frameHeight;
    protected int frameAmount;

    protected TiledImage(int width, int height, IndexColorModel colorModel, boolean columnMode) {
        super(width * TILE_SIZE, height * TILE_SIZE, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
        this.columnMode = columnMode;
        this.colors = initColors();
    }

    protected int[] initColors() {
        int[] colors = new int[16];
        IndexColorModel colorModel = (IndexColorModel) getColorModel();
        colorModel.getRGBs(colors);
        return colors;
    }

    protected abstract void drawTileData(byte[] data);

    protected abstract void drawImage(BufferedImage bim);

    public void setColor(int x, int y, int colorIndex) {
        getRaster().setSample(x, y, 0, colorIndex);
    }

    public int getWidthInTiles() {
        return getWidth() / TILE_SIZE;
    }

    public int getHeightInTiles() {
        return getHeight() / TILE_SIZE;
    }

    public void setFrameDimensions(int frameWidth, int frameHeight) {
        if (frameWidth <= 0 || frameHeight <= 0) {
            throw new IllegalArgumentException("Invalid dimensions " + frameWidth + "x" + frameHeight + ". " +
                    "Both dimensions must be >= 1.");
        }
        if (getWidthInTiles() % frameWidth != 0 || getHeightInTiles() % frameHeight != 0) {
            throw new IllegalArgumentException("Image cannot be split into frames that are " + frameWidth + "x" +
                    frameHeight + " tiles.");
        }
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameAmount = (getWidthInTiles() * getHeightInTiles()) / (frameWidth * frameHeight);
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public int getFrameAmount() {
        return frameAmount;
    }

    public abstract byte[] toBytes();

    public Palette getPalette() {
        return new Palette(colors);
    }

    // TODO: must be a way to make these getSubimage methods non-abstract...

    /**
     * Returns a subimage consisting of only the tiles within a given range.
     *
     * @param from the initial index of the range, inclusive
     * @param to   the final index of the range, exclusive
     */
    public abstract TiledImage getSubimageFromTileRange(int from, int to);

    /**
     * Returns a subimage defined by a rectangular region - in tiles. Similar to
     * {@link BufferedImage#getSubimage(int, int, int, int)}.<br>
     * The columnmode of the subimage is the same as the source image.
     *
     * @param x the X coordinate of the upper-left corner tile of the specified rectangular region
     * @param y the Y coordinate of the upper-left corner tile of the specified rectangular region
     * @param w the width in tiles
     * @param h the height in tiles
     */
    public abstract TiledImage getSubimageFromTileRect(int x, int y, int w, int h);

    /**
     * A version of {@link #getSubimageFromFrame(int, int, int)} where the
     * dimensions have already been set by {@link #setFrameDimensions(int, int)}. <br>
     * Throws an {@link IllegalStateException} if the dimensions are not set.
     *
     * @param i the index of the frame
     */
    public abstract TiledImage getSubimageFromFrame(int i);

    /**
     * Returns a subimage defined as a "frame", a rectangular region
     * given by its index and dimensions. Throws an {@link IllegalArgumentException}
     * if the dimensions of the BufferedImage are not divisible by the frame dimensions.
     * <br><br>
     * This method allows for easier handling of "sheets" of images, since the
     * return is the same regardless whether the frames are laid out horizontally,
     * vertically, or even in a 2D grid. Frames are read row-for-row, left-to-right,
     * top to bottom. I.e.: <br>
     * -----<br>
     * |0|1|<br>
     * |2|3|<br>
     * -----<br>
     *
     * @param i the index of the frame
     * @param w the width of a frame in tiles
     * @param h the height of a frame in tiles
     */
    public abstract TiledImage getSubimageFromFrame(int i, int w, int h);

    @Override
    public boolean equals(Object other) {
        if (other instanceof TiledImage) {
            TiledImage otherImage = (TiledImage) other;
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
