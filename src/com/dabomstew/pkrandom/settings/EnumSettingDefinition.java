package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Map;
import java.util.function.Predicate;

public class EnumSettingDefinition<T extends Enum<T>> extends SettingDefinition<T> {

    Map<T, SettingRestriction> restrictions;
    Map<T, Predicate<RomHandler>> support;

    public EnumSettingDefinition(String name, String category, T defaultValue, SettingRestriction prerequisite,
                                 Predicate<RomHandler> supported, Map<T, SettingRestriction> restrictedStates,
                                 Map<T, Predicate<RomHandler>> supportedStates) {
        super(name, category, defaultValue, prerequisite, supported);
        this.restrictions = restrictedStates;
        support = supportedStates;
    }

    public boolean isValueEnabled(T value) {
        if(restrictions == null) {
            return true;
        }
        SettingRestriction restriction = restrictions.get(value);
        if(restriction == null) {
            return true;
        }
        return restriction.test(manager);
    }

    public boolean isValueSupported(T value, RomHandler game) {
        if(support == null) {
            return true;
        }
        Predicate<RomHandler> supportTest = support.get(value);
        if(supportTest == null) {
            return true;
        }
        return supportTest.test(game);
    }
}
