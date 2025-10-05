package com.dabomstew.pkromio.graphics.packs;

import com.dabomstew.pkromio.GFXFunctions;
import com.dabomstew.pkromio.graphics.palettes.Palette;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class GraphicsPack {

    protected static class SheetImageDescription {
        // this would be a lovely Record, if Oracle let us use anything past Java 8
        public final String key;
        public final int xOffset;
        public final int yOffset;
        public final int margin;
        public final int frameWidth;
        public final int frameHeight;
        public final int[][] frames;

        public SheetImageDescription(String key, int xOffset, int yOffset, int width, int height) {
            this(key, xOffset, yOffset, width, height, 0, new int[][] {{0, 0}});
        }

        public SheetImageDescription(String key, int xOffset, int yOffset, int frameWidth, int frameHeight,
                                     int margin, int[][] frames) {
            this.key = key;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.margin = margin;
            this.frameHeight = frameHeight;
            this.frameWidth = frameWidth;
            this.frames = frames;
        }
    }

    protected static class SheetPaletteDescription {
        public final String key;
        public final int xOffset;
        public final int yOffset;
        public int width;
        public int height;

        public SheetPaletteDescription(String key, int xOffset, int yOffset) {
            // assuming 16 colors in a nice row, 2x2 color squares
            this(key, xOffset, yOffset, 32, 2);
        }

        public SheetPaletteDescription(String key, int xOffset, int yOffset, int width, int height) {
            this.key = key;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.width = width;
            this.height = height;
        }
    }

    private final GraphicsPackEntry entry;
    private final Map<String, BufferedImage> sheetImages = new HashMap<>();
    private final Map<String, Palette> sheetPalettes = new HashMap<>();

    public GraphicsPack(GraphicsPackEntry entry) {
        // System.out.println("Initializing " + entry.getName() + "...");
        this.entry = entry;
        if (!entry.getStringValue("Sheet").isEmpty()) {
            prepareSheet();
        }
    }

    private void prepareSheet() {
        BufferedImage sheet = readImage("Sheet");
        if (sheet == null) {
            return;
        }
        for (SheetImageDescription desc : getSheetImageDescriptions()) {
            sheetImages.put(desc.key, getImageFromSheet(sheet, desc));
        }
        for (SheetPaletteDescription desc : getSheetPaletteDescriptions()) {
            sheetPalettes.put(desc.key, getPaletteFromSheet(sheet, desc));
        }
    }

    private BufferedImage getImageFromSheet(BufferedImage sheet, SheetImageDescription desc) {
        BufferedImage[] bims = new BufferedImage[desc.frames.length];
        for (int i = 0; i < desc.frames.length; i++) {
            int fx = desc.xOffset + (desc.frames[i][0] * (desc.frameWidth + desc.margin));
            int fy = desc.yOffset + (desc.frames[i][1] * (desc.frameHeight + desc.margin));
            bims[i] = sheet.getSubimage(fx, fy, desc.frameWidth, desc.frameHeight);
        }
        return GFXFunctions.stitchToGrid(new BufferedImage[][] {bims});
    }

    private Palette getPaletteFromSheet(BufferedImage sheet, SheetPaletteDescription desc) {
        BufferedImage subImage = sheet.getSubimage(desc.xOffset, desc.yOffset, desc.width, desc.height);
        return Palette.readImagePaletteFromPixels(subImage);
    }

    protected List<SheetImageDescription> getSheetImageDescriptions() {
        return Collections.emptyList();
    }

    protected List<SheetPaletteDescription> getSheetPaletteDescriptions() {
        return Collections.emptyList();
    }

    public String getName() {
        return entry.getName();
    }

    public String getDescription() {
        return entry.getDescription();
    }

    public String getFrom() {
        return entry.getFrom();
    }

    public String getOriginalCreator() {
        return entry.getOriginalCreator();
    }

    public String getAdapter() {
        return entry.getAdapter();
    }

    public abstract List<BufferedImage> getSampleImages();

    protected GraphicsPackEntry getEntry() {
        return entry;
    }

    protected BufferedImage readImage(String key) {
        if (sheetImages.containsKey(key)) {
            return sheetImages.get(key);
        }
        File imageFile = new File(entry.getPath() + "/" + entry.getStringValue(key));
        if (imageFile.canRead()) {
            try {
                return ImageIO.read(imageFile);
            } catch (IOException e) {
                // System.out.println("Could not read " + imageFile + " as a BufferedImage for " + key);
                return null;
            }
        }
        return null;
    }

    protected Palette readPalette(String key) {
        if (sheetPalettes.containsKey(key)) {
            return sheetPalettes.get(key);
        }
        File paletteFile = new File(entry.getPath() + "/" + entry.getStringValue(key));
        if (paletteFile.canRead()) {
            try {
                return Palette.readFromFile(paletteFile);
            } catch (IOException e) {
                // System.out.println("Could not read " + paletteFile + " as a Palette.");
                return null;
            }
        }
        return null;
    }

    protected boolean usesSheet() {
        return !(sheetImages.isEmpty() && sheetPalettes.isEmpty());
    }

    public BufferedImage toSheet() {
        // TODO
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }

}
