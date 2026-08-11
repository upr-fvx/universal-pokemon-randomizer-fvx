package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.*;
import com.uprfvx.random.settings.restrictions.*;
import com.uprfvx.random.updaters.TypeEffectivenessUpdater;
import com.uprfvx.romio.MiscTweak;
import com.uprfvx.romio.gamedata.BattleStyle;
import com.uprfvx.romio.gamedata.ExpCurve;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.Serializable;
import java.util.*;

import static com.uprfvx.random.settings.SettingUtils.*;

//The list of EVERY setting supported by the randomizer.

//The types of setting definition are:
//SimpleSettingDefinition: Any setting that does not have restrictions on its values (that is to say, any value
// supported by the data type is applicable whenever the setting itself is supported and enabled.)
//EnumSettingDefinition: A setting which is an enum type, which can disable certain values based on other settings
// or RomHandler support. If you do not need to disable certain values, use a SimpleSettingDefinition.
//NumericSettingDefinition: A setting that is a numeric type, which is restricted to a certain range. The range can
// be restricted further based on other settings or RomHandler support.
//TODO: StringSettingDefinition, for restrictions like charset and string length.
//TODO: Image settings??
//SpeciesSettingDefinition. A decently weird special case for starter selection.

//Support is determined by a simple Predicate(RomHandler).
//Enabled/Disabled state is determined by SettingRestrictions, which contain two parts:
// The setting(s) which must be checked, and the function to check them against.
//Most cases can be handled by a SimpleSettingRestriction, which compares the value of a single setting against a predicate.
//When checking Enum settings, EnumMatchRestriction is also available.
//If there are multiple relevant settings, there is also MultiSettingRestriction, which combines the results of two
// or more SettingRestrictions in an AND, OR, NAND, or NOR manner. (This can include other MultiSettingRestrictions.)
//For more complicated checks (such as comparing one setting's value to another's) you may need to write your own
// extension of SettingRestriction.

//SettingsUtils contains several helpful functions for convenience:
//isTrue and isFalse, which check the states of boolean settings.
//equalsValue, lessThanValue, greaterThanValue, lessThanOrEqualsValue, and greaterThanOrEqualsValue,
// which compare a numeric setting to a set value.
//matchesEnumValue and doesNotMatchEnumValue, which check enum settings' states.
//ofGeneration, notOfGeneration, atLeastGeneration, and atMostGeneration, which check the generation of a RomHandler.

//Setting names should be unique. They also will (eventually) be used as ini keys, so they should (a) be relatively
// human-readable, (b) contain no spaces nor the equals sign.
//Each setting's name should be listed in Settings.Names so developers don't have to memorize them/type
// them correctly each time. Similarly, categories should be in Settings.Category and should be added to the appropriate
// supercategory.
//Setting categories should be the most specific applicable category. (E.g., "Base Stat Distributions" rather than
// "Pokemon Base Stats" or "Pokemon Traits". Supercategories should be the tabs of the GUI. Intermediate categories are
// skipped.
public class Settings {
    public static final List<SettingDefinition<? extends Serializable>> ALL_SETTINGS;
    public static final List<SettingDefinition<? extends Serializable>> REMOVED_SETTINGS;
    //When splitting a setting into multiple or changing its type, add the old version to the list of removed settings
    //so that we can load it in with the correct data type and SettingsUpdater can convert it to the new setting(s).

    /**
     * Enumerates every setting used in the randomizer.
     */
    public enum Name {
        //Note: Names may be rearranged without issue, but after release they should not be changed or removed from this list.
        //If you must rename a setting, keep the old Name in the enum and add it to REMOVED_SETTINGS.\

        // *** GENERAL OPTIONS ***
        //General options
        LIMIT_POKEMON, NO_IRREGULAR_ALT_FORMES, NO_PREMATURE_EVOLUTIONS, NO_RANDOM_INTRO_MON, RACE_MODE,
        //Limit Pokemon
        ALLOW_GENERATION_1, ALLOW_GENERATION_2, ALLOW_GENERATION_3, ALLOW_GENERATION_4, ALLOW_GENERATION_5,
        ALLOW_GENERATION_6, ALLOW_GENERATION_7,

        // *** SPECIES TRAITS ***
        //Base Stat Totals
        RANDOMIZE_SPECIES_BASE_STAT_TOTALS, SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, SPECIES_BSTS_FOLLOW_EVOLUTION,
        SPECIES_BST_SHUFFLE_LEGENDARIES_SEPARATELY,
        //Base Stat Distribution
        RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS, SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS,
        SPECIES_STAT_DISTRIBUTIONS_FOLLOW_MEGA_EVOLUTIONS, SPECIES_STAT_DISTRIBUTIONS_ASSIGN_EVO_STATS_RANDOMLY,
        //Update Base Stats
        UPDATE_SPECIES_BASE_STATS, SPECIES_UPDATE_BASE_STATS_TO_GENERATION,
        //Species' Types
        RANDOMIZE_SPECIES_TYPES, SPECIES_TYPES_FORCE_DUAL_TYPES, SPECIES_TYPES_FOLLOW_MEGA_EVOLUTIONS,
        //Species' Abilities
        RANDOMIZE_SPECIES_ABILITIES, SPECIES_ABILITIES_FOLLOW_EVOLUTIONS, SPECIES_ABILITIES_FOLLOW_MEGA_EVOLUTIONS,
        SPECIES_ABILITIES_COMBINE_DUPLICATES, SPECIES_ALWAYS_HAVE_TWO_ABILITIES, SPECIES_ABILITIES_BAN_WONDER_GUARD,
        SPECIES_ABILITIES_BAN_MINOR, SPECIES_ABILITIES_BAN_NEGATIVE, SPECIES_ABILITIES_BAN_TRAPPING,
        //Species' Evolutions
        RANDOMIZE_SPECIES_EVOLUTIONS, SPECIES_EVOLUTIONS_USE_SIMILAR_STRENGTH, SPECIES_EVOLUTIONS_STAGES_MUST_SHARE_TYPE,
        SPECIES_EVOLUTIONS_MAX_THREE_STAGES, SPECIES_EVOLUTIONS_NO_CONVERGENCE, SPECIES_EVOLUTIONS_FORCE_CHANGE,
        SPECIES_EVOLUTIONS_FORCE_GROWTH, SPECIES_EVOLUTIONS_ALLOW_ALT_FORMES, SPECIES_EVOLUTIONS_ADJUST_LEVELS_FOR_STRENGTH,
        SPECIES_EVOLUTIONS_MAKE_POSSIBLE, SPECIES_EVOLUTIONS_MAKE_EASIER, SPECIES_EVOLUTIONS_EASIER_SCALING_LEVEL,
        SPECIES_EVOLUTIONS_CHANGES_USE_ESTIMATED_LEVELS, SPECIES_EVOLUTIONS_REMOVE_TIME_BASED,
        //Species' EXP Curves
        STANDARDIZE_SPECIES_EXP_CURVES, SPECIES_EXP_CURVE_STANDARD_SELECTION, SPECIES_EXP_CURVE_STANDARDIZE_EXTENT,

        // *** GIVEN POKEMON ***
        //Starters General
        RANDOMIZE_STARTERS, STARTERS_NO_LEGENDARIES, STARTERS_RANDOMIZE_HELD_ITEMS, STARTERS_BAN_BAD_HELD_ITEMS,
        STARTERS_ALLOW_ALT_FORMES,
        //Starters Custom
        STARTER_CUSTOM_1, STARTER_CUSTOM_2, STARTER_CUSTOM_3,
        //Starter Types
        STARTERS_TYPE_RESTRICTION, STARTERS_NO_DUAL_TYPES, STARTERS_SINGLE_TYPE_SELECTION,
        //Starter BSTs
        STARTERS_BST_USE_MINIMUM, STARTERS_BST_MINIMUM_SELECTION, STARTERS_BST_USE_MAXIMUM,
        STARTERS_BST_MAXIMUM_SELECTION,
        //Statics
        RANDOMIZE_STATIC_ENCOUNTERS, STATICS_FULL_RANDOM_OVER_600_BST, STATICS_LIMIT_MAIN_GAME_LEGENDARIES,
        STATICS_ALLOW_ALT_FORMES, STATICS_SWAP_MEGA_EVOLVABLES, STATICS_FIX_MUSIC, STATICS_USE_LEVEL_MODIFIER,
        STATICS_LEVEL_MODIFIER_PERCENT,
        //In-Game Trades
        RANDOMIZE_IN_GAME_TRADES, TRADES_RANDOMIZE_NICKNAMES, TRADES_RANDOMIZE_ORIGINAL_TRAINERS,
        TRADES_RANDOMIZE_IVS, TRADES_RANDOMIZE_HELD_ITEMS,

        // *** MOVES AND MOVESETS ***
        //Move Traits
        MOVES_RANDOMIZE_POWER, MOVES_RANDOMIZE_ACCURACY, MOVES_RANDOMIZE_PP, MOVES_RANDOMIZE_TYPE,
        MOVES_RANDOMIZE_CATEGORY, MOVES_RANDOMIZE_NAME, UPDATE_MOVES, UPDATE_MOVES_TO_GENERATION,
        //Species' Movesets
        RANDOMIZE_SPECIES_MOVESETS, MOVESETS_GUARANTEE_LEVEL_1_MOVES, MOVESETS_GUARANTEED_LEVEL_1_MOVE_COUNT, //TODO: combine level 1 moves
        MOVESETS_ORDER_BY_DAMAGE, MOVESETS_BAN_OVERPOWERED, MOVESETS_FORCE_GOOD_DAMAGING, //TODO: combine force good damaging
        MOVESETS_FORCE_GOOD_DAMAGING_PERCENT, MOVESETS_GUARANTEE_EVOLUTION_MOVES,

        // *** FOE POKEMON ***
        //Trainers General
        RANDOMIZE_TRAINER_POKEMON, TRAINERS_RIVAL_CARRIES_STARTER, TRAINERS_USE_SIMILAR_STRENGTH,
        TRAINERS_AVOID_DUPLICATES, TRAINERS_WEIGHT_TYPES, TRAINERS_USE_LOCAL, TRAINERS_NO_LEGENDARIES,
        TRAINERS_NO_EARLY_WONDER_GUARD, TRAINERS_ALLOW_ALT_FORMES, TRAINERS_SWAP_MEGA_EVOLVABLE,
         TRAINERS_POKEMON_LEAGUE_HAVE_UNIQUE, TRAINERS_POKEMON_LEAGUE_UNIQUE_COUNT, //TODO: COMBINE
        //That's a lot for one category. We should probably organize these more. TODO: that
        //Trainer Movesets
        TRAINERS_BETTER_MOVESETS_FOR_BOSSES, TRAINERS_BETTER_MOVESETS_FOR_IMPORTANT,
        TRAINERS_BETTER_MOVESETS_FOR_REGULAR,
        //Trainers Additional Pokemon
        TRAINERS_ADD_POKEMON_TO_BOSSES, TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT,
        TRAINERS_ADD_POKEMON_TO_IMPORTANT, TRAINERS_IMPORTANT_ADDITIONAL_POKEMON_COUNT,
        TRAINERS_ADD_POKEMON_TO_REGULAR, TRAINERS_REGULAR_ADDITIONAL_POKEMON_COUNT, //TODO: combine
        //Trainers Held Items
        TRAINERS_ADD_HELD_ITEMS_TO_BOSSES, TRAINERS_ADD_HELD_ITEMS_TO_IMPORTANT, TRAINERS_ADD_HELD_ITEMS_TO_REGULAR,
        TRAINERS_HELD_ITEMS_CONSUMABLE_ONLY, TRAINER_HELD_ITEMS_SENSIBLE_ONLY, TRAINERS_HELD_ITEMS_ACES_ONLY,
        //Trainers Diverse Types
        TRAINERS_BOSSES_USE_DIVERSE_TYPES, TRAINERS_IMPORTANT_USE_DIVERSE_TYPES, TRAINERS_REGULAR_USE_DIVERSE_TYPES,
        //Trainers Cosmetic
        TRAINERS_RANDOM_SHINY_POKEMON,

        // *** WILD ENCOUNTERS ***
        //Wild General
        RANDOMIZE_WILD_ENCOUNTERS, WILD_NO_LEGENDARIES,  WILD_CATCH_EM_ALL, WILD_USE_SIMILAR_STRENGTH,
        WILD_SIMILAR_STRENGTH_BALANCE_LOW_LEVEL, WILD_ALLOW_ALT_FORMES,
        //Wild Replacement Zone
        WILD_REPLACEMENT_ZONE, WILD_SPLIT_REPLACEMENT_ZONE_BY_ENCOUNTER_TYPES, WILD_REMOVE_TIME_BASED,
        //Wild Types
        WILD_TYPE_RESTRICTION, WILD_KEEP_TYPE_THEMES,
        //Wild Evolutions
        WILD_EVOLUTION_RESTRICTION, WILD_EVOLUTION_KEEP_RELATIONS,
        //Wild Post Tweaks
        WILD_SET_MINIMUM_CATCH_RATE, WILD_MINIMUM_CATCH_RATE_SELECTION, WILD_RANDOMIZE_HELD_ITEMS,
        WILD_HELD_ITEMS_BAN_MINOR, WILD_USE_LEVEL_MODIFIER, WILD_LEVEL_MODIFIER_PERCENT,

        // *** MOVE TEACHERS ***
        //TM Moves
        RANDOMIZE_TM_MOVES, TMS_BAN_OVERPOWERED, TMS_KEEP_FIELD_MOVES, TMS_FORCE_GOOD_DAMAGING,
        TMS_GOOD_DAMAGING_PERCENT,
        //TM And HM Compatability
        RANDOMIZE_TM_AND_HM_COMPATABILITY, TM_COMPATABILITY_LEVEL_UP_SANITY, TM_COMPATABILITY_FOLLOW_EVOLUTIONS,
        TMS_FULL_HM_COMPATABILITY,
        //Move Tutor Moves
        RANDOMIZE_TUTOR_MOVES, TUTORS_BAN_OVERPOWERED, TUTORS_KEEP_FIELD_MOVES, TUTORS_FORCE_GOOD_DAMAGING,
        TUTORS_GOOD_DAMAGING_PERCENT,
        //Move Tutor Compatability
        RANDOMIZE_TUTOR_COMPATABILITY, TUTOR_COMPATABILITY_LEVEL_UP_SANITY, TUTOR_COMPATABILITY_FOLLOW_EVOLUTIONS,

        // *** ITEMS ***
        //Field Items
        RANDOMIZE_FIELD_ITEMS, FIELD_ITEMS_BAN_MINOR,
        //Shop Items General
        SHOP_ITEMS_BALANCE_PRICES, SHOP_ITEMS_ADD_CHEAP_RARE_CANDY,
        //Special Shop Items
        RANDOMIZE_SPECIAL_SHOP_ITEMS, SHOP_ITEMS_BAN_MINOR, SHOP_ITEMS_BAN_REGULAR_SHOP_ITEMS,
        SHOP_ITEMS_BAN_OVERPOWERED, SHOP_ITEMS_GUARANTEE_EVOLUTION_ITEMS, SHOP_ITEMS_GUARANTEE_X_ITEMS,
        //Pickup Items
        RANDOMIZE_PICKUP_ITEMS, PICKUP_ITEMS_BAN_MINOR,

        // *** TYPES ***
        //Type Effectiveness
        RANDOMIZE_TYPE_EFFECTIVENESS, TYPE_INVERSE_ADD_RANDOM_IMMUNITIES, UPDATE_TYPE_EFFECTIVENESS,

        // *** GRAPHICS ***
        //Pokemon Palettes
        RANDOMIZE_SPECIES_PALETTES, PALETTES_FOLLOW_TYPES, PALETTES_FOLLOW_EVOLUTIONS, PALETTES_SHINY_FROM_NORMAL,
        //Custom Player Graphics
        //TODO: determine how we're handling these

        // *** MISC ***
        //Misc Tweaks
        TWEAK_USE_SCALED_EXPERIENCE, TWEAK_NERF_X_ACCURACY, TWEAK_UPDATE_CRIT_RATE, TWEAK_FASTEST_TEXT,
        TWEAK_RUN_INDOORS, TWEAK_RANDOMIZE_PC_POTION, TWEAK_ALLOW_PIKACHU_EVOLUTION, TWEAK_NATIONAL_DEX_AT_START,
        TWEAK_FAST_EGG_HATCHING, TWEAK_FORCE_CHALLENGE_MODE, TWEAK_LOWER_CASE_SPECIES_NAMES,
        TWEAK_RANDOMIZE_CATCHING_TUTORIAL, TWEAK_BAN_LUCKY_EGG, TWEAK_NO_FREE_LUCKY_EGG, TWEAK_BAN_BIG_MONEY_ITEMS,
        TWEAK_ALL_WILD_POKEMON_CALL_ALLIES, TWEAK_BALANCE_FOSSIL_LEVELS, TWEAK_RETAIN_TEMPORARY_FORMES,
        TWEAK_RUN_WITHOUT_RUNNING_SHOES, TWEAK_FASTER_BARS, TWEAK_FAST_DISTORTION_WORLD, TWEAK_UPDATE_ROTOM_TYPING,
        TWEAK_DISABLE_LOW_HP_MUSIC, TWEAK_REUSABLE_TMS, TWEAK_FORGETTABLE_HMS, TWEAK_NO_EV_GAIN;
    }

    public enum Category {
        //Unlike Names, there are no limitations on renaming or removing categories.

        // *** GENERAL OPTIONS ***
        GENERAL_OPTIONS, LIMIT_POKEMON,

        // *** SPECIES TRAITS ***
        SPECIES_BASE_STATISTIC_TOTALS, SPECIES_BASE_STATISTIC_DISTRIBUTION, SPECIES_UPDATE_BASE_STATISTICS,
        SPECIES_TYPES, SPECIES_ABILITIES, SPECIES_EVOLUTIONS, SPECIES_EXP_CURVES,

        // *** GIVEN POKEMON ***
        STARTERS_GENERAL, STARTERS_CUSTOM, STARTER_TYPES, STARTER_BSTS, IN_GAME_TRADES,
        //Statics
        STATIC_ENCOUNTERS,

        // *** MOVES AND MOVESETS ***
        MOVE_TRAITS, SPECIES_MOVESETS,
        //It didn't occur to me until naming this category but... should SPECIES_MOVESETS be in SPECIES_TRAITS?
        //(I mean, there's no room for it, so probably not, but logically... kinda?)

        // *** FOE POKEMON ***
        TRAINERS_GENERAL, TRAINER_MOVESETS, TRAINERS_ADDITIONAL_POKEMON, TRAINERS_HELD_ITEMS, TRAINERS_DIVERSE_TYPES,
        TRAINERS_BATTLE_STYLE, TRAINERS_COSMETIC, TOTEM_POKEMON,

        // *** WILD ENCOUNTERS ***
        WILD_GENERAL, WILD_REPLACEMENT_ZONE, WILD_TYPES, WILD_EVOLUTIONS, WILD_POST_TWEAKS,

        // *** MOVE TEACHERS ***
        TM_MOVES, TM_AND_HM_COMPATABILITY, MOVE_TUTOR_MOVES, MOVE_TUTOR_COMPATABILITY,

        // *** ITEMS ***
        FIELD_ITEMS, SHOP_ITEMS_GENERAL, SPECIAL_SHOP_ITEMS, PICKUP_ITEMS,

        // *** TYPES ***
        TYPE_EFFECTIVENESS,

        // *** GRAPHICS ***
        SPECIES_PALETTES, CUSTOM_PLAYER_GRAPHICS,

        // *** MISC. TWEAKS ***
        MISC_TWEAKS;

        // ****** Supercategories ******/
        public static final List<Category> GENERAL = List.of(GENERAL_OPTIONS, LIMIT_POKEMON);
        public static final List<Category> SPECIES_TRAITS = List.of(SPECIES_BASE_STATISTIC_TOTALS,
                SPECIES_BASE_STATISTIC_DISTRIBUTION, SPECIES_UPDATE_BASE_STATISTICS, SPECIES_TYPES,
                SPECIES_ABILITIES, SPECIES_EVOLUTIONS, SPECIES_EXP_CURVES);
        public static final List<Category> GIVEN_POKEMON = List.of(STARTERS_GENERAL, STARTERS_CUSTOM, STARTER_TYPES,
                STARTER_BSTS, IN_GAME_TRADES, STATIC_ENCOUNTERS); //TODO: move statics to WILD_POKEMON
        public static final List<Category> MOVES_AND_MOVESETS = List.of(MOVE_TRAITS, SPECIES_MOVESETS);
        public static final List<Category> WILD_ENCOUNTERS = List.of(WILD_GENERAL, WILD_REPLACEMENT_ZONE, WILD_TYPES,
                WILD_EVOLUTIONS, WILD_POST_TWEAKS);
        public static final List<Category> MOVE_TEACHERS = List.of(TM_MOVES, TM_AND_HM_COMPATABILITY, MOVE_TUTOR_MOVES,
                MOVE_TUTOR_COMPATABILITY);
        public static final List<Category> ITEMS = List.of(FIELD_ITEMS, SHOP_ITEMS_GENERAL, SPECIAL_SHOP_ITEMS,
                PICKUP_ITEMS);
        public static final List<Category> TYPES = List.of(TYPE_EFFECTIVENESS);
        public static final List<Category> GRAPHICS = List.of(SPECIES_PALETTES, CUSTOM_PLAYER_GRAPHICS);
        public static final List<Category> MISC = List.of(MISC_TWEAKS);
    }

    //region general options

    // needs to be up here since general options relies on it
    public static final SettingRestriction notEvolveEveryLevelRestriction = new EnumMatchRestriction<>(
            Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.RANDOM_EVERY_LEVEL, false
    );

    public static final List<SettingDefinition<?>> GENERAL_OPTIONS = List.of(
            new SimpleSettingDefinition<>(
                    Name.LIMIT_POKEMON,
                    Category.GENERAL_OPTIONS,
                    false,
                    null,
                    notOfGeneration(1)
            ), //TODO: might be able to eliminate this setting and just use the "AllowGenerationX" settings (inverted)
            new SimpleSettingDefinition<>(
                    Name.NO_RANDOM_INTRO_MON,
                    Category.GENERAL_OPTIONS, //TODO: move to misc. tweaks?
                    false,
                    null,
                    null
            ),
                            //TODO: make this setting actually work?
                            // "this setting" is race mode?
                            // I believe I was referring to "NoRandomIntroMon" but I cannot recall for sure anymore.
                            //TODO investigate this todo i guess
            new SimpleSettingDefinition<>(
                    Name.RACE_MODE,
                    Category.GENERAL_OPTIONS,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.NO_IRREGULAR_ALT_FORMES,
                    Category.GENERAL_OPTIONS,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.NO_PREMATURE_EVOLUTIONS,
                    Category.GENERAL_OPTIONS,
                    false,
                    notEvolveEveryLevelRestriction,
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.ALLOW_GENERATION_1,
                    Category.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Name.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(2)
            ),
            new SimpleSettingDefinition<>(
                    Name.ALLOW_GENERATION_2,
                    Category.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Name.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(2)
            ),
            new SimpleSettingDefinition<>(
                    Name.ALLOW_GENERATION_3,
                    Category.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Name.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(3)
            ),
            new SimpleSettingDefinition<>(
                    Name.ALLOW_GENERATION_4,
                    Category.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Name.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(4)
            ),
            new SimpleSettingDefinition<>(
                    Name.ALLOW_GENERATION_5,
                    Category.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Name.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(5)
            ),
            new SimpleSettingDefinition<>(
                    Name.ALLOW_GENERATION_6,
                    Category.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Name.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(6)
            ),
            new SimpleSettingDefinition<>(
                    Name.ALLOW_GENERATION_7,
                    Category.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Name.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(7)
            )
    );

    //endregion

    //region species traits

    public enum BSTMod {
        UNCHANGED, RANDOM_BUFF_NERF, SHUFFLE, RANDOM
    }

    public enum BaseStatDistributionsMod {
        UNCHANGED, SHUFFLE, RANDOM
    }

    public enum SpeciesTypesMod {
        UNCHANGED, RANDOM_FOLLOW_EVOLUTIONS, COMPLETELY_RANDOM
    }

    public enum AbilitiesMod {
        UNCHANGED, RANDOMIZE
    }

    public enum EvolutionsMod {
        UNCHANGED, RANDOM, RANDOM_EVERY_LEVEL
    }

    public enum ExpCurveExtentMod {
        LEGENDARIES, STRONG_LEGENDARIES, ALL
    }

    public static final List<SettingDefinition<?>> SPECIES_TRAITS = List.of(
            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                    Category.SPECIES_BASE_STATISTIC_TOTALS,
                    BSTMod.UNCHANGED,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE,
                    Category.SPECIES_BASE_STATISTIC_TOTALS,
                    0,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, BSTMod.RANDOM_BUFF_NERF),
                    null,
                    0, 50
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_BSTS_FOLLOW_EVOLUTION,
                    Category.SPECIES_BASE_STATISTIC_TOTALS,
                    false,
                    new MultiSettingRestriction(false, false,
                            notEvolveEveryLevelRestriction,
                            new MultiSettingRestriction(true, false,
                                new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, BSTMod.RANDOM_BUFF_NERF),
                                new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, BSTMod.SHUFFLE))
                    ),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_BST_SHUFFLE_LEGENDARIES_SEPARATELY,
                    Category.SPECIES_BASE_STATISTIC_TOTALS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, BSTMod.SHUFFLE),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS,
                    Category.SPECIES_BASE_STATISTIC_DISTRIBUTION,
                    BaseStatDistributionsMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS,
                    Category.SPECIES_BASE_STATISTIC_DISTRIBUTION,
                    false,
                    new MultiSettingRestriction(false, false,
                            notEvolveEveryLevelRestriction,
                            new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS,
                                    BaseStatDistributionsMod.UNCHANGED, false)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_MEGA_EVOLUTIONS,
                    Category.SPECIES_BASE_STATISTIC_DISTRIBUTION,
                    false,
                    new SimpleSettingRestriction<>(Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS, isTrue),
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_STAT_DISTRIBUTIONS_ASSIGN_EVO_STATS_RANDOMLY,
                    Category.SPECIES_BASE_STATISTIC_DISTRIBUTION,
                    false,
                    new SimpleSettingRestriction<>(Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS, isTrue),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.UPDATE_SPECIES_BASE_STATS,
                    Category.SPECIES_UPDATE_BASE_STATISTICS,
                    false,
                    null,
                    notOfGeneration(1)),
            new NumericSettingDefinition<>(
                    Name.SPECIES_UPDATE_BASE_STATS_TO_GENERATION,
                    Category.SPECIES_UPDATE_BASE_STATISTICS,
                    9,
                    new SimpleSettingRestriction<>(Name.UPDATE_SPECIES_BASE_STATS, isTrue),
                    null,
                    6, 9,
                    null,
                    null,
                    higherValueThanGeneration(6, 7),
                    null
            ),

            new EnumSettingDefinition<>(
                    Name.RANDOMIZE_SPECIES_TYPES,
                    Category.SPECIES_TYPES,
                    SpeciesTypesMod.UNCHANGED,
                    null,
                    null,
                    Map.of(SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS, notEvolveEveryLevelRestriction),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_TYPES_FORCE_DUAL_TYPES,
                    Category.SPECIES_TYPES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_TYPES, SpeciesTypesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_TYPES_FOLLOW_MEGA_EVOLUTIONS,
                    Category.SPECIES_TYPES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_TYPES, SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS),
                    RomHandler::hasMegaEvolutions
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_SPECIES_ABILITIES,
                    Category.SPECIES_ABILITIES,
                    AbilitiesMod.UNCHANGED,
                    null,
                    rh -> rh.abilitiesPerSpecies() != 0
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_ABILITIES_FOLLOW_EVOLUTIONS,
                    Category.SPECIES_ABILITIES,
                    false,
                    new MultiSettingRestriction(false, false,
                            notEvolveEveryLevelRestriction,
                            new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_ABILITIES,
                                    AbilitiesMod.UNCHANGED, false)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_ABILITIES_FOLLOW_MEGA_EVOLUTIONS,
                    Category.SPECIES_ABILITIES,
                    false,
                    new SimpleSettingRestriction<>(Name.SPECIES_ABILITIES_FOLLOW_EVOLUTIONS, isTrue),
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_ABILITIES_COMBINE_DUPLICATES,
                    Category.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_ALWAYS_HAVE_TWO_ABILITIES,
                    Category.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_ABILITIES_BAN_WONDER_GUARD,
                    Category.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_ABILITIES_BAN_TRAPPING,
                    Category.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_ABILITIES_BAN_NEGATIVE,
                    Category.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_ABILITIES_BAN_MINOR,
                    Category.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),

            new EnumSettingDefinition<>(
                    Name.RANDOMIZE_SPECIES_EVOLUTIONS,
                    Category.SPECIES_EVOLUTIONS,
                    EvolutionsMod.UNCHANGED,
                    null,
                    null,
                    null,
                    Map.of(EvolutionsMod.RANDOM_EVERY_LEVEL, RomHandler::canGiveEverySpeciesOneEvolutionEach)
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_USE_SIMILAR_STRENGTH,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_STAGES_MUST_SHARE_TYPE,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_MAX_THREE_STAGES,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_NO_CONVERGENCE,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_FORCE_CHANGE,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_FORCE_GROWTH,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_ALLOW_ALT_FORMES,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.UNCHANGED, false),
                    ofGeneration(7)
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_ADJUST_LEVELS_FOR_STRENGTH,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, BSTMod.UNCHANGED, false),
                            new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS, EvolutionsMod.UNCHANGED, false)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_MAKE_POSSIBLE,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    notEvolveEveryLevelRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_MAKE_EASIER,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    notEvolveEveryLevelRestriction,
                    null
            ),
            new NumericSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_EASIER_SCALING_LEVEL,
                    Category.SPECIES_EVOLUTIONS,
                    40,
                    new SimpleSettingRestriction<>(Name.SPECIES_EVOLUTIONS_MAKE_EASIER, isTrue),
                    null,
                    30, 65
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_CHANGES_USE_ESTIMATED_LEVELS,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    new MultiSettingRestriction(true, false,
                            new SimpleSettingRestriction<>(Name.SPECIES_EVOLUTIONS_MAKE_POSSIBLE, isTrue),
                            new SimpleSettingRestriction<>(Name.SPECIES_EVOLUTIONS_MAKE_EASIER, isTrue)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EVOLUTIONS_REMOVE_TIME_BASED,
                    Category.SPECIES_EVOLUTIONS,
                    false,
                    notEvolveEveryLevelRestriction,
                    RomHandler::hasTimeBasedEvolutions
            ),

            new SimpleSettingDefinition<>(
                    Name.STANDARDIZE_SPECIES_EXP_CURVES,
                    Category.SPECIES_EXP_CURVES,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EXP_CURVE_STANDARD_SELECTION,
                    Category.SPECIES_EXP_CURVES,
                    ExpCurve.MEDIUM_FAST,
                    new SimpleSettingRestriction<>(Name.STANDARDIZE_SPECIES_EXP_CURVES, isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SPECIES_EXP_CURVE_STANDARDIZE_EXTENT,
                    Category.SPECIES_EXP_CURVES,
                    ExpCurveExtentMod.LEGENDARIES,
                    new SimpleSettingRestriction<>(Name.STANDARDIZE_SPECIES_EXP_CURVES, isTrue),
                    null
            )
    );

    //endregion

    //region given pokemon [currently Starters, Statics, & Trades]
    //TODO: move statics => Wild Pokemon supercategory & tab
    // Should statics be moved already, or is that a future project for once we've split off gift mons?

    public enum StartersMod {
        UNCHANGED, CUSTOM, COMPLETELY_RANDOM, RANDOM_WITH_TWO_EVOLUTIONS, RANDOM_BASIC
    }

    public enum StartersTypeMod {
        NONE, FIRE_WATER_GRASS, TRIANGLE, UNIQUE, SINGLE_TYPE
    }

    private final static SettingRestriction anyStarterIsRandomRestriction = new MultiSettingRestriction(
            true, false,
            new MultiSettingRestriction(false, false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.CUSTOM),
                    new MultiSettingRestriction(true, false,
                            new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_1,
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_2,
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_3,
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES))
                    )
            ),
            new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.COMPLETELY_RANDOM),
            new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.RANDOM_WITH_TWO_EVOLUTIONS),
            new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.RANDOM_BASIC)
    );

    private final static SettingRestriction allStartersAreRandomRestriction = new MultiSettingRestriction(
            true, false,
            new MultiSettingRestriction(false, false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.CUSTOM),
                    new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_1,
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_2,
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_3,
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES))
            ),
            new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.COMPLETELY_RANDOM),
            new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.RANDOM_WITH_TWO_EVOLUTIONS),
            new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.RANDOM_BASIC)
    );

    public enum StaticPokemonMod {
        UNCHANGED, RANDOM_MATCHING, COMPLETELY_RANDOM, SIMILAR_STRENGTH
    }

    public enum InGameTradesMod {
        UNCHANGED, RANDOMIZE_GIVEN, RANDOMIZE_GIVEN_AND_REQUESTED
    }

    public static final List<SettingDefinition<?>> STARTERS_STATICS_AND_TRADES = List.of(
            new EnumSettingDefinition<>(
                    Name.RANDOMIZE_STARTERS,
                    Category.STARTERS_GENERAL,
                    StartersMod.UNCHANGED,
                    null,
                    null,
                    Map.of(
                            StartersMod.RANDOM_WITH_TWO_EVOLUTIONS, notEvolveEveryLevelRestriction,
                            StartersMod.RANDOM_BASIC, notEvolveEveryLevelRestriction
                    ),
                    null
            ),
            new SpeciesIndexSettingDefinition(
                    Name.STARTER_CUSTOM_1,
                    Category.STARTERS_CUSTOM,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.CUSTOM),
                    null
            ),
            new SpeciesIndexSettingDefinition(
                    Name.STARTER_CUSTOM_2,
                    Category.STARTERS_CUSTOM,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.CUSTOM),
                    null
            ),
            new SpeciesIndexSettingDefinition(
                    Name.STARTER_CUSTOM_3,
                    Category.STARTERS_CUSTOM,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STARTERS, StartersMod.CUSTOM),
                    rh -> rh.getStarters().size() > 2
            ),

            new EnumSettingDefinition<>(
                    Name.STARTERS_TYPE_RESTRICTION,
                    Category.STARTER_TYPES,
                    StartersTypeMod.NONE,
                    anyStarterIsRandomRestriction,
                    null,
                    Map.of( // restricted states
                            StartersTypeMod.FIRE_WATER_GRASS, allStartersAreRandomRestriction,
                            StartersTypeMod.TRIANGLE, allStartersAreRandomRestriction
                    ),
                    Map.of( // supported states
                            StartersTypeMod.FIRE_WATER_GRASS, RomHandler::hasStarterTypeTriangleSupport,
                            StartersTypeMod.TRIANGLE, RomHandler::hasStarterTypeTriangleSupport
                    )
            ),
            new SimpleSettingDefinition<>(
                    Name.STARTERS_NO_DUAL_TYPES,
                    Category.STARTER_TYPES,
                    false,
                    anyStarterIsRandomRestriction,
                    null
            ),
            new TypeOrRandomSettingDefinition(
                    Name.STARTERS_SINGLE_TYPE_SELECTION,
                    Category.STARTER_TYPES,
                    new EnumMatchRestriction<>(Name.STARTERS_TYPE_RESTRICTION, StartersTypeMod.SINGLE_TYPE),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.STARTERS_NO_LEGENDARIES,
                    Category.STARTERS_GENERAL,
                    false,
                    anyStarterIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.STARTERS_RANDOMIZE_HELD_ITEMS,
                    Category.STARTERS_GENERAL,
                    false,
                    null,
                    RomHandler::supportsStarterHeldItems
            ),
            new SimpleSettingDefinition<>(
                    Name.STARTERS_BAN_BAD_HELD_ITEMS,
                    Category.STARTERS_GENERAL,
                    false,
                    new SimpleSettingRestriction<>(Name.STARTERS_RANDOMIZE_HELD_ITEMS, isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.STARTERS_BST_USE_MINIMUM,
                    Category.STARTER_BSTS,
                    false,
                    anyStarterIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.STARTERS_BST_USE_MAXIMUM,
                    Category.STARTER_BSTS,
                    false,
                    anyStarterIsRandomRestriction,
                    null
            ),
            // TODO: LimitStartersMinimumBSTValue, LimitStartersMaximumBSTValue;
            //  these need a variable default value depending on RomHandler
            // TODO also: They should ideally have a special enablement constraint, such that Minimum's max is the value
            //  of Maximum, and Maximum's min is the value of Minimum.
            //  (Not sure how that can interact with defaults though. Maybe they just need to default to 0 and MAX_BST
            //  instead of their current defaults? That would also allow us to remove the UseMin, UseMax settings.)
            //  ...Yeah, actually, that suits the standard of default values being expected behavior better.

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_STATIC_ENCOUNTERS,
                    Category.STATIC_ENCOUNTERS,
                    StaticPokemonMod.UNCHANGED,
                    null,
                    RomHandler::canChangeStaticPokemon
            ),
            new SimpleSettingDefinition<>(
                    Name.STATICS_FULL_RANDOM_OVER_600_BST,
                    Category.STATIC_ENCOUNTERS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STATIC_ENCOUNTERS, StaticPokemonMod.UNCHANGED, false),
                    null
            ), //This is such a weirdly specific option...
            new SimpleSettingDefinition<>(
                    Name.STATICS_LIMIT_MAIN_GAME_LEGENDARIES,
                    Category.STATIC_ENCOUNTERS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STATIC_ENCOUNTERS, StaticPokemonMod.SIMILAR_STRENGTH),
                    RomHandler::hasMainGameLegendaries
            ),
            new SimpleSettingDefinition<>(
                    Name.STATICS_ALLOW_ALT_FORMES,
                    Category.STATIC_ENCOUNTERS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STATIC_ENCOUNTERS, StaticPokemonMod.UNCHANGED, false),
                    RomHandler::hasStarterAltFormes
            ),
            new SimpleSettingDefinition<>(
                    Name.STATICS_SWAP_MEGA_EVOLVABLES,
                    Category.STATIC_ENCOUNTERS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STATIC_ENCOUNTERS, StaticPokemonMod.UNCHANGED, false),
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    Name.STATICS_FIX_MUSIC,
                    Category.STATIC_ENCOUNTERS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_STATIC_ENCOUNTERS, StaticPokemonMod.UNCHANGED, false),
                    RomHandler::hasStaticMusicFix
            ),
            new SimpleSettingDefinition<>(
                    Name.STATICS_USE_LEVEL_MODIFIER,
                    Category.STATIC_ENCOUNTERS,
                    false,
                    null,
                    null
            ),  //TODO: remove redundant toggle
            new NumericSettingDefinition<>(
                    Name.STATICS_LEVEL_MODIFIER_PERCENT,
                    Category.STATIC_ENCOUNTERS,
                    100,
                    new SimpleSettingRestriction<>(Name.STATICS_USE_LEVEL_MODIFIER, isTrue),
                    null,
                    -100, 155
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_IN_GAME_TRADES,
                    Category.IN_GAME_TRADES,
                    InGameTradesMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TRADES_RANDOMIZE_NICKNAMES,
                    Category.IN_GAME_TRADES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_IN_GAME_TRADES, InGameTradesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TRADES_RANDOMIZE_ORIGINAL_TRAINERS,
                    Category.IN_GAME_TRADES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_IN_GAME_TRADES, InGameTradesMod.UNCHANGED, false),
                    notOfGeneration(1)
            ),
            new SimpleSettingDefinition<>(
                    Name.TRADES_RANDOMIZE_IVS,
                    Category.IN_GAME_TRADES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_IN_GAME_TRADES, InGameTradesMod.UNCHANGED, false),
                    notOfGeneration(1)
            ),
            new SimpleSettingDefinition<>(
                    Name.TRADES_RANDOMIZE_HELD_ITEMS,
                    Category.IN_GAME_TRADES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_IN_GAME_TRADES, InGameTradesMod.UNCHANGED, false),
                    notOfGeneration(1)
            )
    );

    //endregion

    //region moves & movesets

    public enum MovesetsMod {
        UNCHANGED, RANDOM_PREFER_SAME_TYPE, COMPLETELY_RANDOM, METRONOME_ONLY
    }

    private static final SettingRestriction randomPokemonMovesetsRestriction = new MultiSettingRestriction(
            true, false,
            new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_MOVESETS, MovesetsMod.RANDOM_PREFER_SAME_TYPE),
            new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_MOVESETS, MovesetsMod.COMPLETELY_RANDOM)
    );

    private static final SettingRestriction noMetronomeModeRestriction = new EnumMatchRestriction<>(
            Name.RANDOMIZE_SPECIES_MOVESETS, MovesetsMod.METRONOME_ONLY, false
    );

    public static final List<SettingDefinition<?>> MOVES_AND_MOVESETS = List.of(
            new SimpleSettingDefinition<>(
                    Name.MOVES_RANDOMIZE_POWER,
                    Category.MOVE_TRAITS,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVES_RANDOMIZE_ACCURACY,
                    Category.MOVE_TRAITS,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVES_RANDOMIZE_PP,
                    Category.MOVE_TRAITS,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVES_RANDOMIZE_CATEGORY,
                    Category.MOVE_TRAITS,
                    false,
                    null,
                    RomHandler::hasPhysicalSpecialSplit
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVES_RANDOMIZE_NAME,
                    Category.MOVE_TRAITS,
                    false,
                    null,
                    RomHandler::isEnglish
            ),
            new SimpleSettingDefinition<>(
                    Name.UPDATE_MOVES,
                    Category.MOVE_TRAITS,
                    false,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    Name.UPDATE_MOVES_TO_GENERATION,
                    Category.MOVE_TRAITS,
                    9,
                    new SimpleSettingRestriction<>(Name.UPDATE_MOVES, isTrue),
                    null,
                    2, 9,
                    null,
                    null,
                    higherValueThanGeneration(2,3,4,5,6,7),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_SPECIES_MOVESETS,
                    Category.SPECIES_MOVESETS,
                    MovesetsMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVESETS_GUARANTEE_LEVEL_1_MOVES,
                    Category.SPECIES_MOVESETS,
                    false,
                    randomPokemonMovesetsRestriction,
                    RomHandler::supportsFourStartingMoves
            ),
            new NumericSettingDefinition<>(
                    Name.MOVESETS_GUARANTEED_LEVEL_1_MOVE_COUNT,
                    Category.SPECIES_MOVESETS,
                    2,
                    new SimpleSettingRestriction<>(Name.MOVESETS_GUARANTEE_LEVEL_1_MOVES, isTrue),
                    null,
                    2, 4
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVESETS_ORDER_BY_DAMAGE,
                    Category.SPECIES_MOVESETS,
                    false,
                    randomPokemonMovesetsRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVESETS_BAN_OVERPOWERED,
                    Category.SPECIES_MOVESETS,
                    false,
                    randomPokemonMovesetsRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVESETS_FORCE_GOOD_DAMAGING,
                    Category.SPECIES_MOVESETS,
                    false,
                    randomPokemonMovesetsRestriction,
                    null
            ),
            new NumericSettingDefinition<>(
                    Name.MOVESETS_FORCE_GOOD_DAMAGING_PERCENT,
                    Category.SPECIES_MOVESETS,
                    0,
                    new SimpleSettingRestriction<>(Name.MOVESETS_FORCE_GOOD_DAMAGING, isTrue),
                    null,
                    0, 100
            ),
            new SimpleSettingDefinition<>(
                    Name.MOVESETS_GUARANTEE_EVOLUTION_MOVES,
                    Category.SPECIES_MOVESETS,
                    false,
                    randomPokemonMovesetsRestriction,
                    atLeastGeneration(7)
            )
    );

    //endregion

    //region foe pokemon

    public enum TrainersMod {
        UNCHANGED, RANDOM, DISTRIBUTED, MAINPLAYTHROUGH, TYPE_THEMED,
        TYPE_THEMED_ELITE4_GYMS, KEEP_THEMED, KEEP_THEME_OR_PRIMARY
    }

    private static final SettingRestriction anyTrainerPokemonIsRandomRestriction = new MultiSettingRestriction(
            true, false,
            new EnumMatchRestriction<>("RandomizeTrainerPokemon", TrainersMod.UNCHANGED, false),
            new SimpleSettingRestriction<>("AddPokemonToBossTrainers", isTrue),
            new SimpleSettingRestriction<>("AddPokemonToImportantTrainers", isTrue),
            new SimpleSettingRestriction<>("AddPokemonToRegularTrainers", isTrue)
    );

    private static final SettingRestriction addItemsToAnyTrainerRestriction = new MultiSettingRestriction(
            true, false,
            new SimpleSettingRestriction<>("AddHeldItemsToBossTrainers", isTrue),
            new SimpleSettingRestriction<>("AddHeldItemsToImportantTrainers", isTrue),
            new SimpleSettingRestriction<>("AddHeldItemsToRegularTrainers", isTrue)
    );

    public enum TotemPokemonMod {
        UNCHANGED, RANDOM, SIMILAR_STRENGTH
    }

    public enum AllyPokemonMod {
        UNCHANGED, RANDOM, SIMILAR_STRENGTH
    }

    public enum AuraMod {
        UNCHANGED, RANDOM, SAME_STRENGTH
    }

    public static final List<SettingDefinition<?>> FOE_POKEMON = List.of(
            new SimpleSettingDefinition<>(
                    "RandomizeTrainerPokemon",
                    "TrainerPokemon",
                    TrainersMod.UNCHANGED,
                    null,
                    null
            ),

            new SimpleSettingDefinition<>(
                    "BetterMovesetsForBossTrainers",
                    "TrainerPokemon",
                    false,
                    noMetronomeModeRestriction,
                    RomHandler::canGiveCustomMovesetsToBossTrainers
            ),
            new SimpleSettingDefinition<>(
                    "BetterMovesetsForImportantTrainers",
                    "TrainerPokemon",
                    false,
                    noMetronomeModeRestriction,
                    RomHandler::canGiveCustomMovesetsToImportantTrainers
            ),
            new SimpleSettingDefinition<>(
                    "BetterMovesetsForRegularTrainers",
                    "TrainerPokemon",
                    false,
                    noMetronomeModeRestriction,
                    RomHandler::canGiveCustomMovesetsToRegularTrainers
            ),
            new SimpleSettingDefinition<>(
                    "AdditionalPokemonForBossTrainers",
                    "TrainerPokemon",
                    false,
                    null,
                    RomHandler::canAddPokemonToBossTrainers
            ),
            new SimpleSettingDefinition<>(
                    "AdditionalPokemonForImportantTrainers",
                    "TrainerPokemon",
                    false,
                    null,
                    RomHandler::canAddPokemonToImportantTrainers
            ),
            new SimpleSettingDefinition<>(
                    "AdditionalPokemonForRegularTrainers",
                    "TrainerPokemon",
                    false,
                    null,
                    RomHandler::canAddPokemonToRegularTrainers
            ),
            new SimpleSettingDefinition<>(
                    "AddHeldItemsToBossTrainers",
                    "TrainerPokemon",
                    false,
                    null,
                    RomHandler::canAddHeldItemsToBossTrainers
            ),
            new SimpleSettingDefinition<>(
                    "AddHeldItemsToImportantTrainers",
                    "TrainerPokemon",
                    false,
                    null,
                    RomHandler::canAddHeldItemsToImportantTrainers
            ),
            new SimpleSettingDefinition<>(
                    "AddHeldItemsToRegularTrainers",
                    "TrainerPokemon",
                    false,
                    null,
                    RomHandler::canAddHeldItemsToRegularTrainers
            ),
            new SimpleSettingDefinition<>(
                    "TrainerHeldItemsConsumableOnly",
                    "TrainerPokemon",
                    false,
                    addItemsToAnyTrainerRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TrainerHeldItemsSensible",
                    "TrainerPokemon",
                    false,
                    addItemsToAnyTrainerRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TrainerHeldItemsToHighestLevelOnly",
                    "TrainerPokemon",
                    false,
                    addItemsToAnyTrainerRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "ForceDiverseTypesForBossTrainers",
                    "TrainerPokemon",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizeTrainerPokemon", TrainersMod.UNCHANGED, false),
                            new SimpleSettingRestriction<>("AddPokemonToBossTrainers", isTrue)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "ForceDiverseTypesForImportantTrainers",
                    "TrainerPokemon",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizeTrainerPokemon", TrainersMod.UNCHANGED, false),
                            new SimpleSettingRestriction<>("AddPokemonToImportantTrainers", isTrue)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "ForceDiverseTypesForRegularTrainers",
                    "TrainerPokemon",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizeTrainerPokemon", TrainersMod.UNCHANGED, false),
                            new SimpleSettingRestriction<>("AddPokemonToRegularTrainers", isTrue)),
                    null
            ),

            new SimpleSettingDefinition<>(
                    "RivalCarriesStarter",
                    "TrainerPokemon",
                    false,
                    new MultiSettingRestriction(false, false,
                            new MultiSettingRestriction(true, false,
                                    new EnumMatchRestriction<>("RandomizeStarters", StartersMod.UNCHANGED, false),
                                    new EnumMatchRestriction<>("RandomizeTrainerPokemon", TrainersMod.UNCHANGED, false)),
                            new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.RANDOM_EVERY_LEVEL, false)
                    ),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonSimilarStrength",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonAvoidDuplicates",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonWeighTypes",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonUseLocal",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonNoLegendaries",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonNoEarlyWonderGuard",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    rh -> rh.abilitiesPerSpecies() != 0
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonAllowAltFormes",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    RomHandler::hasFunctionalFormes
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonSwapMegaEvolvables",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonRandomShinies",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    atLeastGeneration(7)
            ),
            new SimpleSettingDefinition<>(
                    "PokemonLeagueHasUniquePokemon",
                    "TrainerPokemon",
                    false,
                    anyTrainerPokemonIsRandomRestriction,
                    null
            ),
            new NumericSettingDefinition<>(
                    "PokemonLeagueUniquePokemonCount",
                    "TrainerPokemon",
                    1,
                    new EnumMatchRestriction<>("RandomizeTrainerPokemon", TrainersMod.UNCHANGED, false),
                    null,
                    1, 2
            ),

            new SimpleSettingDefinition<>(
                    "TrainersEvolveTheirPokemon",
                    "TrainerPokemon",
                    false,
                    new MultiSettingRestriction(false, false,
                            notEvolveEveryLevelRestriction,
                            new EnumMatchRestriction<>("RandomizePokemonEvolutions",
                                    EvolutionsMod.RANDOM_EVERY_LEVEL, false)),
                    null
            ),
            new NumericSettingDefinition<>(
                    "TrainersEvolveTheirPokemonPercentage",
                    "TrainerPokemon",
                    100,
                    new SimpleSettingRestriction<>("TrainersEvolveTheirPokemon", isTrue),
                    null,
                    -100, 155
            ),
            new SimpleSettingDefinition<>(
                    "TrainerPokemonLevelModifier",
                    "TrainerPokemon",
                    false,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    "TrainerPokemonLevelModifierPercentage",
                    "TrainerPokemon",
                    100,
                    new SimpleSettingRestriction<>("TrainerPokemonLevelModifier", isTrue),
                    null,
                    -100, 155
            ),

            new SimpleSettingDefinition<>(
                    "RandomizeBattleStyle",
                    "TrainerPokemon",
                    BattleStyle.Modification.UNCHANGED,
                    null,
                    atLeastGeneration(3)
            ),
            new EnumSettingDefinition<>(
                    "SingleStyleForBattles",
                    "TrainerPokemon",
                    BattleStyle.Style.SINGLE_BATTLE,
                    new EnumMatchRestriction<>("RandomizeBattleStyle", BattleStyle.Modification.SINGLE_STYLE),
                    null,
                    null,
                    Map.of(
                            BattleStyle.Style.TRIPLE_BATTLE,  ofGeneration(5, 6),
                            BattleStyle.Style.ROTATION_BATTLE,  ofGeneration(5, 6)
                    )
            ),

            new SimpleSettingDefinition<>(
                    "RandomizeTrainerNames",
                    "TrainerPokemon",
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeTrainerClassNames",
                    "TrainerPokemon",
                    false,
                    null,
                    null
            ),

            new SimpleSettingDefinition<>(
                    "RandomizeTotemPokemon",
                    "TotemPokemon",
                    TotemPokemonMod.UNCHANGED,
                    null,
                    RomHandler::hasTotemPokemon
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeAllyPokemon",
                    "TotemPokemon",
                    AllyPokemonMod.UNCHANGED,
                    null,
                    RomHandler::hasTotemPokemon
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeTotemAuras",
                    "TotemPokemon",
                    AuraMod.UNCHANGED,
                    null,
                    RomHandler::hasTotemPokemon
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeTotemHeldItems",
                    "TotemPokemon",
                    false,
                    null,
                    RomHandler::hasTotemPokemon
            ),
            new SimpleSettingDefinition<>(
                    "TotemPokemonAllowAltFormes",
                    "TotemPokemon",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizeTotemPokemon", TotemPokemonMod.UNCHANGED, false),
                            new EnumMatchRestriction<>("RandomizeAllyPokemon", AllyPokemonMod.UNCHANGED, false)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TotemPokemonLevelModifier",
                    "TotemPokemon",
                    false,
                    null,
                    RomHandler::hasTotemPokemon
            ),
            new NumericSettingDefinition<>(
                    "TotemPokemonLevelModifierPercentage",
                    "TotemPokemon",
                    0,
                    new SimpleSettingRestriction<>("TotemPokemonLevelModifier", isTrue),
                    null,
                    -100, 155
            )
    );

    //endregion

    //region wild encounters

    public enum WildPokemonZoneMod {
        SINGLE_ENCOUNTER, ENCOUNTER_SET, MAP, NAMED_LOCATION, GAME
    }

    public enum WildPokemonTypeMod {
        NONE, RANDOM_THEMES, KEEP_PRIMARY
    }

    public enum WildPokemonEvolutionMod {
        NONE, BASIC_ONLY, KEEP_STAGE
    }

    public enum CatchRateMod {
        // replaces the numeric (but described with names) catch rates of earlier
        // Randomizer versions
        UNCHANGED, STANDARDIZED, BUFFED, SUPER, ULTRA, GUARANTEED
    }

    public static final List<SettingDefinition<?>> WILD_POKEMON = List.of(
            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_WILD_ENCOUNTERS,
                    Category.WILD_GENERAL,
                    false,
                    null,
                    null
            ),

            new EnumSettingDefinition<>(
                    Name.WILD_REPLACEMENT_ZONE,
                    Category.WILD_REPLACEMENT_ZONE,
                    WildPokemonZoneMod.SINGLE_ENCOUNTER,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                    null,
                    null,
                    Map.of(
                            WildPokemonZoneMod.ENCOUNTER_SET, rh -> !rh.hasMapIndices(),
                            WildPokemonZoneMod.MAP, RomHandler::hasMapIndices,
                            WildPokemonZoneMod.NAMED_LOCATION, RomHandler::hasEncounterLocations
                    )
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_SPLIT_REPLACEMENT_ZONE_BY_ENCOUNTER_TYPES,
                    Category.WILD_REPLACEMENT_ZONE,
                    false,
                    new MultiSettingRestriction(false, false,
                            new EnumMatchRestriction<>(Name.WILD_REPLACEMENT_ZONE, WildPokemonZoneMod.SINGLE_ENCOUNTER, false),
                            new EnumMatchRestriction<>(Name.WILD_REPLACEMENT_ZONE, WildPokemonZoneMod.ENCOUNTER_SET, false)),
                    null
            ),
            new SimpleSettingDefinition<>( // this setting is definitely zone-y
                    Name.WILD_REMOVE_TIME_BASED,
                    Category.WILD_REPLACEMENT_ZONE,
                    true,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                    RomHandler::hasTimeBasedEncounters
            ),

            new EnumSettingDefinition<>(
                    Name.WILD_TYPE_RESTRICTION,
                    Category.WILD_TYPES,
                    WildPokemonTypeMod.NONE,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                    null,
                    Map.of(WildPokemonTypeMod.RANDOM_THEMES,
                            new EnumMatchRestriction<>(Name.WILD_REPLACEMENT_ZONE, WildPokemonZoneMod.GAME, false)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_KEEP_TYPE_THEMES,
                    Category.WILD_TYPES,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.WILD_EVOLUTION_RESTRICTION,
                    Category.WILD_EVOLUTIONS,
                    WildPokemonEvolutionMod.NONE,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_EVOLUTION_KEEP_RELATIONS,
                    Category.WILD_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Name.WILD_REPLACEMENT_ZONE, WildPokemonZoneMod.SINGLE_ENCOUNTER, false),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.WILD_NO_LEGENDARIES,
                    Category.WILD_GENERAL,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_ALLOW_ALT_FORMES,
                    Category.WILD_GENERAL,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                    RomHandler::hasWildAltFormes
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_USE_SIMILAR_STRENGTH,
                    Category.WILD_GENERAL,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_SIMILAR_STRENGTH_BALANCE_LOW_LEVEL,
                    Category.WILD_GENERAL,
                    false,
                    new SimpleSettingRestriction<>(Name.WILD_USE_SIMILAR_STRENGTH, isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_CATCH_EM_ALL,
                    Category.WILD_GENERAL,
                    false,
                    new MultiSettingRestriction(false, false,
                            new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                            new MultiSettingRestriction(true, false,
                                    new EnumMatchRestriction<>(Name.WILD_REPLACEMENT_ZONE, WildPokemonZoneMod.GAME, false),
                                    new SimpleSettingRestriction<>(Name.WILD_SPLIT_REPLACEMENT_ZONE_BY_ENCOUNTER_TYPES, isTrue))
                    ),
                    null
            ),

            // Below: "wild pokemon" settings that don't require random wild pokemon
            new SimpleSettingDefinition<>(
                    Name.WILD_MINIMUM_CATCH_RATE_SELECTION,
                    Category.WILD_POST_TWEAKS,
                    CatchRateMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_RANDOMIZE_HELD_ITEMS,
                    Category.WILD_POST_TWEAKS,
                    false,
                    null,
                    notOfGeneration(1)
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_HELD_ITEMS_BAN_MINOR,
                    Category.WILD_POST_TWEAKS,
                    false,
                    new SimpleSettingRestriction<>(Name.WILD_RANDOMIZE_HELD_ITEMS, isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.WILD_USE_LEVEL_MODIFIER,
                    Category.WILD_POST_TWEAKS,
                    false,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    Name.WILD_LEVEL_MODIFIER_PERCENT,
                    Category.WILD_POST_TWEAKS,
                    100,
                    new SimpleSettingRestriction<>(Name.WILD_USE_LEVEL_MODIFIER, isTrue),
                    null,
                    -100, 155
            )
    );

    //endregion

    //region move teachers

    public enum TMMovesMod {
        UNCHANGED, RANDOM
    }

    public enum TMsHMsCompatibilityMod {
        UNCHANGED, RANDOM_PREFER_TYPE, COMPLETELY_RANDOM, FULL
    }

    public enum MoveTutorMovesMod {
        UNCHANGED, RANDOM
    }

    public enum MoveTutorsCompatibilityMod {
        UNCHANGED, RANDOM_PREFER_TYPE, COMPLETELY_RANDOM, FULL
    }

    public static final List<SettingDefinition<?>> TMS_HMS_AND_TUTORS = List.of(
            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_TM_MOVES,
                    Category.TM_MOVES,
                    TMMovesMod.UNCHANGED,
                    noMetronomeModeRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TMS_BAN_OVERPOWERED,
                    Category.TM_MOVES,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TM_MOVES,
                            doesNotMatchEnumValue(TMMovesMod.UNCHANGED)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TMS_KEEP_FIELD_MOVES,
                    Category.TM_MOVES,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TM_MOVES,
                            doesNotMatchEnumValue(TMMovesMod.UNCHANGED)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TMS_FORCE_GOOD_DAMAGING,
                    Category.TM_MOVES,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TM_MOVES,
                            doesNotMatchEnumValue(TMMovesMod.UNCHANGED)),
                    null
            ),
            new NumericSettingDefinition<>(
                    Name.TMS_GOOD_DAMAGING_PERCENT,
                    Category.TM_MOVES,
                    0,
                    new SimpleSettingRestriction<>(Name.TMS_FORCE_GOOD_DAMAGING, isTrue),
                    null,
                    0, 100
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                    Category.TM_AND_HM_COMPATABILITY,
                    TMsHMsCompatibilityMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TM_COMPATABILITY_LEVEL_UP_SANITY,
                    Category.TM_AND_HM_COMPATABILITY,
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_MOVESETS, MovesetsMod.UNCHANGED, false),
                            new EnumMatchRestriction<>(Name.RANDOMIZE_TM_MOVES, TMMovesMod.UNCHANGED, false),
                            new EnumMatchRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY, TMsHMsCompatibilityMod.COMPLETELY_RANDOM),
                            new EnumMatchRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY, TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TM_COMPATABILITY_FOLLOW_EVOLUTIONS,
                    Category.TM_AND_HM_COMPATABILITY,
                    false,
                    new MultiSettingRestriction(false, false,
                            notEvolveEveryLevelRestriction,
                            new MultiSettingRestriction(true, false,
                                    new EnumMatchRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                                            TMsHMsCompatibilityMod.COMPLETELY_RANDOM),
                                    new EnumMatchRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                                            TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE),
                                    new SimpleSettingRestriction<>(Name.TM_COMPATABILITY_LEVEL_UP_SANITY, isTrue))
                    ),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TMS_FULL_HM_COMPATABILITY,
                    Category.TM_AND_HM_COMPATABILITY,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY, TMsHMsCompatibilityMod.FULL, false),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_TUTOR_MOVES,
                    Category.MOVE_TUTOR_MOVES,
                    MoveTutorMovesMod.UNCHANGED,
                    noMetronomeModeRestriction,
                    RomHandler::hasMoveTutors
            ),
            new SimpleSettingDefinition<>(
                    Name.TUTORS_BAN_OVERPOWERED,
                    Category.MOVE_TUTOR_MOVES,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TUTOR_MOVES,
                            doesNotMatchEnumValue(MoveTutorMovesMod.UNCHANGED)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TUTORS_KEEP_FIELD_MOVES,
                    Category.MOVE_TUTOR_MOVES,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TUTOR_MOVES,
                            doesNotMatchEnumValue(MoveTutorMovesMod.UNCHANGED)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TUTORS_FORCE_GOOD_DAMAGING,
                    Category.MOVE_TUTOR_MOVES,
                    false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TUTOR_MOVES,
                            doesNotMatchEnumValue(MoveTutorMovesMod.UNCHANGED)),
                    null
            ),
            new NumericSettingDefinition<>(
                    Name.TUTORS_GOOD_DAMAGING_PERCENT,
                    Category.MOVE_TUTOR_MOVES,
                    0,
                    new SimpleSettingRestriction<>(Name.TUTORS_FORCE_GOOD_DAMAGING, isTrue),
                    null,
                    0, 100
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_TUTOR_COMPATABILITY,
                    Category.MOVE_TUTOR_COMPATABILITY,
                    MoveTutorsCompatibilityMod.UNCHANGED,
                    null,
                    RomHandler::hasMoveTutors
            ),
            new SimpleSettingDefinition<>(
                    Name.TUTOR_COMPATABILITY_LEVEL_UP_SANITY,
                    Category.MOVE_TUTOR_COMPATABILITY,
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_MOVESETS, MovesetsMod.UNCHANGED, false),
                            new EnumMatchRestriction<>(Name.RANDOMIZE_TUTOR_MOVES, MoveTutorMovesMod.UNCHANGED, false),
                            new EnumMatchRestriction<>(Name.RANDOMIZE_TUTOR_COMPATABILITY, MoveTutorsCompatibilityMod.COMPLETELY_RANDOM),
                            new EnumMatchRestriction<>(Name.RANDOMIZE_TUTOR_COMPATABILITY, MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.TUTOR_COMPATABILITY_FOLLOW_EVOLUTIONS,
                    Category.MOVE_TUTOR_COMPATABILITY,
                    false,
                    new MultiSettingRestriction(false, false,
                            notEvolveEveryLevelRestriction,
                            new MultiSettingRestriction(true, false,
                                    new EnumMatchRestriction<>(Name.RANDOMIZE_TUTOR_COMPATABILITY,
                                            MoveTutorsCompatibilityMod.COMPLETELY_RANDOM),
                                    new EnumMatchRestriction<>(Name.RANDOMIZE_TUTOR_COMPATABILITY,
                                            MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE),
                                    new SimpleSettingRestriction<>(Name.TUTOR_COMPATABILITY_LEVEL_UP_SANITY, isTrue))
                    ),
                    null
            )
    );

    //endregion

    //region items

    //To consider: Should held items (wild and/or trainer) be in this supercategory?

    public enum FieldItemsMod {
        UNCHANGED, SHUFFLE, RANDOM, RANDOM_EVEN
    }

    public enum ShopItemsMod {
        UNCHANGED, SHUFFLE, RANDOM
    }

    public enum PickupItemsMod {
        UNCHANGED, RANDOM
    }

    public static final List<SettingDefinition<?>> ITEMS = List.of(
            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_FIELD_ITEMS,
                    Category.FIELD_ITEMS,
                    FieldItemsMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.FIELD_ITEMS_BAN_MINOR,
                    Category.FIELD_ITEMS,
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>(Name.RANDOMIZE_FIELD_ITEMS, FieldItemsMod.RANDOM),
                            new EnumMatchRestriction<>(Name.RANDOMIZE_FIELD_ITEMS, FieldItemsMod.RANDOM_EVEN)),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_SPECIAL_SHOP_ITEMS,
                    Category.SPECIAL_SHOP_ITEMS,
                    ShopItemsMod.UNCHANGED,
                    null,
                    RomHandler::hasShopSupport
            ),
            new SimpleSettingDefinition<>(
                    Name.SHOP_ITEMS_BAN_MINOR,
                    Category.SPECIAL_SHOP_ITEMS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SHOP_ITEMS_BAN_REGULAR_SHOP_ITEMS,
                    Category.SPECIAL_SHOP_ITEMS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SHOP_ITEMS_BAN_OVERPOWERED,
                    Category.SPECIAL_SHOP_ITEMS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SHOP_ITEMS_GUARANTEE_EVOLUTION_ITEMS,
                    Category.SPECIAL_SHOP_ITEMS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SHOP_ITEMS_GUARANTEE_X_ITEMS,
                    Category.SPECIAL_SHOP_ITEMS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.SHOP_ITEMS_BALANCE_PRICES,
                    Category.SHOP_ITEMS_GENERAL,
                    false,
                    null,
                    RomHandler::hasShopSupport
            ),
            new SimpleSettingDefinition<>(
                    Name.SHOP_ITEMS_ADD_CHEAP_RARE_CANDY,
                    Category.SHOP_ITEMS_GENERAL,
                    false,
                    null,
                    RomHandler::canChangeShopSizes
            ),

            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_PICKUP_ITEMS,
                    Category.PICKUP_ITEMS,
                    PickupItemsMod.UNCHANGED,
                    null,
                    rh -> rh.abilitiesPerSpecies() > 0
            ),
            new SimpleSettingDefinition<>(
                    Name.PICKUP_ITEMS_BAN_MINOR,
                    Category.PICKUP_ITEMS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_PICKUP_ITEMS, PickupItemsMod.RANDOM),
                    null
            )
    );

    //endregion

    //region types

    public enum TypeEffectivenessMod {
        UNCHANGED, RANDOM, RANDOM_BALANCED, KEEP_IDENTITIES, INVERSE
    }

    public static final List<SettingDefinition<?>> TYPES = List.of(
            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_TYPE_EFFECTIVENESS,
                    Category.TYPE_EFFECTIVENESS,
                    TypeEffectivenessMod.UNCHANGED,
                    null,
                    RomHandler::hasTypeEffectivenessSupport
            ),
            new SimpleSettingDefinition<>(
                    Name.TYPE_INVERSE_ADD_RANDOM_IMMUNITIES,
                    Category.TYPE_EFFECTIVENESS,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_TYPE_EFFECTIVENESS, TypeEffectivenessMod.INVERSE),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.UPDATE_TYPE_EFFECTIVENESS,
                    Category.TYPE_EFFECTIVENESS,
                    false,
                    null,
                    rh -> (rh.hasTypeEffectivenessSupport()
                            && rh.generationOfPokemon() < TypeEffectivenessUpdater.UPDATE_TO_GEN)
            )
    );

    //endregion

    //region graphics

    public enum PokemonPalettesMod {
        UNCHANGED, RANDOM
    }

    public static final List<SettingDefinition<?>> GRAPHICS = List.of(
            new SimpleSettingDefinition<>(
                    Name.RANDOMIZE_SPECIES_PALETTES,
                    Category.SPECIES_PALETTES,
                    PokemonPalettesMod.UNCHANGED,
                    null,
                    RomHandler::hasPokemonPaletteSupport
            ),
            new SimpleSettingDefinition<>(
                    Name.PALETTES_FOLLOW_TYPES,
                    Category.SPECIES_PALETTES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_PALETTES, PokemonPalettesMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.PALETTES_FOLLOW_EVOLUTIONS,
                    Category.SPECIES_PALETTES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_PALETTES, PokemonPalettesMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Name.PALETTES_SHINY_FROM_NORMAL,
                    Category.SPECIES_PALETTES,
                    false,
                    new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_PALETTES, PokemonPalettesMod.RANDOM),
                    notOfGeneration(1)
            )

            // TODO: what to do with CPGs? Should they be included here?
    );

    //endregion graphics

    //region misc tweaks

    public static final List<SettingDefinition<?>> MISC_TWEAKS = List.of(
            miscTweakDefinition(Name.TWEAK_USE_SCALED_EXPERIENCE, MiscTweak.BW_EXP_PATCH),
            miscTweakDefinition(Name.TWEAK_NERF_X_ACCURACY, MiscTweak.NERF_X_ACCURACY),
            miscTweakDefinition(Name.TWEAK_UPDATE_CRIT_RATE, MiscTweak.UPDATE_CRIT_RATE),
            miscTweakDefinition(Name.TWEAK_FASTEST_TEXT, MiscTweak.FASTEST_TEXT),
            miscTweakDefinition(Name.TWEAK_RUN_INDOORS, MiscTweak.RUNNING_SHOES_INDOORS),
            miscTweakDefinition(Name.TWEAK_RANDOMIZE_PC_POTION, MiscTweak.RANDOMIZE_PC_POTION),
            miscTweakDefinition(Name.TWEAK_ALLOW_PIKACHU_EVOLUTION, MiscTweak.ALLOW_PIKACHU_EVOLUTION),
            miscTweakDefinition(Name.TWEAK_NATIONAL_DEX_AT_START, MiscTweak.NATIONAL_DEX_AT_START),
            miscTweakDefinition(Name.TWEAK_FAST_EGG_HATCHING, MiscTweak.FAST_EGG_HATCHING),
            miscTweakDefinition(Name.TWEAK_FORCE_CHALLENGE_MODE, MiscTweak.FORCE_CHALLENGE_MODE),
            miscTweakDefinition(Name.TWEAK_LOWER_CASE_SPECIES_NAMES, MiscTweak.LOWER_CASE_POKEMON_NAMES),
            miscTweakDefinition(Name.TWEAK_RANDOMIZE_CATCHING_TUTORIAL, MiscTweak.RANDOMIZE_CATCHING_TUTORIAL),
            miscTweakDefinition(Name.TWEAK_BAN_LUCKY_EGG, MiscTweak.BAN_LUCKY_EGG),
            miscTweakDefinition(Name.TWEAK_NO_FREE_LUCKY_EGG, MiscTweak.NO_FREE_LUCKY_EGG),
            miscTweakDefinition(Name.TWEAK_BAN_BIG_MONEY_ITEMS, MiscTweak.BAN_BIG_MANIAC_ITEMS),
            miscTweakDefinition(Name.TWEAK_ALL_WILD_POKEMON_CALL_ALLIES, MiscTweak.SOS_BATTLES_FOR_ALL),
            miscTweakDefinition(Name.TWEAK_BALANCE_FOSSIL_LEVELS, MiscTweak.BALANCE_STATIC_LEVELS),
            miscTweakDefinition(Name.TWEAK_RETAIN_TEMPORARY_FORMES, MiscTweak.RETAIN_ALT_FORMES),
            miscTweakDefinition(Name.TWEAK_RUN_WITHOUT_RUNNING_SHOES, MiscTweak.RUN_WITHOUT_RUNNING_SHOES),
            miscTweakDefinition(Name.TWEAK_FASTER_BARS, MiscTweak.FASTER_HP_AND_EXP_BARS),
            miscTweakDefinition(Name.TWEAK_FAST_DISTORTION_WORLD, MiscTweak.FAST_DISTORTION_WORLD),
            miscTweakDefinition(Name.TWEAK_UPDATE_ROTOM_TYPING, MiscTweak.UPDATE_ROTOM_FORME_TYPING),
            miscTweakDefinition(Name.TWEAK_DISABLE_LOW_HP_MUSIC, MiscTweak.DISABLE_LOW_HP_MUSIC),
            miscTweakDefinition(Name.TWEAK_REUSABLE_TMS, MiscTweak.REUSABLE_TMS),
            miscTweakDefinition(Name.TWEAK_FORGETTABLE_HMS, MiscTweak.FORGETTABLE_HMS),
            miscTweakDefinition(Name.TWEAK_NO_EV_GAIN, MiscTweak.NO_EV_YIELDS)
    );

    //endregion

    static {
        List<SettingDefinition<?>> all = new ArrayList<>(GENERAL_OPTIONS);
        all.addAll(SPECIES_TRAITS);
        all.addAll(STARTERS_STATICS_AND_TRADES);
        all.addAll(MOVES_AND_MOVESETS);
        all.addAll(FOE_POKEMON);
        all.addAll(WILD_POKEMON);
        all.addAll(TMS_HMS_AND_TUTORS);
        all.addAll(ITEMS);
        all.addAll(TYPES);
        all.addAll(GRAPHICS);
        all.addAll(MISC_TWEAKS);
        ALL_SETTINGS = Collections.unmodifiableList(all);

        List<SettingDefinition<?>> removed = new ArrayList<>();
        REMOVED_SETTINGS = Collections.unmodifiableList(removed);
    }

}
