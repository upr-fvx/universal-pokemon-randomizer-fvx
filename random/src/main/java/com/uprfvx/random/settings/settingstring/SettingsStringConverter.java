package com.uprfvx.random.settings.settingstring;

// TODO: what is the version?

import com.uprfvx.random.Version;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.settings.SettingsUpdater;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Used to convert settings from the "settings string" format
 * used in FVX v1.6.0 into the modern format used in FVX v[VERSION].
 * <br><br>
 * This class only converts from FVX v1.6.0 into FVX v[VERSION].<br>
 * To convert an older settings string into the format of FVX v1.6.0,
 * use {@link SettingsStringUpdater}.<br>
 * To convert FVX v[VERSION] settings into those of the current version,
 * use {@link SettingsUpdater}.
 * <br><br>
 * The "settings string" format is an old format the Randomizer used
 * to use for settings, a binary format that could be written as a
 * (relatively) short base64 string. It had the advantage of being
 * very brief, but was opaque and unwieldy to work with.
 */
public class SettingsStringConverter {

    private static final int VERSION_ID_LENGTH = 3;

    private static boolean restoreState(byte b, int index) {
        if (index >= 8) {
            throw new IllegalArgumentException("Can't read more than 8 bits from a byte!");
        }

        int value = b & 0xFF;
        return ((value >> index) & 0x01) == 0x01;
    }

    private SettingsManager manager;
    private byte[] data;

    /**
     * Takes a {@link SettingsManager} and a FVX v1.6.0 settings string (including version),
     * and converts the settings stored in the latter to populate the former.<br>
     * <b>NOTE:</b> this converts the settings into that of FVX v[VERSION]. Use {@link SettingsUpdater}
     * to get settings that match the current version.
     * @throws NullPointerException if <code>manager</code> or <code>stringWithVersion</code> are null.
     * @throws IllegalArgumentException if the settings string is not of FVX v1.6.0, or is otherwise invalid.
     */
    public void convertAndPopulate(SettingsManager manager, String stringWithVersion) {
        if (manager == null) {
            throw new NullPointerException("manager cannot be null.");
        }
        this.manager = manager;

        if (stringWithVersion == null) {
            throw new NullPointerException("stringWithVersion cannot be null.");
        }
        String versionChars = stringWithVersion.substring(0, VERSION_ID_LENGTH);
        int versionID;
        try {
            versionID = Integer.parseInt(versionChars);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid version id: " + versionChars);
        }
        if (versionID >= Version.FVX_1_6_0.id) {
            throw new IllegalArgumentException("Version id does not match that of FVX v1.6.0\n." +
                    "\tExpected=" + Version.FVX_1_6_0.id + ", Was=" + versionID);
        }

        String withoutVersion = stringWithVersion.substring(VERSION_ID_LENGTH);
        this.data = Base64.getDecoder().decode(withoutVersion);

        // Byte 0: Misc / Species Evolutions
        loadSetting(Settings.Name.SPECIES_EVOLUTIONS_MAKE_POSSIBLE, 0, 0);
        loadSetting(Settings.Name.UPDATE_MOVES, 0, 1);
        // (0, 2) unused
        loadSetting(Settings.Name.TRAINERS_RANDOMIZE_NAMES, 0, 3);
        loadSetting(Settings.Name.TRAINERS_RANDOMIZE_CLASS_NAMES, 0, 4);
        loadSetting(Settings.Name.SPECIES_EVOLUTIONS_MAKE_EASIER, 0, 5);
        loadSetting(Settings.Name.SPECIES_EVOLUTIONS_REMOVE_TIME_BASED, 0, 6);
        loadSetting(Settings.Name.SPECIES_EVOLUTIONS_CHANGES_USE_ESTIMATED_LEVELS, 0, 7);

        // Byte 1: Species Base Stats
        loadSetting(Settings.Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_EVOLUTIONS, 1, 0);
        loadEnumSetting(Settings.Name.RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS, 1,
                Map.of(
                        Settings.BaseStatDistributionsMod.RANDOM, 1,
                        Settings.BaseStatDistributionsMod.SHUFFLE, 2,
                        Settings.BaseStatDistributionsMod.UNCHANGED, 3
                ));
        loadSetting(Settings.Name.STANDARDIZE_SPECIES_EXP_CURVES, 1, 4);
        loadSetting(Settings.Name.UPDATE_SPECIES_BASE_STATS, 1, 5);
        loadSetting(Settings.Name.SPECIES_STAT_DISTRIBUTIONS_FOLLOW_MEGA_EVOLUTIONS, 1, 6);
        loadSetting(Settings.Name.SPECIES_STAT_DISTRIBUTIONS_ASSIGN_EVO_STATS_RANDOMLY, 1, 7);

        // Byte 2: Species Abilities

        // TODO: fill in rest
    }

    public void loadSetting(Settings.Name name, int byteNum, int bitNum) {
        manager.setSetting(name, restoreState(data[byteNum], bitNum));
    }

    public <E extends Enum<E>> void loadEnumSetting(Settings.Name name, int byteNum, Map<E, Integer> map) {
        List<E> enabledValues = map.keySet().stream()
                .filter(key -> restoreState(data[byteNum], map.get(key)))
                .distinct()
                .toList();
        if (enabledValues.size() != 1) {
            throw new IllegalStateException("Cannot load setting " + name + " from string. " +
                    "Multiple or zero values are enabled at once: " + enabledValues);
        }

        E value = enabledValues.getFirst();
        manager.setSetting(name, value);
    }

}
