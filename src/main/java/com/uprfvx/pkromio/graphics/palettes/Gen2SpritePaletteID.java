package com.uprfvx.pkromio.graphics.palettes;

/**
 * Names taken from
 * <a href=https://github.com/pret/pokecrystal/blob/aba1f140443ddb8589a23f877546963430e12167/constants/sprite_data_constants.asm>
 * pret/pokecrystal/constants/sprite_data_constants.asm</a>.<br>
 * "PINK" is unused in the vanilla games.
 */
public enum Gen2SpritePaletteID {
    RED  (new Palette(new int[]{0x00FFFFFF, 0xFFF89850, 0xFFF83808, 0xFF000000})),
    BLUE (new Palette(new int[]{0x00FFFFFF, 0xFFF89850, 0xFF5048F8, 0xFF000000})),
    GREEN(new Palette(new int[]{0x00FFFFFF, 0xFFF89850, 0xFF35B721, 0xFF000000})),
    BROWN(new Palette(new int[]{0x00FFFFFF, 0xFFF89850, 0xFF785018, 0xFF000000})),
    PINK (new Palette(new int[]{0x00FFFFFF, 0xFFF89850, 0xFFF05030, 0xFF000000})),
    EMOTE(new Palette(new int[]{0x00FFFFFF, 0xFFF8F8F8, 0xFF686868, 0xFF000000})),
    TREE (new Palette(new int[]{0x00FFFFFF, 0xFF60C808, 0xFF287000, 0xFF383838})),
    ROCK (new Palette(new int[]{0x00FFFFFF, 0xFFC09038, 0xFFA87830, 0xFF383838}));

    /**
     * Returns the {@link Gen2SpritePaletteID} that matches "close enough",
     * to the given {@link Palette}. If none match close enough, returns null.
     */
    public static Gen2SpritePaletteID getMatching(Palette palette) {
        if (palette.size() != 4) {
            return null;
        }
        for (Gen2SpritePaletteID pid : values()) {
            boolean matching = true;
            for (int col = 1; col < 4; col++) {
                for (int comp = 0; comp < 3; comp++) {
                    if (palette.get(col).getComp(comp) - pid.palette.get(col).getComp(comp) >= 8) {
                        matching = false;
                    }
                }
            }
            if (matching) {
                return pid;
            }
        }
        return null;
    }

    private final Palette palette;

    Gen2SpritePaletteID(Palette palette) {
        this.palette = palette;
    }

    public Palette getPalette() {
        return palette;
    }

}
