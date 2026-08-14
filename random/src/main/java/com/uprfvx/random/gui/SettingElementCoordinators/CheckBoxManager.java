package com.uprfvx.random.gui.SettingElementCoordinators;


import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;
import javax.swing.event.ChangeListener;

public class CheckBoxManager extends SingleElementManager<Boolean, JCheckBox> {

    public CheckBoxManager(JCheckBox checkBox) {
        super(checkBox);
    }

    public void displayValue(Boolean newValue) {
        element.setSelected(newValue);
    }

    public Boolean getElementValue() {
        return element.isSelected();
    }

    public void addListener(ChangeListener listener) {
        element.addChangeListener(listener);
    }
}
