package com.uprfvx.random.settings.definitions;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.restrictions.SettingRestriction;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

public class SimpleSettingDefinition<V extends Serializable> extends SettingDefinition<V>  {

    public static class Builder<B extends Builder<B, V>, V extends Serializable>
            extends SettingDefinition.Builder<B, V> {
        public Builder(Settings.Name name, Settings.Category category, V defaultValue) {
            super(name, category, defaultValue);
        }

        @Override
        public SimpleSettingDefinition<V> build() {
            return new SimpleSettingDefinition<>(
                    name, category,
                    defaultValue,
                    prerequisite, supported,
                    variableDefaultValue
            );
        }
    }

    public static class BooleanBuilder<B extends BooleanBuilder<B>>
            extends Builder<B, Boolean> {

        public BooleanBuilder(Settings.Name name, Settings.Category category) {
            super(name, category, false);
        }
    }

    //A simple SettingDefinition that makes no restrictions on values.

    protected SimpleSettingDefinition(Settings.Name name, Settings.Category category, V defaultValue,
                                   SettingRestriction prerequisite, Predicate<RomHandler> supported,
                                      Function<RomHandler, V> variableDefaultValue) {
        super(name, category, defaultValue, prerequisite, supported, variableDefaultValue,
                null, false);
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
