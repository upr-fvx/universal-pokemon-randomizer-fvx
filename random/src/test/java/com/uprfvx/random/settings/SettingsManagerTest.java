package com.uprfvx.random.settings;

import com.uprfvx.random.settings.Settings.Name;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SettingsManagerTest {

    @Test
    public void canConstruct() {
        new SettingsManager();
    }

    @Test
    public void canGetBooleanValue() {
        SettingsManager manager = new SettingsManager();

        boolean value = manager.get(Name.COSMETIC_RANDOM_INTRO_MON);
        assert(value == false);
    }

    @Test
    public void canSetBooleanValue() {
        SettingsManager manager = new SettingsManager();

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        boolean value = manager.get(Name.COSMETIC_RANDOM_INTRO_MON);
        assert(value == true);
    }

    @Test
    public void canGetIntValue() {
        SettingsManager manager = new SettingsManager();

        int value = manager.get(Name.UPDATE_MOVES_TO_GENERATION);
        assert (value == 9);
    }

    @Test
    public void canSetIntValue() {
        SettingsManager manager = new SettingsManager();
        manager.set(Name.UPDATE_MOVES, true);
        boolean parent = manager.get(Name.UPDATE_MOVES);
        assumeTrue(parent);

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 8);
        int value = manager.get(Name.UPDATE_MOVES_TO_GENERATION);
        assert (value == 8);
    }

    @Test
    public void canGetEnumValue() {
        SettingsManager manager = new SettingsManager();

        Settings.BSTMod value = manager.get(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS);
        assert (value == Settings.BSTMod.UNCHANGED);
    }

    @Test
    public void canSetEnumValue() {
        SettingsManager manager = new SettingsManager();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        Settings.BSTMod value = manager.get(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS);
        assert (value == Settings.BSTMod.RANDOM);
    }

    @Test
    public void canGetSpeciesIndexValue() {
        SettingsManager manager = new SettingsManager();

        int value = manager.get(Name.STARTER_CUSTOM_1);
        assert (value == 0);
    }

    @Test
    public void canSetSpeciesIndexValue() {
        SettingsManager manager = new SettingsManager();
        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);

        manager.set(Name.STARTER_CUSTOM_1, 8);
        int value = manager.get(Name.STARTER_CUSTOM_1);
        assert (value == 8);
    }

    //TODO: other data types used (String, Double, ?)

    @Test
    public void getWrongTypeThrows() {
        SettingsManager manager = new SettingsManager();

        Exception e = assertThrows(ClassCastException.class, () -> {
            int value = manager.get(Name.COSMETIC_RANDOM_INTRO_MON);
        });
    }

    @Test
    public void setWrongTypeThrows() {
        SettingsManager manager = new SettingsManager();

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            manager.set(Name.COSMETIC_RANDOM_INTRO_MON, 3.0);
        });
    }

    @Test
    public void setToOutOfRangeValueFails() {
        SettingsManager manager = new SettingsManager();

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 1);
        int value = manager.get(Name.UPDATE_MOVES_TO_GENERATION);
        assert (value != 1);
    }

    @Test
    public void setToDisabledSettingFails() {
        SettingsManager manager = new SettingsManager();

        manager.set(Name.SPECIES_BST_SHUFFLE_LEGENDARIES_SEPARATELY, true);
        boolean value = manager.get(Name.SPECIES_BST_SHUFFLE_LEGENDARIES_SEPARATELY);
        assert (value != true);
    }

    @Test
    public void setToDisableableSettingWorksWhenEnabled() {
        SettingsManager manager = new SettingsManager();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.set(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, 22);
        int value = manager.get(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE);
        assert (value == 22);
    }

    @Test
    public void returnsToDefaultValueWhenSettingDisabled() {
        SettingsManager manager = new SettingsManager();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.set(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, 22);
        int value = manager.get(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE);
        assumeTrue(value == 22);

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        value = manager.get(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE);
        assert (value != 22);
    }

    @Test
    public void setToDisabledValueFails() {
        SettingsManager manager = new SettingsManager();

        //TODO: replace this (and all disabledValues tests) with a simpler case

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);
        manager.set(Name.STARTER_CUSTOM_1, 15);

        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assert (value != Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void canSetToDisableableValueWhenEnabled() {
        SettingsManager manager = new SettingsManager();

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);
        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assert (value == Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void returnsToDefaultWhenCurrentValueDisabled() {
        SettingsManager manager = new SettingsManager();

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);
        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.set(Name.STARTER_CUSTOM_1, 15);
        value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assert (value != Settings.StartersTypeMod.FIRE_WATER_GRASS);
    }

    @Test
    public void manualChangeListenerIsCalled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addListener(Name.COSMETIC_RANDOM_INTRO_MON, listener);
        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assert(listener.manualSettingChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenSettingEnabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addListener(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, listener);
        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);

        assert(listener.possibleEnablementChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenSettingDisabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);

        manager.addListener(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, listener);
        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener.possibleEnablementChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenValuesEnabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);
        manager.set(Name.STARTER_CUSTOM_1, 15);

        manager.addListener(Name.STARTERS_TYPE_RESTRICTION, listener);
        manager.set(Name.STARTER_CUSTOM_1, 0);

        assert (listener.possibleEnablementChangeCalled);
    }

    @Test
    public void possibleEnablementChangeListenerIsCalledWhenValuesDisabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);

        manager.addListener(Name.STARTERS_TYPE_RESTRICTION, listener);
        manager.set(Name.STARTER_CUSTOM_1, 15);

        assert (listener.possibleEnablementChangeCalled);
    }

    @Test
    public void automaticChangeListenerIsCalledWhenChangedSettingIsDisabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.set(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, 22);
        manager.addListener(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, listener);

        assumeFalse(listener.automaticSettingChangeCalled);

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener.automaticSettingChangeCalled);
    }

    @Test
    public void automaticChangeListenerIsCalledWhenCurrentValueDisabled() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);
        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.addListener(Name.STARTERS_TYPE_RESTRICTION, listener);

        manager.set(Name.STARTER_CUSTOM_1, 15);
        assert(listener.automaticSettingChangeCalled);
    }

    @Test
    public void universalListenerIsCalledOnManualChange() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addUniversalListener(listener);
        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assert(listener.manualSettingChangeCalled);

        listener.reset();
        assumeFalse(listener.manualSettingChangeCalled);

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(listener.manualSettingChangeCalled);
    }

    @Test
    public void universalListenerIsCalledOnPotentialEnablementChanges() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);

        manager.addUniversalListener(listener);
        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        assert(listener.possibleEnablementChangeCalled);

        listener.reset();
        assumeFalse(listener.possibleEnablementChangeCalled);

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener.possibleEnablementChangeCalled);

        listener.reset();
        assumeFalse(listener.possibleEnablementChangeCalled);

        manager.set(Name.STARTER_CUSTOM_1, 15);
        assert (listener.possibleEnablementChangeCalled);

        listener.reset();
        assumeFalse(listener.possibleEnablementChangeCalled);

        manager.set(Name.STARTER_CUSTOM_1, 0);
        assert (listener.possibleEnablementChangeCalled);
    }

    @Test
    public void universalListenerIsCalledOnAutomaticChanges() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.set(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, 22);
        manager.set(Name.RANDOMIZE_STARTERS, Settings.StartersMod.CUSTOM);
        manager.set(Name.STARTERS_TYPE_RESTRICTION, Settings.StartersTypeMod.FIRE_WATER_GRASS);
        Settings.StartersTypeMod value = manager.get(Name.STARTERS_TYPE_RESTRICTION);
        assumeTrue(value == Settings.StartersTypeMod.FIRE_WATER_GRASS);

        manager.addUniversalListener(listener);
        assumeFalse(listener.automaticSettingChangeCalled);

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener.automaticSettingChangeCalled);

        listener.reset();
        assumeFalse(listener.automaticSettingChangeCalled);

        manager.set(Name.STARTER_CUSTOM_1, 15);
        assert(listener.automaticSettingChangeCalled);
    }

    @Test
    public void listenersNotCalledAfterRemoval() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addListener(Name.COSMETIC_RANDOM_INTRO_MON, listener);
        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assumeTrue(listener.manualSettingChangeCalled);

        listener.reset();
        manager.removeListener(Name.COSMETIC_RANDOM_INTRO_MON, listener);
        assumeFalse(listener.manualSettingChangeCalled);

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, false);
        assert !listener.manualSettingChangeCalled;

        manager.addUniversalListener(listener);
        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assumeTrue(listener.manualSettingChangeCalled);

        listener.reset();
        manager.removeUniversalListener(listener);
        assumeFalse(listener.manualSettingChangeCalled);

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, false);
        assert !listener.manualSettingChangeCalled;
    }

    @Test
    public void multipleListenersWorks() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener1 = new TestSettingsListener();
        TestSettingsListener listener2 = new TestSettingsListener();

        manager.addListener(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, listener1);
        manager.addListener(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, listener2);
        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        assert(listener1.possibleEnablementChangeCalled);
        assert(listener2.possibleEnablementChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.set(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, 16);
        assert(listener1.manualSettingChangeCalled);
        assert(listener2.manualSettingChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener1.automaticSettingChangeCalled);
        assert(listener2.automaticSettingChangeCalled);

        manager.removeListener(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, listener1);
        manager.addUniversalListener(listener1);
        listener1.reset();
        listener2.reset();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        assert(listener1.possibleEnablementChangeCalled);
        assert(listener2.possibleEnablementChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.set(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, 16);
        assert(listener1.manualSettingChangeCalled);
        assert(listener2.manualSettingChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener1.automaticSettingChangeCalled);
        assert(listener2.automaticSettingChangeCalled);

        manager.removeListener(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, listener2);
        manager.addUniversalListener(listener2);
        listener1.reset();
        listener2.reset();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        assert(listener1.possibleEnablementChangeCalled);
        assert(listener2.possibleEnablementChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.set(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, 16);
        assert(listener1.manualSettingChangeCalled);
        assert(listener2.manualSettingChangeCalled);

        listener1.reset();
        listener2.reset();

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(listener1.automaticSettingChangeCalled);
        assert(listener2.automaticSettingChangeCalled);
    }

    @Test
    public void duplicateListenersNotAdded() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.addListener(Name.COSMETIC_RANDOM_INTRO_MON, listener);
        manager.addListener(Name.COSMETIC_RANDOM_INTRO_MON, listener);
        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assumeTrue(listener.manualSettingChangeCalled);
        assert listener.manualChangeCallCount == 1;

        listener.reset();
        manager.removeListener(Name.COSMETIC_RANDOM_INTRO_MON, listener);
        assumeFalse(listener.manualSettingChangeCalled);
        assumeTrue(listener.manualChangeCallCount == 0);

        manager.addUniversalListener(listener);
        manager.addUniversalListener(listener);
        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, false);
        assumeTrue(listener.manualSettingChangeCalled);
        assert listener.manualChangeCallCount == 1;
    }

    @Test
    public void resetAllWorks() {
        SettingsManager manager = new SettingsManager();
        manager.set(Name.UPDATE_MOVES, true);

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 8);
        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);

        boolean boolValue = manager.get(Name.COSMETIC_RANDOM_INTRO_MON);
        int intValue = manager.get(Name.UPDATE_MOVES_TO_GENERATION);
        Settings.BSTMod enumValue = manager.get(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS);

        assumeTrue(boolValue);
        assumeTrue(intValue == 8);
        assumeTrue(enumValue == Settings.BSTMod.RANDOM);

        manager.resetAll();

        boolValue = manager.get(Name.COSMETIC_RANDOM_INTRO_MON);
        intValue = manager.get(Name.UPDATE_MOVES_TO_GENERATION);
        enumValue = manager.get(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS);

        assert (boolValue != true);
        assert (intValue != 8);
        assert (enumValue != Settings.BSTMod.RANDOM);
    }

    @Test
    public void listenersCalledOnReset() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.UPDATE_MOVES, true);
        manager.addUniversalListener(listener);

        manager.resetAll();

        assert listener.manualSettingChangeCalled;
        assert listener.possibleEnablementChangeCalled;

        manager.removeUniversalListener(listener);
        listener.reset();
        manager.set(Name.UPDATE_MOVES, true);
        manager.addListener(Name.UPDATE_MOVES, listener);

        manager.resetAll();

        assert listener.manualSettingChangeCalled;
        assert !listener.possibleEnablementChangeCalled;
    }

    @Test
    public void automaticChangeListenerNotCalledOnReset() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();

        manager.set(Name.UPDATE_MOVES, true);
        manager.addUniversalListener(listener);

        manager.resetAll();
        assert !listener.automaticSettingChangeCalled;

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM_BUFF_NERF);
        manager.set(Name.SPECIES_BST_RANDOM_BUFF_NERF_PERCENTAGE, 22);
        listener.reset();

        manager.resetAll();
        assert !listener.automaticSettingChangeCalled;

        manager.removeUniversalListener(listener);
        listener.reset();
        manager.set(Name.UPDATE_MOVES, true);
        manager.addListener(Name.UPDATE_MOVES, listener);

        manager.resetAll();

        assert !listener.automaticSettingChangeCalled;
    }

    @Test
    public void listenersNotCalledWhenNotChangingValue() {
        SettingsManager manager = new SettingsManager();
        TestSettingsListener listener = new TestSettingsListener();
        manager.addUniversalListener(listener);
        manager.set(Name.UPDATE_MOVES, false);

        assert !listener.manualSettingChangeCalled;
        assert !listener.automaticSettingChangeCalled;
        assert !listener.possibleEnablementChangeCalled;

        manager.set(Name.UPDATE_MOVES, true);
        assert !listener.automaticSettingChangeCalled;
        assumeTrue(listener.manualSettingChangeCalled);
        assumeTrue(listener.possibleEnablementChangeCalled);

        listener.reset();
        manager.set(Name.UPDATE_MOVES, true);
        assert !listener.manualSettingChangeCalled;
        assert !listener.automaticSettingChangeCalled;
        assert !listener.possibleEnablementChangeCalled;

        manager.set(Name.UPDATE_MOVES, false);
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
