package com.dabomstew.pkrandom.random;

import java.security.SecureRandom;

public class SeedPicker {

    // The purpose of this class is to hold this single method.
    // I honestly don't know how it works or whether it is better
    // than standard implementations of picking a seed in, say,
    // Random, or Math.random().
    // However, it's from older code used in the
    // Universal Pokemon Randomizer, and risking breaking stuff
    // just because I don't get it seems unwise.
    public static long pickSeed() {
        long value = 0;
        byte[] by = SecureRandom.getSeed(6);
        for (int i = 0; i < by.length; i++) {
            value |= ((long) by[i] & 0xffL) << (8 * i);
        }
        return value;
    }

}
