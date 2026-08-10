package com.uprfvx.random.settings;

import com.uprfvx.random.settings.restrictions.EnumMatchRestriction;
import com.uprfvx.random.settings.restrictions.MultiSettingRestriction;
import com.uprfvx.random.settings.restrictions.SimpleSettingRestriction;
import org.junit.jupiter.api.Test;

import static com.uprfvx.random.settings.SettingUtils.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


public class SettingRestrictionsTest {

    @Test
    public void simpleBooleanRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        SimpleSettingRestriction<Boolean> restriction = new SimpleSettingRestriction<>("NoRandomIntroMon", isTrue);
        assert(restriction.test(manager) == false);

        manager.setSetting("NoRandomIntroMon", true);
        boolean value = manager.getSetting("NoRandomIntroMon");
        assumeTrue(value);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void simpleIntegerRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting("UpdateMoves", true);
        SimpleSettingRestriction<Integer> restriction = new SimpleSettingRestriction<>("UpdateMovesToGeneration",
                lessThanValue(8));

        assert(restriction.test(manager) == false);

        manager.setSetting("UpdateMovesToGeneration", 8);
        assert(restriction.test(manager) == false);

        manager.setSetting("UpdateMovesToGeneration", 6);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void simpleEnumRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        SimpleSettingRestriction<Settings.BSTMod> restriction = new SimpleSettingRestriction<>(
                Settings.Names.RANDOMIZE_BASE_STAT_TOTALS,
                matchesEnumValue(Settings.BSTMod.SHUFFLE));

        assert(restriction.test(manager) == false);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(restriction.test(manager) == false);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == true);

        restriction = new SimpleSettingRestriction<>(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS,
                doesNotMatchEnumValue(Settings.BSTMod.SHUFFLE));

        assert(restriction.test(manager) == false);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(restriction.test(manager) == true);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.UNCHANGED);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void enumMatchRestrictionWorks() {
        SettingsManager manager = new SettingsManager();
        SimpleSettingRestriction<Settings.BSTMod> restriction = new EnumMatchRestriction<>(
                Settings.Names.RANDOMIZE_BASE_STAT_TOTALS,
                Settings.BSTMod.SHUFFLE);

        assert(restriction.test(manager) == false);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(restriction.test(manager) == false);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == true);

        restriction = new EnumMatchRestriction<>(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS,
                Settings.BSTMod.SHUFFLE, false);

        assert(restriction.test(manager) == false);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.RANDOM);
        assert(restriction.test(manager) == true);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.UNCHANGED);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void multiRestrictionOrWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting("UpdateMoves", true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(true, false,
                new SimpleSettingRestriction<>("NoRandomIntroMon", isTrue),
                new SimpleSettingRestriction<>("UpdateMovesToGeneration", lessThanValue(8)),
                new EnumMatchRestriction<>(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE)
        );

        assert(restriction.test(manager) == false);

        manager.setSetting("NoRandomIntroMon", true);
        assert(restriction.test(manager) == true);

        manager.setSetting("UpdateMovesToGeneration", 6);
        assert(restriction.test(manager) == true);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == true);

        manager.setSetting("NoRandomIntroMon", false);
        assert(restriction.test(manager) == true);

        manager.setSetting("UpdateMovesToGeneration", 9);
        assert(restriction.test(manager) == true);
    }

    @Test
    public void multiRestrictionAndWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting("UpdateMoves", true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(false, false,
                new SimpleSettingRestriction<>("NoRandomIntroMon", isTrue),
                new SimpleSettingRestriction<>("UpdateMovesToGeneration", lessThanValue(8)),
                new EnumMatchRestriction<>(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE)
        );

        assert(restriction.test(manager) == false);

        manager.setSetting("NoRandomIntroMon", true);
        assert(restriction.test(manager) == false);

        manager.setSetting("UpdateMovesToGeneration", 6);
        assert(restriction.test(manager) == false);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == true);

        manager.setSetting("NoRandomIntroMon", false);
        assert(restriction.test(manager) == false);

        manager.setSetting("UpdateMovesToGeneration", 9);
        assert(restriction.test(manager) == false);
    }

    @Test
    public void multiRestrictionNorWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting("UpdateMoves", true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(true, true,
                new SimpleSettingRestriction<>("NoRandomIntroMon", isTrue),
                new SimpleSettingRestriction<>("UpdateMovesToGeneration", lessThanValue(8)),
                new EnumMatchRestriction<>(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE)
        );

        assert(restriction.test(manager) == true);

        manager.setSetting("NoRandomIntroMon", true);
        assert(restriction.test(manager) == false);

        manager.setSetting("UpdateMovesToGeneration", 6);
        assert(restriction.test(manager) == false);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == false);

        manager.setSetting("NoRandomIntroMon", false);
        assert(restriction.test(manager) == false);

        manager.setSetting("UpdateMovesToGeneration", 9);
        assert(restriction.test(manager) == false);
    }

    @Test
    public void multiRestrictionNandWorks() {
        SettingsManager manager = new SettingsManager();
        manager.setSetting("UpdateMoves", true);
        MultiSettingRestriction restriction = new MultiSettingRestriction(false, true,
                new SimpleSettingRestriction<>("NoRandomIntroMon", isTrue),
                new SimpleSettingRestriction<>("UpdateMovesToGeneration", lessThanValue(8)),
                new EnumMatchRestriction<>(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE)
        );

        assert(restriction.test(manager) == true);

        manager.setSetting("NoRandomIntroMon", true);
        assert(restriction.test(manager) == true);

        manager.setSetting("UpdateMovesToGeneration", 6);
        assert(restriction.test(manager) == true);

        manager.setSetting(Settings.Names.RANDOMIZE_BASE_STAT_TOTALS, Settings.BSTMod.SHUFFLE);
        assert(restriction.test(manager) == false);

        manager.setSetting("NoRandomIntroMon", false);
        assert(restriction.test(manager) == true);

        manager.setSetting("UpdateMovesToGeneration", 9);
        assert(restriction.test(manager) == true);
    }
}
