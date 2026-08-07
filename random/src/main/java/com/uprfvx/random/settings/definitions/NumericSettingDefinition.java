package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;
import miscutils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A definition for a numeric setting that is bounded to specific ranges.
 * To get the ranges, it is necessary to cast SettingDefinition to this type.
 * All minimums and maximums are INCLUSIVE--They allow values that are equal to themselves.
 * @param <T> The type of numeric value to use. Must implement both Number and Comparable<T>.
 */
public class NumericSettingDefinition<T extends Number & Comparable<T>> extends SettingDefinition<T> {
    //Actually, not sure that it needs to extend Number?

    final T minimum;
    final T maximum;

    final List<Pair<T, SettingRestriction>> restrictedMinimums;
    final List<Pair<T, SettingRestriction>> restrictedMaximums;
    final List<Pair<T, Predicate<RomHandler>>> supportedMinimums;
    final List<Pair<T, Predicate<RomHandler>>> supportedMaximums;

    public NumericSettingDefinition(String name, String category, T defaultValue,
                                    SettingRestriction prerequisite, Predicate<RomHandler> supported,
                                    T minimum, T maximum) {
        super(name, category, defaultValue, prerequisite, supported, null, false);
        if (defaultValue.compareTo(minimum) < 0 || defaultValue.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("Default value for " + name + " is not within the valid range!");
        }

        this.minimum = minimum;
        this.maximum = maximum;

        restrictedMinimums = null;
        restrictedMaximums = null;
        supportedMinimums = null;
        supportedMaximums = null;
    }

    /**
     * Creates a new NumericSettingDefinition.
     * @param name The setting's name. Should be a unique identifier.
     * @param category The setting's category.
     * @param defaultValue The default value for the setting. Should be a value that can always be selected.
     * @param prerequisite The setting is only enabled if this restriction returns true.
     * @param supported The setting is only supported if this predicate returns true.
     * @param minimum The minimum allowed value.
     * @param maximum The maximum allowed value.
     * @param restrictedMinimums A set of additional minimums which apply when the associated restrictions return TRUE.
     * @param restrictedMaximums A set of additional maximums which apply when the associated restrictions return TRUE.
     * @param supportedMinimums A set of additional minimums which apply when the associated predicates return TRUE.
     * @param supportedMaximums A set of additional maximums which apply when the associated predicates return TRUE.
     */
    public NumericSettingDefinition(String name, String category, T defaultValue,
                                    SettingRestriction prerequisite, Predicate<RomHandler> supported,
                                    T minimum, T maximum,
                                    List<Pair<T, SettingRestriction>> restrictedMinimums,
                                    List<Pair<T, SettingRestriction>> restrictedMaximums,
                                    List<Pair<T, Predicate<RomHandler>>> supportedMinimums,
                                    List<Pair<T, Predicate<RomHandler>>> supportedMaximums) {
        super(name, category, defaultValue, prerequisite, supported,
                composeSeconds(restrictedMinimums, restrictedMaximums),
                !composeSeconds(supportedMinimums, supportedMaximums).isEmpty()
        );

        if (defaultValue.compareTo(minimum) < 0 || defaultValue.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("Default value for " + name + " is not within the valid range!");
        }
        this.minimum = minimum;
        this.maximum = maximum;


        checkIntegrity(restrictedMinimums, true);
        checkIntegrity(restrictedMaximums, false);
        checkIntegrity(supportedMinimums, true);
        checkIntegrity(supportedMaximums, false);

        this.restrictedMinimums = restrictedMinimums;
        this.restrictedMaximums = restrictedMaximums;
        this.supportedMinimums = supportedMinimums;
        this.supportedMaximums = supportedMaximums;
    }

    private <U> void checkIntegrity(List<Pair<T, U>> list, boolean isMinimums)
    {
        if (list != null)
        {
            for(Pair<T, U> pair : list) {
                if (isMinimums && pair.getKey().compareTo(defaultValue) > 0) {
                    throw new IllegalArgumentException("Default value for " + name +
                            " is lower than a restricted/supported minimum!");
                } else if (!isMinimums && pair.getKey().compareTo(defaultValue) < 0) {
                    throw new IllegalArgumentException("Default value for " + name +
                            " is higher than a restricted/supported maximum!");
                }

                if (isMinimums && pair.getKey().compareTo(minimum) <= 0) {
                    throw new IllegalArgumentException("Restricted/supported minimum for " + name +
                            " is not higher than the absolute minimum!");
                } else if (!isMinimums && pair.getKey().compareTo(maximum) >= 0) {
                    throw new IllegalArgumentException("Restricted/supported maximum for " + name +
                            " is not lower than the absolute maximum!");
                }
            }
        }
    }

    //this is janky. Internet advised we might actually want composition. That also feels janky here though...
    //(Specifically, "When you think you need to run code before super(),
    // that's a sign you maybe should use composition.")
    @SafeVarargs
    private static <T, U> List<U> composeSeconds(List<Pair<T, U>>... listsOfPairs) {
        List<U> fullList = new ArrayList<>();
        for(List<Pair<T, U>> list : listsOfPairs) {
            if (list != null) {
                for (Pair<T, U> pair : list) {
                    fullList.add(pair.getValue());
                }
            }
        }
        return fullList;
    }

    @Override
    public boolean isValueValid(T value) {
        if (value == null)
            return false;
        if(value.getClass() != type)
            return false;
        return value.compareTo(minimum) >= 0 && value.compareTo(maximum) <= 0;
    }

    @Override
    public boolean isValueEnabled(T value, SettingsManager manager) {
        return value.compareTo(minimumEnabled(manager)) >= 0 && value.compareTo(maximumEnabled(manager)) <= 0;
    }

    @Override
    public boolean isValueSupported(T value, RomHandler game) {
        return value.compareTo(minimumSupported(game)) >= 0 && value.compareTo(maximumSupported(game)) <= 0;
    }

    /**
     * The lowest value that can ever be applied to this setting.
     */
    public T getMinimum() {
        return minimum;
    }

    /**
     * The highest value that can ever be applied to this setting.
     */
    public T getMaximum() {
        return maximum;
    }

    /**
     * The lowest value that is currently enabled.
     * Note that this may be lower or higher than the lowest value supported,
     * so both should always be checked.
     * @param manager The SettingsManager to test against.
     * @return The lowest enabled value.
     */
    public T minimumEnabled(SettingsManager manager)
    {
        if(restrictedMinimums == null)
            return minimum;

        T rollingMinimum = minimum;

        for (Pair<T, SettingRestriction> pair : restrictedMinimums) {
            if (pair.getValue().test(manager) && pair.getKey().compareTo(rollingMinimum) > 0) {
                rollingMinimum = pair.getKey();
            }
        }

        return rollingMinimum;
    }

    /**
     * The highest value that is currently enabled.
     * Note that this may be lower or higher than the highest value supported,
     * so both should always be checked.
     * @param manager The SettingsManager to test against.
     * @return The highest enabled value.
     */
    public T maximumEnabled(SettingsManager manager)
    {
        if(restrictedMaximums == null)
            return maximum;

        T rollingMaximum = maximum;

        for (Pair<T, SettingRestriction> pair : restrictedMaximums) {
            if (pair.getValue().test(manager) && pair.getKey().compareTo(rollingMaximum) < 0) {
                rollingMaximum = pair.getKey();
            }
        }

        return rollingMaximum;
    }

    /**
     * The lowest value that is supported by the current game.
     * @param game The RomHandler to check for support.
     * @return The lowest supported value.
     */
    public T minimumSupported(RomHandler game)
    {
        if(supportedMinimums == null)
            return minimum;

        T rollingMinimum = minimum;

        for (Pair<T, Predicate<RomHandler>> pair : supportedMinimums) {
            if (pair.getValue().test(game) && pair.getKey().compareTo(rollingMinimum) > 0) {
                rollingMinimum = pair.getKey();
            }
        }

        return rollingMinimum;
    }

    /**
     * The highest value that is supported by the current game.
     * @param game The RomHandler to check for support.
     * @return The highest supported value.
     */
    public T maximumSupported(RomHandler game)
    {
        if(supportedMaximums == null)
            return maximum;

        T rollingMaximum = maximum;

        for (Pair<T, Predicate<RomHandler>> pair : supportedMaximums) {
            if (pair.getValue().test(game) && pair.getKey().compareTo(rollingMaximum) < 0) {
                rollingMaximum = pair.getKey();
            }
        }

        return rollingMaximum;
    }
}
