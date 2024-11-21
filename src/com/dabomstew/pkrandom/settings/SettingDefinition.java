package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.function.Predicate;

public class SettingDefinition<T> {
    private final String name;
    private final String category;
    private final T defaultValue;
    private final SettingState<?>[] prerequisites;
    private final Predicate<RomHandler> supported;

    public SettingDefinition(String name, String category, T defaultValue, Predicate<RomHandler> supported, SettingState<?>... prerequisites) {
        this.name = name;
        this.category = category;
        this.defaultValue = defaultValue;
        this.prerequisites = prerequisites;
        this.supported = supported;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isEnabled(SettingsManager settings) {
        if(prerequisites == null) {
            return true;
        }
        for(SettingState<?> prereq : prerequisites)
        {
            /*
            if(!settings.isFulfilled(prerequisite)) {
                return false;
            }
            //*/
            //hmm. this works for "AND"s, but what if it's an "OR"?
            //I might end up needing to make it a Predicate<SettingsManager>.
            //Hope not, though...
        }
        return false;
    }

    public boolean isSupported(RomHandler game) {
        if(supported == null) {
            return true;
        }
        return supported.test(game);
    }

}
