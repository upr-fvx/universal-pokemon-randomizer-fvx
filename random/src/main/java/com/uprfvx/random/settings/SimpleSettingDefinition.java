package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.Serializable;
import java.util.function.Predicate;

public class SimpleSettingDefinition<T extends Serializable> extends SettingDefinition<T>  {

    //A simple SettingDefinition that makes no restrictions on values.

    public SimpleSettingDefinition(String name, String category, T defaultValue, SettingRestriction prerequisite,
                                   Predicate<RomHandler> supported) {
        super(name, category, defaultValue, prerequisite, supported, null, false);
    }

    @Override
    public boolean isValueEnabled(T value, SettingsManager manager) {
        return true;
    }

    @Override
    public boolean isValueSupported(T value, RomHandler game) {
        return true;
    }
}
