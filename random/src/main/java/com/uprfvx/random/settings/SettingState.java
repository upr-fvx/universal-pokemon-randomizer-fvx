package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;

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
        return definition.isEnabled(manager) && definition.isValueEnabled(value, manager);
    }

    /**
     * Returns this setting to its default value.
     */
    public void reset() {
        value = definition.getDefaultValue();
    }
}
