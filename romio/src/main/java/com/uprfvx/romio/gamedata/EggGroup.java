package com.uprfvx.romio.gamedata;

import java.util.List;

public enum EggGroup {
    // We use the official names.
    // The first few here have obvious names/use:
    MONSTER, BUG, FLYING, FIELD, FAIRY, GRASS, HUMAN_LIKE, MINERAL, AMORPHOUS, DRAGON,
    // The water groups are easy to confuse, but they are used for:
    WATER_1, // amphibious mons
    WATER_2, // fish
    WATER_3, // invertebrates
    // And finally there are egg groups with special functionality:
    DITTO, // breeds with everything
    UNDISCOVERED; // breeds with nothing

    // should be same in all vanilla games, afaik
    private static final List<EggGroup> IN_ORDER = List.of(
            MONSTER, WATER_1, BUG, FLYING, FIELD, FAIRY, GRASS, HUMAN_LIKE,
            WATER_3, MINERAL, AMORPHOUS, WATER_2, DITTO, DRAGON, UNDISCOVERED
    );

    public static EggGroup fromID(int id) {
        if (id < 1) {
            throw new IndexOutOfBoundsException("id must be 1-15");
        }
        return IN_ORDER.get(id - 1);
    }

    public int toID() {
        return IN_ORDER.indexOf(this) + 1;
    }
}
