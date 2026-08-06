package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.*;
import com.uprfvx.random.settings.restrictions.*;
import com.uprfvx.random.updaters.TypeEffectivenessUpdater;
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
//TODO: SpeciesSettingDefinition. That one will be real weird.

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
        public static final String SPECIES_ABILITIES_FOLLOW_MEGA_EVO ="SpeciesAbilitiesFollowMegaEvolutions";
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

    public static final List<SettingDefinition<?>> GENERAL_OPTIONS = List.of(
            new SimpleSettingDefinition<>(
                    Names.LIMIT_POKEMON,
                    Categories.GENERAL_OPTIONS,
                    false,
                    null,
                    notOfGeneration(1)
            ), //TODO: might be able to eliminate this setting and just use the "AllowGenerationX" settings (inverted)
            new SimpleSettingDefinition<>(
                    Names.NO_RANDOM_INTRO_MON,
                    Categories.GENERAL_OPTIONS, //TODO: move to misc. tweaks?
                    false,
                    null,
                    null
            ),
                            //TODO: make this setting actually work?
                            // "this setting" is race mode?
                            // I believe I was referring to "NoRandomIntroMon" but I cannot recall for sure anymore.
                            //TODO investigate this todo i guess
            new SimpleSettingDefinition<>(
                    Names.RACE_MODE,
                    Categories.GENERAL_OPTIONS,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.NO_IRREGULAR_ALT_FORMES,
                    Categories.GENERAL_OPTIONS,
                    false,
                    null,
                    null
            ),

            new SimpleSettingDefinition<>(
                    Names.ALLOW_GENERATION_1,
                    Categories.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Names.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(2)
            ),
            new SimpleSettingDefinition<>(
                    Names.ALLOW_GENERATION_2,
                    Categories.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Names.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(2)
            ),
            new SimpleSettingDefinition<>(
                    Names.ALLOW_GENERATION_3,
                    Categories.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Names.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(3)
            ),
            new SimpleSettingDefinition<>(
                    Names.ALLOW_GENERATION_4,
                    Categories.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Names.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(4)
            ),
            new SimpleSettingDefinition<>(
                    Names.ALLOW_GENERATION_5,
                    Categories.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Names.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(5)
            ),
            new SimpleSettingDefinition<>(
                    Names.ALLOW_GENERATION_6,
                    Categories.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Names.LIMIT_POKEMON, isTrue),
                    atLeastGeneration(6)
            ),
            new SimpleSettingDefinition<>(
                    Names.ALLOW_GENERATION_7,
                    Categories.LIMIT_POKEMON,
                    false,
                    new SimpleSettingRestriction<>(Names.LIMIT_POKEMON, isTrue),
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
                    Names.RANDOMIZE_BASE_STAT_TOTALS,
                    Categories.BASE_STAT_TOTALS,
                    BSTMod.UNCHANGED,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    Names.BST_BUFF_NERF_PERCENT,
                    Categories.BASE_STAT_TOTALS,
                    0,
                    new EnumMatchRestriction<>(Names.RANDOMIZE_BASE_STAT_TOTALS, BSTMod.RANDOM_BUFF_NERF),
                    null,
                    0, 50
            ),
            new SimpleSettingDefinition<>(
                    Names.BSTS_FOLLOW_EVOLUTION,
                    Categories.BASE_STAT_TOTALS,
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>(Names.RANDOMIZE_BASE_STAT_TOTALS, BSTMod.RANDOM_BUFF_NERF),
                            new EnumMatchRestriction<>(Names.RANDOMIZE_BASE_STAT_TOTALS, BSTMod.SHUFFLE)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.BST_SHUFFLE_SEPARATE_LEGENDARIES,
                    Categories.BASE_STAT_TOTALS,
                    false,
                    new EnumMatchRestriction<>(Names.RANDOMIZE_BASE_STAT_TOTALS, BSTMod.SHUFFLE),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Names.RANDOMIZE_BASE_STAT_DISTRIBUTIONS,
                    Categories.BASE_STAT_DISTRIBUTION,
                    BaseStatDistributionsMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.BSDS_FOLLOW_EVOLUTION,
                    Categories.BASE_STAT_DISTRIBUTION,
                    false,
                    new EnumMatchRestriction<>(Names.RANDOMIZE_BASE_STAT_DISTRIBUTIONS, BaseStatDistributionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.BSDS_FOLLOW_MEGA_EVOS,
                    Categories.BASE_STAT_DISTRIBUTION,
                    false,
                    new SimpleSettingRestriction<>(Names.BSDS_FOLLOW_EVOLUTION, isTrue),
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    Names.BSDS_ASSIGN_EVO_STATS_RANDOMLY,
                    Categories.BASE_STAT_DISTRIBUTION,
                    false,
                    new SimpleSettingRestriction<>(Names.BSDS_FOLLOW_EVOLUTION, isTrue),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Names.UPDATE_BASE_STATS,
                    Categories.UPDATE_BASE_STATS,
                    false,
                    null,
                    notOfGeneration(1)),
            new NumericSettingDefinition<>(
                    Names.UPDATE_STATS_GENERATION,
                    Categories.UPDATE_BASE_STATS,
                    9,
                    new SimpleSettingRestriction<>(Names.UPDATE_BASE_STATS, isTrue),
                    null,
                    6, 9,
                    null,
                    null,
                    higherValueThanGeneration(6, 7),
                    null
            ),

            new SimpleSettingDefinition<>(
                    Names.SPECIES_RANDOMIZE_TYPES,
                    Categories.SPECIES_TYPES,
                    SpeciesTypesMod.UNCHANGED,
                    null,
                    null
            ), // TODO: disable follow evos
            new SimpleSettingDefinition<>(
                    Names.SPECIES_FORCE_DUAL_TYPES,
                    Categories.SPECIES_TYPES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_TYPES, SpeciesTypesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_TYPES_FOLLOW_MEGA_EVO,
                    Categories.SPECIES_TYPES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_TYPES, SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS),
                    RomHandler::hasMegaEvolutions
            ),

            new SimpleSettingDefinition<>(
                    Names.SPECIES_RANDOMIZE_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    AbilitiesMod.UNCHANGED,
                    null,
                    rh -> rh.abilitiesPerSpecies() != 0
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_ABILITIES_FOLLOW_EVO,
                    Categories.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_ABILITIES_FOLLOW_MEGA_EVO,
                    Categories.SPECIES_ABILITIES,
                    false,
                    new SimpleSettingRestriction<>(Names.SPECIES_ABILITIES_FOLLOW_EVO, isTrue),
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_ABILITIES_COMBINE_DUPES,
                    Categories.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_FORCE_TWO_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_BAN_WONDER_GUARD,
                    Categories.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_BAN_TRAPPING_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_BAN_NEGATIVE_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_BAN_MINOR_ABILITIES,
                    Categories.SPECIES_ABILITIES,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_ABILITIES, AbilitiesMod.UNCHANGED, false),
                    null
            ),

            // TODO: remember that evo randomization should affect a bunch of options

            new SimpleSettingDefinition<>(
                    Names.SPECIES_RANDOMIZE_EVOLUTIONS,
                    Categories.SPECIES_EVOLUTIONS,
                    EvolutionsMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_SIMILAR_STRENGTH,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS, EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_SAME_TYPE,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS, EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_MAX_THREE,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS, EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_NO_CONVERGENCE,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS, EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_FORCE_CHANGE,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS, EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_FORCE_GROWTH,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS, EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_ALLOW_ALTS,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS, EvolutionsMod.UNCHANGED, false),
                    ofGeneration(7)
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_ADJUST_LEVELS,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>(Names.RANDOMIZE_BASE_STAT_TOTALS, BSTMod.UNCHANGED, false),
                            new EnumMatchRestriction<>(Names.SPECIES_RANDOMIZE_EVOLUTIONS, EvolutionsMod.UNCHANGED, false)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_MAKE_POSSIBLE,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_MAKE_EASIER,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_EASIER_LEVEL,
                    Categories.SPECIES_EVOLUTIONS,
                    40,
                    new SimpleSettingRestriction<>(Names.SPECIES_EVOLUTIONS_MAKE_EASIER, isTrue),
                    null,
                    30, 65
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_USE_ESTIMATED_LEVELS,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    new MultiSettingRestriction(true, false,
                            new SimpleSettingRestriction<>(Names.SPECIES_EVOLUTIONS_MAKE_POSSIBLE, isTrue),
                            new SimpleSettingRestriction<>(Names.SPECIES_EVOLUTIONS_MAKE_EASIER, isTrue)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EVOLUTIONS_REMOVE_TIME_BASED,
                    Categories.SPECIES_EVOLUTIONS,
                    false,
                    null,
                    RomHandler::hasTimeBasedEvolutions
            ),

            new SimpleSettingDefinition<>(
                    Names.SPECIES_STANDARD_EXP_CURVE,
                    Categories.SPECIES_EXP_CURVES,
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_EXP_CURVE_TO_USE,
                    Categories.SPECIES_EXP_CURVES,
                    ExpCurve.MEDIUM_FAST,
                    new SimpleSettingRestriction<>(Categories.SPECIES_EXP_CURVES, isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    Names.SPECIES_STANDARDIZE_EXP_CURVE_EXTENT,
                    Categories.SPECIES_EXP_CURVES,
                    ExpCurveExtentMod.LEGENDARIES,
                    new SimpleSettingRestriction<>(Categories.SPECIES_EXP_CURVES, isTrue),
                    null
            )
    );

    //endregion

    //region given pokemon [currently Starters, Statics, & Trades]
    //TODO: move statics => Wild Pokemon supercategory & tab

    public enum StartersMod {
        UNCHANGED, CUSTOM, COMPLETELY_RANDOM, RANDOM_WITH_TWO_EVOLUTIONS, RANDOM_BASIC
    }

    public enum StartersTypeMod {
        NONE, FIRE_WATER_GRASS, TRIANGLE, UNIQUE, SINGLE_TYPE
    }

    private final static SettingRestriction anyStarterIsRandomRestriction = new MultiSettingRestriction(
            true, false,
            new MultiSettingRestriction(false, false,
                    new EnumMatchRestriction<>("RandomizeStarters", StartersMod.CUSTOM),
                    new MultiSettingRestriction(true, false,
                            new SimpleSettingRestriction<>("CustomStarter1",
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>("CustomStarter2",
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>("CustomStarter3",
                                    equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES))
                    )
            ),
            new EnumMatchRestriction<>("RandomizeStarters", StartersMod.COMPLETELY_RANDOM),
            new EnumMatchRestriction<>("RandomizeStarters", StartersMod.RANDOM_WITH_TWO_EVOLUTIONS),
            new EnumMatchRestriction<>("RandomizeStarters", StartersMod.RANDOM_BASIC)
    );

    private final static SettingRestriction allStartersAreRandomRestriction = new MultiSettingRestriction(
            true, false,
            new MultiSettingRestriction(false, false,
                    new EnumMatchRestriction<>("RandomizeStarters", StartersMod.CUSTOM),
                    new SimpleSettingRestriction<>("CustomStarter1",
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>("CustomStarter2",
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>("CustomStarter3",
                            equalsValue(SpeciesIndexSettingDefinition.RANDOM_SPECIES))
            ),
            new EnumMatchRestriction<>("RandomizeStarters", StartersMod.COMPLETELY_RANDOM),
            new EnumMatchRestriction<>("RandomizeStarters", StartersMod.RANDOM_WITH_TWO_EVOLUTIONS),
            new EnumMatchRestriction<>("RandomizeStarters", StartersMod.RANDOM_BASIC)
    );

    public enum StaticPokemonMod {
        UNCHANGED, RANDOM_MATCHING, COMPLETELY_RANDOM, SIMILAR_STRENGTH
    }

    public enum InGameTradesMod {
        UNCHANGED, RANDOMIZE_GIVEN, RANDOMIZE_GIVEN_AND_REQUESTED
    }

    public static final List<SettingDefinition<?>> STARTERS_STATICS_AND_TRADES = List.of(
            new SimpleSettingDefinition<>(
                    "RandomizeStarters",
                    "Starters",
                    StartersMod.UNCHANGED,
                    null,
                    null
            ),
            new SpeciesIndexSettingDefinition(
                    "CustomStarter1",
                    "Starters",
                    new EnumMatchRestriction<>("RandomizeStarters", StartersMod.CUSTOM),
                    null
            ),
            new SpeciesIndexSettingDefinition(
                    "CustomStarter2",
                    "Starters",
                    new EnumMatchRestriction<>("RandomizeStarters", StartersMod.CUSTOM),
                    null
            ),
            new SpeciesIndexSettingDefinition(
                    "CustomStarter3",
                    "Starters",
                    new EnumMatchRestriction<>("RandomizeStarters", StartersMod.CUSTOM),
                    rh -> rh.getStarters().size() > 2
            ),

            new EnumSettingDefinition<>(
                    "StartersTypeRestriction",
                    "Starters",
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
                    "NoDualTypeStarters",
                    "Starters",
                    false,
                    anyStarterIsRandomRestriction,
                    null
            ),
            new TypeOrRandomSettingDefinition(
                    "SingleStarterType",
                    "Starters",
                    new EnumMatchRestriction<>("StartersTypeRestriction", StartersTypeMod.SINGLE_TYPE),
                    null
            ),

            new SimpleSettingDefinition<>(
                    "StartersNoLegendaries",
                    "Starters",
                    false,
                    anyStarterIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeStarterHeldItems",
                    "Starters",
                    false,
                    null,
                    RomHandler::supportsStarterHeldItems
            ),
            new SimpleSettingDefinition<>(
                    "BanBadStarterHeldItems",
                    "Starters",
                    false,
                    new SimpleSettingRestriction<>("RandomizeStarterHeldItems", isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "LimitStartersMinimumBST",
                    "Starters",
                    false,
                    anyStarterIsRandomRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "LimitStartersMaximumBST",
                    "Starters",
                    false,
                    anyStarterIsRandomRestriction,
                    null
            ),
            // TODO: LimitStartersMinimumBSTValue, LimitStartersMaximumBSTValue;
            //  these need a variable default value depending on RomHandler

            new SimpleSettingDefinition<>(
                    "RandomizeStaticPokemon",
                    "StaticPokemon",
                    StaticPokemonMod.UNCHANGED,
                    null,
                    RomHandler::canChangeStaticPokemon
            ),
            new SimpleSettingDefinition<>(
                    "StaticPokemonRandomize600PlusBST",
                    "StaticPokemon",
                    false,
                    new EnumMatchRestriction<>("RandomizeStaticPokemon", StaticPokemonMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "StaticPokemonLimitMainGameLegendaries",
                    "StaticPokemon",
                    false,
                    new EnumMatchRestriction<>("RandomizeStaticPokemon", StaticPokemonMod.SIMILAR_STRENGTH),
                    RomHandler::hasMainGameLegendaries
            ),
            new SimpleSettingDefinition<>(
                    "StaticPokemonAllowAltFormes",
                    "StaticPokemon",
                    false,
                    new EnumMatchRestriction<>("RandomizeStaticPokemon", StaticPokemonMod.UNCHANGED, false),
                    RomHandler::hasStarterAltFormes
            ),
            new SimpleSettingDefinition<>(
                    "StaticPokemonSwapMegaEvolvables",
                    "StaticPokemon",
                    false,
                    new EnumMatchRestriction<>("RandomizeStaticPokemon", StaticPokemonMod.UNCHANGED, false),
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    "StaticPokemonFixMusic",
                    "StaticPokemon",
                    false,
                    new EnumMatchRestriction<>("RandomizeStaticPokemon", StaticPokemonMod.UNCHANGED, false),
                    RomHandler::hasStaticMusicFix
            ),
            new SimpleSettingDefinition<>(
                    "StaticPokemonLevelModifier",
                    "StaticPokemon",
                    false,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    "StaticPokemonLevelModifierPercentage",
                    "StaticPokemon",
                    100,
                    new SimpleSettingRestriction<>("StaticPokemonLevelModifier", isTrue),
                    null,
                    -100, 155
            ),

            new SimpleSettingDefinition<>(
                    "RandomizeInGameTrades",
                    "InGameTrades",
                    InGameTradesMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "InGameTradesRandomizeNicknames",
                    "InGameTrades",
                    false,
                    new EnumMatchRestriction<>("RandomizeInGameTrades", InGameTradesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "InGameTradesRandomizeOTs",
                    "InGameTrades",
                    false,
                    new EnumMatchRestriction<>("RandomizeInGameTrades", InGameTradesMod.UNCHANGED, false),
                    notOfGeneration(1)
            ),
            new SimpleSettingDefinition<>(
                    "InGameTradesRandomizeIVs",
                    "InGameTrades",
                    false,
                    new EnumMatchRestriction<>("RandomizeInGameTrades", InGameTradesMod.UNCHANGED, false),
                    notOfGeneration(1)
            ),
            new SimpleSettingDefinition<>(
                    "InGameTradesRandomizeHeldItems",
                    "InGameTrades",
                    false,
                    new EnumMatchRestriction<>("RandomizeInGameTrades", InGameTradesMod.UNCHANGED, false),
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
            new EnumMatchRestriction<>("RandomizePokemonMovesets", MovesetsMod.RANDOM_PREFER_SAME_TYPE),
            new EnumMatchRestriction<>("RandomizePokemonMovesets", MovesetsMod.COMPLETELY_RANDOM)
    );

    public static final List<SettingDefinition<?>> MOVES_AND_MOVESETS = List.of(
            new SimpleSettingDefinition<>(
                    "RandomizeMovePower",
                    "MoveData",
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeMoveAccuracy",
                    "MoveData",
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeMovePP",
                    "MoveData",
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeMoveCategory",
                    "MoveData",
                    false,
                    null,
                    RomHandler::hasPhysicalSpecialSplit
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeMoveNames",
                    "MoveData",
                    false,
                    null,
                    RomHandler::isEnglish
            ),
            new SimpleSettingDefinition<>(
                    "UpdateMoves",
                    "MoveData",
                    false,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    "UpdateMovesToGeneration",
                    "MoveData",
                    9,
                    new SimpleSettingRestriction<>("UpdateMoves", isTrue),
                    null,
                    2, 9,
                    null,
                    null,
                    higherValueThanGeneration(2,3,4,5,6,7),
                    null
            ),

            new SimpleSettingDefinition<>(
                    "RandomizePokemonMovesets",
                    "PokemonMovesets",
                    MovesetsMod.UNCHANGED,
                    null,
                    null
            ),
            // TODO: deal with metronome only mode, it should turn off options in other places
            new SimpleSettingDefinition<>(
                    "GuaranteedLevel1Moves",
                    "PokemonMovesets",
                    false,
                    randomPokemonMovesetsRestriction,
                    RomHandler::supportsFourStartingMoves
            ),
            new NumericSettingDefinition<>(
                    "GuaranteedLevel1MovesCount",
                    "PokemonMovesets",
                    2,
                    new SimpleSettingRestriction<>("GuaranteedLevel1Moves", isTrue),
                    null,
                    2, 4
            ),
            new SimpleSettingDefinition<>(
                    "MovesetsReorderDamagingMoves",
                    "PokemonMovesets",
                    false,
                    randomPokemonMovesetsRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "MovesetsNoGameBreakingMoves",
                    "PokemonMovesets",
                    false,
                    randomPokemonMovesetsRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "MovesetsForceGoodDamagingMoves",
                    "PokemonMovesets",
                    false,
                    randomPokemonMovesetsRestriction,
                    null
            ),
            new NumericSettingDefinition<>(
                    "MovesetsForceGoodDamagingMovesPercentage",
                    "PokemonMovesets",
                    0,
                    new SimpleSettingRestriction<>("MovesetsForceGoodDamagingMoves", isTrue),
                    null,
                    0, 100
            ),
            new SimpleSettingDefinition<>(
                    "EvolutionMovesForAllPokemon",
                    "PokemonMovesets",
                    false,
                    randomPokemonMovesetsRestriction,
                    atLeastGeneration(7)
            )
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
                    null,
                    RomHandler::canGiveCustomMovesetsToBossTrainers
            ),
            new SimpleSettingDefinition<>(
                    "BetterMovesetsForImportantTrainers",
                    "TrainerPokemon",
                    false,
                    null,
                    RomHandler::canGiveCustomMovesetsToImportantTrainers
            ),
            new SimpleSettingDefinition<>(
                    "BetterMovesetsForRegularTrainers",
                    "TrainerPokemon",
                    false,
                    null,
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
                    new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.RANDOM_EVERY_LEVEL, false),
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

    public enum CatchRateMod {
        // replaces the numeric (but described with names) catch rates of earlier
        // Randomizer versions
        UNCHANGED, STANDARDIZED, BUFFED, SUPER, ULTRA, GUARANTEED
    }

    public static final List<SettingDefinition<?>> WILD_POKEMON = List.of(
            // TODO: all the wild mon options that have to do with randomizing wild mons

            new SimpleSettingDefinition<>(
                    "WildPokemonCatchRate",
                    "WildPokemon",
                    CatchRateMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "RandomizeWildPokemonHeldItems",
                    "WildPokemon",
                    false,
                    null,
                    notOfGeneration(1)
            ),
            new SimpleSettingDefinition<>(
                    "BanBadWildPokemonHeldItems",
                    "WildPokemon",
                    false,
                    new SimpleSettingRestriction<>("RandomizeWildPokemonHeldItems", isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "WildPokemonLevelModifier",
                    "WildPokemon",
                    false,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    "WildPokemonLevelModifierPercentage",
                    "WildPokemon",
                    100,
                    new SimpleSettingRestriction<>("WildPokemonLevelModifier", isTrue),
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
                    "RandomizeTMMoves",
                    "TMsAndHMs",
                    TMMovesMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TMsNoGameBreakingMoves",
                    "TMsAndHMs",
                    false,
                    new SimpleSettingRestriction<>("RandomizeTMMoves",
                            doesNotMatchEnumValue(TMMovesMod.UNCHANGED)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "KeepFieldMoveTMs",
                    "TMsAndHMs",
                    false,
                    new SimpleSettingRestriction<>("RandomizeTMMoves",
                            doesNotMatchEnumValue(TMMovesMod.UNCHANGED)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TMsForceGoodDamagingMoves",
                    "TMsAndHMs",
                    false,
                    new SimpleSettingRestriction<>("RandomizeTMMoves",
                            doesNotMatchEnumValue(TMMovesMod.UNCHANGED)),
                    null
            ),
            new NumericSettingDefinition<>(
                    "TMsForceGoodDamagingMovesPercentage",
                    "TMsAndHMs",
                    0,
                    new SimpleSettingRestriction<>("TMsForceGoodDamagingMoves", isTrue),
                    null,
                    0, 100
            ),

            new SimpleSettingDefinition<>(
                    "RandomizeTMHMCompatibility",
                    "TMsAndHMs",
                    TMsHMsCompatibilityMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TMLevelupMoveSanity",
                    "TMsAndHMs",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizePokemonMovesets", MovesetsMod.UNCHANGED, false),
                            new EnumMatchRestriction<>("RandomizeTMMoves", TMMovesMod.UNCHANGED, false),
                            new EnumMatchRestriction<>("RandomizeTMHMCompatibility", TMsHMsCompatibilityMod.COMPLETELY_RANDOM),
                            new EnumMatchRestriction<>("RandomizeTMHMCompatibility", TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TMHMCompatibilityFollowEvolutions",
                    "TMsAndHMs",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizeTMHMCompatibility", TMsHMsCompatibilityMod.COMPLETELY_RANDOM),
                            new EnumMatchRestriction<>("RandomizeTMHMCompatibility", TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE),
                            new SimpleSettingRestriction<>("TMLevelupMoveSanity", isTrue)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "FullHMCompatibility",
                    "TMsAndHMs",
                    false,
                    new EnumMatchRestriction<>("RandomizeTMHMCompatibility", TMsHMsCompatibilityMod.FULL, false),
                    null
            ),

            new SimpleSettingDefinition<>(
                    "RandomizeMoveTutorMoves",
                    "MoveTutors",
                    MoveTutorMovesMod.UNCHANGED,
                    null,
                    RomHandler::hasMoveTutors
            ),
            new SimpleSettingDefinition<>(
                    "MoveTutorsNoGameBreakingMoves",
                    "MoveTutors",
                    false,
                    new SimpleSettingRestriction<>("RandomizeMoveTutorMoves",
                            doesNotMatchEnumValue(MoveTutorMovesMod.UNCHANGED)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "KeepFieldMoveTutors",
                    "MoveTutors",
                    false,
                    new SimpleSettingRestriction<>("RandomizeMoveTutorMoves",
                            doesNotMatchEnumValue(MoveTutorMovesMod.UNCHANGED)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "MoveTutorsForceGoodDamagingMoves",
                    "MoveTutors",
                    false,
                    new SimpleSettingRestriction<>("RandomizeMoveTutorMoves",
                            doesNotMatchEnumValue(MoveTutorMovesMod.UNCHANGED)),
                    null
            ),
            new NumericSettingDefinition<>(
                    "MoveTutorsForceGoodDamagingMovesPercentage",
                    "MoveTutors",
                    0,
                    new SimpleSettingRestriction<>("MoveTutorsForceGoodDamagingMoves", isTrue),
                    null,
                    0, 100
            ),

            new SimpleSettingDefinition<>(
                    "RandomizeMoveTutorCompatibility",
                    "MoveTutors",
                    MoveTutorsCompatibilityMod.UNCHANGED,
                    null,
                    RomHandler::hasMoveTutors
            ),
            new SimpleSettingDefinition<>(
                    "MoveTutorLevelupMoveSanity",
                    "MoveTutors",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizePokemonMovesets", MovesetsMod.UNCHANGED, false),
                            new EnumMatchRestriction<>("RandomizeMoveTutors", MoveTutorMovesMod.UNCHANGED, false),
                            new EnumMatchRestriction<>("RandomizeMoveTutorCompatibility", MoveTutorsCompatibilityMod.COMPLETELY_RANDOM),
                            new EnumMatchRestriction<>("RandomizeMoveTutorCompatibility", MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "MoveTutorCompatibilityFollowEvolutions",
                    "MoveTutors",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizeMoveTutorCompatibility", MoveTutorsCompatibilityMod.COMPLETELY_RANDOM),
                            new EnumMatchRestriction<>("RandomizeMoveTutorCompatibility", MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE),
                            new SimpleSettingRestriction<>("MoveTutorLevelupMoveSanity", isTrue)),
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
                    "RandomizeFieldItems",
                    "FieldItems",
                    FieldItemsMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanBanFieldItems",
                    "FieldItems",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizeFieldItems", FieldItemsMod.RANDOM),
                            new EnumMatchRestriction<>("RandomizeFieldItems", FieldItemsMod.RANDOM_EVEN)),
                    null
            ),

            new SimpleSettingDefinition<>(
                    "RandomizeSpecialShopItems",
                    "ShopItems",
                    ShopItemsMod.UNCHANGED,
                    null,
                    RomHandler::hasShopSupport
            ),
            new SimpleSettingDefinition<>(
                    "BanBadShopItems",
                    "ShopItems",
                    false,
                    new EnumMatchRestriction<>("RandomizeSpecialShopItems", ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanRegularShopItems",
                    "ShopItems",
                    false,
                    new EnumMatchRestriction<>("RandomizeSpecialShopItems", ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanOverpoweredShopItems",
                    "ShopItems",
                    false,
                    new EnumMatchRestriction<>("RandomizeSpecialShopItems", ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "GuaranteeEvolutionItemsInShops",
                    "ShopItems",
                    false,
                    new EnumMatchRestriction<>("RandomizeSpecialShopItems", ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "GuaranteeXItemsInShops",
                    "ShopItems",
                    false,
                    new EnumMatchRestriction<>("RandomizeSpecialShopItems", ShopItemsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BalanceShopItemPrices",
                    "ShopItems",
                    false,
                    null,
                    RomHandler::hasShopSupport
            ),
            new SimpleSettingDefinition<>(
                    "AddCheapRareCandiesToShops",
                    "ShopItems",
                    false,
                    null,
                    RomHandler::canChangeShopSizes
            ),

            new SimpleSettingDefinition<>(
                    "RandomizePickupItems",
                    "PickupItems",
                    PickupItemsMod.UNCHANGED,
                    null,
                    rh -> rh.abilitiesPerSpecies() > 0
            ),
            new SimpleSettingDefinition<>(
                    "BanBadPickupItems",
                    "PickupItems",
                    false,
                    new EnumMatchRestriction<>("RandomizePickupItems", PickupItemsMod.RANDOM),
                    null
            )
    );

    //endregion

    //region type effectiveness

    public enum TypeEffectivenessMod {
        UNCHANGED, RANDOM, RANDOM_BALANCED, KEEP_IDENTITIES, INVERSE
    }

    public static final List<SettingDefinition<?>> TYPES = List.of(
            new SimpleSettingDefinition<>(
                    "RandomizeTypeEffectiveness",
                    "TypeEffectiveness",
                    TypeEffectivenessMod.UNCHANGED,
                    null,
                    RomHandler::hasTypeEffectivenessSupport
            ),
            new SimpleSettingDefinition<>(
                    "InverseTypesRandomImmunities",
                    "TypeEffectiveness",
                    false,
                    new EnumMatchRestriction<>("RandomizeTypeEffectiveness", TypeEffectivenessMod.INVERSE),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "UpdateTypeEffectiveness",
                    "TypeEffectiveness",
                    false,
                    null,
                    rh -> rh.hasTypeEffectivenessSupport()
                            && rh.generationOfPokemon() < TypeEffectivenessUpdater.UPDATE_TO_GEN
                    )
    );

    //endregion

    //region graphics

    public enum PokemonPalettesMod {
        UNCHANGED, RANDOM
    }

    public static final List<SettingDefinition<?>> GRAPHICS = List.of(
            new SimpleSettingDefinition<>(
                    "RandomizePokemonPalettes",
                    "PokemonPalettes",
                    PokemonPalettesMod.UNCHANGED,
                    null,
                    RomHandler::hasPokemonPaletteSupport
            ),
            new SimpleSettingDefinition<>(
                    "PokemonPalettesFollowTypes",
                    "PokemonPalettes",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonPalettes", PokemonPalettesMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "PokemonPalettesFollowEvolutions",
                    "PokemonPalettes",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonPalettes", PokemonPalettesMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "PokemonPalettesShinyFromNormal",
                    "PokemonPalettes",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonPalettes", PokemonPalettesMod.RANDOM),
                    notOfGeneration(1)
            )

            // TODO: what to do with CPGs? Should they be included here?
    );

    //endregion graphics

    //region misc tweaks

    public static final List<SettingDefinition<?>> MISC_TWEAKS = List.of(
            // TODO
    );

    //endregion

    //TODO: make sure all enum declarations have been moved to this file.

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
