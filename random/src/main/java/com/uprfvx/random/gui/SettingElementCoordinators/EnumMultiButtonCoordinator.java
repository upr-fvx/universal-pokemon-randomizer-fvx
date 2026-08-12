package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;
import java.util.Map;

public class EnumMultiButtonCoordinator<E extends Enum<E>, J extends AbstractButton>
        extends SettingUICoordinator<E> {
    //Would have an abstract parent, except... I'm pretty sure this is the only case of multiple elements
    // per Setting that ever comes up.
    // (Since MenuItems also count as Buttons.)

    protected final Map<E, J> elements;

    public EnumMultiButtonCoordinator(Settings.Name settingName, SettingsManager settings, Map<E, J> elements) {
        this(settingName, settings, elements, null);
    }

    public EnumMultiButtonCoordinator(Settings.Name settingName, SettingsManager settings, Map<E, J> elements,
                                      JCheckBox latch) {
        super(settingName, settings, latch);

        //TODO: consider grouping?

        this.elements = elements;
        setInitialState();
    }

    @Override
    protected void setEnabled(boolean enabled) {
        elements.forEach((e, j) -> j.setEnabled(enabled && manager.isValueEnabled(settingName, e)));
    }

    //These are overreaching a bit, but... doing them in a less-reachy way would
    // take more storage AND be less convenient AND require more method calls. So there's no benefit.
    @Override
    protected void setVisible(boolean visible) {
        elements.forEach((e, j) -> j.setVisible(visible && manager.isValueSupported(settingName, e)));
    }

    @Override
    protected void displayValue(E newValue) {
        elements.get(newValue).setSelected(true);
    }

    @Override
    protected E getElementValue() {
        return null;
    }

    @Override
    public void onPossibleSupportedValuesChange(Settings.Name setting, SettingsManager manager, RomHandler game) {
        setVisible(manager.isSupported(settingName));
    }
}
