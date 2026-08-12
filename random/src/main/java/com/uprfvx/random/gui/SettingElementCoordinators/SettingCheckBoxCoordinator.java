package com.uprfvx.random.gui.SettingElementCoordinators;


import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;

public class SettingCheckBoxCoordinator extends SettingUICoordinator<Boolean> {

    private JCheckBox checkBox;

    public SettingCheckBoxCoordinator(Settings.Name settingName, SettingsManager settings, JCheckBox checkBox) {
        super(settingName, settings);
        this.checkBox = checkBox;

        checkBox.addActionListener(_ -> elementValueChanged());
    }

    @Override
    protected void displayValue(Boolean newValue) {
        checkBox.setSelected(newValue);
    }

    @Override
    protected Boolean getElementValue() {
        return checkBox.isSelected();
    }

    @Override
    public void onPossibleEnablementChange(Settings.Name setting, SettingsManager manager) {
        checkBox.setEnabled(manager.getSetting(setting));
    }

    @Override
    public void onSupportChange(Settings.Name setting, SettingsManager manager, boolean isSupported) {
        checkBox.setVisible(isSupported);
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {
        //This should never happen
    }
}
