package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.function.Predicate;

public class SettingDefinition<T> {
    private final String name;
    private final String category;
    private final T defaultValue;
    private final SettingRestriction prerequisite;
    private final Predicate<RomHandler> supported;
    protected static SettingsManager manager;

    //Okay, this is not ideal; we should probably figure out a better way...
    //TODO: yeah
    public static void setSettingsManager(SettingsManager manager) {
        SettingDefinition.manager = manager;
    }

    public SettingDefinition(String name, String category, T defaultValue, SettingRestriction prerequisite,
                             Predicate<RomHandler> supported) {
        this.name = name;
        this.category = category;
        this.defaultValue = defaultValue;
        this.prerequisite = prerequisite;
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

    public boolean isEnabled() {
        if(prerequisite == null) {
            return true;
        }

        return prerequisite.test(manager);
    }

    public boolean isSupported(RomHandler game) {
        if(supported == null) {
            return true;
        }
        return supported.test(game);
    }

}
