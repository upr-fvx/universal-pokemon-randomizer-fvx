package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;
import com.uprfvx.random.settings.settingstring.SettingsStringConverter;
import com.uprfvx.random.settings.settingstring.SettingsStringUpdater;

/**
 * Updates older settings to be compatible with the current version
 * of the Randomizer.
 * <br><br>
 * To update from the "settings string" format used up to FVX v1.6.0,
 * use {@link SettingsStringUpdater} and {@link SettingsStringConverter}.
 */
public class SettingsUpdater {

    /**
     * Takes a {@link SettingsManager} which has loaded settings.
     * If the settings are old and there are any {@link SettingDefinition}s that
     * have since changed*, this method will update the settings accordingly.<br>
     * *E.g. been split, removed, renamed, or had their type changed.
     */
    public void update(SettingsManager sm) {
        // No SettingDefinitions have changed yet, so this method is appropriately empty.
        // TODO: examples?
    }

}
