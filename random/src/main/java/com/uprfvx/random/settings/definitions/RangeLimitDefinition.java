package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Definition for one of a pair of Settings that is the limit of a range.
 * The minimum and maximum extents of the range must match exactly (including supported minimums/maximums)
 * between the two settings, or odd behaviors could occur.
 * @param <N>
 */
public class RangeLimitDefinition<N extends Number & Comparable<N>> extends NumericSettingDefinition<N> {

    public static abstract class Builder<B extends Builder<B, N>, N extends Number & Comparable<N>>
            extends SettingDefinition.Builder<B, N> {

        protected final N minimum;
        protected final N maximum;
        protected final Settings.Name other;
        protected final boolean isLowerLimit;
        protected Function<RomHandler, N> supportedMinimums;
        protected Function<RomHandler, N> supportedMaximums;
        protected final RelativeValueRestriction<N> restriction;

        public Builder(Settings.Name name, Settings.Name other, boolean isLowerLimit, Settings.Category category,
                       N defaultValue, N minimum, N maximum) {
            super(name, category, defaultValue);
            this.minimum = minimum;
            this.maximum = maximum;
            this.other = other;
            this.isLowerLimit = isLowerLimit;
            this.restriction = new RelativeValueRestriction<>(name, other, isLowerLimit);
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
        public RangeLimitDefinition<N> build() {
            return new RangeLimitDefinition<>(
                    name, category, defaultValue,
                    prerequisite, supported,
                    variableDefaultValue,
                    minimum, maximum,
                    other, isLowerLimit,
                    supportedMinimums, supportedMaximums,
                    restriction
            );
        }
    }

    /**
     * A Builder for the lower limit of a range.
     * Automatically assigns default value equal to the range's minimum (including supported minimums).
     */
    public static class LowerLimitBuilder<B extends LowerLimitBuilder<B, N>, N extends Number & Comparable<N>>
            extends Builder<B, N> {

        public LowerLimitBuilder(Settings.Name name, Settings.Category category, Settings.Name upperLimit,
                                 N minimum, N maximum) {
            super(name, upperLimit, true, category, minimum, minimum, maximum);
        }

        @Override
        public B supportedMinimums(Function<RomHandler, N> supportedMinimums) {
            variableDefaultValue = supportedMinimums;
            return super.supportedMinimums(supportedMinimums);
        }
    }

    /**
     * A Builder for the upper limit of a range.
     * Automatically assigns default value equal to the range's maximum (including supported maximums).
     */
    public static class UpperLimitBuilder<B extends UpperLimitBuilder<B, N>, N extends Number & Comparable<N>>
            extends Builder<B, N> {

        public UpperLimitBuilder(Settings.Name name, Settings.Category category, Settings.Name lowerLimit,
                                 N minimum, N maximum) {
            super(name, lowerLimit, false, category, maximum, minimum, maximum);
        }

        @Override
        public B supportedMaximums(Function<RomHandler, N> supportedMaximums) {
            variableDefaultValue = supportedMaximums;
            return super.supportedMaximums(supportedMaximums);
        }
    }

    //This one could be dangerous if it's not used carefully, hence it being private.
    //...Oddly, it will never actually get tested.
    protected static class RelativeValueRestriction<N extends Number & Comparable<N>> implements SettingRestriction {

        private final Settings.Name primary;
        private final boolean primaryLower;
        private final Settings.Name other;

        /**
         *
         * @param primary The primary setting for this restriction. MUST be the setting on which the restriction
         *                is placed.
         * @param other
         * @param primaryLower
         */
        public RelativeValueRestriction(Settings.Name primary, Settings.Name other, boolean primaryLower) {
            this.primary = primary;
            this.other = other;
            this.primaryLower = primaryLower;
        }

        @Override
        public List<Settings.Name> getRelevantSettingNames() {
            return List.of(other);
            //Although it technically uses primary, it's not included so that the setting will not have a dependency
            //on itself.
        }

        @Override
        public boolean test(SettingsManager manager) {
            N primaryValue = manager.getSetting(primary);
            N otherValue = manager.getSetting(primary);
            return primaryLower ? primaryValue.compareTo(otherValue) <= 0 : primaryValue.compareTo(otherValue) >= 0;
        }
    }

    private final Settings.Name other;
    private final boolean isLowerLimit;

    /**
     * Creates a new RangeLimitDefinition.
     *
     * @param name                 The setting's name. Should be a unique identifier.
     * @param category             The setting's category.
     * @param defaultValue         The default value for the setting. Should be a value that can always be selected.
     * @param prerequisite         The setting is only enabled if this restriction returns true.
     * @param supported            The setting is only supported if this predicate returns true.
     * @param variableDefaultValue
     * @param minimum              The minimum allowed value.
     * @param maximum              The maximum allowed value.
     * @param other
     * @param islowerLimit         Whether this is the lower end of the range. If false, it's the upper end.
     * @param supportedMinimums    A function that returns an additional minimum, depending on RomHandler.
     *                             This minimum must be >= the normal minimum.
     *                             If the function returns null, the normal minimum is used.
     * @param supportedMaximums    A function that returns an additional maximum, depending on RomHandler.
     *                             This maximum must be <= the normal maximum.
     *                             If the function returns null, the normal maximum is used.
     */
    protected RangeLimitDefinition(Settings.Name name, Settings.Category category, N defaultValue,
                                   SettingRestriction prerequisite, Predicate<RomHandler> supported,
                                   Function<RomHandler, N> variableDefaultValue, N minimum, N maximum,
                                   Settings.Name other, boolean islowerLimit,
                                   Function<RomHandler, N> supportedMinimums,
                                   Function<RomHandler, N> supportedMaximums,
                                   RelativeValueRestriction<N> restriction) {
        super(name, category, defaultValue, prerequisite, supported, variableDefaultValue, minimum, maximum,
                null, null, supportedMinimums, supportedMaximums,
                List.of(restriction));

        //uhh... wait, how do I pass down the dependency... I don't want to make a *fake* restriction...

        this.other = other;
        this.isLowerLimit = islowerLimit;

    }

    @Override
    public boolean isValueEnabled(N value, SettingsManager manager) {
        N otherValue = manager.getSetting(other);

        return isLowerLimit ? value.compareTo(otherValue) <= 0 : value.compareTo(otherValue) >= 0;
    }

    @Override
    public N minimumEnabled(SettingsManager manager) {
        return isLowerLimit ? minimum : manager.getSetting(other);
    }

    @Override
    public N maximumEnabled(SettingsManager manager) {
        return isLowerLimit ? manager.getSetting(other) : maximum;
    }
}
