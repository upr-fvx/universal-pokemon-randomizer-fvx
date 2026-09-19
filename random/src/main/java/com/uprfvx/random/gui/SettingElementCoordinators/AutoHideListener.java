package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.SettingChangeListener;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class AutoHideListener implements SettingChangeListener {

    /**
     * Creates an {@link AutoHideListener} and associates it with the manager.<br>
     * If all the listenTo settings are unsupported, this will automatically run {@code toHide.setVisible(false)}.
     * And vice versa if any listenTo settings become supported.<br>
     * This is made to use with e.g. panels and labels.
     * @param toHide the JComponent to automatically hide.
     * @param manager The SettingsManager to listen to.
     * @param listenTo The Name of the settings to listen to.
     */
    public static void associate(JComponent toHide, SettingsManager manager, Settings.Name... listenTo) {
        new AutoHideListener(toHide, manager, listenTo);
    }

    private final JComponent toHide;
    private final Map<Settings.Name, Boolean> relevantSupported;

    private AutoHideListener(JComponent toHide, SettingsManager manager, Settings.Name... listenTo) {
        this.toHide = toHide;
        this.relevantSupported = new HashMap<>();
        for (Settings.Name listenToName : listenTo) {
            this.relevantSupported.put(listenToName, true);
            manager.addListener(listenToName, this);
        }
    }

    @Override
    public void onAutomaticSettingChange(Settings.Name setting, SettingsManager manager) {
    }

    @Override
    public void onPossibleEnablementChange(Settings.Name setting, SettingsManager manager) {
    }

    @Override
    public void onManualSettingChange(Settings.Name setting, SettingsManager manager) {
    }

    @Override
    public void onSupportChange(Settings.Name setting, SettingsManager manager, boolean isSupported) {
        if (relevantSupported.containsKey(setting)) {
            relevantSupported.put(setting, isSupported);
        }
        toHide.setVisible(relevantSupported.containsValue(true));
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {
    }
}
