package com.uprfvx.random.settings;

/*----------------------------------------------------------------------------*/
/*--  Settings.java - encapsulates a configuration of settings used by the  --*/
/*--                  randomizer to determine how to randomize the          --*/
/*--                  target game.                                          --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.uprfvx.random.Version;
import com.uprfvx.random.settings.definitions.NumericSettingDefinition;
import com.uprfvx.random.settings.definitions.SettingDefinition;
import com.uprfvx.random.settings.settingstring.SettingsStringUpdater;
import com.uprfvx.romio.romhandlers.*;
import filefunctions.FileFunctions;
import filefunctions.IOFunctions;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.CRC32;

import static com.uprfvx.random.settings.Settings.ALL_SETTINGS;
import static com.uprfvx.random.settings.Settings.Name;
import com.uprfvx.random.settings.Settings.Category;

public class SettingsManager {

    //region private fields

    private Map<Name, SettingState<? extends Serializable>> settingStates;
    private Map<Name, Set<Name>> dependencies;
    //A reverse lookup of setting restrictions (full setting and value).
    //Needed so that dependent settings can be updated, and listeners alerted, when a setting is changed.
    private Map<Category, List<Name>> categorizedNames;
    private Map<Name, Set<SettingChangeListener>> listeners;

    private Set<SettingChangeListener> universalListeners;

    private RomHandler game;

    //endregion

    //region public functions

    /**
     * Creates a SettingsManager without associating it to a game/{@link RomHandler}.
     */
    public SettingsManager() {
        initializeSettings();

        listeners = new HashMap<>();
        universalListeners = new HashSet<>();
        game = null;
    }

    /**
     * Creates a SettingsManager and immediately associates it with a game/{@link RomHandler}.
     */
    public SettingsManager(RomHandler game) {
        initializeSettings();

        listeners = new HashMap<>();
        universalListeners = new HashSet<>();
        associateGame(game);
    }

    /**
     * Retrieves the value of the requested setting.
     * @param settingName The setting to retrieve the value of.
     * @return The value of the setting.
     * @param <T> The type of the setting.
     * @throws IllegalArgumentException if there is no setting of the given name.
     * @throws ClassCastException if the setting's value cannot be cast to T.
     */
    public <T extends Serializable> T get(Name settingName) {
        SettingState<T> state = getTypedState(settingName);
        try {
            return state.getValue();
        } catch(ClassCastException e) {
            System.out.println("Cannot cast setting \"" + settingName + "\" to the requested type!");
            System.out.println("Setting's type: " + state.getValue().getClass().getName());
            throw e;
        }
    }



    /**
     * Sets the requested setting's state to the given value.
     * May cause other settings to change their enable/disable states and possibly return to default value.
     * For security reasons, only works if the type of the value given exactly matches the type of the setting's
     * current value.
     * @param settingName The setting to set.
     * @param newValue The value to set the setting to.
     * @param <T> The type of the setting.
     * @return True if the value now matches the value set, false if the value is not currently valid.
     * @throws IllegalArgumentException if there is no setting of the given name,
     *                                  or if the type of the setting does not match the type of the value.
     */
    public <T extends Serializable> boolean set(Name settingName, T newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("Cannot set settings to null!");
        }

        SettingState<T> state = getTypedState(settingName);
        SettingDefinition<T> definition = state.getDefinition();

        valueTypeCheck(newValue, definition);

        T currentValue = state.getValue();

        if (currentValue.equals(newValue))
            return true; //if the setting is already set to the relevant value, save us checking dependencies

        if (!definition.isValueSettable(newValue, this, game)) {
            System.out.println("value was not settable: " + settingName + ", " + newValue);
            System.out.println("current value is: " + currentValue + " (" + currentValue.getClass() + ")");
            // TODO: should this method throw here, or return a boolean?
            throw new RuntimeException();
            //return false;
        }

        state.setValue(newValue);

        alertListenersToManualChange(settingName);

        Set<Name> possibleChanges = checkDependencies(settingName);
        if(possibleChanges != null)
            alertListenersToPossibleEnablementChanges(possibleChanges);

        return true;
    }

    /**
     * Adds the given listener to the set of listeners for the given setting.
     * @param settingName The setting to listen to.
     * @param listener The listener to add.
     * @throws IllegalArgumentException If no setting of that name exists.
     */
    public void addListener(Name settingName, SettingChangeListener listener) {
        if (settingStates.get(settingName) == null)
            throw new IllegalArgumentException("The setting \"" + settingName + "\" does not exist.");

        Set<SettingChangeListener> settingListeners = listeners.computeIfAbsent(settingName,
                set -> new HashSet<>());
        settingListeners.add(listener);
    }

    /**
     * Removes the given listener from the set of listeners for the given setting.
     * @param settingName The setting to stop listening to.
     * @param listener The listener to remove.
     * @return True if the listener was removed, false if it was not present.
     * @throws IllegalArgumentException If no setting of that name exists.
     */
    public boolean removeListener(Name settingName, SettingChangeListener listener) {
        if (settingStates.get(settingName) == null)
            throw new IllegalArgumentException("The setting \"" + settingName + "\" does not exist.");

        Set<SettingChangeListener> settingListeners = listeners.get(settingName);
        if(settingListeners == null)
            return false;

        return settingListeners.remove(listener);
    }

    /**
     * Adds a listener that is alerted to changes to ALL settings.
     * @param listener The listener to add.
     */
    public void addUniversalListener(SettingChangeListener listener) {
        universalListeners.add(listener);
    }

    /**
     * Removes the given listener from the set of universal listeners.
     * @param listener The listener to remove.
     * @return True if the listener was removed, false if it was not present.
     */
    public boolean removeUniversalListener(SettingChangeListener listener) {
        return universalListeners.remove(listener);
    }

    /**
     * Associates a game (in RomHandler form) with this SettingsManager, for purposes of determining which
     * settings are supported.
     * Immediately resets all unsupported settings and values to their default values.
     * The game must be unassociated before another game can be associated.
     * @param game The game to associate this SettingsManager with.
     */
    public void associateGame(RomHandler game) {
        if (this.game != null) {
            throw new IllegalStateException("Current game must be unassociated before associating new game.");
        }

        this.game = game;

        Set<Name> changed = new HashSet<>();

        for (Map.Entry<Name, SettingState<? extends Serializable>> setting : settingStates.entrySet()) {
            Name name = setting.getKey();
            SettingState<?> state = setting.getValue();

            boolean didReset = false;
            if(!state.currentValueIsSupported(game)) {
                changed.add(name);
                state.reset();
                didReset = true;
            }

            SettingDefinition<?> definition = state.getDefinition();
            boolean isSupported = !definition.isSupported(game);
            alertListenersToSupportEvents(name, !isSupported, isSupported,
                    definition.hasValueSupportRestrictions(), didReset, game);
        }

        Set<Name> possiblyChanged = new HashSet<>();
        for(Name changedSettingName : changed) {
            Set<Name> changedDependencies = checkDependencies(changedSettingName);
            if(changedDependencies != null)
                possiblyChanged.addAll(changedDependencies);
        }

        alertListenersToPossibleEnablementChanges(possiblyChanged);
    }

    /**
     * Removes the current game association, causing all settings to be considered supported.
     */
    public void unassociateGame() {
        RomHandler oldGame = game;
        game = null;

        for (Map.Entry<Name, SettingState<? extends Serializable>> setting : settingStates.entrySet()) {
            Name name = setting.getKey();
            SettingState<?> state = setting.getValue();

            SettingDefinition<?> definition = state.getDefinition();
            alertListenersToSupportEvents(name, !definition.isSupported(oldGame), true,
                    definition.hasValueSupportRestrictions(), false, null);
        }
    }

    /**
     * Finds the minimum value currently settable to the setting requested.
     * @param settingName The setting to find the minimum for.
     * @return The minimum settable value for the setting.
     * @param <N> The type of number stored in the setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    public <N extends Number & Comparable<N>> N getCurrentMinimum(Name settingName) {
        N validMinimum = getValidMinimum(settingName);
        N enabledMinimum = getEnabledMinimum(settingName);
        N supportedMinimum = getSupportedMinimum(settingName);

        return max(validMinimum, enabledMinimum, supportedMinimum);
    }

    /**
     * Finds the minimum value that is ever valid for the setting requested.
     * @param settingName The setting to find the minimum for.
     * @return The minimum valid value for the setting.
     * @param <N> The type of number stored in the setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    public <N extends Number & Comparable<N>> N getValidMinimum(Name settingName) {
        NumericSettingDefinition<N> definition = getTypedNumericDefinition(settingName);
        return definition.getMinimum();
    }

    /**
     * Finds the minimum value currently enabled for the setting requested.
     * For most cases, getCurrentMinimum is preferred.
     * @param settingName The setting to find the minimum for.
     * @return The minimum enabled value for the setting.
     * @param <N> The type of number stored in the setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    public <N extends Number & Comparable<N>> N getEnabledMinimum(Name settingName) {
        NumericSettingDefinition<N> definition = getTypedNumericDefinition(settingName);
        return definition.minimumEnabled(this);
    }

    /**
     * Finds the minimum value currently supported for the setting requested.
     * For most cases, getCurrentMinimum is preferred.
     * @param settingName The setting to find the minimum for.
     * @return The minimum supported value for the setting.
     * @param <N> The type of number stored in the setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    public <N extends Number & Comparable<N>> N getSupportedMinimum(Name settingName) {
        NumericSettingDefinition<N> definition = getTypedNumericDefinition(settingName);
        return definition.minimumSupported(game);
    }

    /**
     * Finds the maximum value currently settable to the setting requested.
     * @param settingName The setting to find the maximum for.
     * @return The maximum settable value for the setting.
     * @param <N> The type of number stored in the setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    public <N extends Number & Comparable<N>> N getCurrentMaximum(Name settingName) {
        N validMaximum = getValidMaximum(settingName);
        N enabledMaximum = getEnabledMaximum(settingName);
        N supportedMaximum = getSupportedMaximum(settingName);

        return min(validMaximum, enabledMaximum, supportedMaximum);
    }

    /**
     * Finds the maximum value that is ever valid for the setting requested.
     * @param settingName The setting to find the maximum for.
     * @return The maximum valid value for the setting.
     * @param <N> The type of number stored in the setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    public <N extends Number & Comparable<N>> N getValidMaximum(Name settingName) {
        NumericSettingDefinition<N> definition = getTypedNumericDefinition(settingName);
        return definition.getMaximum();
    }

    /**
     * Finds the maximum value currently enabled for the setting requested.
     * @param settingName The setting to find the maximum for.
     * @return The maximum enabled value for the setting.
     * @param <N> The type of number stored in the setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    public <N extends Number & Comparable<N>> N getEnabledMaximum(Name settingName) {
        NumericSettingDefinition<N> definition = getTypedNumericDefinition(settingName);
        return definition.maximumEnabled(this);
    }

    /**
     * Finds the maximum value currently supported for the setting requested.
     * @param settingName The setting to find the maximum for.
     * @return The maximum supported value for the setting.
     * @param <N> The type of number stored in the setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    public <N extends Number & Comparable<N>> N getSupportedMaximum(Name settingName) {
        NumericSettingDefinition<N> definition = getTypedNumericDefinition(settingName);
        return definition.maximumSupported(game);
    }

    /**
     * Checks whether the requested setting as a whole is enabled.
     * Can still be true if some values of the setting are disabled.
     * @param settingName The setting to check.
     * @return True if the setting is enabled, false if it is disabled.
     * @throws IllegalArgumentException if there is no setting with the given name.
     */
    public boolean isEnabled(Name settingName) {
        return getUntypedDefinition(settingName).isEnabled(this);
    }

    /**
     * Checks whether the requested setting as a whole is supported.
     * Can still be true if some values of the setting are unsupported.
     * @param settingName The setting to check.
     * @return True if the setting is supported, false if it is unsupported.
     * @throws IllegalArgumentException if there is no setting with the given name.
     */
    public boolean isSupported(Name settingName) {
        SettingDefinition<?> definition = getUntypedDefinition(settingName);
        //Pulling definition now for the checks performed.

        if(game == null)
            return true;

        return definition.isSupported(game);
    }

    /**
     * Checks whether the requested setting is currently in its default state.
     * @return True if the setting is in its default state, false otherwise.
     * @throws IllegalArgumentException if there is no setting with the given name.
     */
    public boolean isDefault(Name settingName) {
        SettingState<?> state = getTypedState(settingName);
        return state.isDefault();
    }

    /**
     * Checks whether the value given can ever be a valid value for the requested setting.
     * Always returns false if the value is null, since null values are never valid for any setting.
     * @param settingName The setting to check.
     * @param value The value to check.
     * @return True if the value is valid, false otherwise.
     * @param <T> The type of the value given.
     * @throws IllegalArgumentException if there is no setting with the given name,
     *                                  or if the given value is the wrong type for the setting.
     */
    public <T extends Serializable> boolean isValueValid(Name settingName, T value)
    {
        if(value == null)
            return false;

        SettingDefinition<T> definition = getTypedDefinition(settingName);

        valueTypeCheck(value, definition);

        return definition.isValueValid(value);
    }

    /**
     * Checks whether the value given is currently enabled, given the state of the SettingsManager.
     * Also returns false if the value is not valid. Does not check if the setting as a whole is disabled.
     * @param settingName The setting to check.
     * @param value The value to check.
     * @return False if the value is invalid or disabled, true otherwise.
     * @param <T> The type of the value given.
     * @throws IllegalArgumentException if there is no setting with the given name,
     *                                  or if the given value is the wrong type for the setting.
     */
    public <T extends Serializable> boolean isValueEnabled(Name settingName, T value)
    {
        if(!isValueValid(settingName, value))
            return false;
        //This also performs the type check for us.

        return getTypedDefinition(settingName).isValueEnabled(value, this);
    }

    /**
     * Checks whether the value given is currently supported, given the game currently loaded into the SettingsManager.
     * Also returns false if the value is not valid. Does not check if the setting as a whole is unsupported.
     * @param settingName The setting to check.
     * @param value The value to check.
     * @return False if the value is invalid or unsupported, true otherwise.
     * @param <T> The type of the value given.
     * @throws IllegalArgumentException if there is no setting with the given name,
     *                                  or if the given value is the wrong type for the setting.
     */
    public <T extends Serializable> boolean isValueSupported(Name settingName, T value)
    {
        if(!isValueValid(settingName, value))
            return false;
        //This also performs the type check for us.

        if(game == null)
            return true;

        return getTypedDefinition(settingName).isValueSupported(value, game);
    }

    /**
     * Resets all settings to their default values.
     */
    public void resetAll() {
        settingStates.forEach((name, state) -> {
            if (!state.isDefault()) {
                state.reset();
                alertListenersToManualChange(name); //debatable if this counts as manual or automatic
                alertListenersToPossibleEnablementChanges(dependencies.get(name));
            }
        });
    }

    /**
     * Resets the given setting to its default value. May change other settings as a result.
     * @param settingName The setting to reset.
     * @throws IllegalArgumentException if there is no setting with the given name.
     */
    public void reset(Name settingName) {
        SettingState<?> state = getUntypedState(settingName);
        if (!state.isDefault()) {
            state.reset();
            alertListenersToManualChange(settingName);

            Set<Name> possiblyChanged = checkDependencies(settingName);
            alertListenersToPossibleEnablementChanges(possiblyChanged);
        }
    }

    //endregion

    //region private and package-private functions

    /**
     * Gets the setting indicated, or throws if it does not exist.
     * @param settingName The name of the setting to retrieve.
     * @return The setting's state.
     * @throws IllegalArgumentException if there is no setting of the given name.
     */
    private SettingState<?> getUntypedState(Name settingName) {
        SettingState<?> state = settingStates.get(settingName);
        if(state == null)
        {
            throw new IllegalArgumentException("The setting \"" + settingName + "\" does not exist!");
        }

        return state;
    }

    /**
     * Gets the definition of the indicated setting, or throws if the setting does not exist.
     * @param settingName The name of the setting to retrieve the definition of.
     * @return The setting's defintion.
     * @throws IllegalArgumentException if there is no setting with the given name.
     */
    private SettingDefinition<?> getUntypedDefinition(Name settingName) {
        return getUntypedState(settingName).getDefinition();
    }

    /**
     * The unsafe part of the implementation. Given a setting name, retrieves its setting state,
     * then casts it to SettingState<T>.<br>
     * This cast may be improper; as far as I know, the language gives us no way to check.
     * Therefore, we should make every effort to ensure any operations performed
     * with the resulting SettingState are either checked or non-dangerous.
     * @param settingName The name of the setting to retrieve.
     * @return The setting's state, cast to SettingState<T>.
     * @param <T> The type of the setting.
     * @throws IllegalArgumentException if there is no setting of the given name.
     */
    @SuppressWarnings("unchecked")
    private <T extends Serializable> SettingState<T> getTypedState(Name settingName) {
        SettingState<?> uncastState = getUntypedState(settingName);
        return (SettingState<T>) uncastState;
    }

    /**
     * Retrieves the definition of the requested setting, cast to SettingDefinition<T>. <br>
     * Note: Like getTypedState, this cast may be improper. Always make sure any operations performed
     * with the returned SettingDefinition are type checked as much as possible.
     * @param settingName The setting to retrieve the definition of.
     * @return The setting's definition.
     * @param <T> The type of the setting.
     * @throws IllegalArgumentException if there is no setting of the given name.
     */
    private <T extends Serializable> SettingDefinition<T> getTypedDefinition(Name settingName) {
        SettingState<T> state = getTypedState(settingName);

        return state.getDefinition();
    }

    /**
     * Retrieves the definition of the requested setting, cast to NumericSettingDefinition<N>. <br>
     * Note: This cast may be improper. Always make sure any operations performed
     * with the returned SettingDefinition are type checked as much as possible.
     * @param settingName The setting's name.
     * @return The setting's definition, cast to NumericSettingDefinition<N>.
     * @param <N> The type of number stored in the Setting.
     * @throws ClassCastException if the setting given is not defined with a NumericSettingDefinition
     */
    private <N extends Number & Comparable<N>> NumericSettingDefinition<N> getTypedNumericDefinition(Name settingName) {
        SettingDefinition<N> definition = getTypedDefinition(settingName);

        return (NumericSettingDefinition<N>) definition;
    }

    /**
     * Checks that the value given is the appropriate type for the setting given.
     * Throws an IllegalArgumentException if it is not.
     * @param value The value to check.
     * @param state The state of the setting to check against.
     * @param <T> The type of the value given.
     * @throws IllegalArgumentException if the type of the value does not match the type for the setting.
     */
    private <T extends Serializable> void valueTypeCheck(T value, SettingState<?> state) {
        valueTypeCheck(value, state.getDefinition());
    }

    /**
     * Checks that the value given is the appropriate type for the setting given.
     * Throws an IllegalArgumentException if it is not.
     * @param value The value to check.
     * @param definition The definition of the setting to check against.
     * @param <T> The type of the value given.
     * @throws IllegalArgumentException if the type of the value does not match the type for the setting.
     */
    private <T extends Serializable> void valueTypeCheck(T value, SettingDefinition<?> definition) {
        if(value.getClass() != definition.getType()) {
            throw new IllegalArgumentException("Wrong type given for setting \"" + definition.getName() +
                    "Type given: " + value.getClass().getName() + "\n" +
                    "Type needed: " + definition.getType().getName());
        }
    }

    /**
     * Checks all settings dependent on this one and resets them to default if their current value is invalid.
     * @param settingName The changed setting to start from.
     * @return All settings which may have been enabled, disabled, partially enabled or disabled, or had their
     *         values change.
     */
    private Set<Name> checkDependencies(Name settingName) {

        Set<Name> dependents = dependencies.get(settingName);
        if (dependents == null)
            return null;

        Set<Name> possiblyChanged = new HashSet<>(dependents);

        for (Name dependentSetting : dependents) {
            if (!settingStates.get(dependentSetting).currentValueIsEnabled(this)) {
                settingStates.get(dependentSetting).reset();
                alertListenersToAutomaticChange(dependentSetting);
                Set<Name> recursedDependents = checkDependencies(dependentSetting);
                if (recursedDependents != null)
                    possiblyChanged.addAll(recursedDependents);
            }
        }

        return possiblyChanged;
    }

    /**
     * Populates the map of settings (and other related maps) from the setting definitions in Settings.ALL_SETTINGS
     */
    private void initializeSettings() {
        settingStates = new HashMap<Name, SettingState<? extends Serializable>>();
        dependencies = new HashMap<>();
        categorizedNames = new HashMap<>();

        for (SettingDefinition<?> definition : ALL_SETTINGS)
        {
            //Create an initial state from the definition (with default value)
            Name name = definition.getName();
            SettingState<?> state = new SettingState<>(definition);
            settingStates.put(name, state);

            //Register this setting as dependent on each setting it's dependent on
            List<Name> settingsDependentOn = definition.getSettingsDependentOn();
            for (Name otherSetting : settingsDependentOn) {
                Set<Name> otherSettingDependents = dependencies.computeIfAbsent(otherSetting,
                        set -> new HashSet<>());
                otherSettingDependents.add(name);
            }

            //Add the setting to its category, creating the category if needed.
            List<Name> category = categorizedNames.computeIfAbsent(definition.getCategory(),
                    _ -> new ArrayList<>());
            category.add(name);
        }
    }

    /**
     * A function to get ALL settings' states.
     * Should only be used for testing.
     * @return A collection containing every setting's state.
     */
    Collection<SettingState<? extends Serializable>> testGetAllSettings() {
        return settingStates.values();
    }

    /**
     * Gets a Stream of all listeners relevant to the setting given
     * (universal listeners and listeners for that setting).
     * @param settingName The setting in question.
     * @return A stream of every relevant listener.
     */
    private Stream<SettingChangeListener> getAllListeners(Name settingName) {
        if(listeners.get(settingName) != null) {
            return Stream.concat(universalListeners.stream(), listeners.get(settingName).stream());
        } else {
            return universalListeners.stream();
        }
    }

    private void alertListenersToPossibleEnablementChanges(Collection<Name> settingNames) {
        if (settingNames == null)
            return;

        for (Name name : settingNames) {

            Stream<SettingChangeListener> relevantListeners = getAllListeners(name);
            relevantListeners.forEach(l -> l.onPossibleEnablementChange(name, this));

        }
    }

    private void alertListenersToManualChange(Name settingName) {

        Stream<SettingChangeListener> relevantListeners = getAllListeners(settingName);
        relevantListeners.forEach(l -> l.onManualSettingChange(settingName, this));

    }

    private void alertListenersToAutomaticChange(Name settingName) {
        Stream<SettingChangeListener> relevantListeners = getAllListeners(settingName);
        relevantListeners.forEach(l -> l.onAutomaticSettingChange(settingName, this));
    }

    /**
     * Alerts listeners to various events that trigger on associating/unassociating a game.
     * @param settingName The name of the relevant setting.
     * @param supportChanged If the overall support status changed.
     * @param currentlySupported The current overall support status. (Only matters if supportChanged is true.)
     * @param hasValueRestrictions If the setting is listed as having support value restrictions.
     * @param automaticallyReset If the setting has been automatically reset.
     * @param game The game being loaded, or null if it is being unloaded.
     */
    private void alertListenersToSupportEvents(Name settingName, boolean supportChanged, boolean currentlySupported,
                                               boolean hasValueRestrictions, boolean automaticallyReset,
                                               RomHandler game) {
        if (!(supportChanged || hasValueRestrictions || automaticallyReset))
            return;

        Stream<SettingChangeListener> relevantListeners = getAllListeners(settingName);

        relevantListeners.forEach(l -> {
            if (supportChanged)
                l.onSupportChange(settingName, this, currentlySupported);
            if (hasValueRestrictions)
                l.onPossibleSupportedValuesChange(settingName, this, game);
            if (automaticallyReset)
                l.onAutomaticSettingChange(settingName, this);
        });
    }

    //Can't believe these aren't utility functions built into the language... anyway.
    //Maybe because they're non-deterministic if the comparators are equal
    // (but different in some non-comparison-affecting way)?
    @SafeVarargs
    private <N extends Comparable<N>> N min(N... comparators) {
        N currentMin = comparators[0];

        //this compares the first comparator to itself but whatever
        for (N comparator : comparators) {
            if (currentMin.compareTo(comparator) < 0)
                currentMin = comparator;
        }
        return currentMin;
    }

    @SafeVarargs
    private <N extends Comparable<N>> N max(N... comparators) {
        N currentMax = comparators[0];

        //this compares the first comparator to itself but whatever
        for (N comparator : comparators) {
            if (currentMax.compareTo(comparator) > 0)
                currentMax = comparator;
        }
        return currentMax;
    }

    //endregion









    //*********************************************************
    //BELOW LIES PRE-REFACTOR CODE

    public static final int VERSION = Version.LATEST.id;

    public static final int LENGTH_OF_SETTINGS_DATA = 69;
    public static final int LENGTH_OF_NAME_LENGTH = 1;
    public static final int LENGTH_OF_CHECKSUM = 4;
    // There used to be a checksum for the custom names, post the usual checksum
    // As you can see, the custom names no longer live here, but keeping the bytes as padding
    // makes older settings easier to read.
    public static final int LENGTH_OF_END_PADDING = 4;
    public static final int TOTAL_LENGTH_EXCEPT_NAME =
            LENGTH_OF_SETTINGS_DATA + LENGTH_OF_NAME_LENGTH + LENGTH_OF_CHECKSUM + LENGTH_OF_END_PADDING;

    public static final int MAKE_EVOLUTIONS_EASIER_DEFAULT_LVL = 40;

    private String romName;
    private boolean updatedFromOldVersion = false;

    public void writeToFileFormat(FileOutputStream out) throws IOException {
        byte[] settings = toStringWithoutVersion().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(settings.length + 8);
        buf.putInt(VERSION);
        buf.putInt(settings.length);
        buf.put(settings);
        out.write(buf.array());
    }

    public static SettingsManager readFromFileFormat(FileInputStream in) throws IOException, UnsupportedOperationException {
        byte[] versionBytes = new byte[4];
        byte[] lengthBytes = new byte[4];
        int nread = in.read(versionBytes);
        if (nread < 4) {
            throw new UnsupportedOperationException("Error reading version number from settings string.");
        }
        int version = ByteBuffer.wrap(versionBytes).getInt();
        if (((version >> 24) & 0xFF) > 0 && ((version >> 24) & 0xFF) <= 172) {
            throw new UnsupportedOperationException("The settings file is too old to update and cannot be loaded.");
        }
        if (version > VERSION) {
            throw new UnsupportedOperationException("Cannot read settings from a newer version of the randomizer.");
        }
        nread = in.read(lengthBytes);
        if (nread < 4) {
            throw new UnsupportedOperationException("Error reading settings length from settings string.");
        }
        int length = ByteBuffer.wrap(lengthBytes).getInt();
        byte[] buffer = FileFunctions.readFullyIntoBuffer(in, length);
        String settings = new String(buffer, StandardCharsets.UTF_8);

        return fromStringAndVersion(settings, version);
    }

    /**
     * Creates a Settings object from a settings string WITH its first 3 chars being the version ID,
     * of the version the string was created in. Updates the Settings object if needed.
     */
    public static SettingsManager fromString(String withVersion) {
        int version = Integer.parseInt(withVersion.substring(0, 3));
        String withoutVersion = withVersion.substring(3);
        return fromStringAndVersion(withoutVersion, version);
    }

    private static SettingsManager fromStringAndVersion(String s, int version) {
        boolean updated = false;
        if (version < VERSION) {
            updated = true;
            s = new SettingsStringUpdater().update(version, s);
        }
        SettingsManager settings = fromStringWithoutVersion(s);
        settings.setUpdatedFromOldVersion(updated);
        return settings;
    }

    @Override
    public String toString() {
        return VERSION + toStringWithoutVersion();
    }

    // TODO: remove once we've tested SettingsStringConverter
    @Deprecated
    private String toStringWithoutVersion() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        /*

        // 0: general options #1 + trainer/class names
        out.write(makeByteSelected(changeImpossibleEvolutions, updateMoves, updateMovesLegacy, randomizeTrainerNames,
                randomizeTrainerClassNames, makeEvolutionsEasier, removeTimeBasedEvolutions, estimateLevelForEvolutionImprovements));

        // 1: pokemon base stats
        out.write(makeByteSelected(baseStatsFollowEvolutions, baseStatisticsMod == BaseStatisticsMod.RANDOM,
                baseStatisticsMod == BaseStatisticsMod.SHUFFLE, baseStatisticsMod == BaseStatisticsMod.UNCHANGED,
                standardizeEXPCurves, updateBaseStats, baseStatsFollowMegaEvolutions, assignEvoStatsRandomly));

        // 2: pokemon types
        out.write(makeByteSelected(speciesTypesMod == SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS,
                speciesTypesMod == SpeciesTypesMod.COMPLETELY_RANDOM, speciesTypesMod == SpeciesTypesMod.UNCHANGED,
                false, false, false, typesFollowMegaEvolutions, dualTypeOnly));

        // 3: abilities
        out.write(makeByteSelected(abilitiesMod == AbilitiesMod.UNCHANGED, abilitiesMod == AbilitiesMod.RANDOMIZE,
                allowWonderGuard, abilitiesFollowEvolutions, banTrappingAbilities, banNegativeAbilities, banBadAbilities,
                abilitiesFollowMegaEvolutions));

        // 4: starter pokemon stuff
        out.write(makeByteSelected(startersMod == StartersMod.CUSTOM, startersMod == StartersMod.COMPLETELY_RANDOM,
                startersMod == StartersMod.UNCHANGED, startersMod == StartersMod.RANDOM_WITH_TWO_EVOLUTIONS,
                randomizeStartersHeldItems, banBadRandomStarterHeldItems, allowStarterAltFormes,
                startersMod == StartersMod.RANDOM_BASIC));

        // 5 - 10: dropdowns
        write2ByteIntBigEndian(out, customStarters[0]);
        write2ByteIntBigEndian(out, customStarters[1]);
        write2ByteIntBigEndian(out, customStarters[2]);

        // 11 movesets
        out.write(makeByteSelected(movesetsMod == MovesetsMod.COMPLETELY_RANDOM,
                movesetsMod == MovesetsMod.RANDOM_PREFER_SAME_TYPE, movesetsMod == MovesetsMod.UNCHANGED,
                movesetsMod == MovesetsMod.METRONOME_ONLY, startWithGuaranteedMoves, reorderDamagingMoves)
                | ((guaranteedMoveCount - 2) << 6));

        // 12 movesets good damaging
        out.write((movesetsForceGoodDamaging ? 0x80 : 0) | movesetsGoodDamagingPercent);

        // 13 trainer pokemon
        out.write(makeByteSelected(trainersMod == TrainersMod.UNCHANGED,
                trainersMod == TrainersMod.RANDOM,
                trainersMod == TrainersMod.DISTRIBUTED,
                trainersMod == TrainersMod.MAINPLAYTHROUGH,
                trainersMod == TrainersMod.TYPE_THEMED,
                trainersMod == TrainersMod.TYPE_THEMED_ELITE4_GYMS,
                trainersMod == TrainersMod.KEEP_THEMED,
                trainersMod == TrainersMod.KEEP_THEME_OR_PRIMARY));
        
        // 14 trainer pokemon evolution level modifier
        out.write(trainersEvolutionLevelModifier - 28);  // Shift to int8 range: [-100, 155] --> [-128, 127]

        // 15 wild pokemon (areas)
        out.write(makeByteSelected(!randomizeWildPokemon,
                wildPokemonZoneMod == WildPokemonZoneMod.NONE,
                wildPokemonZoneMod == WildPokemonZoneMod.ENCOUNTER_SET,
                wildPokemonZoneMod == WildPokemonZoneMod.GAME,
                keepWildEvolutionFamilies,
                wildPokemonZoneMod == WildPokemonZoneMod.NAMED_LOCATION,
                wildPokemonZoneMod == WildPokemonZoneMod.MAP,
                splitWildZoneByEncounterTypes));

        // 16 wild pokemon (restriction)
        out.write(makeByteSelected(false,
                similarStrengthEncounters,
                catchEmAllEncounters,
                false, false, false, false, false));

        // 17 wild pokemon (types/evolutions)
        out.write(makeByteSelected(wildPokemonTypeMod == WildPokemonTypeMod.NONE,
                wildPokemonTypeMod == WildPokemonTypeMod.KEEP_PRIMARY,
                wildPokemonTypeMod == WildPokemonTypeMod.RANDOM_THEMES,
                keepWildTypeThemes,
                wildPokemonEvolutionMod == WildPokemonEvolutionMod.NONE,
                wildPokemonEvolutionMod == WildPokemonEvolutionMod.BASIC_ONLY,
                wildPokemonEvolutionMod == WildPokemonEvolutionMod.KEEP_STAGE,
                false));

        // 18 wild pokemon (various)
        out.write(makeByteSelected(useTimeBasedEncounters, useMinimumCatchRate,
                blockWildLegendaries, randomizeWildPokemonHeldItems,
                banBadRandomWildPokemonHeldItems, balanceShakingGrass,
                false, false));

        // 19 static pokemon
        out.write(makeByteSelected(staticPokemonMod == StaticPokemonMod.UNCHANGED,
                staticPokemonMod == StaticPokemonMod.RANDOM_MATCHING,
                staticPokemonMod == StaticPokemonMod.COMPLETELY_RANDOM,
                staticPokemonMod == StaticPokemonMod.SIMILAR_STRENGTH,
                limitMainGameLegendaries, limit600, allowStaticAltFormes, swapStaticMegaEvos));

        // 20 tm randomization
        out.write(makeByteSelected(tmsHmsCompatibilityMod == TMsHMsCompatibilityMod.COMPLETELY_RANDOM,
                tmsHmsCompatibilityMod == TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE,
                tmsHmsCompatibilityMod == TMsHMsCompatibilityMod.UNCHANGED, tmsMod == TMsMod.RANDOM,
                tmsMod == TMsMod.UNCHANGED, tmLevelUpMoveSanity, keepFieldMoveTMs,
                tmsHmsCompatibilityMod == TMsHMsCompatibilityMod.FULL));

        // 21 tms part 2
        out.write(makeByteSelected(fullHMCompat, tmsFollowEvolutions, tutorFollowEvolutions));

        // 22 tms good damaging
        out.write((tmsForceGoodDamaging ? 0x80 : 0) | tmsGoodDamagingPercent);

        // 23 move tutor randomization
        out.write(makeByteSelected(moveTutorsCompatibilityMod == MoveTutorsCompatibilityMod.COMPLETELY_RANDOM,
                moveTutorsCompatibilityMod == MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE,
                moveTutorsCompatibilityMod == MoveTutorsCompatibilityMod.UNCHANGED,
                moveTutorMovesMod == MoveTutorMovesMod.RANDOM, moveTutorMovesMod == MoveTutorMovesMod.UNCHANGED,
                tutorLevelUpMoveSanity, keepFieldMoveTutors,
                moveTutorsCompatibilityMod == MoveTutorsCompatibilityMod.FULL));

        // 24 tutors good damaging
        out.write((tutorsForceGoodDamaging ? 0x80 : 0) | tutorsGoodDamagingPercent);

        // 25 in game trades
        out.write(makeByteSelected(inGameTradesMod == InGameTradesMod.RANDOMIZE_GIVEN_AND_REQUESTED,
                inGameTradesMod == InGameTradesMod.RANDOMIZE_GIVEN, randomizeInGameTradesItems,
                randomizeInGameTradesIVs, randomizeInGameTradesNicknames, randomizeInGameTradesOTs,
                inGameTradesMod == InGameTradesMod.UNCHANGED));

        // 26 field items
        out.write(makeByteSelected(fieldItemsMod == FieldItemsMod.RANDOM, fieldItemsMod == FieldItemsMod.SHUFFLE,
                fieldItemsMod == FieldItemsMod.UNCHANGED, banBadRandomFieldItems, fieldItemsMod == FieldItemsMod.RANDOM_EVEN));

        // 27 move randomizers
        // + static music
        out.write(makeByteSelected(randomizeMovePowers, randomizeMoveAccuracies, randomizeMovePPs, randomizeMoveTypes,
                randomizeMoveCategory, correctStaticMusic, randomizeMoveNames));

        // 28 evolutions 1
        out.write(makeByteSelected(evolutionsMod == EvolutionsMod.UNCHANGED, evolutionsMod == EvolutionsMod.RANDOM,
                evosSimilarStrength, evosSameTyping, evosMaxThreeStages, evosForceChange, evosAllowAltFormes,
                evolutionsMod == EvolutionsMod.RANDOM_EVERY_LEVEL));
        
        // 29 pokemon trainer misc
        out.write(makeByteSelected(trainersUsePokemonOfSimilarStrength, 
                rivalCarriesStarterThroughout,
                trainersMatchTypingDistribution,
                trainersBlockLegendaries,
                trainersBlockEarlyWonderGuard,
                swapTrainerMegaEvos,
                shinyChance,
                trainersAvoidDuplicates));

        // 30 - 33: pokemon restrictions
        int restrictionsVal = currentRestrictions == null ? -1 : currentRestrictions.toInt();
        writeFullInt(out, restrictionsVal);

        // 34 - 37: misc tweaks
        // TODO: make misc tweaks little endian. No one likes big endian.
        writeFullIntBigEndian(out, currentMiscTweaks);

        // 38 trainer pokemon level modifier
        out.write(trainersLevelModifier - 28); // Shift to int8 range: [-100, 155] --> [-128, 127]

        // 39 shop items 1
        out.write(makeByteSelected(shopItemsMod == ShopItemsMod.RANDOM, shopItemsMod == ShopItemsMod.SHUFFLE,
                shopItemsMod == ShopItemsMod.UNCHANGED, banBadRandomShopItems, banRegularShopItems, banOPShopItems,
                false, guaranteeEvolutionItems));

        // 40 wild level modifier
        out.write(wildLevelModifier - 28); // Shift to int8 range: [-100, 155] --> [-128, 127]

        // 41 EXP curve mod, block broken moves, alt forme stuff
        out.write(makeByteSelected(
                expCurveMod == ExpCurveMod.LEGENDARIES,
                expCurveMod == ExpCurveMod.STRONG_LEGENDARIES,
                expCurveMod == ExpCurveMod.ALL,
                blockBrokenMovesetMoves,
                blockBrokenTMMoves,
                blockBrokenTutorMoves,
                allowTrainerAlternateFormes,
                allowWildAltFormes));

        // 42 (Legacy Double Battle Mode), Additional Boss/Important Trainer Pokemon, Weigh Duplicate Abilities
        out.write((0) |
                (additionalBossTrainerPokemon << 1) |
                (additionalImportantTrainerPokemon << 4) |
                (weighDuplicateAbilitiesTogether ? 0x80 : 0));

        // 43 Additional Regular Trainer Pokemon, Aura modification, evolution moves, guarantee X items
        out.write(additionalRegularTrainerPokemon |
                ((auraMod == AuraMod.UNCHANGED) ? 0x8 : 0) |
                ((auraMod == AuraMod.RANDOM) ? 0x10 : 0) |
                ((auraMod == AuraMod.SAME_STRENGTH) ? 0x20 : 0) |
                (evolutionMovesForAll ? 0x40 : 0) |
                (guaranteeXItems ? 0x80 : 0));

        // 44 Totem Pokemon settings
        out.write(makeByteSelected(
                totemPokemonMod == TotemPokemonMod.UNCHANGED,
                totemPokemonMod == TotemPokemonMod.RANDOM,
                totemPokemonMod == TotemPokemonMod.SIMILAR_STRENGTH,
                allyPokemonMod == AllyPokemonMod.UNCHANGED,
                allyPokemonMod == AllyPokemonMod.RANDOM,
                allyPokemonMod == AllyPokemonMod.SIMILAR_STRENGTH,
                randomizeTotemHeldItems,
                allowTotemAltFormes));

        // 45 Totem level modifier
        out.write(totemLevelModifier - 28); // Shift to int8 range: [-100, 155] --> [-128, 127]

        // 46 - 47: These two get a byte each for future proofing
        out.write(updateBaseStatsToGeneration);
        out.write(updateMovesToGeneration);

        // 48 Selected EXP curve
        out.write(selectedEXPCurve.toByte());

        // 49 Static level modifier
        out.write(staticLevelModifier - 28); // Shift to int8 range: [-100, 155] --> [-128, 127]

        // 50 trainer pokemon held items / pokemon ensure two abilities / trainers use local pokemon
        out.write(makeByteSelected(randomizeHeldItemsForBossTrainerPokemon,
                randomizeHeldItemsForImportantTrainerPokemon,
                randomizeHeldItemsForRegularTrainerPokemon,
                consumableItemsOnlyForTrainerPokemon,
                sensibleItemsOnlyForTrainerPokemon,
                highestLevelOnlyGetsItemsForTrainerPokemon,
                ensureTwoAbilities,
                trainersUseLocalPokemon));

        // 51 pickup item randomization
        out.write(makeByteSelected(pickupItemsMod == PickupItemsMod.RANDOM,
                pickupItemsMod == PickupItemsMod.UNCHANGED, banBadRandomPickupItems,
                banIrregularAltFormes));

        // 52 elite four unique pokemon (3 bits) + catch rate level (3 bits)
        out.write(eliteFourUniquePokemonNumber | ((minimumCatchRateLevel - 1) << 3));

        // 53 starter type mod / starter no legendaries / starter no dual type checkbox
        out.write(makeByteSelected(startersTypeMod == StartersTypeMod.NONE,
                startersTypeMod == StartersTypeMod.FIRE_WATER_GRASS, startersTypeMod == StartersTypeMod.TRIANGLE,
                startersTypeMod == StartersTypeMod.UNIQUE, startersTypeMod == StartersTypeMod.SINGLE_TYPE,
                false, startersNoLegendaries, startersNoDualTypes));

        // 54 starter single-type type choice (5 bits)
        if(startersSingleType != null) {
            out.write(startersSingleType.toInt() + 1);
        } else {
            out.write(0);
        }
        
        // 55 Pokémon palette randomization
        out.write(makeByteSelected(pokemonPalettesMod == PokemonPalettesMod.UNCHANGED,
                pokemonPalettesMod == PokemonPalettesMod.RANDOM,
                pokemonPalettesFollowTypes,
                pokemonPalettesFollowEvolutions,
                pokemonPalettesShinyFromNormal));

        // 56 Type effectiveness
        out.write(makeByteSelected(typeEffectivenessMod == TypeEffectivenessMod.UNCHANGED,
                typeEffectivenessMod == TypeEffectivenessMod.RANDOM,
                typeEffectivenessMod == TypeEffectivenessMod.RANDOM_BALANCED,
                typeEffectivenessMod == TypeEffectivenessMod.KEEP_IDENTITIES,
                typeEffectivenessMod == TypeEffectivenessMod.INVERSE,
                inverseTypesRandomImmunities, updateTypeEffectiveness));

        // 57 evolutions 2
        out.write(makeByteSelected(evosForceGrowth, evosNoConvergence, adjustEvolutionLevels,
                false, false, false, false, false));

        // 58-60 starter BST limits
        byte highEndByte = (byte)(((startersBSTMinimum >> 8) & 0x0F) + ((startersBSTMaximum >> 4) & 0xF0));
        out.write(highEndByte);
        out.write((byte) startersBSTMinimum);
        out.write((byte) startersBSTMaximum);

        // 61 trainer type diversity + better movesets
        out.write(makeByteSelected(diverseTypesForBossTrainers, diverseTypesForImportantTrainers,
                diverseTypesForRegularTrainers, betterBossTrainerMovesets, betterImportantTrainerMovesets,
                betterRegularTrainerMovesets, false, false));

        // 62 setting battle style: modification (3bits) + style (4bits)
        out.write(makeByteSelected(settingBattleStyle.getModification() == BattleStyle.Modification.UNCHANGED,
                settingBattleStyle.getModification() == BattleStyle.Modification.RANDOM,
                settingBattleStyle.getModification() == BattleStyle.Modification.SINGLE_STYLE) |
                (makeByteSelected(settingBattleStyle.getStyle() == BattleStyle.Style.SINGLE_BATTLE,
                        settingBattleStyle.getStyle() == BattleStyle.Style.DOUBLE_BATTLE,
                        settingBattleStyle.getStyle() == BattleStyle.Style.TRIPLE_BATTLE,
                        settingBattleStyle.getStyle() == BattleStyle.Style.ROTATION_BATTLE) << 3));

        // 63 trainer pokemon evolve, no premature evolutions, Other SpinSlider activation checkboxes
        out.write(makeByteSelected(trainersEvolveTheirPokemon, banPrematureEvos, trainersLevelModified,
                wildLevelsModified, totemLevelsModified, staticLevelModified));

        // 64 shop items 2
        out.write(makeByteSelected(balanceShopPrices, addCheapRareCandiesToShops,
                false, false, false, false, false, false));

        // 65 general options #2
        out.write(makeByteSelected(randomizeIntroMon, raceMode, false, limitPokemon,
                false, false, false, false));

        // 66 'Make evolutions easier' level select slider
        out.write(makeEvolutionsEasierLvl);

        // 67 base stat totals
        out.write(makeByteSelected(bstMod == BSTMod.UNCHANGED,
                bstMod == BSTMod.RANDOM_BUFF_NERF,
                bstMod == BSTMod.SHUFFLE,
                bstMod == BSTMod.RANDOM,
                bstFollowEvolutions, bstShuffleSwapLegendaries,
                false, false));

        // 68 base stat total, random buff/nerf max percentage
        out.write(bstBuffNerfMaxPercentage);

        byte[] romName = this.romName.getBytes(StandardCharsets.US_ASCII);
        out.write(romName.length);
        out.write(romName, 0, romName.length);

        byte[] current = out.toByteArray();
        CRC32 checksum = new CRC32();
        checksum.update(current);
        writeFullIntBigEndian(out, (int) checksum.getValue());

        writeFullInt(out, 0); // padding

         */

        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    // TODO: remove once we've tested SettingsStringConverter
    @Deprecated
    private static SettingsManager fromStringWithoutVersion(String settingsString) throws IllegalArgumentException {
        return new SettingsManager();
        /*
        byte[] data = Base64.getDecoder().decode(settingsString);
        if (hasInvalidChecksum(data)) {
            throw new IllegalArgumentException("Malformed input string");
        }

        SettingsManager settings = new SettingsManager();

        // Restore the actual controls
        settings.setChangeImpossibleEvolutions(restoreState(data[0], 0));
        settings.setUpdateMoves(restoreState(data[0], 1));
        settings.setUpdateMovesLegacy(restoreState(data[0], 2));
        settings.setRandomizeTrainerNames(restoreState(data[0], 3));
        settings.setRandomizeTrainerClassNames(restoreState(data[0], 4));
        settings.setMakeEvolutionsEasier(restoreState(data[0], 5));
        settings.setRemoveTimeBasedEvolutions(restoreState(data[0], 6));
        settings.setEstimateLevelForEvolutionImprovements(restoreState(data[0], 7));

        settings.setBaseStatisticsMod(restoreEnum(BaseStatisticsMod.class, data[1], 3, // UNCHANGED
                2, // SHUFFLE
                1 // RANDOM
        ));
        settings.setStandardizeEXPCurves(restoreState(data[1], 4));
        settings.setBaseStatsFollowEvolutions(restoreState(data[1], 0));
        settings.setUpdateBaseStats(restoreState(data[1], 5));
        settings.setBaseStatsFollowMegaEvolutions(restoreState(data[1],6));
        settings.setAssignEvoStatsRandomly(restoreState(data[1],7));

        settings.setSpeciesTypesMod(restoreEnum(SpeciesTypesMod.class, data[2], 2, // UNCHANGED
                0, // RANDOM_FOLLOW_EVOLUTIONS
                1 // COMPLETELY_RANDOM
        ));
        settings.setTypesFollowMegaEvolutions(restoreState(data[2],6));
        settings.setDualTypeOnly(restoreState(data[2], 7));

        settings.setAbilitiesMod(restoreEnum(AbilitiesMod.class, data[3], 0, // UNCHANGED
                1 // RANDOMIZE
        ));
        settings.setAllowWonderGuard(restoreState(data[3], 2));
        settings.setAbilitiesFollowEvolutions(restoreState(data[3], 3));
        settings.setBanTrappingAbilities(restoreState(data[3], 4));
        settings.setBanNegativeAbilities(restoreState(data[3], 5));
        settings.setBanBadAbilities(restoreState(data[3], 6));
        settings.setAbilitiesFollowMegaEvolutions(restoreState(data[3],7));

        settings.setStartersMod(restoreEnum(StartersMod.class, data[4], 2, // UNCHANGED
                0, // CUSTOM
                1, // COMPLETELY_RANDOM
                3, // RANDOM_WITH_TWO_EVOLUTIONS
                7  // RANDOM_BASIC
        ));
        settings.setRandomizeStartersHeldItems(restoreState(data[4], 4));
        settings.setBanBadRandomStarterHeldItems(restoreState(data[4], 5));
        settings.setAllowStarterAltFormes(restoreState(data[4],6));

        settings.setCustomStarters(new int[]{IOFunctions.read2ByteInt(data, 5),
                IOFunctions.read2ByteInt(data, 7), IOFunctions.read2ByteInt(data, 9)});

        settings.setMovesetsMod(restoreEnum(MovesetsMod.class, data[11], 2, // UNCHANGED
                1, // RANDOM_PREFER_SAME_TYPE
                0, // COMPLETELY_RANDOM
                3 // METRONOME_ONLY
        ));
        settings.setStartWithGuaranteedMoves(restoreState(data[11], 4));
        settings.setReorderDamagingMoves(restoreState(data[11], 5));
        settings.setGuaranteedMoveCount(((data[11] & 0xC0) >> 6) + 2);

        settings.setMovesetsForceGoodDamaging(restoreState(data[12], 7));
        settings.setMovesetsGoodDamagingPercent(data[12] & 0x7F);

        // changed 160
        settings.setTrainersMod(restoreEnum(TrainersMod.class, data[13], 0, // UNCHANGED
                1, // RANDOM
                2, // DISTRIBUTED
                3, // MAINPLAYTHROUGH 
                4, // TYPE_THEMED
                5, // TYPE_THEMED_ELITE4_GYMS
                6, // KEEP_THEMED
                7  // KEEP_THEME_OR_PRIMARY
        ));

        settings.setTrainersEvolutionLevelModifier(data[14] + 28);  // Shift from int8 range: [-128, 127] --> [-100, 155]

        settings.setRandomizeWildPokemon(!restoreState(data[15], 0));

        settings.setWildPokemonZoneMod(restoreEnum(WildPokemonZoneMod.class, data[15], 1, // RANDOM
                2, // AREA_MAPPING
                6, // MAP_MAPPING
                5, // LOCATION_MAPPING
                3 // GLOBAL_MAPPING
        ));

        settings.setKeepWildEvolutionFamilies(restoreState(data[15], 4));
        settings.setSplitWildZoneByEncounterTypes(restoreState(data[15], 7));

        settings.setSimilarStrengthEncounters(restoreState(data[16], 1));
        settings.setCatchEmAllEncounters(restoreState(data[16], 2));
        settings.setWildPokemonTypeMod(restoreEnum(WildPokemonTypeMod.class, data[17], 0, // NONE
                2, // THEMED_AREAS
                1 // KEEP_PRIMARY
        ));
        settings.setKeepWildTypeThemes(restoreState(data[17], 3));
        settings.setWildPokemonEvolutionMod(restoreEnum(WildPokemonEvolutionMod.class, data[17],
                4, //NONE
                5, //BASIC_ONLY
                6 //KEEP_STAGE
        ));
        
        settings.setUseTimeBasedEncounters(restoreState(data[18], 0));
        settings.setUseMinimumCatchRate(restoreState(data[18], 1));
        settings.setBlockWildLegendaries(restoreState(data[18], 2));
        settings.setRandomizeWildPokemonHeldItems(restoreState(data[18], 3));
        settings.setBanBadRandomWildPokemonHeldItems(restoreState(data[18], 4));
        settings.setBalanceShakingGrass(restoreState(data[18], 5));

        settings.setStaticPokemonMod(restoreEnum(StaticPokemonMod.class, data[19], 0, // UNCHANGED
                1, // RANDOM_MATCHING
                2, // COMPLETELY_RANDOM
                3  // SIMILAR_STRENGTH 
        ));
        
        settings.setLimitMainGameLegendaries(restoreState(data[19], 4));
        settings.setLimit600(restoreState(data[19], 5));
        settings.setAllowStaticAltFormes(restoreState(data[19], 6));
        settings.setSwapStaticMegaEvos(restoreState(data[19], 7));
        
        settings.setTmsMod(restoreEnum(TMsMod.class, data[20], 4, // UNCHANGED
                3 // RANDOM
        ));
        settings.setTmsHmsCompatibilityMod(restoreEnum(TMsHMsCompatibilityMod.class, data[20], 2, // UNCHANGED
                1, // RANDOM_PREFER_TYPE
                0, // COMPLETELY_RANDOM
                7 // FULL
        )); 
        settings.setTmLevelUpMoveSanity(restoreState(data[20], 5));
        settings.setKeepFieldMoveTMs(restoreState(data[20], 6));

        settings.setFullHMCompat(restoreState(data[21], 0));
        settings.setTmsFollowEvolutions(restoreState(data[21], 1));
        settings.setTutorFollowEvolutions(restoreState(data[21], 2));

        settings.setTmsForceGoodDamaging(restoreState(data[22], 7));
        settings.setTmsGoodDamagingPercent(data[22] & 0x7F);

        settings.setMoveTutorMovesMod(restoreEnum(MoveTutorMovesMod.class, data[23], 4, // UNCHANGED
                3 // RANDOM
        ));
        settings.setMoveTutorsCompatibilityMod(restoreEnum(MoveTutorsCompatibilityMod.class, data[23], 2, // UNCHANGED
                1, // RANDOM_PREFER_TYPE
                0, // COMPLETELY_RANDOM
                7 // FULL
        ));
        settings.setTutorLevelUpMoveSanity(restoreState(data[23], 5));
        settings.setKeepFieldMoveTutors(restoreState(data[23], 6));

        settings.setTutorsForceGoodDamaging(restoreState(data[24], 7));
        settings.setTutorsGoodDamagingPercent(data[24] & 0x7F);

        // new 150
        settings.setInGameTradesMod(restoreEnum(InGameTradesMod.class, data[25], 6, // UNCHANGED
                1, // RANDOMIZE_GIVEN
                0 // RANDOMIZE_GIVEN_AND_REQUESTED
        ));
        settings.setRandomizeInGameTradesItems(restoreState(data[25], 2));
        settings.setRandomizeInGameTradesIVs(restoreState(data[25], 3));
        settings.setRandomizeInGameTradesNicknames(restoreState(data[25], 4));
        settings.setRandomizeInGameTradesOTs(restoreState(data[25], 5));

        settings.setFieldItemsMod(restoreEnum(FieldItemsMod.class, data[26],
                2,  // UNCHANGED
                1,  // SHUFFLE
                0,  // RANDOM
                4   // RANDOM_EVEN
        ));
        settings.setBanBadRandomFieldItems(restoreState(data[26], 3));

        // new 170
        settings.setRandomizeMovePowers(restoreState(data[27], 0));
        settings.setRandomizeMoveAccuracies(restoreState(data[27], 1));
        settings.setRandomizeMovePPs(restoreState(data[27], 2));
        settings.setRandomizeMoveTypes(restoreState(data[27], 3));
        settings.setRandomizeMoveCategory(restoreState(data[27], 4));
        settings.setCorrectStaticMusic(restoreState(data[27], 5));
        settings.setRandomizeMoveNames(restoreState(data[27], 6));

        settings.setEvolutionsMod(restoreEnum(EvolutionsMod.class, data[28], 0, // UNCHANGED
                1, // RANDOM
                7 // RANDOM_EVERY_LEVEL
        ));
        settings.setEvosSimilarStrength(restoreState(data[28], 2));
        settings.setEvosSameTyping(restoreState(data[28], 3));
        settings.setEvosMaxThreeStages(restoreState(data[28], 4));
        settings.setEvosForceChange(restoreState(data[28], 5));
        settings.setEvosAllowAltFormes(restoreState(data[28],6));

        // new pokemon trainer misc
        settings.setTrainersUsePokemonOfSimilarStrength(restoreState(data[29], 0));
        settings.setRivalCarriesStarterThroughout(restoreState(data[29], 1));
        settings.setTrainersMatchTypingDistribution(restoreState(data[29], 2));
        settings.setTrainersBlockLegendaries(restoreState(data[29], 3));
        settings.setTrainersBlockEarlyWonderGuard(restoreState(data[29], 4));
        settings.setSwapTrainerMegaEvos(restoreState(data[29], 5));
        settings.setShinyChance(restoreState(data[29], 6));
        settings.setTrainersAvoidDuplicates(restoreState(data[29], 7));

        // gen restrictions
        int genLimit = IOFunctions.readFullInt(data, 30);
        GenRestrictions restrictions = new GenRestrictions(genLimit);
        settings.setCurrentRestrictions(restrictions);

        int codeTweaks = IOFunctions.readFullIntBigEndian(data, 34);

        settings.setCurrentMiscTweaks(codeTweaks);

        settings.setTrainersLevelModifier(data[38] + 28); // Shift from int8 range: [-128, 127] --> [-100, 155]
        settings.setShopItemsMod(restoreEnum(ShopItemsMod.class,data[39],
                2,
                1,
                0));
        settings.setBanBadRandomShopItems(restoreState(data[39],3));
        settings.setBanRegularShopItems(restoreState(data[39],4));
        settings.setBanOPShopItems(restoreState(data[39],5));
        settings.setGuaranteeEvolutionItems(restoreState(data[39],7));

        settings.setWildLevelModifier(data[40] + 28); // Shift from int8 range: [-128, 127] --> [-100, 155]

        settings.setExpCurveMod(restoreEnum(ExpCurveMod.class,data[41],0,1,2));

        settings.setBlockBrokenMovesetMoves(restoreState(data[41],3));
        settings.setBlockBrokenTMMoves(restoreState(data[41],4));
        settings.setBlockBrokenTutorMoves(restoreState(data[41],5));

        settings.setAllowTrainerAlternateFormes(restoreState(data[41],6));
        settings.setAllowWildAltFormes(restoreState(data[41],7));

        // restoreState(data[42], 0))  Legacy setting. This bit used to be used for "Double Battle Only Mode"
        settings.setAdditionalBossTrainerPokemon((data[42] & 0xE) >> 1);
        settings.setAdditionalImportantTrainerPokemon((data[42] & 0x70) >> 4);
        settings.setWeighDuplicateAbilitiesTogether(restoreState(data[42], 7));

        settings.setAdditionalRegularTrainerPokemon((data[43] & 0x7));
        settings.setAuraMod(restoreEnum(AuraMod.class,data[43],3,4,5));
        settings.setEvolutionMovesForAll(restoreState(data[43],6));
        settings.setGuaranteeXItems(restoreState(data[43],7));

        settings.setTotemPokemonMod(restoreEnum(TotemPokemonMod.class,data[44],0,1,2));
        settings.setAllyPokemonMod(restoreEnum(AllyPokemonMod.class,data[44],3,4,5));
        settings.setRandomizeTotemHeldItems(restoreState(data[44],6));
        settings.setAllowTotemAltFormes(restoreState(data[44],7));
        settings.setTotemLevelModifier(data[45] + 28); // Shift from int8 range: [-128, 127] --> [-100, 155]

        settings.setUpdateBaseStatsToGeneration(data[46]);

        settings.setUpdateMovesToGeneration(data[47]);

        settings.setSelectedEXPCurve(ExpCurve.fromByte(data[48]));

        settings.setStaticLevelModifier(data[49] + 28); // Shift from int8 range: [-128, 127] --> [-100, 155]

        settings.setRandomizeHeldItemsForBossTrainerPokemon(restoreState(data[50], 0));
        settings.setRandomizeHeldItemsForImportantTrainerPokemon(restoreState(data[50], 1));
        settings.setRandomizeHeldItemsForRegularTrainerPokemon(restoreState(data[50], 2));
        settings.setConsumableItemsOnlyForTrainers(restoreState(data[50], 3));
        settings.setSensibleItemsOnlyForTrainers(restoreState(data[50], 4));
        settings.setHighestLevelGetsItemsForTrainers(restoreState(data[50], 5));
        settings.setEnsureTwoAbilities(restoreState(data[50], 6));
        settings.setTrainersUseLocalPokemon(restoreState(data[50], 7));

        settings.setPickupItemsMod(restoreEnum(PickupItemsMod.class, data[51],
                1, // UNCHANGED
                0));       // RANDOMIZE
        settings.setBanBadRandomPickupItems(restoreState(data[51], 2));
        settings.setBanIrregularAltFormes(restoreState(data[51], 3));

        settings.setEliteFourUniquePokemonNumber(data[52] & 0x7);
        settings.setMinimumCatchRateLevel(((data[52] & 0x38) >> 3) + 1);

        settings.setStartersTypeMod(restoreEnum(StartersTypeMod.class, data[53], 0, //NONE
                1, //FIRE_WATER_GRASS
                2, //TRIANGLE
                3, //UNIQUE
                4  //SINGLE_TYPE
            ));

        settings.setStartersNoLegendaries(restoreState(data[53], 6));
        settings.setStartersNoDualTypes(restoreState(data[53], 7));

        if(data[54] == 0) {
            settings.setStartersSingleType(null);
        } else {
            settings.setStartersSingleType(Type.fromInt((data[54] & 0x1F) - 1));
        }

        settings.setPokemonPalettesMod(restoreEnum(PokemonPalettesMod.class, data[55], 0, // UNCHANGED
                1 // RANDOM
        ));
        settings.setPokemonPalettesFollowTypes(restoreState(data[55], 2));
        settings.setPokemonPalettesFollowEvolutions(restoreState(data[55], 3));
        settings.setPokemonPalettesShinyFromNormal(restoreState(data[55], 4));

        settings.setTypeEffectivenessMod(restoreEnum(TypeEffectivenessMod.class, data[56], 0, // UNCHANGED
                1, // RANDOM
                2, // RANDOM_BALANCED
                3, // KEEP_IDENTITIES
                4  // REVERSE
        ));
        settings.setInverseTypesRandomImmunities(restoreState(data[56], 5));
        settings.setUpdateTypeEffectiveness(restoreState(data[56], 6));

        settings.setEvosForceGrowth(restoreState(data[57], 0));
        settings.setEvosNoConvergence(restoreState(data[57], 1));
        settings.setAdjustEvolutionLevels(restoreState(data[57], 2));

        settings.setStartersBSTMinimum(((Byte.toUnsignedInt(data[58]) & 0x0F) << 8) + Byte.toUnsignedInt(data[59]));
        settings.setStartersBSTMaximum(((Byte.toUnsignedInt(data[58]) & 0xF0) << 4) + Byte.toUnsignedInt(data[60]));

        settings.setDiverseTypesForBossTrainers(restoreState(data[61], 0));
        settings.setDiverseTypesForImportantTrainers(restoreState(data[61], 1));
        settings.setDiverseTypesForRegularTrainers(restoreState(data[61], 2));
        settings.setBetterBossTrainerMovesets(restoreState(data[61], 3));
        settings.setBetterImportantTrainerMovesets(restoreState(data[61], 4));
        settings.setBetterRegularTrainerMovesets(restoreState(data[61], 5));

        settings.settingBattleStyle.setModification(restoreEnum(BattleStyle.Modification.class, data[62], 0, 1, 2));
        settings.settingBattleStyle.setStyle(restoreEnum(BattleStyle.Style.class, data[62], 3, 4, 5, 6));

        settings.setTrainersEvolveTheirPokemon(restoreState(data[63], 0));
        settings.setBanPrematureEvos(restoreState(data[63], 1));
        settings.setTrainersLevelModified(restoreState(data[63], 2));
        settings.setWildLevelsModified(restoreState(data[63], 3));
        settings.setTotemLevelsModified(restoreState(data[63],4));
        settings.setStaticLevelModified(restoreState(data[63], 5));

        settings.setBalanceShopPrices(restoreState(data[64],0));
        settings.setAddCheapRareCandiesToShops(restoreState(data[64], 1));

        settings.setRandomizeIntroMon(restoreState(data[65], 0));
        settings.setRaceMode(restoreState(data[65], 1));

        settings.setLimitPokemon(restoreState(data[65], 3));
        settings.setMakeEvolutionsEasierLvl(data[66] & 0x7F);

        settings.setBSTMod(restoreEnum(BSTMod.class, data[67], 0, 1, 2, 3));
        settings.setBSTFollowEvolutions(restoreState(data[67], 4));
        settings.setBSTShuffleSwapLegendaries(restoreState(data[67], 5));

        settings.setBSTBuffNerfMaxPercentage(data[68]); // small enough values that int8 range [-128, 127] is ok

        int romNameLength = data[LENGTH_OF_SETTINGS_DATA] & 0xFF;
        String romName = new String(data, LENGTH_OF_SETTINGS_DATA + 1, romNameLength, StandardCharsets.US_ASCII);
        settings.setRomName(romName);

        return settings;
        */
    }

    // getters and setters

    public String getRomName() {
        return romName;
    }

    public void setRomName(String romName) {
        this.romName = romName;
    }

    public boolean isUpdatedFromOldVersion() {
        return updatedFromOldVersion;
    }

    private void setUpdatedFromOldVersion(boolean updatedFromOldVersion) {
        this.updatedFromOldVersion = updatedFromOldVersion;
    }

}
