package com.uprfvx.random.settings.restrictions;

/**
 * A setting restriction which tests whether the state of an enum setting does NOT match the undesired value.
 */
public class EnumNotMatchRestriction<E extends Enum<E>> extends SimpleSettingRestriction<E> {

    /**
     * Creates a new EnumMatchRestriction, which tests whether the state of an enum setting does NOT match the undesired value.
     * @param name The setting to test.
     * @param undesired The undesired value for the setting.
     */
    public EnumNotMatchRestriction(String name, E undesired) {
        super(name, e -> e != undesired);
    }
}
