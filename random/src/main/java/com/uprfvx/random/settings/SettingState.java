package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.Serializable;

//Making this class package-private, just in case.
class SettingState<T extends Serializable> {
    private final SettingDefinition<T> definition;
    private T value;

    public SettingState(SettingDefinition<T> definition) {
        this.definition = definition;
        value = definition.getDefaultValue();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public SettingDefinition<T> getDefinition() {
        return definition;
    }

    /**
     * Checks that the current value of the SettingState is valid given the state of the SettingsManager.
     * @param manager The SettingsManager holding the current state of the settings.
     * @return True if the current value is valid, false otherwise.
     */
    public boolean currentValueIsEnabled(SettingsManager manager) {
        return (definition.isEnabled(manager) || this.value.equals(definition.getDefaultValue()))
                && definition.isValueEnabled(value, manager);
    }

    /**
     * Checks that the current value of the SettingState is supported in the given game.
     * @param game The RomHandler handling the game in question.
     * @return True if the current value is supported, false otherwise.
     */
    public boolean currentValueIsSupported(RomHandler game) {
        if(game == null)
            return true;

        return (definition.isSupported(game) || this.value.equals(definition.getDefaultValue()))
                && definition.isValueSupported(value, game);
    }

    /**
     * Checks if this setting is currently set to its default value.
     * @return True if the setting's value is equal to the default value, false otherwise.
     */
    public boolean isDefault() {
        return value.equals(definition.getDefaultValue());
    }

    /**
     * Returns this setting to its default value.
     */
    public void reset() {
        value = definition.getDefaultValue();
    }
}
