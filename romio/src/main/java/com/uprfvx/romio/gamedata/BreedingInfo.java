package com.uprfvx.romio.gamedata;

public class BreedingInfo {

    // these aren't official terms, but ones chosen since their behavior code-wise mirrors the species types
    private EggGroup primaryEggGroup;
    private EggGroup secondaryEggGroup;

    private int eggCycles;

    public BreedingInfo(EggGroup primaryEggGroup, EggGroup secondaryEggGroup, int eggCycles) {
        if (eggCycles < 0) {
            throw new IllegalArgumentException("eggCycles must be >=0");
        }
        this.primaryEggGroup = primaryEggGroup;
        this.secondaryEggGroup = primaryEggGroup == secondaryEggGroup ? null : secondaryEggGroup;
        this.eggCycles = eggCycles;
    }

    public BreedingInfo(BreedingInfo original) {
        this.primaryEggGroup = original.primaryEggGroup;
        this.secondaryEggGroup = original.secondaryEggGroup;
        this.eggCycles = original.eggCycles;
    }

    public EggGroup getPrimaryEggGroup() {
        return primaryEggGroup;
    }

    public void setPrimaryEggGroup(EggGroup primaryEggGroup) {
        this.primaryEggGroup = primaryEggGroup;
    }

    public EggGroup getSecondaryEggGroup() {
        return secondaryEggGroup;
    }

    public void setSecondaryEggGroup(EggGroup secondaryEggGroup) {
        this.secondaryEggGroup = secondaryEggGroup;
    }

    public int getEggCycles() {
        return eggCycles;
    }

    public void setEggCycles(int eggCycles) {
        if (eggCycles < 0) {
            throw new IllegalArgumentException("eggCycles must be >=0");
        }
        this.eggCycles = eggCycles;
    }
}
