package com.uprfvx.random.settings.restrictions;

/**
 * A setting restriction which tests whether the state of an enum setting matches the desired value.
 */
public class EnumMatchRestriction<E extends Enum<E>> extends SimpleSettingRestriction<E> {

    /**
     * Creates a new EnumMatchRestriction, which tests whether the state of an enum setting matches the desired value.
     * @param name The setting to test.
     * @param desiredValue The desired value for the setting.
     */
    public EnumMatchRestriction(String name, E desiredValue) {
        super(name, e -> e == desiredValue);
    }
}
