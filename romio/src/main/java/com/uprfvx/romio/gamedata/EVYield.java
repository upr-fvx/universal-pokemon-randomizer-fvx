package com.uprfvx.romio.gamedata;

public class EVYield {

    // It is not obvious that fromWord()/toWord() should be here, and not in say AbstractRomHandler.
    // At some point it might be nice to split out the whole gamedata model from romio;
    // at which point decisions about things like these need to be made.

    /**
     * Reads an EVYield from a 2-byte int/word.<br>
     * This is the format used in all mainline Pokémon games with EVs.
     */
    public static EVYield fromWord(int word) {
        int hp = word & 0b11;
        int attack = (word >> 2) & 0b11;
        int defense = (word >> 4) & 0b11;
        int speed = (word >> 6) & 0b11;
        int spatk = (word >> 8) & 0b11;
        int spdef = (word >> 10) & 0b11;
        return new EVYield(hp, attack, defense, spatk, spdef, speed);
    }

    private int hp;
    private int attack;
    private int defense;
    private int spatk;
    private int spdef;
    private int speed;

    public EVYield(int hp, int attack, int defense, int spatk, int spdef, int speed) {
        setHP(hp);
        setAttack(attack);
        setDefense(defense);
        setSpatk(spatk);
        setSpdef(spdef);
        setSpeed(speed);
    }

    public EVYield(EVYield original) {
        this.hp = original.hp;
        this.attack = original.attack;
        this.defense = original.defense;
        this.spatk = original.spatk;
        this.spdef = original.spdef;
        this.speed = original.speed;
    }

    private void boundsCheck(int stat) {
        if (stat < 0 || stat > 3) {
            throw new IllegalArgumentException("EV yield for any stat must be between 0-3. Was: " + stat);
        }
    }

    public void setHP(int hp) {
        boundsCheck(hp);
        this.hp = hp;
    }

    public int getHP() {
        return hp;
    }

    public void setAttack(int attack) {
        boundsCheck(attack);
        this.attack = attack;
    }

    public int getAttack() {
        return attack;
    }

    public void setDefense(int defense) {
        boundsCheck(defense);
        this.defense = defense;
    }

    public int getDefense() {
        return defense;
    }

    public void setSpatk(int spatk) {
        boundsCheck(spatk);
        this.spatk = spatk;
    }

    public int getSpatk() {
        return spatk;
    }

    public void setSpdef(int spdef) {
        boundsCheck(spdef);
        this.spdef = spdef;
    }

    public int getSpdef() {
        return spdef;
    }

    public void setSpeed(int speed) {
        boundsCheck(speed);
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    /**
     * Transforms this EVYield into a 2-byte int/word.<br>
     * This is the format used in all mainline Pokémon games with EVs.
     */
    public int toWord() {
        return (getSpdef() << 10) | (getSpatk() << 8) | (getSpeed() << 6)| (getDefense() << 4) | (getAttack() << 2) | getHP();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EVYield that)) return false;
        return hp == that.hp && attack == that.attack && defense == that.defense && spatk == that.spatk
                && spdef == that.spdef && speed == that.speed;
    }

    @Override
    public String toString() {
        return String.format("EVYield{%d/%d/%d/%d/%d/%d}", hp, attack, defense, spatk, spdef, speed);
    }
}
