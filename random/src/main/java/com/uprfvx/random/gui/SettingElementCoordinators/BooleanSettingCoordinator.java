package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;

public class BooleanSettingCoordinator<U extends UIManager<Boolean>> extends SettingCoordinator<Boolean, U> {

    public BooleanSettingCoordinator(Settings.Name settingName, SettingsManager manager, U element) {
        super(settingName, manager, element);
    }

    public BooleanSettingCoordinator(Settings.Name settingName, SettingsManager manager, U element, JCheckBox latch) {
        super(settingName, manager, element, latch);
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {

    }
}
