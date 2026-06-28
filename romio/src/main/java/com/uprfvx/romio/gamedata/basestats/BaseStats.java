package com.uprfvx.romio.gamedata.basestats;

import com.uprfvx.romio.gamedata.Species;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * The base stats of a {@link Species}. HP, attack, etc...
 */
public class BaseStats {
    
    public static final int STAT_MAX = 255;

    protected int bst;

    protected double hpRatio;
    protected double attackRatio;
    protected double defenseRatio;
    protected double spatkRatio;
    protected double spdefRatio;
    protected double speedRatio;

    protected int hp;
    protected int attack;
    protected int defense;
    protected int spatk;
    protected int spdef;
    protected int speed;

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
        if (val < 0) {
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
     * up to the BST. In addition, each stat is capped at 255, and cannot be negative.
     * This has the consequence that stat ratios cannot always be followed.<br>
     * <b>As an example</b>, Blissey has the stats 255/10/10/75/135/55. Its BST is 540.
     * If we were to increase its BST, no further points could be put into its HP.
     * Instead, this method would distribute the points among the rest of its stats,
     * according to their ratios. Blissey's (new) BST and the stat cap would be respected,
     * but its HP would be <i>relatively</i> lower.
     * <br><br>
     * Stat ratios of 0 will always lead to a stat of 0. This, in addition to the stat cap,
     * means that not all stat ratios+BSTs will be possible. In these cases, an {@link IllegalStateException}
     * will be thrown. This also happens if BST > 255 * 6.
     * <br><br>
     * Most parts of this algorithm is written to be agnostic to exactly which the stats are.<br>
     * When implementing a subclass, all that needs to be overwritten are the {@link #getStatRatios()}
     * and {@link #assignCalculatedStats(int[])} methods.
     * @throws IllegalStateException when all stat ratios are 0, but BST is not.
     * @throws IllegalStateException when the stats cannot be assigned according to the stat ratios + BST.
     */
    protected void calculateStats() {
        double[] raw = calculateRawStats();
        int[] stats = calculateIntStatsWithinBounds(raw);
        assignCalculatedStats(stats);
    }

    /**
     * Calculates idealized "raw" stats, which follow the stat ratios and bst but <br>
     * Assumes the stat ratios and bst are non-negative.
     */
    private double[] calculateRawStats() {
        double[] ratios = getStatRatios();
        double total = Arrays.stream(ratios).sum();
        if (total == 0) {
            if (bst != 0) {
                throw new IllegalStateException("All stat ratios are 0, but BST is not 0.");
            }
            // having 0 in all stats is fine if bst also is, but we need to avoid the coming division-by-zero
            return new double[]{0, 0, 0, 0, 0, 0};
        }
        return Arrays.stream(ratios).map(ratio -> bst * (ratio / total)).toArray();
    }

    protected double[] getStatRatios() {
        return new double[]{hpRatio, attackRatio, defenseRatio, spatkRatio, spdefRatio, speedRatio};
    }

    private int[] calculateIntStatsWithinBounds(double[] raw) {
        int[] stats = new int[raw.length];

        PriorityQueue<Integer> statNeedinessQueue = new PriorityQueue<>(raw.length, Comparator.comparingDouble(
                // + 0.000001 ensures higher raw stats get priority before any points have been handed out.
                // Without it, it simplifies to -raw[statOff]/raw[statOff] = -1 for all stats,
                // and the order becomes essentially random (really based on stat order).
                statOff -> (stats[statOff] - raw[statOff] + 0.000001) / raw[statOff]
        ));
        for (int statOff = 0; statOff < raw.length; statOff++) {
            if (raw[statOff] != 0) { // stats with ratio 0 should not get any points
                statNeedinessQueue.add(statOff);
            }
        }

        for (int i = 0; i < bst; i++) {
            Integer statOff = statNeedinessQueue.poll();
            if (statOff == null) {
                throw new IllegalStateException("Could not distribute all stat points."
                        + "distributed=" + Arrays.toString(stats) + " left=" + (bst - i));
            }
            stats[statOff]++;
            if (stats[statOff] < STAT_MAX) {
                statNeedinessQueue.add(statOff);
            }
        }
        return stats;
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
