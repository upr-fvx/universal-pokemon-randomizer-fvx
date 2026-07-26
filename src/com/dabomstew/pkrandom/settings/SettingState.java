package com.dabomstew.pkrandom.settings;

import java.io.Serializable;

public class SettingState<T extends Serializable> {
    private final SettingDefinition<T> definition;
    private T value;

    public SettingState(SettingDefinition<T> definition) {
        this.definition = definition;
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
}
