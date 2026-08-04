package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandlerTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;

public class SettingsSupportTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canTestAllSettingsSupportInEachGame(String romName) {
        SettingsManager manager = new SettingsManager();
        loadROM(romName);
        Collection<SettingState<?>> allSettings = manager.testGetAllSettings();

        for (SettingState<?> setting : allSettings) {
            System.out.println("Testing: " + setting.getDefinition().getName());
            setting.currentValueIsSupported(romHandler);
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allSettingsDefaultValuesSupportedInEveryGame(String romName) {
        SettingsManager manager = new SettingsManager();
        loadROM(romName);
        Collection<SettingState<?>> allSettings = manager.testGetAllSettings();

        boolean passing = true;
        for (SettingState<?> setting : allSettings) {
            if(!setting.currentValueIsSupported(romHandler)) {
                System.out.println("Initial value unsupported for: " + setting.getDefinition().getName());
                passing = false;
            }
        }
        assert(passing);
    }
}
