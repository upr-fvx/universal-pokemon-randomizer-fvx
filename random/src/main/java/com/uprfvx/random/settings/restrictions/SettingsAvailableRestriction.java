package com.uprfvx.random.settings.restrictions;

import com.uprfvx.random.settings.Settings;

import static com.uprfvx.random.settings.SettingUtils.isFalse;

/**
 * A restriction which only tests true if any of the given settings is both supported and false.<br>
 * Useful for groups of options that each ban a sub-set of a whole, to prevent them from all being selected at once.
 */
public class SettingsAvailableRestriction extends MultiSettingRestriction {

    /**
     * Creates a new {@link SettingsAvailableRestriction}.
     * @param names The names of boolean-value settings.
     */
    public SettingsAvailableRestriction(Settings.Name... names) {
        SettingRestriction[] restrictions = new SettingRestriction[names.length];
        for (int i = 0; i < restrictions.length; i++) {
            restrictions[i] = new MultiSettingRestriction(false, false,
                    new SimpleSettingRestriction<>(names[i], isFalse),
                    new SettingSupportRestriction(names[i], true)
            );
        }
        super(true, false, restrictions);
    }
}
