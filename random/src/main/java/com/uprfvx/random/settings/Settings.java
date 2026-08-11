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
// "Pokemon Base Stats" or "Pokemon Traits". Supercategories should be the tabs of the GUI. Intermediate categories are
// skipped.

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
    //so that we can load it in and convert it to the new setting.

    /**
     * Contains a constant for the name of each setting used in the randomizer.
     */
    public static class Names {
        /*** GENERAL OPTIONS ***/
        //General options
        public static final String LIMIT_POKEMON = "LimitPokemon";
        public static final String NO_RANDOM_INTRO_MON = "NoRandomIntroMon";
        public static final String RACE_MODE = "RaceMode";
        public static final String NO_IRREGULAR_ALT_FORMES = "NoIrregularAltFormes";
        //Limit Pokemon
        public static final String ALLOW_GENERATION_1 = "AllowGeneration1";
        public static final String ALLOW_GENERATION_2 = "AllowGeneration2";
        public static final String ALLOW_GENERATION_3 = "AllowGeneration3";
        public static final String ALLOW_GENERATION_4 = "AllowGeneration4";
        public static final String ALLOW_GENERATION_5 = "AllowGeneration5";
        public static final String ALLOW_GENERATION_6 = "AllowGeneration6";
        public static final String ALLOW_GENERATION_7 = "AllowGeneration7";

        /*** SPECIES TRAITS ***/
        //Base Stat Totals
        public static final String RANDOMIZE_BASE_STAT_TOTALS = "RandomizeSpeciesBaseStatTotals";
        public static final String BST_BUFF_NERF_PERCENT = "BSTRandomBuffNerfPercentage";
        public static final String BSTS_FOLLOW_EVOLUTION = "StatTotalsFollowEvolutions";
        public static final String BST_SHUFFLE_SEPARATE_LEGENDARIES = "BSTShuffleLegendariesSeparately";
        //Base Stat Distribution
        public static final String RANDOMIZE_BASE_STAT_DISTRIBUTIONS = "RandomizeSpeciesBaseStatDistributions";
        public static final String BSDS_FOLLOW_EVOLUTION = "StatDistributionsFollowEvolutions";
        public static final String BSDS_FOLLOW_MEGA_EVOS = "StatDistributionsFollowMegaEvolutions";
        public static final String BSDS_ASSIGN_EVO_STATS_RANDOMLY = "StatDistributionsAssignEvoStatsRandomly";
        //Update Base Stats
        public static final String UPDATE_BASE_STATS = "UpdateBaseStats";
        public static final String UPDATE_STATS_GENERATION = "UpdateBaseStatsToGeneration";
        //Species' Types
        public static final String SPECIES_RANDOMIZE_TYPES = "RandomizeSpeciesTypes";
        public static final String SPECIES_FORCE_DUAL_TYPES = "SpeciesTypesForceDualTypes";
        public static final String SPECIES_TYPES_FOLLOW_MEGA_EVO = "SpeciesTypesFollowMegaEvolutions";
        //Species' Abilities
        public static final String SPECIES_RANDOMIZE_ABILITIES = "RandomizeSpeciesAbilities";
        public static final String SPECIES_ABILITIES_FOLLOW_EVO = "SpeciesAbilitiesFollowEvolutions";
        public static final String SPECIES_ABILITIES_FOLLOW_MEGA_EVO = "SpeciesAbilitiesFollowMegaEvolutions";
        public static final String SPECIES_ABILITIES_COMBINE_DUPES = "SpeciesCombineDuplicateAbilities";
        public static final String SPECIES_FORCE_TWO_ABILITIES = "SpeciesAlwaysHaveTwoAbilities";
        public static final String SPECIES_BAN_WONDER_GUARD = "SpeciesBanWonderGuard";
        public static final String SPECIES_BAN_MINOR_ABILITIES = "SpeciesBanMinorAbilities";
        public static final String SPECIES_BAN_NEGATIVE_ABILITIES = "SpeciesBanNegativeAbilities";
        public static final String SPECIES_BAN_TRAPPING_ABILITIES = "SpeciesBanTrappingAbilities";
        //Species' Evolutions
        public static final String SPECIES_RANDOMIZE_EVOLUTIONS = "RandomizeSpeciesEvolutions";
        public static final String SPECIES_EVOLUTIONS_SIMILAR_STRENGTH = "SpeciesEvolutionsUseSimilarStrength";
        public static final String SPECIES_EVOLUTIONS_SAME_TYPE = "SpeciesEvolutionsUseSameType"; //TODO: clarify
        public static final String SPECIES_EVOLUTIONS_MAX_THREE = "SpeciesEvolutionsMaxThreeStages";
        public static final String SPECIES_EVOLUTIONS_NO_CONVERGENCE = "SpeciesEvolutionsNoConvergence";
        public static final String SPECIES_EVOLUTIONS_FORCE_CHANGE = "SpeciesEvolutionsForceChange";
        public static final String SPECIES_EVOLUTIONS_FORCE_GROWTH = "SpeciesEvolutionsForceGrowth";
        public static final String SPECIES_EVOLUTIONS_ALLOW_ALTS = "SpeciesEvolutionsAllowAltFormes";
        public static final String SPECIES_EVOLUTIONS_ADJUST_LEVELS = "SpeciesEvolutionsAdjustLevels"; //TODO: clarify
        public static final String SPECIES_EVOLUTIONS_MAKE_POSSIBLE = "ChangeImpossibleEvolutions";
        public static final String SPECIES_EVOLUTIONS_MAKE_EASIER = "MakeEvolutionsEasier";
        public static final String SPECIES_EVOLUTIONS_EASIER_LEVEL = "MakeEvolutionsEasierScalingLevel";
        public static final String SPECIES_EVOLUTIONS_USE_ESTIMATED_LEVELS = "UseEstimatedEvolutionLevels"; //TODO: clarify
        public static final String SPECIES_EVOLUTIONS_REMOVE_TIME_BASED = "RemoveTimeBasedEvolutions";
        //Species' EXP Curves
        public static final String SPECIES_STANDARD_EXP_CURVE = "SpeciesStandardizeEXPCurves";
        public static final String SPECIES_EXP_CURVE_TO_USE = "SpeciesStandardEXPCurveSelection";
        public static final String SPECIES_STANDARDIZE_EXP_CURVE_EXTENT = "SpeciesStandardizeEXPCurveExtent";

    }

    public static class Categories {
        /*** GENERAL OPTIONS ***/
        public static final String GENERAL_OPTIONS = "GeneralOptions";
        public static final String LIMIT_POKEMON = "LimitPokemon";

        /*** SPECIES TRAITS ***/
        public static final String BASE_STAT_TOTALS = "BaseStatisticTotals";
        public static final String BASE_STAT_DISTRIBUTION = "BaseStatisticDistribution";
        public static final String UPDATE_BASE_STATS = "UpdateBaseStatistics";
        public static final String SPECIES_TYPES = "SpeciesTypes";
        public static final String SPECIES_ABILITIES = "SpeciesAbilites";
        public static final String SPECIES_EVOLUTIONS = "SpeciesEvolutions";
        public static final String SPECIES_EXP_CURVES = "SpeciesExpCurves";

        /****** Supercategories ******/
        public static final List<String> GENERAL = List.of(GENERAL_OPTIONS, LIMIT_POKEMON);
        public static final List<String> SPECIES_TRAITS = List.of(BASE_STAT_TOTALS, BASE_STAT_DISTRIBUTION,
                UPDATE_BASE_STATS, SPECIES_TYPES, SPECIES_ABILITIES, SPECIES_EVOLUTIONS, SPECIES_EXP_CURVES);
    }

    //region general options

    // needs to be up here since general options relies on it
    public static final SettingRestriction notEvolveEveryLevelRestriction = new SimpleSettingRestriction<>(
            Names.SPECIES_RANDOMIZE_EVOLUTIONS, notMatchesEnum(EvolutionsMod.RANDOM_EVERY_LEVEL)
    );

    public static final List<SettingDefinition<?>> GENERAL_OPTIONS = List.of(
            new SimpleSettingDefinition.Builder<>(
                    Names.NO_RANDOM_INTRO_MON,
                    Categories.GENERAL_OPTIONS, //TODO: move to misc. tweaks?
                    false)
                    .build(),

            //TODO: make this setting actually work?
            // "this setting" is race mode?
            // I believe I was referring to "NoRandomIntroMon" but I cannot recall for sure anymore.
            //TODO investigate this todo i guess
            new SimpleSettingDefinition.Builder<>(
                    Names.RACE_MODE,
                    Categories.GENERAL_OPTIONS,
                    false)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.NO_IRREGULAR_ALT_FORMES,
                    Categories.GENERAL_OPTIONS,
                    false)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "NoPrematureEvolutions",
                    Categories.GENERAL_OPTIONS,
                    false)
                    .prerequisite(notEvolveEveryLevelRestriction)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Names.ALLOW_GENERATION_1,
                    Categories.LIMIT_POKEMON,
                    true)
                    .supported(atLeastGeneration(1))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.ALLOW_GENERATION_2,
                    Categories.LIMIT_POKEMON,
                    true)
                    .supported(atLeastGeneration(2))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.ALLOW_GENERATION_3,
                    Categories.LIMIT_POKEMON,
                    true)
                    .supported(atLeastGeneration(3))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.ALLOW_GENERATION_4,
                    Categories.LIMIT_POKEMON,
                    true)
                    .supported(atLeastGeneration(4))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.ALLOW_GENERATION_5,
                    Categories.LIMIT_POKEMON,
                    true)
                    .supported(atLeastGeneration(5))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.ALLOW_GENERATION_6,
                    Categories.LIMIT_POKEMON,
                    true)
                    .supported(atLeastGeneration(6))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.ALLOW_GENERATION_7,
                    Categories.LIMIT_POKEMON,
                    true)
                    .supported(atLeastGeneration(7))
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
                    Names.RANDOMIZE_BASE_STAT_TOTALS,
                    Categories.BASE_STAT_TOTALS,
                    BSTMod.UNCHANGED)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Names.BST_BUFF_NERF_PERCENT,
                    Categories.BASE_STAT_TOTALS,
                    1,
                    1, 50)
                    .prerequisite(Names.RANDOMIZE_BASE_STAT_TOTALS, matchesEnum(BSTMod.RANDOM_BUFF_NERF))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.BSTS_FOLLOW_EVOLUTION,
                    Categories.BASE_STAT_TOTALS,
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>(Names.RANDOMIZE_BASE_STAT_TOTALS,
                                                    matchesEnum(BSTMod.RANDOM_BUFF_NERF)),
                                            new SimpleSettingRestriction<>(Names.RANDOMIZE_BASE_STAT_TOTALS,
                                                    matchesEnum(BSTMod.SHUFFLE)))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.BST_SHUFFLE_SEPARATE_LEGENDARIES,
                    Categories.BASE_STAT_TOTALS,
                    false)
                    .prerequisite(Names.RANDOMIZE_BASE_STAT_TOTALS, matchesEnum(BSTMod.SHUFFLE))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Names.RANDOMIZE_BASE_STAT_DISTRIBUTIONS,
                    Categories.BASE_STAT_DISTRIBUTION,
                    BaseStatDistributionsMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.BSDS_FOLLOW_EVOLUTION,
                    Categories.BASE_STAT_DISTRIBUTION,
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new SimpleSettingRestriction<>(Names.RANDOMIZE_BASE_STAT_DISTRIBUTIONS,
                                            notMatchesEnum(BaseStatDistributionsMod.UNCHANGED))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.BSDS_FOLLOW_MEGA_EVOS,
                    Categories.BASE_STAT_DISTRIBUTION,
                    false)
                    .prerequisite(Names.BSDS_FOLLOW_EVOLUTION, isTrue)
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.BSDS_ASSIGN_EVO_STATS_RANDOMLY,
                    Categories.BASE_STAT_DISTRIBUTION,
                    false)
                    .prerequisite(Names.BSDS_FOLLOW_EVOLUTION, isTrue)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Names.UPDATE_BASE_STATS,
                    Categories.UPDATE_BASE_STATS,
                    false)
                    .supported(notOfGeneration(1))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Names.UPDATE_STATS_GENERATION,
                    Categories.UPDATE_BASE_STATS,
                    9,
                    6, 9)
                    .prerequisite(Names.UPDATE_BASE_STATS, isTrue)
                    .supportedMinimums(rh -> rh.generationOfPokemon() + 1)
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    Names.SPECIES_RANDOMIZE_TYPES,
                    Categories.SPECIES_TYPES,
                    SpeciesTypesMod.UNCHANGED)
                    .restrictedStates(Map.of(SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS, notEvolveEveryLevelRestriction))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_FORCE_DUAL_TYPES,
                    Categories.SPECIES_TYPES,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_TYPES, notMatchesEnum(SpeciesTypesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_TYPES_FOLLOW_MEGA_EVO,
                    Categories.SPECIES_TYPES,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_TYPES, matchesEnum(SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS))
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_RANDOMIZE_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    AbilitiesMod.UNCHANGED)
                    .supported(rh -> rh.abilitiesPerSpecies() != 0)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_ABILITIES_FOLLOW_EVO,
                    Categories.SPECIES_ABILITIES,
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new SimpleSettingRestriction<>(Names.SPECIES_RANDOMIZE_ABILITIES,
                                            notMatchesEnum(AbilitiesMod.UNCHANGED))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_ABILITIES_FOLLOW_MEGA_EVO,
                    Categories.SPECIES_ABILITIES,
                    false)
                    .prerequisite(Names.SPECIES_ABILITIES_FOLLOW_EVO, isTrue)
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_ABILITIES_COMBINE_DUPES,
                    Categories.SPECIES_ABILITIES,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_FORCE_TWO_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_BAN_WONDER_GUARD,
                    Categories.SPECIES_ABILITIES,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_BAN_TRAPPING_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_BAN_NEGATIVE_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_BAN_MINOR_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_ABILITIES, notMatchesEnum(AbilitiesMod.UNCHANGED))
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    Names.SPECIES_RANDOMIZE_EVOLUTIONS,
                    Categories.SPECIES_EVOLUTIONS,
                    EvolutionsMod.UNCHANGED)
                    .supportedStates(Map.of(EvolutionsMod.RANDOM_EVERY_LEVEL, RomHandler::canGiveEverySpeciesOneEvolutionEach))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_SIMILAR_STRENGTH,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_EVOLUTIONS, matchesEnum(EvolutionsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_SAME_TYPE,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_EVOLUTIONS, notMatchesEnum(EvolutionsMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_MAX_THREE,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_EVOLUTIONS, matchesEnum(EvolutionsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_NO_CONVERGENCE,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_EVOLUTIONS, notMatchesEnum(EvolutionsMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_FORCE_CHANGE,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_EVOLUTIONS, notMatchesEnum(EvolutionsMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_FORCE_GROWTH,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_EVOLUTIONS, matchesEnum(EvolutionsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_ALLOW_ALTS,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(Names.SPECIES_RANDOMIZE_EVOLUTIONS, notMatchesEnum(EvolutionsMod.UNCHANGED))
                    .supported(ofGeneration(7))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_ADJUST_LEVELS,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Names.RANDOMIZE_BASE_STAT_TOTALS,
                                            notMatchesEnum(BSTMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS,
                                            notMatchesEnum(EvolutionsMod.UNCHANGED))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_MAKE_POSSIBLE,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(notEvolveEveryLevelRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_MAKE_EASIER,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(notEvolveEveryLevelRestriction)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_EASIER_LEVEL,
                    Categories.SPECIES_EVOLUTIONS,
                    40,
                    30, 65)
                    .prerequisite(Names.SPECIES_EVOLUTIONS_MAKE_EASIER, isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_USE_ESTIMATED_LEVELS,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>(Names.SPECIES_EVOLUTIONS_MAKE_POSSIBLE, isTrue),
                                    new SimpleSettingRestriction<>(Names.SPECIES_EVOLUTIONS_MAKE_EASIER, isTrue)))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EVOLUTIONS_REMOVE_TIME_BASED,
                    Categories.SPECIES_EVOLUTIONS,
                    false)
                    .prerequisite(notEvolveEveryLevelRestriction)
                    .supported(RomHandler::hasTimeBasedEvolutions)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_STANDARD_EXP_CURVE,
                    Categories.SPECIES_EXP_CURVES,
                    false)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_EXP_CURVE_TO_USE,
                    Categories.SPECIES_EXP_CURVES,
                    ExpCurve.MEDIUM_FAST)
                    .prerequisite(Names.SPECIES_STANDARD_EXP_CURVE, isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    Names.SPECIES_STANDARDIZE_EXP_CURVE_EXTENT,
                    Categories.SPECIES_EXP_CURVES,
                    ExpCurveExtentMod.LEGENDARIES)
                    .prerequisite(Names.SPECIES_STANDARD_EXP_CURVE, isTrue)
                    .build()
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
                    new SimpleSettingRestriction<>("RandomizeStarters",
                            matchesEnum(StartersMod.CUSTOM)),
                    new MultiSettingRestriction(true, false,
                            new SimpleSettingRestriction<>("CustomStarter1",
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>("CustomStarter2",
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>("CustomStarter3",
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES))
                    )
            ),
            new SimpleSettingRestriction<>("RandomizeStarters",
                    matchesEnum(StartersMod.COMPLETELY_RANDOM)),
            new SimpleSettingRestriction<>("RandomizeStarters",
                    matchesEnum(StartersMod.RANDOM_WITH_TWO_EVOLUTIONS)),
            new SimpleSettingRestriction<>("RandomizeStarters",
                    matchesEnum(StartersMod.RANDOM_BASIC))
    );

    private final static SettingRestriction allStartersAreRandomRestriction = new MultiSettingRestriction(
            true, false,
            new MultiSettingRestriction(false, false,
                    new SimpleSettingRestriction<>("RandomizeStarters",
                            matchesEnum(StartersMod.CUSTOM)),
                    new SimpleSettingRestriction<>("CustomStarter1",
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>("CustomStarter2",
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>("CustomStarter3",
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES))
            ),
            new SimpleSettingRestriction<>("RandomizeStarters",
                    matchesEnum(StartersMod.COMPLETELY_RANDOM)),
            new SimpleSettingRestriction<>("RandomizeStarters",
                    matchesEnum(StartersMod.RANDOM_WITH_TWO_EVOLUTIONS)),
            new SimpleSettingRestriction<>("RandomizeStarters",
                    matchesEnum(StartersMod.RANDOM_BASIC))
    );

    public enum StaticPokemonMod {
        UNCHANGED, RANDOM_MATCHING, COMPLETELY_RANDOM, SIMILAR_STRENGTH
    }

    public enum InGameTradesMod {
        UNCHANGED, RANDOMIZE_GIVEN, RANDOMIZE_GIVEN_AND_REQUESTED
    }

    private static final int STARTER_MIN_BST = 307;
    private static final int STARTER_MAX_BST = 320;
    private static final int STARTER_MIN_BST_GEN_1 = 249;
    private static final int STARTER_MAX_BST_GEN_1 = 253;

    public static final List<SettingDefinition<?>> STARTERS_STATICS_AND_TRADES = List.of(
            new EnumSettingDefinition.Builder<>(
                    "RandomizeStarters",
                    "Starters",
                    StartersMod.UNCHANGED)
                    .restrictedStates(Map.of(
                            StartersMod.RANDOM_WITH_TWO_EVOLUTIONS, notEvolveEveryLevelRestriction,
                            StartersMod.RANDOM_BASIC, notEvolveEveryLevelRestriction))
                    .build(),
            new SpeciesIndexSettingDefinition.Builder<>(
                    "CustomStarter1",
                    "Starters")
                    .prerequisite("RandomizeStarters", matchesEnum(StartersMod.CUSTOM))
                    .supportedMaximums(rh -> rh.getSpecies().size() - 1)
                    .variableDefaultValue(rh -> rh.getStarters().get(0).getNumber())
                    .build(),
            new SpeciesIndexSettingDefinition.Builder<>(
                    "CustomStarter2",
                    "Starters")
                    .prerequisite("RandomizeStarters", matchesEnum(StartersMod.CUSTOM))
                    .supportedMaximums(rh -> rh.getSpecies().size() - 1)
                    .variableDefaultValue(rh -> rh.getStarters().get(1).getNumber())
                    .build(),
            new SpeciesIndexSettingDefinition.Builder<>(
                    "CustomStarter3",
                    "Starters")
                    .prerequisite("RandomizeStarters", matchesEnum(StartersMod.CUSTOM))
                    .supported(rh -> rh.getStarters().size() > 2)
                    .supportedMaximums(rh -> rh.getSpecies().size() - 1)
                    .variableDefaultValue(rh -> rh.getStarters().get(2).getNumber())
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    "StartersTypeRestriction",
                    "Starters",
                    StartersTypeMod.NONE)
                    .prerequisite(anyStarterIsRandomRestriction)
                    .restrictedStates(Map.of(
                            StartersTypeMod.FIRE_WATER_GRASS, allStartersAreRandomRestriction,
                            StartersTypeMod.TRIANGLE, allStartersAreRandomRestriction))
                    .supportedStates(Map.of(
                            StartersTypeMod.FIRE_WATER_GRASS, RomHandler::hasStarterTypeTriangleSupport,
                            StartersTypeMod.TRIANGLE, RomHandler::hasStarterTypeTriangleSupport))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "NoDualTypeStarters",
                    "Starters",
                    false)
                    .prerequisite(anyStarterIsRandomRestriction)
                    .build(),
            new TypeOrRandomSettingDefinition.Builder<>(
                    "SingleStarterType",
                    "Starters")
                    .prerequisite("StartersTypeRestriction", matchesEnum(StartersTypeMod.SINGLE_TYPE))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "StartersNoLegendaries",
                    "Starters",
                    false)
                    .prerequisite(anyStarterIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeStarterHeldItems",
                    "Starters",
                    false)
                    .supported(RomHandler::supportsStarterHeldItems)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BanBadStarterHeldItems",
                    "Starters",
                    false)
                    .prerequisite("RandomizeStarterHeldItems", isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "LimitStartersMinimumBST",
                    "Starters",
                    false)
                    .prerequisite(anyStarterIsRandomRestriction)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "LimitStartersMinimumBSTValue",
                    "Starters",
                    STARTER_MIN_BST,
                    1, BaseStats.STAT_MAX * 6)
                    .prerequisite("LimitStartersMinimumBST", isTrue)
                    .variableDefaultValue(overrideForGeneration(1, STARTER_MIN_BST_GEN_1))
                    .supportedMaximums(overrideForGeneration(1, BaseStats.STAT_MAX * 5))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "LimitStartersMaximumBST",
                    "Starters",
                    false)
                    .prerequisite(anyStarterIsRandomRestriction)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "LimitStartersMaximumBSTValue",
                    "Starters",
                    STARTER_MAX_BST,
                    1, BaseStats.STAT_MAX * 6)
                    .prerequisite("LimitStartersMaximumBST", isTrue)
                    .variableDefaultValue(overrideForGeneration(1, STARTER_MAX_BST_GEN_1))
                    .supportedMaximums(overrideForGeneration(1, BaseStats.STAT_MAX * 5))
                    .build(),
            // TODO also: LimitStartersMinimumBSTValue, LimitStartersMaximumBSTValue
            //  should ideally have a special enablement constraint, such that Minimum's max is the value
            //  of Maximum, and Maximum's min is the value of Minimum.

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeStaticPokemon",
                    "StaticPokemon",
                    StaticPokemonMod.UNCHANGED)
                    .supported(RomHandler::canChangeStaticPokemon)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "StaticPokemonRandomize600PlusBST",
                    "StaticPokemon",
                    false)
                    .prerequisite("RandomizeStaticPokemon", notMatchesEnum(StaticPokemonMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "StaticPokemonLimitMainGameLegendaries",
                    "StaticPokemon",
                    false)
                    .prerequisite("RandomizeStaticPokemon", matchesEnum(StaticPokemonMod.SIMILAR_STRENGTH))
                    .supported(RomHandler::hasMainGameLegendaries)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "StaticPokemonAllowAltFormes",
                    "StaticPokemon",
                    false)
                    .prerequisite("RandomizeStaticPokemon", notMatchesEnum(StaticPokemonMod.UNCHANGED))
                    .supported(RomHandler::hasStarterAltFormes)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "StaticPokemonSwapMegaEvolvables",
                    "StaticPokemon",
                    false)
                    .prerequisite("RandomizeStaticPokemon", notMatchesEnum(StaticPokemonMod.UNCHANGED))
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "StaticPokemonFixMusic",
                    "StaticPokemon",
                    false)
                    .prerequisite("RandomizeStaticPokemon", notMatchesEnum(StaticPokemonMod.UNCHANGED))
                    .supported(RomHandler::hasStaticMusicFix)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "StaticPokemonLevelModifierPercentage",
                    "StaticPokemon",
                    0,
                    -100, 155)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeInGameTrades",
                    "InGameTrades",
                    InGameTradesMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "InGameTradesRandomizeNicknames",
                    "InGameTrades",
                    false)
                    .prerequisite("RandomizeInGameTrades", notMatchesEnum(InGameTradesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "InGameTradesRandomizeOTs",
                    "InGameTrades",
                    false)
                    .prerequisite("RandomizeInGameTrades", notMatchesEnum(InGameTradesMod.UNCHANGED))
                    .supported(notOfGeneration(1))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "InGameTradesRandomizeIVs",
                    "InGameTrades",
                    false)
                    .prerequisite("RandomizeInGameTrades", notMatchesEnum(InGameTradesMod.UNCHANGED))
                    .supported(notOfGeneration(1))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "InGameTradesRandomizeHeldItems",
                    "InGameTrades",
                    false)
                    .prerequisite("RandomizeInGameTrades", notMatchesEnum(InGameTradesMod.UNCHANGED))
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
            new SimpleSettingRestriction<>("RandomizePokemonMovesets",
                    matchesEnum(MovesetsMod.RANDOM_PREFER_SAME_TYPE)),
            new SimpleSettingRestriction<>("RandomizePokemonMovesets",
                    matchesEnum(MovesetsMod.COMPLETELY_RANDOM))
    );

    private static final SettingRestriction noMetronomeModeRestriction = new SimpleSettingRestriction<>(
            "RandomizePokemonMovesets", notMatchesEnum(MovesetsMod.METRONOME_ONLY)
    );

    public static final List<SettingDefinition<?>> MOVES_AND_MOVESETS = List.of(
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeMovePower",
                    "MoveData",
                    false)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeMoveAccuracy",
                    "MoveData",
                    false)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeMovePP",
                    "MoveData",
                    false)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeMoveCategory",
                    "MoveData",
                    false)
                    .supported(RomHandler::hasPhysicalSpecialSplit)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeMoveNames",
                    "MoveData",
                    false)
                    .supported(RomHandler::isEnglish)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "UpdateMoves",
                    "MoveData",
                    false)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "UpdateMovesToGeneration",
                    "MoveData",
                    9,
                    2, 9)
                    .prerequisite("UpdateMoves", isTrue)
                    .supportedMinimums(rh -> rh.generationOfPokemon() + 1)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizePokemonMovesets",
                    "PokemonMovesets",
                    MovesetsMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "GuaranteedLevel1Moves",
                    "PokemonMovesets",
                    false)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .supported(RomHandler::supportsFourStartingMoves)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "GuaranteedLevel1MovesCount",
                    "PokemonMovesets",
                    2,
                    2, 4)
                    .prerequisite("GuaranteedLevel1Moves", isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "MovesetsReorderDamagingMoves",
                    "PokemonMovesets",
                    false)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "MovesetsNoGameBreakingMoves",
                    "PokemonMovesets",
                    false)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "MovesetsForceGoodDamagingMovesPercentage",
                    "PokemonMovesets",
                    0,
                    0, 100)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "EvolutionMovesForAllPokemon",
                    "PokemonMovesets",
                    false)
                    .prerequisite(randomPokemonMovesetsRestriction)
                    .supported(atLeastGeneration(7))
                    .build()
    );

    //endregion

    //region foe pokemon

    //To consider: should Totem pokemon be here, or in Wild?
    //(There's more space here, if nothing else.)

    public enum TrainersMod {
        UNCHANGED, RANDOM, DISTRIBUTED, MAINPLAYTHROUGH, TYPE_THEMED,
        TYPE_THEMED_ELITE4_GYMS, KEEP_THEMED, KEEP_THEME_OR_PRIMARY
    }

    private static final SettingRestriction anyTrainerPokemonIsRandomRestriction = new MultiSettingRestriction(
            true, false,
            new SimpleSettingRestriction<>("RandomizeTrainerPokemon", notMatchesEnum(TrainersMod.UNCHANGED)),
            new SimpleSettingRestriction<>("AdditionalPokemonForBossTrainers", greaterThanValue(0)),
            new SimpleSettingRestriction<>("AdditionalPokemonForImportantTrainers", greaterThanValue(0)),
            new SimpleSettingRestriction<>("AdditionalPokemonForRegularTrainers", greaterThanValue(0))
    );

    private static final SettingRestriction addHeldItemsToAnyTrainerRestriction = new MultiSettingRestriction(
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
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeTrainerPokemon",
                    "TrainerPokemon",
                    TrainersMod.UNCHANGED)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "BetterMovesetsForBossTrainers",
                    "TrainerPokemon",
                    false)
                    .prerequisite(noMetronomeModeRestriction)
                    .supported(RomHandler::canGiveCustomMovesetsToBossTrainers)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BetterMovesetsForImportantTrainers",
                    "TrainerPokemon",
                    false)
                    .prerequisite(noMetronomeModeRestriction)
                    .supported(RomHandler::canGiveCustomMovesetsToImportantTrainers)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BetterMovesetsForRegularTrainers",
                    "TrainerPokemon",
                    false)
                    .prerequisite(noMetronomeModeRestriction)
                    .supported(RomHandler::canGiveCustomMovesetsToRegularTrainers)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "AdditionalPokemonForBossTrainers",
                    "TrainerPokemon",
                    0,
                    0, 5)
                    .supported(RomHandler::canAddPokemonToBossTrainers)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "AdditionalPokemonForImportantTrainers",
                    "TrainerPokemon",
                    0,
                    0, 5)
                    .supported(RomHandler::canAddPokemonToImportantTrainers)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "AdditionalPokemonForRegularTrainers",
                    "TrainerPokemon",
                    0,
                    0, 5)
                    .supported(RomHandler::canAddPokemonToRegularTrainers)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "AddHeldItemsToBossTrainers",
                    "TrainerPokemon",
                    false)
                    .supported(RomHandler::canAddHeldItemsToBossTrainers)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "AddHeldItemsToImportantTrainers",
                    "TrainerPokemon",
                    false)
                    .supported(RomHandler::canAddHeldItemsToImportantTrainers)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "AddHeldItemsToRegularTrainers",
                    "TrainerPokemon",
                    false)
                    .supported(RomHandler::canAddHeldItemsToRegularTrainers)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerHeldItemsConsumableOnly",
                    "TrainerPokemon",
                    false)
                    .prerequisite(addHeldItemsToAnyTrainerRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerHeldItemsSensible",
                    "TrainerPokemon",
                    false)
                    .prerequisite(addHeldItemsToAnyTrainerRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerHeldItemsToHighestLevelOnly",
                    "TrainerPokemon",
                    false)
                    .prerequisite(addHeldItemsToAnyTrainerRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "ForceDiverseTypesForBossTrainers",
                    "TrainerPokemon",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>("RandomizeTrainerPokemon",
                                            notMatchesEnum(TrainersMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>("AdditionalPokemonForBossTrainers",
                                            greaterThanValue(0))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "ForceDiverseTypesForImportantTrainers",
                    "TrainerPokemon",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>("RandomizeTrainerPokemon",
                                            notMatchesEnum(TrainersMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>("AdditionalPokemonForImportantTrainers",
                                            greaterThanValue(0))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "ForceDiverseTypesForRegularTrainers",
                    "TrainerPokemon",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>("RandomizeTrainerPokemon",
                                            notMatchesEnum(TrainersMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>("AdditionalPokemonForRegularTrainers",
                                            greaterThanValue(0))))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RivalCarriesStarter",
                    "TrainerPokemon",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>("RandomizeStarters",
                                                    notMatchesEnum(StartersMod.UNCHANGED)),
                                            new SimpleSettingRestriction<>("RandomizeTrainerPokemon",
                                                    notMatchesEnum(TrainersMod.UNCHANGED))),
                                    new SimpleSettingRestriction<>("RandomizeSpeciesEvolutions",
                                            notMatchesEnum(EvolutionsMod.RANDOM_EVERY_LEVEL))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonSimilarStrength",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonAvoidDuplicates",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonWeighTypes",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonUseLocal",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonNoLegendaries",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonNoEarlyWonderGuard",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .supported(rh -> rh.abilitiesPerSpecies() != 0)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonAllowAltFormes",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .supported(RomHandler::hasFunctionalFormes)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonSwapMegaEvolvables",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .supported(RomHandler::hasMegaEvolutions)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TrainerPokemonRandomShinies",
                    "TrainerPokemon",
                    false)
                    .prerequisite(anyTrainerPokemonIsRandomRestriction)
                    .supported(atLeastGeneration(7))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "PokemonLeagueUniquePokemonCount",
                    "TrainerPokemon",
                    0,
                    0, 2)
                    // this prerequisite can't be "anyTrainerPokemonIsRandomRestriction", because that doesn't
                    // guarantee that the E4+champion get any random mons
                    .prerequisite("RandomizeTrainerPokemon", notMatchesEnum(TrainersMod.UNCHANGED))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "TrainersEvolveTheirPokemon",
                    "TrainerPokemon",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new SimpleSettingRestriction<>("RandomizeSpeciesEvolutions",
                                            notMatchesEnum(EvolutionsMod.RANDOM_EVERY_LEVEL))))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "TrainersEvolveTheirPokemonPercentage",
                    "TrainerPokemon",
                    0,
                    -100, 155)
                    .prerequisite("TrainersEvolveTheirPokemon", isTrue)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "TrainerPokemonLevelModifierPercentage",
                    "TrainerPokemon",
                    0,
                    -100, 155)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeBattleStyle",
                    "TrainerPokemon",
                    BattleStyle.Modification.UNCHANGED)
                    .supported(atLeastGeneration(3))
                    .build(),
            new EnumSettingDefinition.Builder<>(
                    "SingleStyleForBattles",
                    "TrainerPokemon",
                    BattleStyle.Style.SINGLE_BATTLE)
                    .prerequisite("RandomizeBattleStyle", matchesEnum(BattleStyle.Modification.SINGLE_STYLE))
                    .supportedStates(Map.of(
                            BattleStyle.Style.TRIPLE_BATTLE, ofGeneration(5, 6),
                            BattleStyle.Style.ROTATION_BATTLE, ofGeneration(5, 6)))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeTrainerNames",
                    "TrainerPokemon",
                    false)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeTrainerClassNames",
                    "TrainerPokemon",
                    false)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeTotemPokemon",
                    "TotemPokemon",
                    TotemPokemonMod.UNCHANGED)
                    .supported(RomHandler::hasTotemPokemon)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeAllyPokemon",
                    "TotemPokemon",
                    AllyPokemonMod.UNCHANGED)
                    .supported(RomHandler::hasTotemPokemon)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeTotemAuras",
                    "TotemPokemon",
                    AuraMod.UNCHANGED)
                    .supported(RomHandler::hasTotemPokemon)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeTotemHeldItems",
                    "TotemPokemon",
                    false)
                    .supported(RomHandler::hasTotemPokemon)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TotemPokemonAllowAltFormes",
                    "TotemPokemon",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>("RandomizeTotemPokemon",
                                            notMatchesEnum(TotemPokemonMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>("RandomizeAllyPokemon",
                                            notMatchesEnum(AllyPokemonMod.UNCHANGED))))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "TotemPokemonLevelModifierPercentage",
                    "TotemPokemon",
                    0,
                    -100, 155)
                    .supported(RomHandler::hasTotemPokemon)
                    .build()
    );

    //endregion

    //region wild encounters

    public enum WildPokemonZoneMod {
        NONE, ENCOUNTER_SET, MAP, NAMED_LOCATION, GAME
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
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeWildPokemon",
                    "WildPokemon",
                    false)
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    "WildPokemonZone",
                    "WildPokemon",
                    WildPokemonZoneMod.NONE)
                    .prerequisite("RandomizeWildPokemon", isTrue)
                    .supportedStates(Map.of(
                            WildPokemonZoneMod.ENCOUNTER_SET, rh -> !rh.hasMapIndices(),
                            WildPokemonZoneMod.MAP, RomHandler::hasMapIndices,
                            WildPokemonZoneMod.NAMED_LOCATION, RomHandler::hasEncounterLocations))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "SplitWildZoneByEncounterTypes",
                    "WildPokemon",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    new SimpleSettingRestriction<>("WildPokemonZone",
                                            notMatchesEnum(WildPokemonZoneMod.NONE)),
                                    new SimpleSettingRestriction<>("WildPokemonZone",
                                            notMatchesEnum(WildPokemonZoneMod.ENCOUNTER_SET))))
                    .build(),
            new SimpleSettingDefinition.Builder<>( // this setting is definitely zone-y
                    "UseTimeBasedEncounters",
                    "WildPokemon",
                    true)
                    .prerequisite("RandomizeWildPokemon", isTrue)
                    .supported(RomHandler::hasTimeBasedEncounters)
                    .build(),

            new EnumSettingDefinition.Builder<>(
                    "WildPokemonTypeRestriction",
                    "WildPokemon",
                    WildPokemonTypeMod.NONE)
                    .prerequisite("RandomizeWildPokemon", isTrue)
                    .restrictedStates(Map.of(
                            WildPokemonTypeMod.RANDOM_THEMES,
                            new SimpleSettingRestriction<>("WildPokemonZone",
                                    notMatchesEnum(WildPokemonZoneMod.GAME))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "KeepWildTypeThemes",
                    "WildPokemon",
                    false)
                    .prerequisite("RandomizeWildPokemon", isTrue)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "WildPokemonEvolutionRestriction",
                    "WildPokemon",
                    WildPokemonEvolutionMod.NONE)
                    .prerequisite("RandomizeWildPokemon", isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "KeepWildEvolutionFamilies",
                    "WildPokemon",
                    false)
                    .prerequisite("WildPokemonZone", notMatchesEnum(WildPokemonZoneMod.NONE))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "WildPokemonNoLegendaries",
                    "WildPokemon",
                    false)
                    .prerequisite("RandomizeWildPokemon", isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "WildPokemonAllowAltFormes",
                    "WildPokemon",
                    false)
                    .prerequisite("RandomizeWildPokemon", isTrue)
                    .supported(RomHandler::hasWildAltFormes)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "WildPokemonSimilarStrength",
                    "WildPokemon",
                    false)
                    .prerequisite("RandomizeWildPokemon", isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BalanceLowLevelEncounters",
                    "WildPokemon",
                    false)
                    .prerequisite("WildPokemonSimilarStrength", isTrue)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "CatchEmAllMode",
                    "WildPokemon",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    new SimpleSettingRestriction<>("RandomizeWildPokemon", isTrue),
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>("WildPokemonZone",
                                                    notMatchesEnum(WildPokemonZoneMod.GAME)),
                                            new SimpleSettingRestriction<>("SplitWildZoneByEncounterTypes", isTrue))))
                    .build(),

            // Below: "wild pokemon" settings that don't require random wild pokemon
            new SimpleSettingDefinition.Builder<>(
                    "WildPokemonCatchRate",
                    "WildPokemon",
                    CatchRateMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeWildPokemonHeldItems",
                    "WildPokemon",
                    false)
                    .supported(notOfGeneration(1))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BanBadWildPokemonHeldItems",
                    "WildPokemon",
                    false)
                    .prerequisite("RandomizeWildPokemonHeldItems", isTrue)
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "WildPokemonLevelModifierPercentage",
                    "WildPokemon",
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
                    "RandomizeTMMoves",
                    "TMsAndHMs",
                    TMMovesMod.UNCHANGED)
                    .prerequisite(noMetronomeModeRestriction)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TMsNoGameBreakingMoves",
                    "TMsAndHMs",
                    false)
                    .prerequisite("RandomizeTMMoves", notMatchesEnum(TMMovesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "KeepFieldMoveTMs",
                    "TMsAndHMs",
                    false)
                    .prerequisite("RandomizeTMMoves", notMatchesEnum(TMMovesMod.UNCHANGED))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "TMsForceGoodDamagingMovesPercentage",
                    "TMsAndHMs",
                    0,
                    0, 100)
                    .prerequisite("RandomizeTMMoves", notMatchesEnum(TMMovesMod.UNCHANGED))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeTMHMCompatibility",
                    "TMsAndHMs",
                    TMsHMsCompatibilityMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TMLevelupMoveSanity",
                    "TMsAndHMs",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>("RandomizePokemonMovesets",
                                            notMatchesEnum(MovesetsMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>("RandomizeTMMoves",
                                            notMatchesEnum(TMMovesMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>("RandomizeTMHMCompatibility",
                                            matchesEnum(TMsHMsCompatibilityMod.COMPLETELY_RANDOM)),
                                    new SimpleSettingRestriction<>("RandomizeTMHMCompatibility",
                                            matchesEnum(TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "TMHMCompatibilityFollowEvolutions",
                    "TMsAndHMs",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>("RandomizeTMHMCompatibility",
                                                    matchesEnum(TMsHMsCompatibilityMod.COMPLETELY_RANDOM)),
                                            new SimpleSettingRestriction<>("RandomizeTMHMCompatibility",
                                                    matchesEnum(TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE)),
                                            new SimpleSettingRestriction<>("TMLevelupMoveSanity", isTrue))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "FullHMCompatibility",
                    "TMsAndHMs",
                    false)
                    .prerequisite("RandomizeTMHMCompatibility", notMatchesEnum(TMsHMsCompatibilityMod.FULL))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeMoveTutorMoves",
                    "MoveTutors",
                    MoveTutorMovesMod.UNCHANGED)
                    .prerequisite(noMetronomeModeRestriction)
                    .supported(RomHandler::hasMoveTutors)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "MoveTutorsNoGameBreakingMoves",
                    "MoveTutors",
                    false)
                    .prerequisite("RandomizeMoveTutorMoves", notMatchesEnum(MoveTutorMovesMod.UNCHANGED))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "KeepFieldMoveTutors",
                    "MoveTutors",
                    false)
                    .prerequisite("RandomizeMoveTutorMoves", notMatchesEnum(MoveTutorMovesMod.UNCHANGED))
                    .build(),
            new NumericSettingDefinition.Builder<>(
                    "MoveTutorsForceGoodDamagingMovesPercentage",
                    "MoveTutors",
                    0,
                    0, 100)
                    .prerequisite("RandomizeMoveTutorMoves", notMatchesEnum(MoveTutorMovesMod.UNCHANGED))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeMoveTutorCompatibility",
                    "MoveTutors",
                    MoveTutorsCompatibilityMod.UNCHANGED)
                    .supported(RomHandler::hasMoveTutors)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "MoveTutorLevelupMoveSanity",
                    "MoveTutors",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>("RandomizePokemonMovesets",
                                            notMatchesEnum(MovesetsMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>("RandomizeMoveTutorMoves",
                                            notMatchesEnum(MoveTutorMovesMod.UNCHANGED)),
                                    new SimpleSettingRestriction<>("RandomizeMoveTutorCompatibility",
                                            matchesEnum(MoveTutorsCompatibilityMod.COMPLETELY_RANDOM)),
                                    new SimpleSettingRestriction<>("RandomizeMoveTutorCompatibility",
                                            matchesEnum(MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE))))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "MoveTutorCompatibilityFollowEvolutions",
                    "MoveTutors",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(false, false,
                                    notEvolveEveryLevelRestriction,
                                    new MultiSettingRestriction(true, false,
                                            new SimpleSettingRestriction<>("RandomizeMoveTutorCompatibility",
                                                    matchesEnum(MoveTutorsCompatibilityMod.COMPLETELY_RANDOM)),
                                            new SimpleSettingRestriction<>("RandomizeMoveTutorCompatibility",
                                                    matchesEnum(MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE)),
                                            new SimpleSettingRestriction<>("MoveTutorLevelupMoveSanity", isTrue))))
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
                    "RandomizeFieldItems",
                    "FieldItems",
                    FieldItemsMod.UNCHANGED)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BanBanFieldItems",
                    "FieldItems",
                    false)
                    .prerequisite(
                            new MultiSettingRestriction(true, false,
                                    new SimpleSettingRestriction<>("RandomizeFieldItems",
                                            matchesEnum(FieldItemsMod.RANDOM)),
                                    new SimpleSettingRestriction<>("RandomizeFieldItems",
                                            matchesEnum(FieldItemsMod.RANDOM_EVEN))))
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizeSpecialShopItems",
                    "ShopItems",
                    ShopItemsMod.UNCHANGED)
                    .supported(RomHandler::hasShopSupport)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BanBadShopItems",
                    "ShopItems",
                    false)
                    .prerequisite("RandomizeSpecialShopItems", matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BanRegularShopItems",
                    "ShopItems",
                    false)
                    .prerequisite("RandomizeSpecialShopItems", matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BanOverpoweredShopItems",
                    "ShopItems",
                    false)
                    .prerequisite("RandomizeSpecialShopItems", matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "GuaranteeEvolutionItemsInShops",
                    "ShopItems",
                    false)
                    .prerequisite("RandomizeSpecialShopItems", matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "GuaranteeXItemsInShops",
                    "ShopItems",
                    false)
                    .prerequisite("RandomizeSpecialShopItems", matchesEnum(ShopItemsMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BalanceShopItemPrices",
                    "ShopItems",
                    false)
                    .supported(RomHandler::hasShopSupport)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "AddCheapRareCandiesToShops",
                    "ShopItems",
                    false)
                    .supported(RomHandler::canChangeShopSizes)
                    .build(),

            new SimpleSettingDefinition.Builder<>(
                    "RandomizePickupItems",
                    "PickupItems",
                    PickupItemsMod.UNCHANGED)
                    .supported(rh -> rh.abilitiesPerSpecies() > 0)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "BanBadPickupItems",
                    "PickupItems",
                    false)
                    .prerequisite("RandomizePickupItems", matchesEnum(PickupItemsMod.RANDOM))
                    .build()
    );

    //endregion

    //region type effectiveness

    public enum TypeEffectivenessMod {
        UNCHANGED, RANDOM, RANDOM_BALANCED, KEEP_IDENTITIES, INVERSE
    }

    public static final List<SettingDefinition<?>> TYPES = List.of(
            new SimpleSettingDefinition.Builder<>(
                    "RandomizeTypeEffectiveness",
                    "TypeEffectiveness",
                    TypeEffectivenessMod.UNCHANGED)
                    .supported(RomHandler::hasTypeEffectivenessSupport)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "InverseTypesRandomImmunities",
                    "TypeEffectiveness",
                    false)
                    .prerequisite("RandomizeTypeEffectiveness", matchesEnum(TypeEffectivenessMod.INVERSE))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "UpdateTypeEffectiveness",
                    "TypeEffectiveness",
                    false)
                    .supported(rh -> rh.hasTypeEffectivenessSupport()
                            && rh.generationOfPokemon() < TypeEffectivenessUpdater.UPDATE_TO_GEN)
                    .build()
    );

    //endregion

    //region graphics

    public enum PokemonPalettesMod {
        UNCHANGED, RANDOM
    }

    public static final List<SettingDefinition<?>> GRAPHICS = List.of(
            new SimpleSettingDefinition.Builder<>(
                    "RandomizePokemonPalettes",
                    "PokemonPalettes",
                    PokemonPalettesMod.UNCHANGED)
                    .supported(RomHandler::hasPokemonPaletteSupport)
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "PokemonPalettesFollowTypes",
                    "PokemonPalettes",
                    false)
                    .prerequisite("RandomizePokemonPalettes", matchesEnum(PokemonPalettesMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "PokemonPalettesFollowEvolutions",
                    "PokemonPalettes",
                    false)
                    .prerequisite("RandomizePokemonPalettes", matchesEnum(PokemonPalettesMod.RANDOM))
                    .build(),
            new SimpleSettingDefinition.Builder<>(
                    "PokemonPalettesShinyFromNormal",
                    "PokemonPalettes",
                    false)
                    .prerequisite("RandomizePokemonPalettes", matchesEnum(PokemonPalettesMod.RANDOM))
                    .supported(notOfGeneration(1))
                    .build()

            // TODO: what to do with CPGs? Should they be included here?
    );

    //endregion graphics

    //region misc tweaks

    public static final List<SettingDefinition<?>> MISC_TWEAKS = List.of(
            miscTweakDefinition("BWEXPPatch", MiscTweak.BW_EXP_PATCH),
            miscTweakDefinition("NerfXAccuracy", MiscTweak.NERF_X_ACCURACY),
            miscTweakDefinition("FixCritRate", MiscTweak.FIX_CRIT_RATE),
            miscTweakDefinition("FastestText", MiscTweak.FASTEST_TEXT),
            miscTweakDefinition("RunningShoesIndoors", MiscTweak.RUNNING_SHOES_INDOORS),
            miscTweakDefinition("RandomizePCPotion", MiscTweak.RANDOMIZE_PC_POTION),
            miscTweakDefinition("AllowPikachuEvolution", MiscTweak.ALLOW_PIKACHU_EVOLUTION),
            miscTweakDefinition("NationalDexAtStart", MiscTweak.NATIONAL_DEX_AT_START),
            miscTweakDefinition("FastEggHatching", MiscTweak.FAST_EGG_HATCHING),
            miscTweakDefinition("ForceChallengeMode", MiscTweak.FORCE_CHALLENGE_MODE),
            miscTweakDefinition("LowerCasePokemonNames", MiscTweak.LOWER_CASE_POKEMON_NAMES),
            miscTweakDefinition("RandomizeCatchingTutorial", MiscTweak.RANDOMIZE_CATCHING_TUTORIAL),
            miscTweakDefinition("BanLuckyEgg", MiscTweak.BAN_LUCKY_EGG),
            miscTweakDefinition("NoFreeLuckyEgg", MiscTweak.NO_FREE_LUCKY_EGG),
            miscTweakDefinition("BanBigManiacItems", MiscTweak.BAN_BIG_MANIAC_ITEMS),
            miscTweakDefinition("SOSBattlesForAll", MiscTweak.SOS_BATTLES_FOR_ALL),
            miscTweakDefinition("BalanceStaticLevels", MiscTweak.BALANCE_STATIC_LEVELS),
            miscTweakDefinition("RetainAltFormes", MiscTweak.RETAIN_ALT_FORMES),
            miscTweakDefinition("RunWithoutRunningShoes", MiscTweak.RUN_WITHOUT_RUNNING_SHOES),
            miscTweakDefinition("FasterHPAndEXPBars", MiscTweak.FASTER_HP_AND_EXP_BARS),
            miscTweakDefinition("FastDistortionWorld", MiscTweak.FAST_DISTORTION_WORLD),
            miscTweakDefinition("UpdateRotomFormeTyping", MiscTweak.UPDATE_ROTOM_FORME_TYPING),
            miscTweakDefinition("DisableLowHPMusic", MiscTweak.DISABLE_LOW_HP_MUSIC),
            miscTweakDefinition("ReusableTMs", MiscTweak.REUSABLE_TMS),
            miscTweakDefinition("ForgettableHMs", MiscTweak.FORGETTABLE_HMS),
            miscTweakDefinition("NoEVYields", MiscTweak.NO_EV_YIELDS)
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
