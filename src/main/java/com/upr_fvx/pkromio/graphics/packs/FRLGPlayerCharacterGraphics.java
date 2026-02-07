package com.upr_fvx.pkromio.graphics.packs;

import com.upr_fvx.pkromio.GFXFunctions;
import com.upr_fvx.pkromio.graphics.images.GBAImage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FRLGPlayerCharacterGraphics extends Gen3PlayerCharacterGraphics {

    private static final List<SheetImageDescription> SHEET_IMAGE_DESCRIPTIONS = Arrays.asList(
            new SheetImageDescription("BackImage", 90, 53, 64, 64,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}}),
            new SheetImageDescription("SitSprite", 11, 327, 16, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}}),
            new SheetImageDescription("SurfBlobSprite", 32, 327, 32, 32,
                    1, new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}, {0, 2}, {1, 2}}),
            new SheetImageDescription("BirdSprite", 102, 327, 64, 64,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 0}}),
            new SheetImageDescription("ItemSprite", 11, 250, 16, 32,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 1}, {3, 1}, {4, 1}, {2, 0}, {3, 0}, {4, 0}, {5, 0}}),
            new SheetImageDescription("ItemBikeSprite", 117, 250, 32, 32,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 0}})
    );

    private static final int BACK_IMAGE_WIDTH = 8;
    private static final int BACK_IMAGE_HEIGHT = 8 * 5;

    private static final int SIT_FRAME_WIDTH = MEDIUM_SPRITE_WIDTH;
    private static final int SIT_FRAME_HEIGHT = MEDIUM_SPRITE_HEIGHT;
    private static final int SURF_BLOB_SPRITE_FRAME_NUM = 6;
    private static final int BIRD_SPRITE_FRAME_NUM = 3;

    private static final int ITEM_SPRITE_FRAME_NUM = 9;
    private static final int ITEM_BIKE_SPRITE_FRAME_NUM = 6;

    private final GBAImage item;
    private final GBAImage itemBike;

    public FRLGPlayerCharacterGraphics(GraphicsPackEntry entry) {
        super(entry);
        this.item = initSprite("ItemSprite", ITEM_SPRITE_FRAME_NUM, MEDIUM_SPRITE_WIDTH, MEDIUM_SPRITE_HEIGHT);
        this.itemBike = initSprite("ItemBikeSprite", ITEM_BIKE_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
    }

    @Override
    protected int getBackImageWidth() {
        return BACK_IMAGE_WIDTH;
    }

    @Override
    protected int getBackImageHeight() {
        return BACK_IMAGE_HEIGHT;
    }

    @Override
    protected GBAImage handleRSERunMode(GBAImage run) {
        if (run == null) {
            return null;
        }
        // this probably doesn't respect "palette tricks" since it comes down to a Graphics2D.draw call...
        run = new GBAImage.Builder(GFXFunctions.stitchToGrid(new BufferedImage[][]{{
                run.getSubimageFromFrame(0), run.getSubimageFromFrame(3), run.getSubimageFromFrame(4),
                run.getSubimageFromFrame(1), run.getSubimageFromFrame(5), run.getSubimageFromFrame(6),
                run.getSubimageFromFrame(2), run.getSubimageFromFrame(7), run.getSubimageFromFrame(8)
        }})).build();
        run.setFrameDimensions(MEDIUM_SPRITE_WIDTH, MEDIUM_SPRITE_HEIGHT);
        return run;
    }

    @Override
    protected GBAImage handleFRLGRunMode(GBAImage run) {
        return run;
    }

    @Override
    protected int getSitFrameWidth() {
        return SIT_FRAME_WIDTH;
    }

    @Override
    protected int getSitFrameHeight() {
        return SIT_FRAME_HEIGHT;
    }

    @Override
    protected int getSurfBlobFrameNum() {
        return SURF_BLOB_SPRITE_FRAME_NUM;
    }

    @Override
    protected int getBirdFrameNum() {
        return BIRD_SPRITE_FRAME_NUM;
    }

    @Override
    protected int getBirdFrameWidth() {
        return HUGE_SPRITE_WIDTH;
    }

    @Override
    protected int getBirdFrameHeight() {
        return HUGE_SPRITE_HEIGHT;
    }

    public boolean hasItemSprite() {
        return item != null;
    }

    public GBAImage getItemSprite() {
        return item;
    }

    public boolean hasItemBikeSprite() {
        return itemBike != null;
    }

    public GBAImage getItemBikeSprite() {
        return itemBike;
    }

    // TODO: the oak speech image and its 32-color palette

    @Override
    protected List<SheetImageDescription> getSheetImageDescriptions() {
        List<SheetImageDescription> sids = new ArrayList<>(super.getSheetImageDescriptions());
        sids.addAll(SHEET_IMAGE_DESCRIPTIONS);
        return sids;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (obj instanceof FRLGPlayerCharacterGraphics) {
            FRLGPlayerCharacterGraphics other = (FRLGPlayerCharacterGraphics) obj;
            return Objects.equals(item, other.item) && Objects.equals(itemBike, other.itemBike);
        }
        return false;
    }
}
