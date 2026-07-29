package com.uprfvx.random.settings.restrictions;

/**
 * A setting restriction which tests whether the state of an enum setting matches the relevant value.
 */
public class EnumMatchRestriction<E extends Enum<E>> extends SimpleSettingRestriction<E> {

    /**
     * Creates a new EnumMatchRestriction, which tests whether the state of an enum setting matches the desired value.
     * Returns true if it does. To invert, use the other constructor.
     * @param name The setting to test.
     * @param desiredValue The desired value for the setting.
     */
    public EnumMatchRestriction(String name, E desiredValue) {
        super(name, e -> e == desiredValue);
    }


    /**
     * Creates a new EnumMatchRestriction, which tests whether the state of an enum setting matches the relevant value.
     * Can be inverted to return true if the setting does NOT match the relevant value.
     * @param name The setting to test.
     * @param relevant The desired value for the setting.
     * @param shouldMatch Whether to return true if the setting matches the relevant state (true)
     *                    or if it does NOT match the relevant state (false).
     */
    public EnumMatchRestriction(String name, E relevant, boolean shouldMatch) {
        super(name, shouldMatch ?
                e -> e == relevant :
                e -> e != relevant );
    }
}
