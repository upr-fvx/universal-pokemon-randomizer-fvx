package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Settings {

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

    public static final List<SettingDefinition<?>> ALL_SETTINGS = Arrays.asList(
            //Top Level
            new SettingDefinition<Boolean>("LimitPokemon", "GeneralOptions",
                    false, notOfGeneration(1)), //might be able to eliminate this setting
            new SettingDefinition<Boolean>("PokemonRestrictions", "GeneralOptions",
                    false, null, new SettingState<Boolean>("LimitPokemon", isTrue)),
            new SettingDefinition<Boolean>("RaceMode", "GeneralOptions",
                    false, null),
            new SettingDefinition<Boolean>("BanIrregularAltFormes", "GeneralOptions",
                    false, null),

            //Pokemon Traits
            new SettingDefinition<SettingsManager.BaseStatisticsMod>("RandomizePokemonBaseStatistics",
                    "PokemonBaseStatistics", SettingsManager.BaseStatisticsMod.UNCHANGED, null),
            new SettingDefinition<Boolean>("FollowEvolutions", "PokemonBaseStatistics",
                    false, null,
                    new SettingState<SettingsManager.BaseStatisticsMod>("RandomizePokemonBaseStatistics",
                            pbs -> !(pbs == SettingsManager.BaseStatisticsMod.UNCHANGED))),

            new SettingDefinition<Boolean>("ChangeImpossibleEvolutions", "PokemonEvolutions",
                    false, null),
            new SettingDefinition<Boolean>("MakeEvolutionsEasier", "PokemonEvolutions",
                    false, null),
            new SettingDefinition<Boolean>("RemoveTimeBasedEvolutions", "PokemonEvolutions",
                    false, null),

            //Moves and Movesets
            new SettingDefinition<Boolean>("UpdateMoves", "MoveData",
                    false, null),
            new SettingDefinition<Integer>("UpdateMovesToGeneration", "MoveData",
                    0, null, new SettingState<Boolean>("UpdateMoves", isTrue)),

            //Foe Pokemon
            new SettingDefinition<Boolean>("RandomizeTrainerNames", "TrainerPokemon",
                    false, null),
            new SettingDefinition<Boolean>("RandomizeTrainerClassNames", "TrainerPokemon",
                    false, null)
    );
}
