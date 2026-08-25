package com.uprfvx.random.settings;

import com.uprfvx.random.settings.Settings.Name;
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

        manager.set(Name.BAN_GENERATION_1, true);

        boolean value = manager.get(Name.BAN_GENERATION_1);
        assert(value == false);
    }

    @Test
    public void canSetToSettingWhenSupported() {
        SettingsManager manager = new SettingsManager();
        loadROM("Gold (U)");

        manager.associateGame(romHandler);

        manager.set(Name.BAN_GENERATION_1, true);

        boolean value = manager.get(Name.BAN_GENERATION_1);
        assert(value == true);
    }

    @Test
    public void setToUnsupportedValueFails()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.associateGame(romHandler);
        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);

        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assert(value != Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void canSetToSupportedValuesWhenOthersUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.associateGame(romHandler);
        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);

        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.UNIQUE);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assert(value == Settings.StartersTypeMod.UNIQUE);
    }

    @Test
    public void canSetToValueWhenSupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);
        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);

        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assert(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void returnsToDefaultWhenSettingUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.set(Name.BAN_GENERATION_1, true);
        boolean value = manager.get(Name.BAN_GENERATION_1);
        assumeTrue(value);

        manager.associateGame(romHandler);

        value = manager.get(Name.BAN_GENERATION_1);
        assert(value == false);
    }

    @Test
    public void returnsToDefaultWhenCurrentValueUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.COMPLETELY_RANDOM);

        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.associateGame(romHandler);

        value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assert(value != Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void canSetToUnsupportedSettingAfterGameUnassociated()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);
        manager.unassociateGame();

        manager.set(Name.BAN_GENERATION_1, true);

        boolean value = manager.get(Name.BAN_GENERATION_1);
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

    //TODO: listener tests
}
