package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.dabomstew.pkrandom.settings.SettingUtils.*;

public class Settings {
    public static final List<SettingDefinition<?>> ALL_SETTINGS;

    public static final List<SettingDefinition<?>> GENERAL_OPTIONS = Arrays.asList(
            new SettingDefinition<>("LimitPokemon", "GeneralOptions",
                    false, null, notOfGeneration(1)), //TODO: might be able to eliminate this setting
            new SettingDefinition<>("NoRandomIntroMon", "GeneralOptions",
                    false, null, null),
                            //TODO: make this setting actually work?
            new SettingDefinition<>("RaceMode", "GeneralOptions",
                    false, null, null),
            new SettingDefinition<>("BanIrregularAltFormes", "GeneralOptions",
                    false, null, null),

            new SettingDefinition<>("AllowGeneration1", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(2)),
            new SettingDefinition<>("AllowGeneration2", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(2)),
            new SettingDefinition<>("AllowGeneration3", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(3)),
            new SettingDefinition<>("AllowGeneration4", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(4)),
            new SettingDefinition<>("AllowGeneration5", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(5)),
            new SettingDefinition<>("AllowGeneration6", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(6)),
            new SettingDefinition<>("AllowGeneration7", "LimitPokemon",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), atLeastGeneration(7))

    );

    public static final List<SettingDefinition<?>> POKEMON_TRAITS = Arrays.asList(
            new SettingDefinition<>("RandomizePokemonBaseStatistics",
                    "PokemonBaseStatistics", SettingsManager.BaseStatisticsMod.UNCHANGED, null, null),
            new SettingDefinition<>("FollowEvolutions", "PokemonBaseStatistics",
                    false,
                    new SimpleSettingRestriction<SettingsManager.BaseStatisticsMod>("RandomizePokemonBaseStatistics",
                            pbs -> !(pbs == SettingsManager.BaseStatisticsMod.UNCHANGED)),
                    null),

            new SettingDefinition<>("ChangeImpossibleEvolutions", "PokemonEvolutions",
                    false, null, null),
            new SettingDefinition<>("MakeEvolutionsEasier", "PokemonEvolutions",
                    false, null, null),
            new SettingDefinition<>("RemoveTimeBasedEvolutions", "PokemonEvolutions",
                    false, null, null)
    );

    public static final List<SettingDefinition<?>> MOVES_AND_MOVESETS = Arrays.asList(
            new SettingDefinition<>("UpdateMoves", "MoveData",
            false, null, null),
            new SettingDefinition<>("UpdateMovesToGeneration", "MoveData",
            0, new SimpleSettingRestriction<>("UpdateMoves", isTrue), null)
    );

    public static final List<SettingDefinition<?>> FOE_POKEMON = Arrays.asList(
            new SettingDefinition<>("RandomizeTrainerNames", "TrainerPokemon",
                    false, null, null),
            new SettingDefinition<>("RandomizeTrainerClassNames", "TrainerPokemon",
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
