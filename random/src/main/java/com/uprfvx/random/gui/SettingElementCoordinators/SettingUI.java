package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.SettingChangeListener;
import com.uprfvx.random.settings.SettingsManager;

import javax.swing.*;
import java.io.Serializable;

/**
 * A class which coordinates a single setting with one or multiple GUI elements.
 * @param <T> The type of data held by the setting.
 */
public abstract class SettingUI<T extends Serializable> implements SettingChangeListener {
    private T displayedValue;
    private String settingName;
    private SettingsManager settings;
    //I don't like that I have to keep the settingsManager here...
    //but I don't know how else I'd access it to set the setting.

    public SettingUI(String settingName, SettingsManager settings) {
        this.settingName = settingName;
        this.settings = settings;

        T initialValue = settings.getSetting(settingName);
        setValue(initialValue);

        settings.addListener(settingName, this);
    }

    public void setValue(T newValue) {
        displayValue(newValue);
        displayedValue = newValue;
    }

    protected abstract void displayValue(T newValue);

    protected void elementValueChanged() {
        T newValue = getElementValue();
        if (newValue != displayedValue) {
            settings.setSetting(settingName, newValue);
            displayedValue = newValue;
        }
    }

    protected abstract T getElementValue();

    @Override
    public void onManualSettingChange(String setting, SettingsManager manager) {
        if(setting != settingName)
            throw new IllegalArgumentException("Received event for non-managed setting!");

        setValue(manager.getSetting(setting));
    }

    @Override
    public void onAutomaticSettingChange(String setting, SettingsManager manager) {
        if(setting != settingName)
            throw new IllegalArgumentException("Received event for non-managed setting!");

        setValue(manager.getSetting(setting));
    }
}
