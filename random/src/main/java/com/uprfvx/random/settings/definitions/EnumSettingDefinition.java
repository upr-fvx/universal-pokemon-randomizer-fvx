package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.Map;
import java.util.function.Predicate;

/**
 * A SettingDefinition for enums that supports restrictions for individual enum states.
 * If no such definitions are needed, use SimpleSettingDefinition instead.
 * Assumes that all enum states (except null) are valid at some point.
 */
public class EnumSettingDefinition<T extends Enum<T>> extends SettingDefinition<T> {

    Map<T, SettingRestriction> restrictions;
    Map<T, Predicate<RomHandler>> support;

    /**
     * Creates an EnumSettingDefinition, which allows restricting certain enum values based on other settings or
     * the game (RomHandler).
     * If no such restrictions are needed, use SimpleSettingDefinition instead.
     * Assumes that all values of the enum are valid at some time.
     * @param name The setting's name. Must be unique among all settings.
     *             Should contain no spaces and be relatively human-readable.
     * @param category The setting's category.
     * @param defaultValue The setting's default value.
     * @param prerequisite A SettingRestriction that must return true for this setting to be enabled, or null.
     * @param supported A Predicate<RomHandler> that must return true for this setting to be supported, or null.
     * @param restrictedStates A Map of enum values to SettingRestrictions that must return true for the value in
     *                         question to be enabled, or null if there are no such restrictions.
     * @param supportedStates A Map of enum values to Predicate<RomHandler>s that must return true for the value in
     *                         question to be supported, or null if there are no such restrictions.
     */
    public EnumSettingDefinition(String name, String category, T defaultValue, SettingRestriction prerequisite,
                                 Predicate<RomHandler> supported, Map<T, SettingRestriction> restrictedStates,
                                 Map<T, Predicate<RomHandler>> supportedStates) {
        super(name, category, defaultValue, prerequisite, supported,
                restrictedStates != null ? restrictedStates.values() : null,
                supportedStates != null);
        this.restrictions = restrictedStates;
        this.support = supportedStates;
    }

    @Override
    public boolean isValueValid(T value) {
        return value != null && value.getClass() == type;
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
