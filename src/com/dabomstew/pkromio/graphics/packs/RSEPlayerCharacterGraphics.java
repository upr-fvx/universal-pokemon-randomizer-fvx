package com.dabomstew.pkromio.graphics.packs;

import com.dabomstew.pkromio.GFXFunctions;
import com.dabomstew.pkromio.graphics.images.GBAImage;
import com.dabomstew.pkromio.graphics.palettes.Palette;

import java.awt.image.BufferedImage;

public class RSEPlayerCharacterGraphics extends Gen3PlayerCharacterGraphics {

    private static final int BACK_IMAGE_WIDTH = 8;
    private static final int BACK_IMAGE_HEIGHT = 8 * 4;

    private static final int SIT_FRAME_WIDTH = BIG_SPRITE_WIDTH;
    private static final int SIT_FRAME_HEIGHT = BIG_SPRITE_HEIGHT;
    private static final int SURF_BLOB_SPRITE_FRAME_NUM = 3;
    private static final int BIRD_SPRITE_FRAME_NUM = 1;

    private final static int SIT_JUMP_SPRITE_FRAME_NUM = 3;
    private static final int ACRO_BIKE_SPRITE_FRAME_NUM = 27;
    private static final int UNDERWATER_SPRITE_FRAME_NUM = 3; // ignore the unused 4th frame
    private static final int WATERING_CAN_SPRITE_FRAME_NUM = 6;
    private static final int DECORATE_SPRITE_FRAME_NUM = 1;
    private static final int FIELD_MOVE_SPRITE_FRAME_NUM = 5;

    private final GBAImage sitJump;
    private final GBAImage acroBike;
    private final GBAImage underwater;
    private final GBAImage wateringCan;
    private final GBAImage decorate;
    private final GBAImage fieldMove;

    public RSEPlayerCharacterGraphics(GraphicsPackEntry entry) {
        super(entry);
        this.sitJump = initSprite("SitJumpSprite", SIT_JUMP_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.acroBike = initSprite("AcroBikeSprite", ACRO_BIKE_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.underwater = initSprite("UnderwaterSprite", UNDERWATER_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.wateringCan = initSprite("WateringCanSprite", WATERING_CAN_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.decorate = initSprite("DecorateSprite", DECORATE_SPRITE_FRAME_NUM, MEDIUM_SPRITE_WIDTH, MEDIUM_SPRITE_HEIGHT);
        this.fieldMove = initSprite("FieldMoveSprite", FIELD_MOVE_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
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
        return run;
    }

    @Override
    protected GBAImage handleFRLGRunMode(GBAImage run) {
        if (run == null) {
            return null;
        }
        // this probably doesn't respect "palette tricks" since it comes down to a Graphics2D.draw call...
        run = new GBAImage.Builder(GFXFunctions.stitchToGrid(new BufferedImage[][]{{
                run.getSubimageFromFrame(0), run.getSubimageFromFrame(3), run.getSubimageFromFrame(6),
                run.getSubimageFromFrame(1), run.getSubimageFromFrame(2),
                run.getSubimageFromFrame(4), run.getSubimageFromFrame(5),
                run.getSubimageFromFrame(7), run.getSubimageFromFrame(8)
        }})).build();
        run.setFrameDimensions(MEDIUM_SPRITE_WIDTH, MEDIUM_SPRITE_HEIGHT);
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
        return BIG_SPRITE_WIDTH;
    }

    @Override
    protected int getBirdFrameHeight() {
        return BIG_SPRITE_HEIGHT;
    }

    public boolean hasSitJumpSprite() {
        return sitJump != null;
    }

    public GBAImage getSitJumpSprite() {
        return sitJump;
    }

    public boolean hasAcroBikeSprite() {
        return acroBike != null;
    }

    public GBAImage getAcroBikeSprite() {
        return acroBike;
    }

    public boolean hasUnderwaterSprite() {
        return underwater != null;
    }

    public GBAImage getUnderwaterSprite() {
        return underwater;
    }

    public Palette getUnderwaterPalette() {
        return underwater.getPalette();
    }

    public boolean hasWateringCanSprite() {
        return wateringCan != null;
    }

    public GBAImage getWateringCanSprite() {
        return wateringCan;
    }

    public boolean hasDecorateSprite() {
        return decorate != null;
    }

    public GBAImage getDecorateSprite() {
        return decorate;
    }

    public boolean hasFieldMoveSprite() {
        return fieldMove != null;
    }

    public GBAImage getFieldMoveSprite() {
        return fieldMove;
    }
}
