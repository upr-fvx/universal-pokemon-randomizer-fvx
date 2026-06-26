package com.uprfvx.romio.gamedata;

/**
 * The {@link BaseStats} for a Gen 1 {@link Species}. Accounts for the combined special stat.
 */
public class Gen1BaseStats extends BaseStats {

    private final int special;

    public Gen1BaseStats(int hp, int attack, int defense, int speed, int special) {
        super(hp, attack, defense, 0, 0, speed);
        rangeCheck(special, "special");
        this.special = special;
    }

    public Gen1BaseStats(Gen1BaseStats original) {
        super(original);
        this.special = original.special;
    }

    public int getSpecial() {
        return special;
    }

    @Override
    public int getSpatk() {
        throw new UnsupportedOperationException("Not a valid Gen 1 stat");
    }

    @Override
    public int getSpdef() {
        throw new UnsupportedOperationException("Not a valid Gen 1 stat");
    }

    @Override
    public int getBST() {
        return getHp() + getAttack() + getDefense() + getSpecial() + getSpeed();
    }

    @Override
    public void setBST(int bst) {
        // TODO: implement
    }

    @Override
    public boolean setStatRatios(double hp, double attack, double defense, double spatk, double spdef, double speed) {
        throw new UnsupportedOperationException("Use the Gen1BaseStats-specific setStatRatios instead.");
    }

    /**
     * A Gen 1 version of {@link #setStatRatios(double, double, double, double, double, double)},
     * with the combined special stat.
     */
    public boolean setStatRatios(double hp, double attack, double defense, double speed, double special) {
        // TODO: implement
        return false;
    }

    @Override
    public double getAttackSpecialAttackRatio() {
        return (double) getAttack() / ((double) getAttack() + (double) getSpecial());
    }

    @Override
    public String toString() {
        // designed to easily fit into Species toString()
        return "hp=" + getHp() + ", attack=" + getAttack() + ", defense=" + getDefense() + ", speed=" + getSpeed()
        + ", special=" + getSpecial();
    }
}
