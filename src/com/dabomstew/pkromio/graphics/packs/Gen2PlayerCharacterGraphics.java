package com.dabomstew.pkromio.graphics.packs;

import com.dabomstew.pkromio.graphics.images.GBCImage;
import com.dabomstew.pkromio.graphics.palettes.Color;
import com.dabomstew.pkromio.graphics.palettes.Gen2SpritePaletteID;
import com.dabomstew.pkromio.graphics.palettes.Palette;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Gen2PlayerCharacterGraphics extends GBCPlayerCharacterGraphics {

    private static final List<SheetImageDescription> SHEET_IMAGE_DESCRIPTIONS = Arrays.asList(
            new SheetImageDescription("FrontImage", 9, 3, 56, 56),
            new SheetImageDescription("BackImage", 68, 11, 48, 48),
            new SheetImageDescription("WalkSprite", 12, 62, 16, 16,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}}),
            new SheetImageDescription("BikeSprite", 63, 62, 16, 16,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}}),
            new SheetImageDescription("FishFrontSprite", 12, 104, 16, 8),
            new SheetImageDescription("FishBackSprite", 29, 112, 16, 8),
            new SheetImageDescription("FishSideSprite", 54, 112, 16, 8)
    );

    private static final int BACK_IMAGE_DIMENSIONS = 6;
    private static final int TRAINER_CARD_IMAGE_WIDTH = 5;
    private static final int TRAINER_CARD_IMAGE_HEIGHT = 7;

    private final GBCImage trainerCard;
    private final Palette imagePalette;
    private final Gen2SpritePaletteID spritePaletteID;

    public Gen2PlayerCharacterGraphics(GraphicsPackEntry entry) {
        super(entry);
        this.trainerCard = initTrainerCard();
        this.imagePalette = initImagePalette();
        this.spritePaletteID = initSpritePaletteID();
    }

    @Override
    protected int getBackImageDimensions() {
        return BACK_IMAGE_DIMENSIONS;
    }

    private GBCImage initTrainerCard() {
        GBCImage trainerCard = initTrainerCardFromFile();
        if (trainerCard == null && hasFrontImage()) {
            trainerCard = initTrainerCardFromFrontImage();
        }
        return trainerCard;
    }

    private GBCImage initTrainerCardFromFile() {
        BufferedImage base = readImage("TrainerCardImage");
        if (base == null) {
            return null;
        }
        GBCImage trainerCard = new GBCImage.Builder(base).columnMode(true).build();
        if (trainerCard.getWidthInTiles() != TRAINER_CARD_IMAGE_WIDTH ||
                trainerCard.getHeightInTiles() != TRAINER_CARD_IMAGE_HEIGHT) {
            System.out.println(getName() + ": Invalid trainer card image dimensions");
            return null;
        }
        return trainerCard;
    }

    private GBCImage initTrainerCardFromFrontImage() {
        return getFrontImage().getSubimageFromTileRect(1, 0, TRAINER_CARD_IMAGE_WIDTH, TRAINER_CARD_IMAGE_HEIGHT);
    }

    private Palette initImagePalette() {
        Palette palette = readPalette("ImagePalette");
        if (palette == null) {
            Palette fourColors;
            if (hasFrontImage()) {
                fourColors = getFrontImage().getPalette();
            } else if (hasBackImage()) {
                fourColors = getBackImage().getPalette();
            } else {
                return null;
            }
            return new Palette(new Color[]{fourColors.get(1), fourColors.get(2)});
        }
        if (palette.size() != 2) {
            System.out.println(getName() + ": Invalid ImagePalette; wrong amount of colors. Expected 2, was " + palette.size());
            return null;
        }
        return palette;
    }

    private Gen2SpritePaletteID initSpritePaletteID() {
        Gen2SpritePaletteID palID;
        String paletteName = getEntry().getStringValue("SpritePalette");
        if (paletteName.isEmpty()) {
            palID = Gen2SpritePaletteID.getMatching(getWalkSprite().getPalette());
        } else {
            try {
                palID = Gen2SpritePaletteID.valueOf(paletteName.toUpperCase());
            } catch (IllegalArgumentException e) {
                palID = null;
            }
        }
        return palID == null ? Gen2SpritePaletteID.RED : palID; // default to RED
    }

    // no hasTrainerCardImage(); redundant with hasFrontImage()

    public GBCImage getTrainerCardImage() {
        return trainerCard;
    }

    public boolean hasImagePalette() {
        return imagePalette != null;
    }

    public Palette getImagePalette() {
        return imagePalette;
    }

    public boolean hasSpritePaletteID() {
        return spritePaletteID != null;
    }

    public Gen2SpritePaletteID getSpritePaletteID() {
        return spritePaletteID;
    }

    @Override
    protected Palette getOverworldPalette() {
        return getSpritePaletteID().getPalette();
    }

    @Override
    public List<BufferedImage> getSampleImages() {
        List<BufferedImage> sampleImages = new ArrayList<>(super.getSampleImages());
        sampleImages.add(getTrainerCardImage());
        return sampleImages;
    }

    @Override
    protected List<SheetImageDescription> getSheetImageDescriptions() {
        return SHEET_IMAGE_DESCRIPTIONS;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (obj instanceof Gen2PlayerCharacterGraphics) {
            Gen2PlayerCharacterGraphics other = (Gen2PlayerCharacterGraphics) obj;
            return Objects.equals(trainerCard, other.trainerCard)
                    && imagePalette.equals(other.imagePalette)
                    && spritePaletteID == other.spritePaletteID;
        }
        return false;
    }

}
