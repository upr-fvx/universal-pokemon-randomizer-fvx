package com.uprfvx.romio.gamedata;

import java.util.Arrays;

/**
 * The base stats of a {@link Species}. HP, attack, etc...
 */
public class BaseStats {
    
    private static final int STAT_MAX = 255;

    private int bst;

    private double hpRatio;
    private double attackRatio;
    private double defenseRatio;
    private double spatkRatio;
    private double spdefRatio;
    private double speedRatio;

    private int hp;
    private int attack;
    private int defense;
    private int spatk;
    private int spdef;
    private int speed;

    public BaseStats(int hp, int attack, int defense, int spatk, int spdef, int speed) {
        rangeCheck(hp, "hp");
        rangeCheck(attack, "attack");
        rangeCheck(defense, "defense");
        rangeCheck(spatk, "spatk");
        rangeCheck(spdef, "spdef");
        rangeCheck(speed, "speed");

        this.bst = hp + attack + defense + spatk + spdef + speed;

        this.hpRatio = hp;
        this.attackRatio = attack;
        this.defenseRatio = defense;
        this.spatkRatio = spatk;
        this.spdefRatio = spdef;
        this.speedRatio = speed;

        calculateStats();
    }

    protected void rangeCheck(int val, String name) {
        if (val < 0 || val > STAT_MAX) {
            throw new IllegalArgumentException(name + " must be between 0-" + STAT_MAX + " Was: " + val);
        }
    }

    public BaseStats(BaseStats original) {
        this.bst = original.bst;

        this.hpRatio = original.hpRatio;
        this.attackRatio = original.attackRatio;
        this.defenseRatio = original.defenseRatio;
        this.spatkRatio = original.spatkRatio;
        this.spdefRatio = original.spdefRatio;
        this.speedRatio = original.speedRatio;

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

    /**
     * Gets the Base Stat Total.
     */
    public int getBST() {
        return hp + attack + defense + spatk + spdef + speed;
    }

    /**
     * Modifies the stats by setting the BST. The ratios of each stat will be the same, as far as possible.<br>
     * <code>bst</code> must be positive.
     */
    public void setBST(int bst) {
        if (bst < 0) {
            throw new IllegalArgumentException("bst must be positive. Was: " + bst);
        }
        this.bst = bst;
        calculateStats();
    }

    /**
     * Modifies the stats by setting the ratios of each stat. The BST is guaranteed to be untouched.<br>
     * All arguments must be positive.
     */
    public void setStatRatios(double hp, double attack, double defense, double spatk, double spdef, double speed) {
        positiveCheck(hp, "hp");
        positiveCheck(attack, "attack");
        positiveCheck(defense, "defense");
        positiveCheck(spatk, "spatk");
        positiveCheck(spdef, "spdef");
        positiveCheck(speed, "speed");
        this.hpRatio = hp;
        this.attackRatio = attack;
        this.defenseRatio = defense;
        this.spatkRatio = spatk;
        this.spdefRatio = spdef;
        this.speedRatio = speed;
        calculateStats();
    }

    protected void positiveCheck(double val, String name) {
        if (bst < 0) {
            throw new IllegalArgumentException(name + " must be positive. Was: " + val);
        }
    }

    /**
     * Doesn't actually return attack/spatk. Returns attack/(attack + spatk).
     */
    public double getAttackSpecialAttackRatio() {
        return (double)attack / ((double)attack + (double)spatk);
    }

    /**
     * Calculates and sets the integer stats returned by the getters - e.g. {@link #getAttack()} -
     * using the BST and stat ratios held by this object. This does not change the BST nor the stat ratios.
     * <br><br>
     * BST takes priority in this calculation, and is always matched. The integer stats always add
     * up to the BST. In addition, each stat is capped at 255.
     * This has the consequence that stat ratios cannot always be followed.<br>
     * <b>As an example</b>, Blissey has the stats 255/10/10/75/135/55. Its BST is 540.
     * If we were to increase its BST, no further points could be put into its HP.
     * Instead, this method would distribute the points among the rest of its stats,
     * according to their ratios. Blissey's (new) BST and the stat cap would be respected,
     * but its HP would be <i>relatively</i> lower.
     * <br><br>
     * Stats can be calculated to be 0, but not negative.
     */
    private void calculateStats() {
        double[] raw = calculateRawStats();
        int[] stats = calculateIntStatsWithinBounds(raw);
        alignStatsWithBST(stats, raw);
        assignCalculatedStats(stats);
    }

    private double[] calculateRawStats() {
        double total = hpRatio + attackRatio + defenseRatio + spatkRatio + spdefRatio + speedRatio;
        return new double[]{
                bst * (hpRatio / total),
                bst * (attackRatio / total),
                bst * (defenseRatio / total),
                bst * (spatkRatio / total),
                bst * (spdefRatio / total),
                bst * (speedRatio / total)
        };
    }

    private int[] calculateIntStatsWithinBounds(double[] raw) {
        int[] stats = new int[6];
        for (int i = 0; i < 6; i++) {
            stats[i] = Math.clamp((int) Math.floor(raw[i]), 0, STAT_MAX);
        }
        return stats;
    }

    private void alignStatsWithBST(int[] stats, double[] raw) {
        int sum = Arrays.stream(stats).sum();
        int diff = bst - sum;

        while (diff != 0) {
            if (diff > 0) {
                addToStat(stats, raw);
                diff--;
            } else {
                subtractFromStat(stats);
                diff++;
            }
        }
    }

    // TODO: the add/subtract methods below are likely to change.
    //  Used genAI to get a sketch (not directly committed), and these are the non-obvious parts.

    private void addToStat(int[] stats, double[] raw) {
        // adds to stat with highest fraction
        int best = -1;
        double bestFrac = -1;
        for (int i = 0; i < 6; i++) {
            if (stats[i] < STAT_MAX) {
                double frac = raw[i] - Math.floor(raw[i]);
                if (frac > bestFrac) {
                    bestFrac = frac;
                    best = i;
                }
            }
        }
        if (best == -1) {
            throw new IllegalStateException("Cannot increase stats without exceeding " + STAT_MAX);
        }
        stats[best]++;
    }

    private void subtractFromStat(int[] stats) {
        // subtracts from stat with highest value
        int best = -1;
        int bestVal = -1;
        for (int i = 0; i < 6; i++) {
            if (stats[i] > bestVal) {
                bestVal = stats[i];
                best = i;
            }
        }
        stats[best]--;
    }

    protected void assignCalculatedStats(int[] stats) {
        hp = stats[0];
        attack = stats[1];
        defense = stats[2];
        spatk = stats[3];
        spdef = stats[4];
        speed = stats[5];
    }

    @Override
    public String toString() {
        // designed to easily fit into Species toString()
        return "hp=" + getHp() + ", attack=" + getAttack() + ", defense=" + getDefense() + ", spatk=" + getSpatk()
                + ", spdef=" + getSpdef() + ", speed=" + getSpeed();
    }
}
