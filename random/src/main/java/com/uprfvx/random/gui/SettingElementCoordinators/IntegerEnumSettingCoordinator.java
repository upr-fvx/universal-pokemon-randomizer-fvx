package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class IntegerEnumSettingCoordinator extends SettingCoordinator<Integer, IntegerEnumComboBoxManager> {

    public IntegerEnumSettingCoordinator(Settings.Name settingName, SettingsManager manager,
                                         IntegerEnumComboBoxManager element) {
        super(settingName, manager, element);
    }

    public IntegerEnumSettingCoordinator(Settings.Name settingName, SettingsManager manager,
                                         IntegerEnumComboBoxManager element, JCheckBox latch) {
        super(settingName, manager, element, latch);
    }

    @Override
    public void onPossibleEnablementChange(Settings.Name setting, SettingsManager manager) {
        super.onPossibleEnablementChange(setting, manager);
        settingMatchCheck(setting);

        if(!manager.isEnabled(settingName)) {
            return;
        }

        Map<Integer, Boolean> enablement = new HashMap<>();
        for(Integer value : element.getValues()) {
            enablement.put(value, manager.isValueEnabled(settingName, value));
        }
        element.setEnabled(enablement);
    }

    @Override
    public void onSupportChange(Settings.Name setting, SettingsManager manager, boolean isSupported) {
        settingMatchCheck(setting);

        if(!isSupported) {
            element.setVisible(false);
            return;
        }
        element.setVisible(true);

        Map<Integer, Boolean> support = new HashMap<>();
        for(Integer value : element.getValues()) {
            support.put(value, manager.isValueSupported(settingName, value));
        }
        element.setVisible(support);
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {
        onSupportChange(setting, manager, manager.isSupported(setting));
    }
}
