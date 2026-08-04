package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandler;

/**
 * Interface for any listener to changes of settings.
 */
public interface SettingChangeListener {
    /**
     * Alerts the listener that the setting was automatically reset to its default value.
     *
     * @param setting The setting that had its value reset.
     * @param manager The settings manager handling the setting.
     */
    void onAutomaticSettingChange(String setting, SettingsManager manager);


    /**
     * Alerts the listener that the enabled state or values of this setting might have changed.
     *
     * @param setting The setting that may have changed.
     * @param manager The settings manager handling the setting.
     */
    void onPossibleEnablementChange(String setting, SettingsManager manager);

    /**
     * Alerts the listener to a manual change to the setting's value.
     *
     * @param setting The setting that had its value changed.
     * @param manager The settings manager handling the setting.
     */
    void onManualSettingChange(String setting, SettingsManager manager);

    /**
     * Alerts the listener that the support state of this setting has changed.
     *
     * @param setting     The setting that has been changed.
     * @param manager     The settings manager handling the setting.
     * @param isSupported Whether the setting is now supported.
     */
    void onSupportChange(String setting, SettingsManager manager, boolean isSupported);

    /**
     * Alerts the listener that the supported values of this setting may have changed.
     *
     * @param setting The setting that may have been changed.
     * @param manager The settings manager handling the setting.
     * @param game    The RomHandler of the game that was loaded, or null if a game was unloaded.
     */
    void onPossibleSupportedValuesChange(String setting, SettingsManager manager, RomHandler game);
}
