package com.uprfvx.random.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SettingsManagerTest {

    @Test
    public void canConstruct() {
        SettingsManager manager = new SettingsManager();
    }

    @Test
    public void canGetBooleanValue() {
        SettingsManager manager = new SettingsManager();

        boolean value = manager.getSetting(Settings.Names.LIMIT_POKEMON);
        assert(value == false);
    }

    @Test
    public void canSetBooleanValue() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, true);
        boolean value = manager.getSetting(Settings.Names.NO_RANDOM_INTRO_MON);
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
        boolean parent = manager.getSetting("UpdateMoves");
        assumeTrue(parent);

        manager.setSetting("UpdateMovesToGeneration", 8);
        int value = manager.getSetting("UpdateMovesToGeneration");
        assert (value == 8);
    }

    @Test
    public void canGetEnumValue() {
        SettingsManager manager = new SettingsManager();

        Settings.BSTMod value = manager.getSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS);
        assert (value == Settings.BSTMod.UNCHANGED);
    }

    @Test
    public void canSetEnumValue() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        Settings.BSTMod value = manager.getSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS);
        assert (value == Settings.BSTMod.RANDOM);
    }

    @Test
    public void canGetSpeciesIndexValue() {
        SettingsManager manager = new SettingsManager();

        int value = manager.getSetting("CustomStarter1");
        assert (value == 0);
    }

    @Test
    public void canSetSpeciesIndexValue() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);

        manager.setSetting("CustomStarter1", 8);
        int value = manager.getSetting("CustomStarter1");
        assert (value == 8);
    }

    //TODO: other data types used (String, Double, ?)

    @Test
    public void getWrongTypeThrows() {
        SettingsManager manager = new SettingsManager();

        Exception e = assertThrows(ClassCastException.class, () -> {
            int value = manager.getSetting(Settings.Names.LIMIT_POKEMON);
        });
    }

    @Test
    public void setWrongTypeThrows() {
        SettingsManager manager = new SettingsManager();

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.setSetting(Settings.Names.LIMIT_POKEMON, 3.0);
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

    @Test
    public void setToDisableableSettingWorksWhenEnabled() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.setSetting(Settings.Names.BST_BUFF_NERF_PERCENT, 22);
        int value = manager.getSetting(Settings.Names.BST_BUFF_NERF_PERCENT);
        assert (value == 22);
    }

    @Test
    public void returnsToDefaultValueWhenSettingDisabled() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.setSetting(Settings.Names.BST_BUFF_NERF_PERCENT, 22);
        int value = manager.getSetting(Settings.Names.BST_BUFF_NERF_PERCENT);
        assumeTrue(value == 22);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        value = manager.getSetting(Settings.Names.BST_BUFF_NERF_PERCENT);
        assert (value != 22);
    }

    @Test
    public void setToDisabledValueFails() {
        SettingsManager manager = new SettingsManager();

        //TODO: replace this (and all disabledValues tests) with a simpler case

        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);
        manager.setSetting("CustomStarter1", 15);

        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assert (value != Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void canSetToDisableableValueWhenEnabled() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);
        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assert (value == Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void returnsToDefaultWhenCurrentValueDisabled() {
        SettingsManager manager = new SettingsManager();

        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);
        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.setSetting("CustomStarter1", 15);
        value = manager.getSetting("StartersTypeRestriction");
        assert (value != Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void manualChangeListenerIsCalled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addListener(Settings.Names.NO_RANDOM_INTRO_MON, listener);
        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, true);
        assert(listener.manualSettingChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenSettingEnabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addListener(Settings.Names.BST_BUFF_NERF_PERCENT, listener);
        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);

        assert(listener.possibleEnablementChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenSettingDisabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);

        manager.addListener(Settings.Names.BST_BUFF_NERF_PERCENT, listener);
        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener.possibleEnablementChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenValuesEnabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);
        manager.setSetting("CustomStarter1", 15);

        manager.addListener("StartersTypeRestriction", listener);
        manager.setSetting("CustomStarter1", 0);

        assert (listener.possibleEnablementChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenValuesDisabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);

        manager.addListener("StartersTypeRestriction", listener);
        manager.setSetting("CustomStarter1", 15);

        assert (listener.possibleEnablementChangeCalled);
    }

    @Test
    public void automaticChangeListenerIsCalledWhenChangedSettingIsDisabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.setSetting(Settings.Names.BST_BUFF_NERF_PERCENT, 22);
        manager.addListener(Settings.Names.BST_BUFF_NERF_PERCENT, listener);

        assumeFalse(listener.automaticSettingChangeCalled);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener.automaticSettingChangeCalled);
    }

    @Test
    public void automaticChangeListenerIsCalledWhenCurrentValueDisabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);
        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.addListener("StartersTypeRestriction", listener);

        manager.setSetting("CustomStarter1", 15);
        assert(listener.automaticSettingChangeCalled);
    }

    @Test
    public void universalListenerIsCalledOnManualChange() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addUniversalListener(listener);
        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, true);
        assert(listener.manualSettingChangeCalled);

        listener.reset();
        assumeFalse(listener.manualSettingChangeCalled);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(listener.manualSettingChangeCalled);
    }

    @Test
    public void universalListenerIsCalledOnPotentialEnablementChanges() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);

        manager.addUniversalListener(listener);
        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        assert(listener.possibleEnablementChangeCalled);

        listener.reset();
        assumeFalse(listener.possibleEnablementChangeCalled);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener.possibleEnablementChangeCalled);

        listener.reset();
        assumeFalse(listener.possibleEnablementChangeCalled);

        manager.setSetting("CustomStarter1", 15);
        assert (listener.possibleEnablementChangeCalled);

        listener.reset();
        assumeFalse(listener.possibleEnablementChangeCalled);

        manager.setSetting("CustomStarter1", 0);
        assert (listener.possibleEnablementChangeCalled);
    }

    @Test
    public void universalListenerIsCalledOnAutomaticChanges() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.setSetting(Settings.Names.BST_BUFF_NERF_PERCENT, 22);
        manager.setSetting("RandomizeStarters", Settings.StartersMod.CUSTOM);
        manager.setSetting("StartersTypeRestriction", Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.getSetting("StartersTypeRestriction");
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.addUniversalListener(listener);
        assumeFalse(listener.automaticSettingChangeCalled);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener.automaticSettingChangeCalled);

        listener.reset();
        assumeFalse(listener.automaticSettingChangeCalled);

        manager.setSetting("CustomStarter1", 15);
        assert(listener.automaticSettingChangeCalled);
    }

    @Test
    public void listenersNotCalledAfterRemoval() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addListener(Settings.Names.NO_RANDOM_INTRO_MON, listener);
        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, true);
        assumeTrue(listener.manualSettingChangeCalled);

        listener.reset();
        manager.removeListener(Settings.Names.NO_RANDOM_INTRO_MON, listener);
        assumeFalse(listener.manualSettingChangeCalled);

        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, false);
        assert !listener.manualSettingChangeCalled;

        manager.addUniversalListener(listener);
        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, true);
        assumeTrue(listener.manualSettingChangeCalled);

        listener.reset();
        manager.removeUniversalListener(listener);
        assumeFalse(listener.manualSettingChangeCalled);

        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, false);
        assert !listener.manualSettingChangeCalled;
    }

    @Test
    public void multipleListenersWorks() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener1 = new TestSettingsListener();
        TestSettingsListener listener2 = new TestSettingsListener();

        manager.addListener(Settings.Names.BST_BUFF_NERF_PERCENT, listener1);
        manager.addListener(Settings.Names.BST_BUFF_NERF_PERCENT, listener2);
        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        assert(listener1.possibleEnablementChangeCalled);
        assert(listener2.possibleEnablementChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.setSetting(Settings.Names.BST_BUFF_NERF_PERCENT, 16);
        assert(listener1.manualSettingChangeCalled);
        assert(listener2.manualSettingChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener1.automaticSettingChangeCalled);
        assert(listener2.automaticSettingChangeCalled);

        manager.removeListener(Settings.Names.BST_BUFF_NERF_PERCENT, listener1);
        manager.addUniversalListener(listener1);
        listener1.reset();
        listener2.reset();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        assert(listener1.possibleEnablementChangeCalled);
        assert(listener2.possibleEnablementChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.setSetting(Settings.Names.BST_BUFF_NERF_PERCENT, 16);
        assert(listener1.manualSettingChangeCalled);
        assert(listener2.manualSettingChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener1.automaticSettingChangeCalled);
        assert(listener2.automaticSettingChangeCalled);

        manager.removeListener(Settings.Names.BST_BUFF_NERF_PERCENT, listener2);
        manager.addUniversalListener(listener2);
        listener1.reset();
        listener2.reset();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        assert(listener1.possibleEnablementChangeCalled);
        assert(listener2.possibleEnablementChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.setSetting(Settings.Names.BST_BUFF_NERF_PERCENT, 16);
        assert(listener1.manualSettingChangeCalled);
        assert(listener2.manualSettingChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener1.automaticSettingChangeCalled);
        assert(listener2.automaticSettingChangeCalled);
    }

    @Test
    public void duplicateListenersNotAdded() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addListener(Settings.Names.NO_RANDOM_INTRO_MON, listener);
        manager.addListener(Settings.Names.NO_RANDOM_INTRO_MON, listener);
        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, true);
        assumeTrue(listener.manualSettingChangeCalled);
        assert listener.manualChangeCallCount == 1;

        listener.reset();
        manager.removeListener(Settings.Names.NO_RANDOM_INTRO_MON, listener);
        assumeFalse(listener.manualSettingChangeCalled);
        assumeTrue(listener.manualChangeCallCount == 0);

        manager.addUniversalListener(listener);
        manager.addUniversalListener(listener);
        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, false);
        assumeTrue(listener.manualSettingChangeCalled);
        assert listener.manualChangeCallCount == 1;
    }

    @Test
    public void resetAllWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting("UpdateMoves", true);

        manager.setSetting(Settings.Names.NO_RANDOM_INTRO_MON, true);
        manager.setSetting("UpdateMovesToGeneration", 8);
        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);

        boolean boolValue = manager.getSetting(Settings.Names.NO_RANDOM_INTRO_MON);
        int intValue = manager.getSetting("UpdateMovesToGeneration");
        Settings.BSTMod enumValue = manager.getSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS);

        assumeTrue(boolValue);
        assumeTrue(intValue == 8);
        assumeTrue(enumValue == Settings.BSTMod.RANDOM);

        manager.resetAll();

        boolValue = manager.getSetting(Settings.Names.NO_RANDOM_INTRO_MON);
        intValue = manager.getSetting("UpdateMovesToGeneration");
        enumValue = manager.getSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS);

        assert (boolValue != true);
        assert (intValue != 8);
        assert (enumValue != Settings.BSTMod.RANDOM);
    }

    @Test
    public void listenersCalledOnReset() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting("UpdateMoves", true);
        manager.addUniversalListener(listener);

        manager.resetAll();

        assert listener.manualSettingChangeCalled;
        assert listener.possibleEnablementChangeCalled;

        manager.removeUniversalListener(listener);
        listener.reset();
        manager.setSetting("UpdateMoves", true);
        manager.addListener("UpdateMoves", listener);

        manager.resetAll();

        assert listener.manualSettingChangeCalled;
        assert !listener.possibleEnablementChangeCalled;
    }

    @Test
    public void automaticChangeListenerNotCalledOnReset() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.setSetting("UpdateMoves", true);
        manager.addUniversalListener(listener);

        manager.resetAll();
        assert !listener.automaticSettingChangeCalled;

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.setSetting(Settings.Names.BST_BUFF_NERF_PERCENT, 22);
        listener.reset();

        manager.resetAll();
        assert !listener.automaticSettingChangeCalled;

        manager.removeUniversalListener(listener);
        listener.reset();
        manager.setSetting("UpdateMoves", true);
        manager.addListener("UpdateMoves", listener);

        manager.resetAll();

        assert !listener.automaticSettingChangeCalled;
    }

    @Test
    public void listenersNotCalledWhenNotChangingValue() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();
        manager.addUniversalListener(listener);
        manager.setSetting("UpdateMoves", false);

        assert !listener.manualSettingChangeCalled;
        assert !listener.automaticSettingChangeCalled;
        assert !listener.possibleEnablementChangeCalled;

        manager.setSetting("UpdateMoves", true);
        assert !listener.automaticSettingChangeCalled;
        assumeTrue(listener.manualSettingChangeCalled);
        assumeTrue(listener.possibleEnablementChangeCalled);

        listener.reset();
        manager.setSetting("UpdateMoves", true);
        assert !listener.manualSettingChangeCalled;
        assert !listener.automaticSettingChangeCalled;
        assert !listener.possibleEnablementChangeCalled;

        manager.setSetting("UpdateMoves", false);
        assert !listener.automaticSettingChangeCalled;
        assumeTrue(listener.manualSettingChangeCalled);
        assumeTrue(listener.possibleEnablementChangeCalled);
    }

    @Test
    public void listenersNotCalledWhenResettingNothing() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();
        manager.addUniversalListener(listener);

    }


}
