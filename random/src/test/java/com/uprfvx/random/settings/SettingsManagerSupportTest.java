package com.uprfvx.random.settings;

import com.uprfvx.romio.romhandlers.RomHandlerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SettingsManagerSupportTest extends RomHandlerTest {

    @Test
    public void setToUnsupportedSettingFails()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);

        manager.setSetting("LimitPokemon", true);

        boolean value = manager.getSetting("LimitPokemon");
        assert(value == false);
    }

    @Test
    public void canSetToSettingWhenSupported() {
        SettingsManager manager = new SettingsManager();
        loadROM("Gold (U)");

        manager.associateGame(romHandler);

        manager.setSetting("LimitPokemon", true);

        boolean value = manager.getSetting("LimitPokemon");
        assert(value == true);
    }

    @Test
    public void setToUnsupportedValueFails()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.associateGame(romHandler);
        manager.setSetting("RandomizeStarters", Settings.StartersMod.COMPLETELY_RANDOM);

        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assert(value != Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void canSetToSupportedValuesWhenOthersUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.associateGame(romHandler);
        manager.setSetting("RandomizeStarters", Settings.StartersMod.COMPLETELY_RANDOM);

        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.UNIQUE);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assert(value == Settings.StartersTypeMod.UNIQUE);
    }

    @Test
    public void canSetToValueWhenSupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);
        manager.setSetting("RandomizeStarters", Settings.StartersMod.COMPLETELY_RANDOM);

        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assert(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void returnsToDefaultWhenSettingUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.setSetting("LimitPokemon", true);
        boolean value = manager.getSetting("LimitPokemon");
        assumeTrue(value);

        manager.associateGame(romHandler);

        value = manager.getSetting("LimitPokemon");
        assert(value == false);
    }

    @Test
    public void returnsToDefaultWhenCurrentValueUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.setSetting("RandomizeStarters", Settings.StartersMod.COMPLETELY_RANDOM);

        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.associateGame(romHandler);

        value = manager.getSetting("StartersTypeRestriction");
        assert(value != Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void canSetToUnsupportedSettingAfterGameUnassociated()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);
        manager.unassociateGame();

        manager.setSetting("LimitPokemon", true);

        boolean value = manager.getSetting("LimitPokemon");
        assert(value == true);
    }

    @Test
    public void throwsWhenAssociatingGameTwice()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);

        assertThrows(IllegalStateException.class, () -> manager.associateGame(romHandler));
    }

    @Test
    public void doesNotThrowWhenUnassociatingAndAssociatingAgain()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);
        manager.unassociateGame();

        assertDoesNotThrow(() -> manager.associateGame(romHandler));
    }
}
