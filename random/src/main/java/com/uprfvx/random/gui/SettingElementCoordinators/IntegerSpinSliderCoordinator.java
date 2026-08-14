package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.gui.SpinSlider;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;

import javax.swing.*;

public class IntegerSpinSliderCoordinator extends NumericSingleElementCoordinator<Integer, SpinSlider> {

    public IntegerSpinSliderCoordinator(Settings.Name settingName, SettingsManager settings, SpinSlider element) {
        this(settingName, settings, element, null);
    }

    public IntegerSpinSliderCoordinator(Settings.Name settingName, SettingsManager settings, SpinSlider element, JCheckBox latch) {
        super(settingName, settings, element, latch);

        element.addChangeListener(_ -> elementValueChanged());
    }

    @Override
    protected void setExtents(Integer newMin, Integer newMax) {
        element.setExtents(newMin, newMax);
    }

    @Override
    protected void displayValue(Integer newValue) {
        element.setValue(newValue);
    }

    @Override
    protected Integer getElementValue() {
        return element.getValue();
    }
}
