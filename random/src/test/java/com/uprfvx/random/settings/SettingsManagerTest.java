package com.uprfvx.random.settings;

import org.junit.jupiter.api.Test;

public class SettingsManagerTest {

    @Test
    public void canConstruct()
    {
        SettingsManager manager = new SettingsManager();
    }

    @Test
    public void canGetValue()
    {
        SettingsManager manager = new SettingsManager();

        boolean value = manager.getSetting("LimitPokemon");
        assert(value == false);
    }
}
