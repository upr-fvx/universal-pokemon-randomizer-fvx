package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.SettingChangeListener;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.Serializable;

/**
 * A class which coordinates a single setting with one or multiple GUI elements.
 * @param <V> The type of data held by the setting.
 */
public abstract class SettingUICoordinator<V extends Serializable> implements SettingChangeListener {

    protected final Settings.Name settingName;
    protected final SettingsManager manager;
    //I don't like that I have to keep the settingsManager here...
    //but I don't know how else I'd access it to set the setting.
    private final JCheckBox latch;

    private V displayedValue;
    private boolean unlatched;

    public SettingUICoordinator(Settings.Name settingName, SettingsManager manager) {
        this(settingName, manager, null);
    }

    public SettingUICoordinator(Settings.Name settingName, SettingsManager manager, JCheckBox latch) {
        this.settingName = settingName;
        this.manager = manager;
        this.latch = latch;

        if (latch == null) {
            unlatched = true;
        } else {
            unlatched = latch.isSelected();
            latch.addActionListener(this::latchValueChanged);
        }

        manager.addListener(settingName, this);
    }

    protected void setInitialState()
    {
        V initialValue = manager.getSetting(settingName);
        setValue(initialValue);

        setEnabled(unlatched && manager.isEnabled(settingName));
        setVisible(manager.isSupported(settingName));
    }

    private void latchValueChanged(ActionEvent event) {
        unlatched = latch.isSelected();
    }

    public void setValue(V newValue) {
        displayValue(newValue);
        displayedValue = newValue;
    }

    protected abstract void displayValue(V newValue);

    protected void elementValueChanged() {
        V newValue = getElementValue();
        if (!newValue.equals(displayedValue)) {
            manager.setSetting(settingName, newValue);
            displayedValue = newValue;
        }
    }

    protected abstract V getElementValue();

    @Override
    public void onManualSettingChange(Settings.Name setting, SettingsManager manager) {
        if(setting != this.settingName)
            throw new IllegalArgumentException("Received event for non-managed setting!");

        V newValue = manager.getSetting(setting);

        if (!newValue.equals(displayedValue)) {
            setValue(newValue);
        }
    }

    @Override
    public void onAutomaticSettingChange(Settings.Name setting, SettingsManager manager) {
        if(setting != this.settingName)
            throw new IllegalArgumentException("Received event for non-managed setting!");

        V newValue = manager.getSetting(setting);

        if (!newValue.equals(displayedValue)) {
            setValue(newValue);
        }
    }

    @Override
    public void onPossibleEnablementChange(Settings.Name setting, SettingsManager manager) {
        if (setting != this.settingName)
            throw new IllegalArgumentException("Received event for non-managed setting!");

        setEnabled(unlatched && manager.isEnabled(settingName));
    }

    protected abstract void setEnabled(boolean enabled);

    @Override
    public void onSupportChange(Settings.Name setting, SettingsManager manager, boolean isSupported) {
        if (setting != this.settingName)
            throw new IllegalArgumentException("Received event for non-managed setting!");

        setVisible(isSupported);
    }

    protected abstract void setVisible(boolean visible);
}
