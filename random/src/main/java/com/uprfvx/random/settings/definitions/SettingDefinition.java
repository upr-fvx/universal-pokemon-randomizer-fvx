package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

//TODO: StringSettingDefinition
//Possibly some other oddball ones

/**
 * A definition of a setting. Establishes its name, category, default value,
 * how to determine if it is enabled and supported,
 * the possible values and, if applicable, how to determine if values are enabled and supported.
 * @param <T> The type of data the setting holds.
 */
public abstract class SettingDefinition<T extends Serializable> {

    //The setting's name. Should be a unique identifier. Should be relatively human-readable.
    protected final String name;

    //The setting's category. Should be the lowest-level category applicable (e.g. "StarterTypeRestrictions"
    //rather than "Starters" or "Starters, Statics, & Trades"
    protected final String category;

    //The default value.
    protected final T defaultValue;
    //TODO: variable default values? (By RomHandler only; changing default by SettingRestriction risks loops.)
    // There is at least one case for this (Starter BST limits) although it's not *extremely* necessary.
    // Two: Custom starters. (That's a bit more important.)

    protected final Class<? extends Serializable> type;

    //The prerequisite of other settings' states required for this setting to be applicable.
    //If the prerequisite conditions are false, this setting will be disabled and set to the default value.
    private final SettingRestriction prerequisite;

    //The conditions of the RomHandler (that is to say, the game) required for this setting to be applicable.
    //If the conditions are not met, the setting will be hidden and ignored.
    private final Predicate<RomHandler> supported;

    //A list of settings that disable or apply restrictions to this setting.
    //Different types of settings apply restrictions differently, so this list can only check if changes MIGHT occur,
    //not determine if they actually do occur or what those changes are.
    private final List<String> dependentOn;

    //Whether the setting has values that are not supported by all games. If true, the possible values will be
    // polled when a new game is loaded.
    private final boolean hasSupportRestrictions;


    /**
     *
     * @param name
     * @param category
     * @param defaultValue
     * @param prerequisite
     * @param supported
     * @param hasValueSupportRestrictions
     * @param valueRestrictors
     */
    public SettingDefinition(String name, String category, T defaultValue,
                             SettingRestriction prerequisite, Predicate<RomHandler> supported,
                             boolean hasValueSupportRestrictions, List<String> valueRestrictors) {
        this.name = name;
        this.category = category;
        this.defaultValue = defaultValue;
        type = defaultValue.getClass();
        this.prerequisite = prerequisite;
        this.supported = supported;
        Set<String> restrictors = new HashSet<>();
        if(valueRestrictors != null) {
             restrictors.addAll(valueRestrictors);
        }
        if (prerequisite != null)
        {
            restrictors.addAll(prerequisite.getRelevantSettingNames());
        }
        this.dependentOn = Collections.unmodifiableList(new ArrayList<>(restrictors));
        this.hasSupportRestrictions = hasValueSupportRestrictions;
    }

    /**
     * Creates a new SettingDefinition.
     * @param name The setting's name.
     * @param category The category the setting falls under.
     * @param defaultValue
     * @param prerequisite
     * @param supported
     * @param valueRestrictions
     * @param hasValueSupportRestrictions
     */
    public SettingDefinition(String name, String category, T defaultValue,
                             SettingRestriction prerequisite, Predicate<RomHandler> supported,
                             Collection<SettingRestriction> valueRestrictions, boolean hasValueSupportRestrictions) {
        this.name = name;
        this.category = category;
        this.defaultValue = defaultValue;
        type = defaultValue.getClass();
        this.prerequisite = prerequisite;
        this.supported = supported;

        Set<String> restrictors = new HashSet<>();
        if (valueRestrictions != null) {
            for (SettingRestriction restriction : valueRestrictions) {
                restrictors.addAll(restriction.getRelevantSettingNames());
            }
        }
        if (prerequisite != null)
        {
            restrictors.addAll(prerequisite.getRelevantSettingNames());
        }
        this.dependentOn = Collections.unmodifiableList(new ArrayList<>(restrictors));
        this.hasSupportRestrictions = hasValueSupportRestrictions;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    //TODO: on implementing, we may decide it's more convenient to load the SettingsManager ahead of time

    /**
     * Checks to see if the setting should be enabled with the current state of the SettingsManager given.
     * Does not check if the setting is supported by the game, nor if any specific values are disabled.
     * @param manager The SettingsManager to check against.
     * @return True if the setting is enabled, false otherwise.
     */
    public boolean isEnabled(SettingsManager manager) {
        if(prerequisite == null) {
            return true;
        }

        return prerequisite.test(manager);
    }

    /**
     * Checks to see if the setting is supported by the RomHandler given.
     * Does not check if specific values are supported.
     * @param game The game (RomHandler) to check against.
     * @return True if the setting is supported, false otherwise.
     */
    public boolean isSupported(RomHandler game) {
        if (supported == null) {
            return true;
        }
        return supported.test(game);
    }

    /**
     * Checks whether the given value is EVER a valid value for this setting.
     * @param value The value to check.
     * @return True if the value is valid, false otherwise.
     */
    public abstract boolean isValueValid(T value);

    /**
     * Tests to see if the given value is enabled given the current SettingsManager state.
     * Does not check if the setting is enabled as a whole; both should be checked.
     * Not guaranteed to return false if the value is invalid; that should also be checked.
     * @param value The particular value to check.
     * @param manager The SettingsManager to test against.
     * @return True if the value is enabled, false otherwise.
     */
    public abstract boolean isValueEnabled(T value, SettingsManager manager);

    /**
     * Tests to see if the given value is supported for the given game.
     * Does not check if the setting is supported as a whole; both should be checked.
     * @param value The particular value to check.
     * @param game The RomHandler to check for support.
     * @return True if the value is supported, false otherwise.
     */
    public abstract boolean isValueSupported(T value, RomHandler game);

    /**
     * Determines if all conditions are satisfied such that the setting can be set to this value.
     * Returns false for ALL values (including the default value) if the setting is disabled or unsupported.
     * @param value The value to test.
     * @param manager The SettingsManager to test conditions against.
     * @param game The RomHandler to test support against, or null to not test support.
     * @return If the setting can be set to this value.
     */
    public boolean isValueSettable(T value, SettingsManager manager, RomHandler game) {
        return isEnabled(manager)
                && (game == null || isSupported(game))
                && isValueValid(value)
                && isValueEnabled(value, manager)
                && (game == null || isValueSupported(value, game));
    }

    //TODO: compilation functions? (isSettingActive, isValueValid, isValueFullyEnabled, isValueFullySupported, etc

    /**
     * Returns a list of all settings which have states that enable/disable this setting or some of its values.
     * If there are none, returns an empty list.
     * @return An unmodifiable List containing the names of all settings which this setting is dependent on.
     */
    public List<String> getSettingsDependentOn()
    {
        return dependentOn;
    }

    /**
     * Whether the setting has restrictions on what values are supported based on the game (RomHandler)
     * being randomized.
     * Does NOT include if the entire setting is disabled based on support. isSupported should ALWAYS be called.
     * TODO: consider if this should be changed?
     * @return True if there are any restrictions on values based on support.
     */
    public boolean hasValueSupportRestrictions()
    {
        return hasSupportRestrictions;
    }

    /**
     * Gets the type of object that this setting stores.
     * @return The Class of the setting's initial value.
     */
    public Class<? extends Serializable> getType() {
        return type;
    }
}
