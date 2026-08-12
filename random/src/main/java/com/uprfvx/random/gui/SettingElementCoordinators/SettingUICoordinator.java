package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.SettingChangeListener;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Objects;

/**
 * A class which coordinates a single setting with one or multiple GUI elements.
 * @param <V> The type of data held by the setting.
 */
public abstract class SettingUICoordinator<V extends Serializable, E extends JComponent>
        implements SettingChangeListener {

    private final Settings.Name settingName;
    private final SettingsManager settings;
    //I don't like that I have to keep the settingsManager here...
    //but I don't know how else I'd access it to set the setting.
    private final JCheckBox latch;

    private V displayedValue;
    protected E element;
    private boolean unlatched;

    public SettingUICoordinator(Settings.Name settingName, SettingsManager settings, E element) {
        this(settingName, settings, element, null);
    }

    public SettingUICoordinator(Settings.Name settingName, SettingsManager settings, E element, JCheckBox latch) {
        this.settingName = settingName;
        this.settings = settings;
        this.element = element;
        this.latch = latch;

        if (latch == null) {
            unlatched = true;
        } else {
            unlatched = latch.isSelected();
            latch.addActionListener(this::latchValueChanged);
        }

        V initialValue = settings.getSetting(settingName);
        setValue(initialValue);

        settings.addListener(settingName, this);
    }

    private void latchValueChanged(ActionEvent event) {
        unlatched = latch.isSelected();
    }

    public void setValue(V newValue) {
        displayValue(newValue);
        displayedValue = newValue;
    }

    protected abstract void displayValue(V newValue);

    protected void elementValueChanged(ActionEvent event) {
        V newValue = getElementValue();
        if (!newValue.equals(displayedValue)) {
            settings.setSetting(settingName, newValue);
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

        element.setEnabled(unlatched && manager.isEnabled(settingName));
    }

    @Override
    public void onSupportChange(Settings.Name setting, SettingsManager manager, boolean isSupported) {
        if (setting != this.settingName)
            throw new IllegalArgumentException("Received event for non-managed setting!");

        element.setVisible(isSupported);
    }
}
