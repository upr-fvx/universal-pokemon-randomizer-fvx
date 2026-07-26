package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.sun.istack.internal.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.function.Predicate;

public abstract class SettingDefinition<T> {

    //The setting's name. Should be a unique identifier. Should be relatively human-readable.
    private final String name;

    //The setting's category. Should be the lowest-level category applicable (e.g. "StarterTypeRestrictions"
    //rather than "Starters" or "Starters, Statics, & Trades"
    private final String category;

    //The default value.
    private final T defaultValue;

    //The prerequisite of other settings' states required for this setting to be applicable.
    //If the prerequisite conditions are false, this setting will be disabled and set to the default value.
    private final SettingRestriction prerequisite;

    //The conditions of the RomHandler (that is to say, the game) required for this setting to be applicable.
    //If the conditions are not met, the setting will be hidden and ignored.
    private final Predicate<RomHandler> supported;

    //A list of settings that apply restrictions to some values of the setting.
    //Different types of settings apply restrictions differently, so this list can only check if changes might occur,
    //not determine if they actually do occur or what those changes are.
    private final List<String> valueRestrictors;

    //Whether the setting has values that are not supported by all games. If true, the possible values will be
    // polled when a new game is loaded.
    private final boolean hasValueSupportRestrictions;


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
        this.valueRestrictors = Collections.unmodifiableList(valueRestrictors);
        this.hasValueSupportRestrictions = hasValueSupportRestrictions;
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

        Set<String> valueRestrictors = new HashSet<>();
        if (valueRestrictions != null) {
            for (SettingRestriction restriction : valueRestrictions) {
                valueRestrictors.addAll(restriction.getRelevantSettingNames());
            }
        }
        this.valueRestrictors = Collections.unmodifiableList(new ArrayList<>(valueRestrictors));
        this.hasValueSupportRestrictions = hasValueSupportRestrictions;
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
        if(supported == null) {
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


    //These functions only make sense for numeric values,
    //but are being put in the base class anyway for ease of interfacing.
    //Obviously, they throw exceptions when called on a non-numeric SettingDefinition.
    //#region numeric-functions

    /**
     * The lowest value that can ever be applied to this setting.
     */
    public T minimum() { throw new NotImplementedException(); }

    /**
     * The highest value that can ever be applied to this setting.
     */
    public T maximum() { throw new NotImplementedException(); }

    /**
     * The lowest value that is currently enabled.
     * Note that this may be lower or higher than the lowest value supported,
     * so both should always be checked.
     * @param manager The SettingsManager to test against.
     * @return The lowest enabled value.
     */
    public T minimumEnabled(SettingsManager manager)
    {
        throw new NotImplementedException();
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
        throw new NotImplementedException();
    }

    /**
     * The lowest value that is supported by the current game.
     * @param game The RomHandler to check for support.
     * @return The lowest supported value.
     */
    public T minimumSupported(RomHandler game)
    {
        throw new NotImplementedException();
    }

    /**
     * The highest value that is supported by the current game.
     * @param game The RomHandler to check for support.
     * @return The highest supported value.
     */
    public T maximumSupported(RomHandler game)
    {
        throw new NotImplementedException();
    }

    //#endregion
}
