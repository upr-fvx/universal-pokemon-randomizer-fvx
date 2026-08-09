package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.SettingChangeListener;
import com.uprfvx.random.settings.SettingsManager;

import java.io.Serializable;
import java.util.Objects;

/**
 * A class which coordinates a single setting with one or multiple GUI elements.
 * @param <V> The type of data held by the setting.
 */
public abstract class SettingUI<V extends Serializable> implements SettingChangeListener {
    private V displayedValue;
    private String settingName;
    private SettingsManager settings;
    //I don't like that I have to keep the settingsManager here...
    //but I don't know how else I'd access it to set the setting.

    public SettingUI(String settingName, SettingsManager settings) {
        this.settingName = settingName;
        this.settings = settings;

        V initialValue = settings.getSetting(settingName);
        setValue(initialValue);

        settings.addListener(settingName, this);
    }

    public void setValue(V newValue) {
        displayValue(newValue);
        displayedValue = newValue;
    }

    protected abstract void displayValue(V newValue);

    protected void elementValueChanged() {
        V newValue = getElementValue();
        if (!newValue.equals(displayedValue)) {
            settings.setSetting(settingName, newValue);
            displayedValue = newValue;
        }
    }

    protected abstract V getElementValue();

    @Override
    public void onManualSettingChange(String setting, SettingsManager manager) {
        if(!Objects.equals(setting, settingName))
            throw new IllegalArgumentException("Received event for non-managed setting!");

        V newValue = manager.getSetting(setting);

        if (!newValue.equals(displayedValue)) {
            setValue(newValue);
        }
    }

    @Override
    public void onAutomaticSettingChange(String setting, SettingsManager manager) {
        if(!Objects.equals(setting, settingName))
            throw new IllegalArgumentException("Received event for non-managed setting!");

        V newValue = manager.getSetting(setting);

        if (!newValue.equals(displayedValue)) {
            setValue(newValue);
        }
    }
}
