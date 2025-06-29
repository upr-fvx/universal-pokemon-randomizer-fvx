package com.dabomstew.pkromio.graphics.packs;

import com.dabomstew.pkromio.GFXFunctions;
import com.dabomstew.pkromio.graphics.images.GBCImage;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public abstract class GBCPlayerCharacterGraphics extends GraphicsPack {

    private static final int FRONT_IMAGE_DIMENSIONS = 7;
    private static final int OVERWORLD_SPRITE_TILE_AMOUNT = 4 * 3 * 2; // 4 tiles shown at once, 3 directions, 2 frames

    private final GBCImage front;
    private final GBCImage back;
    private final GBCImage walk;
    private final GBCImage bike;
    private final GBCImage fish;

    public GBCPlayerCharacterGraphics(GraphicsPackEntry entry) {
        super(entry);
        this.front = initFront();
        this.back = initBack();
        this.walk = initWalk();
        this.bike = initBike();
        this.fish = initFish();
    }

    private GBCImage initFront() {
        BufferedImage base = readImage("FrontImage");
        if (base == null) {
            return null;
        }
        GBCImage front = new GBCImage.Builder(base).columnMode(true).build();
        if (front.getWidthInTiles() != FRONT_IMAGE_DIMENSIONS || front.getHeightInTiles() != FRONT_IMAGE_DIMENSIONS) {
            System.out.println(getName() + ": Invalid front image dimensions");
            return null;
        }
        return front;
    }

    private GBCImage initBack() {
        BufferedImage base = readImage("BackImage");
        if (base == null) {
            return null;
        }
        GBCImage back = new GBCImage.Builder(base).columnMode(true).build();
        if (back.getWidthInTiles() != getBackImageDimensions() || back.getHeightInTiles() != getBackImageDimensions()) {
            System.out.println(getName() + ": Invalid back image dimensions");
            return null;
        }
        return back;
    }

    protected abstract int getBackImageDimensions();

    private GBCImage initWalk() {
        BufferedImage base = readImage("WalkSprite");
        if (base == null) {
            return null;
        }
        GBCImage walk = new GBCImage.Builder(base).build();
        if (walk.getWidthInTiles() * walk.getHeightInTiles() != OVERWORLD_SPRITE_TILE_AMOUNT) {
            System.out.println(getName() + ": Invalid walk sprite dimensions");
            return null;
        }
        return walk;
    }

    private GBCImage initBike() {
        BufferedImage base = readImage("BikeSprite");
        if (base == null) {
            return null;
        }
        GBCImage bike = new GBCImage.Builder(base).build();
        if (bike.getWidthInTiles() * bike.getHeightInTiles() != OVERWORLD_SPRITE_TILE_AMOUNT) {
            System.out.println(getName() + ": Invalid bike sprite dimensions");
            return null;
        }
        return bike;
    }

    private GBCImage initFish() {
        String fishSpriteMode = getEntry().getStringValue("FishSpriteMode");
        GBCImage fish = null;
        if (fishSpriteMode.equalsIgnoreCase("combined")) {
            fish = initFishFromCombined();
        } else if (fishSpriteMode.equalsIgnoreCase("separate")) {
            fish = initFishFromSeparate();
        } else {
            System.out.println(getName() + ": Invalid fish sprite mode");
        }

        if (fish == null && hasWalkSprite()) {
            fish = initFishFromWalkSprite();
        }
        return fish;
    }

    private GBCImage initFishFromCombined() {
        BufferedImage base = readImage("FishSprite");
        if (base == null) {
            return null;
        }
        GBCImage fish = new GBCImage.Builder(base).build();
        if (fish.getWidthInTiles() * fish.getHeightInTiles() != (OVERWORLD_SPRITE_TILE_AMOUNT) / 4) {
            System.out.println(getName() + ": Invalid fish sprite dimensions");
            return null;
        }
        return fish;
    }

    private GBCImage initFishFromSeparate() {
        // assumes front, back, side uses the same palette
        BufferedImage front = readImage("FishFrontSprite");
        BufferedImage back = readImage("FishBackSprite");
        BufferedImage side = readImage("FishSideSprite");
        BufferedImage stitched = GFXFunctions.stitchToGrid(new BufferedImage[][]{{front, back, side}});
        GBCImage fish = new GBCImage.Builder(stitched).build();
        if (fish.getWidthInTiles() * fish.getHeightInTiles() != (OVERWORLD_SPRITE_TILE_AMOUNT) / 4) {
            System.out.println(getName() + ": Invalid fish sprite dimensions");
            return null;
        }
        return fish;
    }

    private GBCImage initFishFromWalkSprite() {
        System.out.println("initFishFromWalkSprite");
        GBCImage walk = getWalkSprite();
        GBCImage front = walk.getSubimageFromTileRange(2, 4);
        GBCImage back = walk.getSubimageFromTileRange(6, 8);
        GBCImage side = walk.getSubimageFromTileRange(10, 12);
        BufferedImage stitched = GFXFunctions.stitchToGrid(new BufferedImage[][]{{front, back, side}});
        return new GBCImage.Builder(stitched).build();
    }

    public boolean hasFrontImage() {
        return front != null;
    }

    public GBCImage getFrontImage() {
        return front;
    }

    public boolean hasBackImage() {
        return back != null;
    }

    public GBCImage getBackImage() {
        return back;
    }

    public boolean hasWalkSprite() {
        return walk != null;
    }

    public GBCImage getWalkSprite() {
        return walk;
    }

    public boolean hasBikeSprite() {
        return bike != null;
    }

    public GBCImage getBikeSprite() {
        return bike;
    }

    public boolean hasFishSprite() {
        return fish != null;
    }

    public GBCImage getFishSprite() {
        return fish;
    }

    private BufferedImage getFishSpriteForSample() {
        GBCImage walk = getWalkSprite();
        GBCImage fish = getFishSprite();
        GBCImage walkFront = walk.getSubimageFromTileRange(0, 2);
        GBCImage walkBack = walk.getSubimageFromTileRange(4, 6);
        GBCImage walkSide = walk.getSubimageFromTileRange(8, 10);
        GBCImage fishFront = fish.getSubimageFromTileRange(0, 2);
        GBCImage fishBack = fish.getSubimageFromTileRange(2, 4);
        GBCImage fishSide = fish.getSubimageFromTileRange(4, 6);
        return GFXFunctions.stitchToGrid(new BufferedImage[][]{
                {walkFront, fishFront, walkBack, fishBack, walkSide, fishSide}});
    }

    @Override
    public List<BufferedImage> getSampleImages() {
        return Arrays.asList(getFrontImage(), getBackImage(), getWalkSprite(), getBikeSprite(), getFishSpriteForSample());
    }
}
