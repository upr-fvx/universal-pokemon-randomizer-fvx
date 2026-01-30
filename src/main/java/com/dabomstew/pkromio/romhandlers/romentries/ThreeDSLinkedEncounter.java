package com.dabomstew.pkromio.romhandlers.romentries;

public class ThreeDSLinkedEncounter {
    private final int base;
    private final int linked;

    public ThreeDSLinkedEncounter(int base, int linked) {
        this.base = base;
        this.linked = linked;
    }

    public int getBase() {
        return base;
    }

    public int getLinked() {
        return linked;
    }
}
