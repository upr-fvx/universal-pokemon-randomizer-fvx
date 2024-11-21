package com.dabomstew.pkrandom.settings;

import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.function.Predicate;

public class SettingDefinition<T> {
    private final String name;
    private final String category;
    private final T defaultValue;
    private final SettingState prerequisite;
    private final Predicate<RomHandler> supported;

    public SettingDefinition(String name, String category, T defaultValue, SettingState prerequisite, Predicate<RomHandler> supported) {
        this.name = name;
        this.category = category;
        this.defaultValue = defaultValue;
        this.prerequisite = prerequisite;
        this.supported = supported;
    }

    public static class SettingState {
        final String name;
        final Predicate<Setting<?>> state;

        public SettingState(String name, Predicate<Setting<?>> state) {
            this.name = name;
            this.state = state;
        }
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
        return settings.isFulfilled(prerequisite);
    }

    public boolean isSupported(RomHandler game) {
        return supported.test(game);
    }

}
