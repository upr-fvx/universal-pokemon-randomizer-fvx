package com.uprfvx.random.settings.restrictions;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class SettingSupportRestriction implements SettingRestriction{
    final Settings.Name name;
    final boolean isSupportDesired;

    /**
     * Creates a new SettingSupportRestriction, which checks whether a single setting is currently supported.
     * @param name The setting to test.
     * @param isSupportDesired Whether this should report true if the setting is supported (true),
     *                         or if the setting is unsupported (false).
     */
    public SettingSupportRestriction(Settings.Name name, boolean isSupportDesired) {
        this.name = name;
        this.isSupportDesired = isSupportDesired;
    }

    @Override
    public List<Settings.Name> getRelevantSettingNames() {
        return Collections.singletonList(name);
    }

    @Override
    public boolean test(SettingsManager manager) {
        return (manager.isSupported(name) == isSupportDesired);
    }
}
