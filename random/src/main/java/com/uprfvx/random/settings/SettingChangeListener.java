package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandler;

/**
 * Interface for any listener to changes of settings.
 */
public interface SettingChangeListener {
    /**
     * Alerts the listener of a possible automatic change to the setting.
     * This could be a change to its enabled/disabled state, the enabled/disabled state of its possible values,
     * and/or a reset to its default value.
     * It could also be no change.
     *
     * @param setting The setting that might have changed.
     * @param manager The settings manager handling the setting.
     */
    public void onPossibleAutomaticSettingChange(String setting, SettingsManager manager);

    /**
     * Alerts the listener to a manual change to the setting's value.
     *
     * @param setting The setting that had its value changed.
     * @param manager The settings manager handling the setting.
     */
    public void onManualSettingChange(String setting, SettingsManager manager);

    /**
     * Alerts the listener that the support state of this setting has changed.
     *
     * @param setting     The setting that has been changed.
     * @param manager     The settings manager handling the setting.
     * @param isSupported Whether the setting is now supported.
     */
    public void onSupportChange(String setting, SettingsManager manager, boolean isSupported);

    /**
     * Alerts the listener that the supported values of this setting may have changed.
     *
     * @param setting The setting that may have been changed.
     * @param manager The settings manager handling the setting.
     * @param game    The RomHandler of the game that was loaded, or null if a game was unloaded.
     */
    public void onPossibleSupportedValuesChange(String setting, SettingsManager manager, RomHandler game);
}
