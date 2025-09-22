package com.dabomstew.pkromio.graphics.packs;

public class Gen1PlayerCharacterGraphics extends GBCPlayerCharacterGraphics {

    private static final int BACK_IMAGE_DIMENSIONS = 4;

    public Gen1PlayerCharacterGraphics(GraphicsPackEntry entry) {
        super(entry);
    }

    @Override
    protected int getBackImageDimensions() {
        return BACK_IMAGE_DIMENSIONS;
    }

}
