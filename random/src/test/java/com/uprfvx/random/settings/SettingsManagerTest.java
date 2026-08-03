package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SettingsManagerTest {

    @Test
    public void canConstruct() {
        SettingsManager manager = new SettingsManager();
    }

    @Test
    public void canGetBooleanValue() {
        SettingsManager manager = new SettingsManager();

        boolean value = manager.getSetting("LimitPokemon");
        assert(value == false);
    }

    @Test
    public void canSetBooleanValue() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("LimitPokemon", true);
        boolean value = manager.getSetting("LimitPokemon");
        assert(value == true);
    }

    @Test
    public void canGetIntValue() {
        SettingsManager manager = new SettingsManager();

        int value = manager.getSetting("UpdateMovesToGeneration");
        assert (value == 9);
    }

    @Test
    public void canSetIntValue() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("UpdateMovesToGeneration", 7);
        int value = manager.getSetting("UpdateMovesToGeneration");
        assert (value == 7);
    }

    @Test
    public void getWrongTypeThrows() {
        SettingsManager manager = new SettingsManager();

        Exception e = assertThrows(ClassCastException.class, () -> {
            int value = manager.getSetting("LimitPokemon");
        });
    }

    @Test
    public void setWrongTypeThrows() {
        SettingsManager manager = new SettingsManager();

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.setSetting("LimitPokemon", 3.0);
        });
    }

    @Test
    public void getWrongTypeDefinitionThrows() {
        SettingsManager manager = new SettingsManager();

        Exception e = assertThrows(ClassCastException.class, () -> {
            SettingDefinition<Integer> definition = manager.getSettingDefinition("LimitPokemon");
        });
    }
}
