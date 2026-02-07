package com.uprfvx.pkromio.graphics.packs;

import java.util.Arrays;
import java.util.List;

public class Gen1PlayerCharacterGraphics extends GBCPlayerCharacterGraphics {

    private static final int BACK_IMAGE_DIMENSIONS = 4;
    private static final List<SheetImageDescription> SHEET_IMAGE_DESCRIPTIONS = Arrays.asList(
            new SheetImageDescription("FrontImage", 9, 3, 56, 56),
            new SheetImageDescription("BackImage", 68, 27, 32, 32),
            new SheetImageDescription("WalkSprite", 4, 62, 16, 16,
                    1, new int[][] {{0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}}),
            new SheetImageDescription("BikeSprite", 55, 62, 16, 16,
                    1, new int[][] {{0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}}),
            new SheetImageDescription("FishFrontSprite", 4, 104, 16, 8),
            new SheetImageDescription("FishBackSprite", 21, 112, 16, 8),
            new SheetImageDescription("FishSideSprite", 46, 112, 16, 8)
    );

    public Gen1PlayerCharacterGraphics(GraphicsPackEntry entry) {
        super(entry);
    }

    @Override
    protected int getBackImageDimensions() {
        return BACK_IMAGE_DIMENSIONS;
    }

    @Override
    protected List<SheetImageDescription> getSheetImageDescriptions() {
        return SHEET_IMAGE_DESCRIPTIONS;
    }
}
