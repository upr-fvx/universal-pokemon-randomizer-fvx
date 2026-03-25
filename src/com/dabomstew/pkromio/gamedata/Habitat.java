package com.dabomstew.pkromio.gamedata;

/**
 * Represents a Pokémon's habitat as defined in the FRLG Pokédex.
 * Extended to cover all generations for use in habitat-based wild encounter randomization.
 * <p>
 * The nine habitats are taken directly from FireRed/LeafGreen's Pokédex habitat categories.
 * For Pokémon introduced in generations beyond Gen III, habitats are assigned based on
 * lore, typing, Pokédex entries, and design intent to maintain consistency with the
 * original classification system.
 */
public enum Habitat {
    GRASSLAND("Grassland"),
    FOREST("Forest"),
    WATERS_EDGE("Water's-edge"),
    SEA("Sea"),
    CAVE("Cave"),
    MOUNTAIN("Mountain"),
    ROUGH_TERRAIN("Rough-terrain"),
    URBAN("Urban"),
    RARE("Rare");

    private final String displayName;

    Habitat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Parses a habitat from its display name (case-insensitive).
     * @param name The display name to parse.
     * @return The matching Habitat, or null if no match was found.
     */
    public static Habitat fromDisplayName(String name) {
        if (name == null) return null;
        for (Habitat h : values()) {
            if (h.displayName.equalsIgnoreCase(name.trim())) {
                return h;
            }
        }
        return null;
    }
}