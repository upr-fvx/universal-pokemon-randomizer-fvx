package com.uprfvx.random.settings;

import com.uprfvx.random.settings.Settings.Name;
import com.uprfvx.random.settings.restrictions.MultiSettingRestriction;
import com.uprfvx.random.settings.restrictions.SimpleSettingRestriction;
import org.junit.jupiter.api.Test;

import static com.uprfvx.random.settings.SettingUtils.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


public class SettingRestrictionsTest {

    @Test
    public void simpleBooleanRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        SimpleSettingRestriction<Boolean> restriction = new SimpleSettingRestriction<>(Name.COSMETIC_RANDOM_INTRO_MON, isTrue);
        assertFalse(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        boolean value = manager.get(Name.COSMETIC_RANDOM_INTRO_MON);
        assumeTrue(value);
        assertTrue(restriction.test(manager));
    }

    @Test
    public void simpleIntegerRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        manager.set(Name.UPDATE_MOVES, true);
        SimpleSettingRestriction<Integer> restriction = new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION,
                lessThanValue(8));

        assertFalse(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 8);
        assertFalse(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assertTrue(restriction.test(manager));
    }

    @Test
    public void simpleEnumRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        SimpleSettingRestriction<Settings.BSTMod> restriction = new SimpleSettingRestriction<>(
                Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                matchesEnum(Settings.BSTMod.SHUFFLE));

        assertFalse(restriction.test(manager));

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assertFalse(restriction.test(manager));

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assertTrue(restriction.test(manager));

        restriction = new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                notMatchesEnum(Settings.BSTMod.SHUFFLE));

        assertFalse(restriction.test(manager));

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assertTrue(restriction.test(manager));

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.UNCHANGED);
        assertTrue(restriction.test(manager));
    }

    @Test
    public void multiRestrictionOrWorks() {
        SettingsManager manager = new SettingsManager();
        manager.set(Name.UPDATE_MOVES, true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(true, false,
                new SimpleSettingRestriction<>(Name.COSMETIC_RANDOM_INTRO_MON, isTrue),
                new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION, lessThanValue(8)),
                new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        matchesEnum(Settings.BSTMod.SHUFFLE))
        );

        assertFalse(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assertTrue(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assertTrue(restriction.test(manager));

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assertTrue(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, false);
        assertTrue(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 9);
        assertTrue(restriction.test(manager));
    }

    @Test
    public void multiRestrictionAndWorks() {
        SettingsManager manager = new SettingsManager();
        manager.set(Name.UPDATE_MOVES, true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(false, false,
                new SimpleSettingRestriction<>(Name.COSMETIC_RANDOM_INTRO_MON, isTrue),
                new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION, lessThanValue(8)),
                new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        matchesEnum(Settings.BSTMod.SHUFFLE))
        );

        assertFalse(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assertFalse(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assertFalse(restriction.test(manager));

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assertTrue(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, false);
        assertFalse(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 9);
        assertFalse(restriction.test(manager));
    }

    @Test
    public void multiRestrictionNorWorks() {
        SettingsManager manager = new SettingsManager();
        manager.set(Name.UPDATE_MOVES, true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(true, true,
                new SimpleSettingRestriction<>(Name.COSMETIC_RANDOM_INTRO_MON, isTrue),
                new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION, lessThanValue(8)),
                new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        matchesEnum(Settings.BSTMod.SHUFFLE))
        );

        assertTrue(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assertFalse(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assertFalse(restriction.test(manager));

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assertFalse(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, false);
        assertFalse(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 9);
        assertFalse(restriction.test(manager));
    }

    @Test
    public void multiRestrictionNandWorks() {
        SettingsManager manager = new SettingsManager();
        manager.set(Name.UPDATE_MOVES, true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(false, true,
                new SimpleSettingRestriction<>(Name.COSMETIC_RANDOM_INTRO_MON, isTrue),
                new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION, lessThanValue(8)),
                new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        matchesEnum(Settings.BSTMod.SHUFFLE))
        );

        assertTrue(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, true);
        assertTrue(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assertTrue(restriction.test(manager));

        manager.set(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assertFalse(restriction.test(manager));

        manager.set(Name.COSMETIC_RANDOM_INTRO_MON, false);
        assertTrue(restriction.test(manager));

        manager.set(Name.UPDATE_MOVES_TO_GENERATION, 9);
        assertTrue(restriction.test(manager));
    }
}
