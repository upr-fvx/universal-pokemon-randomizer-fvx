package com.uprfvx.random.settings.settingstring;

// TODO: what is the version?

import com.uprfvx.random.Version;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.SettingsUpdater;
import com.uprfvx.romio.gamedata.BattleStyle;
import com.uprfvx.romio.gamedata.ExpCurve;
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

        // TODO: investigate "update moves legacy" (i.e., the pre-ZX setting that only/always updated to Gen 5)
        //  Can a settings string actually have it, when it has come this far?

        // TODO: testing all of this, somehow

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


        // Bytes 5--10: Custom Starters
        load2ByteInt(m, d, 5, Settings.Name.STARTER_CUSTOM_1);
        load2ByteInt(m, d, 7, Settings.Name.STARTER_CUSTOM_2);
        load2ByteInt(m, d, 9, Settings.Name.STARTER_CUSTOM_3);


        // Byte 11: Movesets
        loadEnum(m, d, 11, Settings.Name.RANDOMIZE_SPECIES_MOVESETS,
                Map.of(
                        0, Settings.MovesetsMod.COMPLETELY_RANDOM,
                        1, Settings.MovesetsMod.RANDOM_PREFER_SAME_TYPE,
                        2, Settings.MovesetsMod.UNCHANGED,
                        3, Settings.MovesetsMod.METRONOME_ONLY
                ));
        loadBoolean(m, d, 11, 5, Settings.Name.MOVESETS_ORDER_BY_DAMAGE);
        boolean hasGuaranteedMoves = restoreState(d[11], 4);
        int guaranteedMoveCount = readBits(d, 11, 6, 2) + 2;
        if (!hasGuaranteedMoves) {
            guaranteedMoveCount = 1;
        }
        m.set(Settings.Name.MOVESETS_GUARANTEED_LEVEL_1_MOVE_COUNT, guaranteedMoveCount);


        // Byte 12: Movesets Force Good Damaging
        loadBits(m, d, 12, 0, 7, Settings.Name.MOVESETS_FORCE_GOOD_DAMAGING_PERCENT);
        // (12, 7) is a boolean that can entirely disable the option
        if (restoreState(d[12], 7)) {
            m.set(Settings.Name.MOVESETS_FORCE_GOOD_DAMAGING_PERCENT, 0);
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
        boolean unchangedCatchRate = restoreState(d[18], 1);
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
        loadBits(m, d, 22, 0, 7, Settings.Name.TMS_GOOD_DAMAGING_PERCENT);
        // (22, 7) is a boolean that can entirely disable the option
        if (restoreState(d[22], 7)) {
            m.set(Settings.Name.TMS_GOOD_DAMAGING_PERCENT, 0);
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
        loadBits(m, d, 24, 0, 7, Settings.Name.TUTORS_GOOD_DAMAGING_PERCENT);
        // (24, 7) is a boolean that can entirely disable the option
        if (restoreState(d[24], 7)) {
            m.set(Settings.Name.TUTORS_GOOD_DAMAGING_PERCENT, 0);
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


        // Byte 26: Field items
        loadEnum(m, d, 26, Settings.Name.RANDOMIZE_FIELD_ITEMS,
                Map.of(
                        0, Settings.FieldItemsMod.RANDOM,
                        1, Settings.FieldItemsMod.SHUFFLE,
                        2, Settings.FieldItemsMod.UNCHANGED,
                        4, Settings.FieldItemsMod.RANDOM_EVEN
                ));
        loadBoolean(m, d, 26, 3, Settings.Name.FIELD_ITEMS_BAN_MINOR);
        // (26, 5--7) unused


        // Byte 27: Move Data
        loadBoolean(m, d, 27, 0, Settings.Name.MOVES_RANDOMIZE_POWER);
        loadBoolean(m, d, 27, 1, Settings.Name.MOVES_RANDOMIZE_ACCURACY);
        loadBoolean(m, d, 27, 2, Settings.Name.MOVES_RANDOMIZE_PP);
        loadBoolean(m, d, 27, 3, Settings.Name.MOVES_RANDOMIZE_TYPE);
        loadBoolean(m, d, 27, 4, Settings.Name.MOVES_RANDOMIZE_CATEGORY);
        loadBoolean(m, d, 27, 5, Settings.Name.STATICS_FIX_MUSIC);
        loadBoolean(m, d, 27, 6, Settings.Name.MOVES_RANDOMIZE_NAME);
        // (27, 7) unused


        // Byte 28: Evolutions
        loadEnum(m, d, 28, Settings.Name.RANDOMIZE_SPECIES_EVOLUTIONS,
                Map.of(
                        0, Settings.EvolutionsMod.UNCHANGED,
                        1, Settings.EvolutionsMod.RANDOM,
                        7, Settings.EvolutionsMod.RANDOM_EVERY_LEVEL
                ));
        loadBoolean(m, d, 28, 2, Settings.Name.SPECIES_EVOLUTIONS_USE_SIMILAR_STRENGTH);
        loadBoolean(m, d, 28, 3, Settings.Name.SPECIES_EVOLUTIONS_STAGES_MUST_SHARE_TYPE);
        loadBoolean(m, d, 28, 4, Settings.Name.SPECIES_EVOLUTIONS_MAX_THREE_STAGES);
        loadBoolean(m, d, 28, 5, Settings.Name.SPECIES_EVOLUTIONS_FORCE_CHANGE);
        loadBoolean(m, d, 28, 6, Settings.Name.SPECIES_EVOLUTIONS_ALLOW_ALT_FORMES);


        // Byte 29: Trainer Pokémon
        loadBoolean(m, d, 29, 0, Settings.Name.TRAINERS_USE_SIMILAR_STRENGTH);
        loadBoolean(m, d, 29, 1, Settings.Name.TRAINERS_RIVAL_CARRIES_STARTER);
        loadBoolean(m, d, 29, 2, Settings.Name.TRAINERS_WEIGHT_TYPES);
        loadBoolean(m, d, 29, 3, Settings.Name.TRAINERS_NO_LEGENDARIES);
        loadBoolean(m, d, 29, 4, Settings.Name.TRAINERS_NO_EARLY_WONDER_GUARD);
        loadBoolean(m, d, 29, 5, Settings.Name.TRAINERS_SWAP_MEGA_EVOLVABLES);
        loadBoolean(m, d, 29, 6, Settings.Name.TRAINERS_RANDOM_SHINY_POKEMON);
        loadBoolean(m, d, 29, 7, Settings.Name.TRAINERS_AVOID_DUPLICATES);


        // Byte 30: Pokémon Generation Restrictions
        loadBoolean(m, d, 30, 0, Settings.Name.LIMIT_ALLOW_RELATIVES);
        loadBoolean(m, d, 30, 1, Settings.Name.LIMIT_BAN_GENERATION_1, true);
        loadBoolean(m, d, 30, 2, Settings.Name.LIMIT_BAN_GENERATION_2, true);
        loadBoolean(m, d, 30, 3, Settings.Name.LIMIT_BAN_GENERATION_3, true);
        loadBoolean(m, d, 30, 4, Settings.Name.LIMIT_BAN_GENERATION_4, true);
        loadBoolean(m, d, 30, 5, Settings.Name.LIMIT_BAN_GENERATION_5, true);
        loadBoolean(m, d, 30, 6, Settings.Name.LIMIT_BAN_GENERATION_6, true);
        loadBoolean(m, d, 30, 7, Settings.Name.LIMIT_BAN_GENERATION_7, true);
        // Bytes 31--33 Unused (meant to be more Generation Restrictions, but all those fit in the first byte)


        // Bytes 34--37: Misc Tweaks
        int mt = IOFunctions.readFullIntBigEndian(d, 34);
        loadMiscTweak(m, mt, MiscTweakValues.BW_EXP_PATCH, Settings.Name.TWEAK_USE_SCALED_EXPERIENCE);
        loadMiscTweak(m, mt, MiscTweakValues.NERF_X_ACCURACY, Settings.Name.TWEAK_NERF_X_ACCURACY);
        loadMiscTweak(m, mt, MiscTweakValues.UPDATE_CRIT_RATE, Settings.Name.TWEAK_UPDATE_CRIT_RATE);
        loadMiscTweak(m, mt, MiscTweakValues.FASTEST_TEXT, Settings.Name.TWEAK_FASTEST_TEXT);
        loadMiscTweak(m, mt, MiscTweakValues.RUNNING_SHOES_INDOORS, Settings.Name.TWEAK_RUN_INDOORS);
        loadMiscTweak(m, mt, MiscTweakValues.RANDOMIZE_PC_POTION, Settings.Name.TWEAK_RANDOMIZE_PC_POTION);
        loadMiscTweak(m, mt, MiscTweakValues.ALLOW_PIKACHU_EVOLUTION, Settings.Name.TWEAK_ALLOW_PIKACHU_EVOLUTION);
        loadMiscTweak(m, mt, MiscTweakValues.NATIONAL_DEX_AT_START, Settings.Name.TWEAK_NATIONAL_DEX_AT_START);
        loadMiscTweak(m, mt, MiscTweakValues.FAST_EGG_HATCHING, Settings.Name.TWEAK_FAST_EGG_HATCHING);
        loadMiscTweak(m, mt, MiscTweakValues.FORCE_CHALLENGE_MODE, Settings.Name.TWEAK_FORCE_CHALLENGE_MODE);
        loadMiscTweak(m, mt, MiscTweakValues.LOWER_CASE_POKEMON_NAMES, Settings.Name.TWEAK_CAPITAL_CASE_SPECIES_NAMES);
        loadMiscTweak(m, mt, MiscTweakValues.RANDOMIZE_CATCHING_TUTORIAL, Settings.Name.TWEAK_RANDOMIZE_CATCHING_TUTORIAL);
        loadMiscTweak(m, mt, MiscTweakValues.BAN_LUCKY_EGG, Settings.Name.TWEAK_BAN_LUCKY_EGG);
        loadMiscTweak(m, mt, MiscTweakValues.NO_FREE_LUCKY_EGG, Settings.Name.TWEAK_NO_FREE_LUCKY_EGG);
        loadMiscTweak(m, mt, MiscTweakValues.BAN_BIG_MANIAC_ITEMS, Settings.Name.TWEAK_BAN_BIG_MONEY_MANIAC_ITEMS);
        loadMiscTweak(m, mt, MiscTweakValues.SOS_BATTLES_FOR_ALL, Settings.Name.TWEAK_ALL_WILD_POKEMON_CALL_ALLIES);
        loadMiscTweak(m, mt, MiscTweakValues.BALANCE_STATIC_LEVELS, Settings.Name.TWEAK_BALANCE_FOSSIL_LEVELS);
        loadMiscTweak(m, mt, MiscTweakValues.RETAIN_ALT_FORMES, Settings.Name.TWEAK_RETAIN_TEMPORARY_FORMES);
        loadMiscTweak(m, mt, MiscTweakValues.RUN_WITHOUT_RUNNING_SHOES, Settings.Name.TWEAK_RUN_WITHOUT_RUNNING_SHOES);
        loadMiscTweak(m, mt, MiscTweakValues.FASTER_HP_AND_EXP_BARS, Settings.Name.TWEAK_FASTER_HP_AND_EXP_BARS);
        loadMiscTweak(m, mt, MiscTweakValues.FAST_DISTORTION_WORLD, Settings.Name.TWEAK_FAST_DISTORTION_WORLD);
        loadMiscTweak(m, mt, MiscTweakValues.UPDATE_ROTOM_FORME_TYPING, Settings.Name.TWEAK_UPDATE_ROTOM_TYPING);
        loadMiscTweak(m, mt, MiscTweakValues.DISABLE_LOW_HP_MUSIC, Settings.Name.TWEAK_DISABLE_LOW_HP_MUSIC);
        loadMiscTweak(m, mt, MiscTweakValues.REUSABLE_TMS, Settings.Name.TWEAK_REUSABLE_TMS);
        loadMiscTweak(m, mt, MiscTweakValues.FORGETTABLE_HMS, Settings.Name.TWEAK_FORGETTABLE_HMS);
        loadMiscTweak(m, mt, MiscTweakValues.NO_EV_YIELDS, Settings.Name.TWEAK_NO_EV_YIELDS);


        // Byte 38: Trainer Pokémon level modifier
        // Shift from int8 range: [-128, 127] --> [-100, 155]
        loadByte(m, d, 38, Settings.Name.TRAINERS_LEVEL_MODIFIER_PERCENT, 28);


        // Byte 39: Shop items
        loadEnum(m, d, 39, Settings.Name.RANDOMIZE_SPECIAL_SHOP_ITEMS,
                Map.of(
                        0, Settings.ShopItemsMod.RANDOM,
                        1, Settings.ShopItemsMod.SHUFFLE,
                        2, Settings.ShopItemsMod.UNCHANGED
                ));
        loadBoolean(m, d, 39, 3, Settings.Name.SHOP_ITEMS_BAN_MINOR);
        loadBoolean(m, d, 39, 4, Settings.Name.SHOP_ITEMS_BAN_REGULAR_SHOP_ITEMS);
        loadBoolean(m, d, 39, 5, Settings.Name.SHOP_ITEMS_BAN_OVERPOWERED);
        // (39, 6) unused
        loadBoolean(m, d, 39, 7, Settings.Name.SHOP_ITEMS_GUARANTEE_EVOLUTION_ITEMS);


        // Byte 40: Wild Pokémon level modifier
        // Shift from int8 range: [-128, 127] --> [-100, 155]
        loadByte(m, d, 40, Settings.Name.WILD_LEVEL_MODIFIER_PERCENT, 28);


        // Byte 41: EXP curves, OP moves, and alt formes
        loadEnum(m, d, 41, Settings.Name.SPECIES_EXP_CURVE_STANDARDIZE_EXTENT,
                Map.of(
                        0, Settings.ExpCurveExtentMod.LEGENDARIES,
                        1, Settings.ExpCurveExtentMod.STRONG_LEGENDARIES,
                        2, Settings.ExpCurveExtentMod.ALL
                ));
        loadBoolean(m, d, 41, 3, Settings.Name.MOVESETS_BAN_OVERPOWERED);
        loadBoolean(m, d, 41, 4, Settings.Name.TMS_BAN_OVERPOWERED);
        loadBoolean(m, d, 41, 5, Settings.Name.TUTORS_BAN_OVERPOWERED);
        loadBoolean(m, d, 41, 6, Settings.Name.TRAINERS_ALLOW_ALT_FORMES);
        loadBoolean(m, d, 41, 7, Settings.Name.WILD_ALLOW_ALT_FORMES);


        // Byte 42: Additional Trainer Pokémon
        // (42, 0) unused
        loadBits(m, d, 42, 1, 3, Settings.Name.TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT);
        loadBits(m, d, 42, 4, 3, Settings.Name.TRAINERS_IMPORTANT_ADDITIONAL_POKEMON_COUNT);
        loadBoolean(m, d, 42, 7, Settings.Name.SPECIES_ABILITIES_COMBINE_DUPLICATES);


        // Byte 43: Misc.
        loadBits(m, d, 42, 0, 3, Settings.Name.TRAINERS_REGULAR_ADDITIONAL_POKEMON_COUNT);
        loadEnum(m, d, 43, Settings.Name.TOTEMS_RANDOMIZE_AURAS,
                Map.of(
                        3, Settings.AuraMod.UNCHANGED,
                        4, Settings.AuraMod.RANDOM,
                        5, Settings.AuraMod.SAME_STRENGTH
                ));
        loadBoolean(m, d, 43, 6, Settings.Name.MOVESETS_GUARANTEE_EVOLUTION_MOVES);
        loadBoolean(m, d, 43, 7, Settings.Name.SHOP_ITEMS_GUARANTEE_X_ITEMS);


        // Byte 44: Totem Pokémon
        loadEnum(m, d, 44, Settings.Name.RANDOMIZE_TOTEM_POKEMON,
                Map.of(
                        0, Settings.TotemPokemonMod.UNCHANGED,
                        1, Settings.TotemPokemonMod.RANDOM,
                        2, Settings.TotemPokemonMod.SIMILAR_STRENGTH
                ));
        loadEnum(m, d, 44, Settings.Name.TOTEMS_RANDOMIZE_ALLIES,
                Map.of(
                        3, Settings.AllyPokemonMod.UNCHANGED,
                        4, Settings.AllyPokemonMod.RANDOM,
                        5, Settings.AllyPokemonMod.SIMILAR_STRENGTH
                ));
        loadBoolean(m, d, 44, 6, Settings.Name.TOTEMS_RANDOMIZE_HELD_ITEMS);
        loadBoolean(m, d, 44, 7, Settings.Name.TOTEMS_ALLOW_ALT_FORMES);


        // Byte 45: Totem Pokémon level modifier
        // Shift from int8 range: [-128, 127] --> [-100, 155]
        loadByte(m, d, 45, Settings.Name.TOTEMS_LEVEL_MODIFIER_PERCENT, 28);


        // Byte 46: Base stat update generation
        loadByte(m, d, 46, Settings.Name.SPECIES_UPDATE_BASE_STATS_TO_GENERATION);


        // Byte 47: Move update generation
        loadByte(m, d, 47, Settings.Name.UPDATE_MOVES_TO_GENERATION);


        // Byte 48: Standard EXP curve
        // For some reason this one enum is written in an entirely different way. Do not ask why.
        ExpCurve standard = switch(d[48]) {
            case 0 -> ExpCurve.MEDIUM_FAST;
            case 1 -> ExpCurve.ERRATIC;
            case 2 -> ExpCurve.FLUCTUATING;
            case 3 -> ExpCurve.MEDIUM_SLOW;
            case 4 -> ExpCurve.FAST;
            case 5 -> ExpCurve.SLOW;
            default -> throw new IllegalStateException("Invalid byte value for Standard EXP curve: " + d[48]);
        };
        m.set(Settings.Name.SPECIES_EXP_CURVE_STANDARD_SELECTION, standard);


        // Byte 49: Static Pokémon level modifier
        // Shift from int8 range: [-128, 127] --> [-100, 155]
        loadByte(m, d, 49, Settings.Name.STATICS_LEVEL_MODIFIER_PERCENT, 28);


        // Byte 50: Trainer Pokémon held items + misc.
        loadBoolean(m, d, 50, 0, Settings.Name.TRAINERS_ADD_HELD_ITEMS_TO_BOSSES);
        loadBoolean(m, d, 50, 1, Settings.Name.TRAINERS_ADD_HELD_ITEMS_TO_IMPORTANT);
        loadBoolean(m, d, 50, 2, Settings.Name.TRAINERS_ADD_HELD_ITEMS_TO_REGULAR);
        loadBoolean(m, d, 50, 3, Settings.Name.TRAINERS_HELD_ITEMS_CONSUMABLE_ONLY);
        loadBoolean(m, d, 50, 4, Settings.Name.TRAINER_HELD_ITEMS_SENSIBLE_ONLY);
        loadBoolean(m, d, 50, 5, Settings.Name.TRAINERS_HELD_ITEMS_ACES_ONLY);
        loadBoolean(m, d, 50, 6, Settings.Name.SPECIES_ALWAYS_HAVE_TWO_ABILITIES);
        loadBoolean(m, d, 50, 7, Settings.Name.TRAINERS_USE_LOCAL);

        // Byte 51: Pickup items
        loadEnum(m, d, 51, Settings.Name.RANDOMIZE_PICKUP_ITEMS,
                Map.of(
                        0, Settings.PickupItemsMod.RANDOM,
                        1, Settings.PickupItemsMod.UNCHANGED
                ));
        loadBoolean(m, d, 51, 2, Settings.Name.PICKUP_ITEMS_BAN_MINOR);
        loadBoolean(m, d, 51, 3, Settings.Name.NO_IRREGULAR_ALT_FORMES);
        // (51, 4--7) unused


        // Byte 52: Elite Four uniqueness and minimum catch rate
        loadBits(m, d, 52, 0, 3, Settings.Name.TRAINERS_POKEMON_LEAGUE_UNIQUE_COUNT);
        int minCatchRateIndex = readBits(d, 0, 3, 3);
        Settings.CatchRateMod minCatchRate = switch (minCatchRateIndex) {
            case 0 -> Settings.CatchRateMod.STANDARDIZED;
            case 1 -> Settings.CatchRateMod.BUFFED;
            case 2 -> Settings.CatchRateMod.SUPER;
            case 3 -> Settings.CatchRateMod.ULTRA;
            case 4 -> Settings.CatchRateMod.GUARANTEED;
            default -> throw new IllegalStateException("Invalid minimal catch rate index: " + minCatchRateIndex);
        };
        m.set(Settings.Name.WILD_MINIMUM_CATCH_RATE_SELECTION, minCatchRate);
        if (unchangedCatchRate) {
            m.set(Settings.Name.WILD_MINIMUM_CATCH_RATE_SELECTION, Settings.CatchRateMod.UNCHANGED);
        }
        // (52, 6--7) unused


        // Byte 53: Starter type restrictions
        loadEnum(m, d, 53, Settings.Name.STARTERS_TYPE_RESTRICTION,
                Map.of(
                        0, Settings.StartersTypeMod.NONE,
                        1, Settings.StartersTypeMod.FIRE_WATER_GRASS,
                        2, Settings.StartersTypeMod.TRIANGLE,
                        3, Settings.StartersTypeMod.UNIQUE,
                        4, Settings.StartersTypeMod.SINGLE_TYPE
                ));
        // (53, 5) unused
        loadBoolean(m, d, 53, 6, Settings.Name.STARTERS_NO_LEGENDARIES);
        loadBoolean(m, d, 53, 7, Settings.Name.STARTERS_NO_DUAL_TYPES);


        // Byte 54: Starter single type
        // Shift from [Random=0 ... Highest Type Value + 1] -> [Random=-1 ... Highest Type Value]
        loadByte(m, d, 54, Settings.Name.STARTERS_SINGLE_TYPE_SELECTION, -1);


        // Byte 55: Pokémon palettes
        loadEnum(m, d, 55, Settings.Name.RANDOMIZE_SPECIES_PALETTES,
                Map.of(
                        0, Settings.SpeciesPalettesMod.UNCHANGED,
                        1, Settings.SpeciesPalettesMod.RANDOM
                ));
        loadBoolean(m, d, 55, 2, Settings.Name.PALETTES_FOLLOW_TYPES);
        loadBoolean(m, d, 55, 3, Settings.Name.PALETTES_FOLLOW_EVOLUTIONS);
        loadBoolean(m, d, 55, 4, Settings.Name.PALETTES_SHINY_FROM_NORMAL);
        // (55, 5--7) unused

        // Byte 56: Type effectiveness
        loadEnum(m, d, 56, Settings.Name.RANDOMIZE_TYPE_EFFECTIVENESS,
                Map.of(
                        0, Settings.TypeEffectivenessMod.UNCHANGED,
                        1, Settings.TypeEffectivenessMod.RANDOM,
                        2, Settings.TypeEffectivenessMod.RANDOM_BALANCED,
                        3, Settings.TypeEffectivenessMod.KEEP_IDENTITIES,
                        4, Settings.TypeEffectivenessMod.INVERSE
                ));
        loadBoolean(m, d, 56, 5, Settings.Name.TYPE_INVERSE_ADD_RANDOM_IMMUNITIES);
        loadBoolean(m, d, 56, 6, Settings.Name.UPDATE_TYPE_EFFECTIVENESS);
        // (56, 7) unused


        // Byte 57: Pokémon Evolutions
        loadBoolean(m, d, 57, 0, Settings.Name.SPECIES_EVOLUTIONS_FORCE_GROWTH);
        loadBoolean(m, d, 57, 1, Settings.Name.SPECIES_EVOLUTIONS_NO_CONVERGENCE);
        loadBoolean(m, d, 57, 2, Settings.Name.SPECIES_EVOLUTIONS_ADJUST_LEVELS_FOR_STRENGTH);
        // (57, 3--7) unused


        // Bytes 58--60: Starter BST limits
        // TODO: what is even up with these (ask stella)
        loadPackedBytePair(m, d, 58, 59, 0x0F, 8, Settings.Name.STARTERS_BST_MINIMUM);
        loadPackedBytePair(m, d, 58, 60, 0xF0, 4, Settings.Name.STARTERS_BST_MAXIMUM);


        // Byte 61: Trainer type diversity and better movesets
        loadBoolean(m, d, 61, 0, Settings.Name.TRAINERS_BOSSES_USE_DIVERSE_TYPES);
        loadBoolean(m, d, 61, 1, Settings.Name.TRAINERS_IMPORTANT_USE_DIVERSE_TYPES);
        loadBoolean(m, d, 61, 2, Settings.Name.TRAINERS_REGULAR_USE_DIVERSE_TYPES);
        loadBoolean(m, d, 61, 3, Settings.Name.TRAINERS_BETTER_MOVESETS_FOR_BOSSES);
        loadBoolean(m, d, 61, 4, Settings.Name.TRAINERS_BETTER_MOVESETS_FOR_IMPORTANT);
        loadBoolean(m, d, 61, 5, Settings.Name.TRAINERS_BETTER_MOVESETS_FOR_REGULAR);
        // (61, 6--7) unused

        
        // Byte 62: Battle style
        loadEnum(m, d, 62, Settings.Name.TRAINERS_RANDOMIZE_BATTLE_STYLE,
                Map.of(
                        0, BattleStyle.Modification.UNCHANGED,
                        1, BattleStyle.Modification.RANDOM,
                        2, BattleStyle.Modification.SINGLE_STYLE
                ));
        loadEnum(m, d, 62, Settings.Name.TRAINERS_SINGLE_STYLE_SELECTION,
                Map.of(
                        3, BattleStyle.Style.SINGLE_BATTLE,
                        4, BattleStyle.Style.DOUBLE_BATTLE,
                        5, BattleStyle.Style.TRIPLE_BATTLE,
                        6, BattleStyle.Style.ROTATION_BATTLE
                ));
        // (62, 7) unused


        // Byte 63: Evolution Stuff + level-modifier activation
        loadBoolean(m, d, 63, 0, Settings.Name.TRAINERS_EVOLVE_POKEMON);
        loadBoolean(m, d, 63, 1, Settings.Name.NO_PREMATURE_EVOLUTIONS);
        // (63, 2--5) are booleans that can entirely disable other level percentage options
        if (!restoreState(d[63], 2)) {
            m.set(Settings.Name.TRAINERS_LEVEL_MODIFIER_PERCENT, 0);
        }
        if (!restoreState(d[63], 3)) {
            m.set(Settings.Name.WILD_LEVEL_MODIFIER_PERCENT, 0);
        }
        if (!restoreState(d[63], 4)) {
            m.set(Settings.Name.TOTEMS_LEVEL_MODIFIER_PERCENT, 0);
        }
        if (!restoreState(d[63], 5)) {
            m.set(Settings.Name.STATICS_LEVEL_MODIFIER_PERCENT, 0);
        }
        // (63, 6--7) unused


        // Byte 64: Shop Items
        loadBoolean(m, d, 64, 0, Settings.Name.SHOP_ITEMS_BALANCE_PRICES);
        loadBoolean(m, d, 64, 1, Settings.Name.SHOP_ITEMS_ADD_CHEAP_RARE_CANDY);
        // (64, 2--7) unused


        // Byte 65: General Options
        loadBoolean(m, d, 65, 0, Settings.Name.NO_RANDOM_INTRO_MON, true);
        loadBoolean(m, d, 65, 1, Settings.Name.RACE_MODE);
        // (65, 2) unused
        // (65, 3) is a generation restriction override
        if (restoreState(d[65], 3)) {
            m.set(Settings.Name.LIMIT_BAN_GENERATION_1, false);
            m.set(Settings.Name.LIMIT_BAN_GENERATION_2, false);
            m.set(Settings.Name.LIMIT_BAN_GENERATION_3, false);
            m.set(Settings.Name.LIMIT_BAN_GENERATION_4, false);
            m.set(Settings.Name.LIMIT_BAN_GENERATION_5, false);
            m.set(Settings.Name.LIMIT_BAN_GENERATION_6, false);
            m.set(Settings.Name.LIMIT_BAN_GENERATION_7, false);
        }
        // (65, 4--7) unused


        // Byte 66: Easier Evolution Level
        loadByte(m, d, 66, Settings.Name.SPECIES_EVOLUTIONS_EASIER_SCALING_LEVEL);


        // Byte 67: Base Stat Totals
        loadEnum(m, d, 67, Settings.Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                Map.of(
                        0, Settings.BSTMod.UNCHANGED,
                        1, Settings.BSTMod.RANDOM_BUFF_NERF,
                        2, Settings.BSTMod.SHUFFLE,
                        3, Settings.BSTMod.RANDOM
                ));
        loadBoolean(m, d, 67, 4, Settings.Name.SPECIES_BSTS_FOLLOW_EVOLUTION);
        loadBoolean(m, d, 67, 5, Settings.Name.SPECIES_BST_SHUFFLE_LEGENDARIES_SEPARATELY);
        // (67, 6--7) unused


        // Byte 68: BST Random Buff/Nerf Max Percentage
        loadByte(m, d, 68, Settings.Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE);

        // TODO: loading the ROM name?
    }

    private void loadBoolean(SettingsManager manager, byte[] data, int byteNum, int bitNum, Settings.Name name) {
        manager.set(name, restoreState(data[byteNum], bitNum));
    }

    private void loadBoolean(SettingsManager manager, byte[] data, int byteNum, int bitNum,
                            Settings.Name name, boolean invert) {
        boolean state = restoreState(data[byteNum], bitNum);
        if (invert) {state = !state;}
        manager.set(name, state);
    }

    /**
     * Loads an unsigned integer <code>bitLength</code> bits long.
     */
    private void loadBits(SettingsManager manager, byte[] data, int byteNum, int bitNum, int bitLength,
                         Settings.Name name) {
        int value = readBits(data, byteNum, bitNum, bitLength);
        manager.set(name, value);
    }

    /**
     * Reads an unsigned integer <code>bitLength</code> bits long.
     */
    private int readBits(byte[] data, int byteNum, int bitNum, int bitLength) {
        return (data[byteNum] & 0xFF) >> bitNum & ((1 << bitLength) - 1);
    }

    /**
     * Loads a signed byte.
     */
    private void loadByte(SettingsManager manager, byte[] data, int byteNum, Settings.Name name) {
        manager.set(name, data[byteNum]);
    }

    /**
     * Loads a signed byte, shifted by a constant.
     */
    private void loadByte(SettingsManager manager, byte[] data, int byteNum, Settings.Name name, int shift) {
        manager.set(name, data[byteNum] + shift);
    }

    /**
     * Loads a value whose high bits are packed into one byte and whose low byte
     * is stored separately.
     */
    private void loadPackedBytePair(SettingsManager manager, byte[] data, int highByteNum, int lowByteNum,
                                    int highMask, int highShift, Settings.Name name) {
        int value = ((data[highByteNum] & highMask) << highShift) | (data[lowByteNum] & 0xFF);
        manager.set(name, value);
    }

    /**
     * Loads a signed 2-byte small-endian int.
     */
    private void load2ByteInt(SettingsManager manager, byte[] data, int byteNum, Settings.Name name) {
        manager.set(name, IOFunctions.read2ByteInt(data, byteNum));
    }

    /**
     * Loads an Enum using a number of bits that each correspond to an Enum value.
     * @throws IllegalStateException if [#bits that are 1/on] != 1.
     */
    private <E extends Enum<E>> void loadEnum(SettingsManager manager, byte[] data, int byteNum, Settings.Name name, Map<Integer, E> map) {
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
        manager.set(name, value);
    }

    private void loadMiscTweak(SettingsManager manager, int allTweaks, int bitMask, Settings.Name name) {
        manager.set(name, (allTweaks & bitMask) != 0);
    }

}
