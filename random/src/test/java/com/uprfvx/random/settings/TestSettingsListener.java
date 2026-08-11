package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandler;

/**
 * A very simple testing tool for the SettingChangeListener interface.
 */
class TestSettingsListener implements SettingChangeListener {

    public boolean automaticSettingChangeCalled = false;
    public boolean possibleEnablementChangeCalled = false;
    public boolean manualSettingChangeCalled = false;
    public boolean supportChangeCalled = false;
    public boolean possibleSupportedValuesChangeCalled = false;

    public int manualChangeCallCount = 0;

    @Override
    public void onAutomaticSettingChange(Settings.Name setting, SettingsManager manager) {
        automaticSettingChangeCalled = true;
    }

    @Override
    public void onPossibleEnablementChange(Settings.Name setting, SettingsManager manager) {
        possibleEnablementChangeCalled = true;
    }

    @Override
    public void onManualSettingChange(Settings.Name setting, SettingsManager manager) {
        manualSettingChangeCalled = true;
        manualChangeCallCount++;
    }

    @Override
    public void onSupportChange(Settings.Name setting, SettingsManager manager, boolean isSupported) {
        supportChangeCalled = true;
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {
        possibleSupportedValuesChangeCalled = true;
    }

    public void reset()
    {
        automaticSettingChangeCalled = false;
        possibleEnablementChangeCalled = false;
        manualSettingChangeCalled = false;
        supportChangeCalled = false;
        possibleSupportedValuesChangeCalled = false;
        manualChangeCallCount = 0;
    }
}
