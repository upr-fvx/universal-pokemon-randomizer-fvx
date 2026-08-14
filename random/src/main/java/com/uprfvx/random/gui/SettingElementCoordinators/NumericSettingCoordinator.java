package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;

public class NumericSettingCoordinator<N extends Number & Comparable<N>, U extends NumericUIManager<N>>
        extends SettingCoordinator<N, U> {

    protected N currentMin;
    protected N currentMax;

    public NumericSettingCoordinator(Settings.Name settingName, SettingsManager settings, U element) {
        this(settingName, settings, element, null);
    }

    public NumericSettingCoordinator(Settings.Name settingName, SettingsManager settings, U element, JCheckBox latch) {
        super(settingName, settings, element, latch);

        currentMin = manager.getCurrentMinimum(settingName);
        element.setMinimum(currentMin);

        currentMax = manager.getCurrentMaximum(settingName);
        element.setMaximum(currentMax);
    }

    @Override
    public void onPossibleEnablementChange(Settings.Name setting, SettingsManager manager) {
        super.onPossibleEnablementChange(setting, manager);
        //Super takes care of full setting enable/disable, so now we just need min/max

        checkExtents();
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {

        checkExtents();
    }

    private void checkExtents() {
        N newMin = manager.getCurrentMinimum(settingName);
        if (!currentMin.equals(newMin)) {
            element.setMinimum(newMin);
            currentMin = newMin;
        }

        N newMax = manager.getCurrentMaximum(settingName);
        if (!currentMax.equals(newMax)) {
            element.setMaximum(newMax);
            currentMax = newMax;
        }
    }
}
