package com.uprfvx.random.gui.SettingElementCoordinators;


import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;

public class SettingCheckBoxCoordinator extends SettingUICoordinator<Boolean> {

    private JCheckBox checkBox;

    public SettingCheckBoxCoordinator(String settingName, SettingsManager settings, JCheckBox checkBox) {
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
    public void onPossibleEnablementChange(String setting, SettingsManager manager) {
        checkBox.setEnabled(manager.getSetting(setting));
    }

    @Override
    public void onSupportChange(String setting, SettingsManager manager, boolean isSupported) {
        checkBox.setVisible(isSupported);
    }

    @Override
    public void onPossibleSupportedValuesChange(String setting, SettingsManager manager, RomHandler game) {
        //This should never happen
    }
}
