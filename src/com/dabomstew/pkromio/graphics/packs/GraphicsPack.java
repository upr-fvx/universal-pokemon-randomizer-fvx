package com.dabomstew.pkromio.graphics.packs;

import com.dabomstew.pkromio.graphics.palettes.Palette;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class GraphicsPack {

    private final GraphicsPackEntry entry;

    public GraphicsPack(GraphicsPackEntry entry) {
        this.entry = entry;
        // System.out.println("Initializing " + getName() + "...");
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

    @Override
    public String toString() {
        return getName();
    }

}
