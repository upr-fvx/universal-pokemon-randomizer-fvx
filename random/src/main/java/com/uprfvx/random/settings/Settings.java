package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.*;
import com.uprfvx.random.settings.restrictions.*;
import com.uprfvx.random.updaters.TypeEffectivenessUpdater;
import com.uprfvx.romio.MiscTweak;
import com.uprfvx.romio.gamedata.BattleStyle;
import com.uprfvx.romio.gamedata.ExpCurve;
import com.uprfvx.romio.gamedata.basestats.BaseStats;
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
// "Pokemon Base Stats" or "Pokemon Traits".)
//Supercategories should be the tabs of the GUI.
//Intermediate categories are skipped.

// A Setting in its default state should do NOTHING.
// In other words, it should not change any aspect of the game going through the Randomizer.
// Something like "give all Trainer Pokémon a +0% level boost" counts as doing nothing,
// as long as there are no side effects. Following this is the responsibility of outside classes.
// (NoRandomIntroMon is possibly an exception to this rule, since the randomized mon
//  acts as confirmation the Randomizer has been applied) // TODO: should it be?
// A Setting in any other state should do SOMETHING.
// An exception to these rules is when a Setting A in a non-default state enables
// Setting B which has a default state that does something. In this case,
// it is acceptable that the default state of Setting B does something,
// and that the non-default state of Setting A does nothing (directly, enabling Setting B not counted).
// If the default state of A enables B, or the default state of B does nothing,
// these are bad settings. Change or remove either of them.
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

        // *** GENERAL ***
        //General options
        NO_IRREGULAR_ALT_FORMES, NO_PREMATURE_EVOLUTIONS, NO_RANDOM_INTRO_MON, RACE_MODE,
        //Limit Pokemon
        LIMIT_BAN_GENERATION_1, LIMIT_BAN_GENERATION_2, LIMIT_BAN_GENERATION_3, LIMIT_BAN_GENERATION_4,
        LIMIT_BAN_GENERATION_5, LIMIT_BAN_GENERATION_6, LIMIT_BAN_GENERATION_7, LIMIT_ALLOW_RELATIVES,

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
        STARTERS_BST_MINIMUM, STARTERS_BST_MAXIMUM,
        //In-Game Trades
        RANDOMIZE_IN_GAME_TRADES, TRADES_RANDOMIZE_NICKNAMES, TRADES_RANDOMIZE_ORIGINAL_TRAINERS,
        TRADES_RANDOMIZE_IVS, TRADES_RANDOMIZE_HELD_ITEMS,

        // *** MOVES AND MOVESETS ***
        //Move Traits
        MOVES_RANDOMIZE_POWER, MOVES_RANDOMIZE_ACCURACY, MOVES_RANDOMIZE_PP, MOVES_RANDOMIZE_TYPE,
        MOVES_RANDOMIZE_CATEGORY, MOVES_RANDOMIZE_NAME, UPDATE_MOVES, UPDATE_MOVES_TO_GENERATION,
        //Species' Movesets
        RANDOMIZE_SPECIES_MOVESETS, MOVESETS_GUARANTEED_LEVEL_1_MOVE_COUNT,
        MOVESETS_ORDER_BY_DAMAGE, MOVESETS_BAN_OVERPOWERED,
        MOVESETS_FORCE_GOOD_DAMAGING_PERCENT, MOVESETS_GUARANTEE_EVOLUTION_MOVES,

        // *** FOE POKEMON ***
        //Trainers General
        RANDOMIZE_TRAINER_POKEMON, TRAINERS_RIVAL_CARRIES_STARTER, TRAINERS_USE_SIMILAR_STRENGTH,
        TRAINERS_AVOID_DUPLICATES, TRAINERS_WEIGHT_TYPES, TRAINERS_USE_LOCAL, TRAINERS_NO_LEGENDARIES,
        TRAINERS_NO_EARLY_WONDER_GUARD, TRAINERS_ALLOW_ALT_FORMES, TRAINERS_SWAP_MEGA_EVOLVABLES,
        TRAINERS_POKEMON_LEAGUE_UNIQUE_COUNT, TRAINERS_EVOLVE_POKEMON,
        TRAINERS_EVOLVE_LEVEL_PERCENT_MODIFIER, TRAINERS_LEVEL_MODIFIER_PERCENT,
        //That's a lot for one category. We should probably organize these more. TODO: that
        //Trainer Movesets
        TRAINERS_BETTER_MOVESETS_FOR_BOSSES, TRAINERS_BETTER_MOVESETS_FOR_IMPORTANT,
        TRAINERS_BETTER_MOVESETS_FOR_REGULAR,
        //Trainers Additional Pokemon
        TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT, TRAINERS_IMPORTANT_ADDITIONAL_POKEMON_COUNT,
        TRAINERS_REGULAR_ADDITIONAL_POKEMON_COUNT,
        //Trainers Held Items
        TRAINERS_ADD_HELD_ITEMS_TO_BOSSES, TRAINERS_ADD_HELD_ITEMS_TO_IMPORTANT, TRAINERS_ADD_HELD_ITEMS_TO_REGULAR,
        TRAINERS_HELD_ITEMS_CONSUMABLE_ONLY, TRAINER_HELD_ITEMS_SENSIBLE_ONLY, TRAINERS_HELD_ITEMS_ACES_ONLY,
        //Trainers Diverse Types
        TRAINERS_BOSSES_USE_DIVERSE_TYPES, TRAINERS_IMPORTANT_USE_DIVERSE_TYPES, TRAINERS_REGULAR_USE_DIVERSE_TYPES,
        //Trainers Cosmetic
        TRAINERS_RANDOM_SHINY_POKEMON, TRAINERS_RANDOMIZE_NAMES, TRAINERS_RANDOMIZE_CLASS_NAMES,
        //Trainers Battle Styles
        TRAINERS_RANDOMIZE_BATTLE_STYLE, TRAINERS_SINGLE_STYLE_SELECTION,
        //Totem Pokemon
        RANDOMIZE_TOTEM_POKEMON, TOTEMS_RANDOMIZE_ALLIES, TOTEMS_RANDOMIZE_AURAS, TOTEMS_RANDOMIZE_HELD_ITEMS,
        TOTEMS_ALLOW_ALT_FORMES, TOTEMS_LEVEL_MODIFIER_PERCENT,

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
        WILD_MINIMUM_CATCH_RATE_SELECTION, WILD_RANDOMIZE_HELD_ITEMS,
        WILD_HELD_ITEMS_BAN_MINOR, WILD_LEVEL_MODIFIER_PERCENT,
        //Static Encounters
        RANDOMIZE_STATIC_ENCOUNTERS, STATICS_FULL_RANDOM_OVER_600_BST, STATICS_LIMIT_MAIN_GAME_LEGENDARIES,
        STATICS_ALLOW_ALT_FORMES, STATICS_SWAP_MEGA_EVOLVABLES, STATICS_FIX_MUSIC,
        STATICS_LEVEL_MODIFIER_PERCENT,

        // *** MOVE TEACHERS ***
        //TM Moves
        RANDOMIZE_TM_MOVES, TMS_BAN_OVERPOWERED, TMS_KEEP_FIELD_MOVES, TMS_GOOD_DAMAGING_PERCENT,
        //TM And HM Compatability
        RANDOMIZE_TM_AND_HM_COMPATABILITY, TM_COMPATABILITY_LEVEL_UP_SANITY, TM_COMPATABILITY_FOLLOW_EVOLUTIONS,
        TMS_FULL_HM_COMPATABILITY,
        //Move Tutor Moves
        RANDOMIZE_TUTOR_MOVES, TUTORS_BAN_OVERPOWERED, TUTORS_KEEP_FIELD_MOVES, TUTORS_GOOD_DAMAGING_PERCENT,
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
        TWEAK_DISABLE_LOW_HP_MUSIC, TWEAK_REUSABLE_TMS, TWEAK_FORGETTABLE_HMS, TWEAK_NO_EV_GAIN
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

        // *** MOVES AND MOVESETS ***
        MOVE_TRAITS, SPECIES_MOVESETS,
        //It didn't occur to me until naming this category but... should SPECIES_MOVESETS be in SPECIES_TRAITS?
        //(I mean, there's no room for it, so probably not, but logically... kinda?)

        // *** FOE POKEMON ***
        TRAINERS_GENERAL, TRAINERS_MOVESETS, TRAINERS_ADDITIONAL_POKEMON, TRAINERS_HELD_ITEMS, TRAINERS_DIVERSE_TYPES,
        TRAINERS_BATTLE_STYLE, TRAINERS_COSMETIC, TOTEM_POKEMON,

        // *** WILD ENCOUNTERS ***
        WILD_GENERAL, WILD_REPLACEMENT_ZONE, WILD_TYPES, WILD_EVOLUTIONS, WILD_POST_TWEAKS, STATIC_ENCOUNTERS,

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
                STARTER_BSTS, IN_GAME_TRADES);
        public static final List<Category> MOVES_AND_MOVESETS = List.of(MOVE_TRAITS, SPECIES_MOVESETS);
        public static final List<Category> WILD_ENCOUNTERS = List.of(WILD_GENERAL, WILD_REPLACEMENT_ZONE, WILD_TYPES,
                WILD_EVOLUTIONS, WILD_POST_TWEAKS, STATIC_ENCOUNTERS);
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
    public static final SettingRestriction notEvolveEveryLevelRestriction = new SimpleSettingRestriction<>(
            Name.RANDOMIZE_SPECIES_EVOLUTIONS, notMatchesEnum(EvolutionsMod.RANDOM_EVERY_LEVEL)
    );

    public static final List<SettingDefinition<?>> GENERAL_OPTIONS = List.of(
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.NO_RANDOM_INTRO_MON,
                    Category.GENERAL_OPTIONS)
                    //TODO: move to misc. tweaks?
                    // (I guess there's an internal reason it isn't, but from a user perspective there's no clear reason)
                    .build(),

            //TODO: make this setting actually work?
            // "this setting" is race mode?
            // I believe I was referring to "NoRandomIntroMon" but I cannot recall for sure anymore.
            //TODO investigate this todo i guess
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.RACE_MODE,
                    Category.GENERAL_OPTIONS)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.NO_IRREGULAR_ALT_FORMES,
                    Category.GENERAL_OPTIONS)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.NO_PREMATURE_EVOLUTIONS,
                    Category.GENERAL_OPTIONS)
                    .prerequisite(notEvolveEveryLevelRestriction)
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.LIMIT_BAN_GENERATION_1,
                    Category.LIMIT_POKEMON)
                    .supported(atLeastGeneration(2))  //There's no sense letting a user ban ALL Pokémon
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.LIMIT_BAN_GENERATION_2,
                    Category.LIMIT_POKEMON)
                    .supported(atLeastGeneration(2))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.LIMIT_BAN_GENERATION_3,
                    Category.LIMIT_POKEMON)
                    .supported(atLeastGeneration(3))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.LIMIT_BAN_GENERATION_4,
                    Category.LIMIT_POKEMON)
                    .supported(atLeastGeneration(4))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.LIMIT_BAN_GENERATION_5,
                    Category.LIMIT_POKEMON)
                    .supported(atLeastGeneration(5))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.LIMIT_BAN_GENERATION_6,
                    Category.LIMIT_POKEMON)
                    .supported(atLeastGeneration(6))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.LIMIT_BAN_GENERATION_7,
                    Category.LIMIT_POKEMON)
                    .supported(atLeastGeneration(7))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.LIMIT_ALLOW_RELATIVES,
                    Category.LIMIT_POKEMON)
                    .supported(atLeastGeneration(2))
                    .prerequisite(new MultiSettingRestriction(true, false,
                            new SimpleSettingRestriction<>(Name.LIMIT_BAN_GENERATION_1, isTrue),
                            new SimpleSettingRestriction<>(Name.LIMIT_BAN_GENERATION_2, isTrue),
                            new SimpleSettingRestriction<>(Name.LIMIT_BAN_GENERATION_3, isTrue),
                            new SimpleSettingRestriction<>(Name.LIMIT_BAN_GENERATION_4, isTrue),
                            new SimpleSettingRestriction<>(Name.LIMIT_BAN_GENERATION_5, isTrue),
                            new SimpleSettingRestriction<>(Name.LIMIT_BAN_GENERATION_6, isTrue),
                            new SimpleSettingRestriction<>(Name.LIMIT_BAN_GENERATION_7, isTrue)))
                    .build()
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
            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                    Category.SPECIES_BASE_STATISTIC_TOTALS,
                    BSTMod.UNCHANGED)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE,
                    Category.SPECIES_BASE_STATISTIC_TOTALS,
                    1,
                    1, 50)
                    .prerequisite(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, matchesEnum(BSTMod.RANDOM_BUFF_NERF))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_BSTS_FOLLOW_EVOLUTION,
                    Category.SPECIES_BASE_STATISTIC_TOTALS)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                                                    matchesEnum(BSTMod.RANDOM_BUFF_NERF)),
                                            new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                                                    matchesEnum(BSTMod.SHUFFLE)))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_BST_SHUFFLE_LEGENDARIES_SEPARATELY,
                    Category.SPECIES_BASE_STATISTIC_TOTALS)
                    .prerequisite(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, matchesEnum(BSTMod.SHUFFLE))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS,
                    Category.SPECIES_BASE_STATISTIC_DISTRIBUTION,
                    BaseStatDistributionsMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS,
                    Category.SPECIES_BASE_STATISTIC_DISTRIBUTION)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS,
                                            notMatchesEnum(BaseStatDistributionsMod.UNCHANGED))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_MEGA_EVOLUTIONS,
                    Category.SPECIES_BASE_STATISTIC_DISTRIBUTION)
                    .prerequisite(Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS, isTrue)
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_STAT_DISTRIBUTIONS_ASSIGN_EVO_STATS_RANDOMLY,
                    Category.SPECIES_BASE_STATISTIC_DISTRIBUTION)
                    .prerequisite(Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS, isTrue)
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.UPDATE_SPECIES_BASE_STATS,
                    Category.SPECIES_UPDATE_BASE_STATISTICS)
                    .supported(notOfGeneration(1))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.SPECIES_UPDATE_BASE_STATS_TO_GENERATION,
                    Category.SPECIES_UPDATE_BASE_STATISTICS,
                    9,
                    6, 9)
                    .prerequisite(Name.UPDATE_SPECIES_BASE_STATS, isTrue)
                    .supportedMinimums(rh -> Math.max(6, rh.generationOfPokemon() + 1))
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    Name.RANDOMIZE_SPECIES_TYPES,
                    Category.SPECIES_TYPES,
                    SpeciesTypesMod.UNCHANGED)
                    .restrictedStates(Map.of(SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS, notEvolveEveryLevelRestriction))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_TYPES_FORCE_DUAL_TYPES,
                    Category.SPECIES_TYPES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_TYPES, notMatchesEnum(SpeciesTypesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_TYPES_FOLLOW_MEGA_EVOLUTIONS,
                    Category.SPECIES_TYPES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_TYPES, matchesEnum(SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS))
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_SPECIES_ABILITIES,
                    Category.SPECIES_ABILITIES,
                    AbilitiesMod.UNCHANGED)
                    .supported(rh -> rh.abilitiesPerSpecies() != 0)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_ABILITIES_FOLLOW_EVOLUTIONS,
                    Category.SPECIES_ABILITIES)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_ABILITIES,
                                            notMatchesEnum(AbilitiesMod.UNCHANGED))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_ABILITIES_FOLLOW_MEGA_EVOLUTIONS,
                    Category.SPECIES_ABILITIES)
                    .prerequisite(Name.SPECIES_ABILITIES_FOLLOW_EVOLUTIONS, isTrue)
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_ABILITIES_COMBINE_DUPLICATES,
                    Category.SPECIES_ABILITIES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_ALWAYS_HAVE_TWO_ABILITIES,
                    Category.SPECIES_ABILITIES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_ABILITIES_BAN_WONDER_GUARD,
                    Category.SPECIES_ABILITIES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_ABILITIES_BAN_TRAPPING,
                    Category.SPECIES_ABILITIES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_ABILITIES_BAN_NEGATIVE,
                    Category.SPECIES_ABILITIES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_ABILITIES_BAN_MINOR,
                    Category.SPECIES_ABILITIES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    Name.RANDOMIZE_SPECIES_EVOLUTIONS,
                    Category.SPECIES_EVOLUTIONS,
                    EvolutionsMod.UNCHANGED)
                    .supportedStates(Map.of(EvolutionsMod.RANDOM_EVERY_LEVEL, RomHandler::canGiveEverySpeciesOneEvolutionEach))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_USE_SIMILAR_STRENGTH,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(Name.RANDOMIZE_SPECIES_EVOLUTIONS, matchesEnum(EvolutionsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_STAGES_MUST_SHARE_TYPE,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(Name.RANDOMIZE_SPECIES_EVOLUTIONS, notMatchesEnum(EvolutionsMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_MAX_THREE_STAGES,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(Name.RANDOMIZE_SPECIES_EVOLUTIONS, matchesEnum(EvolutionsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_NO_CONVERGENCE,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(Name.RANDOMIZE_SPECIES_EVOLUTIONS, notMatchesEnum(EvolutionsMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_FORCE_CHANGE,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(Name.RANDOMIZE_SPECIES_EVOLUTIONS, notMatchesEnum(EvolutionsMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_FORCE_GROWTH,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(Name.RANDOMIZE_SPECIES_EVOLUTIONS, matchesEnum(EvolutionsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_ALLOW_ALT_FORMES,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(Name.RANDOMIZE_SPECIES_EVOLUTIONS, notMatchesEnum(EvolutionsMod.UNCHANGED))
                    .supported(ofGeneration(7))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_ADJUST_LEVELS_FOR_STRENGTH,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                                            notMatchesEnum(BSTMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS,
                                            notMatchesEnum(EvolutionsMod.UNCHANGED))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_MAKE_POSSIBLE,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(notEvolveEveryLevelRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_MAKE_EASIER,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(notEvolveEveryLevelRestriction)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.SPECIES_EVOLUTIONS_EASIER_SCALING_LEVEL,
                    Category.SPECIES_EVOLUTIONS,
                    40,
                    30, 65)
                    .prerequisite(Name.SPECIES_EVOLUTIONS_MAKE_EASIER, isTrue)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_CHANGES_USE_ESTIMATED_LEVELS,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.SPECIES_EVOLUTIONS_MAKE_POSSIBLE, isTrue),
                                    new SimpleSettingRestriction<>(Name.SPECIES_EVOLUTIONS_MAKE_EASIER, isTrue)))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SPECIES_EVOLUTIONS_REMOVE_TIME_BASED,
                    Category.SPECIES_EVOLUTIONS)
                    .prerequisite(notEvolveEveryLevelRestriction)
                    .supported(RomHandler::hasTimeBasedEvolutions)
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STANDARDIZE_SPECIES_EXP_CURVES,
                    Category.SPECIES_EXP_CURVES)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Name.SPECIES_EXP_CURVE_STANDARD_SELECTION,
                    Category.SPECIES_EXP_CURVES,
                    ExpCurve.MEDIUM_FAST)
                    .prerequisite(Name.STANDARDIZE_SPECIES_EXP_CURVES, isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Name.SPECIES_EXP_CURVE_STANDARDIZE_EXTENT,
                    Category.SPECIES_EXP_CURVES,
                    ExpCurveExtentMod.LEGENDARIES)
                    .prerequisite(Name.STANDARDIZE_SPECIES_EXP_CURVES, isTrue)
                    .build()
    );

    //endregion

    //region given pokemon [currently Starters, Statics, & Trades]
    // I think yes move them now, while we're doing all this setting stuff.

    public enum StartersMod {
        UNCHANGED, CUSTOM, COMPLETELY_RANDOM, RANDOM_WITH_TWO_EVOLUTIONS, RANDOM_BASIC
    }

    public enum StartersTypeMod {
        NONE, FIRE_WATER_GRASS, TRIANGLE, UNIQUE, SINGLE_TYPE
    }

    private final static SettingRestriction anyStarterIsRandomRestriction = new MultiSettingRestriction(
            true, false,
            new MultiSettingRestriction(false, false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                            matchesEnum(StartersMod.CUSTOM)),
                    new MultiSettingRestriction(true, false,
                            new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_1,
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_2,
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_3,
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES))
                    )
            ),
            new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                    matchesEnum(StartersMod.COMPLETELY_RANDOM)),
            new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                    matchesEnum(StartersMod.RANDOM_WITH_TWO_EVOLUTIONS)),
            new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                    matchesEnum(StartersMod.RANDOM_BASIC))
    );

    private final static SettingRestriction allStartersAreRandomRestriction = new MultiSettingRestriction(
            true, false,
            new MultiSettingRestriction(false, false,
                    new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                            matchesEnum(StartersMod.CUSTOM)),
                    new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_1,
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_2,
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>(Name.STARTER_CUSTOM_3,
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES))
            ),
            new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                    matchesEnum(StartersMod.COMPLETELY_RANDOM)),
            new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                    matchesEnum(StartersMod.RANDOM_WITH_TWO_EVOLUTIONS)),
            new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                    matchesEnum(StartersMod.RANDOM_BASIC))
    );

    public enum StaticPokemonMod {
        UNCHANGED, RANDOM_MATCHING, COMPLETELY_RANDOM, SIMILAR_STRENGTH
    }

    public enum InGameTradesMod {
        UNCHANGED, RANDOMIZE_GIVEN, RANDOMIZE_GIVEN_AND_REQUESTED
    }

    public static final List<SettingDefinition<?>> STARTERS_AND_TRADES = List.of(
            new EnumSettingDefinition.Builder<>(
                    Name.RANDOMIZE_STARTERS,
                    Category.STARTERS_GENERAL,
                    StartersMod.UNCHANGED)
                    .restrictedStates(Map.of(
                            StartersMod.RANDOM_WITH_TWO_EVOLUTIONS, notEvolveEveryLevelRestriction,
                            StartersMod.RANDOM_BASIC, notEvolveEveryLevelRestriction))
                    .build(),
            new SpeciesIndexSettingDefinition.Builder<>(
                    Name.STARTER_CUSTOM_1,
                    Category.STARTERS_CUSTOM)
                    .prerequisite(Name.RANDOMIZE_STARTERS, matchesEnum(StartersMod.CUSTOM))
                    .supportedMaximums(rh -> rh.getSpecies().size() - 1)
                    .variableDefaultValue(rh -> rh.getStarters().get(0).getNumber())
                    .build(),
            new SpeciesIndexSettingDefinition.Builder<>(
                    Name.STARTER_CUSTOM_2,
                    Category.STARTERS_CUSTOM)
                    .prerequisite(Name.RANDOMIZE_STARTERS, matchesEnum(StartersMod.CUSTOM))
                    .supportedMaximums(rh -> rh.getSpecies().size() - 1)
                    .variableDefaultValue(rh -> rh.getStarters().get(1).getNumber())
                    .build(),
            new SpeciesIndexSettingDefinition.Builder<>(
                    Name.STARTER_CUSTOM_3,
                    Category.STARTERS_CUSTOM)
                    .prerequisite(Name.RANDOMIZE_STARTERS, matchesEnum(StartersMod.CUSTOM))
                    .supported(rh -> rh.getStarters().size() > 2)
                    .supportedMaximums(rh -> rh.getSpecies().size() - 1)
                    .variableDefaultValue(rh -> rh.getStarters().get(2).getNumber())
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    Name.STARTERS_TYPE_RESTRICTION,
                    Category.STARTER_TYPES,
                    StartersTypeMod.NONE)
                    .prerequisite(anyStarterIsRandomRestriction)
                    .restrictedStates(Map.of(
                            StartersTypeMod.FIRE_WATER_GRASS, allStartersAreRandomRestriction,
                            StartersTypeMod.TRIANGLE, allStartersAreRandomRestriction))
                    .supportedStates(Map.of(
                            StartersTypeMod.FIRE_WATER_GRASS, RomHandler::hasStarterTypeTriangleSupport,
                            StartersTypeMod.TRIANGLE, RomHandler::hasStarterTypeTriangleSupport))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STARTERS_NO_DUAL_TYPES,
                    Category.STARTER_TYPES)
                    .prerequisite(anyStarterIsRandomRestriction)
                    .build(),
            new TypeOrRandomSettingDefinition.Builder<>(
                    Name.STARTERS_SINGLE_TYPE_SELECTION,
                    Category.STARTER_TYPES)
                    .prerequisite(Name.STARTERS_TYPE_RESTRICTION, matchesEnum(StartersTypeMod.SINGLE_TYPE))
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STARTERS_NO_LEGENDARIES,
                    Category.STARTERS_GENERAL)
                    .prerequisite(anyStarterIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STARTERS_RANDOMIZE_HELD_ITEMS,
                    Category.STARTERS_GENERAL)
                    .supported(RomHandler::supportsStarterHeldItems)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STARTERS_BAN_BAD_HELD_ITEMS,
                    Category.STARTERS_GENERAL)
                    .prerequisite(Name.STARTERS_RANDOMIZE_HELD_ITEMS, isTrue)
                    .build(),
            new RangeLimitDefinition.LowerLimitBuilder<>(
                    Name.STARTERS_BST_MINIMUM,
                    Category.STARTER_BSTS,
                    Name.STARTERS_BST_MAXIMUM,
                    1, BaseStats.STAT_MAX * 6)
                    .supportedMaximums(overrideForGeneration(1, BaseStats.STAT_MAX * 5))
                    .build(),
            new RangeLimitDefinition.UpperLimitBuilder<>(
                    Name.STARTERS_BST_MAXIMUM,
                    Category.STARTER_BSTS,
                    Name.STARTERS_BST_MINIMUM,
                    1, BaseStats.STAT_MAX * 6)
                    .supportedMaximums(overrideForGeneration(1, BaseStats.STAT_MAX * 5))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_IN_GAME_TRADES,
                    Category.IN_GAME_TRADES,
                    InGameTradesMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRADES_RANDOMIZE_NICKNAMES,
                    Category.IN_GAME_TRADES)
                    .prerequisite(Name.RANDOMIZE_IN_GAME_TRADES, notMatchesEnum(InGameTradesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRADES_RANDOMIZE_ORIGINAL_TRAINERS,
                    Category.IN_GAME_TRADES)
                    .prerequisite(Name.RANDOMIZE_IN_GAME_TRADES, notMatchesEnum(InGameTradesMod.UNCHANGED))
                    .supported(notOfGeneration(1))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRADES_RANDOMIZE_IVS,
                    Category.IN_GAME_TRADES)
                    .prerequisite(Name.RANDOMIZE_IN_GAME_TRADES, notMatchesEnum(InGameTradesMod.UNCHANGED))
                    .supported(notOfGeneration(1))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRADES_RANDOMIZE_HELD_ITEMS,
                    Category.IN_GAME_TRADES)
                    .prerequisite(Name.RANDOMIZE_IN_GAME_TRADES, notMatchesEnum(InGameTradesMod.UNCHANGED))
                    .supported(notOfGeneration(1))
                    .build()
    );

    //endregion

    //region moves & movesets

    public enum MovesetsMod {
        UNCHANGED, RANDOM_PREFER_SAME_TYPE, COMPLETELY_RANDOM, METRONOME_ONLY
    }

    private static final SettingRestriction randomPokemonMovesetsRestriction = new MultiSettingRestriction(
            true, false,
            new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_MOVESETS,
                    matchesEnum(MovesetsMod.RANDOM_PREFER_SAME_TYPE)),
            new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_MOVESETS,
                    matchesEnum(MovesetsMod.COMPLETELY_RANDOM))
    );

    private static final SettingRestriction noMetronomeModeRestriction = new SimpleSettingRestriction<>(
            Name.RANDOMIZE_SPECIES_MOVESETS, notMatchesEnum(MovesetsMod.METRONOME_ONLY)
    );

    public static final List<SettingDefinition<?>> MOVES_AND_MOVESETS = List.of(
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.MOVES_RANDOMIZE_POWER,
                    Category.MOVE_TRAITS)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.MOVES_RANDOMIZE_ACCURACY,
                    Category.MOVE_TRAITS)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.MOVES_RANDOMIZE_PP,
                    Category.MOVE_TRAITS)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.MOVES_RANDOMIZE_CATEGORY,
                    Category.MOVE_TRAITS)
                    .supported(RomHandler::hasPhysicalSpecialSplit)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.MOVES_RANDOMIZE_NAME,
                    Category.MOVE_TRAITS)
                    .supported(RomHandler::isEnglish)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.UPDATE_MOVES,
                    Category.MOVE_TRAITS)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.UPDATE_MOVES_TO_GENERATION,
                    Category.MOVE_TRAITS,
                    9,
                    2, 9)
                    .prerequisite(Name.UPDATE_MOVES, isTrue)
                    .supportedMinimums(rh -> rh.generationOfPokemon() + 1)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_SPECIES_MOVESETS,
                    Category.SPECIES_MOVESETS,
                    MovesetsMod.UNCHANGED)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.MOVESETS_GUARANTEED_LEVEL_1_MOVE_COUNT,
                    Category.SPECIES_MOVESETS,
                    1,
                    1, 4)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .supported(RomHandler::supportsFourStartingMoves)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.MOVESETS_ORDER_BY_DAMAGE,
                    Category.SPECIES_MOVESETS)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.MOVESETS_BAN_OVERPOWERED,
                    Category.SPECIES_MOVESETS)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.MOVESETS_FORCE_GOOD_DAMAGING_PERCENT,
                    Category.SPECIES_MOVESETS,
                    0,
                    0, 100)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.MOVESETS_GUARANTEE_EVOLUTION_MOVES,
                    Category.SPECIES_MOVESETS)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .supported(atLeastGeneration(7))
                    .build()
    );

    //endregion

    //region foe pokemon

    public enum TrainersMod {
        UNCHANGED, RANDOM, DISTRIBUTED, MAINPLAYTHROUGH, TYPE_THEMED,
        TYPE_THEMED_ELITE4_GYMS, KEEP_THEMED, KEEP_THEME_OR_PRIMARY
    }

    private static final SettingRestriction anyTrainerPokemonIsRandomRestriction = new MultiSettingRestriction(
            true, false,
            new SimpleSettingRestriction<>(Name.RANDOMIZE_TRAINER_POKEMON, notMatchesEnum(TrainersMod.UNCHANGED)),
            new SimpleSettingRestriction<>(Name.TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT, greaterThanValue(0)),
            new SimpleSettingRestriction<>(Name.TRAINERS_IMPORTANT_ADDITIONAL_POKEMON_COUNT, greaterThanValue(0)),
            new SimpleSettingRestriction<>(Name.TRAINERS_REGULAR_ADDITIONAL_POKEMON_COUNT, greaterThanValue(0))
    );

    private static final SettingRestriction addHeldItemsToAnyTrainerRestriction = new MultiSettingRestriction(
            true, false,
            new SimpleSettingRestriction<>(Name.TRAINERS_ADD_HELD_ITEMS_TO_BOSSES, isTrue),
            new SimpleSettingRestriction<>(Name.TRAINERS_ADD_HELD_ITEMS_TO_IMPORTANT, isTrue),
            new SimpleSettingRestriction<>(Name.TRAINERS_ADD_HELD_ITEMS_TO_REGULAR, isTrue)
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
            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_TRAINER_POKEMON,
                    Category.TRAINERS_GENERAL,
                    TrainersMod.UNCHANGED)
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_BETTER_MOVESETS_FOR_BOSSES,
                    Category.TRAINERS_MOVESETS)
                    .prerequisite(noMetronomeModeRestriction)
                    .supported(RomHandler::canGiveCustomMovesetsToBossTrainers)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_BETTER_MOVESETS_FOR_IMPORTANT,
                    Category.TRAINERS_MOVESETS)
                    .prerequisite(noMetronomeModeRestriction)
                    .supported(RomHandler::canGiveCustomMovesetsToImportantTrainers)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_BETTER_MOVESETS_FOR_REGULAR,
                    Category.TRAINERS_MOVESETS)
                    .prerequisite(noMetronomeModeRestriction)
                    .supported(RomHandler::canGiveCustomMovesetsToRegularTrainers)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT,
                    Category.TRAINERS_ADDITIONAL_POKEMON,
                    0,
                    0, 5)
                    .supported(RomHandler::canAddPokemonToBossTrainers)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TRAINERS_IMPORTANT_ADDITIONAL_POKEMON_COUNT,
                    Category.TRAINERS_ADDITIONAL_POKEMON,
                    0,
                    0, 5)
                    .supported(RomHandler::canAddPokemonToImportantTrainers)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TRAINERS_REGULAR_ADDITIONAL_POKEMON_COUNT,
                    Category.TRAINERS_ADDITIONAL_POKEMON,
                    0,
                    0, 5)
                    .supported(RomHandler::canAddPokemonToRegularTrainers)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_ADD_HELD_ITEMS_TO_BOSSES,
                    Category.TRAINERS_HELD_ITEMS)
                    .supported(RomHandler::canAddHeldItemsToBossTrainers)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_ADD_HELD_ITEMS_TO_IMPORTANT,
                    Category.TRAINERS_HELD_ITEMS)
                    .supported(RomHandler::canAddHeldItemsToImportantTrainers)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_ADD_HELD_ITEMS_TO_REGULAR,
                    Category.TRAINERS_HELD_ITEMS)
                    .supported(RomHandler::canAddHeldItemsToRegularTrainers)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_HELD_ITEMS_CONSUMABLE_ONLY,
                    Category.TRAINERS_HELD_ITEMS)
                    .prerequisite(addHeldItemsToAnyTrainerRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINER_HELD_ITEMS_SENSIBLE_ONLY,
                    Category.TRAINERS_HELD_ITEMS)
                    .prerequisite(addHeldItemsToAnyTrainerRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_HELD_ITEMS_ACES_ONLY,
                    Category.TRAINERS_HELD_ITEMS)
                    .prerequisite(addHeldItemsToAnyTrainerRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_BOSSES_USE_DIVERSE_TYPES,
                    Category.TRAINERS_DIVERSE_TYPES)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TRAINER_POKEMON,
                                            notMatchesEnum(TrainersMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT,
                                            greaterThanValue(0))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_IMPORTANT_USE_DIVERSE_TYPES,
                    Category.TRAINERS_DIVERSE_TYPES)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TRAINER_POKEMON,
                                            notMatchesEnum(TrainersMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.TRAINERS_IMPORTANT_ADDITIONAL_POKEMON_COUNT,
                                            greaterThanValue(0))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_REGULAR_USE_DIVERSE_TYPES,
                    Category.TRAINERS_DIVERSE_TYPES)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TRAINER_POKEMON,
                                            notMatchesEnum(TrainersMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.TRAINERS_REGULAR_ADDITIONAL_POKEMON_COUNT,
                                            greaterThanValue(0))))
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_RIVAL_CARRIES_STARTER,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>(Name.RANDOMIZE_STARTERS,
                                                    notMatchesEnum(StartersMod.UNCHANGED)),
                                            new SimpleSettingRestriction<>(Name.RANDOMIZE_TRAINER_POKEMON,
                                                    notMatchesEnum(TrainersMod.UNCHANGED))),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS,
                                            notMatchesEnum(EvolutionsMod.RANDOM_EVERY_LEVEL))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_USE_SIMILAR_STRENGTH,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_AVOID_DUPLICATES,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_WEIGHT_TYPES,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_USE_LOCAL,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_NO_LEGENDARIES,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_NO_EARLY_WONDER_GUARD,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .supported(rh -> rh.abilitiesPerSpecies() != 0)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_ALLOW_ALT_FORMES,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .supported(RomHandler::hasFunctionalFormes)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_SWAP_MEGA_EVOLVABLES,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_RANDOM_SHINY_POKEMON,
                    Category.TRAINERS_COSMETIC)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .supported(atLeastGeneration(7))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TRAINERS_POKEMON_LEAGUE_UNIQUE_COUNT,
                    Category.TRAINERS_GENERAL,
                    0,
                    0, 2)
                    // This prerequisite can't be "anyTrainerPokemonIsRandomRestriction",
                    // because it requires that the E4+Champion get 2 random mons (to force to be unique).
                    // TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT might seem like it would do that,
                    // but in most games the E4 members have 5 mons, and the Champion a full team of 6!
                    // (and filling up a team beyond 6 is not allowed)
                    // So that option doesn't give the E4+Champion 2 random mons.
                    .prerequisite(Name.RANDOMIZE_TRAINER_POKEMON, notMatchesEnum(TrainersMod.UNCHANGED))
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_EVOLVE_POKEMON,
                    Category.TRAINERS_GENERAL)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_EVOLUTIONS,
                                            notMatchesEnum(EvolutionsMod.RANDOM_EVERY_LEVEL))))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TRAINERS_EVOLVE_LEVEL_PERCENT_MODIFIER,
                    Category.TRAINERS_GENERAL,
                    0,
                    -100, 155)
                    .prerequisite(Name.TRAINERS_EVOLVE_POKEMON, isTrue)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TRAINERS_LEVEL_MODIFIER_PERCENT,
                    Category.TRAINERS_GENERAL,
                    0,
                    -100, 155)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.TRAINERS_RANDOMIZE_BATTLE_STYLE,
                    Category.TRAINERS_BATTLE_STYLE,
                    BattleStyle.Modification.UNCHANGED)
                    .supported(atLeastGeneration(3))
                    .build(),
            new EnumSettingDefinition.Builder<>(
                    Name.TRAINERS_SINGLE_STYLE_SELECTION,
                    Category.TRAINERS_BATTLE_STYLE,
                    BattleStyle.Style.SINGLE_BATTLE)
                    .prerequisite(Name.TRAINERS_RANDOMIZE_BATTLE_STYLE, matchesEnum(BattleStyle.Modification.SINGLE_STYLE))
                    .supportedStates(Map.of(
                            BattleStyle.Style.TRIPLE_BATTLE, ofGeneration(5, 6),
                            BattleStyle.Style.ROTATION_BATTLE, ofGeneration(5, 6)))
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_RANDOMIZE_NAMES,
                    Category.TRAINERS_COSMETIC)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_RANDOMIZE_CLASS_NAMES,
                    Category.TRAINERS_COSMETIC)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TRAINERS_RANDOM_SHINY_POKEMON,
                    Category.TRAINERS_COSMETIC)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction) //why does this need randomized Pokémon?
                    .supported(atLeastGeneration(7))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_TOTEM_POKEMON,
                    Category.TOTEM_POKEMON,
                    TotemPokemonMod.UNCHANGED)
                    .supported(RomHandler::hasTotemPokemon)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Name.TOTEMS_RANDOMIZE_ALLIES,
                    Category.TOTEM_POKEMON,
                    AllyPokemonMod.UNCHANGED)
                    .supported(RomHandler::hasTotemPokemon)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Name.TOTEMS_RANDOMIZE_AURAS,
                    Category.TOTEM_POKEMON,
                    AuraMod.UNCHANGED)
                    .supported(RomHandler::hasTotemPokemon)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TOTEMS_RANDOMIZE_HELD_ITEMS,
                    Category.TOTEM_POKEMON)
                    .supported(RomHandler::hasTotemPokemon)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TOTEMS_ALLOW_ALT_FORMES,
                    Category.TOTEM_POKEMON)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TOTEM_POKEMON,
                                            notMatchesEnum(TotemPokemonMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.TOTEMS_RANDOMIZE_ALLIES,
                                            notMatchesEnum(AllyPokemonMod.UNCHANGED))))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TOTEMS_LEVEL_MODIFIER_PERCENT,
                    Category.TOTEM_POKEMON,
                    0,
                    -100, 155)
                    .supported(RomHandler::hasTotemPokemon)
                    .build()
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
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.RANDOMIZE_WILD_ENCOUNTERS,
                    Category.WILD_GENERAL)
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    Name.WILD_REPLACEMENT_ZONE,
                    Category.WILD_REPLACEMENT_ZONE,
                    WildPokemonZoneMod.SINGLE_ENCOUNTER)
                    .prerequisite(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue)
                    .supportedStates(Map.of(
                            WildPokemonZoneMod.ENCOUNTER_SET, rh -> !rh.hasMapIndices(),
                            WildPokemonZoneMod.MAP, RomHandler::hasMapIndices,
                            WildPokemonZoneMod.NAMED_LOCATION, RomHandler::hasEncounterLocations))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_SPLIT_REPLACEMENT_ZONE_BY_ENCOUNTER_TYPES,
                    Category.WILD_REPLACEMENT_ZONE)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    new SimpleSettingRestriction<>(Name.WILD_REPLACEMENT_ZONE,
                                            notMatchesEnum(WildPokemonZoneMod.SINGLE_ENCOUNTER)),
                                    new SimpleSettingRestriction<>(Name.WILD_REPLACEMENT_ZONE,
                                            notMatchesEnum(WildPokemonZoneMod.ENCOUNTER_SET))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>( // this setting is definitely zone-y
                    Name.WILD_REMOVE_TIME_BASED,
                    Category.WILD_REPLACEMENT_ZONE)
                    .prerequisite(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue)
                    .supported(RomHandler::hasTimeBasedEncounters)
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    Name.WILD_TYPE_RESTRICTION,
                    Category.WILD_TYPES,
                    WildPokemonTypeMod.NONE)
                    .prerequisite(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue)
                    .restrictedStates(Map.of(
                            WildPokemonTypeMod.RANDOM_THEMES,
                            new SimpleSettingRestriction<>(Name.WILD_REPLACEMENT_ZONE,
                                    notMatchesEnum(WildPokemonZoneMod.GAME))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_KEEP_TYPE_THEMES,
                    Category.WILD_TYPES)
                    .prerequisite(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.WILD_EVOLUTION_RESTRICTION,
                    Category.WILD_EVOLUTIONS,
                    WildPokemonEvolutionMod.NONE)
                    .prerequisite(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_EVOLUTION_KEEP_RELATIONS,
                    Category.WILD_EVOLUTIONS)
                    .prerequisite(Name.WILD_REPLACEMENT_ZONE, notMatchesEnum(WildPokemonZoneMod.SINGLE_ENCOUNTER))
                    .build(),

            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_NO_LEGENDARIES,
                    Category.WILD_GENERAL)
                    .prerequisite(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_ALLOW_ALT_FORMES,
                    Category.WILD_GENERAL)
                    .prerequisite(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue)
                    .supported(RomHandler::hasWildAltFormes)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_USE_SIMILAR_STRENGTH,
                    Category.WILD_GENERAL)
                    .prerequisite(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_SIMILAR_STRENGTH_BALANCE_LOW_LEVEL,
                    Category.WILD_GENERAL)
                    .prerequisite(Name.WILD_USE_SIMILAR_STRENGTH, isTrue)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_CATCH_EM_ALL,
                    Category.WILD_GENERAL)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_WILD_ENCOUNTERS, isTrue),
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>(Name.WILD_REPLACEMENT_ZONE,
                                                    notMatchesEnum(WildPokemonZoneMod.GAME)),
                                            new SimpleSettingRestriction<>(Name.WILD_SPLIT_REPLACEMENT_ZONE_BY_ENCOUNTER_TYPES, isTrue))))
                    .build(),

            // Below: "wild pokemon" settings that don't require random wild pokemon
            new SimpleSettingDefinition.Builder<>(
                    Name.WILD_MINIMUM_CATCH_RATE_SELECTION,
                    Category.WILD_POST_TWEAKS,
                    CatchRateMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_RANDOMIZE_HELD_ITEMS,
                    Category.WILD_POST_TWEAKS)
                    .supported(notOfGeneration(1))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.WILD_HELD_ITEMS_BAN_MINOR,
                    Category.WILD_POST_TWEAKS)
                    .prerequisite(Name.WILD_RANDOMIZE_HELD_ITEMS, isTrue)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.WILD_LEVEL_MODIFIER_PERCENT,
                    Category.WILD_POST_TWEAKS,
                    0,
                    -100, 155)
                    .build(),

            // Statics are somewhat awkward here since they currently encompass gift mons too,
            // but once those are broken out this is the right place for them to be settings-wise
            // TODO: break out gift mon randomization
            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_STATIC_ENCOUNTERS,
                    Category.STATIC_ENCOUNTERS,
                    StaticPokemonMod.UNCHANGED)
                    .supported(RomHandler::canChangeStaticPokemon)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STATICS_FULL_RANDOM_OVER_600_BST,
                    Category.STATIC_ENCOUNTERS)
                    .prerequisite(Name.RANDOMIZE_STATIC_ENCOUNTERS, notMatchesEnum(StaticPokemonMod.UNCHANGED))
                    .build(), //This is such a weirdly specific setting...
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STATICS_LIMIT_MAIN_GAME_LEGENDARIES,
                    Category.STATIC_ENCOUNTERS)
                    .prerequisite(Name.RANDOMIZE_STATIC_ENCOUNTERS, matchesEnum(StaticPokemonMod.SIMILAR_STRENGTH))
                    .supported(RomHandler::hasMainGameLegendaries)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STATICS_ALLOW_ALT_FORMES,
                    Category.STATIC_ENCOUNTERS)
                    .prerequisite(Name.RANDOMIZE_STATIC_ENCOUNTERS, notMatchesEnum(StaticPokemonMod.UNCHANGED))
                    .supported(RomHandler::hasStarterAltFormes)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STATICS_SWAP_MEGA_EVOLVABLES,
                    Category.STATIC_ENCOUNTERS)
                    .prerequisite(Name.RANDOMIZE_STATIC_ENCOUNTERS, notMatchesEnum(StaticPokemonMod.UNCHANGED))
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.STATICS_FIX_MUSIC,
                    Category.STATIC_ENCOUNTERS)
                    .prerequisite(Name.RANDOMIZE_STATIC_ENCOUNTERS, notMatchesEnum(StaticPokemonMod.UNCHANGED))
                    .supported(RomHandler::hasStaticMusicFix)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.STATICS_LEVEL_MODIFIER_PERCENT,
                    Category.STATIC_ENCOUNTERS,
                    0,
                    -100, 155)
                    .build()
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
            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_TM_MOVES,
                    Category.TM_MOVES,
                    TMMovesMod.UNCHANGED)
                    .prerequisite(noMetronomeModeRestriction)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TMS_BAN_OVERPOWERED,
                    Category.TM_MOVES)
                    .prerequisite(Name.RANDOMIZE_TM_MOVES, notMatchesEnum(TMMovesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TMS_KEEP_FIELD_MOVES,
                    Category.TM_MOVES)
                    .prerequisite(Name.RANDOMIZE_TM_MOVES, notMatchesEnum(TMMovesMod.UNCHANGED))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TMS_GOOD_DAMAGING_PERCENT,
                    Category.TM_MOVES,
                    0,
                    0, 100)
                    .prerequisite(Name.RANDOMIZE_TM_MOVES, notMatchesEnum(TMMovesMod.UNCHANGED))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                    Category.TM_AND_HM_COMPATABILITY,
                    TMsHMsCompatibilityMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TM_COMPATABILITY_LEVEL_UP_SANITY,
                    Category.TM_AND_HM_COMPATABILITY)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_MOVESETS,
                                            notMatchesEnum(MovesetsMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TM_MOVES,
                                            notMatchesEnum(TMMovesMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                                            matchesEnum(TMsHMsCompatibilityMod.COMPLETELY_RANDOM)),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                                            matchesEnum(TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TM_COMPATABILITY_FOLLOW_EVOLUTIONS,
                    Category.TM_AND_HM_COMPATABILITY)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                                                    matchesEnum(TMsHMsCompatibilityMod.COMPLETELY_RANDOM)),
                                            new SimpleSettingRestriction<>(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY,
                                                    matchesEnum(TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE)),
                                            new SimpleSettingRestriction<>(Name.TM_COMPATABILITY_LEVEL_UP_SANITY, isTrue))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TMS_FULL_HM_COMPATABILITY,
                    Category.TM_AND_HM_COMPATABILITY)
                    .prerequisite(Name.RANDOMIZE_TM_AND_HM_COMPATABILITY, notMatchesEnum(TMsHMsCompatibilityMod.FULL))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_TUTOR_MOVES,
                    Category.MOVE_TUTOR_MOVES,
                    MoveTutorMovesMod.UNCHANGED)
                    .prerequisite(noMetronomeModeRestriction)
                    .supported(RomHandler::hasMoveTutors)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TUTORS_BAN_OVERPOWERED,
                    Category.MOVE_TUTOR_MOVES)
                    .prerequisite(Name.RANDOMIZE_TUTOR_MOVES, notMatchesEnum(MoveTutorMovesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TUTORS_KEEP_FIELD_MOVES,
                    Category.MOVE_TUTOR_MOVES)
                    .prerequisite(Name.RANDOMIZE_TUTOR_MOVES, notMatchesEnum(MoveTutorMovesMod.UNCHANGED))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Name.TUTORS_GOOD_DAMAGING_PERCENT,
                    Category.MOVE_TUTOR_MOVES,
                    0,
                    0, 100)
                    .prerequisite(Name.RANDOMIZE_TUTOR_MOVES, notMatchesEnum(MoveTutorMovesMod.UNCHANGED))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_TUTOR_COMPATABILITY,
                    Category.MOVE_TUTOR_COMPATABILITY,
                    MoveTutorsCompatibilityMod.UNCHANGED)
                    .supported(RomHandler::hasMoveTutors)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TUTOR_COMPATABILITY_LEVEL_UP_SANITY,
                    Category.MOVE_TUTOR_COMPATABILITY)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_MOVESETS,
                                            notMatchesEnum(MovesetsMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TUTOR_MOVES,
                                            notMatchesEnum(MoveTutorMovesMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TUTOR_COMPATABILITY,
                                            matchesEnum(MoveTutorsCompatibilityMod.COMPLETELY_RANDOM)),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_TUTOR_COMPATABILITY,
                                            matchesEnum(MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE))))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TUTOR_COMPATABILITY_FOLLOW_EVOLUTIONS,
                    Category.MOVE_TUTOR_COMPATABILITY)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>(Name.RANDOMIZE_TUTOR_COMPATABILITY,
                                                    matchesEnum(MoveTutorsCompatibilityMod.COMPLETELY_RANDOM)),
                                            new SimpleSettingRestriction<>(Name.RANDOMIZE_TUTOR_COMPATABILITY,
                                                    matchesEnum(MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE)),
                                            new SimpleSettingRestriction<>(Name.TUTOR_COMPATABILITY_LEVEL_UP_SANITY, isTrue))))
                    .build()
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
            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_FIELD_ITEMS,
                    Category.FIELD_ITEMS,
                    FieldItemsMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.FIELD_ITEMS_BAN_MINOR,
                    Category.FIELD_ITEMS)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_FIELD_ITEMS,
                                            matchesEnum(FieldItemsMod.RANDOM)),
                                    new SimpleSettingRestriction<>(Name.RANDOMIZE_FIELD_ITEMS,
                                            matchesEnum(FieldItemsMod.RANDOM_EVEN))))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_SPECIAL_SHOP_ITEMS,
                    Category.SPECIAL_SHOP_ITEMS,
                    ShopItemsMod.UNCHANGED)
                    .supported(RomHandler::hasShopSupport)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SHOP_ITEMS_BAN_MINOR,
                    Category.SPECIAL_SHOP_ITEMS)
                    .prerequisite(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SHOP_ITEMS_BAN_REGULAR_SHOP_ITEMS,
                    Category.SPECIAL_SHOP_ITEMS)
                    .prerequisite(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SHOP_ITEMS_BAN_OVERPOWERED,
                    Category.SPECIAL_SHOP_ITEMS)
                    .prerequisite(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SHOP_ITEMS_GUARANTEE_EVOLUTION_ITEMS,
                    Category.SPECIAL_SHOP_ITEMS)
                    .prerequisite(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SHOP_ITEMS_GUARANTEE_X_ITEMS,
                    Category.SPECIAL_SHOP_ITEMS)
                    .prerequisite(Name.RANDOMIZE_SPECIAL_SHOP_ITEMS, matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SHOP_ITEMS_BALANCE_PRICES,
                    Category.SHOP_ITEMS_GENERAL)
                    .supported(RomHandler::hasShopSupport)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.SHOP_ITEMS_ADD_CHEAP_RARE_CANDY,
                    Category.SHOP_ITEMS_GENERAL)
                    .supported(RomHandler::canChangeShopSizes)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_PICKUP_ITEMS,
                    Category.PICKUP_ITEMS,
                    PickupItemsMod.UNCHANGED)
                    .supported(rh -> rh.abilitiesPerSpecies() > 0)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.PICKUP_ITEMS_BAN_MINOR,
                    Category.PICKUP_ITEMS)
                    .prerequisite(Name.RANDOMIZE_PICKUP_ITEMS, matchesEnum(PickupItemsMod.RANDOM))
                    .build()
    );

    //endregion

    //region types

    public enum TypeEffectivenessMod {
        UNCHANGED, RANDOM, RANDOM_BALANCED, KEEP_IDENTITIES, INVERSE
    }

    public static final List<SettingDefinition<?>> TYPES = List.of(
            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_TYPE_EFFECTIVENESS,
                    Category.TYPE_EFFECTIVENESS,
                    TypeEffectivenessMod.UNCHANGED)
                    .supported(RomHandler::hasTypeEffectivenessSupport)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.TYPE_INVERSE_ADD_RANDOM_IMMUNITIES,
                    Category.TYPE_EFFECTIVENESS)
                    .prerequisite(Name.RANDOMIZE_TYPE_EFFECTIVENESS, matchesEnum(TypeEffectivenessMod.INVERSE))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.UPDATE_TYPE_EFFECTIVENESS,
                    Category.TYPE_EFFECTIVENESS)
                    .supported(rh -> rh.hasTypeEffectivenessSupport()
                            && rh.generationOfPokemon() < TypeEffectivenessUpdater.UPDATE_TO_GEN)
                    .build()
    );

    //endregion

    //region graphics

    public enum SpeciesPalettesMod {
        UNCHANGED, RANDOM
    }

    public static final List<SettingDefinition<?>> GRAPHICS = List.of(
            new SimpleSettingDefinition.Builder<>(
                    Name.RANDOMIZE_SPECIES_PALETTES,
                    Category.SPECIES_PALETTES,
                    SpeciesPalettesMod.UNCHANGED)
                    .supported(RomHandler::hasPokemonPaletteSupport)
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.PALETTES_FOLLOW_TYPES,
                    Category.SPECIES_PALETTES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_PALETTES, matchesEnum(SpeciesPalettesMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.PALETTES_FOLLOW_EVOLUTIONS,
                    Category.SPECIES_PALETTES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_PALETTES, matchesEnum(SpeciesPalettesMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.BooleanBuilder<>(
                    Name.PALETTES_SHINY_FROM_NORMAL,
                    Category.SPECIES_PALETTES)
                    .prerequisite(Name.RANDOMIZE_SPECIES_PALETTES, matchesEnum(SpeciesPalettesMod.RANDOM))
                    .supported(notOfGeneration(1))
                    .build()

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
        all.addAll(STARTERS_AND_TRADES);
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
