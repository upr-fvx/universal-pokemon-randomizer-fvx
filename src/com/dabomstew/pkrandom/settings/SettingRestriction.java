package com.dabomstew.pkrandom.settings;

import java.util.List;

/**
 * Presents a standardized way to represent a decision based on one or more settings.
 * Used internally to prevent nonsensical setting states.
 */
public interface SettingRestriction {

    /**
     * Gets the names of all settings which affect whether this restriction returns true or false.
     * @return The names of all relevant settings.
     */
    List<String> getRelevantSettingNames();

    /**
     * Tests the restriction against the given SettingsManager.
     * @param manager The SettingsManager holding the relevant settings' states.
     * @return Whether the restriction holds given the current setting states.
     */
    boolean test(SettingsManager manager);
}
