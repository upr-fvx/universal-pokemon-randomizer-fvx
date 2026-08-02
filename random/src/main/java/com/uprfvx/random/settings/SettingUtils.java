package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandler;
import miscutils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class SettingUtils {

    //region setting value predicates
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

    public static <T extends Enum<T>> Predicate<T> matchesEnumValue(T value)
    {
        return e -> e == value;
    }

    public static <T extends Enum<T>> Predicate<T> doesNotMatchEnumValue(T value)
    {
        return e -> e != value;
    }

    //endregion

    //region romhandler predicates
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

    /**
     * Creates a set of minimums such that each generation given supports only generations higher than itself.
     * @param generations Each generation that needs such a limit.
     * @return A set of minimums as described.
     */
    public static List<Pair<Integer, Predicate<RomHandler>>> higherGenerationsThan(int... generations) {
        List<Pair<Integer, Predicate<RomHandler>>> supportMinimums = new ArrayList<>();
        for(int generation : generations){
            supportMinimums.add(new Pair<>(generation + 1, atLeastGeneration(generation)));
        }
        return Collections.unmodifiableList(supportMinimums);
    }

    //endregion

}
