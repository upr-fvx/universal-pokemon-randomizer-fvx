package com.uprfvx.romio.gamedata;

public class BreedingInfo {

    private EggGroup primaryEggGroup;
    private EggGroup secondaryEggGroup;

    private int eggCycles;

    public BreedingInfo(EggGroup eggGroup1, EggGroup eggGroup2, int eggCycles) {
        if (eggCycles < 0) {
            throw new IllegalArgumentException("eggCycles must be >=0");
        }
        this.primaryEggGroup = eggGroup1;
        this.secondaryEggGroup = eggGroup1 == eggGroup2 ? null : eggGroup2;
        this.eggCycles = eggCycles;
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
