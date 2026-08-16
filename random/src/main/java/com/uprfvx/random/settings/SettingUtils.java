package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;
import com.uprfvx.random.settings.definitions.SimpleSettingDefinition;
import com.uprfvx.romio.MiscTweak;
import com.uprfvx.romio.romhandlers.RomHandler;
import miscutils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
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

    /**
     * Returns a {@link Predicate} that returns true if its argument equals <code>value</code>.
     */
    public static <E extends Enum<E>> Predicate<E> matchesEnum(E value)
    {
        return e -> e == value;
    }

    /**
     * Returns a {@link Predicate} that returns false if its argument equals <code>value</code>.
     */
    public static <E extends Enum<E>> Predicate<E> notMatchesEnum(E value)
    {
        return e -> e != value;
    }

    //endregion

    //region romhandler predicates
    /**
     * Determines if the given {@link RomHandler} is a game that is any of the given generations.<br>
     * This method should be used sparsely. If you want to know whether a game supports a feature,
     * consider adding a corresponding method to RomHandler instead.
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
     * Determines if the given {@link RomHandler} is a game that is NOT any of the given generations.<br>
     * This method should be used sparingly. If you want to know whether a game supports a feature,
     * consider adding a corresponding method to RomHandler instead.
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

    /**
     * Determines if the given {@link RomHandler} is a game of at least the given generation.<br>
     * This method should be used sparingly. If you want to know whether a game supports a feature,
     * consider adding a corresponding method to RomHandler instead.
     * @param generation The minimum generation to match.
     * @return False if the game is of a generation before the given one, true otherwise.
     */
    public static Predicate<RomHandler> atLeastGeneration(int generation) {
        return rom -> {
            int gameGen = rom.generationOfPokemon();
            return gameGen >= generation;
        };
    }

    /**
     * Determines if the given {@link RomHandler} is a game of at most the given generation.<br>
     * This method should be used sparingly. If you want to know whether a game supports a feature,
     * consider adding a corresponding method to RomHandler instead.
     * @param generation The maximum generation to match.
     * @return False if the game is of a generation after the given one, true otherwise.
     */
    public static Predicate<RomHandler> atMostGeneration(int generation) {
        return rom -> {
            int gameGen = rom.generationOfPokemon();
            return gameGen <= generation;
        };
    }

    //endregion

    //region romhandler functions

    public static <V> Function<RomHandler, V> overrideForGeneration(int generation, V override) {
        return rh -> rh.generationOfPokemon() == generation ? override : null;
    }

    //endregion

}
