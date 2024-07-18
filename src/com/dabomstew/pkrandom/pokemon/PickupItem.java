package com.dabomstew.pkrandom.pokemon;

import java.util.Arrays;

public class PickupItem {
    public static final int PROBABILITY_SLOTS = 10;

    private final Item item;
    private final int[] probabilities = new int[PROBABILITY_SLOTS];

    public PickupItem(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public int[] getProbabilities() {
        return probabilities;
    }

    @Override
    public String toString() {
        return "PickupItem[" + item + ", probs=" + Arrays.toString(probabilities) + "]";
    }
}
