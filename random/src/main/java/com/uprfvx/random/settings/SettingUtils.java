package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.function.Predicate;

public class SettingUtils {

    public static final Predicate<Boolean> isTrue = s -> s;
    public static final Predicate<Boolean> isFalse = s -> !s;

    public static <T extends Comparable<T>> Predicate<T> equalsValue(T value)
    {
        return c -> c.compareTo(value) == 0;
    }

    public static <T extends Comparable<T>> Predicate<T> lessThanValue(T value)
    {
        return c -> c.compareTo(value) < 0;
    }

    public static <T extends Comparable<T>> Predicate<T> greaterThanValue(T value)
    {
        return c -> c.compareTo(value) > 0;
    }

    public static <T extends Comparable<T>> Predicate<T> lessThanOrEqualsValue(T value)
    {
        return c -> c.compareTo(value) <= 0;
    }

    public static <T extends Comparable<T>> Predicate<T> greaterThanOrEqualsValue(T value)
    {
        return c -> c.compareTo(value) >= 0;
    }

    /**
     * Determines if the given RomHandler is a game that is any of the given generations.
     * @param generations The generations to select.
     * @return True if the game is any of the given generations, false otherwise.
     */
    public static Predicate<RomHandler> ofGeneration(int... generations) {
        return rom -> {
            int gameGen = rom.generationOfPokemon();
            for(int gen : generations) {
                if(gen == gameGen) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Determines if the given RomHandler is a game that is NOT any of the given generations.
     * @param generations The generations to avoid.
     * @return False if the game is any of the given generations, true otherwise.
     */
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
