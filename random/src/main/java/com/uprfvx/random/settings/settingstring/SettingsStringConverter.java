package com.uprfvx.random.settings.settingstring;

// TODO: what is the version?

import com.uprfvx.random.Version;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.SettingsUpdater;
import filefunctions.IOFunctions;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Used to convert settings from the "settings string" format
 * used in FVX v1.6.0 into the modern format used in FVX v[VERSION].
 * <br><br>
 * This class only converts from FVX v1.6.0 into FVX v[VERSION].<br>
 * To convert an older settings string into the format of FVX v1.6.0,
 * use {@link SettingsStringUpdater}.<br>
 * To convert FVX v[VERSION] settings into those of the current version,
 * use {@link SettingsUpdater}.
 * <br><br>
 * The "settings string" format is an old format the Randomizer used
 * to use for settings, a binary format that could be written as a
 * (relatively) short base64 string. It had the advantage of being
 * very brief, but was opaque and unwieldy to work with.
 */
public class SettingsStringConverter {

    private static final int VERSION_ID_LENGTH = 3;

    private static boolean restoreState(byte b, int index) {
        if (index >= 8) {
            throw new IllegalArgumentException("Can't read more than 8 bits from a byte!");
        }

        int value = b & 0xFF;
        return ((value >> index) & 0x01) == 0x01;
    }

    /**
     * Takes a {@link SettingsManager} and a FVX v1.6.0 settings string (including version),
     * and converts the settings stored in the latter to populate the former.<br>
     * <b>NOTE:</b> this converts the settings into that of FVX v[VERSION]. Use {@link SettingsUpdater}
     * to get settings that match the current version.
     * @throws NullPointerException if <code>manager</code> or <code>stringWithVersion</code> are null.
     * @throws IllegalArgumentException if the settings string is not of FVX v1.6.0, or is otherwise invalid.
     */
    public void convertAndPopulate(SettingsManager manager, String stringWithVersion) {
        if (manager == null) {
            throw new NullPointerException("manager cannot be null.");
        }

        checkValidStringVersion(stringWithVersion);

        String withoutVersion = stringWithVersion.substring(VERSION_ID_LENGTH);
        byte[] data = Base64.getDecoder().decode(withoutVersion);

        convertAndPopulateFromData(manager, data);
    }

    private void checkValidStringVersion(String stringWithVersion) {
        if (stringWithVersion == null) {
            throw new NullPointerException("stringWithVersion cannot be null.");
        }
        String versionChars = stringWithVersion.substring(0, VERSION_ID_LENGTH);
        int versionID;
        try {
            versionID = Integer.parseInt(versionChars);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid version id: " + versionChars);
        }
        if (versionID >= Version.FVX_1_6_0.id) {
            throw new IllegalArgumentException("Version id does not match that of FVX v1.6.0\n." +
                    "\tExpected=" + Version.FVX_1_6_0.id + ", Was=" + versionID);
        }
    }

    // m and d are short so they take less space in the function calls
    private void convertAndPopulateFromData(SettingsManager m, byte[] d) {

        // Byte 0: Misc / Species Evolutions
        loadBoolean(m, d, 0, 0, Settings.Name.SPECIES_EVOLUTIONS_MAKE_POSSIBLE);
        loadBoolean(m, d, 0, 1, Settings.Name.UPDATE_MOVES);
        // (0, 2) unused
        loadBoolean(m, d, 0, 3, Settings.Name.TRAINERS_RANDOMIZE_NAMES);
        loadBoolean(m, d, 0, 4, Settings.Name.TRAINERS_RANDOMIZE_CLASS_NAMES);
        loadBoolean(m, d, 0, 5, Settings.Name.SPECIES_EVOLUTIONS_MAKE_EASIER);
        loadBoolean(m, d, 0, 6, Settings.Name.SPECIES_EVOLUTIONS_REMOVE_TIME_BASED);
        loadBoolean(m, d, 0, 7, Settings.Name.SPECIES_EVOLUTIONS_CHANGES_USE_ESTIMATED_LEVELS);


        // Byte 1: Species Base Stats
        loadBoolean(m, d, 1, 0, Settings.Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS);
        loadEnum(m, d, 1, Settings.Name.RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS,
                Map.of(
                        1, Settings.BaseStatDistributionsMod.RANDOM,
                        2, Settings.BaseStatDistributionsMod.SHUFFLE,
                        3, Settings.BaseStatDistributionsMod.UNCHANGED
                ));
        loadBoolean(m, d, 1, 4, Settings.Name.STANDARDIZE_SPECIES_EXP_CURVES);
        loadBoolean(m, d, 1, 5, Settings.Name.UPDATE_SPECIES_BASE_STATS);
        loadBoolean(m, d, 1, 6, Settings.Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_MEGA_EVOLUTIONS);
        loadBoolean(m, d, 1, 7, Settings.Name.SPECIES_STAT_DISTRIBUTIONS_ASSIGN_EVO_STATS_RANDOMLY);


        // Byte 2: Species Types
        loadEnum(m, d, 2, Settings.Name.RANDOMIZE_SPECIES_TYPES,
                Map.of(
                        0, Settings.SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS,
                        1, Settings.SpeciesTypesMod.COMPLETELY_RANDOM,
                        2, Settings.SpeciesTypesMod.UNCHANGED
                ));
        // (2, 3--5) unused
        loadBoolean(m, d, 2, 6, Settings.Name.SPECIES_TYPES_FOLLOW_MEGA_EVOLUTIONS);
        loadBoolean(m, d, 2, 7, Settings.Name.SPECIES_TYPES_FORCE_DUAL_TYPES);


        // Byte 3: Species Abilities
        loadEnum(m, d, 3, Settings.Name.RANDOMIZE_SPECIES_ABILITIES,
                Map.of(
                        0, Settings.AbilitiesMod.UNCHANGED,
                        1, Settings.AbilitiesMod.RANDOMIZE
                ));
        loadBoolean(m, d, 3, 2, Settings.Name.SPECIES_ABILITIES_BAN_WONDER_GUARD, true);
        loadBoolean(m, d, 3, 3, Settings.Name.SPECIES_ABILITIES_FOLLOW_EVOLUTIONS);
        loadBoolean(m, d, 3, 4, Settings.Name.SPECIES_ABILITIES_BAN_TRAPPING);
        loadBoolean(m, d, 3, 5, Settings.Name.SPECIES_ABILITIES_BAN_NEGATIVE);
        loadBoolean(m, d, 3, 6, Settings.Name.SPECIES_ABILITIES_BAN_MINOR);
        loadBoolean(m, d, 3, 7, Settings.Name.SPECIES_ABILITIES_FOLLOW_MEGA_EVOLUTIONS);


        // Byte 4: Starters
        loadEnum(m, d, 4, Settings.Name.RANDOMIZE_STARTERS,
                Map.of(
                        0, Settings.StartersMod.CUSTOM,
                        1, Settings.StartersMod.COMPLETELY_RANDOM,
                        2, Settings.StartersMod.UNCHANGED,
                        3, Settings.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS,
                        7, Settings.StartersMod.RANDOM_BASIC
                ));
        loadBoolean(m, d, 4, 4, Settings.Name.STARTERS_RANDOMIZE_HELD_ITEMS);
        loadBoolean(m, d, 4, 5, Settings.Name.STARTERS_BAN_BAD_HELD_ITEMS);
        loadBoolean(m, d, 4, 6, Settings.Name.STARTERS_ALLOW_ALT_FORMES);


        // Bytes 5-10: Custom Starters
        load2ByteInt(m, d, 5, Settings.Name.STARTER_CUSTOM_1);
        load2ByteInt(m, d, 7, Settings.Name.STARTER_CUSTOM_1);
        load2ByteInt(m, d, 9, Settings.Name.STARTER_CUSTOM_1);


        // Byte 11: Movesets
        loadEnum(m, d, 11, Settings.Name.RANDOMIZE_SPECIES_MOVESETS,
                Map.of(
                        0, Settings.MovesetsMod.COMPLETELY_RANDOM,
                        1, Settings.MovesetsMod.RANDOM_PREFER_SAME_TYPE,
                        2, Settings.MovesetsMod.UNCHANGED,
                        3, Settings.MovesetsMod.METRONOME_ONLY
                ));
        loadBoolean(m, d, 11, 5, Settings.Name.MOVESETS_ORDER_BY_DAMAGE);
        // TODO: guaranteed move count


        // Byte 12: Movesets Force Good Damaging
        loadNybble(m, d, 12, 0, Settings.Name.MOVESETS_FORCE_GOOD_DAMAGING_PERCENT);
        // (12, 7) is a boolean that can entirely disable the option
        if (restoreState(d[12], 7)) {
            m.setSetting(Settings.Name.MOVESETS_FORCE_GOOD_DAMAGING_PERCENT, 0);
        }


        // Byte 13: Trainer Pokemon
        loadEnum(m, d, 13, Settings.Name.RANDOMIZE_TRAINER_POKEMON,
                Map.of(
                        0, Settings.TrainersMod.UNCHANGED,
                        1, Settings.TrainersMod.RANDOM,
                        2, Settings.TrainersMod.DISTRIBUTED,
                        3, Settings.TrainersMod.MAINPLAYTHROUGH,
                        4, Settings.TrainersMod.TYPE_THEMED,
                        5, Settings.TrainersMod.TYPE_THEMED_ELITE4_GYMS,
                        6, Settings.TrainersMod.KEEP_THEMED,
                        7, Settings.TrainersMod.KEEP_THEME_OR_PRIMARY
                ));


        // Byte 14: Trainers Evolution Level Modifier
        // Shift from int8 range: [-128, 127] --> [-100, 155]
        loadByte(m, d, 14, Settings.Name.TRAINERS_EVOLVE_LEVEL_PERCENT_MODIFIER, 28);


        // Byte 15: Wild Pokémon
        loadBoolean(m, d, 15, 0, Settings.Name.RANDOMIZE_WILD_ENCOUNTERS, true);
        loadEnum(m, d, 15, Settings.Name.WILD_REPLACEMENT_ZONE,
                Map.of(
                        1, Settings.WildPokemonZoneMod.SINGLE_ENCOUNTER,
                        2, Settings.WildPokemonZoneMod.ENCOUNTER_SET,
                        3, Settings.WildPokemonZoneMod.GAME,
                        5, Settings.WildPokemonZoneMod.NAMED_LOCATION,
                        6, Settings.WildPokemonZoneMod.MAP
                ));
        loadBoolean(m, d, 15, 4, Settings.Name.WILD_EVOLUTION_KEEP_RELATIONS);
        loadBoolean(m, d, 15, 7, Settings.Name.WILD_SPLIT_REPLACEMENT_ZONE_BY_ENCOUNTER_TYPES);


        // Byte 16: Wild Pokémon (continued)
        // (16, 0) unused
        loadBoolean(m, d, 16, 1, Settings.Name.WILD_USE_SIMILAR_STRENGTH);
        loadBoolean(m, d, 16, 2, Settings.Name.WILD_CATCH_EM_ALL);
        // (16, 3--7) unused


        // Byte 17: Wild Pokémon (continued #2)
        loadEnum(m, d, 17, Settings.Name.WILD_TYPE_RESTRICTION,
                Map.of(
                        0, Settings.WildPokemonTypeMod.NONE,
                        1, Settings.WildPokemonTypeMod.KEEP_PRIMARY,
                        2, Settings.WildPokemonTypeMod.RANDOM_THEMES
                ));
        loadBoolean(m, d, 17, 3, Settings.Name.WILD_KEEP_TYPE_THEMES);
        loadEnum(m, d, 17, Settings.Name.WILD_EVOLUTION_RESTRICTION,
                Map.of(
                        4, Settings.WildPokemonEvolutionMod.NONE,
                        5, Settings.WildPokemonEvolutionMod.BASIC_ONLY,
                        6, Settings.WildPokemonEvolutionMod.KEEP_STAGE
                ));
        // (17, 7) unused


        // Byte 18: Wild Pokémon (continued #3)
        loadBoolean(m, d, 18, 0, Settings.Name.WILD_REMOVE_TIME_BASED, true);
        // TODO: minimum catch rate
        loadBoolean(m, d, 18, 2, Settings.Name.WILD_NO_LEGENDARIES);
        loadBoolean(m, d, 18, 3, Settings.Name.WILD_RANDOMIZE_HELD_ITEMS);
        loadBoolean(m, d, 18, 4, Settings.Name.WILD_HELD_ITEMS_BAN_MINOR);
        loadBoolean(m, d, 18, 5, Settings.Name.WILD_SIMILAR_STRENGTH_BALANCE_LOW_LEVEL);
        // (18, 6--7) unused


        // Byte 19: Static Pokémon
        loadEnum(m, d, 19, Settings.Name.RANDOMIZE_STATIC_ENCOUNTERS,
                Map.of(
                        0, Settings.StaticPokemonMod.UNCHANGED,
                        1, Settings.StaticPokemonMod.RANDOM_MATCHING,
                        2, Settings.StaticPokemonMod.COMPLETELY_RANDOM,
                        3, Settings.StaticPokemonMod.SIMILAR_STRENGTH
                ));
        loadBoolean(m, d, 19, 4, Settings.Name.STATICS_LIMIT_MAIN_GAME_LEGENDARIES);
        loadBoolean(m, d, 19, 5, Settings.Name.STATICS_FULL_RANDOM_OVER_600_BST);
        loadBoolean(m, d, 19, 6, Settings.Name.STATICS_ALLOW_ALT_FORMES);
        loadBoolean(m, d, 19, 7, Settings.Name.STATICS_SWAP_MEGA_EVOLVABLES);


        // Byte 20: TMs and HMs
        loadEnum(m, d, 20, Settings.Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                Map.of(
                        0, Settings.TMsHMsCompatibilityMod.COMPLETELY_RANDOM,
                        1, Settings.TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE,
                        2, Settings.TMsHMsCompatibilityMod.UNCHANGED,
                        7, Settings.TMsHMsCompatibilityMod.FULL
                ));
        loadEnum(m, d, 20, Settings.Name.RANDOMIZE_TM_MOVES,
                Map.of(
                        3, Settings.TMMovesMod.RANDOM,
                        4, Settings.TMMovesMod.UNCHANGED
                ));
        loadBoolean(m, d, 20, 5, Settings.Name.TM_COMPATABILITY_LEVEL_UP_SANITY);
        loadBoolean(m, d, 20, 6, Settings.Name.TMS_KEEP_FIELD_MOVES);


        // Byte 21: TM and Tutor compatibility
        loadBoolean(m, d, 21, 0, Settings.Name.TMS_FULL_HM_COMPATABILITY);
        loadBoolean(m, d, 21, 1, Settings.Name.TM_COMPATABILITY_FOLLOW_EVOLUTIONS);
        loadBoolean(m, d, 21, 2, Settings.Name.TUTOR_COMPATABILITY_FOLLOW_EVOLUTIONS);
        // (21, 3--7) unused


        // Byte 22: TM Force Good Damaging
        loadNybble(m, d, 22, 0, Settings.Name.TMS_GOOD_DAMAGING_PERCENT);
        // (22, 7) is a boolean that can entirely disable the option
        if (restoreState(d[22], 7)) {
            m.setSetting(Settings.Name.TMS_GOOD_DAMAGING_PERCENT, 0);
        }


        // Byte 23: Move Tutors
        loadEnum(m, d, 23, Settings.Name.RANDOMIZE_TUTOR_COMPATABILITY,
                Map.of(
                        0, Settings.MoveTutorsCompatibilityMod.COMPLETELY_RANDOM,
                        1, Settings.MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE,
                        2, Settings.MoveTutorsCompatibilityMod.UNCHANGED,
                        7, Settings.MoveTutorsCompatibilityMod.FULL
                ));
        loadEnum(m, d, 23, Settings.Name.RANDOMIZE_TUTOR_MOVES,
                Map.of(
                        3, Settings.MoveTutorMovesMod.RANDOM,
                        4, Settings.MoveTutorMovesMod.UNCHANGED
                ));
        loadBoolean(m, d, 23, 5, Settings.Name.TUTOR_COMPATABILITY_LEVEL_UP_SANITY);
        loadBoolean(m, d, 23, 6, Settings.Name.TUTORS_KEEP_FIELD_MOVES);


        // Byte 24: Tutor Force Good Damaging
        loadNybble(m, d, 24, 0, Settings.Name.TUTORS_GOOD_DAMAGING_PERCENT);
        // (24, 7) is a boolean that can entirely disable the option
        if (restoreState(d[24], 7)) {
            m.setSetting(Settings.Name.TUTORS_GOOD_DAMAGING_PERCENT, 0);
        }


        // Byte 25: In-game trades
        loadEnum(m, d, 25, Settings.Name.RANDOMIZE_IN_GAME_TRADES,
                Map.of(
                        0, Settings.InGameTradesMod.RANDOMIZE_GIVEN_AND_REQUESTED,
                        1, Settings.InGameTradesMod.RANDOMIZE_GIVEN,
                        6, Settings.InGameTradesMod.UNCHANGED
                ));
        loadBoolean(m, d, 25, 2, Settings.Name.TRADES_RANDOMIZE_HELD_ITEMS);
        loadBoolean(m, d, 25, 3, Settings.Name.TRADES_RANDOMIZE_IVS);
        loadBoolean(m, d, 25, 4, Settings.Name.TRADES_RANDOMIZE_NICKNAMES);
        loadBoolean(m, d, 25, 5, Settings.Name.TRADES_RANDOMIZE_ORIGINAL_TRAINERS);
        // (25, 7) unused

        // TODO: fill in rest (up to byte 68)
    }

    public void loadBoolean(SettingsManager manager, byte[] data, int byteNum, int bitNum, Settings.Name name) {
        manager.setSetting(name, restoreState(data[byteNum], bitNum));
    }

    public void loadBoolean(SettingsManager manager, byte[] data, int byteNum, int bitNum,
                            Settings.Name name, boolean invert) {
        boolean state = restoreState(data[byteNum], bitNum);
        if (invert) {state = !state;}
        manager.setSetting(name, state);
    }

    public void loadNybble(SettingsManager manager, byte[] data, int byteNum, int bitNum, Settings.Name name) {
        int fullByte = data[byteNum] & 0xFF;
        int nybble = fullByte >> bitNum & 0x0F;
        manager.setSetting(name, nybble);
    }

    /**
     * Loads a signed byte, shifted by a constant.
     */
    public void loadByte(SettingsManager manager, byte[] data, int byteNum, Settings.Name name, int shift) {
        manager.setSetting(name, data[byteNum] + shift);
    }

    /**
     * Loads a signed 2-byte small-endian int.
     */
    public void load2ByteInt(SettingsManager manager, byte[] data, int byteNum, Settings.Name name) {
        manager.setSetting(name, IOFunctions.read2ByteInt(data, byteNum));
    }

    public <E extends Enum<E>> void loadEnum(SettingsManager manager, byte[] data, int byteNum, Settings.Name name, Map<Integer, E> map) {
        List<E> enabledValues = map.keySet().stream()
                .filter(key -> restoreState(data[byteNum], key))
                .map(map::get)
                .distinct()
                .toList();
        if (enabledValues.size() != 1) {
            throw new IllegalStateException("Cannot load setting " + name + " from string. " +
                    "Multiple or zero values are enabled at once: " + enabledValues);
        }

        E value = enabledValues.getFirst();
        manager.setSetting(name, value);
    }

}
