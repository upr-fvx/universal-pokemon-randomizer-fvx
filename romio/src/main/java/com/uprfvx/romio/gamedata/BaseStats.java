package com.uprfvx.romio.gamedata;

/**
 * The base stats of a {@link Species}. HP, attack, etc...
 */
public class BaseStats {

    private int hp;
    private int attack;
    private int defense;
    private int spatk;
    private int spdef;
    private int speed;

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

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpatk() {
        return spatk;
    }

    public void setSpatk(int spatk) {
        this.spatk = spatk;
    }

    public int getSpdef() {
        return spdef;
    }

    public void setSpdef(int spdef) {
        this.spdef = spdef;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
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

    public int getBSTForPowerLevels() {
        // Take into account Shedinja's purposefully nerfed HP
        if (isShedinja) {
            return (attack + defense + spatk + spdef + speed) * 6 / 5;
        } else {
            return getBST();
        }
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
