package com.dabomstew.pkromio.graphics.packs;

import com.dabomstew.pkromio.graphics.images.GBAImage;
import com.dabomstew.pkromio.graphics.palettes.Palette;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Gen3PlayerCharacterGraphics extends GraphicsPack {

    private static final List<SheetImageDescription> SHEET_IMAGE_DESCRIPTIONS = Arrays.asList(
            new SheetImageDescription("FrontImage", 11, 53, 64, 64),
            new SheetImageDescription("BackImage", 90, 53, 64, 64,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}}),
            new SheetImageDescription("MapIcon", 11, 15, 16, 16),
            new SheetImageDescription("WalkSprite", 11, 139, 16, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}, {1, 0}, {2, 0}, {1, 1}, {2, 1}, {2, 1}, {2, 2}}),
            new SheetImageDescription("RunSprite", 66, 139, 16, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}, {1, 0}, {2, 0}, {1, 1}, {2, 1}, {2, 1}, {2, 2}}),
            new SheetImageDescription("BikeSprite", 121, 139, 32, 32,
                    1, new int[][]{{0, 0}, {0, 1}, {0, 2}, {1, 0}, {2, 0}, {1, 1}, {2, 1}, {2, 1}, {2, 2}}),
            new SheetImageDescription("FishSprite", 12, 104, 32, 32,
                    1, new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}, {0, 1}, {1, 1}, {2, 1}, {3, 1},
                                          {0, 2}, {1, 2}, {2, 2}, {3, 2}})
    );

    private static final List<SheetPaletteDescription> SHEET_PALETTE_DESCRIPTIONS = Arrays.asList(
            new SheetPaletteDescription("FrontImagePalette", 43, 49),
            new SheetPaletteDescription("MapIconPalette", 11, 11),
            new SheetPaletteDescription("SpriteNormalPalette", 323, 135)
            // TODO: include reflection palette in sheet
    );

    private final static int FRONT_IMAGE_DIMENSIONS = 8;
    private final static int MAP_ICON_DIMENSIONS = 2;

    public final static int MEDIUM_SPRITE_WIDTH = 2;
    public final static int MEDIUM_SPRITE_HEIGHT = 4;
    public final static int BIG_SPRITE_WIDTH = 4;
    public final static int BIG_SPRITE_HEIGHT = 4;
    public final static int HUGE_SPRITE_WIDTH = 8;
    public final static int HUGE_SPRITE_HEIGHT = 8;

    public final static int WALK_SPRITE_FRAME_NUM = 3 * 3;
    public final static int RUN_SPRITE_FRAME_NUM = WALK_SPRITE_FRAME_NUM;
    public final static int BIKE_SPRITE_FRAME_NUM = 3 * 3;
    public final static int FISH_SPRITE_FRAME_NUM = 3 * 4;
    public final static int SIT_SPRITE_FRAME_NUM = 3;

    private final static int PALETTE_SIZE = 16;

    private final GBAImage front;
    private final GBAImage back;
    private final GBAImage walk;
    private final GBAImage run;
    private final GBAImage bike; // mach bike
    private final GBAImage fish;
    private final GBAImage sit;
    private final GBAImage surfBlob;
    private final GBAImage bird;
    private final GBAImage mapIcon;

    private final Palette normalSpritePalette;
    private final Palette reflectionSpritePalette;

    public Gen3PlayerCharacterGraphics(GraphicsPackEntry entry) {
        super(entry);
        if (usesSheet()) {
            entry.putStringValue("RunSpriteMode", "rse");
        }
        this.front = initImage("FrontImage", FRONT_IMAGE_DIMENSIONS, FRONT_IMAGE_DIMENSIONS);
        this.back = initImage("BackImage", getBackImageWidth(), getBackImageHeight());
        this.walk = initSprite("WalkSprite", WALK_SPRITE_FRAME_NUM, MEDIUM_SPRITE_WIDTH, MEDIUM_SPRITE_HEIGHT);
        this.run = initRun();
        this.bike = initSprite("BikeSprite", BIKE_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.fish = initSprite("FishSprite", FISH_SPRITE_FRAME_NUM, BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.sit = initSprite("SitSprite", SIT_SPRITE_FRAME_NUM, getSitFrameWidth(), getSitFrameHeight());
        this.surfBlob = initSprite("SurfBlobSprite", getSurfBlobFrameNum(), BIG_SPRITE_WIDTH, BIG_SPRITE_HEIGHT);
        this.bird = initSprite("BirdSprite", getBirdFrameNum(), getBirdFrameWidth(), getBirdFrameHeight());
        this.mapIcon = initImage("MapIcon", MAP_ICON_DIMENSIONS, MAP_ICON_DIMENSIONS);
        this.normalSpritePalette = initNormalSpritePalette();
        this.reflectionSpritePalette = initReflectionSpritePalette();
    }

    protected abstract int getBackImageWidth();

    protected abstract int getBackImageHeight();

    private GBAImage initRun() {
        String mode = getEntry().getStringValue("RunSpriteMode");

        GBAImage run = initSprite("RunSprite", RUN_SPRITE_FRAME_NUM, MEDIUM_SPRITE_WIDTH, MEDIUM_SPRITE_HEIGHT);
        if (run == null) {
            run = walk;
            mode = "RSE";
        }

        if (mode.equalsIgnoreCase("rse")) {
            run = handleRSERunMode(run);
        } else if (mode.equalsIgnoreCase("frlg")) {
            run = handleFRLGRunMode(run);
        } else {
            System.out.println("Invalid run sprite mode");
        }

        return run;
    }

    protected abstract GBAImage handleRSERunMode(GBAImage run);

    protected abstract GBAImage handleFRLGRunMode(GBAImage run);

    protected abstract int getSitFrameWidth();

    protected abstract int getSitFrameHeight();

    protected abstract int getSurfBlobFrameNum();

    protected abstract int getBirdFrameNum();

    protected abstract int getBirdFrameWidth();

    protected abstract int getBirdFrameHeight();

    private Palette initNormalSpritePalette() {
        Palette palette = readPalette("SpriteNormalPalette");
        if (palette == null && hasWalkSprite()) {
            palette = walk.getPalette();
        }
        return palette;
    }

    private Palette initReflectionSpritePalette() {
        Palette palette = readPalette("SpriteReflectionPalette");
        if (palette == null) {
            palette = normalSpritePalette; // TODO: auto-soften the palette
        }
        return palette;
    }

    protected GBAImage initImage(String key, int width, int height) {
        BufferedImage base = readImage(key);
        if (base == null) {
            return null;
        }
        Palette palette = readPalette(key + "Palette");

        GBAImage.Builder builder = new GBAImage.Builder(base);
        if (palette != null) {
            builder.prepreparedPalette(palette);
        }
        GBAImage image = builder.build();

        if (image.getWidthInTiles() != width || image.getHeightInTiles() != height) {
            System.out.println("Invalid " + key + " dimensions. Expected " + width + "x" + height + ", was " +
                    image.getWidthInTiles() + "x" + image.getHeightInTiles());
            return null;
        }
        return image;
    }

    protected GBAImage initSprite(String key, int frameAmount, int frameWidth, int frameHeight) {
        GBAImage image = initSprite(key, frameAmount * frameHeight * frameWidth);
        if (image != null) {
            image.setFrameDimensions(frameWidth, frameHeight);
        }
        return image;
    }

    protected GBAImage initSprite(String key, int tileAmount) {
        BufferedImage base = readImage(key);
        if (base == null) {
            return null;
        }        
        Palette palette = readPalette("NormalSpritePalette");

        GBAImage.Builder builder = new GBAImage.Builder(base);
        if (palette != null) {
            builder.prepreparedPalette(palette);
        }
        GBAImage sprite = builder.build();

        if (sprite.getWidthInTiles() * sprite.getHeightInTiles() != tileAmount) {
            System.out.println("Invalid " + key + " dimensions");
            return null;
        }
        return sprite;
    }

    public boolean hasFrontImage() {
        return front != null;
    }

    public GBAImage getFrontImage() {
        return front;
    }

    public boolean hasBackImage() {
        return back != null;
    }

    public GBAImage getBackImage() {
        return back;
    }

    public boolean hasWalkSprite() {
        return walk != null;
    }

    public GBAImage getWalkSprite() {
        return walk;
    }

    public GBAImage getRunSprite() {
        return run;
    }

    public boolean hasBikeSprite() {
        return bike != null;
    }

    public GBAImage getBikeSprite() {
        return bike;
    }

    public boolean hasFishSprite() {
        return fish != null;
    }

    public GBAImage getFishSprite() {
        return fish;
    }

    public boolean hasSitSprite() {
        return sit != null;
    }

    public GBAImage getSitSprite() {
        return sit;
    }

    public boolean hasSurfBlobSprite() {
        return surfBlob != null;
    }

    public GBAImage getSurfBlobSprite() {
        return surfBlob;
    }

    public boolean hasBirdSprite() {
        return bird != null;
    }

    public GBAImage getBirdSprite() {
        return bird;
    }

    public boolean hasMapIcon() {
        return mapIcon != null;
    }

    public GBAImage getMapIcon() {
        return mapIcon;
    }

    /**
     * If there is a normal sprite palette, there is also one for reflections.
     */
    public boolean hasSpritePalettes() {
        return normalSpritePalette != null;
    }

    public Palette getNormalSpritePalette() {
        return normalSpritePalette;
    }

    public Palette getReflectionSpritePalette() {
        return reflectionSpritePalette;
    }

    private BufferedImage getBackImageSpriteForSample() {
        return back == null ? null : back.getSubimageFromFrame(0, getBackImageWidth(), getBackImageWidth());
    }

    private BufferedImage getWalkSpriteForSample() {
        return walk == null ? null : walk.getSubimageFromFrame(0, MEDIUM_SPRITE_WIDTH, MEDIUM_SPRITE_HEIGHT);
    }

    private GBAImage toSample(BufferedImage bim) {
        return new GBAImage.Builder(bim).transparent(true).build();
    }

    @Override
    public List<BufferedImage> getSampleImages() {
        return Arrays.asList(toSample(getFrontImage()),
                toSample(getBackImageSpriteForSample()),
                toSample(getWalkSpriteForSample()));
    }

    @Override
    protected Palette readPalette(String key) {
        Palette palette = super.readPalette(key);
        if (palette == null) {
            return null;
        }
        if (palette.size() != PALETTE_SIZE) {
            System.out.println("Invalid palette size");
            return null;
        }
        return palette;
    }

    @Override
    protected List<SheetImageDescription> getSheetImageDescriptions() {
        return SHEET_IMAGE_DESCRIPTIONS;
    }

    @Override
    protected List<SheetPaletteDescription> getSheetPaletteDescriptions() {
        return SHEET_PALETTE_DESCRIPTIONS;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Gen3PlayerCharacterGraphics) {
            Gen3PlayerCharacterGraphics other = (Gen3PlayerCharacterGraphics) obj;
            System.out.println(Objects.equals(front, other.front) + " " + Objects.equals(back, other.back)
                    + " " + Objects.equals(walk, other.walk) + " " + Objects.equals(bike, other.bike)
                    + " " + Objects.equals(fish, other.fish) + " " + Objects.equals(sit, other.sit)
                    + " " + Objects.equals(surfBlob, other.surfBlob) + " " + Objects.equals(bird, other.bird)
                    + " " + Objects.equals(normalSpritePalette, other.normalSpritePalette)
                    + " " + Objects.equals(reflectionSpritePalette, other.reflectionSpritePalette));
            return Objects.equals(front, other.front) && Objects.equals(back, other.back)
                    && Objects.equals(walk, other.walk) && Objects.equals(bike, other.bike)
                    && Objects.equals(fish, other.fish) && Objects.equals(sit, other.sit)
                    && Objects.equals(surfBlob, other.surfBlob) && Objects.equals(bird, other.bird)
                    && Objects.equals(normalSpritePalette, other.normalSpritePalette)
                    && Objects.equals(reflectionSpritePalette, other.reflectionSpritePalette);
        }
        return false;
    }

}
