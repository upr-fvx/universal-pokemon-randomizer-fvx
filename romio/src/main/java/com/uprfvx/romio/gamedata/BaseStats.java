package com.uprfvx.romio.gamedata;

/**
 * The base stats of a {@link Species}. HP, attack, etc...
 */
public class BaseStats {

    private final int hp;
    private final int attack;
    private final int defense;
    private final int spatk;
    private final int spdef;
    private final int speed;

    private final boolean isShedinja;

    public BaseStats(int hp, int attack, int defense, int spatk, int spdef, int speed, boolean isShedinja) {
        rangeCheck(hp, "hp");
        rangeCheck(attack, "attack");
        rangeCheck(defense, "defense");
        rangeCheck(spatk, "spatk");
        rangeCheck(spdef, "spdef");
        rangeCheck(speed, "speed");
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.spatk = spatk;
        this.spdef = spdef;
        this.speed = speed;
        this.isShedinja = isShedinja;
    }

    protected void rangeCheck(int val, String name) {
        if (val < 0 || val > 255) {
            throw new IllegalArgumentException(name + " must be between 0-255. Was: " + val);
        }
    }

    public BaseStats(BaseStats original) {
        this.isShedinja = original.isShedinja;
        this.hp = original.hp;
        this.attack = original.attack;
        this.defense = original.defense;
        this.spatk = original.spatk;
        this.spdef = original.spdef;
        this.speed = original.speed;
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpatk() {
        return spatk;
    }

    public int getSpdef() {
        return spdef;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isShedinja() {
        return isShedinja;
    }

    /**
     * Gets the raw Base Stat Total. In most cases, {@link #getBSTForPowerLevels()}
     * should be used instead.
     */
    public int getBST() {
        return hp + attack + defense + spatk + spdef + speed;
    }

    /**
     * Gets the Base Stat Total, taking Shedinja's purposefully nerfed HP into account.
     */
    public int getBSTForPowerLevels() {
        if (isShedinja) {
            return (attack + defense + spatk + spdef + speed) * 6 / 5;
        } else {
            return getBST();
        }
    }

    /**
     * Modifies the stats by setting the BST. The ratios of each stat will be the same, as far as possible.<br>
     * This is "for power levels" since it takes Shedinja's purposefully nerfed HP into account. It thus mirrors
     * {@link #getBSTForPowerLevels()}.
     */
    public void setBSTForPowerLevels(int bst) {
        // TODO: implement
    }

    /**
     * Modifies the stats by setting the ratios of each stat. The BST is guaranteed to be untouched.<br>
     * Ensures every stat is at least 1.
     * <br><br>
     * Returns false if any of the stats would be above 255, or above 1.
     */
    public boolean setStatRatios(double hp, double attack, double defense, double spatk, double spdef, double speed) {
        // TODO: implement
        return false;
    }

    /**
     * Doesn't actually return attack/spatk. Returns attack/(attack + spatk).
     */
    public double getAttackSpecialAttackRatio() {
        return (double)attack / ((double)attack + (double)spatk);
    }

    @Override
    public String toString() {
        // designed to easily fit into Species toString()
        return "hp=" + getHp() + ", attack=" + getAttack() + ", defense=" + getDefense() + ", spatk=" + getSpatk()
                + ", spdef=" + getSpdef() + ", speed=" + getSpeed();
    }
}
