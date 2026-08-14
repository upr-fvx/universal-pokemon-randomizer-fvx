package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;

public abstract class NumericSingleElementCoordinator<N extends Number & Comparable<N>, J extends JComponent>
        extends SettingSingleElementCoordinator<N, J> {

    protected N currentMin;
    protected N currentMax;

    public NumericSingleElementCoordinator(Settings.Name settingName, SettingsManager settings, J element) {
        this(settingName, settings, element, null);
    }

    public NumericSingleElementCoordinator(Settings.Name settingName, SettingsManager settings, J element, JCheckBox latch) {
        super(settingName, settings, element, latch);

        currentMin = settings.getCurrentMinimum(settingName);
        currentMax = settings.getCurrentMaximum(settingName);
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
        N newMax = manager.getCurrentMaximum(settingName);

        if (!currentMin.equals(newMin) || !currentMax.equals(newMax)) {
            setExtents(newMin, newMax);
            currentMin = newMin;
            currentMax = newMax;
        }
    }

    //This is a single method instead of two because two out of three numeric elements must set both at once, so
    // it would be silly to call two separate methods.
    protected abstract void setExtents(N newMin, N newMax);
}
