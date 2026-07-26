package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A SettingDefinition for enums that supports restrictions for individual enum states.
 * @param <T>
 */
public class EnumSettingDefinition<T extends Enum<T>> extends SettingDefinition<T> {

    //TODO: There may need to be an equivalent to this class for numerics.
    // Possibly there's a way to combine them into a single more generic class?
    // Or an interface, at least.
    // If so, it would probably have to have some functions that don't always make sense
    // (Such as min and max value functions)

    Map<T, SettingRestriction> restrictions;
    Map<T, Predicate<RomHandler>> support;

    public EnumSettingDefinition(String name, String category, T defaultValue, SettingRestriction prerequisite,
                                 Predicate<RomHandler> supported, Map<T, SettingRestriction> restrictedStates,
                                 Map<T, Predicate<RomHandler>> supportedStates) {
        super(name, category, defaultValue, prerequisite, supported, restrictedStates.values(), supportedStates.values());
        this.restrictions = restrictedStates;
        support = supportedStates;
    }

    public EnumSettingDefinition(String name, String category, T defaultValue, SettingRestriction prerequisite,
                                 Predicate<RomHandler> supported) {
        this(name, category, defaultValue, prerequisite, supported, null, null);
    }

    /**
     * Tests to see if a particular value of this EnumSetting is enabled given the current state of the SettingsManager.
     * Ignores any restrictions that apply to the setting as a whole, so IsEnabled() should also be run.
     * @param value The particular enum value to check.
     * @param manager The SettingsManager to test against.
     * @return True if the value is enabled, false otherwise.
     */
    @Override
    public boolean isValueEnabled(T value, SettingsManager manager) {
        if(restrictions == null) {
            return true;
        }
        SettingRestriction restriction = restrictions.get(value);
        if(restriction == null) {
            return true;
        }
        return restriction.test(manager);
    }

    @Override
    public boolean isValueSupported(T value, RomHandler game) {
        if(support == null) {
            return true;
        }
        Predicate<RomHandler> supportTest = support.get(value);
        if(supportTest == null) {
            return true;
        }
        return supportTest.test(game);
    }
}
