package com.dabomstew.pkromio.gamedata;

import java.util.Arrays;
import java.util.Objects;

public class PickupItem {
    public static final int PROBABILITY_SLOTS = 10;

    private final Item item;
    private final int[] probabilities = new int[PROBABILITY_SLOTS];

    public PickupItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        this.item = item;
    }

    public PickupItem(PickupItem original) {
        this.item = original.item;
        System.arraycopy(original.probabilities, 0, probabilities, 0, PROBABILITY_SLOTS);
    }

    public Item getItem() {
        return item;
    }

    public int[] getProbabilities() {
        return probabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PickupItem) {
            PickupItem other = (PickupItem) o;
            return item.equals(other.item) && Arrays.equals(probabilities, other.probabilities);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, Arrays.hashCode(probabilities));
    }

    @Override
    public String toString() {
        return "PickupItem[" + item + ", probs=" + Arrays.toString(probabilities) + "]";
    }
}
