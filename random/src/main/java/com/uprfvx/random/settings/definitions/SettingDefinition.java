package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

//TODO: StringSettingDefinition
//Possibly some other oddball ones

public abstract class SettingDefinition<T extends Serializable> {

    //The setting's name. Should be a unique identifier. Should be relatively human-readable.
    protected final String name;

    //The setting's category. Should be the lowest-level category applicable (e.g. "StarterTypeRestrictions"
    //rather than "Starters" or "Starters, Statics, & Trades"
    protected final String category;

    //The default value.
    protected final T defaultValue;

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
        this.prerequisite = prerequisite;
        this.supported = supported;
        Set<String> restrictors = new HashSet<>(valueRestrictors);
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
    public boolean isEnabled(SettingsManager manager) {
        if(prerequisite == null) {
            return true;
        }

        return prerequisite.test(manager);
    }

    public boolean isSupported(RomHandler game) {
        if (supported == null) {
            return true;
        }
        return supported.test(game);
    }

    /**
     * Tests to see if the given value is valid given the current SettingsManager state.
     * @param value The particular value to check.
     * @param manager The SettingsManager to test against.
     * @return True if the value is enabled, false otherwise.
     */
    public abstract boolean isValueEnabled(T value, SettingsManager manager);

    /**
     * Tests to see if the given value is supported for the given game.
     * @param value The particular value to check.
     * @param game The RomHandler to check for support.
     * @return True if the value is enabled, false otherwise.
     */
    public abstract boolean isValueSupported(T value, RomHandler game);

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
}
