package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;

import java.io.Serializable;

public class SettingState<T extends Serializable> {
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
     * If it is not, resets the state to the setting's default value.
     * @param manager The SettingsManager holding the current state of the settings.
     * @return True if the setting's state was changed, false otherwise.
     */
    public boolean checkValidity(SettingsManager manager) {
        if(value == definition.getDefaultValue())
            return false;

        if(!definition.isEnabled(manager) || !definition.isValueEnabled(value, manager)) {
            value = definition.getDefaultValue();
            return true;
        }
        return false;
    }
}
