package com.uprfvx.random.gui.SettingElementCoordinators;


import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;

public class BooleanCheckBoxCoordinator extends SettingSingleElementCoordinator<Boolean, JCheckBox> {

    public BooleanCheckBoxCoordinator(Settings.Name settingName, SettingsManager settings, JCheckBox checkBox) {
        super(settingName, settings, checkBox);

        checkBox.addActionListener(this::elementValueChanged);
    }

    @Override
    protected void displayValue(Boolean newValue) {
        element.setSelected(newValue);
    }

    @Override
    protected Boolean getElementValue() {
        return element.isSelected();
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {
        //This should never happen
    }
}
