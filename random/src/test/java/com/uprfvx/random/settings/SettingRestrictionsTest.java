package com.uprfvx.random.settings;

import com.uprfvx.random.settings.Settings.Name;
import com.uprfvx.random.settings.restrictions.MultiSettingRestriction;
import com.uprfvx.random.settings.restrictions.SimpleSettingRestriction;
import org.junit.jupiter.api.Test;

import static com.uprfvx.random.settings.SettingUtils.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


public class SettingRestrictionsTest {

    @Test
    public void simpleBooleanRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        SimpleSettingRestriction<Boolean> restriction = new SimpleSettingRestriction<>(Name.NO_RANDOM_INTRO_MON, isTrue);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, true);
        boolean value = manager.getSetting(Name.NO_RANDOM_INTRO_MON);
        assumeTrue(value);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void simpleIntegerRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting(Name.UPDATE_MOVES, true);
        SimpleSettingRestriction<Integer> restriction = new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION,
                lessThanValue(8));

        assert(restriction.test(manager) == false);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 8);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void simpleEnumRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        SimpleSettingRestriction<Settings.BSTMod> restriction = new SimpleSettingRestriction<>(
                Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                matchesEnum(Settings.BSTMod.SHUFFLE));

        assert(restriction.test(manager) == false);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == true);

        restriction = new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                notMatchesEnum(Settings.BSTMod.SHUFFLE));

        assert(restriction.test(manager) == false);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.UNCHANGED);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void enumMatchRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        SimpleSettingRestriction<Settings.BSTMod> restriction = new SimpleSettingRestriction<>(
                Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                matchesEnum(Settings.BSTMod.SHUFFLE));

        assert(restriction.test(manager) == false);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == true);

        restriction = new EnumMatchRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                notMatchesEnum(Settings.BSTMod.SHUFFLE));

        assert(restriction.test(manager) == false);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.UNCHANGED);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void multiRestrictionOrWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting(Name.UPDATE_MOVES, true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(true, false,
                new SimpleSettingRestriction<>(Name.NO_RANDOM_INTRO_MON, isTrue),
                new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION, lessThanValue(8)),
                new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        matchesEnum(Settings.BSTMod.SHUFFLE))
        );

        assert(restriction.test(manager) == false);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, true);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, false);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 9);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void multiRestrictionAndWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting(Name.UPDATE_MOVES, true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(false, false,
                new SimpleSettingRestriction<>(Name.NO_RANDOM_INTRO_MON, isTrue),
                new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION, lessThanValue(8)),
                new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        matchesEnum(Settings.BSTMod.SHUFFLE))
        );

        assert(restriction.test(manager) == false);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, true);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, false);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 9);
        assert(restriction.test(manager) == false);
    }

    @Test
    public void multiRestrictionNorWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting(Name.UPDATE_MOVES, true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(true, true,
                new SimpleSettingRestriction<>(Name.NO_RANDOM_INTRO_MON, isTrue),
                new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION, lessThanValue(8)),
                new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        matchesEnum(Settings.BSTMod.SHUFFLE))
        );

        assert(restriction.test(manager) == true);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, true);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, false);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 9);
        assert(restriction.test(manager) == false);
    }

    @Test
    public void multiRestrictionNandWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting(Name.UPDATE_MOVES, true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(false, true,
                new SimpleSettingRestriction<>(Name.NO_RANDOM_INTRO_MON, isTrue),
                new SimpleSettingRestriction<>(Name.UPDATE_MOVES_TO_GENERATION, lessThanValue(8)),
                new SimpleSettingRestriction<>(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS,
                        matchesEnum(Settings.BSTMod.SHUFFLE))
        );

        assert(restriction.test(manager) == true);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, true);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 6);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == false);

        manager.setSetting(Name.NO_RANDOM_INTRO_MON, false);
        assert(restriction.test(manager) == true);

        manager.setSetting(Name.UPDATE_MOVES_TO_GENERATION, 9);
        assert(restriction.test(manager) == true);
    }
}
