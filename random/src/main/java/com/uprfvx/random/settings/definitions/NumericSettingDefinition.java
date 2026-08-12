package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.romio.romhandlers.RomHandler;
import miscutils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A definition for a numeric setting that is bounded to specific ranges.
 * To get the ranges, it is necessary to cast SettingDefinition to this type.
 * All minimums and maximums are INCLUSIVE--They allow values that are equal to themselves.
 * @param <N> The type of numeric value to use. Must implement both Number and Comparable<T>.
 */
public class NumericSettingDefinition<N extends Number & Comparable<N>> extends SettingDefinition<N> {
    //Actually, not sure that it needs to extend Number?

    public static class Builder<B extends Builder<B, N>, N extends Number & Comparable<N>>
            extends SettingDefinition.Builder<B, N> {

        protected final N minimum;
        protected final N maximum;
        protected List<Pair<N, SettingRestriction>> restrictedMinimums;
        protected List<Pair<N, SettingRestriction>> restrictedMaximums;
        protected Function<RomHandler, N> supportedMinimums;
        protected Function<RomHandler, N> supportedMaximums;

        public Builder(Settings.Name name, Settings.Category category, N defaultValue, N minimum, N maximum) {
            super(name, category, defaultValue);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public B restrictedMinimums(List<Pair<N, SettingRestriction>> restrictedMinimums) {
            this.restrictedMinimums = restrictedMinimums;
            return self();
        }

        public B restrictedMaximums(List<Pair<N, SettingRestriction>> restrictedMaximums) {
            this.restrictedMaximums = restrictedMaximums;
            return self();
        }

        public B supportedMinimums(Function<RomHandler, N> supportedMinimums) {
            this.supportedMinimums = supportedMinimums;
            return self();
        }

        public B supportedMaximums(Function<RomHandler, N> supportedMaximums) {
            this.supportedMaximums = supportedMaximums;
            return self();
        }

        @Override
        public NumericSettingDefinition<N> build() {
            return new NumericSettingDefinition<>(
                    name, category, defaultValue,
                    prerequisite, supported,
                    variableDefaultValue,
                    minimum, maximum,
                    restrictedMinimums, restrictedMaximums,
                    supportedMinimums, supportedMaximums
            );
        }
    }

    final N minimum;
    final N maximum;

    final List<Pair<N, SettingRestriction>> restrictedMinimums;
    final List<Pair<N, SettingRestriction>> restrictedMaximums;
    final Function<RomHandler, N> supportedMinimums;
    final Function<RomHandler, N> supportedMaximums;

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
     * @param supportedMinimums A function that returns an additional minimum, depending on RomHandler.
     *                          This minimum must be >= the normal minimum.
     *                          If the function returns null, the normal minimum is used.
     * @param supportedMaximums A function that returns an additional maximum, depending on RomHandler.
     *                          This maximum must be <= the normal maximum.
     *                          If the function returns null, the normal maximum is used.
     */
    public NumericSettingDefinition(Settings.Name name, Settings.Category category, N defaultValue,
                                    SettingRestriction prerequisite, Predicate<RomHandler> supported,
                                    Function<RomHandler, N> variableDefaultValue,
                                    N minimum, N maximum,
                                    List<Pair<N, SettingRestriction>> restrictedMinimums,
                                    List<Pair<N, SettingRestriction>> restrictedMaximums,
                                    Function<RomHandler, N> supportedMinimums,
                                    Function<RomHandler, N> supportedMaximums) {
        super(name, category, defaultValue, prerequisite, supported, variableDefaultValue,
                composeSeconds(restrictedMinimums, restrictedMaximums),
                supportedMinimums != null || supportedMaximums != null
        );

        if (defaultValue.compareTo(minimum) < 0 || defaultValue.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("Default value for " + name + " is not within the valid range!");
        }
        this.minimum = minimum;
        this.maximum = maximum;


        checkIntegrity(restrictedMinimums, true);
        checkIntegrity(restrictedMaximums, false);

        this.restrictedMinimums = restrictedMinimums;
        this.restrictedMaximums = restrictedMaximums;
        this.supportedMinimums = supportedMinimums;
        this.supportedMaximums = supportedMaximums;
    }

    private <U> void checkIntegrity(List<Pair<N, U>> list, boolean isMinimums)
    {
        if (list != null)
        {
            for(Pair<N, U> pair : list) {
                if (isMinimums && pair.getKey().compareTo(defaultValue) > 0) {
                    throw new IllegalArgumentException("Default value for " + name +
                            " is lower than a restricted minimum!");
                } else if (!isMinimums && pair.getKey().compareTo(defaultValue) < 0) {
                    throw new IllegalArgumentException("Default value for " + name +
                            " is higher than a restricted maximum!");
                }

                if (isMinimums && pair.getKey().compareTo(minimum) <= 0) {
                    throw new IllegalArgumentException("Restricted minimum for " + name +
                            " is not higher than the absolute minimum!");
                } else if (!isMinimums && pair.getKey().compareTo(maximum) >= 0) {
                    throw new IllegalArgumentException("Restricted maximum for " + name +
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
    public boolean isValueValid(N value) {
        if (value == null)
            return false;
        if(value.getClass() != type)
            return false;
        return value.compareTo(minimum) >= 0 && value.compareTo(maximum) <= 0;
    }

    @Override
    public boolean isValueEnabled(N value, SettingsManager manager) {
        return value.compareTo(minimumEnabled(manager)) >= 0 && value.compareTo(maximumEnabled(manager)) <= 0;
    }

    @Override
    public boolean isValueSupported(N value, RomHandler game) {
        return value.compareTo(minimumSupported(game)) >= 0 && value.compareTo(maximumSupported(game)) <= 0;
    }

    /**
     * The lowest value that can ever be applied to this setting.
     */
    public N getMinimum() {
        return minimum;
    }

    /**
     * The highest value that can ever be applied to this setting.
     */
    public N getMaximum() {
        return maximum;
    }

    /**
     * The lowest value that is currently enabled.
     * Note that this may be lower or higher than the lowest value supported,
     * so both should always be checked.
     * @param manager The SettingsManager to test against.
     * @return The lowest enabled value.
     */
    public N minimumEnabled(SettingsManager manager)
    {
        if(restrictedMinimums == null)
            return minimum;

        N rollingMinimum = minimum;

        for (Pair<N, SettingRestriction> pair : restrictedMinimums) {
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
    public N maximumEnabled(SettingsManager manager)
    {
        if(restrictedMaximums == null)
            return maximum;

        N rollingMaximum = maximum;

        for (Pair<N, SettingRestriction> pair : restrictedMaximums) {
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
    public N minimumSupported(RomHandler game) {
        if (supportedMinimums == null) {
            return minimum;
        }
        N supportedMinimum = supportedMinimums.apply(game);
        if (supportedMinimum == null) {
            return minimum;
        }
        if (supportedMinimum.compareTo(minimum) < 0) {
            throw new IllegalStateException("supportedMinimum is less than the absolute minimum");
        }
        return supportedMinimum;
    }

    /**
     * The highest value that is supported by the current game.
     * @param game The RomHandler to check for support.
     * @return The highest supported value.
     */
    public N maximumSupported(RomHandler game) {
        if (supportedMaximums == null) {
            return maximum;
        }
        N supportedMaximum = supportedMaximums.apply(game);
        if (supportedMaximum == null) {
            return maximum;
        }
        if (supportedMaximum.compareTo(maximum) < 0) {
            throw new IllegalStateException("supportedMaximum is less than the absolute maximum");
        }
        return supportedMaximum;
    }
}
