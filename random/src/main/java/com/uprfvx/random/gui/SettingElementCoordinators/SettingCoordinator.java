package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.SettingChangeListener;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.io.Serializable;

/**
 * A class which coordinates a single setting with one or multiple GUI elements.
 * @param <V> The type of data held by the setting.
 */
public abstract class SettingCoordinator<V extends Serializable, U extends UIManager<V>>
        implements SettingChangeListener {

    protected final Settings.Name settingName;
    protected final SettingsManager manager;
    protected U element;

    private final JCheckBox latch;
    private V displayedValue;
    private boolean unlatched;

    public SettingCoordinator(Settings.Name settingName, SettingsManager manager, U element) {
        this(settingName, manager, element, null);
    }

    public SettingCoordinator(Settings.Name settingName, SettingsManager manager, U element, JCheckBox latch) {
        this.settingName = settingName;
        this.manager = manager;
        this.element = element;
        this.latch = latch;

        if (latch == null) {
            unlatched = true;
        } else {
            unlatched = latch.isSelected();
            latch.addActionListener(this::latchValueChanged);
        }

        manager.addListener(settingName, this);
        element.addListener(this::elementValueChanged);

        setInitialState();
    }

    protected void setInitialState()
    {
        V initialValue = manager.get(settingName);
        setValue(initialValue);

        element.setEnabled(unlatched && manager.isEnabled(settingName));
        element.setVisible(manager.isSupported(settingName));
    }

    private void latchValueChanged(ActionEvent event) {
        unlatched = latch.isSelected();
    }

    public void setValue(V newValue) {
        element.displayValue(newValue);
        displayedValue = newValue;
    }

    protected void elementValueChanged(ChangeEvent e) {
        V newValue = element.getElementValue();
        if (!newValue.equals(displayedValue)) {
            manager.set(settingName, newValue);
            displayedValue = newValue;
        }
    }

    @Override
    public void onManualSettingChange(Settings.Name setting, SettingsManager manager) {
        settingMatchCheck(setting);

        V newValue = manager.get(setting);

        if (!newValue.equals(displayedValue)) {
            setValue(newValue);
        }
    }

    @Override
    public void onAutomaticSettingChange(Settings.Name setting, SettingsManager manager) {
        settingMatchCheck(setting);

        V newValue = manager.get(setting);

        if (!newValue.equals(displayedValue)) {
            setValue(newValue);
        }
    }

    @Override
    public void onPossibleEnablementChange(Settings.Name setting, SettingsManager manager) {
        settingMatchCheck(setting);

        element.setEnabled(unlatched && manager.isEnabled(settingName));
    }

    @Override
    public void onSupportChange(Settings.Name setting, SettingsManager manager, boolean isSupported) {
        settingMatchCheck(setting);

        element.setVisible(isSupported);
    }

    protected void settingMatchCheck(Settings.Name nameGiven) {
        if(nameGiven != this.settingName)
            throw new IllegalArgumentException("Received event for non-managed setting!");
    }
}
