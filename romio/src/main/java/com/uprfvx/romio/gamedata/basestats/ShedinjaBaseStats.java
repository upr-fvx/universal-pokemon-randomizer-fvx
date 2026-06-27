package com.uprfvx.romio.gamedata.basestats;

/**
 * A {@link BaseStats} for Shedinja. Unlike all other mons,
 * it has a hard-coded HP of exactly 1. In other words,
 * it does not really have an HP stat. This class reflects that.
 */
public class ShedinjaBaseStats extends BaseStats {

    public ShedinjaBaseStats(int attack, int defense, int spatk, int spdef, int speed) {
        super(0, attack, defense, spatk, spdef, speed);
        this.bst = attack + defense + spatk + spdef + speed;
        calculateStats();
    }

    public ShedinjaBaseStats(ShedinjaBaseStats original) {
        super(original);
    }

    @Override
    public int getHp() {
        // getHp() is used in various places just for checking "is this mon HP-rich?" and for logging.
        // Even though Shedinja doesn't really have an HP stat of 1 (or an HP stat at all),
        // this is a good enough answer in those cases, to avoid the special handling required
        // if we were to throw here instead.
        return 1;
    }

    @Override
    public int getBST() {
        // * 6/5 to reflect that Shedinja is much stronger than its BST would otherwise reflect;
        // and almost every BST usage is for gauging power levels.
        return (getAttack() + getDefense() + getSpatk() + getSpdef() + getSpeed()) * 6 / 5;
    }

    @Override
    public void setBST(int bst) {
        this.bst = bst * 5 / 6;
        calculateStats();
    }

    @Override
    public void setStatRatios(double hp, double attack, double defense, double spatk, double spdef, double speed) {
        throw new UnsupportedOperationException("Use the ShedinjaBaseStats-specific setStatRatios instead.");
    }

    public void setStatRatios(double attack, double defense, double spatk, double spdef, double speed) {
        positiveCheck(attack, "attack");
        positiveCheck(defense, "defense");
        positiveCheck(spatk, "spatk");
        positiveCheck(spdef, "spdef");
        positiveCheck(speed, "speed");
        this.attackRatio = attack;
        this.defenseRatio = defense;
        this.spatkRatio = spatk;
        this.spdefRatio = spdef;
        this.speedRatio = speed;
        calculateStats();
    }

    @Override
    protected double[] calculateRawStats() {
        double total = attackRatio + defenseRatio + spatkRatio + spdefRatio + speedRatio;
        return new double[]{
                bst * (attackRatio / total),
                bst * (defenseRatio / total),
                bst * (spatkRatio / total),
                bst * (spdefRatio / total),
                bst * (speedRatio / total)
        };
    }

    @Override
    protected void assignCalculatedStats(int[] stats) {
        attack = stats[0];
        defense = stats[1];
        spatk = stats[2];
        spdef = stats[3];
        speed = stats[4];
    }

    @Override
    public String toString() {
        // designed to easily fit into Species toString()
        return "hp=1(Shedinja), attack=" + getAttack() + ", defense=" + getDefense() + ", spatk=" + getSpatk()
                + ", spdef=" + getSpdef() + ", speed=" + getSpeed();
    }
}
