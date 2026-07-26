package com.dabomstew.pkrandom.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.dabomstew.pkrandom.settings.SettingUtils.*;

//The list of EVERY setting supported by the randomizer.

//The types of setting definition are:
//SimpleSettingDefinition: Any setting that does not have restrictions on its values (that is to say, any value
// supported by the data type is applicable whenever the setting itself is supported and enabled.)
//EnumSettingDefinition: A setting which is an enum type, which can disable certain values based on other settings
// or RomHandler support. If you do not need to disable certain values, use a SimpleSettingDefinition.
//NumericSettingDefinition: A setting that is a numeric type, which is restricted to a certain range. The range can
// be restricted further based on other settings or RomHandler support.

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

    public static final List<SettingDefinition<?>> POKEMON_TRAITS = Arrays.asList(
            new SimpleSettingDefinition<>("RandomizePokemonBaseStatistics",
                    "PokemonBaseStatistics", SettingsManager.BaseStatisticsMod.UNCHANGED, null, null),
            new SimpleSettingDefinition<>("FollowEvolutions", "PokemonBaseStatistics",
                    false,
                    new SimpleSettingRestriction<SettingsManager.BaseStatisticsMod>("RandomizePokemonBaseStatistics",
                            pbs -> !(pbs == SettingsManager.BaseStatisticsMod.UNCHANGED)),
                    null),

            new SimpleSettingDefinition<>("ChangeImpossibleEvolutions", "PokemonEvolutions",
                    false, null, null),
            new SimpleSettingDefinition<>("MakeEvolutionsEasier", "PokemonEvolutions",
                    false, null, null),
            new SimpleSettingDefinition<>("RemoveTimeBasedEvolutions", "PokemonEvolutions",
                    false, null, null)
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
