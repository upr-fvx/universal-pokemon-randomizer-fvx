package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Map;
import java.util.function.Predicate;

public class EnumSettingDefinition<T extends Enum<T>> extends SettingDefinition<T> {

    Map<T, SettingRestriction> restrictions;

    public EnumSettingDefinition(String name, String category, T defaultValue, SettingRestriction prerequisite,
                                 Predicate<RomHandler> supported, Map<T, SettingRestriction> restrictions) {
        super(name, category, defaultValue, prerequisite, supported);
        this.restrictions = restrictions;
    }

    public boolean isValueEnabled(T value) {
        return restrictions.get(value).test(manager);
    }
}
