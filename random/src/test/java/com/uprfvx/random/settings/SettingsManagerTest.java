package com.uprfvx.random.settings;

import com.uprfvx.random.settings.definitions.SettingDefinition;
import org.junit.jupiter.api.Test;
import com.uprfvx.random.settings.Settings;

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

        manager.setSetting("NoRandomIntroMon", true);
        boolean value = manager.getSetting("NoRandomIntroMon");
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
        manager.setSetting("UpdateMoves", true);

        manager.setSetting("UpdateMovesToGeneration", 8);
        int value = manager.getSetting("UpdateMovesToGeneration");
        assert (value == 8);
    }

    @Test
    public void canGetEnumValue() {
        SettingsManager manager = new SettingsManager();

        Settings.BSTMod value = manager.getSetting("RandomizePokemonBaseStatTotals");
        assert (value == Settings.BSTMod.UNCHANGED);
    }

    @Test
    public void canSetEnumValue() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("RandomizePokemonBaseStatTotals", Settings.BSTMod.RANDOM);
        Settings.BSTMod value = manager.getSetting("RandomizePokemonBaseStatTotals");
        assert (value == Settings.BSTMod.RANDOM);
    }

    //TODO: other data types used (String, Double, ?)

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
    public void setToOutOfRangeValueFails() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("UpdateMovesToGeneration", 1);
        int value = manager.getSetting("UpdateMovesToGeneration");
        assert (value != 1);
    }

    @Test
    public void setToDisabledSettingFails() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("AllowGeneration1", true);
        boolean value = manager.getSetting("AllowGeneration1");
        assert (value != true);
    }

    /*
    We don't yet have any settings defined that are appropriate for this test!
    (Need one that has enabled *value* restrictions,
    and that is enabled by some means other than custom starters (bc that's a whole mess.))
    Although... I guess we can still do it, it's just a hassle.
    TODO: Implement test
    @Test
    public void setToDisabledValueFails() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("", ); //Some enum setting, probably
        boolean value = manager.getSetting("");
        assert (value != true);
    }
    */



    @Test
    public void returnsToDefaultValueWhenSettingDisabled() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("RandomizePokemonBaseStatTotals", Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.setSetting("BSTRandomBuffNerfPercentage", 22);
        int value = manager.getSetting("BSTRandomBuffNerfPercentage");
        assert (value == 22);

        manager.setSetting("RandomizePokemonBaseStatTotals", Settings.BSTMod.SHUFFLE);
        value = manager.getSetting("BSTRandomBuffNerfPercentage");
        assert (value != 22);
    }

    //TODO: returnsToDefaultWhenCurrentValueDisabled
    //Same issue as with setToDisabledValueFails

    @Test
    public void manualChangeListenerIsCalled() {
        SettingsManager manager = new SettingsManager();
        SettingsListenerTestTool listener = new SettingsListenerTestTool();

        manager.addListener("NoRandomIntroMon", listener);
        manager.setSetting("NoRandomIntroMon", true);
        assert(listener.manualSettingChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenSettingEnabled() {
        SettingsManager manager = new SettingsManager();
        SettingsListenerTestTool listener = new SettingsListenerTestTool();

        manager.addListener("BSTRandomBuffNerfPercentage", listener);
        manager.setSetting("RandomizePokemonBaseStatTotals", Settings.BSTMod.RANDOM_BUFF_NERF);

        assert(listener.possibleEnablementChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenSettingDisabled() {
        SettingsManager manager = new SettingsManager();
        SettingsListenerTestTool listener = new SettingsListenerTestTool();

        manager.setSetting("RandomizePokemonBaseStatTotals", Settings.BSTMod.RANDOM_BUFF_NERF);

        manager.addListener("BSTRandomBuffNerfPercentage", listener);
        manager.setSetting("RandomizePokemonBaseStatTotals", Settings.BSTMod.SHUFFLE);
        assert(listener.possibleEnablementChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenValuesEnabled() {

    }

    @Test
    public void automaticChangeListenerIsCalledWhenChangedSettingIsDisabled() {
        SettingsManager manager = new SettingsManager();
        SettingsListenerTestTool listener = new SettingsListenerTestTool();

        manager.setSetting("RandomizePokemonBaseStatTotals", Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.setSetting("BSTRandomBuffNerfPercentage", 22);
        manager.addListener("BSTRandomBuffNerfPercentage", listener);

        assert(!listener.automaticSettingChangeCalled);

        manager.setSetting("RandomizePokemonBaseStatTotals", Settings.BSTMod.SHUFFLE);
        assert(listener.automaticSettingChangeCalled);
    }
}
