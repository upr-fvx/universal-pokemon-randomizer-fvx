package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.Serializable;
import java.util.function.Predicate;

public class SimpleSettingDefinition<V extends Serializable> extends SettingDefinition<V>  {

    public static class Builder<B extends Builder<B, V>, V extends Serializable>
            extends SettingDefinition.Builder<B, V> {
        public Builder(String name, String category, V defaultValue) {
            super(name, category, defaultValue);
        }

        @Override
        public SimpleSettingDefinition<V> build() {
            return new SimpleSettingDefinition<>(name, category, defaultValue, prerequisite, supported);
        }
    }

    //A simple SettingDefinition that makes no restrictions on values.

    public SimpleSettingDefinition(String name, String category, V defaultValue, SettingRestriction prerequisite,
                                   Predicate<RomHandler> supported) {
        super(name, category, defaultValue, prerequisite, supported, null, false);
    }

    @Override
    public boolean isValueValid(V value) {
        return value != null && value.getClass() == defaultValue.getClass();
    }

    @Override
    public boolean isValueEnabled(V value, SettingsManager manager) {
        return true;
    }

    @Override
    public boolean isValueSupported(V value, RomHandler game) {
        return true;
    }
}
