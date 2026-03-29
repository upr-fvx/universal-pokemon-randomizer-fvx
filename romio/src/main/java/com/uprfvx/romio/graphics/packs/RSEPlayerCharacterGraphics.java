package com.uprfvx.romio.graphics.packs;

import com.uprfvx.romio.GFXFunctions;
import com.uprfvx.romio.graphics.images.GBAImage;
import com.uprfvx.romio.graphics.palettes.Palette;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RSEPlayerCharacterGraphics extends Gen3PlayerCharacterGraphics {

    private static final List<SheetImageDescription> SHEET_IMAGE_DESCRIPTIONS = Arrays.asList(
            new SheetImageDescription("BackImage", 90, 53, 64, 64,
                    1, new int[][]{{1, 0}, {2, 0}, {3, 0}, {0, 0}}),
            new SheetImageDescription("SitSprite", 81, 360, 32, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}}),
            new SheetImageDescription("SurfBlobSprite", 155, 360, 32, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}}),
            new SheetImageDescription("BirdSprite", 192, 426, 32, 32),
            new SheetImageDescription("SitJumpSprite", 118, 360, 32, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}}),
            new SheetImageDescription("AcroBikeSprite", 11, 250, 32, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}, {1, 0}, {2, 0}, {1, 1}, {2, 1}, {1, 2}, {2, 2},
                                          {3, 0}, {4, 0}, {5, 0}, {6, 0}, // wheelies
                                          {3, 1}, {4, 1}, {5, 1}, {6, 1},
                                          {3, 2}, {4, 2}, {5, 2}, {6, 2},
                                          {7, 0}, {8, 0}, {7, 1}, {8, 1}, {7, 2}, {8, 2}}), // balancing
            new SheetImageDescription("UnderwaterSprite", 239, 404, 32, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}}),
            new SheetImageDescription("WateringCanSprite", 11, 360, 32, 32,
                    1, new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}, {0, 2}, {1, 2}}),
            new SheetImageDescription("DecorateSprite", 180, 470, 16, 32),
            new SheetImageDescription("FieldMoveSprite", 11, 470, 32, 32 ,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}})
    );

    private static final List<SheetPaletteDescription> SHEET_PALETTE_DESCRIPTIONS = Arrays.asList(
            new SheetPaletteDescription("BackImagePalette", 317, 49),
            new SheetPaletteDescription("SpriteUnderwaterPalette", 239, 385)
    );

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
    private final Palette underwaterPalette;

    public RSEPlayerCharacterGraphics(GraphicsPackEntry entry) {
        super(entry);
        this.sitJump = initSprite("SitJumpSprite", SIT_JUMP_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.acroBike = initSprite("AcroBikeSprite", ACRO_BIKE_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.underwater = initSprite("UnderwaterSprite", "SpriteUnderwaterPalette", UNDERWATER_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.wateringCan = initSprite("WateringCanSprite", WATERING_CAN_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.decorate = initSprite("DecorateSprite", DECORATE_SPRITE_FRAME_NUM, MEDIUM_SPRITE_WIDTH, MEDIUM_SPRITE_HEIGHT);
        this.fieldMove = initSprite("FieldMoveSprite", FIELD_MOVE_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.underwaterPalette = initUnderwaterSpritePalette();
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

    private Palette initUnderwaterSpritePalette() {
        Palette palette = readPalette("SpriteUnderwaterPalette");
        if (palette == null && hasUnderwaterSprite()) {
            palette = underwater.getPalette();
        }
        return palette;
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
        return underwaterPalette;
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

    @Override
    protected List<SheetImageDescription> getSheetImageDescriptions() {
        List<SheetImageDescription> descs = new ArrayList<>(super.getSheetImageDescriptions());
        descs.addAll(SHEET_IMAGE_DESCRIPTIONS);
        return descs;
    }

    @Override
    protected List<SheetPaletteDescription> getSheetPaletteDescriptions() {
        List<SheetPaletteDescription> descs = new ArrayList<>(super.getSheetPaletteDescriptions());
        descs.addAll(SHEET_PALETTE_DESCRIPTIONS);
        return descs;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (obj instanceof RSEPlayerCharacterGraphics) {
            RSEPlayerCharacterGraphics other = (RSEPlayerCharacterGraphics) obj;
            return Objects.equals(sitJump, other.sitJump) && Objects.equals(acroBike, other.acroBike)
                    && Objects.equals(underwater, other.underwater) && Objects.equals(wateringCan, other.wateringCan)
                    && Objects.equals(decorate, other.decorate)
                    && Objects.equals(getUnderwaterPalette(), other.getUnderwaterPalette());
        }
        return false;
    }

}
