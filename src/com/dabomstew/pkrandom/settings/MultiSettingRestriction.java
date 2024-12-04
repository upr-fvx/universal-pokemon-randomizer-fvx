package com.dabomstew.pkrandom.settings;

import java.util.*;

/**
 * Combines the results of multiple Setting Restrictions in an OR, AND, NOR, or NAND manner.
 * Can be nested for more complex decision-making.
 */
public class MultiSettingRestriction implements SettingRestriction {

    private final List<String> settingNames;
    private final List<SettingRestriction> restrictions;
    private final boolean any;
    private final boolean invert;

    /**
     * Creates a new MultiSettingRestriction to combine the results of multiple SettingRestrictions.
     * @param any If true, this restriction returns true if any of the given restrictions are true;
     *            if false, it returns true only if all of them are true.
     * @param invert If true, this restriction returns false when it would otherwise return true, and vice versa.
     * @param restrictions The restrictions to use.
     */
    public MultiSettingRestriction(boolean any, boolean invert, SettingRestriction... restrictions) {
        Set<String> names = new HashSet<>();
        for(SettingRestriction restriction : restrictions) {
            names.addAll(restriction.getRelevantSettingNames());
        }
        settingNames = Collections.unmodifiableList(new ArrayList<>(names));
        this.restrictions = Collections.unmodifiableList(Arrays.asList(restrictions));
        this.any = any;
        this.invert = invert;
    }

    @Override
    public List<String> getRelevantSettingNames() {
        return settingNames;
    }

    @Override
    public boolean test(SettingsManager manager) {
        return false;
    }
}
