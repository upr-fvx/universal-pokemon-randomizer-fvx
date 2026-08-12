package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;

import javax.swing.*;
import java.io.Serializable;

/**
 * Coordinates a Setting with a single UI element.
 */
public abstract class SettingSingleElementCoordinator<V extends Serializable, J extends JComponent>
        extends SettingUICoordinator<V> {

    protected final J element;

    public SettingSingleElementCoordinator(Settings.Name settingName, SettingsManager settings, J element) {
        this(settingName, settings, element, null);
    }

    public SettingSingleElementCoordinator(Settings.Name settingName, SettingsManager settings, J element, JCheckBox latch) {
        super(settingName, settings, latch);

        this.element = element;
        setInitialState();
    }

    @Override
    protected void setEnabled(boolean enabled) {
        element.setEnabled(enabled);
    }

    @Override
    protected void setVisible(boolean visible) {
        element.setVisible(visible);
    }
}
