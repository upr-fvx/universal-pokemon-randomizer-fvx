package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandlerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;

//Extends RomHandlerTest to test on all ROMs
public class SettingsTest {

    @Test
    public void canTestEverySettingEnabledWithoutCrash() {
        SettingsManager manager = new SettingsManager();
        Collection<SettingState<?>> allSettings = manager.testGetAllSettings();

        for (SettingState<?> setting : allSettings) {
            System.out.println("Testing: " + setting.getDefinition().getName());
            setting.currentValueIsEnabled(manager);
        }
    }

    @Test
    public void allSettingsDefaultValuesAreInitiallyEnabled() {
        SettingsManager manager = new SettingsManager();
        Collection<SettingState<?>> allSettings = manager.testGetAllSettings();

        boolean passing = true;
        for (SettingState<?> setting : allSettings) {
            if(!setting.currentValueIsEnabled(manager)) {
                System.out.println("Initial value disabled for: " + setting.getDefinition().getName());
                passing = false;
            }
        }
        assert(passing);
    }


}
