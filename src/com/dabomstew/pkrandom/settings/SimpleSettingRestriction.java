package com.dabomstew.pkrandom.settings;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class SimpleSettingRestriction<T> implements SettingRestriction {
    final String name;
    final Predicate<T> desiredState;

    public SimpleSettingRestriction(String name, Predicate<T> desiredState) {
        this.name = name;
        this.desiredState = desiredState;
    }

    @Override
    public List<String> settingNames() {
        return Collections.singletonList(name);
    }

    //Well, this is ugly. But it works, I guess.
    @Override
    public boolean test(SettingsManager manager) {
        return desiredState.test(manager.getSetting(name));
    }
}
