package com.uprfvx.random.settings;

import com.uprfvx.random.Version;
import com.uprfvx.random.settings.Settings.Name;
import com.uprfvx.romio.romhandlers.RomHandlerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SettingsManagerSupportTest extends RomHandlerTest {

    @Test
    public void setToUnsupportedSettingFails()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);

        assertThrows(IllegalStateException.class,
                () -> manager.set(Name.LIMIT_BAN_GENERATION_1, true));
        boolean value = manager.get(Name.LIMIT_BAN_GENERATION_1);
        assertFalse(value);
    }

    @Test
    public void canSetToSettingWhenSupported() {
        SettingsManager manager = new SettingsManager();
        loadROM("Gold (U)");

        manager.associateGame(romHandler);

        manager.set(Name.LIMIT_BAN_GENERATION_1, true);

        boolean value = manager.get(Name.LIMIT_BAN_GENERATION_1);
        assertTrue(value);
    }

    @Test
    public void setToUnsupportedValueFails()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.associateGame(romHandler);
        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM);


        assertThrows(IllegalStateException.class,
                () -> manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS));
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assertNotEquals(Settings.StartersTypeMod.FIRE_WATER_GRASS, value);
    }

    @Test
    public void canSetToSupportedValuesWhenOthersUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.associateGame(romHandler);
        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM);

        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.UNIQUE);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assertEquals(Settings.StartersTypeMod.UNIQUE, value);
    }

    @Test
    public void canSetToValueWhenSupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);
        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM);

        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assertEquals(Settings.StartersTypeMod.FIRE_WATER_GRASS, value);
    }

    @Test
    public void returnsToDefaultWhenSettingUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.set(Name.LIMIT_BAN_GENERATION_1, true);
        boolean value = manager.get(Name.LIMIT_BAN_GENERATION_1);
        assumeTrue(value);

        manager.associateGame(romHandler);

        value = manager.get(Name.LIMIT_BAN_GENERATION_1);
        assertFalse(value);
    }

    @Test
    public void returnsToDefaultWhenCurrentValueUnsupported()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Yellow (U)");

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.RANDOM);

        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.associateGame(romHandler);

        value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assertNotEquals(Settings.StartersTypeMod.FIRE_WATER_GRASS, value);
    }

    @Test
    public void canSetToUnsupportedSettingAfterGameUnassociated()
    {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");

        manager.associateGame(romHandler);
        manager.unassociateGame();

        manager.set(Name.LIMIT_BAN_GENERATION_1, true);

        boolean value = manager.get(Name.LIMIT_BAN_GENERATION_1);
        assertTrue(value);
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

    @Test
    public void populateFromIni_NoGameLoaded_NoGameInIni_GameMatchIsNoGameLoaded() {
        SettingsManager manager = new SettingsManager();
        manager.populateFromIni(String.format("""
                        [Settings]
                        VersionID=%d
                        ROMName=NONE
                        Setting<UPDATE_MOVES>=true
                        [Settings_end]
                        """, Version.LATEST.id));
        assertEquals(SettingsManager.GameMatch.NO_GAME_LOADED, manager.getLoadedSettingsMatchGame());
    }

    @Test
    public void populateFromIni_NoGameLoaded_GameInIni_GameMatchIsNoGameLoaded() {
        SettingsManager manager = new SettingsManager();
        manager.populateFromIni(String.format("""
                        [Settings]
                        VersionID=%d
                        ROMName=Red (U)
                        Setting<UPDATE_MOVES>=true
                        [Settings_end]
                        """, Version.LATEST.id));
        assertEquals(SettingsManager.GameMatch.NO_GAME_LOADED, manager.getLoadedSettingsMatchGame());
    }

    @Test
    public void populateFromIni_GameLoaded_NoGameInIni_GameMatchIsNoMatch() {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");
        manager.associateGame(romHandler);

        manager.populateFromIni(String.format("""
                        [Settings]
                        VersionID=%d
                        ROMName=NONE
                        Setting<UPDATE_MOVES>=true
                        [Settings_end]
                        """, Version.LATEST.id));
        assertEquals(SettingsManager.GameMatch.NO_MATCH, manager.getLoadedSettingsMatchGame());
    }

    @Test
    public void populateFromIni_GameLoaded_OtherGameInIni_GameMatchIsNoMatch() {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");
        manager.associateGame(romHandler);

        manager.populateFromIni(String.format("""
                        [Settings]
                        VersionID=%d
                        ROMName=Yellow (U)
                        Setting<UPDATE_MOVES>=true
                        [Settings_end]
                        """, Version.LATEST.id));
        assertEquals(SettingsManager.GameMatch.NO_MATCH, manager.getLoadedSettingsMatchGame());
    }

    @Test
    public void populateFromIni_GameLoaded_SameGameInIni_GameMatchIsNoMatch() {
        SettingsManager manager = new SettingsManager();
        loadROM("Red (U)");
        manager.associateGame(romHandler);

        manager.populateFromIni(String.format("""
                        [Settings]
                        VersionID=%d
                        ROMName=Red (U)
                        Setting<UPDATE_MOVES>=true
                        [Settings_end]
                        """, Version.LATEST.id));
        assertEquals(SettingsManager.GameMatch.NO_MATCH, manager.getLoadedSettingsMatchGame());
    }
}
