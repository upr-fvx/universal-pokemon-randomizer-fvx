package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.function.Predicate;

public class SettingUtils {

    public static final Predicate<Boolean> isTrue = s -> s;

    public static Predicate<RomHandler> notOfGeneration(int... generations) {
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

    public static Predicate<RomHandler> atLeastGeneration(int generation) {
        return rom -> {
            int gameGen = rom.generationOfPokemon();
            return gameGen >= generation;
        };
    }

    public static Predicate<RomHandler> atMostGeneration(int generation) {
        return rom -> {
            int gameGen = rom.generationOfPokemon();
            return gameGen <= generation;
        };
    }

}
