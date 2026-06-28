package com.uprfvx.romio.gamedata.basestats;

import com.uprfvx.romio.gamedata.Species;

/**
 * The {@link BaseStats} for a Gen 1 {@link Species}. Accounts for the combined special stat.
 */
public class Gen1BaseStats extends BaseStats {

    protected double specialRatio;

    private int special;

    public Gen1BaseStats(int hp, int attack, int defense, int speed, int special) {
        super(hp, attack, defense, 0, 0, speed);

        rangeCheck(special, "special");
        this.bst = hp + attack + defense + speed + special;
        this.specialRatio = special;

        calculateStats();
    }

    public Gen1BaseStats(Gen1BaseStats original) {
        super(original);
        this.specialRatio = original.specialRatio;
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
    public double getAttackSpecialAttackRatio() {
        return (double) getAttack() / ((double) getAttack() + (double) getSpecial());
    }

    @Override
    public int getMaxBST() {
        return STAT_MAX * 5;
    }

    @Override
    public int getBST() {
        return getHp() + getAttack() + getDefense() + getSpecial() + getSpeed();
    }

    @Override
    public void setStatRatios(double hp, double attack, double defense, double spatk, double spdef, double speed) {
        throw new UnsupportedOperationException("Use the Gen1BaseStats-specific setStatRatios instead.");
    }

    /**
     * A Gen 1 version of {@link #setStatRatios(double, double, double, double, double, double)},
     * with the combined special stat.
     */
    public void setStatRatios(double hp, double attack, double defense, double speed, double special) {
        positiveCheck(hp, "hp");
        positiveCheck(attack, "attack");
        positiveCheck(defense, "defense");
        positiveCheck(speed, "speed");
        positiveCheck(special, "special");
        this.hpRatio = hp;
        this.attackRatio = attack;
        this.defenseRatio = defense;
        this.speedRatio = speed;
        this.specialRatio = special;
        calculateStats();
    }

    @Override
    protected double[] getStatRatios() {
        return new double[]{hpRatio, attackRatio, defenseRatio, speedRatio, specialRatio};
    }

    @Override
    protected void assignCalculatedStats(int[] stats) {
        hp = stats[0];
        attack = stats[1];
        defense = stats[2];
        speed = stats[3];
        special = stats[4];
    }

    @Override
    public String toString() {
        // designed to easily fit into Species toString()
        return "hp=" + getHp() + ", attack=" + getAttack() + ", defense=" + getDefense() + ", speed=" + getSpeed()
        + ", special=" + getSpecial();
    }
}
