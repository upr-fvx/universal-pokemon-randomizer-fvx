package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Settings {
    public static final List<SettingDefinition<?>> ALL_SETTINGS;

    private static final Predicate<Boolean> isTrue = s -> s;

    private static Predicate<RomHandler> notOfGeneration(int... generations) {
        return rom -> {
            int gameGen = rom.generationOfPokemon();
            for(int gen : generations) {
                if(gen == gameGen) {
                    return false;
                }
            }
            return true;
        };
    }

    public static final List<SettingDefinition<?>> TOP_LEVEL = Arrays.asList(
            new SettingDefinition<>("LimitPokemon", "GeneralOptions",
                    false, null, notOfGeneration(1)), //TODO: might be able to eliminate this setting
            new SettingDefinition<>("PokemonRestrictions", "GeneralOptions",
                    false, new SimpleSettingRestriction<>("LimitPokemon", isTrue), null),
            new SettingDefinition<>("RaceMode", "GeneralOptions",
                    false, null, null),
            new SettingDefinition<>("BanIrregularAltFormes", "GeneralOptions",
                    false, null, null)
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
        List<SettingDefinition<?>> all = new ArrayList<>(TOP_LEVEL);
        all.addAll(POKEMON_TRAITS);
        all.addAll(MOVES_AND_MOVESETS);
        all.addAll(FOE_POKEMON);
        ALL_SETTINGS = Collections.unmodifiableList(all);
    }








}
