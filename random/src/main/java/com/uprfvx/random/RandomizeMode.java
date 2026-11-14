package com.uprfvx.random;

/**
 * Represents the "mode" used when randomizing a game.
 */
public enum RandomizeMode {

    /**
     * The mode for normal use. Random seed, freely chosen settings, etc...
     */
    NORMAL,

    /**
     * The mode for generating games to race in. Mostly like {@link #NORMAL}, but preset files created using this
     * remember that they are meant to be used for racing.
     */
    CREATE_RACE,

    /**
     * The mode for creating multiple randomized games at once.
     */
    BATCH,

    /**
     * The mode for loading a preset, with seed and settings already defined.
     */
    LOAD_PRESET
}
