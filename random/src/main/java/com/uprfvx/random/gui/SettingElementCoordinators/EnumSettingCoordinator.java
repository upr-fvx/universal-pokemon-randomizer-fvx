package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class EnumSettingCoordinator<E extends Enum<E>> extends SettingCoordinator<E, EnumeratedUIManager<E>> {
    public EnumSettingCoordinator(Settings.Name settingName, SettingsManager manager, EnumeratedUIManager<E> element) {
        super(settingName, manager, element);
    }

    public EnumSettingCoordinator(Settings.Name settingName, SettingsManager manager, EnumeratedUIManager<E> element,
                                  JCheckBox latch) {
        super(settingName, manager, element, latch);
    }

    @Override
    public void onPossibleEnablementChange(Settings.Name setting, SettingsManager manager) {
        super.onPossibleEnablementChange(setting, manager);
        settingMatchCheck(setting);

        if(!manager.isEnabled(settingName)) {
            return;
        }

        Map<E, Boolean> enablement = new HashMap<>();
        for(E value : element.getValues()) {
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

        Map<E, Boolean> support = new HashMap<>();
        for(E value : element.getValues()) {
            support.put(value, manager.isValueSupported(settingName, value));
        }
        element.setVisible(support);
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {
        onSupportChange(setting, manager, manager.isSupported(setting));
    }
}
