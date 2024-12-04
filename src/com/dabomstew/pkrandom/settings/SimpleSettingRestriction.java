package com.dabomstew.pkrandom.settings;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * A simple setting restriction which tests the state of a single setting.
 * @param <T> The type of the setting to test.
 */
public class SimpleSettingRestriction<T> implements SettingRestriction {
    final String name;
    final Predicate<T> desiredState;

    /**
     * Creates a new SimpleSettingRestriction, which tests the state of a single setting.
     * @param name The setting to test.
     * @param desiredState A Predicate which returns true for the desired state.
     */
    public SimpleSettingRestriction(String name, Predicate<T> desiredState) {
        this.name = name;
        this.desiredState = desiredState;
    }

    @Override
    public List<String> getRelevantSettingNames() {
        return Collections.singletonList(name);
    }

    @Override
    public boolean test(SettingsManager manager) {
        return desiredState.test(manager.getSetting(name));
    }
}
