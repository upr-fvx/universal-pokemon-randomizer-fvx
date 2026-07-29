package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.EnumSettingDefinition;
import com.uprfvx.random.settings.definitions.NumericSettingDefinition;
import com.uprfvx.random.settings.definitions.SettingDefinition;
import com.uprfvx.random.settings.definitions.SimpleSettingDefinition;
import com.uprfvx.random.settings.restrictions.*;
import com.uprfvx.romio.gamedata.ExpCurve;
import com.uprfvx.romio.gamedata.Type;
import com.uprfvx.romio.romhandlers.RomHandler;

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

//Support is determined by a simple Predicate(RomHandler).
//Enablement(? word?) is determined by SettingRestrictions, which contain two parts: The setting(s) which must be checked,
// and the function to check them against.
//Most cases can be handled by a SimpleSettingRestriction, which compares the value of a single setting against a predicate.
//If there are multiple relevant settings, there is also MultiSettingRestriction, which combines the results of two
// or more SettingRestrictions in an AND, OR, NAND, or NOR manner. (This can include other MultiSettingRestrictions.)
//For more complicated checks (such as comparing one setting's value to another's) you may need to write your own
// extension of SettingRestriction.

//SettingsUtils contains several helpful functions for convenience:
//isTrue and isFalse, which check the states of boolean settings.
//TODO: enum and numeric helper functions
//ofGeneration, notOfGeneration, atLeastGeneration, and atMostGeneration, which check the generation of a RomHandler.

//Setting names should be unique. They also will (eventually) be used as ini keys, so they should (a) be relatively
// human-readable, (b) contain no spaces nor the equals sign.
public class Settings {
    public static final List<SettingDefinition<?>> ALL_SETTINGS;

    public static final List<SettingDefinition<?>> GENERAL_OPTIONS = Arrays.asList(
            new SimpleSettingDefinition<>("LimitPokemon", "GeneralOptions",
                    false, null, notOfGeneration(1)), //TODO: might be able to eliminate this setting
            new SimpleSettingDefinition<>("NoRandomIntroMon", "GeneralOptions",
                    false, null, null),
                            //TODO: make this setting actually work?
            new SimpleSettingDefinition<>("RaceMode", "GeneralOptions",
                    false, null, null),
            new SimpleSettingDefinition<>("BanIrregularAltFormes", "GeneralOptions",
                    false, null, null),

            new SimpleSettingDefinition<>("AllowGeneration1", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(2)),
            new SimpleSettingDefinition<>("AllowGeneration2", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(2)),
            new SimpleSettingDefinition<>("AllowGeneration3", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(3)),
            new SimpleSettingDefinition<>("AllowGeneration4", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(4)),
            new SimpleSettingDefinition<>("AllowGeneration5", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(5)),
            new SimpleSettingDefinition<>("AllowGeneration6", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(6)),
            new SimpleSettingDefinition<>("AllowGeneration7", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(7))

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

    public static final List<SettingDefinition<?>> POKEMON_TRAITS = Arrays.asList(
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
                    new EnumNotMatchRestriction<>("RandomizePokemonBaseStatDistributions", BaseStatisticsMod.UNCHANGED),
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
                    6, 9
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
                    new EnumNotMatchRestriction<>("RandomizePokemonTypes", SpeciesTypesMod.UNCHANGED),
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
                    new EnumNotMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED),
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
                    new EnumNotMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EnsureTwoAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumNotMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "AbilitiesAllowWonderGuard",
                    "PokemonAbilities", // TODO: flip this one? To be "BanWonderGuard", defaulting to true?
                    false,
                    new EnumNotMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanTrappingAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumNotMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanNegativeAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumNotMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "BanBadAbilities",
                    "PokemonAbilities",
                    false,
                    new EnumNotMatchRestriction<>("RandomizePokemonAbilities", AbilitiesMod.UNCHANGED),
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
                    new EnumNotMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED),
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
                    new EnumNotMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED),
                    null
            ),
            new SimpleSettingDefinition<>(
                    "EvolutionsForceChange",
                    "PokemonEvolutions",
                    false,
                    new EnumNotMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED),
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
                    new EnumNotMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED),
                    ofGeneration(7)
            ),
            new SimpleSettingDefinition<>(
                    "AdjustEvolutionLevels",
                    "PokemonEvolutions",
                    false,
                    new MultiSettingRestriction(true, false,
                            new EnumNotMatchRestriction<>("RandomizePokemonBaseStatTotals", BSTMod.UNCHANGED),
                            new EnumNotMatchRestriction<>("RandomizePokemonEvolutions", EvolutionsMod.UNCHANGED)),
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

    // TODO: starter restrictions need to be way more complicated,
    //  since a lot of options should turn on when 1+ custom starter is "random"
    private final static SettingRestriction notUnchangedOrCustomStarterRestriction = new MultiSettingRestriction(
            false, false,
            new EnumNotMatchRestriction<>("RandomizeStarters", StartersMod.UNCHANGED),
            new EnumNotMatchRestriction<>("RandomizeStarters", StartersMod.CUSTOM)
    );

    public static final List<SettingDefinition<?>> STARTERS_STATICS_AND_TRADES = Arrays.asList(
            new SimpleSettingDefinition<>(
                    "RandomizeStarters",
                    "Starters",
                    StartersMod.UNCHANGED,
                    null,
                    null
            ),
            // TODO: custom starter selection


            new EnumSettingDefinition<>(
                    "StartersTypeRestriction",
                    "Starters",
                    StartersTypeMod.NONE,
                    null,
                    null,
                    Map.of(), // restricted states
                    Map.of( // supported states
                            StartersTypeMod.FIRE_WATER_GRASS, RomHandler::hasStarterTypeTriangleSupport,
                            StartersTypeMod.TRIANGLE, RomHandler::hasStarterTypeTriangleSupport
                    )
            ),
            new SimpleSettingDefinition<>(
                    "NoDualTypeStarters",
                    "Starters",
                    false,
                    notUnchangedOrCustomStarterRestriction,
                    null
            ),
            new EnumSettingDefinition<>(
                    "SingleStarterType",
                    "Starters",
                    null, // random
                    new EnumMatchRestriction<>("StartersTypeRestriction", StartersTypeMod.SINGLE_TYPE),
                    null,
                    Map.of(),
                    Arrays.stream(Type.values()).collect(Collectors.toMap(
                            // TODO: ensure null (random) is always possible
                            t -> t, t -> (rh -> rh.getTypeService().typeInGame(t))
                    ))
            ),

            new SimpleSettingDefinition<>(
                    "StartersNoLegendaries",
                    "Starters",
                    false,
                    notUnchangedOrCustomStarterRestriction,
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
                    notUnchangedOrCustomStarterRestriction,
                    null
            ),
            new SimpleSettingDefinition<>(
                    "LimitStartersMaximumBST",
                    "Starters",
                    false,
                    notUnchangedOrCustomStarterRestriction,
                    null
            )
            // TODO: LimitStartersMinimumBSTValue, LimitStartersMaximumBSTValue;
            //  these need a variable default value depending on RomHandler
    );

    public static final List<SettingDefinition<?>> MOVES_AND_MOVESETS = Arrays.asList(
            new SimpleSettingDefinition<>("UpdateMoves", "MoveData",
            false, null, null),
            new SimpleSettingDefinition<>("UpdateMovesToGeneration", "MoveData",
            0, new SimpleSettingRestriction<>("UpdateMoves", isTrue), null)
    );

    public static final List<SettingDefinition<?>> FOE_POKEMON = Arrays.asList(
            new SimpleSettingDefinition<>("RandomizeTrainerNames", "TrainerPokemon",
                    false, null, null),
            new SimpleSettingDefinition<>("RandomizeTrainerClassNames", "TrainerPokemon",
                    false, null, null)
    );

    //TODO: complete list, move enum declarations to this file.

    static {
        List<SettingDefinition<?>> all = new ArrayList<>(GENERAL_OPTIONS);
        all.addAll(POKEMON_TRAITS);
        all.addAll(MOVES_AND_MOVESETS);
        all.addAll(FOE_POKEMON);
        ALL_SETTINGS = Collections.unmodifiableList(all);
    }

}
