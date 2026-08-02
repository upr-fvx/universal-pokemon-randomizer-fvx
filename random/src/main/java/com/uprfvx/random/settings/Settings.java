package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.*;
import com.uprfvx.random.settings.restrictions.*;
import com.uprfvx.random.updaters.TypeEffectivenessUpdater;
import com.uprfvx.romio.gamedata.BattleStyle;
import com.uprfvx.romio.gamedata.ExpCurve;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;
import miscutils.Pair;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
public class Settings {
    public static final List<SettingDefinition<? extends Serializable>> ALL_SETTINGS;
    public static final List<SettingDefinition<? extends Serializable>> REMOVED_SETTINGS;
    //When splitting a setting into multiple or changing its type, add the old version to the list of removed settings
    //so that we can load it in and convert it to the new setting.

    public static final List<SettingDefinition<?>> GENERAL_OPTIONS = List.of(
            new SimpleSettingDefinition<>(
                    "LimitPokemon",
                    "GeneralOptions",
                    false,
                    null,
                    notOfGeneration(1)
            ), //TODO: might be able to eliminate this setting
            new SimpleSettingDefinition<>(
                    "NoRandomIntroMon",
                    "GeneralOptions",
                    false,
                    null,
                    null
            ),
                            //TODO: make this setting actually work?
                            // "this setting" is race mode?
                            // I believe I was referring to "NoRandomIntroMon" but I cannot recall for sure anymore.
                            //TODO investigate this todo i guess
            new SimpleSettingDefinition<>(
                    "RaceMode",
                    "GeneralOptions",
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanIrregularAltFormes",
                    "GeneralOptions",
                    false,
                    null,
                    null
            ),

            new SimpleSettingDefinition<>(
                    "AllowGeneration1",
                    "LimitPokemon",
                    false,
                    new SimpleSettingRestriction<>("LimitPokemon", isTrue),
                    atLeastGeneration(2)
            ),
            new SimpleSettingDefinition<>(
                    "AllowGeneration2",
                    "LimitPokemon",
                    false,
                    new SimpleSettingRestriction<>("LimitPokemon", isTrue),
                    atLeastGeneration(2)
            ),
            new SimpleSettingDefinition<>(
                    "AllowGeneration3",
                    "LimitPokemon",
                    false,
                    new SimpleSettingRestriction<>("LimitPokemon", isTrue),
                    atLeastGeneration(3)
            ),
            new SimpleSettingDefinition<>(
                    "AllowGeneration4",
                    "LimitPokemon",
                    false,
                    new SimpleSettingRestriction<>("LimitPokemon", isTrue),
                    atLeastGeneration(4)
            ),
            new SimpleSettingDefinition<>(
                    "AllowGeneration5",
                    "LimitPokemon",
                    false,
                    new SimpleSettingRestriction<>("LimitPokemon", isTrue),
                    atLeastGeneration(5)
            ),
            new SimpleSettingDefinition<>(
                    "AllowGeneration6",
                    "LimitPokemon",
                    false,
                    new SimpleSettingRestriction<>("LimitPokemon", isTrue),
                    atLeastGeneration(6)
            ),
            new SimpleSettingDefinition<>(
                    "AllowGeneration7",
                    "LimitPokemon",
                    false,
                    new SimpleSettingRestriction<>("LimitPokemon", isTrue),
                    atLeastGeneration(7)
            )
    );

    public enum BSTMod {
        UNCHANGED, RANDOM_BUFF_NERF, SHUFFLE, RANDOM
    }

    public enum BaseStatisticsMod {
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

    public static final List<SettingDefinition<?>> POKEMON_TRAITS = List.of(
            new SimpleSettingDefinition<>(
                    "RandomizePokemonBaseStatTotals",
                    "PokemonBaseStatistics",
                    BSTMod.UNCHANGED,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    "BSTRandomBuffNerfPercentage",
                    "PokemonBaseStatistics",
                    0,
                    new EnumMatchRestriction<>("RandomizePokemonBaseStatTotals", BSTMod.RANDOM_BUFF_NERF),
                    null,
                    0, 50
            ),
            new SimpleSettingDefinition<>(
                    "BSTsFollowEvolutions",
                    "PokemonBaseStatistics",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizePokemonBaseStatTotals", BSTMod.RANDOM_BUFF_NERF),
                            new EnumMatchRestriction<>("RandomizePokemonBaseStatTotals", BSTMod.SHUFFLE)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BSTShuffleSeparateLegendaries",
                    "PokemonBaseStatistics",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonBaseStatTotals", BSTMod.SHUFFLE),
                    null
            ),

            new SimpleSettingDefinition<>(
                    "RandomizePokemonBaseStatDistributions",
                    "PokemonBaseStatistics",
                    BaseStatisticsMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "StatDistributionsFollowEvolutions",
                    "PokemonBaseStatistics",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonBaseStatDistributions", BaseStatisticsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "StatDistributionsFollowMegaEvolutions",
                    "PokemonBaseStatistics",
                    false,
                    new SimpleSettingRestriction<>("StatDistributionsFollowEvolutions", isTrue),
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    "StatDistributionsAssignEvoStatsRandomly",
                    "PokemonBaseStatistics",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonBaseStatDistributions", BaseStatisticsMod.RANDOM),
                    null
            ),

            new SimpleSettingDefinition<>(
                    "UpdateBaseStats",
                    "PokemonBaseStatistics",
                    false,
                    null,
                    notOfGeneration(1)),
            new NumericSettingDefinition<>(
                    "UpdateBaseStatsGeneration",
                    "PokemonBaseStatistics",
                    9,
                    new SimpleSettingRestriction<>("UpdateBaseStats", isTrue),
                    null,
                    6, 9,
                    null,
                    null,
                    List.of(new Pair<>(7, atLeastGeneration(6)), new Pair<>(8, atLeastGeneration(7))),
                    null
            ),

            new SimpleSettingDefinition<>(
                    "RandomizePokemonTypes",
                    "PokemonTypes",
                    SpeciesTypesMod.UNCHANGED,
                    null,
                    null
            ), // TODO: disable follow evos
            new SimpleSettingDefinition<>(
                    "PokemonTypesForceDualTypes",
                    "PokemonTypes",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonTypes", SpeciesTypesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TypesFollowMegaEvolutions",
                    "PokemonTypes",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonTypes", SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS),
                    RomHandler::hasMegaEvolutions
            ),

            new SimpleSettingDefinition<>(
                    "RandomizePokemonAbilities",
                    "PokemonAbilities",
                    AbilitiesMod.UNCHANGED,
                    null,
                    rh -> rh.abilitiesPerSpecies() != 0
            ),
            new SimpleSettingDefinition<>(
                    "AbilitiesFollowEvolutions",
                    "PokemonAbilities",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "AbilitiesFollowMegaEvolutions",
                    "PokemonAbilities",
                    false,
                    new SimpleSettingRestriction<>("AbilitiesFollowEvolutions", isTrue),
                    RomHandler::hasMegaEvolutions
            ),
            new SimpleSettingDefinition<>(
                    "CombineDuplicateAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EnsureTwoAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "AbilitiesBanWonderGuard",
                    "PokemonAbilities",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanTrappingAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanNegativeAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanBadAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED, false),
                    null
            ),

            // TODO: remember that evo randomization should affect a bunch of options

            new SimpleSettingDefinition<>(
                    "RandomizePokemonEvolutions",
                    "PokemonEvolutions",
                    EvolutionsMod.UNCHANGED,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EvolutionsSimilarStrength",
                    "PokemonEvolutions",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EvolutionsSameType",
                    "PokemonEvolutions",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "LimitEvolutionsToThreeStages",
                    "PokemonEvolutions",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EvolutionsNoConvergence",
                    "PokemonEvolutions",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EvolutionsForceChange",
                    "PokemonEvolutions",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED, false),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EvolutionsForceGrowth",
                    "PokemonEvolutions",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.RANDOM),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EvolutionsAllowAltFormes",
                    "PokemonEvolutions",
                    false,
                    new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED, false),
                    ofGeneration(7)
            ),
            new SimpleSettingDefinition<>(
                    "AdjustEvolutionLevels",
                    "PokemonEvolutions",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumMatchRestriction<>("RandomizePokemonBaseStatTotals", BSTMod.UNCHANGED, false),
                            new EnumMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED, false)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "ChangeImpossibleEvolutions",
                    "PokemonEvolutions",
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "MakeEvolutionsEasier",
                    "PokemonEvolutions",
                    false,
                    null,
                    null
            ),
            new NumericSettingDefinition<>(
                    "MakeEvolutionsEasierLevel",
                    "PokemonEvolutions",
                    40,
                    new SimpleSettingRestriction<>("MakeEvolutionsEasier", isTrue),
                    null,
                    30, 65
            ),
            new SimpleSettingDefinition<>(
                    "UseEstimatedEvolutionLevels",
                    "PokemonEvolutions",
                    false,
                    new MultiSettingRestriction(true, false,
                            new SimpleSettingRestriction<>("ChangeImpossibleEvolutions", isTrue),
                            new SimpleSettingRestriction<>("MakeEvolutionsEasier", isTrue)),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "RemoveTimeBasedEvolutions",
                    "PokemonEvolutions",
                    false,
                    null,
                    RomHandler::hasTimeBasedEvolutions
            ),

            new SimpleSettingDefinition<>(
                    "StandardizeExpCurve",
                    "PokemonExpCurves",
                    false,
                    null,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "StandardExpCurveSelection",
                    "PokemonExpCurves",
                    ExpCurve.MEDIUM_FAST,
                    new SimpleSettingRestriction<>("StandardizeExpCurve", isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "StandardizeExpCurvesExtent",
                    "PokemonExpCurves",
                    ExpCurveExtentMod.LEGENDARIES,
                    new SimpleSettingRestriction<>("StandardizeExpCurve", isTrue),
                    null
            )
    );

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
                                    equalsValue(SpeciesSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>("CustomStarter2",
                                    equalsValue(SpeciesSettingDefinition.RANDOM_SPECIES)),
                            new SimpleSettingRestriction<>("CustomStarter3",
                                    equalsValue(SpeciesSettingDefinition.RANDOM_SPECIES))
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
                            equalsValue(SpeciesSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>("CustomStarter2",
                            equalsValue(SpeciesSettingDefinition.RANDOM_SPECIES)),
                    new SimpleSettingRestriction<>("CustomStarter3",
                            equalsValue(SpeciesSettingDefinition.RANDOM_SPECIES))
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
            new SpeciesSettingDefinition(
                    "CustomStarter1",
                    "Starters",
                    new EnumMatchRestriction<>("RandomizeStarters", StartersMod.CUSTOM),
                    null
            ),
            new SpeciesSettingDefinition(
                    "CustomStarter2",
                    "Starters",
                    new EnumMatchRestriction<>("RandomizeStarters", StartersMod.CUSTOM),
                    null
            ),
            new SpeciesSettingDefinition(
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
            new EnumSettingDefinition<Type>( //Compiler is lying, this one needs to be explicit
                    "SingleStarterType",
                    "Starters",
                    null, // random
                    new EnumMatchRestriction<>("StartersTypeRestriction", StartersTypeMod.SINGLE_TYPE),
                    null,
                    null,
                    Arrays.stream(Type.values()).collect(Collectors.toMap(
                            // TODO: ensure null (random) is always possible
                            t -> t, t -> (rh -> rh.getTypeService().typeInGame(t))
                    ))
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
                    List.of(
                            // It does feel a bit silly to need to list each value like this
                            new Pair<>(3, atLeastGeneration(2)), new Pair<>(4, atLeastGeneration(3)),
                            new Pair<>(5, atLeastGeneration(4)), new Pair<>(6, atLeastGeneration(5)),
                            new Pair<>(7, atLeastGeneration(6)), new Pair<>(8, atLeastGeneration(7))
                    ),
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
                    null,
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
                    new SimpleSettingRestriction<>("RandomizeTMMoves", isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "KeepFieldMoveTMs",
                    "TMsAndHMs",
                    false,
                    new SimpleSettingRestriction<>("RandomizeTMMoves", isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "TMsForceGoodDamagingMoves",
                    "TMsAndHMs",
                    false,
                    new SimpleSettingRestriction<>("RandomizeTMMoves", isTrue),
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
                    new SimpleSettingRestriction<>("RandomizeMoveTutorMoves", isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "KeepFieldMoveTutors",
                    "MoveTutors",
                    false,
                    new SimpleSettingRestriction<>("RandomizeMoveTutorMoves", isTrue),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "MoveTutorsForceGoodDamagingMoves",
                    "MoveTutors",
                    false,
                    new SimpleSettingRestriction<>("RandomizeMoveTutorMoves", isTrue),
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

    public static final List<SettingDefinition<?>> MISC_TWEAKS = List.of(
            // TODO
    );

    //TODO: make sure all enum declarations have been moved to this file.

    static {
        List<SettingDefinition<?>> all = new ArrayList<>(GENERAL_OPTIONS);
        all.addAll(POKEMON_TRAITS);
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
