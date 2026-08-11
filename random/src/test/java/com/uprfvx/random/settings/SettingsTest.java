package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;
import com.uprfvx.romio.romhandlers.RomHandlerTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static com.uprfvx.random.settings.Settings.ALL_SETTINGS;

/**
 * A class for tests on the set of SettingDefinitions.
 * Should test not the code, but the defined values.
 */
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

    @Test
    public void EverySettingHasUniqueName() {
        Map<Settings.Name, Settings.Category> namesToCategories = new HashMap<>();
        boolean passing = true;
        for (SettingDefinition<?> setting : ALL_SETTINGS) {
            if(namesToCategories.containsKey(setting.getName())) {
                passing = false;
                System.out.println("Duplicate setting name: " + setting.getName());
                System.out.println("Categories: " + setting.getCategory() + ", " + namesToCategories.get(setting.getName()));
            }
            namesToCategories.put(setting.getName(), setting.getCategory());
        }

        assert passing;
    }

    @Test
    public void noSettingHasInitialNullValue() {
        SettingsManager manager = new SettingsManager();
        Collection<SettingState<?>> allSettings = manager.testGetAllSettings();

        boolean passing = true;
        for (SettingState<?> setting : allSettings) {
            if(setting.getValue() == null) {
                System.out.println("Initial value is null for: " + setting.getDefinition().getName());
                passing = false;
            }
        }
        assert(passing);
    }
}
