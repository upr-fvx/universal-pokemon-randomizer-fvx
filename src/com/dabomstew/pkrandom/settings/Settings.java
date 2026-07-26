package com.dabomstew.pkrandom.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.dabomstew.pkrandom.settings.SettingUtils.*;

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

    static {
        List<SettingDefinition<?>> all = new ArrayList<>(GENERAL_OPTIONS);
        all.addAll(POKEMON_TRAITS);
        all.addAll(MOVES_AND_MOVESETS);
        all.addAll(FOE_POKEMON);
        ALL_SETTINGS = Collections.unmodifiableList(all);
    }







}
